/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.cdc.cdsi.engine;

import gov.cdc.cdsi.db.DBGetter;
import gov.cdc.cdsi.engine.CDSiPatientData.AntigenAdministered;
import gov.cdc.cdsi.engine.CDSiPatientData.MedicalHistory;
import gov.cdc.cdsi.engine.CDSiPatientSeries.TargetDose;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 *
 * @author eric
 */
public class CDSiForecaster {

  // Chapter 5
  public static void forecastNextDose(CDSiPatientSeries ps) throws Exception
  {
    // Get last Dose administered as our reference point
    AntigenAdministered lastAA = null;
    List<AntigenAdministered> aaList = ps.getPatientData().getAntigenAdministeredList();
    if(aaList != null && aaList.size() > 0)
      lastAA = aaList.get(aaList.size() - 1);


    // Get the target Dose (First target dose with a Not Satisfied status
    TargetDose forecastTD = getTargetDoseToForecast(ps.getTargetDoses());

    //5.1 Conditional Skip via recursion
    if(CDSiEvaluator.conditionallySkipTargetDose(lastAA, forecastTD, ps, ps.getAssessmentDate(), "Forecast"))
    {
      forecastTD.setStatusSkipped();
      forecastNextDose(ps);
      return;
    }
      
    // 5.2 Determine Evidence of Immunity
    //if (DetermineEvidenceOfImmunity())
    
    // 5.3 Determine Need
    if (determineNeed(forecastTD, ps))
    {
      // 5.6 Forecast Vaccine Types
      forecastVaccineType(ps, forecastTD);
      
      // 5.7 Generate Forecast Dates
      generateForecastDates(ps, lastAA, forecastTD);
    }
  }

  // 5.4 Determine Evidence of Immunity
  // TODO: Implement this.

  // 5.5 Determine Need
  private static boolean determineNeed(TargetDose forecastTD, CDSiPatientSeries ps) throws Exception {

    // Column 2 on DT (Patient Complete)
    if(forecastTD == null && ps.getCountOfSatisfiedTargetDoses() > 0) {
      ps.setStatusComplete();
      ps.getPatientData().getForecast().setReason(CDSiGlobals.FORECAST_REASON_COMPLETE);
      return false;
    }
    
    // Column X
    if (forecastTD == null && ps.getCountOfSatisfiedTargetDoses() == 0)
    {
      ps.setStatusNotRecommended();
      ps.getPatientData().getForecast().setReason(CDSiGlobals.FORECAST_REASON_NOT_RECOMMENDED);
      return false;
    }

    // Column 3 (patient has a Contraindication)
    if(ps.getPatientData().hasMedicalHistory()) {
      List<SDContraindication> contraList = SupportingData.getContraindicationsForAntigen(ps.getAntigenId());
      if(contraList != null) {
        for(SDContraindication contra : contraList) {
          for(MedicalHistory medHx : ps.getPatientData().getMedicalHistory()) {
            if(contra.getMedHistoryCode().equalsIgnoreCase(medHx.getMedHistoryCode()) &&
               contra.getMedHistoryCodeSys().equalsIgnoreCase(medHx.getMedHistoryCodeSys()) &&
               ps.getVaccineGroupId() == (medHx.isVaccineGroupSpecificCondition()? medHx.getVaccineGroupId() : ps.getVaccineGroupId())) {
              ps.getPatientData().getForecast().setReason(CDSiGlobals.FORECAST_REASON_CONTRAINDICATED);
              ps.setStatus(CDSiGlobals.SERIES_CONTRAINDICATED);
              return false;
            }
          }
        }
      }
    }


    // Column 5 on DT (Patient has Exceeded Max Age)
    // Make sure we have age parameters to evaluate
    SDAge sdAge = SupportingData.getAgeData(forecastTD.getDoseId(), ps.getAssessmentDate());
    if(sdAge != null)
    {
      // Important dates for this operation
      Date assessmentDate = ps.getAssessmentDate();
      Date maxAgeDate     = CDSiDate.calculateDate(ps.getPatientData().getPatient().getDob(), sdAge.getMaximumAge(), "12/31/2999");

      if(!assessmentDate.before(maxAgeDate))
      {
        ps.getPatientData().getForecast().setReason(CDSiGlobals.FORECAST_REASON_TOO_OLD);
        ps.setStatus(CDSiGlobals.SERIES_AGED_OUT);
        return false;
      }
    }

    // TODO Column 6 (Seasonal Recommendation past season end date)
    SDSeasonalRecommendation sdSeas = SupportingData.getSeasonalRecommendationData(forecastTD.getDoseId());
    if(sdSeas != null)
    {
      Date assessmentDate = ps.getAssessmentDate();
      if(!assessmentDate.before(sdSeas.getEndDate()))
      {
        ps.getPatientData().getForecast().setReason(CDSiGlobals.FORECAST_REASON_PAST_SEASON);
        ps.setStatus(CDSiGlobals.SERIES_NOT_COMPLETE);
        return false;
      }
    }
      

    // Column 1  If we haven't found any reason to prevent a forecast.
    return true;
  }

