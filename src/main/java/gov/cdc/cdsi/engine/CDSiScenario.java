/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.cdc.cdsi.engine;

import gov.cdc.cdsi.db.DBGetter;
import gov.cdc.util.CDSiUtil;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author eric
 */
public class CDSiScenario {
  private Date   dob;
  private String gender = "";
  private int    vaccineGroupId;
  private String vaccineGroupName;
  private Date   assessmentDate;
  private List<ScenarioImmunization> simms = new ArrayList();
  private String testCaseId   = "";
  private String testCaseName = "";
  private List<ScenarioMedHistory> sMedHx = new ArrayList();
  

  public void addMedicalHistory(ScenarioMedHistory smh) 
  {
    if(smh == null) return;
    sMedHx.add(smh);
  }

  public void addMedicalHistory(String medHistoryCode, String medHistoryCodeSystem) {
    addMedicalHistory(new ScenarioMedHistory(medHistoryCode, medHistoryCodeSystem));
  }

  public void addMedicalHistory(String medHistoryCode, String medHistoryCodeSystem, String vaccGrpId) {
    addMedicalHistory(new ScenarioMedHistory(medHistoryCode, medHistoryCodeSystem, Integer.parseInt(vaccGrpId)));
  }

  public List<ScenarioMedHistory> getMedicalHistory() {
    return sMedHx;
  }

  public String getVaccineGroupName() {
    return vaccineGroupName;
  }

  public void setVaccineGroupName(String vaccineGroupName) {
    this.vaccineGroupName = vaccineGroupName;
  }

  public String getTestCaseId() {
    return testCaseId;
  }

  public void setTestCaseId(String testCaseId) {
    this.testCaseId = testCaseId;
  }

  public String getTestCaseName() {
    return testCaseName;
  }

  public void setTestCaseName(String testCaseName) {
    this.testCaseName = testCaseName;
  }

  public Date getAssessmentDate() {
    return assessmentDate;
  }

  public void setAssessmentDate(Date assessmentDate) {
    this.assessmentDate = assessmentDate;
  }

  public void setAssessmentDate(String assessmentDate) throws Exception {
    if(assessmentDate == null || assessmentDate.isEmpty())
      this.assessmentDate = new Date();
    else {
      try {
        this.assessmentDate = new SimpleDateFormat("MM/dd/yyyy").parse(assessmentDate);
      }
      catch (ParseException pe) {
        this.assessmentDate = new SimpleDateFormat("yyyyMMdd").parse(assessmentDate);
      }
    }
  }

  public Date getDob() {
    return dob;
  }

