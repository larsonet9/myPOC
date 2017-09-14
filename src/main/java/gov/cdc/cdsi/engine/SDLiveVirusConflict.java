/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.cdc.cdsi.engine;

/**
 *
 * @author eric
 */
public class SDLiveVirusConflict {
  private int    previousVaccineId;
  private int    currentVaccineId;
  private String beginInterval;
  private String minimumEndInterval;
  private String endInterval;

  public String getBeginInterval() {
    return beginInterval;
  }

  public void setBeginInterval(String beginInterval) {
    this.beginInterval = beginInterval;
  }

  public int getCurrentVaccineId() {
    return currentVaccineId;
  }

  public void setCurrentVaccineId(int currentVaccineId) {
    this.currentVaccineId = currentVaccineId;
  }

  public String getEndInterval() {
    return endInterval;
  }

  public void setEndInterval(String endInterval) {
    this.endInterval = endInterval;
  }

  public String getMinimumEndInterval() {
    return minimumEndInterval;
  }

  public void setMinimumEndInterval(String minimumEndInterval) {
    this.minimumEndInterval = minimumEndInterval;
  }

  public int getPreviousVaccineId() {
    return previousVaccineId;
  }

  public void setPreviousVaccineId(int previousVaccineId) {
    this.previousVaccineId = previousVaccineId;
  }

  
}
