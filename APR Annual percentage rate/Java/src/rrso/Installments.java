/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rrso;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.lang.Math;

/**
 *
 * @author Maciej Szymczak
 */

public class Installments {

  List installments = new ArrayList();

  private String debugText;

  private void addInstallment (Installment installment)
  {
    installments.add ( installment );
    debugText = "";
  };

  private boolean isLeapYear(int year)
  {
     boolean res;
     if ( (year % 400) == 0 || ( (year % 4) == 0 && (year % 100) != 0) )
     {
       res = true;
     }
     else
     {
       res = false;
     };
     return res;
  };

  private int daysInYear (Date d)
  {
    int res;
    Calendar cal = Calendar.getInstance();
    cal.setTime(d);
    if ( isLeapYear( cal.get(Calendar.YEAR) ) )
    {
        res = 366;
    }
    else
    {
      res = 365;
    };
    return res;
  };

  private int dayOfYear (Date d )
  {
    Calendar cal = Calendar.getInstance();
    cal.setTime( d );
    return cal.get(Calendar.DAY_OF_YEAR);
  };

  private int getYear (Date d )
  {
    Calendar cal = Calendar.getInstance();
    cal.setTime( d );
    return cal.get(Calendar.YEAR);
  };

  private Date lastDayOfYear ( Date d ) throws ParseException
  {
    Calendar cal = Calendar.getInstance();
    cal.setTime( d );
    cal.set(Calendar.MONTH, 11); //11=december
    cal.set(Calendar.DAY_OF_MONTH, 31);
    return cal.getTime();
  };

  private Date addMonths ( Date d, int months )
  {
    Calendar cal = Calendar.getInstance();
    cal.setTime( d );
    cal.add( Calendar.MONTH, months);
    return cal.getTime();
  };

  private int lastMonthDay ( Date d )
  {
    Calendar cal = Calendar.getInstance();
    cal.setTime( d );    
    return cal.getActualMaximum(Calendar.DAY_OF_MONTH);
  }

  private double calculateRrsoTime ( Date loanDate, Date installmentDueDate ) throws ParseException
  {
    double res;
    int yearsComparision;
    yearsComparision = getYear(installmentDueDate) - getYear(loanDate);
    if ( yearsComparision == 0 )
    {
      debugText =  "( " + dayOfYear(installmentDueDate) +" - "+ dayOfYear(loanDate)  +") / "+  daysInYear(installmentDueDate) + " = " + (  dayOfYear(installmentDueDate) - dayOfYear(loanDate)  ) /  daysInYear(installmentDueDate);
      res = (  (double)dayOfYear(installmentDueDate) - (double)dayOfYear(loanDate)   ) /  (double)daysInYear(installmentDueDate);
    }
    else
    {
      debugText =
             dayOfYear(installmentDueDate) +"/"+  daysInYear(installmentDueDate) +"+"+
             "(" + dayOfYear( lastDayOfYear(loanDate) ) +"-"+ dayOfYear(loanDate) +")/"+  daysInYear(installmentDueDate) +"+"+
             yearsComparision + "-1";

      res =  (double)dayOfYear(installmentDueDate) /  (double)daysInYear(installmentDueDate) +
             (   (double)dayOfYear( lastDayOfYear(loanDate) ) - (double)dayOfYear(loanDate)   ) /  (double)daysInYear(installmentDueDate) +
             (double)yearsComparision -1;
    };
    return res;
  };

  private int nonZero ( int a, int b)
  {
    return (a == 0)?b:a;
  }

  private static double round(double d, int decimalPlace){
  // see the Javadoc about why we use a String in the constructor
  // http://java.sun.com/j2se/1.5.0/docs/api/java/math/BigDecimal.html#BigDecimal(double)
  BigDecimal bd = new BigDecimal(Double.toString(d));
  bd = bd.setScale(decimalPlace,BigDecimal.ROUND_HALF_UP);
  return bd.doubleValue();
}

  private double getRealYearlyInterestRate (
          int   installmentNumber,
          int   increaseRatePeriodFrom,
          int   increaseRatePeriodTo,
          double yearlyInterestRate,
          double yearlyInterestRateIncrease
          )
  {
    if ( installmentNumber >= increaseRatePeriodFrom && installmentNumber <= increaseRatePeriodTo )
    {
      return yearlyInterestRate + yearlyInterestRateIncrease;
    }
    else
    {
     return yearlyInterestRate;
    }
  }

