/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.cdc.cdsi.supportingdata.load;

import gov.cdc.cdsi.db.DBHelper;
import gov.cdc.cdsi.supportingdata.mapping.AntigenSupportingData;
import gov.cdc.cdsi.supportingdata.mapping.AntigenSupportingData.Series;
import gov.cdc.cdsi.supportingdata.mapping.AntigenSupportingData.Series.SeriesDose;
import gov.cdc.cdsi.supportingdata.mapping.AntigenSupportingData.Series.SeriesDose.Age;
import gov.cdc.cdsi.supportingdata.mapping.AntigenSupportingData.Series.SeriesDose.AllowableInterval;
import gov.cdc.cdsi.supportingdata.mapping.AntigenSupportingData.Series.SeriesDose.AllowableVaccine;
import gov.cdc.cdsi.supportingdata.mapping.AntigenSupportingData.Series.SeriesDose.ConditionalSkip;
import gov.cdc.cdsi.supportingdata.mapping.AntigenSupportingData.Series.SeriesDose.ConditionalSkip.Set;
import gov.cdc.cdsi.supportingdata.mapping.AntigenSupportingData.Series.SeriesDose.ConditionalSkip.Set.Condition;
import gov.cdc.cdsi.supportingdata.mapping.AntigenSupportingData.Series.SeriesDose.InadvertentVaccine;
import gov.cdc.cdsi.supportingdata.mapping.AntigenSupportingData.Series.SeriesDose.Interval;
import gov.cdc.cdsi.supportingdata.mapping.AntigenSupportingData.Series.SeriesDose.PreferableVaccine;
import gov.cdc.cdsi.supportingdata.mapping.AntigenSupportingData.Series.SeriesDose.SeasonalRecommendation;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author eric
 */
public class SDLoader {

  PreparedStatement psSeries,   psSeriesDose, psDoseAge,  psInterval, psAllowableInterval, 
                    psPref,     psAllow,      psInadvert, psRecur,    xpsCondSet,  
                    xpsCSDetail, xpsCSDVac,   psSeasonal, xpsSub,     psGender,
                    psCondSkip, psCSSet,      psCSCond,   psCSCVac,   psIntervalFMR;

