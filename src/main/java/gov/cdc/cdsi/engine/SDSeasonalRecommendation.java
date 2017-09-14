/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.cdc.cdsi.engine;

import java.util.Date;

/**
 *
 * @author eric
 */
public class SDSeasonalRecommendation {
 private Date startDate = new Date();
 private Date endDate   = new Date();

  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }
 
 

}
