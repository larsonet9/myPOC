/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.cdc.cdsi.engine;

import gov.cdc.cdsi.db.DBGetter;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author eric
 */
public class CDSiPatientData implements Serializable {
  private Patient                   patient           = new Patient();
  private List<AntigenAdministered> aaList            = new ArrayList();
  private Forecast                  forecast          = new Forecast();
  private List<MedicalHistory>      medHxList         = new ArrayList();

  
  public Patient getPatient() {
    return patient;
  }

  public void setPatient(Patient patient) {
    this.patient = patient;
  }

  public void setPatient(Date dob, String gender) {
    this.patient.setDob(dob);
    this.patient.setGender(gender);
  }

  public void addMedicalHistory(String medHistoryCode, String medHistoryCodeSystem) {
    addMedicalHistory(new MedicalHistory(medHistoryCode, medHistoryCodeSystem));
  }
  
  public void addMedicalHistory(String medHistoryCode, String medHistoryCodeSystem, int vaccGrpId) {
    addMedicalHistory(new MedicalHistory(medHistoryCode, medHistoryCodeSystem, vaccGrpId));
  }

  public void addMedicalHistory(MedicalHistory mhx) {
    if(mhx == null) return;
    medHxList.add(mhx);
  }
  
  public boolean hasMedicalHistory() {
    return !medHxList.isEmpty();
  }
    
  public List<MedicalHistory> getMedicalHistory() {
    return medHxList;
  }


  public void addAntigenAdministered(AntigenAdministered aa) {
    this.aaList.add(aa);
  }

  public List<AntigenAdministered> getAntigenAdministeredList() {
    List<AntigenAdministered> evalList = new ArrayList();
    for(AntigenAdministered aa : aaList) {
      if(aa.mustEvaluate()) evalList.add(aa);
    }
    return evalList;
  }

  public List<AntigenAdministered> getAllVaccineDosesAdministered() {
    return aaList;
  }

  public List<AntigenAdministered> getAllNeedleSticks(int antigenId) throws Exception {
    List<AntigenAdministered> shotList = new ArrayList();
    for(AntigenAdministered aa : aaList) {
      if(DBGetter.isMultiAntigenShot(aa, patient.getDob())) {
        if(aa.getAntigenId() == antigenId)
          shotList.add(aa);
      }
      else
          shotList.add(aa);
    }
    return shotList;
  }
  private boolean isAlreadyIn(AntigenAdministered aa, List<AntigenAdministered> shotList) {
    for(AntigenAdministered shotAA : shotList) {
      if(shotAA.getDateAdministered().equals(aa.getDateAdministered()) &&
         shotAA.getCvx().equals(aa.getCvx()))
        return true;
    }
    return false;
  }
  
  public AntigenAdministered getFirstSatisfiedTargetDose() {
    for(AntigenAdministered aa : aaList) {
      if(aa.isValid())
        return aa;
    }
    return null;
  }

  public int getCountOfValidDoses() {
    int cntValid = 0;
    for(AntigenAdministered aa : aaList) {
      if(aa.isValid())
        cntValid++;
    }
    return cntValid;
  }

  public boolean hasAllValidDoses() {
    for(AntigenAdministered aa : aaList) {
      if(!aa.isValid())
        return false;
    }
    return true;
  }

  public Date getLastValidDoseDate() {
    for(int i = aaList.size() - 1; i>= 0; i--) {
      if(aaList.get(i).isValid())
        return aaList.get(i).getDateAdministered();
    }

    return null;
  }


  public Forecast getForecast() {
    return forecast;
  }

  public void setForecast(Forecast forecast) {
    this.forecast = forecast;
  }

  public String toString() {
    String str = patient.toString();
    str += "<h3>Immunization History Evaluation</h3>";
    str += "<table border=\"1\"><tr>";
    str += "<th>Dose#</th>";
    str += "<th>TD#</th>";
    str += "<th>Date Admin</th>";
    str += "<th>Dose Administered</th>";
/*
    str += "<th>VaccineId</th>";
    str += "<th>MfgId</th>";
    str += "<th>AntigenId</th>";
    str += "<th>Dose Condition</th>";
    str += "<th>Volume</th>";
    str += "<th>Expiration Date</th>";
    str += "<th>Exp Eval Status</th>";
    str += "<th>Exp Eval Reason</th>";
*/
    str += "<th>Evaluation Status</th>";
    str += "<th>Evaluation Reason</th></tr>";

    for(AntigenAdministered aa : getAntigenAdministeredList())
    {
      str+= aa.toString();
    }
    str += "</table>";

    str += "<h3>Forecast</h3>";
    str += forecast.toString();

    return str;
  }

