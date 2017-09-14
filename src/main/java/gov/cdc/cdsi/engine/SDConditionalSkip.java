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
public class SDConditionalSkip {
  private List<SDCSSet> csSets = new ArrayList();
  private String        setLogic        = "";
  private boolean       setMet          = false;

  public List<SDCSSet> getCsSets() {
    return csSets;
  }

  public void addCsSet(SDCSSet csSet) {
    csSets.add(csSet);
  }

  public String getSetLogic() {
    return setLogic;
  }

  public void setSetLogic(String setLogic) {
    this.setLogic = setLogic;
  }

  public boolean isSetMet() {
    return setMet;
  }

  public void setSetMet(boolean setMet) {
    this.setMet = setMet;
  }
  
  
  
}
