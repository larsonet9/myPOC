/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.cdc.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.Years;

/**
 *
 * @author Eric
 */
public class CDSiUtil {
  
   public static String getAge(Date earlierDate, Date laterDate) {
     LocalDate eDate = new LocalDate(earlierDate);
     LocalDate lDate = new LocalDate(laterDate);
     Period p  = new Period(eDate, lDate, PeriodType.yearMonthDay());
     
     String str = "";
     str += p.getYears() == 1 ? p.getYears() + " year " : p.getYears() + " years ";
     str += p.getMonths() == 1 ? p.getMonths() + " month " : p.getMonths() + " months ";
     str += p.getDays() == 1 ? p.getDays() + " day " : p.getDays() + " days ";

     return str;
    }
}
