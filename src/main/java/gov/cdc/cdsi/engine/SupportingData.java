/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.cdc.cdsi.engine;

import gov.cdc.cdsi.db.DBHelper;
import gov.cdc.cdsi.engine.CDSiPatientSeries.TargetDose;
import gov.cdc.util.CloneSerializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author eric
 */
public class SupportingData {

  public static List<CDSiPatientSeries> getPatientSeries(int vaccineGroupId, CDSiPatientData patientData, Date assessmentDate) throws Exception
  {
    List<CDSiPatientSeries> psList = new ArrayList();
    
    String SQL;
    if(vaccineGroupId != 0) // One Vaccine Group
      SQL = "select series_id, antigen_id, series_name, default_series_ind, product_path_ind, series_preference, max_age_to_start, vaccine_group_id, sd_version, upload_date from sd_series where vaccine_group_id = " + vaccineGroupId + " order by series_id ";
    else // All Vaccine Groups
      SQL = "select series_id, antigen_id, series_name, default_series_ind, product_path_ind, series_preference, max_age_to_start, vaccine_group_id, sd_version, upload_date from sd_series order by series_id ";
    
    Statement stmt  = DBHelper.dbConn().createStatement();
    ResultSet rs = stmt.executeQuery(SQL);

    String sqlTD = "select dose_id, dose_number from sd_dose where series_id = ? order by dose_number asc";
    PreparedStatement pstmt = DBHelper.dbConn().prepareStatement(sqlTD);

    while(rs.next())
    {
      CDSiPatientSeries ps = new CDSiPatientSeries();
      ps.setSeriesId(rs.getInt(1));
      ps.setAntigenId(rs.getInt(2));
      ps.setSeriesName(rs.getString(3));
      ps.setDefaultSeries((rs.getString(4).equalsIgnoreCase("Y") ? true : false));
      ps.setProductSeries((rs.getString(5).equalsIgnoreCase("Y") ? true : false));
      ps.setSeriesPreference(rs.getInt(6));
      ps.setMaxAgeToStart(rs.getString(7));
      ps.setVaccineGroupId(rs.getInt(8));
      ps.setSdVersion(rs.getString(9));
      ps.setUploadDate(rs.getDate(10));
      ps.setAssessmentDate(assessmentDate);

      pstmt.setInt(1, ps.getSeriesId());
      ResultSet rsTD = pstmt.executeQuery();
      while(rsTD.next())
      {
        TargetDose td = ps.new TargetDose();
        td.setDoseId(rsTD.getInt(1));
        td.setDoseNumber(rsTD.getInt(2));
        ps.addTargetDose(td);
      }
      ps.setPatientData((CDSiPatientData)CloneSerializable.clone(patientData));
      rsTD.close();
      psList.add(ps);
    }

    pstmt.close();
    DBHelper.disconnect(null, stmt, rs);
    return psList;
  }

  
  public static SDAge getAgeData(int doseId) throws Exception {
    SDAge sdAge = null;

    String SQL = "select abs_min_age, min_age, earliest_rec_age, latest_rec_age, max_age from sd_age where dose_id = " + doseId;
    Statement stmt = DBHelper.dbConn().createStatement();

    ResultSet rs = stmt.executeQuery(SQL);

    if(rs.next())
    {
      sdAge = new SDAge();
      sdAge.setAbsoluteMinimumAge(rs.getString(1));
      sdAge.setMinimumAge(rs.getString(2));
      sdAge.setEarliestRecommendedAge(rs.getString(3));
      sdAge.setLatestRecommendedAge(rs.getString(4));
      sdAge.setMaximumAge(rs.getString(5));
    }

    DBHelper.disconnect(null, stmt, rs);
    return sdAge;
  }