  public void loadAntigenSupportingData(InputStream is, String sdVersion) throws Exception {
    try {
    // This isn't a good practice.  It'll bite me someday.
    int acipScheduleId = 1;

    JAXBContext           jaxbContext      = JAXBContext.newInstance(AntigenSupportingData.class);
    Unmarshaller          jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    AntigenSupportingData sd               = (AntigenSupportingData)jaxbUnmarshaller.unmarshal(is);

    // First Delete all antigen series for this antigen
    System.out.println("Delete the Series for this Antigen");
    SDUnloader.deleteAllSeriesForAntigen(sd.getSeries().get(0).getTargetDisease());

    System.out.println("Get a DB Connection.");
    Connection conn = DBHelper.dbConn();
    conn.setAutoCommit(false);

    // Prepare the statements
    System.out.println("Prepare the Statements.");
    prepareStatements(conn);

    // For Each series
    for(Series series : sd.getSeries()) {
      if(series.getSelectSeries().getSeriesGroupName().startsWith("Standard"))
      { // Temporary if statement until 3.0 logic is included.  This will exlclude all RISK series
        long seriesId = insertSeries(psSeries, acipScheduleId, series, sdVersion);
        System.out.println(series.getSeriesName() + " -> seriesId = " + seriesId);

        // For each Required gender in a series
        for(String gender : series.getRequiredGender()) {
          long genID = insertGender(psGender, seriesId, gender);
          System.out.println("      Gender -> genID = " + genID);
        }

        
        // For Each Dose in a Series
        for(SeriesDose dose : series.getSeriesDose()) {
          long doseId = insertSeriesDose(psSeriesDose, seriesId, dose);
          System.out.println("   " + dose.getDoseNumber() + " -> DoseId = " + doseId);

          // For Each Interval in a Dose
          for(Age age : dose.getAge()) {
            long ageId = insertDoseAge(psDoseAge, doseId, age);
            System.out.println("      " + age.getEarliestRecAge() + " -> ageId = " + ageId);
          }

          // For Each Interval in a Dose
          for(Interval interval : dose.getInterval()) {
            long intId = insertDoseInterval(psInterval, doseId, interval);
            System.out.println("      " + interval.getFromPrevious() + " -> intId = " + intId);

            // Insert From Most Recent Vaccines if they exist
            if(!SDLoaderUtil.isEmpty(interval) && !interval.getFromMostRecent().isEmpty()) {
              List<String> cvxList = Arrays.asList(interval.getFromMostRecent().split("\\s*;\\s*"));
              for (String cvx : cvxList) {
                    long tmp = insertIntervalFMRVaccine(psIntervalFMR, intId, cvx);
                    System.out.println("          IntFMR -> cvx = " + cvx);
                
              }
            }
          }

          // For Each Interval in a Dose
          for(AllowableInterval ainterval : dose.getAllowableInterval()) {
            long aintId = insertAllowableInterval(psAllowableInterval, doseId, ainterval);
            System.out.println("      " + ainterval.getFromPrevious() + " -> allowableintId = " + aintId);
          }

          // For Each Preferable Vaccine in a Dose
          for(PreferableVaccine pref : dose.getPreferableVaccine()) {
            long prefId = insertPreferableVaccine(psPref, doseId, pref);
            System.out.println("      " + pref.getVaccineType() + " (" + pref.getCvx() + ") -> prefId = " + prefId);
          }

          // For Each Allowable Vaccine in a Dose
          for(AllowableVaccine allow : dose.getAllowableVaccine()) {
            long allowId = insertAllowableVaccine(psAllow, doseId, allow);
            System.out.println("      " + allow.getVaccineType() + " (" + allow.getCvx() + ") -> allowId = " + allowId);
          }
          
          // For Each Allowable Vaccine in a Dose
          for(InadvertentVaccine inadvert : dose.getInadvertentVaccine()) {
            long inadvertId = insertInadvertentVaccine(psInadvert, doseId, inadvert);
            System.out.println("      " + inadvert.getVaccineType() + " (" + inadvert.getCvx() + ") -> inadvertId = " + inadvertId);
          }
          

          long recurId = insertRecurringDose(psRecur, doseId, dose.getRecurringDose());
          System.out.println("      Recurring -> recurId = " + recurId);

          // Seasonal Recommendation
          long seasonID = insertSeasonalRecommendation(psSeasonal, doseId, dose.getSeasonalRecommendation());
          System.out.println("      Seasonal -> seasonID = " + seasonID);

          // If we have a conditional Skip
          for(ConditionalSkip condSkip : dose.getConditionalSkip()) {
            long condSkipId = insertConditionalSkip(psCondSkip, doseId, condSkip);
            // For each Conditonal Skip Set
            for(Set csSet : condSkip.getSet()) {
              long csSetId  = insertCSSet(psCSSet, condSkipId, csSet);
              System.out.println("      CSSet -> csSetID = " + csSetId);
              // For each Conditional Skip Set -> Condition
              for(Condition csCondition : csSet.getCondition() ) {
                long csConditionId = insertCSCondition(psCSCond, csSetId, csCondition);
                System.out.println("        CSCondition -> csConditionID = " + csConditionId);

                  List<String> cvxList = Arrays.asList(csCondition.getVaccineTypes().split("\\s*;\\s*"));
                  for(String cvx : cvxList) {
                    long tmp = insertCSCVaccine(psCSCVac, csConditionId, cvx);
                    System.out.println("          CSCVac -> cvx = " + cvx);
                  }
              }
            }
          }
        }
      }
    }
    conn.commit();
    }
    catch (Exception e) {
      throw e;
    }
    finally {
    closeStatements();
    }
  }