  protected class Patient  implements Serializable {
    private Date   dob;
    private String gender;

    public Date getDob() {
      return dob;
    }

    public void setDob(Date dob) {
      this.dob = dob;
    }

    public String getGender() {
      return gender;
    }

    public void setGender(String gender) {
      this.gender = gender;
    }

    public String toString()
    {
      return "DOB: " + new SimpleDateFormat("MM/dd/yyyy").format(dob) + "<br>GENDER: " + gender + "<br><br>";
    }

  }

  public class MedicalHistory implements Serializable {
    private String  medHistoryCode    = "";
    private String  medHistoryCodeSys = "";
    private int     vaccineGroupId;

    private MedicalHistory(String medHistoryCode, String medHistoryCodeSystem) {
      this.medHistoryCode = medHistoryCode;
      this.medHistoryCodeSys = medHistoryCodeSystem;
    }

    private MedicalHistory(String medHistoryCode, String medHistoryCodeSystem, int vaccineGroupId) {
      this.medHistoryCode    = medHistoryCode;
      this.medHistoryCodeSys = medHistoryCodeSystem;
      this.vaccineGroupId    = vaccineGroupId;
    }
 
    public String getMedHistoryCode() {
      return medHistoryCode;
    }

    public void setMedHistoryCode(String medHistoryCode) {
      this.medHistoryCode = medHistoryCode;
    }

    public String getMedHistoryCodeSys() {
      return medHistoryCodeSys;
    }

    public void setMedHistoryCodeSys(String medHistoryCodeSys) {
      this.medHistoryCodeSys = medHistoryCodeSys;
    }

    public int getVaccineGroupId() {
      return vaccineGroupId;
    }

    public void setVaccineGroupId(int vaccineGroupId) {
      this.vaccineGroupId = vaccineGroupId;
    }
    
    public boolean isVaccineGroupSpecificCondition() {
      return this.vaccineGroupId > 0;
    }
  }
  
  public class AntigenAdministered implements Serializable  {
    private int     doseId            = 0;
    private int     chronologicalPosition = 0;
    private int     vaccineId         = 0;
    private int     manufacturerId    = 0;
    private int     antigenId         = 0;
    private String  cvx               = "";
    private String  mvx               = "";
    private String  tradeName         = "";
    private Date    dateAdministered;
    private boolean doseCondition     = false;
    private double  volume            = 1.0;
    private Date    expirationDate;
    private boolean mustEvaluate      = false;
    private String  evaluationStatus  = "";
//    private String  evaluationReason  = "";
    private int     satisfiedTargetDoseNumber;
    private int     unsatisfiedTargetDoseNumber;
    private String  expectedEvalStatus = "";
    private String  expectedEvalReason = "";
    private List<EvaluationReason>    evaluationReasons   = new ArrayList();
    private boolean inadvertentVaccine = false;

    public void setIsInadvertentVaccine() {
      inadvertentVaccine = true;
    }
    
    public boolean isInadvertentVaccine() {
      return inadvertentVaccine;
    }
    
    public String getExpectedEvalReason() {
      return expectedEvalReason;
    }

    public void setExpectedEvalReason(String expectedEvalReason) {
      this.expectedEvalReason = expectedEvalReason;
    }

    public String getExpectedEvalStatus() {
      return expectedEvalStatus;
    }

    public void setExpectedEvalStatus(String expectedEvalStatus) {
      this.expectedEvalStatus = expectedEvalStatus;
    }

    

    public boolean mustEvaluate() {
      return mustEvaluate;
    }

    public void setMustEvaluate(boolean mustEvaluate) {
      this.mustEvaluate = mustEvaluate;
    }


    public String getCvx() {
      return cvx;
    }

    public void setCvx(String cvx) {
      this.cvx = cvx;
    }

    public String getMvx() {
      return mvx;
    }

    public void setMvx(String mvx) {
      this.mvx = mvx;
    }

    public String getTradeName() {
      return tradeName;
    }

    public void setTradeName(String tradeName) {
      this.tradeName = tradeName;
    }

    public int getChronologicalPosition() {
      return chronologicalPosition;
    }

    public void setChronologicalPosition(int chronologicalPosition) {
      this.chronologicalPosition = chronologicalPosition;
    }

