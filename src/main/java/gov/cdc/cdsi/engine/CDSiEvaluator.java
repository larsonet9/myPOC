/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.cdc.cdsi.engine;

import gov.cdc.cdsi.db.DBGetter;
import static gov.cdc.cdsi.engine.CDSiDate.dropTime;
import gov.cdc.cdsi.engine.CDSiPatientSeries.TargetDose;
import gov.cdc.cdsi.engine.CDSiPatientData.AntigenAdministered;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author eric
 */
public class CDSiEvaluator {


  //============= EVALUATE IMMUNIZATION HISTORY ===============================|
  // This is diagram 4-6 in chapter 4.  The driver of evaluation of a complete |
  // administered history against a specific patient series                    |
  //============= EVALUATE IMMUNIZATION HISTORY ===============================|
  public static void evaluateDosesAdministeredAgainstPatientSeries(CDSiPatientSeries ps) throws Exception
  {
    // Set up the iterators for our two lists (Target Doses and Antigen Admins)
    ListIterator<TargetDose>      tdIter  = ps.getTargetDoses().listIterator();
    Iterator<AntigenAdministered> aaIter  = ps.getPatientData().getAntigenAdministeredList().iterator();

    
    // If no doses were administered we can quit.  Nothing to evaluate
    if(aaIter == null || !aaIter.hasNext())
      return;
    
    // This emulates Figure 4-6 from chapter 4.
    TargetDose          td = tdIter.next();
    AntigenAdministered aa = aaIter.next();
    do {
      // performEvaluation is Chapter 6.
      int tdToSub = performEvaluation(aa, td, ps);
      if(td.isStatusSkipped())
      {
        if(tdIter.hasNext())
          td = tdIter.next();
        else if(SupportingData.isRecurringTargetDose(td.getDoseId())) {
          TargetDose recDose = ps.new TargetDose();
          recDose.setDoseId(td.getDoseId());
          recDose.setDoseNumber(td.getDoseNumber() + 1);
          tdIter.add(recDose);
          // Strange but correct.  .add() puts the cursor past the new element and .next() fails.
          // Further testing may result in the need to call .next() after .previous to move cursor to proper position.
          // It works for now.
          td = tdIter.previous();
        }
        else
        {
          aa.addEvaluationReason("", "", "Patient Series already complete");
          aa.setExtraneous();
          break;
        }
      }
      else if(td.isStatusSubstituted())
      {
        for(int i = 1; i <= tdToSub; i++) {
          td.setStatusSubstituted();
          td = tdIter.next();
        }
      }
      else {
        if(td.isStatusSatisfied()) {
          if(tdIter.hasNext()) {
            td = tdIter.next();
          }
          else if(SupportingData.isRecurringTargetDose(td.getDoseId())) {
            TargetDose recDose = ps.new TargetDose();
            recDose.setDoseId(td.getDoseId());
            recDose.setDoseNumber(td.getDoseNumber() + 1);
            tdIter.add(recDose);
            // Strange but correct.  .add() puts the cursor past the new element and .next() fails.
            // Further testing may result in the need to call .next() after .previous to move cursor to proper position.
            // It works for now.
            td = tdIter.previous();
          }
          else // No more Target Doses to iterate
            break;
        }

        if(aaIter.hasNext()) {
          aa = aaIter.next();
        }
        else
          break;
      }
    } while(true);

    // Set Admin status to "Extraneous" for any extra doses
    while(aaIter.hasNext())
    {
      aa = aaIter.next();
      aa.addEvaluationReason("", "", "Patient Series already complete");
      aa.setExtraneous();
    }
  }

  //============= EVALUATE THIN PROCESS MODEL ================================|
  // This is chapter 6 of the Logic Spec                                      |
  // This method mimics the thin process model at the beginning of chapter 6. |
  //============= EVALUATE THIN PROCESS MODEL ================================|
  private static int performEvaluation(AntigenAdministered aa, TargetDose td, CDSiPatientSeries patientSeries) throws Exception {
    // TODO 6.1 Can the vaccine dose administered be evaluated?
 
    // 6.2 Can the Target Dose be skipped?
    if (conditionallySkipTargetDose(aa, td, patientSeries, aa.getDateAdministered(), "Evaluation", "Initial")) {
      td.setStatusSkipped();
      return 0;
    } 

    // 6.3 Was an inadvertent vaccine administered?
    if (evaluateInadvertentVaccine(aa, td))
    {
      td.setStatusNotSatisified();
      return 0;
    }
    
    // 6.4 Was the vaccine dose adminsitered at a valid age?
    int ageStatus = evaluateAge(aa, td, patientSeries.getPatientData().getPatient().getDob());

    // 6.5 Was the vaccine dose administered at a preferable interval?
    int intStatus = evaluateInterval(aa, td, patientSeries);

    // 6.6 Was the vaccined dose administered at an allowable interval?
    int allowIntStatus = CDSiGlobals.COMPONENT_STATUS_VALID;
    if (intStatus != CDSiGlobals.COMPONENT_STATUS_VALID)
      allowIntStatus = evaluateAllowableInterval(aa, td, patientSeries);
    
    // 6.7 Was the live virus vaccine dose administered in conflict with any previous live virus vaccine doses administered?
    int liveStatus = evaluateLiveVirusConflict(aa, td, patientSeries);
    
    // 6.8 Did the patient recieve a preferable vaccine?
    int prefStatus = evaluatePreferableVaccine(aa, td, patientSeries.getPatientData().getPatient().getDob());

    // 6.9 Did the patient receive an allowable vaccine?
    int allowStatus = CDSiGlobals.COMPONENT_STATUS_VALID;
    if (prefStatus != CDSiGlobals.COMPONENT_STATUS_VALID)
      allowStatus = evaluateAllowableVaccine(aa, td, patientSeries.getPatientData().getPatient().getDob());

    // 6.10 Was the target dose satisfied?
    td.setStatus(isTargetDoseSatisfied(ageStatus, intStatus, allowIntStatus, liveStatus, prefStatus, allowStatus));
    if(td.isStatusSatisfied())
    {
      aa.setValid();
      aa.setSatisfiedTargetDoseNumber(td.getDoseNumber());
    }
    else if (ageStatus == CDSiGlobals.COMPONENT_STATUS_EXTRANEOUS)
    {
      aa.setExtraneous();
      aa.setUnsatisfiedTargetDoseNumber(td.getDoseNumber());
    }
    else
    {
      aa.setNotValid();
      aa.setUnsatisfiedTargetDoseNumber(td.getDoseNumber());
    }
    return 0;
  }

