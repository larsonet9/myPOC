/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.cdc.cdsi.engine;

/**
 *
 * @author eric
 */
public class SDContraindication {
  private String medHistoryCode    = "";
  private String medHistoryCodeSys = "";

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
}