    public int getDoseId() {
      return doseId;
    }

    public void setDoseId(int doseId) {
      this.doseId = doseId;
    }

    public int getSatisfiedTargetDoseNumber() {
      return satisfiedTargetDoseNumber;
    }

    public void setSatisfiedTargetDoseNumber(int satisfiedTargetDoseNumber) {
      this.satisfiedTargetDoseNumber = satisfiedTargetDoseNumber;
    }

    public int getUnsatisfiedTargetDoseNumber() {
      return unsatisfiedTargetDoseNumber;
    }

    public void setUnsatisfiedTargetDoseNumber(int unsatisfiedTargetDoseNumber) {
      this.unsatisfiedTargetDoseNumber = unsatisfiedTargetDoseNumber;
    }

    public Date getDateAdministered() {
      return dateAdministered;
    }

    public void setDateAdministered(Date dateAdministered) {
      this.dateAdministered = dateAdministered;
    }

    public boolean isDoseCondition() {
      return doseCondition;
    }

    public void setDoseCondition(boolean doseCondition) {
      this.doseCondition = doseCondition;
    }

    public int getManufacturerId() {
      return manufacturerId;
    }

    public void setManufacturerId(int manufacturerId) {
      this.manufacturerId = manufacturerId;
    }

    public int getVaccineId() {
      return vaccineId;
    }

    public void setVaccineId(int vaccineId) {
      this.vaccineId = vaccineId;
    }

    public double getVolume() {
      return volume;
    }

    public void setVolume(double volume) {
      this.volume = volume;
    }

    public int getAntigenId() {
      return antigenId;
    }

    public void setAntigenId(int antigenId) {
      this.antigenId = antigenId;
    }

    public String getEvaluationReason() {
      String concatStr = "";
      for (EvaluationReason er : evaluationReasons) {
        concatStr += er.getConcept() + ": " + er.getValidity() + " -- " + er.getDetails() + "<br>";
      }
      return concatStr;
    }
    
    public String getEvaluationReasonsInTableFmt() {
      String str = "<table>";
        for(EvaluationReason er : evaluationReasons) {
          str += er.toString();
        }
      return str + "</table>";
    }

    public void addEvaluationReason(String concept, String validity, String details) {
      this.evaluationReasons.add(new EvaluationReason(concept, null, null, validity, details));
    }
    
    public void addEvaluationReason(String concept, Date effectiveDate, Date cessationDate, String validity, String details) {
      this.evaluationReasons.add(new EvaluationReason(concept, effectiveDate, cessationDate, validity, details));
    }

    public String getEvaluationStatus() {
      return evaluationStatus;
    }

    public void setEvaluationStatus(String evaluationStatus) {
      this.evaluationStatus = evaluationStatus;
    }

    public Date getExpirationDate() {
      return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
      this.expirationDate = expirationDate;
    }

    public void setExtraneous() {
      this.evaluationStatus = CDSiGlobals.ANTIGEN_ADMINISTERED_EXTRANEOUS;
    }

    public void setValid() {
      this.evaluationStatus = CDSiGlobals.ANTIGEN_ADMINISTERED_VALID;
    }

    public void setNotValid() {
      this.evaluationStatus = CDSiGlobals.ANTIGEN_ADMINISTERED_NOT_VALID;
    }

    public void setSubStandard() {
      this.evaluationStatus = CDSiGlobals.ANTIGEN_ADMINISTERED_SUB_STANDARD;
    }

    public boolean isValid() {
      return this.evaluationStatus.equals(CDSiGlobals.ANTIGEN_ADMINISTERED_VALID);
    }

