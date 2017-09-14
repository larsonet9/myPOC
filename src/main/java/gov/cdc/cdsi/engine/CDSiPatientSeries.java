/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.cdc.cdsi.engine;

import gov.cdc.cdsi.engine.CDSiPatientData.AntigenAdministered;
import gov.cdc.util.Pair;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author eric
 */
public class CDSiPatientSeries implements Serializable  {
  private int    seriesId              = 0;
  private int    antigenId             = 0;
  private int    vaccineGroupId        = 0;
  private Date   assessmentDate        = new Date();
  private String seriesName            = "";
  private String status                = CDSiGlobals.SERIES_NOT_COMPLETE;
  private String sdVersion             = "";
  private Date   uploadDate            = new Date();
  private List<TargetDose> targetDoses = new ArrayList();
  private CDSiPatientData  patientData = null;
  private boolean bestSeries           = false;
  private int     seriesScore          = -9999;
  private List<Pair<String,Integer>> detailedScore = new ArrayList<>();
  private boolean scored               = false;
  private boolean candidateSeries      = true;
  private boolean defaultSeries        = false;
  private boolean productSeries        = false;
  private int     seriesPreference     = 0;
  private String  maxAgeToStart        = "";

  public String getSdVersion() {
    return sdVersion;
  }

  public void setSdVersion(String sdVersion) {
    this.sdVersion = sdVersion;
  }

  public Date getUploadDate() {
    return uploadDate;
  }

  public void setUploadDate(Date uploadDate) {
    this.uploadDate = uploadDate;
  }

  
  
  public String getMaxAgeToStart() {
    return maxAgeToStart;
  }

  public void setMaxAgeToStart(String maxAgeToStart) {
    this.maxAgeToStart = maxAgeToStart;
  }

  public int getCountOfSatisfiedTargetDoses() {
    int ret = 0;
    for(TargetDose td : targetDoses)
      if(td.isStatusSatisfied()) ret++ ;

    return ret;
  }
  
  public boolean isLateStart() throws Exception {
    Date maxStartDate = CDSiDate.calculateDate(patientData.getPatient().getDob(), maxAgeToStart, "12/31/2999");
    List<AntigenAdministered> aaList = patientData.getAntigenAdministeredList();

    if(aaList == null || aaList.isEmpty()) return false;
    
    for(AntigenAdministered aa : aaList) {
      if(aa.isValid())
        return(aa.getDateAdministered().compareTo(maxStartDate) >= 0);
    }
    
    return false;
  }

  public boolean isCandidateSeries() {
    return candidateSeries;
  }

  public List<Pair<String, Integer>> getDetailedScore() {
    return detailedScore;
  }

  public void addDetailedScore(Pair<String, Integer> detailedScore) {
    this.detailedScore.add(detailedScore);
  }

  public void setCandidateSeries(boolean candidateSeries) {
    this.candidateSeries = candidateSeries;
  }

  public boolean isDefaultSeries() {
    return defaultSeries;
  }

  public void setDefaultSeries(boolean defaultSeries) {
    this.defaultSeries = defaultSeries;
  }

  public boolean isProductSeries() {
    return productSeries;
  }

  public void setProductSeries(boolean productSeries) {
    this.productSeries = productSeries;
  }

  public int getSeriesPreference() {
    return seriesPreference;
  }

  public void setSeriesPreference(int seriesPreference) {
    this.seriesPreference = seriesPreference;
  }

  public boolean isBestSeries() {
    return bestSeries;
  }

  public void setBestSeries(boolean bestSeries) {
    this.bestSeries = bestSeries;
  }

  public int getSeriesScore() {
    return seriesScore;
  }

  public boolean isScored() {
    return scored;
  }

  public void setSeriesScore(int seriesScore) {
    this.seriesScore = seriesScore;
    this.scored = true;
  }

  public Date getAssessmentDate() {
    return assessmentDate;
  }

  public void setAssessmentDate(Date assessmentDate) {
    this.assessmentDate = CDSiDate.dropTime(assessmentDate);
  }

  public String getSeriesName() {
    return seriesName;
  }

  public void setSeriesName(String seriesName) {
    this.seriesName = seriesName;
  }