  //================================ DECISION TABLE METHODS ==================|
  //  These methods mimic the decision tables found in Chapter 6.             |
  //  They are called from the performEvaluation Method above                 |
  //================================ DECISION TABLE METHODS ==================|

  // TODO 6.1 Can the vaccine dose administered be evaluated?

  // 6.2 Can the target dose be skipped?
  public static boolean conditionallySkipTargetDose(AntigenAdministered aa, TargetDose td, CDSiPatientSeries ps, Date referenceDate, String context, String processStep) throws Exception {
    if(td == null) return false;

    SDConditionalSkip condSkip = SupportingData.getConditionalSkip(td.getDoseId(), referenceDate, context);
   
    if(condSkip == null || condSkip.getCsSets().isEmpty())
      return false;

    for(SDCSSet csSet : condSkip.getCsSets()) {
      for(SDCSCondition csCondition : csSet.getCsConditions()) {
        // Determine which DT to assess
        if(csCondition.getConditionType().equalsIgnoreCase("age"))
        {
          // Dates needed
          Date CondSkipBeginAgeDate = CDSiDate.calculateDate(ps.getPatientData().getPatient().getDob(), csCondition.getBeginAge(), "01/01/1900");
          Date CondSkipEndAgeDate   = CDSiDate.calculateDate(ps.getPatientData().getPatient().getDob(), csCondition.getEndAge(), "12/31/2999");
          Date refDate              = CDSiDate.dropTime(referenceDate);
          
          // Decision Table Column 1 (Ref Date Between Begin and End)
          if(refDate.compareTo(CondSkipBeginAgeDate) >= 0 && // Ref Date is on or after Begin Age Date AND
             refDate.compareTo(CondSkipEndAgeDate) < 0)      // Ref Date is before End Age Date
            csCondition.setConditionMet(true);
          // Decision Table Column 2 (Ref Date Not between begin and End)
          else if (refDate.compareTo(CondSkipBeginAgeDate) < 0 || // Ref Date is before Begin Age Date OR
                   refDate.compareTo(CondSkipEndAgeDate)>= 0)     // Ref Date is on or after End Age Date
            csCondition.setConditionMet(false);
          else
            throw new Exception("Condition Skip - Age DT has a gap.");
        }
        else if(csCondition.getConditionType().equalsIgnoreCase("interval"))
        {
          // Dates needed
          List<AntigenAdministered> refList = ps.getPatientData().getAntigenAdministeredList();
          List<AntigenAdministered> allAntList = ps.getPatientData().getAllVaccineDosesAdministered();

          // New Dev work for 4.0
          if(refList == null || refList.isEmpty())
          {
            csCondition.setConditionMet(false);
          }
          else
          {
            AntigenAdministered refDose;
            if(context.equalsIgnoreCase("Evaluation")) 
            {
              SDInterval sdInt = new SDInterval();
              sdInt.setFromPreviousDose(true);
              refDose = findReferenceDose(sdInt, aa, refList, allAntList);
            }
            else
            {
              refDose = refList.get(refList.size()-1);
            }

            Date CondSkipIntervalDate = CDSiDate.calculateDate(refDose.getDateAdministered(), csCondition.getInterval(), "");
            Date refDate              = CDSiDate.dropTime(referenceDate);
            // end new Dev work for 4.0

            // Decision Table Column 1
            if(refDate.compareTo(CondSkipIntervalDate) >= 0) // Ref Date is on or after calculated date
              csCondition.setConditionMet(true);
            else if (refDate.compareTo(CondSkipIntervalDate) < 0) // Ref Date is NOT on or after calculated date
              csCondition.setConditionMet(false);
            else
              throw new Exception("Condition Skip - Interval DT has a gap.");

          }
        }
        else if(csCondition.getConditionType().equalsIgnoreCase("vaccine count by age") ||
                csCondition.getConditionType().equalsIgnoreCase("vaccine count by date") )
        {
          // Terms Needed:
          int numOfCondDosesAdmin = getNumberOfConditionalDosesAdmin(csCondition, ps.getPatientData().getAllNeedleSticks(ps.getAntigenId()), ps.getPatientData().getPatient().getDob(), aa, context);
          
          // DT
          if(csCondition.getDoseCountLogic().equalsIgnoreCase("greater than"))
          {
            if(numOfCondDosesAdmin > csCondition.getDoseCount())
              csCondition.setConditionMet(true);
            else
              csCondition.setConditionMet(false);
          }
          else if(csCondition.getDoseCountLogic().equalsIgnoreCase("equal to") || 
                  csCondition.getDoseCountLogic().equalsIgnoreCase("equals"))
          {
            if(numOfCondDosesAdmin == csCondition.getDoseCount())
              csCondition.setConditionMet(true);
            else
              csCondition.setConditionMet(false);
          }
          else if(csCondition.getDoseCountLogic().equalsIgnoreCase("less than"))
          {
            if(numOfCondDosesAdmin < csCondition.getDoseCount())
              csCondition.setConditionMet(true);
            else
              csCondition.setConditionMet(false);
          }
          else
          {
            throw new Exception("Problem identifying dose count logic by age or date.  No logic for type: " + csCondition.getDoseCountLogic());
          }
        }
        else
        {
          throw new Exception ("Problem identifying condtion type.  No logic for type: " + csCondition.getConditionType());
        }
      }
    }
    
    // Determine if the Set is met.  This could be optimized, but wanting to follow logic spec.
    for(SDCSSet csSet : condSkip.getCsSets()) 
    {
      if(csSet.getConditionLogic().equalsIgnoreCase("and"))
      {
        csSet.setSetMet(true);
        for(SDCSCondition csCond : csSet.getCsConditions())
        {
          if(!csCond.isConditionMet())
            csSet.setSetMet(false);
        }
      }
      else if(csSet.getConditionLogic().equalsIgnoreCase("or"))
      {
        csSet.setSetMet(false);
        for(SDCSCondition csCond : csSet.getCsConditions())
        {
          if(csCond.isConditionMet())
            csSet.setSetMet(true);
        }
      }
      else if(csSet.getCsConditions().size() == 1)
      {
        csSet.setSetMet(csSet.getCsConditions().get(0).isConditionMet());
      }
      else
      {
          throw new Exception ("Condition Logic AND/OR missing.");
      }
    }
    
    // Finally!!! Determine if the Target Dose can be skipped
    if(condSkip.getSetLogic().equalsIgnoreCase("and"))
    {
      for(SDCSSet csSet : condSkip.getCsSets()) 
      {
        if(!csSet.isSetMet())
          return false;
      }
  
      // Add all the Reasons
      td.addSkipReason(context + " Context - " + processStep + " Check");
      td.addSkipReason("-- AND Set Logic --");
      for(SDCSSet csSet : condSkip.getCsSets()) 
      {
        td.addSkipReason(getRecEffCessRange(csSet.getEffectiveDate(), csSet.getCessationDate()) + csSet.getDescription());
      }
      return true;
    }
    else if(condSkip.getSetLogic().equalsIgnoreCase("or"))
    {
      boolean ret = false;
      boolean headerPrinted = false;
      for(SDCSSet csSet : condSkip.getCsSets()) 
      {
        if(csSet.isSetMet())
        {
          if(!headerPrinted)
          {
            td.addSkipReason(context + " Context - " + processStep + " Check");
            td.addSkipReason("-- OR Set Logic --");
            headerPrinted = true;
          }
          td.addSkipReason(csSet.getDescription());
          ret = true;
        }
      }
      return ret;
    }
    else if(condSkip.getCsSets().size() == 1)
    {
      if(condSkip.getCsSets().get(0).isSetMet())
      {
        td.addSkipReason(context + " Context - " + processStep + " Check");
        td.addSkipReason("-- N/A Set Logic --");
        td.addSkipReason(condSkip.getCsSets().get(0).getDescription());
      }
      
      return condSkip.getCsSets().get(0).isSetMet();
    }
    else
    {
        throw new Exception ("Set Logic AND/OR missing. (" + context + " Context)");
    }
  }

