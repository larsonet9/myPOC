/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.cdc.cdsi.engine;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author eric
 */
public class SDConditionalNeed {
  private List<SDConditionalSet> conditionalSets = new ArrayList();

  public List<SDConditionalSet> getConditionalSets() {
    return conditionalSets;
  }

  public void addConditionalSet(SDConditionalSet conditionalSet) {
    conditionalSets.add(conditionalSet);
  }


}
