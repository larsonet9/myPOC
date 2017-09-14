/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.cdc.cdsi.engine;

/**
 *
 * @author eric
 */
public class SDSkipTargetDose {
  private String triggerAge        = "";
  private String triggerInterval   = "";
  private int    triggerTargetDose = 0;
  private int    triggerDosesAdministered = 999;

  public String getTriggerAge() {
    return triggerAge;
  }

  public void setTriggerAge(String triggerAge) {
    this.triggerAge = triggerAge;
  }

  public String getTriggerInterval() {
    return triggerInterval;
  }

  public void setTriggerInterval(String triggerInterval) {
    this.triggerInterval = triggerInterval;
  }

  public int getTriggerTargetDose() {
    return triggerTargetDose;
  }

  public void setTriggerTargetDose(int triggerTargetDose) {
    this.triggerTargetDose = triggerTargetDose;
  }

  public int getTriggerDosesAdministered() {
    return triggerDosesAdministered;
  }

  public void setTriggerDosesAdministered(int triggerDosesAdministered) {
    this.triggerDosesAdministered = triggerDosesAdministered;
  }

  
}
