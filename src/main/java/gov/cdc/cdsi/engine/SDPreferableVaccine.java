/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.cdc.cdsi.engine;

/**
 *
 * @author eric
 */
public class SDPreferableVaccine {
  private int     vaccineId;
  private String  beginAge;
  private String  endAge;
  private int     manufacturerId;
  private double  volume;
  private boolean forecastVaccineType;
  
  public int getManufacturerId() {
    return manufacturerId;
  }

  public void setManufacturerId(int manufacturerId) {
    this.manufacturerId = manufacturerId;
  }

  public double getVolume() {
    return volume;
  }

  public void setVolume(double volume) {
    this.volume = volume;
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

  public int getVaccineId() {
    return vaccineId;
  }

  public void setVaccineId(int vaccineId) {
    this.vaccineId = vaccineId;
  }

  public boolean isForecastVaccineType() {
    return forecastVaccineType;
  }

  public void setForecastVaccineType(boolean forecastVaccineType) {
    this.forecastVaccineType = forecastVaccineType;
  }
  
}
