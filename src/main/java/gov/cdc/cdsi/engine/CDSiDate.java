/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.cdc.cdsi.engine;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

/**
 *
 * @author eric
 */
public class CDSiDate  {

  public static Date calculateDate(Date baseDate, String offset) throws Exception { return calculateDate(baseDate, offset, null); }
  public static Date calculateDate(Date baseDate, String offset, String defaultDate) throws Exception
  {
    // Set the default if no offset is provided
    if (offset == null || offset.isEmpty())
    {
      if(defaultDate == null || defaultDate.isEmpty())
        return null;

      Date date = new SimpleDateFormat("MM/dd/yyyy").parse(defaultDate);
      return(dropTime(date));
    }

    Calendar calOrig = Calendar.getInstance();
    calOrig.setTime(baseDate);
    
    Calendar cal = Calendar.getInstance();
    cal.setTime(baseDate);

    // Parse the offset string and apply the operations
    StringTokenizer tokens = new StringTokenizer(offset);

    // Take care of the first add, then see if there are more
    int    amount    = Integer.parseInt(tokens.nextToken());
    String field     = tokens.nextToken();
    int    calField = getField(field);

    if(field.equalsIgnoreCase("weeks") || field.equalsIgnoreCase("week"))
      amount = amount*7;
    
    cal.add(calField, amount);
    
    // CALCDT-5: If we fell back, but should have went forward, let's do that now
    if(mustRollForward(calOrig, cal, calField))
    {
     cal.add(Calendar.DAY_OF_MONTH, 1);
    }
    
    while(tokens.hasMoreTokens())
    {
      String operation = tokens.nextToken();
      amount           = Integer.parseInt(tokens.nextToken());
      field            = tokens.nextToken();
      calField = getField(field);

      if(field.equalsIgnoreCase("weeks") || field.equalsIgnoreCase("week"))
        amount = amount*7;

      if (operation.equals("-"))
        amount = amount*-1;

      cal.add(calField, amount);
      
      // CALCDT-5: If we fell back, but should have went forward, let's do that now
      if(mustRollForward(calOrig, cal, calField))
      {
        cal.add(Calendar.DAY_OF_MONTH, 1);
      }
    }

    // Drop the time as we don't care about that for Vaccines.
    // Only clinical days
    return dropTime(cal);

  }

  private static int getField(String field) {
    if(field.equalsIgnoreCase("years") || field.equalsIgnoreCase("year"))
      return Calendar.YEAR;
    else if(field.equalsIgnoreCase("months") || field.equalsIgnoreCase("month"))
      return Calendar.MONTH;

    return Calendar.DAY_OF_MONTH;
  }

  public static Date dropTime(Date date) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    return dropTime(cal);
  }
  public static Date dropTime(Calendar cal) {
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);

    return(cal.getTime());
  }

    private static boolean mustRollForward(Calendar calOrig, Calendar cal, int calField) {
        return ((calField == Calendar.YEAR || calField == Calendar.MONTH ) &&
        calOrig.getActualMaximum(Calendar.DAY_OF_MONTH) > cal.getActualMaximum(Calendar.DAY_OF_MONTH) &&
        cal.get(Calendar.DAY_OF_MONTH) == cal.getActualMaximum(Calendar.DAY_OF_MONTH) &&
        calOrig.get(Calendar.DAY_OF_MONTH) > cal.get(Calendar.DAY_OF_MONTH));
    }
}