  private void prepareStatements(Connection conn) throws Exception {
    String idCol[] = {"series_id"};
    psSeries       = conn.prepareStatement(SQLInserts.insSeries, idCol);

    idCol[0]     = "dose_id";
    psSeriesDose = conn.prepareStatement(SQLInserts.insSeriesDose, idCol);

    idCol[0]  = "age_id";
    psDoseAge = conn.prepareStatement(SQLInserts.insDoseAge, idCol);

    idCol[0]   = "interval_id";
    psInterval = conn.prepareStatement(SQLInserts.insInterval, idCol);

    idCol[0]      = "interval_id";
    psIntervalFMR = conn.prepareStatement(SQLInserts.insIntervalFMRVaccine, idCol);

    idCol[0]            = "allowable_interval_id";
    psAllowableInterval = conn.prepareStatement(SQLInserts.insAllowableInterval, idCol);

    idCol[0] = "preferable_vaccine_id";
    psPref   = conn.prepareStatement(SQLInserts.insPrefVaccine, idCol);

    idCol[0] = "allowable_vaccine_id";
    psAllow  = conn.prepareStatement(SQLInserts.insAllowVaccine, idCol);

    idCol[0] = "inadvertent_vaccine_id";
    psInadvert  = conn.prepareStatement(SQLInserts.insInadvertentVaccine, idCol);

    idCol[0] = "recurring_dose_id";
    psRecur  = conn.prepareStatement(SQLInserts.insRecurringDose, idCol);

    idCol[0]   = "season_recommendation_id";
    psSeasonal = conn.prepareStatement(SQLInserts.insSeasonal, idCol);

    idCol[0] = "gender_id";
    psGender = conn.prepareStatement(SQLInserts.insGender, idCol);
    
    idCol[0]   = "conditional_skip_id";
    psCondSkip = conn.prepareStatement(SQLInserts.insConditionalSkip, idCol);
    
    idCol[0] = "cs_set_id";
    psCSSet  = conn.prepareStatement(SQLInserts.insCSSet, idCol);

    idCol[0] = "cs_condition_id";
    psCSCond = conn.prepareStatement(SQLInserts.insCSCondition, idCol);
    
    idCol[0] = "cs_condition_id";
    psCSCVac = conn.prepareStatement(SQLInserts.insCSCVaccine, idCol);
  }

  private void closeStatements() throws Exception {
    psSeries.close();
    psSeriesDose.close();
    psDoseAge.close();
    psInterval.close();
    psIntervalFMR.close();
    psAllowableInterval.close();
    psPref.close();
    psAllow.close();
    psInadvert.close();
    psRecur.close();
    psSeasonal.close();
    psGender.close();
    psCondSkip.close();
    psCSSet.close();
    psCSCond.close();
    psCSCVac.close();
  }


  private long executeInsert(PreparedStatement ps) throws Exception {
    ps.executeUpdate();
    ResultSet rs = ps.getGeneratedKeys();
    long id = 0;
    if(rs.next())
      id = rs.getLong(1);
    rs.close();
    return id;
  }

  private long insertSeries(PreparedStatement psSeries, int acipScheduleId, Series series, String sdVersion) throws Exception {
    String seriesPref = SDLoaderUtil.cleanNA(series.getSelectSeries().getSeriesPreference());

    psSeries.setInt(1,     acipScheduleId);
    psSeries.setString(2,  series.getTargetDisease());
    psSeries.setString(3,  series.getVaccineGroup());
    psSeries.setString(4,  series.getSeriesName());
    psSeries.setString(5,  SDLoaderUtil.getFirstChar(series.getSelectSeries().getDefaultSeries()));
    psSeries.setString(6,  series.getSelectSeries().getProductPath().substring(0, 1));
    psSeries.setString(7,  SDLoaderUtil.isEmpty(seriesPref)? "0" : seriesPref);
    psSeries.setString(8,  SDLoaderUtil.cleanNA(series.getSelectSeries().getMaxAgeToStart()));
    psSeries.setString(9,  SDLoaderUtil.cleanNA(series.getSelectSeries().getMinAgeToStart()));
    psSeries.setString(10, sdVersion);
    psSeries.setString(11, "");
    psSeries.setString(12, "");
    psSeries.setString(13, "");
    psSeries.setString(14, "");
    psSeries.setString(15, "");
    psSeries.setString(16, "");
    return executeInsert(psSeries);
  }

  private long insertSeriesDose(PreparedStatement psSeriesDose, long seriesId, SeriesDose dose) throws Exception {
    psSeriesDose.setLong(1, seriesId);
    psSeriesDose.setString(2, dose.getDoseNumber().substring(dose.getDoseNumber().indexOf(" ")));
    return executeInsert(psSeriesDose);
  }