    public String toString() {
      String str = "";

      if(expectedEvalStatus == null || evaluationStatus.equalsIgnoreCase(expectedEvalStatus))
        str = "<tr>";
      else
        str = "<tr class=\"evalMismatch\">";

      str += "<td>" + doseId + "</td>";
      str += "<td>" + (satisfiedTargetDoseNumber == 0 ? "-" : satisfiedTargetDoseNumber) + "</td>";
      str += "<td>" + new SimpleDateFormat("MM/dd/yyyy").format(dateAdministered) + "</td>";
      str += "<td>" + tradeName + " [" + cvx + " - " + mvx + "]";
/*
      str += "<td>" + vaccineId + "</td>";
      str += "<td>" + manufacturerId + "</td>";
      str += "<td>" + antigenId + "</td>";
      str += "<td>" + doseCondition + "</td>";
      str += "<td>" + volume + "</td>";
      str += "<td>" + (expirationDate == null ? "" : new SimpleDateFormat("MM/dd/yyyy").format(expirationDate)) + "</td>";
*/
      str += "<td>";
      if(expectedEvalStatus != null && !expectedEvalStatus.isEmpty())
        str +=   "<b><i>Exp. Status</b></i><br>" + expectedEvalStatus + "<br><br><b><i>Act. Status</b></i><br>";
      str +=   evaluationStatus;
      str += "</td>";
      str += "<td>";
      if (expectedEvalReason != null)
        str += "<b><i>Expected Reason:</b></i> " + expectedEvalReason + "<br><br><b><i>Actual Reasons:</b></i>";
      str +=   "<table>";
      str +=     "<tr>";
      str +=     "  <th>Eval Section</th>";
      str +=     "  <th>ACIP Eff/Cess Range</th>";
      str +=     "  <th>Validity</th>";
      str +=     "  <th>Eval Details</th>";
      str +=     "</tr>";
      for(EvaluationReason er : evaluationReasons)
        str += er.toString();
      str +=   "</table>";
      str += "</td>";
      str += "</tr>";

      return str;
    }
  }

  protected class EvaluationReason implements Serializable {
    private String concept;
    private String recEffCessDateRange;
    private String validity;
    private String details;
    
    public EvaluationReason(String concept, Date effectiveDate, Date cessationDate, String validity, String details) {
      this.concept = concept;
      this.validity = validity;
      this.details = details;
      
      if(effectiveDate == null && cessationDate == null) {
        recEffCessDateRange = "-";
      }
      else {
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        recEffCessDateRange = (effectiveDate == null ? "01/01/1900" : df.format(effectiveDate)) + " - " + (cessationDate == null ? "12/31/2999" : df.format(cessationDate));

      }
        
    }

    public String toString() {
      String str;
      
      str  = "<tr>";
      str += "  <td>"+concept+"</td>";
      str += "  <td>"+recEffCessDateRange+"</td>";
      str += "  <td>"+validity+"</td>";
      str += "  <td>"+details+"</td>";
      str += "</tr>";
      
      return str;
    }
    
    public String getConcept() {
      return concept;
    }

    public String getValidity() {
      return validity;
    }

    public String getDetails() {
      return details;
    }
  }

  public class Forecast implements Serializable  {
    private Date   earliestDate;
    private Date   adjustedRecommendedDate;
    private Date   adjustedPastDueDate;
    private Date   latestDate;
    private Date   unadjustedRecommendedDate;
    private Date   unadjustedPastDueDate;
    private String reason = "";
    private String status;
    private String antigensNeeded;
    private int    vaccineGroupId;
    private int    targetDoseNumber;
    private int    forecastNumber;
    private List<String> vaccineTypes = new ArrayList();
    private String intervalPriorityFlag = "";

    public String getIntervalPriorityFlag() {
      return intervalPriorityFlag;
    }

    public void setIntervalPriorityFlag(String intervalPriorityFlag) {
      this.intervalPriorityFlag = intervalPriorityFlag;
    }
    
    public boolean isIntervalPriority() {
      if (intervalPriorityFlag == null || intervalPriorityFlag.isEmpty()) return false;
      if (intervalPriorityFlag.equalsIgnoreCase("override")) return true;
      
      return false;
    }
    
    public int getTargetDoseNumber() {
      return targetDoseNumber;
    }

    public void setTargetDoseNumber(int targetDoseNumber) {
      this.targetDoseNumber = targetDoseNumber;
    }

    public int getForecastNumber() {
      return forecastNumber;
    }

    public void setForecastNumber(int forecastNumber) {
      this.forecastNumber = forecastNumber;
    }

    public boolean hasForecast() {
      return earliestDate != null;
    }


    public Date getAdjustedPastDueDate() {
      return adjustedPastDueDate;
    }

    public void setAdjustedPastDueDate(Date adjustedPastDueDate) {
      this.adjustedPastDueDate = adjustedPastDueDate;
    }

    public Date getAdjustedRecommendedDate() {
      return adjustedRecommendedDate;
    }

    public void setAdjustedRecommendedDate(Date adjustedRecommendedDate) {
      this.adjustedRecommendedDate = adjustedRecommendedDate;
    }

    public String getAntigensNeeded() {
      return antigensNeeded;
    }