  // 6.3 Was an inadvertent vaccine administered?
  private static boolean evaluateInadvertentVaccine(AntigenAdministered aa, TargetDose td) throws Exception {
    List<SDInadvertentVaccine> ivList = SupportingData.getInadvertentVaccineData(td.getDoseId());
    
    // If no inadvertent vaccines exist, then it's fine
    if(ivList == null || ivList.isEmpty())
    {
      aa.addEvaluationReason("Inadvertent Vaccine", "Valid", "There are no Supporting Data defined inadvertent vaccines");
      return false;
    }

    // Loop through the list until we find an allowable Vaccine.
    for(SDInadvertentVaccine iv : ivList)
    {
      // Decision Table column 1
      if(aa.getVaccineId() == iv.getVaccineId()) 
      {
        aa.addEvaluationReason("Inadvertent Vaccine", "Not Valid", "An Inadvertent Vaccine was administered");
        aa.setNotValid();
        aa.setUnsatisfiedTargetDoseNumber(td.getDoseNumber());
        aa.setIsInadvertentVaccine();
        return true;
      }
    }

    aa.addEvaluationReason("Inadvertent Vaccine", "Valid", "An Inadvertent Vaccine was not administered");
    return false;
  }

  // 6.4 Was the vaccine dose adminsitered at a valid age?
  private static int evaluateAge(AntigenAdministered aa, TargetDose td, Date dob) throws Exception {
    SDAge sdAge = SupportingData.getAgeData(td.getDoseId(), aa.getDateAdministered());
    DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
    
    // Make sure we have age parameters to evaluate
    if(sdAge == null) {
      aa.addEvaluationReason("Age", "Valid", "No Supporting Data Age Requirements");
      return CDSiGlobals.COMPONENT_STATUS_VALID;
    }


    // Important dates for this operation
    Date adminDate     = CDSiDate.dropTime(aa.getDateAdministered());
    Date absMinAgeDate = CDSiDate.calculateDate(dob, sdAge.getAbsoluteMinimumAge(), "01/01/1900");
    Date minAgeDate    = CDSiDate.calculateDate(dob, sdAge.getMinimumAge(), "01/01/1900");
    Date maxAgeDate    = CDSiDate.calculateDate(dob, sdAge.getMaximumAge(), "12/31/2999");
    
    // VDA < abs Min (Column 1 on DT)
    if (adminDate.before(absMinAgeDate)) {
      aa.addEvaluationReason("Age", sdAge.getEffectiveDate(), sdAge.getCessationDate(), "Too Young", "Administered before " + sdAge.getAbsoluteMinimumAge() + " of age.");
      return CDSiGlobals.COMPONENT_STATUS_NOT_VALID;
    }

    // Abs Min <= VDAdate < min (Columns 2 - 4 on DT)
    // TODO work on the grace period around first dose and previous not valid.
    if (adminDate.before(minAgeDate)) {
      aa.addEvaluationReason("Age", sdAge.getEffectiveDate(), sdAge.getCessationDate(), "Grace Period", "Administered on or after " + sdAge.getAbsoluteMinimumAge() + " but before " + sdAge.getMinimumAge() + " of age.");
      return CDSiGlobals.COMPONENT_STATUS_VALID;
    }

    // min <= VDAdate < max (Column 5 on DT)
    if (adminDate.before(maxAgeDate)) {
//      Administered on or after X but before Y of age
//      Administered on or after X of age
//      Administered before Y of age
      String min = sdAge.getMinimumAge().isEmpty() ? "" : "on or after " + sdAge.getMinimumAge();
      String max = sdAge.getMaximumAge().isEmpty() ? "" : "before " + sdAge.getMaximumAge();

      String details = "Administered " + min + (!min.isEmpty() && !max.isEmpty() ? " but " + max : max) + " of age";

      aa.addEvaluationReason("Age", sdAge.getEffectiveDate(), sdAge.getCessationDate(), "Valid", details);
      return CDSiGlobals.COMPONENT_STATUS_VALID;
    }

    // VDADate >= max (Column 6 on DT)
    if (adminDate.compareTo(maxAgeDate) >= 0) {
      aa.addEvaluationReason("Age", sdAge.getEffectiveDate(), sdAge.getCessationDate(), "too old", "Administered on or after " + sdAge.getMaximumAge() + " of age.");
      return CDSiGlobals.COMPONENT_STATUS_EXTRANEOUS;
    }

    aa.addEvaluationReason("Age", sdAge.getEffectiveDate(), sdAge.getCessationDate(), "", "No Specific Condition found.  Bad Decision Table.");
    return CDSiGlobals.COMPONENT_STATUS_VALID;
  }