  private long insertDoseAge(PreparedStatement psDoseAge, long doseId, Age age) throws Exception {
    if(SDLoaderUtil.isEmpty(age)) return -1;

    psDoseAge.setLong(1, doseId);
    psDoseAge.setString(2, age.getAbsMinAge());
    psDoseAge.setString(3, age.getMinAge());
    psDoseAge.setString(4, age.getEarliestRecAge());
    psDoseAge.setString(5, age.getLatestRecAge());
    psDoseAge.setString(6, age.getMaxAge());
    psDoseAge.setDate(7, SDLoaderUtil.isEmpty(age.getEffectiveDate()) ? null : SDLoaderUtil.getDate(age.getEffectiveDate()));
    psDoseAge.setDate(8, SDLoaderUtil.isEmpty(age.getCessationDate()) ? null : SDLoaderUtil.getDate(age.getCessationDate()));
    return executeInsert(psDoseAge);
  }

  private long insertDoseInterval(PreparedStatement psInterval, long doseId, Interval interval) throws Exception {
    if(SDLoaderUtil.isEmpty(interval)) return -1;

    psInterval.setLong(1, doseId);
    psInterval.setString(2, SDLoaderUtil.getFirstChar(interval.getFromPrevious()));
    psInterval.setString(3, SDLoaderUtil.cleanNA(interval.getFromTargetDose()));
    psInterval.setString(4, SDLoaderUtil.cleanNA(interval.getAbsMinInt()));
    psInterval.setString(5, SDLoaderUtil.cleanNA(interval.getMinInt()));
    psInterval.setString(6, SDLoaderUtil.cleanNA(interval.getEarliestRecInt()));
    psInterval.setString(7, SDLoaderUtil.cleanNA(interval.getLatestRecInt()));
    psInterval.setString(8, SDLoaderUtil.cleanNA(interval.getIntervalPriority()));
    psInterval.setDate(9, SDLoaderUtil.isEmpty(interval.getEffectiveDate()) ? null : SDLoaderUtil.getDate(interval.getEffectiveDate()));
    psInterval.setDate(10, SDLoaderUtil.isEmpty(interval.getCessationDate()) ? null : SDLoaderUtil.getDate(interval.getCessationDate()));
    return executeInsert(psInterval);
  }

  private long insertIntervalFMRVaccine(PreparedStatement psIntervalFMR, long intId, String cvx) throws Exception {
    if(SDLoaderUtil.isEmpty(cvx)) return -1;

    psIntervalFMR.setLong(1, intId);
    psIntervalFMR.setString(2, cvx);
    return executeInsert(psIntervalFMR);
  }

  private long insertAllowableInterval(PreparedStatement psAllowableInterval, long doseId, AllowableInterval allowableInterval) throws Exception {
    if(SDLoaderUtil.isEmpty(allowableInterval)) return -1;

    psAllowableInterval.setLong(1, doseId);
    psAllowableInterval.setString(2, SDLoaderUtil.getFirstChar(allowableInterval.getFromPrevious()));
    psAllowableInterval.setString(3, SDLoaderUtil.cleanNA(allowableInterval.getFromTargetDose()));
    psAllowableInterval.setString(4, SDLoaderUtil.cleanNA(allowableInterval.getAbsMinInt()));
    psAllowableInterval.setDate(5, SDLoaderUtil.isEmpty(allowableInterval.getEffectiveDate()) ? null : SDLoaderUtil.getDate(allowableInterval.getEffectiveDate()));
    psAllowableInterval.setDate(6, SDLoaderUtil.isEmpty(allowableInterval.getCessationDate()) ? null : SDLoaderUtil.getDate(allowableInterval.getCessationDate()));
    return executeInsert(psAllowableInterval);
  }

  private long insertPreferableVaccine(PreparedStatement psPref, long doseId, PreferableVaccine pref) throws Exception {
    if(SDLoaderUtil.isEmpty(pref)) return -1;
    psPref.setLong(1, doseId);
    psPref.setString(2, pref.getCvx());
    psPref.setString(3, pref.getBeginAge());
    psPref.setString(4, pref.getEndAge());
    psPref.setString(5, pref.getMvx());
    psPref.setString(6, pref.getVolume());
    psPref.setString(7, pref.getForecastVaccineType());
    return executeInsert(psPref);
  }

  private long insertAllowableVaccine(PreparedStatement psAllow, long doseId, AllowableVaccine allow) throws Exception {
    if(SDLoaderUtil.isEmpty(allow)) return -1;

    psAllow.setLong(1, doseId);
    psAllow.setString(2, allow.getCvx());
    psAllow.setString(3, allow.getBeginAge());
    psAllow.setString(4, allow.getEndAge());
    return executeInsert(psAllow);
  }

