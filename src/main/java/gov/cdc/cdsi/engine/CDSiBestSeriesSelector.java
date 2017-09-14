/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.cdc.cdsi.engine;

import gov.cdc.cdsi.engine.CDSiPatientSeries.TargetDose;
import gov.cdc.util.Pair;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author eric
 */
class CDSiBestSeriesSelector {

  private static final int APPLY_COMPLETE       = 0;
  private static final int APPLY_IN_PROCESS     = 1;
  private static final int APPLY_NO_VALID_DOSES = 2;

  public static void selectBestPatientSeries(List<CDSiPatientSeries> psList) throws Exception {
    // Gather some stats for use in DT.
    int cntComplete   = 0;
    int cntInProcess  = 0;
    int cntNotStarted = 0;

    // 8.2 Pre-filter Patient Series
    List<CDSiPatientSeries> scorableSeries = preFilterPatientSeries(psList); 
            
    // 8.3 Is there one best patient series?
    for(CDSiPatientSeries ps : scorableSeries)
    {
      if(ps.isStatusComplete())
        cntComplete++;
      else if(ps.isInProcess())
        cntInProcess++;
      else
        cntNotStarted++;
    }
    if(oneBestSeries(psList, scorableSeries, cntComplete, cntInProcess, cntNotStarted))
      return;

    // 8.4 Which Patient Series should be scored?
    int scoringToApply = classifyPatientSeries(cntComplete, cntInProcess);

    // Pick one of 8.5, 8.6, or 8.7 based on the results of 8.4

    // 8.5 Complete Patient Series Scoring
    if(scoringToApply == APPLY_COMPLETE) {
      for(CDSiPatientSeries ps : scorableSeries) {
        if(!ps.isStatusComplete())
          ps.setCandidateSeries(false);
      }
      scoreAllCompleteSeries(scorableSeries);
    }

    // 8.6 Score all In-Process Series
    else if (scoringToApply == APPLY_IN_PROCESS) {
      for(CDSiPatientSeries ps : scorableSeries) {
        if(!ps.isInProcess())
          ps.setCandidateSeries(false);
      }
      scoreAllInProcessSeries(scorableSeries);
    }

    // 8.7 Score all Patient Series
    else {
      scoreAllPatientSeries(scorableSeries);
    }

    // 8.8 Select Prioritized Patient Series
    selectBestScoredPatientSeries(scorableSeries);
  }

  // 6.2 Is there one best patient series?
  private static boolean oneBestSeries(List<CDSiPatientSeries> psList, List<CDSiPatientSeries> scorableList, int cntComplete, int cntInProcess, int cntNotStarted) throws Exception {

    // Column 1 on DT: No Scorable Series - Pick Default
    if(scorableList.isEmpty()) {
      for(CDSiPatientSeries ps : psList) {
        if (ps.isDefaultSeries()) {
          ps.setBestSeries(true);
          return true;
        }
      }
    }
    
    // Column 2 on DT: Only 1 candidate patient Series remains
    if(cntComplete + cntInProcess + cntNotStarted == 1) {
      for(CDSiPatientSeries ps : scorableList) {
        if(ps.isCandidateSeries()) {
          ps.setBestSeries(true);
          return true;
        }
      }
    }

    // Column 3 on DT: More than 1 total patient series, but only 1 is complete
    if(cntComplete == 1) {
      for(CDSiPatientSeries ps : scorableList) {
        if(ps.isCandidateSeries() && ps.isStatusComplete()) {
          ps.setBestSeries(true);
          return true;
        }
      }
      throw new Exception("Failed to locate the one complete patient series.");
    }

    // Column 4 on DT: More than 1 total patient series, but only 1 is in-process and 0 complete
    if(cntInProcess == 1 && cntComplete == 0) {
      for(CDSiPatientSeries ps : scorableList) {
        if(ps.isCandidateSeries() && ps.isInProcess()) {
          ps.setBestSeries(true);
          return true;
        }
      }
      throw new Exception("Failed to locate the one in-process patient series.");
    }

    // Column 5 on DT: Nothing has started, but one of these is the default
    if(cntInProcess + cntComplete == 0)
    for(CDSiPatientSeries ps : scorableList) {
      if(ps.isCandidateSeries() && ps.isDefaultSeries()) {
        ps.setBestSeries(true);
        return true;
      }
    }

    // Column 5 on DT: Nothing identifed as a single superior series.  Need to score
    return false;
  }

