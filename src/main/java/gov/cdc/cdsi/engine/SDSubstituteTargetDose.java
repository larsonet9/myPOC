/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.cdc.cdsi.engine;

/**
 *
 * @author eric
 */
public class SDSubstituteTargetDose {

  private String firstDoseBeginAge = "";
  private String firstDoseEndAge   = "";
  private int    countOfValidDoses = 0;
  private int    dosesToSubstitute = 0;

  public int getCountOfValidDoses() {
    return countOfValidDoses;
  }

  public void setCountOfValidDoses(int countOfValidDoses) {
    this.countOfValidDoses = countOfValidDoses;
  }

  public int getDosesToSubstitute() {
    return dosesToSubstitute;
  }

  public void setDosesToSubstitute(int dosesToSubstitute) {
    this.dosesToSubstitute = dosesToSubstitute;
  }

  public String getFirstDoseBeginAge() {
    return firstDoseBeginAge;
  }

  public void setFirstDoseBeginAge(String firstDoseBeginAge) {
    this.firstDoseBeginAge = firstDoseBeginAge;
  }

  public String getFirstDoseEndAge() {
    return firstDoseEndAge;
  }

  public void setFirstDoseEndAge(String firstDoseEndAge) {
    this.firstDoseEndAge = firstDoseEndAge;
  }


}
