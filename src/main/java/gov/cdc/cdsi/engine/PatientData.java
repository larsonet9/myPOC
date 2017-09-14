/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.cdc.cdsi.engine;

import gov.cdc.cdsi.db.DBHelper;
import gov.cdc.cdsi.engine.CDSiPatientData.AntigenAdministered;
import gov.cdc.cdsi.engine.CDSiScenario.ScenarioImmunization;
import gov.cdc.cdsi.engine.CDSiScenario.ScenarioMedHistory;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.List;

/**
 *
 * @author eric
 */
public class PatientData {

  public static CDSiPatientData organizeImmunizationHistory(CDSiScenario scenario) throws Exception
  {
    CDSiPatientData result = new CDSiPatientData();

    result.setPatient(scenario.getDob(), scenario.getGender());

    for(ScenarioMedHistory medHx : scenario.getMedicalHistory()) {
      if(medHx.hasVaccineGroupSpecificCondition())
        result.addMedicalHistory(medHx.getMedHistoryCode(), medHx.getMedHistoryCodeSys(), medHx.getVaccineGroupId());
      else
        result.addMedicalHistory(medHx.getMedHistoryCode(), medHx.getMedHistoryCodeSys());
    }

    // Make sure doses were administered
    List<ScenarioImmunization> simms = scenario.getVaccineDosesAdministered();
    if(simms == null || simms.isEmpty())
      return result;

    // Connect to DB.
    Statement  stmt = DBHelper.dbConn().createStatement();
    ResultSet  rs   = null;

    // Loop through each Vaccine Dose Administered
    int i = 1;
    String sql = "";
    for(ScenarioImmunization si : simms)
    {
      if(si.getVaccineId() > 0 && (si.getManufacturerId() > 0 && si.getMvx() != null && !si.getMvx().equalsIgnoreCase("UNK"))) {
        sql =
        "Select vm.antigen_id, vm.trade_name, v.cvx, m.mvx, vm.association_begin_age, vm.association_end_age " +
        "  from vaccine_makeup vm, vaccine v, manufacturer m " +
        " where vm.vaccine_id      = v.vaccine_id " +
        "   and vm.manufacturer_id = m.manufacturer_id " +
        "   and vm.vaccine_id      = " + si.getVaccineId() +
        "   and vm.manufacturer_id = " + si.getManufacturerId();
      }
      else {
        sql =
        "Select distinct vm.antigen_id, v.short_name, v.cvx, 'UNK', vm.association_begin_age, vm.association_end_age " +
        "  from vaccine_makeup vm, vaccine v, manufacturer m " +
        " where vm.vaccine_id      = v.vaccine_id " +
        "   and vm.manufacturer_id = m.manufacturer_id " +
        "   and vm.vaccine_id      = " + si.getVaccineId();
      }
      rs = stmt.executeQuery(sql);
      //loop through each Antigen in this Vaccine Dose Administered
      while(rs.next())
      {
        String beginAge = rs.getString(5);
        String endAge   = rs.getString(6);
        Date beginAgeDate = CDSiDate.calculateDate(scenario.getDob(), beginAge, "01/01/1900");
        Date endAgeDate   = CDSiDate.calculateDate(scenario.getDob(), endAge, "12/31/2999");
        
        if(si.getDateAdministered().compareTo(beginAgeDate) >= 0 && 
           si.getDateAdministered().before(endAgeDate)) {
          AntigenAdministered aa = result.new AntigenAdministered();
          aa.setChronologicalPosition(i);
          aa.setDoseId(si.getDoseId());
          aa.setDateAdministered(si.getDateAdministered());
          aa.setManufacturerId(si.getManufacturerId());
          aa.setVaccineId(si.getVaccineId());
          aa.setAntigenId(rs.getInt(1));
          aa.setTradeName(rs.getString(2));
          aa.setCvx(rs.getString(3));
          aa.setMvx(rs.getString(4));
          aa.setExpectedEvalStatus(si.getExpectedEvalStatus());
          aa.setExpectedEvalReason(si.getExpectedEvalReason());
          result.addAntigenAdministered(aa);
        }
      }
      rs.close();
      i++;
    }

    DBHelper.disconnect(null, stmt, rs);
    return result;
  }

}