  public static List<SDInterval> getIntervalData(int doseId) throws Exception {
    List<SDInterval> intList = null;

    String SQL = "select interval_id, previous_dose_ind, target_dose_num, abs_min_int, min_int, earliest_rec_int, latest_rec_int, priority_flag from sd_interval where dose_id = " + doseId;
    Statement stmt = DBHelper.dbConn().createStatement();

    String sqlCSCVac = "select vaccine_id from sd_interval_fmr_vaccine where interval_id = ?";
    PreparedStatement pstmtIntervalFMR = DBHelper.dbConn().prepareStatement(sqlCSCVac);

    
    ResultSet rs = stmt.executeQuery(SQL);

    if(rs.next())
    {
      intList = new ArrayList();
      do
      {
        SDInterval sdInt = new SDInterval();
        sdInt.setFromPreviousDose((rs.getString(2).equalsIgnoreCase("Y")? true : false));
        sdInt.setFromTargetDoseNubmer(rs.getInt(3));
        sdInt.setAbsoluteMinimumInterval(rs.getString(4));
        sdInt.setMinimumInterval(rs.getString(5));
        sdInt.setEarliestRecommendedInterval(rs.getString(6));
        sdInt.setLatestRecommendedInterval(rs.getString(7));
        sdInt.setPriorityFlag(rs.getString(8));
        
        // Check for interval From Most Recent Data
        pstmtIntervalFMR.setInt(1, rs.getInt(1));
        ResultSet rsIntFMR = pstmtIntervalFMR.executeQuery();
        if(rsIntFMR.next())
        {
          do
          {
            sdInt.addVacId(rsIntFMR.getString(1));
          } while(rsIntFMR.next());
          rsIntFMR.close();
        }
        intList.add(sdInt);
      } while(rs.next());
      rs.close();
    }

    DBHelper.disconnect(null, stmt, rs);
    return intList;
  }

  public static List<SDInterval> getAllowableIntervalData(int doseId) throws Exception {
    List<SDInterval> intList = null;

    String SQL = "select previous_dose_ind, target_dose_num, abs_min_int from sd_allowable_interval where dose_id = " + doseId;
    Statement stmt = DBHelper.dbConn().createStatement();
    ResultSet rs = stmt.executeQuery(SQL);

    if(rs.next())
    {
      intList = new ArrayList();
      do
      {
        SDInterval sdInt = new SDInterval();
        sdInt.setFromPreviousDose((rs.getString(1).equalsIgnoreCase("Y")? true : false));
        sdInt.setFromTargetDoseNubmer(rs.getInt(2));
        sdInt.setAbsoluteMinimumInterval(rs.getString(3));
        intList.add(sdInt);
      } while(rs.next());
    }

    DBHelper.disconnect(null, stmt, rs);
    return intList;
  }

  public static List<SDPreferableVaccine> getPreferableVaccineData(int doseId) throws Exception {
    List<SDPreferableVaccine> pvList = null;

    String SQL = "select vaccine_id, begin_age, end_age, manufacturer_id, volume, forecast_vaccine_type from sd_preferable_vaccine where dose_id = " + doseId;
    Statement stmt = DBHelper.dbConn().createStatement();

    ResultSet rs = stmt.executeQuery(SQL);

    if(rs.next())
    {
      pvList = new ArrayList();
      do
      {
        SDPreferableVaccine pv = new SDPreferableVaccine();
        pv.setVaccineId(rs.getInt(1));
        pv.setBeginAge(rs.getString(2));
        pv.setEndAge(rs.getString(3));
        pv.setManufacturerId(rs.getInt(4));
        pv.setVolume(rs.getDouble(5));
        pv.setForecastVaccineType((rs.getString(6).equalsIgnoreCase("Y")? true : false));
        pvList.add(pv);
      } while(rs.next());
    }

    DBHelper.disconnect(null, stmt, rs);
    return pvList;
  }

  public static List<SDAllowableVaccine> getAllowableVaccineData(int doseId) throws Exception {
    List<SDAllowableVaccine> avList = null;

    String SQL = "select vaccine_id, begin_age, end_age from sd_allowable_vaccine where dose_id = " + doseId;
    Statement stmt = DBHelper.dbConn().createStatement();

    ResultSet rs = stmt.executeQuery(SQL);

    if(rs.next())
    {
      avList = new ArrayList();
      do
      {
        SDAllowableVaccine av = new SDAllowableVaccine();
        av.setVaccineId(rs.getInt(1));
        av.setBeginAge(rs.getString(2));
        av.setEndAge(rs.getString(3));
        avList.add(av);
      } while(rs.next());
    }

    DBHelper.disconnect(null, stmt, rs);
    return avList;
  }