  // 6.5 Was the vaccine dose administered at a valid interval?
  private static int evaluateInterval(AntigenAdministered aa, TargetDose td, CDSiPatientSeries patientSeries) throws Exception {
    List<SDInterval> intList = SupportingData.getIntervalData(td.getDoseId(), aa.getDateAdministered());

    // If no intervals are defined, it's a valid interval
    if(intList == null || intList.isEmpty()) {
      aa.addEvaluationReason("Interval", "Valid", "No Supporting Data defined Interval Requirements");
      return CDSiGlobals.COMPONENT_STATUS_VALID;
    }

    // If this is the first VDA, then no interval work can be performed.
    // This prevents a problem with Skipping Target Dose #1
    if(aa.getChronologicalPosition() == 1)
      return CDSiGlobals.COMPONENT_STATUS_VALID;

    boolean isValid = true;

    List<AntigenAdministered> refList = patientSeries.getPatientData().getAntigenAdministeredList();
    List<AntigenAdministered> allAntList = patientSeries.getPatientData().getAllVaccineDosesAdministered();
    
    for(SDInterval sdInt : intList)
    {
      // find the proper reference dose
      AntigenAdministered refDose = findReferenceDose(sdInt, aa, refList, allAntList);

      
      // Local Variables w/ defaults as needed.
      Date adminDate     = CDSiDate.dropTime(aa.getDateAdministered());
//      Date absMinIntDate = CDSiDate.calculateDate(refDose.getDateAdministered(), sdInt.getAbsoluteMinimumInterval(), "01/01/1900");
//      Date minIntDate    = CDSiDate.calculateDate(refDose.getDateAdministered(), sdInt.getMinimumInterval(), "01/01/1900");
      Date absMinIntDate = CDSiDate.calculateDate(refDose == null? null : refDose.getDateAdministered(), refDose == null ? null : sdInt.getAbsoluteMinimumInterval(), "01/01/1900");
      Date minIntDate    = CDSiDate.calculateDate(refDose == null? null : refDose.getDateAdministered(), refDose == null ? null : sdInt.getMinimumInterval(), "01/01/1900");
      String strIntType  = getIntervalType(sdInt);

      // Decision table Column 1 adminDate < absMinInt
      if(adminDate.before(absMinIntDate))
      {
        aa.addEvaluationReason("Interval", sdInt.getEffectiveDate(), sdInt.getCessationDate(), "too soon", "Administered less than " + sdInt.getAbsoluteMinimumInterval() + " from " + strIntType);
        isValid = false;
      }
      // Decision table column 2, 3, 4 (for now) absMinInt <= adminDate < MinInt
      // TODO Work on the grace period around grace period.
      else if(adminDate.before(minIntDate))
      {
        aa.addEvaluationReason("Interval", sdInt.getEffectiveDate(), sdInt.getCessationDate(), "Grace Period", "Administered at least " + sdInt.getAbsoluteMinimumInterval() + " but before " + sdInt.getMinimumInterval() + " from " + strIntType);
      }
      // Decision table column 5 is the else case at this point
      else
        aa.addEvaluationReason("Interval", sdInt.getEffectiveDate(), sdInt.getCessationDate(), "Valid", "Administered at least " + sdInt.getMinimumInterval() + " from " + strIntType);
    }

    return (isValid ? CDSiGlobals.COMPONENT_STATUS_VALID : CDSiGlobals.COMPONENT_STATUS_NOT_VALID);
  }