  // 5.6 Forecast Vaccine Type
  private static void forecastVaccineType(CDSiPatientSeries ps, TargetDose forecastTD) throws Exception {
    List<SDPreferableVaccine> SDPref = SupportingData.getPreferableVaccineData(forecastTD.getDoseId());
    
    if(SDPref == null || SDPref.isEmpty()) return;
    

    for(SDPreferableVaccine pv : SDPref) {
      if(pv.isForecastVaccineType())
        if(pv.getManufacturerId() >0 )
          ps.getPatientData().getForecast().addVaccineType(DBGetter.getProductName(pv.getVaccineId(), pv.getManufacturerId()));
        else
          ps.getPatientData().getForecast().addVaccineType(DBGetter.getNameAndCVXFromVaccineId(pv.getVaccineId()));
    }
  }
  
  // 5.7 Generate Forecast Dates
  private static void generateForecastDates(CDSiPatientSeries ps, AntigenAdministered lastAA, TargetDose forecastTD) throws Exception {
    SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
    SDAge                       sdAge   = SupportingData.getAgeData(forecastTD.getDoseId(), ps.getAssessmentDate());
    List<SDInterval>            intList = SupportingData.getIntervalData(forecastTD.getDoseId(), ps.getAssessmentDate());
    SDSeasonalRecommendation    sdSeas  = SupportingData.getSeasonalRecommendationData(forecastTD.getDoseId());

    // if the patient hasn't received any doses, then interval rules don't apply.
    // This avoids a problem when Target Dose #1 was skipped
    if(lastAA == null)
      intList = null;

    // Set the forecast Target Dose number
    ps.getPatientData().getForecast().setTargetDoseNumber(forecastTD.getDoseNumber());
    
    // Set the Interval Priority Flag
    if(intList != null && !intList.isEmpty())
      ps.getPatientData().getForecast().setIntervalPriorityFlag(intList.get(0).getPriorityFlag());

    // Local Variables  Start with Age and then drop back to others as rules apply
    Date dob              = ps.getPatientData().getPatient().getDob();
    Date earliestDate     = CDSiDate.calculateDate(dob, (sdAge != null ? sdAge.getMinimumAge()             : null), "01/01/1900");
    Date recommendedDate  = CDSiDate.calculateDate(dob, (sdAge != null ? sdAge.getEarliestRecommendedAge() : null));
    Date pastDueDate      = CDSiDate.calculateDate(dob, (sdAge != null ? sdAge.getLatestRecommendedAge()   : null));
    Date latestDate       = CDSiDate.calculateDate(dob, (sdAge != null ? sdAge.getMaximumAge()             : null));
    String earliestReason = "EARLIEST DATE: Set by Age value of "    + (sdAge != null ? sdAge.getMinimumAge() :             "01/01/1900");
    String recReason      = "RECOMMENDED DATE: Set by Age value of " + (sdAge != null ? sdAge.getEarliestRecommendedAge() : "");
    String pastDueReason  = "PAST DUE DATE: Set by Age value of "    + (sdAge != null ? sdAge.getLatestRecommendedAge() :   "");
    String latestReason   = "LATEST REASON: Set by Age value of "    + (sdAge != null ? sdAge.getMaximumAge() :             "");

    
//******************** EARLIEST DATE *******************************************//
    // Earliest Date (Interval)
    if(intList != null && !intList.isEmpty())
    {
      for(SDInterval si : intList)
      {
        Date minIntDate = null;
        AntigenAdministered aa = null;
        if(si.isFromPreviousDose())
        {
          aa = CDSiEvaluator.findPreviousDose(ps.getPatientData().getAllVaccineDosesAdministered().size() + 1, ps.getPatientData().getAllVaccineDosesAdministered()); 
          if (aa != null)
            minIntDate = CDSiDate.calculateDate(aa.getDateAdministered(), si.getMinimumInterval());
        }
        else if (si.isFromTargetDose())
        {
          aa = CDSiEvaluator.findTargetDose(si.getFromTargetDoseNubmer(), ps.getPatientData().getAntigenAdministeredList());
          minIntDate = CDSiDate.calculateDate(aa.getDateAdministered(), si.getMinimumInterval());
        }
        else if (si.isFromMostRecent())
        {
          aa = CDSiEvaluator.findMostRecent(si, ps.getPatientData().getAllVaccineDosesAdministered(),lastAA);
          if(aa != null)
            minIntDate = CDSiDate.calculateDate(aa.getDateAdministered(), si.getMinimumInterval());
        }
        else
        {
          throw new Exception("Interval - Unable to detect type of interval during forecasting.  From Previous, TargetDose, or MostRecent");
        }
        
        if(minIntDate!= null && minIntDate.after(earliestDate)) {
          earliestDate = minIntDate;
          earliestReason = "EARLIEST DATE: Set by Interval of " + si.getMinimumInterval() + " from " + aa.getTradeName() + " administered on " + df.format(aa.getDateAdministered());
        }
      }
    }

    // Live Conflict Dates
    Date latestConflictEndDate = getLatestConflictEndDate(ps, lastAA);

    if(latestConflictEndDate.after(earliestDate)) {
      earliestDate = latestConflictEndDate;
      earliestReason = "EARLIEST DATE: Set by Live Virus Spacing.  Need to develop better information.";
    }
    
    // Seasonal Recommendation Start Date
    if(sdSeas != null && sdSeas.getStartDate().after(earliestDate)) {
      earliestDate = sdSeas.getStartDate();
      earliestReason = "EARLIEST DATE: Set by Seasonal Start Date of " + df.format(sdSeas.getStartDate());
    }
    
    // Latest Inadvertent Vaccine Administration
    Date latestInadvertDate = getLatestInadvertentAdministration(ps.getPatientData().getAntigenAdministeredList());
    if(latestInadvertDate.after(earliestDate)) {
      earliestDate = latestInadvertDate;
      earliestReason = "EARLIEST DATE: Set by Inadvertent Administration on " + df.format(latestInadvertDate);
    }
    
    // Assign Earliest Date
    ps.getPatientData().getForecast().setEarliestDate(earliestDate);
    
    //******************** RECOMMENDED DATE *******************************************//

    // Use the initially assigned age date if valued as the Unadjusted Date
    if(recommendedDate == null && intList != null && !intList.isEmpty())  // Try to use the Inerval
    {
      recommendedDate = CDSiDate.calculateDate(null, null, "01/01/1900");
      AntigenAdministered aa = null;
      for(SDInterval si : intList) {
        Date recIntDate = null;
        if(si.isFromPreviousDose())
        {
          aa = lastAA;
          recIntDate = CDSiDate.calculateDate(aa.getDateAdministered(), si.getEarliestRecommendedInterval());
        }
        else if (si.isFromTargetDose())
        {
          aa = CDSiEvaluator.findTargetDose(si.getFromTargetDoseNubmer(), ps.getPatientData().getAntigenAdministeredList());
          recIntDate = CDSiDate.calculateDate(aa.getDateAdministered(), si.getEarliestRecommendedInterval());
        }
        else if (si.isFromMostRecent())
        {
          aa = CDSiEvaluator.findMostRecent(si, ps.getPatientData().getAntigenAdministeredList(), lastAA);
          if(aa != null)
            recIntDate = CDSiDate.calculateDate(aa.getDateAdministered(), si.getEarliestRecommendedInterval());
        }
        else
        {
          throw new Exception("Interval - Unable to detect type of interval during forecasting.  From Previous, TargetDose, or MostRecent");
        }

        if(recIntDate != null && recIntDate.after(recommendedDate)) {
          recommendedDate = recIntDate;
          recReason = "RECOMMENDED DATE: Set by Interval of " + si.getEarliestRecommendedInterval() + " from " + aa.getTradeName() + " administered on " + df.format(aa.getDateAdministered());
        }
      }
    }
    else if(recommendedDate == null) { // default to the earliest date
      recommendedDate = earliestDate;
      recReason = "RECOMMENDED DATE: Set by EARLIEST DATE.";
    }

    // Assign to forecast
    ps.getPatientData().getForecast().setUnadjustedRecommendedDate(recommendedDate);

    // Now we can adjust to make logical sense (for lapse kids)
    if(earliestDate.after(recommendedDate)) {
      recommendedDate = earliestDate;
      recReason = "RECOMMENDED DATE: Set by EARLIEST DATE.";
    }

    // Assign to forecast
    ps.getPatientData().getForecast().setAdjustedRecommendedDate(recommendedDate);


    //******************** PAST DUE DATE *******************************************//

    // Use the initially assigned age date if valued as the Unadjusted Date; otherwise, try an interval if one exists.
    if(pastDueDate == null && intList != null && !intList.isEmpty())  // Try to use the Interval
    {
      for(SDInterval si : intList)
      {
        Date pdIntDate = null;
        AntigenAdministered aa = null;
        if(si.getLatestRecommendedInterval() != null && !si.getLatestRecommendedInterval().isEmpty()) {
          if(si.isFromPreviousDose()) 
          {
            aa = lastAA;
            pdIntDate = CDSiDate.calculateDate(aa.getDateAdministered(), si.getLatestRecommendedInterval());
          }
          else if (si.isFromTargetDose())
          {
            aa = CDSiEvaluator.findTargetDose(si.getFromTargetDoseNubmer(), ps.getPatientData().getAntigenAdministeredList());
            pdIntDate = CDSiDate.calculateDate(aa.getDateAdministered(), si.getLatestRecommendedInterval());
          }
          else if (si.isFromMostRecent())
          {
            aa = CDSiEvaluator.findMostRecent(si, ps.getPatientData().getAntigenAdministeredList(), lastAA);
            if(aa != null)
              pdIntDate = CDSiDate.calculateDate(aa.getDateAdministered(), si.getLatestRecommendedInterval());
          }
          else
          {
            throw new Exception("Interval - Unable to detect type of interval during forecasting.  From Previous, TargetDose, or MostRecent");
          }

          if(pdIntDate != null && (pastDueDate == null || pdIntDate.after(pastDueDate))) {
            pastDueDate = pdIntDate;
            pastDueReason = "PAST DUE DATE: Set by Interval of " + si.getLatestRecommendedInterval() + " from " + aa.getTradeName() + " administered on " + df.format(aa.getDateAdministered());
          }
        }
      }
    }

    // If it is still null, just leave it null per business rules in forecasting
    // If it has a value, then apply the business rules
    if(pastDueDate != null) {
      // Subtract 1 day per Logic Spec.
      pastDueDate = CDSiDate.calculateDate(pastDueDate, "0 days - 1 day");

      // Assign to forecast
      ps.getPatientData().getForecast().setUnadjustedPastDueDate(pastDueDate);

      // Now we can adjust to make logical sense (for lapse kids)
      if(earliestDate.after(pastDueDate)) {
        pastDueDate = earliestDate;
        pastDueReason = "PAST DUE DATE: Set by EARLIEST DATE";
      }

      // Assign to forecast
      ps.getPatientData().getForecast().setAdjustedPastDueDate(pastDueDate);
    }
    else {
      pastDueReason = "PAST DUE DATE: No past due date";
    }

    //****************** LATEST DATE *****************************************//
    //Subtract one from calculate maximum age date or leave null.
    if(latestDate != null)
      latestDate = CDSiDate.calculateDate(latestDate, "0 days - 1 day");
    else
      latestReason = "LATEST DATE: No latest date";

    // Assign to forecast
    ps.getPatientData().getForecast().setLatestDate(latestDate);
    
    ps.getPatientData().getForecast().setReason(earliestReason + "<br>" + recReason + "<br>" + pastDueReason + "<br>" + latestReason);
  }
  