  public static List<SDInadvertentVaccine> getInadvertentVaccineData(int doseId) throws Exception {
    List<SDInadvertentVaccine> ivList = null;

    String SQL = "select vaccine_id from sd_inadvertent_vaccine where dose_id = " + doseId;
    Statement stmt = DBHelper.dbConn().createStatement();

    ResultSet rs = stmt.executeQuery(SQL);

    if(rs.next())
    {
      ivList = new ArrayList();
      do
      {
        SDInadvertentVaccine iv = new SDInadvertentVaccine();
        iv.setVaccineId(rs.getInt(1));
        ivList.add(iv);
      } while(rs.next());
    }

    DBHelper.disconnect(null, stmt, rs);
    return ivList;
  }

  public static SDSkipTargetDose getSkipTargetDoseData(int doseId) throws Exception {
    SDSkipTargetDose sdSkip = null;

    String SQL = "select trigger_age, trigger_int, trigger_target_dose, trigger_doses_administered from sd_skip_dose where dose_id = " + doseId;
    Statement stmt = DBHelper.dbConn().createStatement();

    ResultSet rs = stmt.executeQuery(SQL);

    if(rs.next())
    {
      sdSkip = new SDSkipTargetDose();
      sdSkip.setTriggerAge(rs.getString(1));
      sdSkip.setTriggerInterval(rs.getString(2));
      sdSkip.setTriggerTargetDose(rs.getInt(3));
      sdSkip.setTriggerDosesAdministered(rs.getInt(4));
    }

    DBHelper.disconnect(null, stmt, rs);
    return sdSkip;
  }

  public static List<SDConditionalNeed> getConditionalNeed(int doseId) throws Exception {
    List<SDConditionalNeed> cnList = null;

    String SQL = "select conditional_set_id from sd_conditional_set where dose_id = " + doseId;
    Statement stmt = DBHelper.dbConn().createStatement();
    ResultSet rs = stmt.executeQuery(SQL);

    String sqlCSDetail = "select cs_detail_id, start_date, end_date, dose_count, begin_age, end_age from sd_cs_detail where conditional_set_id = ?";
    PreparedStatement pstmtCSD = DBHelper.dbConn().prepareStatement(sqlCSDetail);

    String sqlCSDVaccine = "select vaccine_id from sd_csd_vaccine where cs_detail_id = ?";
    PreparedStatement pstmtCSDV = DBHelper.dbConn().prepareStatement(sqlCSDVaccine);

    if(rs.next())
    {
      cnList = new ArrayList();
      do
      {
        SDConditionalNeed cn = new SDConditionalNeed();
        pstmtCSD.setInt(1, rs.getInt(1));
        ResultSet rsCSD = pstmtCSD.executeQuery();
        while(rsCSD.next())
        {
          SDConditionalSet cs = new SDConditionalSet();
          cs.setStartDate(rsCSD.getDate(2));
          cs.setEndDate(rsCSD.getDate(3));
          cs.setDoseCount(rsCSD.getInt(4));
          cs.setBeginAge(rsCSD.getString(5));
          cs.setEndAge(rsCSD.getString(6));

          // Get all of the internal VaccineIds
          pstmtCSDV.setInt(1, rsCSD.getInt(1));
          ResultSet rsCSDV = pstmtCSDV.executeQuery();
          while(rsCSDV.next())
          {
            cs.addVacId(rsCSDV.getString(1));
          }

          rsCSDV.close();
          cn.addConditionalSet(cs);
        }

        rsCSD.close();
        cnList.add(cn);
      } while(rs.next());
      pstmtCSD.close();
      pstmtCSDV.close();
    }

    DBHelper.disconnect(null, stmt, rs);
    return cnList;

  }
  
  public static List<SDSubstituteTargetDose> getSubstituteTargetDoseData(int doseId) throws Exception {
    List<SDSubstituteTargetDose> subList = null;

    String SQL = "select begin_age, end_age, dose_count, doses_to_substitute from sd_substitute_dose where dose_id = " + doseId;
    Statement stmt = DBHelper.dbConn().createStatement();
    ResultSet rs = stmt.executeQuery(SQL);

    if(rs.next())
    {
      subList = new ArrayList();
      do
      {
        SDSubstituteTargetDose sdSub = new SDSubstituteTargetDose();
        sdSub.setFirstDoseBeginAge(rs.getString(1));
        sdSub.setFirstDoseEndAge(rs.getString(2));
        sdSub.setCountOfValidDoses(rs.getInt(3));
        sdSub.setDosesToSubstitute(rs.getInt(4));
        subList.add(sdSub);
      } while(rs.next());
    }

    DBHelper.disconnect(null, stmt, rs);
    return subList;
  }

