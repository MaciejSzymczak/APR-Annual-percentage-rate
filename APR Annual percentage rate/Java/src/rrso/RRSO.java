/*
 * 2010.07.06
 *
 * Klasa służy do wyliczania RRSO.
 * RRSO liczone jest zgodnie z regułą opisaną tutaj
 *  http://pl.wikipedia.org/wiki/Rzeczywista_roczna_stopa_oprocentowania
 * 
 * Przykład użycia algorytmu:
     Otrzymuję z banku 200 zł, a muszę zwrócić 230 zł.
     100 zł otrzymuję natychmiast, kolejne 100 zł za rok
     120 zł muszę zapłacić po dwóch latach, 110 po trzech.
     ile wynosi rsso ? 7% (7% wiecej musze oddac, niż pożyczono)

 * RRSO rrso = new RRSO();
 * rrso.initalize(1000, 0.01, 2);
 * rrso.addLoan        (100 ,0); 
 * rrso.addLoan        (100 ,1);
 * rrso.addInstallment (120 ,2);
 * rrso.addInstallment (110 ,3);
 * double resultRRSO = rrso.getRsso();
 * if ( !isEmpty( rrso.errorMessage ) )
 *  { JOptionPane.showMessageDialog(null, "Błąd: " + rrso.errorMessage , "Wynik", 1); }
 * else
 *  { JOptionPane.showMessageDialog(null, "RRSO = " + resultRRSO*100 +"%" , "Wynik", 1); }
 *
 */

package rrso;

import java.util.ArrayList;
import java.util.List;
import java.lang.Math;
import java.math.BigDecimal;

/**
 *
 * @author Maciej Szymczak
 */
public class RRSO {

  private int maxInterations;
  private double acceptableAccurance;
  private int rssoPrecision;

  public String errorMessage;

  private class Rate {
    public double amount;
    public double period;
  }

  List installments = new ArrayList();
  List loans        = new ArrayList();

  public void initalize (
            int  pmaxInterations
          , int  prssoPrecision )
  {
    maxInterations      = pmaxInterations;
    acceptableAccurance = Math.pow(10, -prssoPrecision);
    rssoPrecision       = prssoPrecision;
    installments.clear();
    loans.clear();
  }

  public void addInstallment (double pamount , double pperiod )
  {
   Rate rate = new Rate();
   rate.amount = pamount;
   rate.period = pperiod;

   installments.add ( rate );
  }

  public void addLoan (double pamount , double pperiod )
  {
   Rate rate = new Rate();
   rate.amount = pamount;
   rate.period = pperiod;

   loans.add ( rate );
  }

  private double realInstallmentsCost ( double p_i  )
  {
    double res = 0;
    Rate [] installmentsArray;

    installmentsArray = (Rate[]) installments.toArray( new Rate[installments.size()] );
    for (int i = 0; i < installmentsArray.length; i++) {
       res = res +  ( installmentsArray[i].amount /  Math.pow( (1+p_i),installmentsArray[i].period)  );
    }
   return res;
  };

  private double realLoansCost ( double p_i  )
  {
    double res = 0;
    Rate [] loansArray;
    loansArray = (Rate[]) loans.toArray( new Rate[loans.size()] );
    for (int i = 0; i < loansArray.length; i++) {
       res = res +  ( loansArray[i].amount /  Math.pow( (1+p_i),loansArray[i].period)  );
    }
   return res;
  };

  public static double round(double d, int decimalPlace){
    // see the Javadoc about why we use a String in the constructor
    // http://java.sun.com/j2se/1.5.0/docs/api/java/math/BigDecimal.html#BigDecimal(double)
    BigDecimal bd = new BigDecimal(Double.toString(d));
    bd = bd.setScale(decimalPlace,BigDecimal.ROUND_HALF_UP);
    return bd.doubleValue();
  }

  public double getRsso()
{
 errorMessage = "";
 double prior_i = 0;
 double i       = 100; // =10000%
 double min_i   = 0;
 double max_i   = 100;
 double avg_i   = 50;
 double ric;
 double rlc;
 int    sentry = 0;
 if (loans.size() == 0)        { errorMessage = "At least one loan has to be delivered"; };
 if (installments.size() == 0) { errorMessage = "At least one installment has to be delivered"; };
 while (true)
 {
   prior_i = i;
   i = min_i + Math.abs(max_i - min_i) / 2;
   
   ric = realInstallmentsCost ( i );
   rlc = realLoansCost ( i );
   
   if ( Math.abs(prior_i - i) < acceptableAccurance )
   {
     //i is correct. contratulations !
     return round ( i, rssoPrecision);
   }
   else if ( ric < rlc )
   {
     min_i = min_i;
     max_i = i;
   }
   else if (ric > rlc)
   {
     min_i = i;
     max_i = max_i;
   };
   
   sentry++;
   if (sentry > maxInterations) 
   {
    errorMessage = "Result was not calculated but max approximation steps was exeeded";
    break;
   }
 };
 return -1;
};

}