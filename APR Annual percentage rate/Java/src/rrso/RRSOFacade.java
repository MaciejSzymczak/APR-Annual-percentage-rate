/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rrso;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author admin
 */
public class RRSOFacade {

public RRSOFacadeResults calculate (
 // RRSO - both sides
 // assumption: always one loan paid in time 0. It is possible to add more loans: just multiply rrso.add_loan statement
     double loan
 // RRSO - left side
   , double cost
   , double leftSideRate //currency buy rate
   , int    maxInterations
   , int    rsso_precision
 // RRSO - right side
   , char   scheduleType     // C / D
   , int    gracePeriods
   , int    repaymentPeriods
   , double yearlyInterestRate         // example: 3/100 = 3% used to calculate monthly_interest_rate = yearly_interest_rate * days_in_month / days_in_year
   , int    daysInMonth                // 0 = get real days in month
   , int    daysInYear                 // 0 = get real days in year
   , double yearlyInterestRateIncrease
   , int    increaseRatePeriodFrom
   , int    increaseRatePeriodTo
   , Date   firstInstallmentDate
   , Date   loanDate
   , int    precision
   , double rightSideRate   //currency sale rate
 ) throws ParseException
 {
   double interestAmountSum = 0;

   Installments installments = new Installments();
   List<Installment> res = new ArrayList<Installment>();
   res =
   installments.calculate(
       scheduleType
     , gracePeriods
     , repaymentPeriods
     , loan
     , yearlyInterestRate
     , daysInMonth
     , daysInYear
     , yearlyInterestRateIncrease
     , increaseRatePeriodFrom
     , increaseRatePeriodTo
     , firstInstallmentDate
     , loanDate
     , precision
   );

   RRSO rrso = new RRSO();
   rrso.initalize(maxInterations, rsso_precision);
   rrso.addLoan( (loan + cost )* leftSideRate, 0 ); //0 is correct. loanDate is included in rrsoTime

   for (Installment i : res ) {
     interestAmountSum += i.interestAmount;
     rrso.addInstallment( i.installmentAmount * rightSideRate, i.rrsoTime );
   }

   // outs
   Double resultRRSO = rrso.getRsso();

   RRSOFacadeResults rrsoFacadeResults = new RRSOFacadeResults();
   rrsoFacadeResults.rrso              =  resultRRSO;
   rrsoFacadeResults.errorMessage      =  rrso.errorMessage;
   rrsoFacadeResults.interestAmountSum =  interestAmountSum;

   return rrsoFacadeResults;
 }

}