  //--------- HELPER METHODS --------------------|
  //                                             |
  //--------- HELPER METHODS --------------------|

  private static TargetDose getTargetDoseToForecast(List<TargetDose> targetDoses) {
    // Making sure return the reference and not a copy of the TargetDose
    for(int i=0; i < targetDoses.size(); i++)
    {
      if(targetDoses.get(i).isStatusNotSatisified())
        return targetDoses.get(i);
    }
    return null;
  }

  private static Date getLatestConflictEndDate(CDSiPatientSeries ps, AntigenAdministered lastAA) throws Exception {
    List<SDLiveVirusConflict> liveList = SupportingData.getLiveVirusConflicts(1);

    Date returnDate = CDSiDate.calculateDate(null, null, "01/01/1900");
    int curVacId = 0;
    if(lastAA != null)
      curVacId = lastAA.getVaccineId();
    else
      curVacId = DBGetter.getVaccineId(DBGetter.GetDefaultForecastCVX(ps.getVaccineGroupId()));
    
    // Return a valid status if this isn't a live vaccine
    if(!isLive(liveList, curVacId))
     return returnDate;
    
    
    for(AntigenAdministered prevAA : ps.getPatientData().getAllVaccineDosesAdministered()) {
      if(isLive(liveList, prevAA.getVaccineId())) {
        SDLiveVirusConflict lvc = CDSiEvaluator.getConflictData(liveList, prevAA.getVaccineId(), curVacId);
        Date endIntDate = CDSiDate.calculateDate(prevAA.getDateAdministered(), lvc.getEndInterval(), "01/01/1900");

        if(endIntDate.after(returnDate)) {
          returnDate = endIntDate;
        }
      }
    }
    return returnDate;
  }

  private static boolean isLive(List<SDLiveVirusConflict> liveList, int vaccineId) {
    for(SDLiveVirusConflict lvc : liveList) {
      if(lvc.getPreviousVaccineId() == vaccineId)
        return true;
    }
    return false;
  }

  private static Date getLatestInadvertentAdministration(List<AntigenAdministered> aaList) throws Exception {
    
    Date returnDate = CDSiDate.calculateDate(null, null, "01/01/1900");
 
    for(AntigenAdministered aa : aaList) {
      if(aa.isInadvertentVaccine()) {
        returnDate = aa.getDateAdministered();
      }
    }
    return returnDate;
  }
}