  // 6.3 Which Patient Series should be scored?
  private static int classifyPatientSeries(int cntComplete, int cntInProcess) {
    // Column 1 on DT: 2 or more complete
    if(cntComplete >= 2)
      return APPLY_COMPLETE;

    // Column 2 on DT: 2 or more in-process and no complete
    if(cntInProcess >= 2)
      return APPLY_IN_PROCESS;

    // Column 3 on DT: Fall through.  Score 'em all.
    return APPLY_NO_VALID_DOSES;
  }

  // 8.5 Score All Complete Series
  private static void scoreAllCompleteSeries(List<CDSiPatientSeries> psList)
  {
    for(int i = 0; i < psList.size(); i++) {
      int score = 0;
      int tmpScore = 0;
      CDSiPatientSeries scorePS = psList.get(i);

      // Only score Completed Series
      if(scorePS.isCandidateSeries()) {
        // Most Valid Doses
        tmpScore = hasMostValidDoses(scorePS, psList, i, 1, 0, -1);
        score += tmpScore;
        scorePS.addDetailedScore(new Pair("Most Valid Doses", new Integer(tmpScore)));

        // Is a Product Patient Series and has all valid doses
        tmpScore = isProductSeriesWithAllValidDoses(scorePS, 1, -1);
        score += tmpScore;
        scorePS.addDetailedScore(new Pair("Product And All Valid", new Integer(tmpScore)));

        // Earliest Completing
        tmpScore = isEarliestCompletingSeries(scorePS, psList, i, 2, 1, -1);
        score += tmpScore;
        scorePS.addDetailedScore(new Pair("Earliest Completing", new Integer(tmpScore)));

        // Set the Series score
        scorePS.setSeriesScore(score);
        scorePS.addDetailedScore(new Pair("SERIES COMPLETE SCORE", new Integer(score)));
      }
    } // end "i" loop
  }

  // 8.6 Score all In-Process Series
  private static void scoreAllInProcessSeries(List<CDSiPatientSeries> psList) throws Exception {
    for(int i = 0; i < psList.size(); i++) {
      int score = 0;
      int tmpScore = 0;
      CDSiPatientSeries scorePS = psList.get(i);

      // Only score InProcess Series
      if(scorePS.isCandidateSeries()) {
        // Is a Product Patient Series and has all valid doses
        tmpScore = isProductSeriesWithAllValidDoses(scorePS, 2, -2);
        score += tmpScore;
        scorePS.addDetailedScore(new Pair("Product And All Valid", new Integer(tmpScore)));

        // Is Completable
        tmpScore = isCompletable(scorePS, 3, -3);
        score += tmpScore;
        scorePS.addDetailedScore(new Pair("Is Completable", new Integer(tmpScore)));

        // Has the most valid doses
        tmpScore = hasMostValidDoses(scorePS, psList, i, 2, 0, -2);
        score += tmpScore;
        scorePS.addDetailedScore(new Pair("Most Valid Doses", new Integer(tmpScore)));

        // Is Closest to Completion
        tmpScore = isClosestToCompletion(scorePS, psList, i, 2, 0, -2);
        score += tmpScore;
        scorePS.addDetailedScore(new Pair("Closest To Complete", new Integer(tmpScore)));

        // Can Finish earliest
        tmpScore = canFinishEarliest(scorePS, psList, i, 1, 0, -1);
        score += tmpScore;
        scorePS.addDetailedScore(new Pair("Finish Earliest", new Integer(tmpScore)));

//        // Was a Late Start
//        tmpScore = isLateStart(scorePS, -10, 0);
//        score += tmpScore;
//        scorePS.addDetailedScore(new Pair("Exceeded Max Start Age", new Integer(tmpScore)));


        // Set the Series score
        scorePS.setSeriesScore(score);
        scorePS.addDetailedScore(new Pair("IN-PROCESS SCORE", new Integer(score)));
      }
    } // end "i" loop
  }