  public void setDob(String dob) throws Exception {
    try {
      this.dob = new SimpleDateFormat("MM/dd/yyyy").parse(dob);
    }
    catch (ParseException pe) {
      this.dob = new SimpleDateFormat("yyyyMMdd").parse(dob);
    }
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

  public int getVaccineGroupId() {
    return vaccineGroupId;
  }

  public void setVaccineGroupId(int vaccineGroupId) {
    this.vaccineGroupId = vaccineGroupId;
  }

  public void setVaccineGroupId(String vaccineGroupId) {
    try {
      this.vaccineGroupId = Integer.parseInt(vaccineGroupId);
    }
    catch (NumberFormatException nfe) {
      this.vaccineGroupId = 0;
    }
  }

  public void addVaccineDoseAdministered(ScenarioImmunization si) 
  {
    if(si == null) return;
    
    int i = 0;
    for(ScenarioImmunization s : simms) {
      if(s.getDateAdministered().after(si.getDateAdministered())) {
        simms.add(i, si);
        return;        
      }
      i++;
    }
    simms.add(si);
  }
  
  public void addVaccineDoseAdministered(String cvx, String mvx, String date, int id) throws Exception
  {
    ScenarioImmunization si = new ScenarioImmunization();
    si.setDoseId(id);
    si.setDateAdministered(date);
    si.setCvx(cvx);
    si.setMvx(mvx);
    si.setVaccineId(DBGetter.getVaccineId(si.getCvx()));
    si.setManufacturerId(DBGetter.getManufacturerId(si.getMvx()));
    si.setProduct(DBGetter.getProductName(si.getVaccineId(), si.getManufacturerId()));
    addVaccineDoseAdministered(si);
  }
  
  public void addVaccineDoseAdministered(String cvx, String mvx, Date date, int id) throws Exception
  {
    ScenarioImmunization si = new ScenarioImmunization();
    si.setDoseId(id);
    si.setDateAdministered(date);
    si.setCvx(cvx);
    si.setMvx(mvx);
    si.setVaccineId(DBGetter.getVaccineId(si.getCvx()));
    si.setManufacturerId(DBGetter.getManufacturerId(si.getMvx()));
    si.setProduct(DBGetter.getProductName(si.getVaccineId(), si.getManufacturerId()));
    addVaccineDoseAdministered(si);
  }

  public void addVaccineDoseAdministered(int vaccineId, int manufacturerId, Date date, int id) throws Exception
  {
    ScenarioImmunization si = new ScenarioImmunization();
    si.setDoseId(id);
    si.setDateAdministered(date);
    si.setVaccineId(vaccineId);
    si.setManufacturerId(manufacturerId);
    si.setCvx(DBGetter.getCVXFromVaccineId(si.getVaccineId()));
    si.setMvx(DBGetter.getMVXFromManufacturerId(si.getManufacturerId()));
    si.setProduct(DBGetter.getProductName(si.getVaccineId(), si.getManufacturerId()));

    addVaccineDoseAdministered(si);
  }

  public void addVaccineDoseAdministered(String vaccId_MfgId, String date, int id) throws Exception
  {
    ScenarioImmunization si = new ScenarioImmunization();

    si.setDoseId(id);
    si.setDateAdministered(date);
    si.setVaccineId(vaccId_MfgId.substring(0, vaccId_MfgId.indexOf(":")));
    si.setManufacturerId(vaccId_MfgId.substring(vaccId_MfgId.indexOf(":")+1));
    si.setCvx(DBGetter.getCVXFromVaccineId(si.getVaccineId()));
    si.setMvx(DBGetter.getMVXFromManufacturerId(si.getManufacturerId()));
    si.setProduct(DBGetter.getProductName(si.getVaccineId(), si.getManufacturerId()));

    addVaccineDoseAdministered(si);
  }

  public List<ScenarioImmunization> getVaccineDosesAdministered() {
    return simms;
  }

  public String toString()
  {
    String vaccGroup = (vaccineGroupName == null || vaccineGroupName.isEmpty()) ? "" + vaccineGroupId : vaccineGroupName;
    String str = "<table border=\"0\">";
    str += testCaseId.isEmpty() ? "" : "<tr><td>Test Case ID</td><td>" + testCaseId + "</td></tr>";
    str += testCaseName.isEmpty() ? "" : "<tr><td>Test Case Name</td><td>" + testCaseName + "</td></tr>";
    str += "<tr><td>Date of Birth</td><td>"   + new SimpleDateFormat("MM/dd/yyyy").format(dob) + "</td></tr>" +
           "<tr><td>Assessment Date</td><td>" + new SimpleDateFormat("MM/dd/yyyy").format(assessmentDate) + "</td></tr>" +
           "<tr><td>Age at Assessment</td><td>" + CDSiUtil.getAge(dob, assessmentDate) + "</td></tr>" +
           "<tr><td>Gender</td><td>"          + gender    + "</td></tr>" +
           "<tr><td>Vaccine Group</td><td>"   + vaccGroup + "</td></tr>";
    str +="</table>";

    str += "<h3>Immunization History</h3>";
      str += "<table border=\"1\">";
        str += "<tr>";
          str += "<th>DoseID</th>";
          str += "<th>Date Admin</th>";
          str += "<th>Age at Admin</th>";
          str += "<th>Dose Administered</th>";
        str += "</tr>";

    for(ScenarioImmunization si : simms)
    {
      str += si.toString();
    }
    str += "</table>";

    return str;

  }


  public class ScenarioMedHistory {
    private String medHistoryCode = "";
    private String medHistoryCodeSys = "";
    private int    vaccineGroupId;

    private ScenarioMedHistory(String medHistoryCode, String medHistoryCodeSystem) {
      this.medHistoryCode = medHistoryCode;
      this.medHistoryCodeSys = medHistoryCodeSystem;
    }

    private ScenarioMedHistory(String medHistoryCode, String medHistoryCodeSystem, int vaccineGroupId) {
      this.medHistoryCode = medHistoryCode;
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
    
    public boolean hasVaccineGroupSpecificCondition() {
      return this.vaccineGroupId > 0;
    }
    
  }

  public class ScenarioImmunization {
    private int    doseId;
    private int    vaccineId;
    private String cvx;
    private int    manufacturerId;
    private String mvx;
    private String product;
    private Date   dateAdministered;
    private String expectedEvalStatus;
    private String expectedEvalReason;

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
      this.mvx = (mvx == null || mvx.isEmpty() ? "UNK" : mvx);
    }

    public String getProduct() {
      return product;
    }

    public void setProduct(String product) {
      this.product = product;
    }

    public int getDoseId() {
      return doseId;
    }

    public void setDoseId(int doseId) {
      this.doseId = doseId;
    }

    public Date getDateAdministered() {
      return dateAdministered;
    }

    public void setDateAdministered(Date dateAdministered) {
      this.dateAdministered = dateAdministered;
    }

    public void setDateAdministered(String dateAdministered) throws Exception {
      try {
        this.dateAdministered = new SimpleDateFormat("MM/dd/yyyy").parse(dateAdministered);
      }
      catch(ParseException pe) {  // Maybe it's this format instead.  (Couple different entry points)
        this.dateAdministered = new SimpleDateFormat("yyyyMMdd").parse(dateAdministered);
      }
    }

    public int getManufacturerId() {
      return manufacturerId;
    }

    public void setManufacturerId(int manufacturerId) {
      this.manufacturerId = manufacturerId;
    }

    public void setManufacturerId(String manufacturerId) {
      this.manufacturerId = Integer.parseInt(manufacturerId);

    }

    public int getVaccineId() {
      return vaccineId;
    }

    public void setVaccineId(int vaccineId) {
      this.vaccineId = vaccineId;
    }

    public void setVaccineId(String vaccineId) {
      this.vaccineId = Integer.parseInt(vaccineId);
    }

    public String toString()
    {
      String str = "<tr>";
      str += "<td>" + doseId + "</td>";
      str += "<td>" + new SimpleDateFormat("MM/dd/yyyy").format(dateAdministered) + "</td>";
      str += "<td>" + CDSiUtil.getAge(dob, dateAdministered) + "</td>";
      if(cvx == null || cvx.isEmpty())
        str += "<td> vaccId(" + vaccineId + ")-MfgId(" + manufacturerId + ")</td>";
      else
        str += "<td>" + product + " [" + cvx + "-" + mvx + "]</td>";
      str += "</tr>";

      return str;
    }
  }

}