  public List calculate
   ( char   scheduleType     // C / D
   , int    gracePeriods
   , int    repaymentPeriods
   , double loan
   , double yearlyInterestRate         // example: 3/100 = 3% used to calculate monthly_interest_rate = yearly_interest_rate * days_in_month / days_in_year
   , int    daysInMonth                // 0 = get real days in month
   , int    daysInYear                 // 0 = get real days in year
   , double yearlyInterestRateIncrease
   , int    increaseRatePeriodFrom
   , int    increaseRatePeriodTo
   , Date   firstInstallmentDate
   , Date   loanDate
   , int    precision ) throws ParseException
  {
     int         installmentNumber = 0;
     int         lastMonthDay;
     double      monthlyInterestRate;
     double      balanceCapital;
     double      realYearlyInterestRate;


    installments.clear();
    //
    // grace
    balanceCapital = loan;
    for(int r=1; r <= gracePeriods; r++ ) {
       Installment installment = new Installment();
       installmentNumber                     = installmentNumber + 1;
       installment.installmentNo             = installmentNumber;
       installment.dueDate = addMonths( firstInstallmentDate, installmentNumber-1);
       installment.rrsoTime = calculateRrsoTime ( loanDate, installment.dueDate );
       installment.graceFlag = 'Y';
       lastMonthDay = lastMonthDay( installment.dueDate );
       realYearlyInterestRate = getRealYearlyInterestRate (installmentNumber, increaseRatePeriodFrom, increaseRatePeriodTo, yearlyInterestRate, yearlyInterestRateIncrease );
       installment.interestAmount            = round( loan * realYearlyInterestRate * nonZero( daysInMonth,  lastMonthDay ) / nonZero( daysInYear,  daysInYear(installment.dueDate) ), precision);
       installment.capitalAmount             = 0;
       installment.installmentAmount         = round( installment.interestAmount + installment.capitalAmount , precision);
       installment.capitalBefore             = balanceCapital;
       installment.capitalAfter              = balanceCapital;
       addInstallment ( installment );
    };
    //
    // repayment
    for(int r=1; r <= repaymentPeriods; r++ ) {
       Installment installment = new Installment();
       installmentNumber                       = installmentNumber + 1;
       installment.installmentNo               = installmentNumber;
       installment.dueDate =  addMonths( firstInstallmentDate, installmentNumber-1);
       installment.rrsoTime = calculateRrsoTime ( loanDate, installment.dueDate );
       installment.graceFlag = 'N';
       lastMonthDay = lastMonthDay( installment.dueDate );
       realYearlyInterestRate = getRealYearlyInterestRate (installmentNumber, increaseRatePeriodFrom, increaseRatePeriodTo, yearlyInterestRate, yearlyInterestRateIncrease );
       monthlyInterestRate                    = realYearlyInterestRate * nonZero( daysInMonth,  lastMonthDay ) / nonZero( daysInYear,  daysInYear(installment.dueDate));
       if (scheduleType == 'D')
       {
         installment.capitalBefore             = round( balanceCapital                                          , precision);
         installment.capitalAmount             = round( loan / repaymentPeriods                                 , precision);
         installment.interestAmount            = round( balanceCapital * monthlyInterestRate                    , precision);
         //debugText = debugText +"  !!!!  " + monthlyInterestRate;
         installment.installmentAmount         = round( installment.interestAmount + installment.capitalAmount  , precision);
         balanceCapital                        = round( balanceCapital - installment.capitalAmount              , precision);
         installment.capitalAfter              = round( balanceCapital                                          , precision);
       }
       else //scheduleType=C
       {
         installment.capitalBefore             = round( balanceCapital , precision);
         installment.installmentAmount         = round( loan * Math.pow( (monthlyInterestRate+1) , repaymentPeriods) * ( (monthlyInterestRate+1)-1) / ( Math.pow( (monthlyInterestRate+1) ,repaymentPeriods) - 1 ) , precision);
         installment.interestAmount            = round( balanceCapital * monthlyInterestRate , precision);
         installment.capitalAmount             = round( installment.installmentAmount - installment.interestAmount , precision);
         balanceCapital                        = round( balanceCapital - installment.capitalAmount , precision);
         installment.capitalAfter              = round( balanceCapital , precision);
       };
       // rounds
       if (r == repaymentPeriods ) {
         if (installment.capitalAfter != 0) {
           installment.capitalAmount     = round( installment.capitalAmount + installment.capitalAfter      , precision);
           installment.installmentAmount = round( installment.installmentAmount + installment.capitalAfter  , precision);
           installment.capitalAfter      = 0;
           balanceCapital                = 0;
         };
       };
       installment.comments = debugText;
       addInstallment ( installment );
    };
    return installments;
  };
   
  }