    public void setAntigensNeeded(String antigensNeeded) {
      this.antigensNeeded = antigensNeeded;
    }

    public Date getEarliestDate() {
      return earliestDate;
    }

    public void setEarliestDate(Date earliestDate) {
      this.earliestDate = earliestDate;
    }

    public Date getLatestDate() {
      return latestDate;
    }

    public void setLatestDate(Date latestDate) {
      this.latestDate = latestDate;
    }

    public String getReason() {
      return reason;
    }

    public void setReason(String reason) {
      this.reason = reason;
    }

    public String getStatus() {
      return status;
    }

    public void setStatus(String status) {
      this.status = status;
    }

    public Date getUnadjustedPastDueDate() {
      return unadjustedPastDueDate;
    }

    public void setUnadjustedPastDueDate(Date unadjustedPastDueDate) {
      this.unadjustedPastDueDate = unadjustedPastDueDate;
    }

    public Date getUnadjustedRecommendedDate() {
      return unadjustedRecommendedDate;
    }

    public void setUnadjustedRecommendedDate(Date unadjustedRecommendedDate) {
      this.unadjustedRecommendedDate = unadjustedRecommendedDate;
    }

    public int getVaccineGroupId() {
      return vaccineGroupId;
    }

    public void setVaccineGroupId(int vaccineGroupId) {
      this.vaccineGroupId = vaccineGroupId;
    }

    public List<String> getVaccineTypes() {
      return vaccineTypes;
    }

    public void setVaccineTypes(List<String> vaccineTypes) {
      this.vaccineTypes = vaccineTypes;
    }
    
    public void addVaccineType(String vaccineType) {
      this.vaccineTypes.add(vaccineType);
    }
    
    public String writeRecommendedVaccineTypes() {
      String ret = "";
      for(String str : vaccineTypes)
      {
        ret += str + "<br>";
      }
      return ret;
    }

    public String toString() {
      String str = "<table border=\"1\"><tr>";
      str += "<th>Forecast TD#</th>";
      str += "<th>Earliest Date</th>";
      str += "<th>Recommended Date</th>";
      str += "<th>Past Due Date</th>";
      str += "<th>Latest Date</th>";
      str += "<th>reason</th>";
      str += status         == null || status.isEmpty()         ? "" : "<th>status</th>";
      str += antigensNeeded == null || antigensNeeded.isEmpty() ? "" : "<th>antigens Needed</th>";
      str += vaccineTypes   == null || vaccineTypes.isEmpty()   ? "" : "<th>Vaccine Type Forecast</th>";
/*
      str += "<th>unadjusted Recommended Date</th>";
      str += "<th>unadjusted Past Due Date</th>";
      str += "<th>Vaccine Group Id</th>";
*/
      str += "</tr><tr>";
      str += "<td>" + targetDoseNumber + "</td>";
      str += "<td>" + (earliestDate == null ? "-" : new SimpleDateFormat("MM/dd/yyyy").format(earliestDate)) + "</td>";
      str += "<td>" + (adjustedRecommendedDate == null ? "-" : new SimpleDateFormat("MM/dd/yyyy").format(adjustedRecommendedDate)) + "</td>";
      str += "<td>" + (adjustedPastDueDate == null ? "-" : new SimpleDateFormat("MM/dd/yyyy").format(adjustedPastDueDate)) + "</td>";
      str += "<td>" + (latestDate == null ? "-" : new SimpleDateFormat("MM/dd/yyyy").format(latestDate)) + "</td>";
      str += "<td>" + reason + "</td>";
      str += status         == null || status.isEmpty()         ? "" : "<td>" + status + "</td>";
      str += antigensNeeded == null || antigensNeeded.isEmpty() ? "" : "<td>" + antigensNeeded + "</td>";
      str += vaccineTypes   == null || vaccineTypes.isEmpty()   ? "" : "<td>" + writeRecommendedVaccineTypes() + "</td>";
/*
      str += "<td>" + (unadjustedRecommendedDate == null ? "-" : new SimpleDateFormat("MM/dd/yyyy").format(unadjustedRecommendedDate)) + "</td>";
      str += "<td>" + (unadjustedPastDueDate == null ? "-" : new SimpleDateFormat("MM/dd/yyyy").format(unadjustedPastDueDate)) + "</td>";
      str += "<td>" + vaccineGroupId + "</td>";
*/
      str += "</tr></table>";

      return str;
    }

  }
}
