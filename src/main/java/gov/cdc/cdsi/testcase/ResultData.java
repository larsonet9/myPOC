/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.cdc.cdsi.testcase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author eric
 */
public class ResultData {

  private String           testId;
  private String           testCaseName;
  private List<String>     actualEvaluation   = new ArrayList();
  private List<String>     expectedEvaluation = new ArrayList();
  private TestCaseForecast expectedForecast;
  private TestCaseForecast actualForecast;
  private String           wasScored;
  private String           isDefault;
  private String           isProduct;
  private int              seriesPreference;

  public String getWasScored() {
    return wasScored;
  }

  public void setWasScored(String wasScored) {
    this.wasScored = wasScored;
  }

  public String getIsDefault() {
    return isDefault;
  }

  public void setIsDefault(String isDefault) {
    this.isDefault = isDefault;
  }

  public String getIsProduct() {
    return isProduct;
  }

  public void setIsProduct(String isProduct) {
    this.isProduct = isProduct;
  }

  public int getSeriesPreference() {
    return seriesPreference;
  }

  public void setSeriesPreference(int seriesPreference) {
    this.seriesPreference = seriesPreference;
  }

  
  public List<String> getActualEvaluation() {
    return actualEvaluation;
  }

  public void addActualEvaluation(String status) {
    this.actualEvaluation.add(status);
  }

  public List<String> getExpectedEvaluation() {
    return expectedEvaluation;
  }

  public void addExpectedEvaluation(String status) {
    this.expectedEvaluation.add(status);
  }



  public TestCaseForecast getActualForecast() {
    return actualForecast;
  }

  public void setActualForecast(String forecastVDANumber, Date earliestDate, Date recommendedDate, Date pastDueDate, String seriesStatus, String forecastReason)
  {
    this.actualForecast = new TestCaseForecast(forecastVDANumber, earliestDate, recommendedDate, pastDueDate, seriesStatus, forecastReason);
  }

  public TestCaseForecast getExpectedForecast() {
    return expectedForecast;
  }

  public void setExpectedForecast(String forecastVDANumber, Date earliestDate, Date recommendedDate, Date pastDueDate, String seriesStatus, String forecastReason)
  {
    this.expectedForecast = new TestCaseForecast(forecastVDANumber, earliestDate, recommendedDate, pastDueDate, seriesStatus, forecastReason);
  }

  public String getTestId() {
    return testId;
  }

  public void setTestId(String testId) {
    this.testId = testId;
  }

  public String getTestCaseName() {
    return testCaseName;
  }

  public void setTestCaseName(String testCaseName) {
    this.testCaseName = testCaseName;
  }

  public String toString() {
    String str = "<b>(" + testId + ") - " + testCaseName + "</b><br>";

    str += "<table><tr><th></th><th>Series Status</th><th>Forecast#</th><th>Earliest</th><th>Recommended</th><th>Past Due</th><th>Forecast Notes</th><th>Evaluation Matches</th><th>Best Series Info</th></tr>";

    String bestSeriesStr = "<b>Was Scored:</b>" + wasScored + "<br>" +
                           "<b>Is Default:</b>" + isDefault + "<br>" +
                           "<b>Is Product:</b>" + isProduct + "<br>" +
                           "<b>Series Prefernce:</b>" + seriesPreference;
    
    if (evaluationAndForecastPassed()) {
      str += "<tr><td>CDSi Test Case Expected</td>" + expectedForecast.toString() + "<td>"+ evaluationPassed() + "</td><td></td></tr>";
      str += "<tr><td>CDSi Logic Spec Actual</td>"  + actualForecast.toString()   + "<td>"+ evaluationPassed() + "</td><td>"+bestSeriesStr+"</td></tr>";
    }
    else if (forecastPassed()) {
      str += "<tr><td>CDSi Test Case Expected</td>" + expectedForecast.toString() + "<td class=\"evalMismatch\">"+ evaluationPassed() + "</td><td></td></tr>";
      str += "<tr><td>CDSi Logic Spec Actual</td>"  + actualForecast.toString()   + "<td class=\"evalMismatch\">"+ evaluationPassed() + "</td><td>"+bestSeriesStr+"</td></tr>";
    }
    else if (evaluationPassed()) {
      expectedForecast.setFailed();
      actualForecast.setFailed();
      str += "<tr><td>CDSi Test Case Expected</td>" + expectedForecast.toString() + "<td>"+ evaluationPassed() + "</td><td></td></tr>";
      str += "<tr><td>CDSi Logic Spec Actual</td>"  + actualForecast.toString()   + "<td>"+ evaluationPassed() + "</td><td>"+bestSeriesStr+"</td></tr>";
    }
    else {
      str += "<tr class=\"failed\"><td>CDSi Test Case Expected</td>" + expectedForecast.toString() + "<td>"+ evaluationPassed() + "</td><td></td></tr>";
      str += "<tr class=\"failed\"><td>CDSi Logic Spec Actual</td>"  + actualForecast.toString()   + "<td>"+ evaluationPassed() + "</td><td>"+bestSeriesStr+"</td></tr>";
    }

    str += "</table>";

    return str;
  }

