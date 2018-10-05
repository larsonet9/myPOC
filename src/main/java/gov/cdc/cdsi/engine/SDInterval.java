/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.cdc.cdsi.engine;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author eric
 */
public class SDInterval {
  private boolean     fromPreviousDose            = true;
  private int         fromTargetDoseNubmer        = 0;
  private Set<String> vacIdList                   = new HashSet();
  private String      absoluteMinimumInterval     = "";
  private String      minimumInterval             = "";
  private String      earliestRecommendedInterval = "";
  private String      latestRecommendedInterval   = "";
  private String      priorityFlag                = "";
  private Date        effectiveDate               = new Date();
  private Date        cessationDate               = new Date();

  public String getAbsoluteMinimumInterval() {
    return absoluteMinimumInterval;
  }

  public void setAbsoluteMinimumInterval(String absoluteMinimumInterval) {
    this.absoluteMinimumInterval = absoluteMinimumInterval;
  }

  public String getEarliestRecommendedInterval() {
    return earliestRecommendedInterval;
  }

  public void setEarliestRecommendedInterval(String earliestRecommendedInterval) {
    this.earliestRecommendedInterval = earliestRecommendedInterval;
  }
  
  public boolean isFromPreviousDose() {
    return fromPreviousDose;
  }

  public void setFromPreviousDose(boolean fromPreviousDose) {
    this.fromPreviousDose = fromPreviousDose;
  }

  public boolean isFromTargetDose() {
    return fromTargetDoseNubmer > 0;
  }

  public int getFromTargetDoseNubmer() {
    return fromTargetDoseNubmer;
  }

  public void setFromTargetDoseNubmer(int fromTargetDoseNubmer) {
    this.fromTargetDoseNubmer = fromTargetDoseNubmer;
  }

  public void addVacId(String vacId) {
    vacIdList.add(vacId);
  }

  public boolean containsVaccineId(int vacId) {
    String str = "" + vacId;
    return vacIdList.contains(str);
  }

    public String getLatestRecommendedInterval() {
    return latestRecommendedInterval;
  }

  public void setLatestRecommendedInterval(String latestRecommendedInterval) {
    this.latestRecommendedInterval = latestRecommendedInterval;
  }

  public String getMinimumInterval() {
    return minimumInterval;
  }

  public void setMinimumInterval(String minimumInterval) {
    this.minimumInterval = minimumInterval;
  }

  public String getPriorityFlag() {
    return priorityFlag;
  }

  public void setPriorityFlag(String priorityFlag) {
    this.priorityFlag = priorityFlag;
  }

  boolean isFromMostRecent() {
    return vacIdList.size() > 0;
  }

  public Date getEffectiveDate() {
    return effectiveDate;
  }

  public void setEffectiveDate(Date effectiveDate) {
    this.effectiveDate = effectiveDate;
  }

  public Date getCessationDate() {
    return cessationDate;
  }

  public void setCessationDate(Date cessationDate) {
    this.cessationDate = cessationDate;
  }
  
  
}
