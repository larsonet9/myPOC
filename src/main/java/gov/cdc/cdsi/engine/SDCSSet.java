/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.cdc.cdsi.engine;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Eric
 */
class SDCSSet {
  private List<SDCSCondition> csConditions   = new ArrayList();
  private String              conditionLogic = "";
  private String              description    = "";
  private boolean             setMet         = false;

  public List<SDCSCondition> getCsConditions() {
    return csConditions;
  }

  public void addCsCondition(SDCSCondition csCondition) {
    csConditions.add(csCondition);
  }

  public String getConditionLogic() {
    return conditionLogic;
  }

  public void setConditionLogic(String conditionLogic) {
    this.conditionLogic = conditionLogic;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
  
  public boolean isSetMet() {
    return setMet;
  }

  public void setSetMet(boolean setMet) {
    this.setMet = setMet;
  }
  
  
}