  // 6.6 Score all Series
  private static void scoreAllPatientSeries(List<CDSiPatientSeries> psList) throws Exception {
    for(int i = 0; i < psList.size(); i++) {
      int score = 0;
      int tmpScore = 0;
      CDSiPatientSeries scorePS = psList.get(i);

      if(scorePS.isCandidateSeries()) {
        // Can Start Earliest
        tmpScore = canStartEarliest(scorePS, psList, i, 1, 0, -1);
        score += tmpScore;
        scorePS.addDetailedScore(new Pair("Start Earliest", new Integer(tmpScore)));

        // Is Completable
        tmpScore = isCompletable(scorePS, 1, -1);
        score += tmpScore;
        scorePS.addDetailedScore(new Pair("Is Completable", new Integer(tmpScore)));

        // Is Gender Series and Gender Matches
        tmpScore = isGenderSeriesAndGenderMatches(scorePS, 1, 0);
        score += tmpScore;
        scorePS.addDetailedScore(new Pair("Gender Match", new Integer(tmpScore)));

        // Is a Product Patient Series
        tmpScore = isProductSeries(scorePS, -1, 1);
        score += tmpScore;
        scorePS.addDetailedScore(new Pair("Product Series", new Integer(tmpScore)));

//        // Has exceeded max age
//        tmpScore = hasExceededMaximumAge(scorePS, -1, 1);
//        score += tmpScore;
//        scorePS.addDetailedScore(new Pair("Exceeded Max Age", new Integer(tmpScore)));

        // Set the Series score
        scorePS.setSeriesScore(score);
        scorePS.addDetailedScore(new Pair("NOT STARTED SCORE", new Integer(score)));
      }
    } // end "i" loop
  }

  // 6.7 Select the Best Scored Patient Series
  private static void selectBestScoredPatientSeries(List<CDSiPatientSeries> psList) {
    int maxScore = -99999;
    int index    = -1;

    for(int i = 0; i<psList.size(); i++) {
      if(psList.get(i).isScored()) {
        if(psList.get(i).getSeriesScore() > maxScore) {
          maxScore = psList.get(i).getSeriesScore();
          index = i;
        }
        else if(psList.get(i).getSeriesScore() == maxScore)  { // Tie, use preference
          if(psList.get(i).getSeriesPreference() < psList.get(index).getSeriesPreference()) {
            index = i;
          }
        }
      } // end if scored
    } // end for loop
    psList.get(index).setBestSeries(true);
  }

  
  //--------- SCORING METHODS --------------------|
  //                                              |
  //--------- SCORING METHODS --------------------|

  // Definition of "Most Valid Doses" in section 6.1
  // A patient series has the most valid doses if the number of valid doses is
  // greater than the number of valid doses in all other candidate patient series.
  private static int hasMostValidDoses(CDSiPatientSeries scorePS, List<CDSiPatientSeries> psList, int i, int bestScore, int tieScore, int falseScore) {
    int tmpScore = bestScore;  // Assume best and downgrade if not the best
    for(int j = 0; j < psList.size(); j++) {
      // Don't compare to self or a non-candidate series
      if(i != j && psList.get(j).isCandidateSeries()) {
        CDSiPatientSeries comparePS = psList.get(j);

        // Tie.  Set the score, but don't quit.  We could find a better series, which will disqualify us.
        if(comparePS.getPatientData().getCountOfValidDoses() == scorePS.getPatientData().getCountOfValidDoses()) {
          tmpScore = tieScore;
        }

        // We can quit this comparison.  A different series is better.
        if(comparePS.getPatientData().getCountOfValidDoses() > scorePS.getPatientData().getCountOfValidDoses()) {
          tmpScore = falseScore;
          break;
        }
      }
    }  // end "j" loop
    return tmpScore;
  }

  // Definition of "Earliest Completing" in Section 6.1
  // A complete patient series must be considered to be the earliest completing
  // if the actual finish date is before the Actual Finish Date for all other candidate patient series.
  // ~~~~~~~~~~~~~~~~
  // Definition of "Actual Finish Date"
  // The actual finish date of a complete patient series must be the latest date
  // administered of a vaccine dose administered with an evaluation status “valid.”
  private static int isEarliestCompletingSeries(CDSiPatientSeries scorePS, List<CDSiPatientSeries> psList, int i, int bestScore, int tieScore, int falseScore) {
    int tmpScore = bestScore;  // Assume best and downgrade if not the best
    for(int j = 0; j < psList.size(); j++) {
      // Don't compare to self or a non-candidate series
      if(i != j && psList.get(j).isCandidateSeries()) {
        CDSiPatientSeries comparePS = psList.get(j);
        Date compareFinishDate = comparePS.getActualFinishDate();
        Date scoreFinishDate   = scorePS.getActualFinishDate();

        // Tie.  Set the score, but don't quit.  We could find a better series, which will disqualify us.
        if(compareFinishDate.equals(scoreFinishDate)) {
          tmpScore = tieScore;
        }

        // We can quit this comparison.  A different series is better.
        if(compareFinishDate.before(scoreFinishDate)) {
          tmpScore = falseScore;
          break;
        }
      }
    }  // end "j" loop
    return tmpScore;
  }

