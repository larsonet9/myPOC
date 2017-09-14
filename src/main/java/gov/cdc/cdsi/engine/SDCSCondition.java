/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.cdc.cdsi.engine;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Eric
 */
class SDCSCondition {
  private String conditionType  = "";  
  private Date   startDate      = new Date();
  private Date   endDate        = new Date();
  private String beginAge       = "";
  private String endAge         = "";
  private String interval       = "";
  private int    doseCount      = 0;
  private String doseType       = "";
  private String doseCountLogic = "";
  Set<String>    vacIdList      = new HashSet();
  private boolean conditionMet  = false;
  
  public String getConditionType() {
    return conditionType;
  }

  public void setConditionType(String conditionType) {
    this.conditionType = conditionType;
  }

  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  public String getBeginAge() {
    return beginAge;
  }

  public void setBeginAge(String beginAge) {
    this.beginAge = beginAge;
  }

  public String getEndAge() {
    return endAge;
  }

  public void setEndAge(String endAge) {
    this.endAge = endAge;
  }

  public String getInterval() {
    return interval;
  }

  public void setInterval(String interval) {
    this.interval = interval;
  }

  public int getDoseCount() {
    return doseCount;
  }

  public void setDoseCount(int doseCount) {
    this.doseCount = doseCount;
  }

  public String getDoseType() {
    return doseType;
  }

  public void setDoseType(String doseType) {
    this.doseType = doseType;
  }

  public String getDoseCountLogic() {
    return doseCountLogic;
  }

  public void setDoseCountLogic(String doseCountLogic) {
    this.doseCountLogic = doseCountLogic;
  }

  public void addVacId(String vacId) {
    vacIdList.add(vacId);
  }

  public Set<String> getVacIdList() {
    return vacIdList;
  }
  
  public boolean containsVaccineId(int vacId) {
    String str = "" + vacId;
    return vacIdList.contains(str);
  }

  public boolean isConditionMet() {
    return conditionMet;
  }

  public void setConditionMet(boolean conditionMet) {
    this.conditionMet = conditionMet;
  }
  
}
