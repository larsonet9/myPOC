/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.cdc.cdsi.engine;

/**
 *
 * @author eric
 */
public class SDAge {
  private String absoluteMinimumAge     = "";
  private String minimumAge             = "";
  private String earliestRecommendedAge = "";
  private String latestRecommendedAge   = "";
  private String maximumAge             = "";

  public String getAbsoluteMinimumAge() {
    return absoluteMinimumAge;
  }

  public void setAbsoluteMinimumAge(String absoluteMinimumAge) {
    this.absoluteMinimumAge = absoluteMinimumAge;
  }

  public String getEarliestRecommendedAge() {
    return earliestRecommendedAge;
  }

  public void setEarliestRecommendedAge(String earliestRecommendedAge) {
    this.earliestRecommendedAge = earliestRecommendedAge;
  }

  public String getLatestRecommendedAge() {
    return latestRecommendedAge;
  }

  public void setLatestRecommendedAge(String latestRecommendedAge) {
    this.latestRecommendedAge = latestRecommendedAge;
  }

  public String getMaximumAge() {
    return maximumAge;
  }

  public void setMaximumAge(String maximumAge) {
    this.maximumAge = maximumAge;
  }

  public String getMinimumAge() {
    return minimumAge;
  }

  public void setMinimumAge(String minimumAge) {
    this.minimumAge = minimumAge;
  }



}
