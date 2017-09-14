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
public class SDConditionalSet {
  private Date   startDate = new Date();
  private Date   endDate   = new Date();
  private String beginAge  = "";
  private String endAge    = "";
  private int    doseCount = 0;
  Set<String>    vacIdList = new HashSet();

  public int getDoseCount() {
    return doseCount;
  }

  public void setDoseCount(int doseCount) {
    this.doseCount = doseCount;
  }

  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
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
  
  public void addVacId(String vacId) {
    vacIdList.add(vacId);
  }

  public boolean containsVaccineId(int vacId) {
    String str = "" + vacId;
    return vacIdList.contains(str);
  }
}
