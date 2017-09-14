/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.cdc.cdsi.engine;

/**
 *
 * @author eric
 */
public class SDAllowableVaccine {
  private int     vaccineId;
  private String  beginAge;
  private String  endAge;

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

  public int getVaccineId() {
    return vaccineId;
  }

  public void setVaccineId(int vaccineId) {
    this.vaccineId = vaccineId;
  }
  

}
