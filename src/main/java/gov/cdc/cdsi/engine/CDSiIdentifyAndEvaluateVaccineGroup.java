/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.cdc.cdsi.engine;

import gov.cdc.cdsi.engine.CDSiPatientData.Forecast;
import java.util.Date;
import java.util.List;

/**
 *
 * @author eric
 */
class CDSiIdentifyAndEvaluateVaccineGroup {
  private static final int SINGLE_ANTIGEN = 1;
  private static final int MULTI_ANTIGEN  = 2;

  // Chapter 7 of the Logic Specification
  public static Forecast createVaccineGroupForecast(List<CDSiPatientSeries> listOfBestAntigenSeries) throws Exception {
    // 7.1 Vocab Definitions -- These are implemented as Helper Methods at the bottom

    // 7.2 Classify Vaccine Group and subsequent action (outcome) of the decision table
    int vgType = ClassifyVaccineGroup(listOfBestAntigenSeries);

    switch (vgType) {
      case SINGLE_ANTIGEN:
           // 7.3 Single Antigen Vaccine Group
           return SingleAntigenForecast(listOfBestAntigenSeries);
      case MULTI_ANTIGEN:
           // 7.4 Satisfied Vaccine Group
           return MultiAntigenForecast(listOfBestAntigenSeries);
    }

    return null;

  }

  // 7.2 Classify Vaccine Group
  private static int ClassifyVaccineGroup(List<CDSiPatientSeries> psList) throws Exception {
    // column 1 -- One Antigen in Vaccine group (Most Common)
    if(psList.size() == 1) return SINGLE_ANTIGEN;

    return MULTI_ANTIGEN;
  }

  // 7.3 Single Antigen Vaccine Group
  private static Forecast SingleAntigenForecast(List<CDSiPatientSeries> psList) throws Exception {
    CDSiPatientSeries ps = psList.get(0);
    Forecast fcast = ps.getPatientData().new Forecast();
    Forecast seriesForecast = ps.getPatientData().getForecast();

    // Business Rule SINGLEANTVG-1
    fcast.setStatus(ps.getStatus());

    // Business Rule SINGLEANTVG-2
    fcast.setEarliestDate(seriesForecast.getEarliestDate());
    // Business Rule SINGLEANTVG-3
    fcast.setUnadjustedRecommendedDate(seriesForecast.getUnadjustedRecommendedDate());
    // Business Rule SINGLEANTVG-4
    fcast.setAdjustedRecommendedDate(seriesForecast.getAdjustedRecommendedDate());
    // Business Rule SINGLEANTVG-5
    fcast.setUnadjustedPastDueDate(seriesForecast.getUnadjustedPastDueDate());
    // Business Rule SINGLEANTVG-6
    fcast.setAdjustedPastDueDate(seriesForecast.getAdjustedPastDueDate());
    // Business Rule SINGLEANTVG-7
    fcast.setLatestDate(seriesForecast.getLatestDate());

    // Business Rule SINGLEANTVG-8
    fcast.setReason(seriesForecast.getReason());

    // Business Rule SINGLEANTVG-9
    fcast.setAntigensNeeded(SupportingData.getAntigenName(ps.getAntigenId()));
    
    // Business Rule SINGLEANTVG-10
    fcast.setVaccineTypes(seriesForecast.getVaccineTypes());
    
    // IT rules not in Logic Spec
    fcast.setTargetDoseNumber(seriesForecast.getTargetDoseNumber());
    fcast.setForecastNumber(seriesForecast.getForecastNumber());
    fcast.setVaccineGroupId(ps.getVaccineGroupId());

    return fcast;
  }

  // 7.4 Multi Antigen Vaccine Group
  private static Forecast MultiAntigenForecast(List<CDSiPatientSeries> psList) throws Exception {
    CDSiPatientSeries ps = psList.get(0);
    Forecast fcast = ps.getPatientData().new Forecast();

    // What is the Vaccine Group Status?
    fcast.setStatus(getMultiAntigenVGStatus(psList));

    // Business Rule MULTIANTVG-1
    fcast.setEarliestDate(getLatestEarliestDate(psList));

    // Business Rule MULTIANTVG-2
    fcast.setAdjustedRecommendedDate(getEarliestAdjustedRecommendedDate(psList, fcast.getEarliestDate()));

    // Business Rule MULTIANTVG-3
    fcast.setAdjustedPastDueDate(getEarliestAdjustedPastDueDate(psList, fcast.getEarliestDate()));

    // Business Rule MULTIANTVG-4
    fcast.setLatestDate(getEarliestLatestDate(psList));

    // Business Rule MULTIANTVG-5
    fcast.setUnadjustedRecommendedDate(getEarliestUnadjustedRecommendedDate(psList));

    // Business Rule MULTIANTVG-6
    fcast.setUnadjustedPastDueDate(getEarliestUnadjustedPastDueDate(psList));

    // Business Rule MULTIANTVG-7
    fcast.setReason(getForecastReasons(psList));

    // Business Rule MULTIANTVG-8
    fcast.setAntigensNeeded(getAntigensNeeded(psList));

        // IT rules not in Logic Spec
    fcast.setTargetDoseNumber(getTargetDoseNumber(psList));
    fcast.setForecastNumber(getForecastNumber(psList));
    fcast.setVaccineGroupId(psList.get(0).getVaccineGroupId());

    return fcast;
  }