  public static SDGender getGenderData(int doseId) throws Exception {
    SDGender sdGender = null;

    String SQL = "select required_gender from sd_gender where dose_id = " + doseId;
    Statement stmt = DBHelper.dbConn().createStatement();

    ResultSet rs = stmt.executeQuery(SQL);

    if(rs.next())
    {
      sdGender = new SDGender();
      do
      {
        sdGender.addRequiredGender(rs.getString(1));
      } while(rs.next());
    }

    DBHelper.disconnect(null, stmt, rs);
    return sdGender;
  }

  public static List<SDLiveVirusConflict> getLiveVirusConflicts(int scheduleId) throws Exception {
    List<SDLiveVirusConflict> lvcList = null;

    String SQL = "select previous_vaccine_id, current_vaccine_id, begin_interval, min_end_interval, end_interval from sd_conflict where schedule_id = " + scheduleId;
    Statement stmt = DBHelper.dbConn().createStatement();

    ResultSet rs = stmt.executeQuery(SQL);

    if(rs.next())
    {
      lvcList = new ArrayList();
      do
      {
        SDLiveVirusConflict lvc = new SDLiveVirusConflict();
        lvc.setPreviousVaccineId(rs.getInt(1));
        lvc.setCurrentVaccineId(rs.getInt(2));
        lvc.setBeginInterval(rs.getString(3));
        lvc.setMinimumEndInterval(rs.getString(4));
        lvc.setEndInterval(rs.getString(5));
        lvcList.add(lvc);
      } while(rs.next());
    }

    DBHelper.disconnect(null, stmt, rs);
    return lvcList;
  }

  public static boolean isRecurringTargetDose(int doseId) throws Exception {
    String SQL = "select recurring_ind from sd_recurring_dose where dose_id = " + doseId;
    boolean isRecurring = false;

    Statement stmt = DBHelper.dbConn().createStatement();
    ResultSet rs   = stmt.executeQuery(SQL);

    if(rs.next())
    {
      isRecurring = rs.getString(1).equalsIgnoreCase("Y")? true : false;

    }

    DBHelper.disconnect(null, stmt, rs);
    return isRecurring;
  }

  public static SDSeasonalRecommendation getSeasonalRecommendationData(int doseId) throws Exception {
    SDSeasonalRecommendation sdSeason = null;

    String SQL = "select start_date, end_date from sd_season_recommendation where dose_id = " + doseId;
    Statement stmt = DBHelper.dbConn().createStatement();

    ResultSet rs = stmt.executeQuery(SQL);

    if(rs.next())
    {
      sdSeason = new SDSeasonalRecommendation();
      sdSeason.setStartDate(rs.getDate(1));
      sdSeason.setEndDate(rs.getDate(2));
 
    }

    DBHelper.disconnect(null, stmt, rs);
    return sdSeason;
  }
  
  public static boolean isAdministerFullVaccineGroup(int vaccineGroupId) throws Exception {
    String SQL = "select administer_full_vg_ind from vaccine_group where vaccine_group_id = " + vaccineGroupId;
    boolean isFull = false;

    Statement stmt = DBHelper.dbConn().createStatement();
    ResultSet rs   = stmt.executeQuery(SQL);

    if(rs.next())
    {
      isFull = rs.getString(1).equalsIgnoreCase("Y")? true : false;

    }

    DBHelper.disconnect(null, stmt, rs);
    return isFull;
  }

  public static String getAntigenName(int antigenId) throws Exception {
    String SQL = "select antigen_name from antigen where antigen_id = " + antigenId;
    String antigenName = "NOT FOUND IN DB";

    Statement stmt = DBHelper.dbConn().createStatement();
    ResultSet rs   = stmt.executeQuery(SQL);

    if(rs.next())
    {
      antigenName = rs.getString(1);

    }

    DBHelper.disconnect(null, stmt, rs);
    return antigenName;

  }