  private long insertInadvertentVaccine(PreparedStatement psInadvert, long doseId, InadvertentVaccine inadvert) throws Exception {
    if(SDLoaderUtil.isEmpty(inadvert)) return -1;

    psInadvert.setLong(1, doseId);
    psInadvert.setString(2, inadvert.getCvx());
    return executeInsert(psInadvert);
  }

  private long insertRecurringDose(PreparedStatement psRecur, long doseId, String recurringDose) throws Exception {
    if(!SDLoaderUtil.isRecurringDose(recurringDose)) return -1;

    psRecur.setLong(1, doseId);
    return executeInsert(psRecur);
  }

  private long insertSeasonalRecommendation(PreparedStatement psSeasonal, long doseId, SeasonalRecommendation seasonalRecommendation) throws Exception {
    if(SDLoaderUtil.isEmpty(seasonalRecommendation)) return -1;

    psSeasonal.setLong(1, doseId);
    psSeasonal.setDate(2, SDLoaderUtil.getDate(seasonalRecommendation.getStartDate()));
    psSeasonal.setDate(3, SDLoaderUtil.getDate(seasonalRecommendation.getEndDate()));
    return executeInsert(psSeasonal);
  }

  private long insertGender(PreparedStatement psGender, long seriesId, String gender) throws Exception {
    if(SDLoaderUtil.isEmpty(gender)) return -1;

    psGender.setLong(1, seriesId);
    psGender.setString(2, SDLoaderUtil.getFirstChar(gender));
    return executeInsert(psGender);
  }

  private long insertConditionalSkip(PreparedStatement psCondSkip, long doseId, ConditionalSkip condSkip) throws Exception {
    psCondSkip.setLong(1, doseId);
    psCondSkip.setString(2, condSkip.getSetLogic());
    psCondSkip.setString(3, SDLoaderUtil.isEmpty(condSkip.getContext()) ? "Both" : condSkip.getContext());
    return executeInsert(psCondSkip);
  }

  private long insertCSSet(PreparedStatement psCSSet, long condSkipId, Set condSet) throws Exception {
    psCSSet.setLong(1, condSkipId);
    psCSSet.setString(2,condSet.getConditionLogic());
    psCSSet.setString(3,condSet.getSetDescription());
    psCSSet.setDate(4, SDLoaderUtil.isEmpty(condSet.getEffectiveDate()) ? null : SDLoaderUtil.getDate(condSet.getEffectiveDate()));
    psCSSet.setDate(5, SDLoaderUtil.isEmpty(condSet.getCessationDate()) ? null : SDLoaderUtil.getDate(condSet.getCessationDate()));
    return executeInsert(psCSSet);
  }

  private long insertCSCondition(PreparedStatement psCSCond, long csSetId, Condition condition) throws Exception {
    psCSCond.setLong(1,   csSetId);
    psCSCond.setString(2, condition.getConditionType());
    psCSCond.setDate(3,   SDLoaderUtil.isNAorUnbound(condition.getStartDate()) ? null : SDLoaderUtil.getDate(condition.getStartDate()));
    psCSCond.setDate(4,   SDLoaderUtil.isNAorUnbound(condition.getEndDate()) ? null : SDLoaderUtil.getDate(condition.getEndDate()));
    psCSCond.setString(5, SDLoaderUtil.cleanNAorUnbound(condition.getBeginAge()));
    psCSCond.setString(6, SDLoaderUtil.cleanNAorUnbound(condition.getEndAge()));
    psCSCond.setString(7, SDLoaderUtil.cleanNAorUnbound(condition.getInterval()));
    psCSCond.setString(8, SDLoaderUtil.cleanNAorUnbound(condition.getDoseCount()));
    psCSCond.setString(9, SDLoaderUtil.cleanNAorUnbound(condition.getDoseType()));
    psCSCond.setString(10, SDLoaderUtil.cleanNAorUnbound(condition.getDoseCountLogic()));
    return executeInsert(psCSCond);
  }

  private long insertCSCVaccine(PreparedStatement psCSCVac, long csConditionId, String cvx) throws Exception {
    if(SDLoaderUtil.isEmpty(cvx)) return -1;

    psCSCVac.setLong(1, csConditionId);
    psCSCVac.setString(2, cvx);
    return executeInsert(psCSCVac);
  }

}