  public boolean evaluationAndForecastPassed()
  {
    return (evaluationPassed() && forecastPassed());
  }

  public boolean evaluationPassed()
  {
     if(actualEvaluation.size() != expectedEvaluation.size()) return false;
     for(int i = 0; i < actualEvaluation.size(); i++) {
       if(!objectsMatch(actualEvaluation.get(i)   == null ? null : actualEvaluation.get(i).toUpperCase(),
                        expectedEvaluation.get(i) == null ? null : expectedEvaluation.get(i).toUpperCase())) return false;
     }
     return true;
  }

  public boolean forecastPassed()
  {
    return (actualForecast.getSeriesStatus().equalsIgnoreCase(expectedForecast.getSeriesStatus()) &&
//            actualForecast.getForecastVDANumber().equalsIgnoreCase(expectedForecast.getForecastVDANumber()) &&
            objectsMatch(actualForecast.getEarliestDate(), expectedForecast.getEarliestDate()) &&
            objectsMatch(actualForecast.getRecommendedDate(), expectedForecast.getRecommendedDate()) &&
            objectsMatch(actualForecast.getPastDueDate(), expectedForecast.getPastDueDate()));

  }

  private boolean objectsMatch(Object a, Object b)
  {
    if(a == null && b == null) return true;
    if(a == null && b != null) return false;

    return a.equals(b);
  }


  protected static class TestCaseForecast {
    private String seriesStatus;
    private String forecastReason;
    private String forecastVDANumber;
    private Date   earliestDate;
    private Date   recommendedDate;
    private Date   pastDueDate;
    private boolean passed = true;

    public boolean isPassed() {
      return passed;
    }

    public void setPassed() {
      this.passed = true;
    }

    public void setFailed() {
      this.passed = false;
    }



    public Date getEarliestDate() {
      return earliestDate;
    }

    public String getForecastReason() {
      return forecastReason;
    }

    public String getForecastVDANumber() {
      return forecastVDANumber;
    }

    public Date getPastDueDate() {
      return pastDueDate;
    }

    public Date getRecommendedDate() {
      return recommendedDate;
    }

    public String getSeriesStatus() {
      return seriesStatus;
    }


    public TestCaseForecast(String forecastVDANumber, Date earliestDate, Date recommendedDate, Date pastDueDate, String seriesStatus, String forecastReason) {
      this.forecastVDANumber = forecastVDANumber;
      this.earliestDate      = earliestDate;
      this.recommendedDate   = recommendedDate;
      this.pastDueDate       = pastDueDate;
      this.seriesStatus      = seriesStatus;
      this.forecastReason    = forecastReason;
    }


    public String toString()
    {
      String earliest    = earliestDate    == null ? "-" : new SimpleDateFormat("MM/dd/yyyy").format(earliestDate);
      String recommended = recommendedDate == null ? "-" : new SimpleDateFormat("MM/dd/yyyy").format(recommendedDate);
      String pastDue     = pastDueDate     == null ? "-" : new SimpleDateFormat("MM/dd/yyyy").format(pastDueDate);

      if(passed)
        return "<td>" + seriesStatus      + "</td>" +
               "<td>" + forecastVDANumber + "</td>" +
               "<td>" + earliest          + "</td>" +
               "<td>" + recommended       + "</td>" +
               "<td>" + pastDue           + "</td>" +
               "<td>" + forecastReason    + "</td>";
      else
        return "<td class=\"forecastFailed\">" + seriesStatus      + "</td>" +
               "<td class=\"forecastFailed\">" + forecastVDANumber + "</td>" +
               "<td class=\"forecastFailed\">" + earliest          + "</td>" +
               "<td class=\"forecastFailed\">" + recommended       + "</td>" +
               "<td class=\"forecastFailed\">" + pastDue           + "</td>" +
               "<td class=\"forecastFailed\">" + forecastReason    + "</td>";

    }

  }

}