  // 6.6 Was the vaccine dose administered at an allowable interval?
  private static int evaluateAllowableInterval(AntigenAdministered aa, TargetDose td, CDSiPatientSeries patientSeries) throws Exception {
    List<SDInterval> intList = SupportingData.getAllowableIntervalData(td.getDoseId(), aa.getDateAdministered());

    // If no intervals are defined, it's not a valid interval
    if(intList == null || intList.isEmpty()) {
      aa.addEvaluationReason("Allowable Interval", "Not Valid", "No Supporting Data defined Allowable Intervals exist");
      return CDSiGlobals.COMPONENT_STATUS_NOT_VALID;
    }

    // If this is the first VDA, then no interval work can be performed.
    // This prevents a problem with Skipping Target Dose #1
    if(aa.getChronologicalPosition() == 1)
      return CDSiGlobals.COMPONENT_STATUS_NOT_VALID;

    boolean isValid = true;

    List<AntigenAdministered> refList = patientSeries.getPatientData().getAntigenAdministeredList();
    List<AntigenAdministered> allAntList = patientSeries.getPatientData().getAllVaccineDosesAdministered();
      for(SDInterval sdInt : intList)
    {
      // find the proper reference dose
      AntigenAdministered refDose = findReferenceDose(sdInt, aa, refList, allAntList);

      // Local Variables w/ defaults as needed.
      Date adminDate     = CDSiDate.dropTime(aa.getDateAdministered());
      Date absMinIntDate = CDSiDate.calculateDate(refDose == null? null : refDose.getDateAdministered(), refDose == null ? null : sdInt.getAbsoluteMinimumInterval(), "01/01/1900");
      String strIntType  = getIntervalType(sdInt);
      

      // Decision table Column 1 adminDate < absMinInt
      if(adminDate.before(absMinIntDate))
      {
        aa.addEvaluationReason("Allowable Interval", sdInt.getEffectiveDate(), sdInt.getCessationDate(), "too soon", "Administered less than " + sdInt.getAbsoluteMinimumInterval() + " from " + strIntType);
        isValid = false;
      }
      // Decision table column 2 absMinInt <= adminDate
      else if(!adminDate.before(absMinIntDate))
      {
        aa.addEvaluationReason("Allowable Interval", sdInt.getEffectiveDate(), sdInt.getCessationDate(), "Valid", "Administered at least " + sdInt.getAbsoluteMinimumInterval() + " from " + strIntType);
      }
    }

    return (isValid ? CDSiGlobals.COMPONENT_STATUS_VALID : CDSiGlobals.COMPONENT_STATUS_NOT_VALID);
  }

  // 6.7 Was the live virus vaccine dose administered in conflict with any previous live virus vaccine doses administered?
  private static int evaluateLiveVirusConflict(AntigenAdministered aa, TargetDose td, CDSiPatientSeries patientSeries) throws Exception {
    List<SDLiveVirusConflict> liveList = SupportingData.getLiveVirusConflicts(1);

    // Return a valid status if this isn't a live vaccine
    if(!isLive(liveList, aa.getVaccineId()))
     return CDSiGlobals.COMPONENT_STATUS_VALID;

    for(AntigenAdministered prevAA : patientSeries.getPatientData().getAllVaccineDosesAdministered()) {
      if(isLive(liveList, prevAA.getVaccineId())) {
        SDLiveVirusConflict lvc = getConflictData(liveList, prevAA.getVaccineId(), aa.getVaccineId());
        Date adminDate     = CDSiDate.dropTime(aa.getDateAdministered());
        Date beginIntDate  = CDSiDate.calculateDate(prevAA.getDateAdministered(), lvc.getBeginInterval(), "01/01/1900");
        Date endIntDate    = CDSiDate.calculateDate(prevAA.getDateAdministered(), lvc.getEndInterval(), "01/01/1900");
        if(prevAA.getEvaluationStatus().equals(CDSiGlobals.ANTIGEN_ADMINISTERED_VALID) || prevAA.getEvaluationStatus().isEmpty())
          endIntDate = CDSiDate.calculateDate(prevAA.getDateAdministered(), lvc.getMinimumEndInterval(), "01/01/1900");

        if(adminDate.compareTo(beginIntDate) >= 0 && adminDate.before(endIntDate))
        {
          aa.addEvaluationReason("Live Virus Conflict", "Conflict", "Conflict with " + prevAA.getTradeName() + " on " + new SimpleDateFormat("MM/dd/yyyy").format(prevAA.getDateAdministered()));
          return CDSiGlobals.COMPONENT_STATUS_NOT_VALID;
        }
      }
    }
    aa.addEvaluationReason("Live Virus Conflict", "No Conflicts", "No Conflicts with other live virus vaccines");
    return CDSiGlobals.COMPONENT_STATUS_VALID;
  }

