/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.cdc.cdsi.engine;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author eric
 */
public class SDGender {
  private Set<String> requiredGender = new HashSet();

  public void addRequiredGender(String gender) {
    requiredGender.add(gender);
  }

  public boolean containsGender(String gender) {
    return requiredGender.contains(gender);
  }

  public boolean isEmpty() {
    return requiredGender.isEmpty();
  }
}