  public CDSiPatientData getPatientData() {
    return patientData;
  }

  public void setPatientData(CDSiPatientData patientData) {
    this.patientData = patientData;

    // Using clone to get a deep copy results in everything being copied.
    // We only need to evalute antigen admin records which are part of the Antigen Series.
    int i = 1;
    for(AntigenAdministered aa : patientData.getAllVaccineDosesAdministered())  {
      if(this.antigenId == aa.getAntigenId()) {
        aa.setMustEvaluate(true);
        aa.setChronologicalPosition(i);
        i++;
      }
      else
        aa.setChronologicalPosition(9999);
    }
  }

  public int getSeriesId() {
    return seriesId;
  }

  public void setSeriesId(int seriesId) {
    this.seriesId = seriesId;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public void setStatusNotRecommended() {
    this.status = CDSiGlobals.SERIES_NOT_RECOMMENDED;
  }
  
  public void setStatusComplete() {
    this.status = CDSiGlobals.SERIES_COMPLETE;
  }

  public boolean isStatusNotComplete() {
    return status.equals(CDSiGlobals.SERIES_NOT_COMPLETE);
  }
  
  public boolean isStatusContraindicated() {
      return status.equals(CDSiGlobals.SERIES_CONTRAINDICATED);
  }

  public boolean isStatusImmune() {
      return status.equals(CDSiGlobals.SERIES_IMMUNE);
  }

  // based on definition of "Complete Patient Series" in section 6.1
  public boolean isStatusComplete() {
      return status.equals(CDSiGlobals.SERIES_COMPLETE);
  }

  // based on definition of "In-Process Patient Series" in section 6.1
  public boolean isInProcess() {
    if(isStatusComplete())
      return false;

    for(TargetDose td : targetDoses)
    {
      if(td.isStatusSatisfied())
        return true;
    }
    return false;
  }

  public Date getActualFinishDate() {
    if(!isStatusComplete())
      return null;

    return patientData.getLastValidDoseDate();
  }

  // Definition of "Forecast Finish Date" from section 6.1
  // The forecast finish date for a patient series must be the forecast
  // earliest date plus the minimum interval from the remaining target dose(s).
  public long getForecastFinishDate() throws Exception {
    if(patientData.getForecast().getEarliestDate() == null)
      return CDSiDate.calculateDate(null, null, "12/31/2999").getTime();
    
    Date ffDate        = new Date(patientData.getForecast().getEarliestDate().getTime());
    int  forecastTDnum = patientData.getForecast().getTargetDoseNumber();

    // Loop through the remaining target doses, adding the minimum interval to
    // the ffDate to determine the Forecast Finish Date
    for(TargetDose td : targetDoses) {
      if (td.getDoseNumber() > forecastTDnum) {
        List<SDInterval> intList = SupportingData.getIntervalData(td.getDoseId());
        // TODO This isn't quite accurate yet.  It only uses the adjacent intervals
        // and ignores the non-adjacent intervals.
        if(intList != null) {
          for(SDInterval sdInt : intList) {
            if(sdInt.isFromPreviousDose())
              ffDate = CDSiDate.calculateDate(ffDate, sdInt.getMinimumInterval());
          }
        }
      }
    }
    return ffDate.getTime();
  }

  public void addTargetDose(TargetDose td) {
    this.targetDoses.add(td);
  }

  public List<TargetDose> getTargetDoses() {
    return targetDoses;
  }

  public int getNumberOfTargetDosesNotSatisfied() {
    int cnt = 0;
    for(TargetDose td : targetDoses) {
      if(td.isStatusNotSatisified())
        cnt++;
    }
    return cnt;
  }

  public int getAntigenId() {
    return antigenId;
  }

  public void setAntigenId(int antigenId) {
    this.antigenId = antigenId;
  }

  public int getVaccineGroupId() {
    return vaccineGroupId;
  }

  public void setVaccineGroupId(int vaccineGroupId) {
    this.vaccineGroupId = vaccineGroupId;
  }

  public String toString()
  {

    // Patient Series
    String str = "<br><h2>PATIENT SERIES: " + seriesName + "</h2>" +
                 "Supporting Data Version " + sdVersion + " uploaded on " + uploadDate + "<br>" +
                 "Patient Series Status = " + status + "<br>";

    str += patientData.toString();
    
    str += "<br><br>";

    // Target Doses Results
    str += "<h3>Target Dose Status</h3>";
    str += "<table border=\"1\"><tr>";
    str += "<th>Dose Number</th>";
    str += "<th>Target Dose Status</th>";
    str += "<th>Skip Reason</th>";
    str += "</tr>";
    
    for(TargetDose td : targetDoses)
    {
      str += td.toString();
    }
    str += "</table>";
    
    // Select Best Series
    String strScore = "-";
    if(scored) {
      strScore = "<table border=\"1\">";
      for(Pair ds : detailedScore) {
        strScore += "<tr><td>" + ds.getL() + "</td><td>" + ds.getR() + "</td></tr>";
      }
      strScore += "</table>";
    }

    str += "<h3>Select Best Patient Series</h3>";
    str += "<table border=\"1\"><tr>";
    str += "<th>Was Candidate</th>";
    str += "<th>Was Scored</th>";
    str += "<th>Score</th>";
    str += "<th>Is Default Series</th>";
    str += "<th>Is Product Series</th>";
    str += "<th>Series Preference</th>";
    str += "<th>Is Best</th>";
    str += "</tr>";

    str += bestSeries ? "<tr class=\"best\">" : "<tr>";
    str += "<td>" + candidateSeries  + "</td>";
    str += "<td>" + scored           + "</td>";
    str += "<td>" + strScore         + "</td>";
    str += "<td>" + defaultSeries    + "</td>";
    str += "<td>" + productSeries    + "</td>";
    str += "<td>" + seriesPreference + "</td>";
    str += "<td>" + bestSeries + "</td>";
    str += "</tr></table>";
    return str;

  }
  
  protected class TargetDose {
    private int doseId;
    private int doseNumber;
    private String status = CDSiGlobals.TARGET_DOSE_NOT_SATISFIED;
    private List<String> lstSkipReason = new ArrayList();

    public int getDoseId() {
      return doseId;
    }

    public void setDoseId(int doseId) {
      this.doseId = doseId;
    }

    public int getDoseNumber() {
      return doseNumber;
    }

    public void setDoseNumber(int doseNumber) {
      this.doseNumber = doseNumber;
    }

    public String getStatus() {
      return status;
    }

    public void setStatus(String status) {
      this.status = status;
    }

    public boolean isStatusSatisfied() {
      return status.equals(CDSiGlobals.TARGET_DOSE_SATISFIED);
    }

    public boolean isStatusNotSatisified() {
      return status.equals(CDSiGlobals.TARGET_DOSE_NOT_SATISFIED);
    }

    public boolean isStatusSkipped() {
      return status.equals(CDSiGlobals.TARGET_DOSE_SKIPPED);
    }

    public boolean isStatusSubstituted() {
      return status.equals(CDSiGlobals.TARGET_DOSE_SUBSTITUTED);
    }

    public void setStatusSatisfied() {
      this.status = CDSiGlobals.TARGET_DOSE_SATISFIED;
    }

    public void setStatusNotSatisified() {
      this.status = CDSiGlobals.TARGET_DOSE_NOT_SATISFIED;
    }

    public void setStatusSkipped() {
      this.status = CDSiGlobals.TARGET_DOSE_SKIPPED;
    }

    public void setStatusUnnecessary() {
      this.status = CDSiGlobals.TARGET_DOSE_SKIPPED;
    }

    public void setStatusSubstituted() {
      this.status = CDSiGlobals.TARGET_DOSE_SUBSTITUTED;
    }

    public List<String> getSkipReason() {
      return lstSkipReason;
    }

    public void addSkipReason(String skipReason) {
      lstSkipReason.add(skipReason);
    }
    
    
    
    @Override
    public String toString() {
      String str = "<tr>";
      str += "<td>" + doseNumber + "</td>";
      str += "<td>" + status + "</td>";
      
      str += "<td>";
      for(String sReason : lstSkipReason)
        str += sReason + "<br>";
      str += "</td>";
      str += "</tr>";
      return str;
    }
  }

}