  // 6.8 Did the patient receive a preferable vaccine?
  private static int evaluatePreferableVaccine(AntigenAdministered aa, TargetDose td, Date dob) throws Exception {
    List<SDPreferableVaccine> pvList = SupportingData.getPreferableVaccineData(td.getDoseId());

    // < 2017 comment: this should never happen.  There should always be at least one Preferable Vaccine.
    // 3/24/2017 comment: This happened.  There is no longer a valid OPV product, but we still need to validate historical OPV.
    //                    as such, we have a series with no preferable vaccines.
    if(pvList == null || pvList.isEmpty())
    {
      aa.addEvaluationReason("Preferable Vaccine", "Not Valid", "There are no Supporting Data defined preferable vaccines.");
      return CDSiGlobals.COMPONENT_STATUS_NOT_VALID;
    }

    final int COL3_ERROR = 0;
    final int COL4_ERROR = 1;
    final int COL5_ERROR = 2;

    int errorType = 0;

    // Loop through the list until we find a preferable Vaccine.
    for(SDPreferableVaccine pv : pvList)
    {
      // Local Variables
      Date   beginDate = CDSiDate.calculateDate(dob, pv.getBeginAge(), "01/01/1900");
      Date   endDate   = CDSiDate.calculateDate(dob, pv.getEndAge(),   "12/31/2999");
      Date   adminDate = aa.getDateAdministered();
      int    adminMfg  = aa.getManufacturerId();
      double adminVol  = aa.getVolume();
      int    sdMfgId   = (pv.getManufacturerId() == 0 ? aa.getManufacturerId() : pv.getManufacturerId());
      double sdVolume  = (pv.getVolume() == 0.0 ? aa.getVolume() : pv.getVolume());


      // Decision Table column 1
      if(aa.getVaccineId() == pv.getVaccineId() &&
         (adminDate.compareTo(beginDate) >= 0) &&
         adminDate.before(endDate) &&
         adminMfg == sdMfgId &&
         adminVol >= sdVolume)
      {
        aa.addEvaluationReason("Preferable Vaccine", "Valid", "A Preferable Vaccine was administered");
        return CDSiGlobals.COMPONENT_STATUS_VALID;
      }

      // Decision Table column 2
      if(aa.getVaccineId() == pv.getVaccineId() &&
         (adminDate.compareTo(beginDate) >= 0) &&
         adminDate.before(endDate) &&
         adminMfg == sdMfgId &&
         adminVol < sdVolume)
      {
        aa.addEvaluationReason("Preferable Vaccine", "Valid", "A Preferable Vaccine was administered, but volume was less than recommended volume");
        return CDSiGlobals.COMPONENT_STATUS_VALID;
      }

      // Decision Table column 3
      // This is the fall through of the loop.

      // Decision Table Column 4
      // Right vaccine, wrong age range (Either before begin date or on or after endDate
      if(aa.getVaccineId() == pv.getVaccineId() &&
         (adminDate.before(beginDate) ||
           (adminDate.compareTo(endDate) >= 0)))
      {
        // Track this error unless we have a more specific error already noted.
        errorType = (errorType < COL4_ERROR ? COL4_ERROR : errorType);
      }

      // Decision Table Column 5
      // Right vaccine, right age range, wrong Trade Name
      if(aa.getVaccineId() == pv.getVaccineId() &&
         (adminDate.compareTo(beginDate) >= 0) &&
         adminDate.before(endDate) &&
         adminMfg != sdMfgId)
      {
        errorType = COL5_ERROR;
      }
    }

    // Set the proper Evaluation Reason
    if(errorType == COL3_ERROR)
      aa.addEvaluationReason("Preferable Vaccine", "Not Valid", "The vaccine administered was not a preferable vaccine");
    else if(errorType == COL4_ERROR)
      aa.addEvaluationReason("Preferable Vaccine", "Not Valid", "A Preferable Vaccine was administered, but it was outside of the preferred age range");
    else
      aa.addEvaluationReason("Preferable Vaccine", "Not Valid", "The vaccine administered was not the correct trade name");

    // we didn't find a preferable vaccine, Mark Component Not Valid.
    return CDSiGlobals.COMPONENT_STATUS_NOT_VALID;

  }