  // Definition of "Product Patient Series" from section 6.1
  // A product patient series must have the supporting data “patient path”
  // attribute specified as “Yes.”
  // ~~~~~~~~~~~~~~~
  // Definition of "All Valid Doses" from section 6.1
  // A patient series has all valid doses if all doses administered have an
  // evaluation status “valid.”
  private static int isProductSeries(CDSiPatientSeries scorePS, int trueScore, int falseScore) {
    return (scorePS.isProductSeries() ? trueScore : falseScore);
  }

  private static int isProductSeriesWithAllValidDoses(CDSiPatientSeries scorePS, int trueScore, int falseScore) {
    return (scorePS.isProductSeries() && scorePS.getPatientData().hasAllValidDoses() ? trueScore : falseScore);
  }

  // Definition of "Completable" from section 6.1
  // A patient series must be considered completable if the forecast finish date
  // is less than the maximum age date of the last target dose.
  private static int isCompletable(CDSiPatientSeries scorePS, int trueScore, int falseScore) throws Exception {

    // If there is no Maximum Age on the last target dose, then we pass the test as being completable.
    List<TargetDose> tdList = scorePS.getTargetDoses();
    SDAge sdAge = SupportingData.getAgeData(tdList.get(tdList.size() - 1).getDoseId());
    if(sdAge == null || sdAge.getMaximumAge() == null || sdAge.getMaximumAge().isEmpty())
      return trueScore;

    // Apply the Completable Business Rule/Definition
    Date ffDate = new Date(scorePS.getForecastFinishDate());
    if(ffDate.before(CDSiDate.calculateDate(scorePS.getPatientData().getPatient().getDob(), sdAge.getMaximumAge())))
      return trueScore;
            
    return falseScore;
  }

  // Defintion of "Closest To Completion" from Section 6.1
  // A patient series must be the closest to completion if the number of doses
  // remaining is less than the number of doses remaining in all other candidate
  // patient series.
  private static int isClosestToCompletion(CDSiPatientSeries scorePS, List<CDSiPatientSeries> psList, int i, int bestScore, int tieScore, int falseScore) {
    int tmpScore = bestScore;  // Assume best and downgrade if not the best
    for(int j = 0; j < psList.size(); j++) {
      // Don't compare to self or a non-candidate series
      if(i != j && psList.get(j).isCandidateSeries()) {
        CDSiPatientSeries comparePS = psList.get(j);
        int compareTDRemaining = comparePS.getNumberOfTargetDosesNotSatisfied();
        int scoreTDRemaining   = scorePS.getNumberOfTargetDosesNotSatisfied();

        // Tie.  Set the score, but don't quit.  We could find a better series, which will disqualify us.
        if(compareTDRemaining == scoreTDRemaining) {
          tmpScore = tieScore;
        }

        // We can quit this comparison.  A different series is better.
        if(compareTDRemaining < scoreTDRemaining) {
          tmpScore = falseScore;
          break;
        }
      }
    }  // end "j" loop
    return tmpScore;
  }

