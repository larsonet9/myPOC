/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.cdc.cdsi.testcase;

import gov.cdc.cdsi.db.DBHelper;
import gov.cdc.cdsi.engine.CDSiScenario;
import gov.cdc.cdsi.engine.CDSiScenario.ScenarioImmunization;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author eric
 */
public class TestCaseData {

  public static CDSiScenario getTestCase(String testCaseId) throws Exception {
    CDSiScenario scenario = new CDSiScenario();

    String SQL = "select * from cdsi_test_case where cdsi_test_id = '" + testCaseId + "'";
    Statement stmt = DBHelper.dbConn().createStatement();

    ResultSet rs = stmt.executeQuery(SQL);

    if(rs.next())
    {
      // Basic Test Case and Patient Information
      scenario.setTestCaseId(testCaseId);
      scenario.setTestCaseName(rs.getString("TEST_CASE_NAME"));
      scenario.setDob(rs.getDate("DOB"));
      scenario.setGender(rs.getString("GENDER"));
      scenario.setVaccineGroupId(getVaccineGroupId(rs.getString("VACCINE_GROUP")));
      scenario.setVaccineGroupName(rs.getString("VACCINE_GROUP"));
      scenario.setAssessmentDate(rs.getDate("ASSESSMENT_DATE"));

      // Medical History pulled from test case into Scenario
      scenario.addMedicalHistory(rs.getString("MED_HISTORY_CODE"), rs.getString("MED_HISTORY_CODE_SYS"));

      // Vaccine Doses Administered - Up to 7 of them.
      Date dateAdmin = null;
      for(int i = 1; i <= 7; i++)
      {
        dateAdmin = null;
        dateAdmin = rs.getDate("DATE_ADMIN_"+i);
        if(dateAdmin != null) {
          ScenarioImmunization si = scenario.new ScenarioImmunization();
          si.setDateAdministered(dateAdmin);
          si.setProduct(rs.getString("VAC_NAME_"+i));
          si.setCvx(rs.getString("CVX_"+i));
          si.setMvx(rs.getString("MVX_"+i));
          si.setExpectedEvalStatus(rs.getString("EVAL_STATUS_"+i));
          si.setExpectedEvalReason(rs.getString("EVAL_REASON_"+i));
          si.setDoseId(i);
          si.setVaccineId(getVaccineId(si.getCvx()));
          si.setManufacturerId(getManufacturerId(si.getMvx()));
          scenario.addVaccineDoseAdministered(si);
        }
      }
    }
    else
      throw new Exception("Test Case ID - " + testCaseId + " was not found." );

    DBHelper.disconnect(null, stmt, rs);
    return scenario;

  }

  public static List<String> getAllTestCasesByVaccineGroup(String vaccineGroupId) throws Exception {
    String vg = getVaccineGroupShortName(vaccineGroupId);

    List<String> tcList = new ArrayList();

    String SQL = "select cdsi_test_id from cdsi_test_case where vaccine_group = '" + vg + "' order by cdsi_test_id";
    Statement stmt = DBHelper.dbConn().createStatement();

    ResultSet rs = stmt.executeQuery(SQL);

    if(rs.next()) {
      do {
        tcList.add(rs.getString(1));
      }  while(rs.next());
    }
    else
      throw new Exception("Unable to find Test Cases for Vaccine Group" + vg );

    DBHelper.disconnect(null, stmt, rs);

    return tcList;
  }


  private static int getVaccineId(String cvx) throws Exception {
    int vaccId = 0;
    String SQL = "select vaccine_id from vaccine where cvx = '" + cvx + "'";
    Statement stmt = DBHelper.dbConn().createStatement();

    ResultSet rs = stmt.executeQuery(SQL);

    if(rs.next())
      vaccId = rs.getInt(1);
    else
      throw new Exception("Unable to locate vaccineId for CVX Code " + cvx );

    DBHelper.disconnect(null, stmt, rs);

    return vaccId;
  }

  private static int getManufacturerId(String mvx) throws Exception {
    int vaccId = 0;

    if(mvx == null || mvx.isEmpty())
      mvx = "UNK";

    String SQL = "select manufacturer_id from manufacturer where mvx = '" + mvx + "'";
    Statement stmt = DBHelper.dbConn().createStatement();

    ResultSet rs = stmt.executeQuery(SQL);

    if(rs.next())
      vaccId = rs.getInt(1);
    else
      throw new Exception("Unable to locate manufacturerId for MVX Code " + mvx );

    DBHelper.disconnect(null, stmt, rs);

    return vaccId;
  }

  private static int getVaccineGroupId(String vaccineGroup) throws Exception {
    int vaccGroupId = 0;

    String SQL = "select vaccine_group_id from vaccine_group where UPPER(test_case_name) like  '%" + vaccineGroup.toUpperCase() + "%'";
    Statement stmt = DBHelper.dbConn().createStatement();

    ResultSet rs = stmt.executeQuery(SQL);

    if(rs.next())
      vaccGroupId = rs.getInt(1);
    else
      throw new Exception("Unable to locate vaccineGroupId for Vaccine Group " + vaccineGroup );

    DBHelper.disconnect(null, stmt, rs);

    return vaccGroupId;
  }

  private static String getVaccineGroupShortName(String vaccineGroupId) throws Exception {
    String shortName = "";

    String SQL = "select test_case_name from vaccine_group where vaccine_group_id = " + vaccineGroupId;
    Statement stmt = DBHelper.dbConn().createStatement();

    ResultSet rs = stmt.executeQuery(SQL);

    if(rs.next())
      shortName = rs.getString(1);
    else
      throw new Exception("Unable to locate Vaccine Group for ID " + vaccineGroupId );

    DBHelper.disconnect(null, stmt, rs);

    return shortName;

  }

}