  // 6.9  Did the patient receive an allowable vaccine?
  private static int evaluateAllowableVaccine(AntigenAdministered aa, TargetDose td, Date dob) throws Exception {
    List<SDAllowableVaccine> avList = SupportingData.getAllowableVaccineData(td.getDoseId());

    // If no allowable vaccines exist
    if(avList == null || avList.isEmpty())
    {
      aa.addEvaluationReason("Allowable Vaccine", "Not Valid", "There are no Supporting Data defined allowable vaccines");
      return CDSiGlobals.COMPONENT_STATUS_NOT_VALID;
    }

    final int COL2_ERROR = 0;
    final int COL3_ERROR = 1;

    int errorType = COL2_ERROR;

    // Loop through the list until we find an allowable Vaccine.
    for(SDAllowableVaccine av : avList)
    {
      // Local Variables
      Date   beginDate = CDSiDate.calculateDate(dob, av.getBeginAge(), "01/01/1900");
      Date   endDate   = CDSiDate.calculateDate(dob, av.getEndAge(),   "12/31/2999");
      Date   adminDate = aa.getDateAdministered();


      // Decision Table column 1
      if(aa.getVaccineId() == av.getVaccineId() &&
         (adminDate.compareTo(beginDate) >= 0) &&
         adminDate.before(endDate))
      {
        aa.addEvaluationReason("Allowable Vaccine", "Valid", "An Allowable Vaccine was administered");
        return CDSiGlobals.COMPONENT_STATUS_VALID;
      }

      // Decision Table column 2
      // This is the fall through of the loop.

      // Decision Table Column 3
      // Right vaccine, wrong age range (Either before begin date or on or after endDate
      if(aa.getVaccineId() == av.getVaccineId() &&
         (adminDate.before(beginDate) ||
           (adminDate.compareTo(endDate) >= 0)))
      {
         // Track this error unless we have a more specific error already noted.
        errorType = COL3_ERROR;
      }
    }

    // Set the proper Evaluation Reason
    if(errorType == COL2_ERROR)
      aa.addEvaluationReason("Allowable Vaccine", "Not Valid", "The vaccine administered was not an allowable vaccine");
    else
      aa.addEvaluationReason("Allowable Vaccine", "Not Valid", "An Allowable Vaccine was administered, but it was outside of the allowable age range");

    // we didn't find an allowable vaccine, Mark Component Not Valid.
    return CDSiGlobals.COMPONENT_STATUS_NOT_VALID;

  }

  // 6.10 Is Target Dose Satisfied
  private static String isTargetDoseSatisfied(int ageStatus,
                                              int intervalStatus,
                                              int allowIntStatus,
                                              int liveStatus,
                                              int preferableStatus,
                                              int allowableStatus) {
    
    // join interval statuses together.  If one or the other are valid, then joint interval is valid.
    int jointIntStatus = 
       (intervalStatus == CDSiGlobals.COMPONENT_STATUS_VALID ||
        allowIntStatus == CDSiGlobals.COMPONENT_STATUS_VALID) ? 
            CDSiGlobals.COMPONENT_STATUS_VALID : 
            CDSiGlobals.COMPONENT_STATUS_NOT_VALID;

    // join vaccine statuses together.  If one or the other are valid, then joint vaccine is valid.
    int jointVacStatus = 
       (preferableStatus == CDSiGlobals.COMPONENT_STATUS_VALID ||
        allowableStatus == CDSiGlobals.COMPONENT_STATUS_VALID) ? 
            CDSiGlobals.COMPONENT_STATUS_VALID : 
            CDSiGlobals.COMPONENT_STATUS_NOT_VALID;

            
    if(ageStatus         == CDSiGlobals.COMPONENT_STATUS_EXTRANEOUS ||
       ageStatus         == CDSiGlobals.COMPONENT_STATUS_NOT_VALID  ||
       jointIntStatus    == CDSiGlobals.COMPONENT_STATUS_NOT_VALID  ||
       liveStatus        == CDSiGlobals.COMPONENT_STATUS_NOT_VALID  ||
       jointVacStatus    == CDSiGlobals.COMPONENT_STATUS_NOT_VALID)
    {
      return CDSiGlobals.TARGET_DOSE_NOT_SATISFIED;
    }
    return CDSiGlobals.TARGET_DOSE_SATISFIED;
  }




  //================================ HELPER METHODS ==========================|
  //  Just to keep the "decision tables" clean and Helper Methods to          |
  //  Some specific business rules and/or Java-heavy tasks                    |
  //================================ HELPER METHODS ==========================|
  private static String getIntervalType(SDInterval sdInt) throws Exception {
    if(sdInt.isFromPreviousDose())
      return "previous dose";
    else if (sdInt.isFromTargetDose())
      return "target dose #" + sdInt.getFromTargetDoseNubmer();
    else if (sdInt.isFromMostRecent())
      return "most recent vaccine";
    else
      throw new Exception("Interval - Unable to detect type of interval.  From Previous, TargetDose, or MostRecent");
  }
  
  private static AntigenAdministered findReferenceDose(SDInterval sdInt, AntigenAdministered aa, List<AntigenAdministered> refList, List<AntigenAdministered> allAntList) throws Exception {
    if(sdInt.isFromPreviousDose())
      return findPreviousDose(aa.getChronologicalPosition(), refList);
    else if (sdInt.isFromTargetDose())
      return findTargetDose(sdInt.getFromTargetDoseNubmer(), refList);
    else if (sdInt.isFromMostRecent())
      return findMostRecent(sdInt, allAntList, aa);
    else
      throw new Exception("Interval - Unable to detect type of interval.  From Previous, TargetDose, or MostRecent");
  }