  // Definition of "Finish Earliest" from section 6.1
  // A patient series can finish earliest if the patient series can be completed
  // and the forecast finish date is earlier than the forecast finish date in
  // all other candidate patient series.
  private static int canFinishEarliest(CDSiPatientSeries scorePS, List<CDSiPatientSeries> psList, int i, int bestScore, int tieScore, int falseScore) throws Exception {
    int tmpScore = bestScore;  // Assume best and downgrade if not the best

    // Reuse isCompletable
    if(0 == isCompletable(scorePS, 1, 0))
      return falseScore;

    for(int j = 0; j < psList.size(); j++) {
      // Don't compare to self or a non-candidate series or a non-completable Series.
      if(i != j && psList.get(j).isCandidateSeries() && (1 == isCompletable(psList.get(j), 1, 0))) {
        CDSiPatientSeries comparePS = psList.get(j);
        Date compareFFDate = new Date(comparePS.getForecastFinishDate());
        Date scoreFFDate   = new Date(scorePS.getForecastFinishDate());

        // Tie.  Set the score, but don't quit.  We could find a better series, which will disqualify us.
        if(compareFFDate.equals(scoreFFDate)) {
          tmpScore = tieScore;
        }

        // We can quit this comparison.  A different series is better.
        if(compareFFDate.before(scoreFFDate)) {
          tmpScore = falseScore;
          break;
        }
      }
    }  // end "j" loop
    return tmpScore;
  }

  // Definition of "Start Earliest" from section 6.1
  // A patient series can start earliest if the start date is before the
  // start date for all other candidate patient series.
  // ~~~~~~~~~~~~~~
  // Definition of "Start Date" from section 6.1
  // The start date for a patient series must be the forecast earliest date for
  // target dose 1.
  private static int canStartEarliest(CDSiPatientSeries scorePS, List<CDSiPatientSeries> psList, int i, int bestScore, int tieScore, int falseScore) throws Exception {
    int tmpScore = bestScore;  // Assume best and downgrade if not the best

    Date scoreStartDate = scorePS.getPatientData().getForecast().getEarliestDate();
    if(scoreStartDate == null)
      scoreStartDate = new SimpleDateFormat("MM/dd/yyyy").parse("12/31/2999");

    for(int j = 0; j < psList.size(); j++) {
      // Don't compare to self or a non-candidate series.
      if(i != j && psList.get(j).isCandidateSeries()) {
        CDSiPatientSeries comparePS = psList.get(j);
        Date compareStartDate = comparePS.getPatientData().getForecast().getEarliestDate();
        
        if(compareStartDate == null)
          compareStartDate = new SimpleDateFormat("MM/dd/yyyy").parse("12/31/2999");


        // Tie.  Set the score, but don't quit.  We could find a better series, which will disqualify us.
        if(compareStartDate.equals(scoreStartDate)) {
          tmpScore = tieScore;
        }

        // We can quit this comparison.  A different series is better.
        if(compareStartDate.before(scoreStartDate)) {
          tmpScore = falseScore;
          break;
        }
      }
    }  // end "j" loop
    return tmpScore;
  }

  // Definition of "Gender-Specific Patient Series" from  section 6.1
  // A patient series must be a gender-specific patient series if a gender
  // status for dose 1 of the supporting data is given.
  private static int isGenderSeriesAndGenderMatches(CDSiPatientSeries scorePS, int trueScore, int falseScore) throws Exception {
    SDGender sdGen = SupportingData.getGenderData(scorePS.getTargetDoses().get(0).getDoseId());

    // If there is not a Gender Series, return false score.
    if(sdGen == null || sdGen.isEmpty())
      return falseScore;

    // This is a gender Series, so now the patient Gender must match
    if(sdGen.containsGender(scorePS.getPatientData().getPatient().getGender()))
      return trueScore;

    return falseScore;
  }

  // Definition of "Exceeded Maximum Age" from section 6.1
  // A patient series must be considered to have exceeded maximum age if the
  // forecast reason is “too old”.
  private static int hasExceededMaximumAge(CDSiPatientSeries scorePS, int trueScore, int falseScore) {
    String reason = scorePS.getPatientData().getForecast().getReason();
    return(reason.equals(CDSiGlobals.FORECAST_REASON_TOO_OLD)? trueScore : falseScore);
  }

  private static int isLateStart(CDSiPatientSeries scorePS, int trueScore, int falseScore) throws Exception {
    return(scorePS.isLateStart()? trueScore : falseScore);
  }

  // This needs to be further updated with Series Priority rules
  private static List<CDSiPatientSeries> preFilterPatientSeries(List<CDSiPatientSeries> psList) throws Exception {
    List<CDSiPatientSeries> scorablePS = new ArrayList();
    for(CDSiPatientSeries ps : psList) {
      if(!ps.isLateStart()) {
        scorablePS.add(ps);
      }
    }
    return scorablePS;
  }
  
}