  private static String getMultiAntigenVGStatus(List<CDSiPatientSeries> psList) throws Exception {
    boolean adminFullVG = SupportingData.isAdministerFullVaccineGroup(psList.get(0).getVaccineGroupId());

    // Column 1 (MMR with Contraindication)
    if(!hasImmunityToAllAntigens(psList) &&
        adminFullVG                      &&
        hasAtLeastOneContraindicatedAntigen(psList))
        // at least 1 not complete -- Does Not Matter
      return CDSiGlobals.SERIES_CONTRAINDICATED;

    // Column 2 (Tdap w Contra, but nothing left to complete)
    if(!hasImmunityToAllAntigens(psList) &&
       !adminFullVG                      &&
        hasAtLeastOneContraindicatedAntigen(psList) &&
       !hasAtLeastOneNotCompleteAntigen(psList))
      return CDSiGlobals.SERIES_CONTRAINDICATED;

    // Column 3 (MMR no contraindications; not complete)
    if(!hasImmunityToAllAntigens(psList) &&
        adminFullVG                      &&
       !hasAtLeastOneContraindicatedAntigen(psList) &&
        hasAtLeastOneNotCompleteAntigen(psList))
      return CDSiGlobals.SERIES_NOT_COMPLETE;

    // Column 4 (Tdap at least 1 not complete)
    if(!hasImmunityToAllAntigens(psList) &&
       !adminFullVG                      &&
       // At least 1 contraindication -- Does Not Matter
        hasAtLeastOneNotCompleteAntigen(psList))
      return CDSiGlobals.SERIES_NOT_COMPLETE;

    // Column 5 (Complete or partial immunity with complete)
    if(!hasImmunityToAllAntigens(psList) &&
       // Admin full VG -- Does Not Matter
       !hasAtLeastOneContraindicatedAntigen(psList) &&
       !hasAtLeastOneNotCompleteAntigen(psList))
      return CDSiGlobals.SERIES_COMPLETE;

    // Column 6 (Immunity to all Antigens
    if(hasImmunityToAllAntigens(psList)) return CDSiGlobals.SERIES_IMMUNE;

    // Failure of DT.
    throw new Exception("DT failed to identify VG Status!");

  }


  ////////////////////
  // HELPER METHODS //
  ////////////////////
  private static boolean hasAtLeastOneNotCompleteAntigen(List<CDSiPatientSeries> psList) {
    for(CDSiPatientSeries ps : psList) {
      if(ps.isStatusNotComplete())
        return true;
    }
    return false;
  }

  private static boolean hasAtLeastOneContraindicatedAntigen(List<CDSiPatientSeries> psList) {
    for(CDSiPatientSeries ps : psList) {
      if(ps.isStatusContraindicated())
        return true;
    }
    return false;
  }

  private static boolean hasImmunityToAllAntigens(List<CDSiPatientSeries> psList) {
    for(CDSiPatientSeries ps : psList) {
      if(!ps.isStatusImmune())
        return false;
    }
    return true;
  }

  private static Date getLaterDate(Date dateA, Date dateB) {
    if (dateA == null) return dateB;
    if (dateB == null) return dateA;
    return dateA.after(dateB) ? dateA : dateB;
  }

  private static Date getEarlierDate(Date dateA, Date dateB) {
    if (dateA == null) return dateB;
    if (dateB == null) return dateA;
    return dateA.before(dateB)? dateA : dateB;
  }

  private static Date getLatestEarliestDate(List<CDSiPatientSeries> psList) throws Exception {
    Date vgEarliestDate    = null;
    Date vgLatestAdminDate = null;
    
    boolean hasPriorityInterval = false;
    for(CDSiPatientSeries ps : psList) {
      if(ps.getPatientData().getForecast().isIntervalPriority())  
        hasPriorityInterval = true;
    }
        
    for(CDSiPatientSeries ps : psList) {
      if(hasPriorityInterval)
      {
        if(ps.getPatientData().getForecast().isIntervalPriority())  {
          vgEarliestDate    = getEarlierDate(vgEarliestDate, ps.getPatientData().getForecast().getEarliestDate());
          vgLatestAdminDate = getLaterDate(vgLatestAdminDate, ps.getPatientData().getLastDoseDate()); 
        }
      }
      else
        vgEarliestDate = getLaterDate(vgEarliestDate, ps.getPatientData().getForecast().getEarliestDate());
    }
    return hasPriorityInterval ? getLaterDate(vgEarliestDate, vgLatestAdminDate) : vgEarliestDate;
  }