  // There is a more efficient way to do this by starting backwards.  Someday I'll do that.
  public static AntigenAdministered findPreviousDose(int chronologicalPosition, List<AntigenAdministered> refList) throws Exception {
    int referencePoint = 1;
    do {
      for(AntigenAdministered aa : refList)
      {
        if(aa.getChronologicalPosition() == (chronologicalPosition - referencePoint) &&
           !aa.isInadvertentVaccine())
          if(!aa.isInadvertentVaccine())
            return aa;
      }
      referencePoint++;
    } while (referencePoint <= refList.size());
    
    // Interval - Unable to find previous dose.  There likely isn't one
    return null;
  }

  // This is used by the forecaster too.  Might consider some code refactor some day.
  public static AntigenAdministered findTargetDose(int targetDoseNum, List<AntigenAdministered> refList) throws Exception {
    for(AntigenAdministered aa : refList)
    {
      if(aa.getSatisfiedTargetDoseNumber() == targetDoseNum)
        return aa;
    }
    throw new Exception("Interval - Unable to find Target Dose Number.  Target Dose wanted is: " + targetDoseNum);
  }

  // This is used by the forecaster too.  Might consider some code refactor some day.
  public static AntigenAdministered findMostRecent(SDInterval sdInt, List<AntigenAdministered> refList, AntigenAdministered currAA) throws Exception {
    
    for(int i = refList.size()-1; i >=0; i--)
    {
      AntigenAdministered aa = refList.get(i);
      if(sdInt.containsVaccineId(aa.getVaccineId()))
        return aa;
    }
    return null;
  }
  
  private static boolean isLive(List<SDLiveVirusConflict> liveList, int vaccineId) {
    for(SDLiveVirusConflict lvc : liveList) {
      if(lvc.getCurrentVaccineId() == vaccineId)
        return true;
    }
    return false;
  }

  // This is used by the forecaster too.  Might consider some code refactor some day.
  public static SDLiveVirusConflict getConflictData(List<SDLiveVirusConflict> liveList, int prevVaccId, int curVaccId) {
    for(SDLiveVirusConflict lvc : liveList) {
      if(lvc.getCurrentVaccineId()  == curVaccId &&
         lvc.getPreviousVaccineId() == prevVaccId)
        return lvc;
    }
    return null;
  }

  // This is used by the forecaster too.  Might consider some code refactor some day.
  public static boolean isTriggerTargetDoseSatisfied(int triggerTargetDose, CDSiPatientSeries ps) {
    if(triggerTargetDose == 0) return false;

    for(TargetDose td : ps.getTargetDoses()) {
      if(td.getDoseNumber() == triggerTargetDose &&
         td.isStatusSatisfied())
        return true;
    }

    return false;
  }

  // This used to calculate Business Rule CONDSKIP-1
  private static int getNumberOfConditionalDosesAdmin(SDCSCondition csCondition, List<AntigenAdministered> aaList, Date dob, AntigenAdministered curAA, String context) throws Exception{
    int count = 0;
    // Some Dates
    Date startDate = null;
    Date endDate   = null;
    // By Age
    if(csCondition.getConditionType().equalsIgnoreCase("vaccine count by age"))
    {
      startDate = CDSiDate.calculateDate(dob, csCondition.getBeginAge(), "01/01/1900");
      endDate   = CDSiDate.calculateDate(dob, csCondition.getEndAge(), "12/31/2999");
    }
    // By Date
    else
    {
      startDate = csCondition.getStartDate();
      endDate   = csCondition.getEndDate();
      
      if(startDate == null)
        startDate = dropTime(new SimpleDateFormat("MM/dd/yyyy").parse("01/01/1900"));
      if(endDate == null)
        endDate = dropTime(new SimpleDateFormat("MM/dd/yyyy").parse("12/31/2999"));
    }
    
    // Only count doses up to, but not including the current vaccine during evaluation
    if(context.equalsIgnoreCase("Evaluation") && curAA != null)
      endDate = curAA.getDateAdministered();
    
    for(AntigenAdministered aa : aaList)
    {
      Date tmpDate = CDSiDate.dropTime(aa.getDateAdministered());
      if(csCondition.getVacIdList().isEmpty() || csCondition.containsVaccineId(aa.getVaccineId()))
      {
        if(tmpDate.compareTo(startDate) >= 0 &&
           tmpDate.before(endDate))
        {
          if(csCondition.getDoseType().equalsIgnoreCase("valid"))
          {
            if(aa.isValid())
            {
              count++;
            }
          }
          else if (csCondition.getDoseType().equalsIgnoreCase("total"))
          {
            count++;
          }
          else
            throw new Exception("Dose Type is something other than valid or total. Value is: " + csCondition.getDoseType());
        }
      }
    }
    return count;
  }

  private static String getRecEffCessRange(Date effectiveDate, Date cessationDate) {
    if (effectiveDate == null && cessationDate == null)
      return "";
    
    DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
    return (effectiveDate == null ? "01/01/1900" : df.format(effectiveDate)) + " - " + (cessationDate == null ? "12/31/2999" : df.format(cessationDate)) + " | ";
  }


}