  public static List<SDContraindication> getContraindicationsForAntigen(int antigenId) throws Exception {
    List<SDContraindication> contraList = null;
    String SQL = "select distinct "
            + "          phin_vads_code, snomed_ct_code "
            + "     from sd_contraindication sdc, patient_observation po"
            + "    where sdc.patient_observation_id = po.patient_observation_id "
            + "      and sdc.antigen_id = " + antigenId;

    Statement stmt = DBHelper.dbConn().createStatement();
    ResultSet rs   = stmt.executeQuery(SQL);

    if(rs.next())
    {
      contraList = new ArrayList();
      do
      {
        SDContraindication contra = new SDContraindication();
        String phinVADS = rs.getString(1);
        String snomedCT = rs.getString(2);
        if(phinVADS == null || phinVADS.isEmpty()) {
          contra.setMedHistoryCode(snomedCT);
          contra.setMedHistoryCodeSys("SCT");
        }
        else {
          contra.setMedHistoryCode(phinVADS);
          contra.setMedHistoryCodeSys("CDCPHINVS");
        }

        contraList.add(contra);
      } while(rs.next());
    }

    DBHelper.disconnect(null, stmt, rs);
    return contraList;

  }

  public static SDConditionalSkip getConditionalSkip(int doseId) throws Exception {
    SDConditionalSkip condSkip = null;
    SDCSSet           csSet    = null;
    SDCSCondition     csCond   = null;

    String SQL = "select conditional_skip_id, set_logic from sd_conditional_skip where dose_id = " + doseId;
    Statement stmt = DBHelper.dbConn().createStatement();
    ResultSet rs = stmt.executeQuery(SQL);

    String sqlCSSet = "select cs_set_id, condition_logic, description from sd_cs_set where conditional_skip_id = ?";
    PreparedStatement pstmtCSSet = DBHelper.dbConn().prepareStatement(sqlCSSet);

    String sqlCSCondition = "select cs_condition_id, condition_type, start_date, end_date, begin_age, end_age, min_interval, dose_count, dose_type, dose_count_logic from sd_cs_condition where cs_set_id = ?";
    PreparedStatement pstmtCSCondition = DBHelper.dbConn().prepareStatement(sqlCSCondition);

    String sqlCSCVac = "select vaccine_id from sd_csc_vaccine where cs_condition_id = ?";
    PreparedStatement pstmtCSCVac = DBHelper.dbConn().prepareStatement(sqlCSCVac);

    if(rs.next())
    {
      condSkip = new SDConditionalSkip();
      condSkip.setSetLogic(rs.getString(2));
      do { // Collect all of the Sets
        pstmtCSSet.setInt(1, rs.getInt(1));
        ResultSet rsCSSet = pstmtCSSet.executeQuery();
        while(rsCSSet.next())
        {
          csSet = new SDCSSet();
          csSet.setConditionLogic(rsCSSet.getString(2));
          csSet.setDescription(rsCSSet.getString(3));

        
          // For each set, collect all of the conditions
          pstmtCSCondition.setInt(1, rsCSSet.getInt(1));
          ResultSet rsCSCond = pstmtCSCondition.executeQuery();
          while(rsCSCond.next())
          {
            csCond = new SDCSCondition();
            csCond.setConditionType(rsCSCond.getString(2));
            csCond.setStartDate(rsCSCond.getDate(3));
            csCond.setEndDate(rsCSCond.getDate(4));
            csCond.setBeginAge(rsCSCond.getString(5));
            csCond.setEndAge(rsCSCond.getString(6));
            csCond.setInterval(rsCSCond.getString(7));
            csCond.setDoseCount(rsCSCond.getInt(8));
            csCond.setDoseType(rsCSCond.getString(9));
            csCond.setDoseCountLogic(rsCSCond.getString(10));
            
            // Get all of the internal VaccineIds
            pstmtCSCVac.setInt(1, rsCSCond.getInt(1));
            ResultSet rsCSCVac = pstmtCSCVac.executeQuery();
            while(rsCSCVac.next())
            {
              csCond.addVacId(rsCSCVac.getString(1));
            }
            rsCSCVac.close();
            csSet.addCsCondition(csCond);
          }
          rsCSCond.close();
          condSkip.addCsSet(csSet);
        }
        rsCSSet.close();
      } while(rs.next());
      pstmtCSSet.close();
      pstmtCSCondition.close();
      pstmtCSCVac.close();
    }

    DBHelper.disconnect(null, stmt, rs);
    return condSkip;

  }




}