  private static Date getEarliestAdjustedRecommendedDate(List<CDSiPatientSeries> psList, Date vgFcastEarliestDate) throws Exception {
    Date earliestRecDate = null;

    // Earliest Recommended Date from all Best Patient Series.
    for(CDSiPatientSeries ps : psList) {
      earliestRecDate = getEarlierDate(earliestRecDate, ps.getPatientData().getForecast().getAdjustedRecommendedDate());
    }

    // Adjusted against Vaccine Group Forecast Earliest Date.
    earliestRecDate = getLaterDate(earliestRecDate, vgFcastEarliestDate);
    return earliestRecDate;
  }

  private static Date getEarliestAdjustedPastDueDate(List<CDSiPatientSeries> psList, Date vgFcastEarliestDate) throws Exception {
    Date earliestPastDueDate = null;

    // Earliest Recommended Date from all Best Patient Series.
    for(CDSiPatientSeries ps : psList) {
      earliestPastDueDate = getEarlierDate(earliestPastDueDate, ps.getPatientData().getForecast().getAdjustedPastDueDate());
    }
    // Adjusted against Vaccine Group Forecast Earliest Date.
    earliestPastDueDate = getLaterDate(earliestPastDueDate, vgFcastEarliestDate);

    return earliestPastDueDate;
  }

  private static Date getEarliestLatestDate(List<CDSiPatientSeries> psList) throws Exception {
    Date earliestLatestDate = null;

    for(CDSiPatientSeries ps : psList) {
      earliestLatestDate = getEarlierDate(earliestLatestDate, ps.getPatientData().getForecast().getLatestDate());
    }
    return earliestLatestDate;
  }

  private static Date getEarliestUnadjustedRecommendedDate(List<CDSiPatientSeries> psList) throws Exception {
    Date earliestRecDate = CDSiDate.calculateDate(null, null, "01/01/1900");

    for(CDSiPatientSeries ps : psList) {
      earliestRecDate = getEarlierDate(earliestRecDate, ps.getPatientData().getForecast().getUnadjustedRecommendedDate());
    }
    return earliestRecDate;
  }

  private static Date getEarliestUnadjustedPastDueDate(List<CDSiPatientSeries> psList) throws Exception {
    Date earliestPastDueDate = CDSiDate.calculateDate(null, null, "01/01/1900");

    for(CDSiPatientSeries ps : psList) {
      earliestPastDueDate = getEarlierDate(earliestPastDueDate, ps.getPatientData().getForecast().getUnadjustedPastDueDate());
    }
    return earliestPastDueDate;
  }

  private static String getAntigensNeeded(List<CDSiPatientSeries> psList) throws Exception {
    boolean adminFullVG = SupportingData.isAdministerFullVaccineGroup(psList.get(0).getVaccineGroupId());
    String str = "";
    if(adminFullVG) {
      for (CDSiPatientSeries series : psList) {
        if(series.isStatusNotComplete())
          str += SupportingData.getAntigenName(series.getAntigenId()) + " <br> ";
      }
    }
    else {
      for (CDSiPatientSeries series : psList) {
        if(series.isStatusNotComplete()) {
          boolean disqualified = false;
          for(CDSiPatientSeries innerSeries : psList) {
            if(innerSeries.getPatientData().getForecast().getEarliestDate()!=null && 
               innerSeries.getPatientData().getForecast().getEarliestDate().before(series.getPatientData().getForecast().getEarliestDate())) {
              disqualified = true;
              break;
            }
          }
          if(!disqualified)
            str += SupportingData.getAntigenName(series.getAntigenId()) + " <br> ";
        }
      }
    }

    // Chop off last 4 (" <br> ")
    return str.length() > 6 ? str.substring(0, str.length() - 6) : str;
  }


      
  private static String getForecastReasons(List<CDSiPatientSeries> psList) throws Exception {
    String str = "";
    for (CDSiPatientSeries series : psList) {
      str +=  SupportingData.getAntigenName(series.getAntigenId()) + ": " +
              series.getPatientData().getForecast().getReason() + " <br> ";
    }

    // Chop off last 4 (" <br> ")
    return str.substring(0, str.length() - 6);
  }
  
  private static int getTargetDoseNumber(List<CDSiPatientSeries> psList) throws Exception {
    int targetDoseNumber = 999;
    for(CDSiPatientSeries ps : psList)
    {
      Forecast seriesForecast = ps.getPatientData().getForecast();
      if(seriesForecast.getTargetDoseNumber() > 0 &&
         seriesForecast.getTargetDoseNumber() < targetDoseNumber)
        targetDoseNumber = seriesForecast.getTargetDoseNumber();
    }
    if(targetDoseNumber == 999)
      targetDoseNumber = 0;
    
    return targetDoseNumber;
  }
  
  private static int getForecastNumber(List<CDSiPatientSeries> psList) throws Exception {
    int forecastNumber = 999;
    for(CDSiPatientSeries ps : psList)
    {
      int validDoses = ps.getPatientData().getCountOfValidDoses();
      if(validDoses > 0 &&
         validDoses < forecastNumber)
        forecastNumber = validDoses + 1;
    }
    if(forecastNumber == 999)
      forecastNumber = 0;
    
    return forecastNumber;
  }
}
