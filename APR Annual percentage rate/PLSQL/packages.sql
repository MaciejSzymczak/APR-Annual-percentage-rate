CREATE OR REPLACE package installments_pkg as

--Pakiet liczy raty kredytu w jednym z dwóch wariantów: raty sta³e lub raty malej¹ce
--Za³o¿enie: raty s¹ p³acone co miesi¹c

/*
create global temporary table installments
(
  installment_no      number,
  due_date            date,
  interest_amount     number,
  capital_amount      number,
  installment_amount  number,
  capital_before      number,
  capital_after       number,
  rrso_time           number,
  grace_flag          char(1 byte),
  comments            varchar2(500 byte)
)

begin
  delete from xxmsztools_eventlog where module_name = 'INSTALLMENTS';
  commit;
  installments_pkg.calculate(
    schedule_type          => 'D' --C/D
   ,grace_periods          => 0
   ,repayment_periods      => 144
   ,loan                   => 100000
   ,yearly_interest_rate   => 3.5/100
   ,days_in_month          => 1
   ,days_in_year           => 12
   ,first_installment_date => trunc(sysdate)
   ,loan_date              => trunc(sysdate)
   );
end;

select * from installments order by installment_no

*/

function calculate_rrso_time ( loan_date date, installment_due_date date) return number;

procedure calculate(
    schedule_type                  char   default 'C' --D --=C=constant D=decresing
   ,grace_periods                  number             
   ,repayment_periods              number
   ,loan                           number
   -- monthly_rate = yearly_interest_rate * days_in_month / days_in_year   
   ,yearly_interest_rate           number             -- example: 3/100 = 3% used to calculate monthly_interest_rate = yearly_interest_rate * days_in_month / days_in_year
   ,days_in_month                  number default 0   -- 0 means real days in month
   ,days_in_year                   number default 0   -- 0 meand real days in year
   ,yearly_interest_rate_increase  number default 0 
   ,increase_rate_period_from      number default 0     
   ,increase_rate_period_to        number default 0         
   -- 
   ,first_installment_date         date               --put here correct day of month, next installments will be in same day of month as well
   ,loan_date                      date 
   ,precision                      number default 2
 );

end;
/

CREATE OR REPLACE package body installments_pkg as

type  tinstallment is record (
     installment_no     number,
     due_date           date  ,
     interest_amount    number,
     capital_amount     number,
     installment_amount number,
     capital_before     number,
     capital_after      number,
     rrso_time          number,
     grace_flag         char(1),
     comments           varchar2(500)       
   );

type tinstallments is table of tinstallment index by binary_integer;   
   
installments tinstallments;

debug_text varchar2(500) := '';

--------------------------------------------------------------------------------------------------------
procedure debug ( m varchar2 ) is
begin
  --select * from xxmsztools_eventlog where module_name = 'INSTALLMENTS'  order by id 
  --delete from xxmsztools_eventlog where module_name = 'INSTALLMENTS';
  --xxmsz_tools.insertIntoEventLog(m,'I','INSTALLMENTS');
  null;
end;
  
--------------------------------------------------------------------------------------------------------
procedure add_installment (installment tinstallment) is
  t binary_integer;
begin
  t := installments.count;
  installments ( t )         := installment;
  debug_text := '';
end;

--------------------------------------------------------------------------------------------------------
procedure populate is
  t binary_integer;
begin
  delete from installments;
  t := installments.first;
  while t is not null loop
     insert into installments (
      installment_no     ,
      due_date           ,
      interest_amount    ,
      capital_amount     ,
      installment_amount ,
      capital_before     ,
      capital_after      ,
      rrso_time          ,
      grace_flag         ,
      comments
     ) values (
      installments(t).installment_no     ,
      installments(t).due_date           ,
      installments(t).interest_amount    ,
      installments(t).capital_amount     ,
      installments(t).installment_amount ,
      installments(t).capital_before     ,
      installments(t).capital_after      ,
      installments(t).rrso_time          ,
      installments(t).grace_flag         ,
      installments(t).comments
     );
     t := installments.next(t); 
  end loop;
end;


--------------------------------------------------------------------------------------------------------
function isLeapYear(i_year number) return boolean as
begin
   if MOD(i_year, 400) = 0 or ( MOD(i_year, 4) = 0 and MOD(i_year, 100) != 0) then
      return true;
   else
      return false;
   end if;
end;

--------------------------------------------------------------------------------------------------------
function daysInYear (d date) return number is
begin
  if isLeapYear( to_char(d,'yyyy') ) then
    return 366;
  else
    return 365;
  end if; 
end;

--------------------------------------------------------------------------------------------------------
function calculate_rrso_time ( loan_date date, installment_due_date date) return number is
  years_comparision number;
  ------------------
  function dayOfYear (d date) return number is
  begin
    return trunc(d) - trunc(d,'yyyy') + 1;
  end;
  ------------------
  function lastDayOfYear ( d date ) return date is
  begin
   return to_date( '31-12-'||to_char(d,'yyyy') ,'dd-mm-yyyy');
  end;
  ------------------
begin
  years_comparision := to_char(installment_due_date,'yyyy') - to_char(loan_date,'yyyy');
  if years_comparision = 0 then
      debug_text :=  dayOfYear(installment_due_date) ||'  -  '|| dayOfYear(loan_date)  ||'  /  '||  daysInYear(installment_due_date) || '  =   ' || (  dayOfYear(installment_due_date) - dayOfYear(loan_date)   ) /  daysInYear(installment_due_date);
      return (  dayOfYear(installment_due_date) - dayOfYear(loan_date)   ) /  daysInYear(installment_due_date);
  else
      debug_text :=  
             dayOfYear(installment_due_date) ||'/'|| daysInYear(installment_due_date) ||'+'|| 
             '('||   dayOfYear( lastDayOfYear(loan_date) ) ||'-'|| dayOfYear(loan_date)  ||') /'||  daysInYear(installment_due_date) ||'+'|| 
             years_comparision ||'-1';
      return dayOfYear(installment_due_date) /  daysInYear(installment_due_date) + 
             (   dayOfYear( lastDayOfYear(loan_date) ) - dayOfYear(loan_date)   ) /  daysInYear(installment_due_date) + 
             years_comparision -1;
  end if;
end;


function non_Zero ( a number, b number ) return number is
begin
 if a = 0 then 
  return b;
 else
  return a;
 end if;
end;

--------------------------------------------------------------------------------------------------------
procedure calculate(
    schedule_type          char   default 'C' --D
   ,grace_periods          number
   ,repayment_periods      number
   ,loan                   number
   ,yearly_interest_rate   number
   ,days_in_month          number default 0
   ,days_in_year           number default 0
   ,yearly_interest_rate_increase  number default 0 
   ,increase_rate_period_from      number default 0     
   ,increase_rate_period_to        number default 0         
   ,first_installment_date date
   ,loan_date              date
   ,precision              number default 2   
 )
is
   installment               tinstallment;
   installment_number        number := 0;
   last_month_day            number;
   monthly_interest_rate     number;
   balance_capital           number;
   --------------------------------------------
   function real_yearly_interest_rate return number is
   begin
     if installment_number between increase_rate_period_from and increase_rate_period_to then
       return yearly_interest_rate + yearly_interest_rate_increase; 
     else
      return yearly_interest_rate; 
     end if;         
   end;
 ------------------------------------------------------  
 begin
  installments.delete;
  --
  --grace
  balance_capital := loan;
  for r in 1..grace_periods loop
     installment_number                     := installment_number + 1;
     installment.installment_no             := installment_number;
     select add_months( first_installment_date, installment_number-1) into installment.due_date from dual;
     installment.rrso_time := calculate_rrso_time ( loan_date, installment.due_date );
     installment.grace_flag := 'Y';
     last_month_day := to_number(to_char(last_day( installment.due_date ),'dd'));       
     installment.interest_amount            := round( loan * real_yearly_interest_rate * non_zero ( days_in_month,  last_month_day ) / non_zero( days_in_year,  daysInYear(installment.due_date) ), precision);
     installment.capital_amount             := 0;
     installment.installment_amount         := round( installment.interest_amount + installment.capital_amount , precision);
     installment.capital_before             := balance_capital;
     installment.capital_after              := balance_capital;
     add_installment (installment);
  end loop;
  --
  -- repayment
  for r in 1..repayment_periods loop
     installment_number                       := installment_number + 1;
     installment.installment_no               := installment_number;
     select add_months( first_installment_date, installment_number-1) into installment.due_date from dual;
     installment.rrso_time := calculate_rrso_time ( loan_date, installment.due_date );
     installment.grace_flag := 'N';
     last_month_day := to_number(to_char(last_day( installment.due_date ),'dd'));       
     monthly_interest_rate                    := real_yearly_interest_rate * non_zero( days_in_month,  last_month_day ) / non_zero( days_in_year,  daysInYear(installment.due_date)); 
     if schedule_type = 'D' then
       installment.capital_before             := round( balance_capital                                            , precision); 
       installment.capital_amount             := round( loan / repayment_periods                                   , precision);
       installment.interest_amount            := round( balance_capital * monthly_interest_rate                    , precision);
       installment.installment_amount         := round( installment.interest_amount + installment.capital_amount   , precision);
       balance_capital                        := round( balance_capital - installment.capital_amount               , precision);
       installment.capital_after              := round( balance_capital                                            , precision);
     else --schedule_type=C
       installment.capital_before             := round( balance_capital , precision); 
       installment.installment_amount         := round( loan * power( (monthly_interest_rate+1) , repayment_periods) * ( (monthly_interest_rate+1)-1) / ( power( (monthly_interest_rate+1) ,repayment_periods) - 1 ) , precision);
       /*
       -- calulation installment.installment_amount should be simplyfied:
       select :kredytKwota * (:oproc / 12) / (1 -  power((1 + :oproc / 12) , (-:kredytOkresSplaty)) )
        - 
        :kredytKwota * power( (1 + :oproc / 12) , :kredytOkresSplaty) * ( (1 + :oproc / 12)-1) / ( power( (1 + :oproc / 12) ,:kredytOkresSplaty) - 1 ) x 
       from dual
       */
       installment.interest_amount            := round( balance_capital * monthly_interest_rate , precision);
       installment.capital_amount             := round( installment.installment_amount - installment.interest_amount , precision);
       balance_capital                        := round( balance_capital - installment.capital_amount , precision);
       installment.capital_after              := round( balance_capital , precision); 
     end if;
     --rounds
     if r = repayment_periods then
       if installment.capital_after             <> 0 then
         --raise_application_error(-20000, 'ROUNDS !! : ' || installment.capital_after_installment );
         installment.capital_amount     := round( installment.capital_amount + installment.capital_after      , precision);
         installment.installment_amount := round( installment.installment_amount + installment.capital_after  , precision);  
         installment.capital_after      := 0;
         balance_capital                := 0;
       end if;
     end if; 
     installment.comments := debug_text;
     add_installment (installment);
  end loop;
  populate;
 end;

  
end;
/

CREATE OR REPLACE PACKAGE rrso as

/*
 -- otrzymujê z banku 200 z³, a muszê zwróciæ 230 z³.
 -- 100 z³ otrzymujê natychmiast, kolejne 100 z³ za rok
 -- 120 z³ muszê zap³aciæ po dwóch latach, 110 po trzech.
 -- ile wynosi rsso ? 7% (7% wiecej musze oddac, ni¿ po¿yczono)
declare
 res number;
begin
 rrso.init( pmaxInterations => 1000
          , prsso_precision => 2 
          );
 rrso.add_loan        (100, 0); 
 rrso.add_loan        (100, 1);
 rrso.add_installment (120,2);
 rrso.add_installment (110,3);
 res := rrso.get_rsso;
 raise_application_error(-20000, '    RRSO =    ' || res * 100 ||'%');
end;
*/

procedure init 
          ( pmaxInterations number
          , prsso_precision number 
          );
          
procedure add_installment (pamount number, pperiod number);
procedure add_loan (pamount number, pperiod number);
function  get_rsso return number;

end;
/

CREATE OR REPLACE PACKAGE BODY rrso as

  maxInterations      number;
  acceptableAccurance number; 
  rsso_precision      number; 

  type trate is record (
   amount number,
   period number
  );
  type trates is table of trate index by binary_integer;
  installments trates;
  loans trates;

procedure init 
          ( pmaxInterations number
          , prsso_precision number 
          ) 
is 
begin
  maxInterations      := pmaxInterations;
  acceptableAccurance := power(10,-prsso_precision);
  rsso_precision      := prsso_precision; 
  
  installments.delete;
  loans.delete; 
end;

--------------------------------------------------------------------------------------------------------
procedure add_installment (pamount number, pperiod number) is
  t binary_integer;
begin
  t := installments.count;
  installments ( t ).amount := pamount;
  installments ( t ).period := pperiod;
end;

--------------------------------------------------------------------------------------------------------
procedure add_loan (pamount number, pperiod number) is
  t binary_integer;
begin
  t := loans.count;
  loans ( t ).amount := pamount;
  loans ( t ).period := pperiod;
end;

--------------------------------------------------------------------------------------------------------
function get_rsso return number is
  prior_i number := 0;
  i number := 100; --=10000%
  min_i number := 0;
  max_i number := 100;
  avg_i number := 50;
  ric number;
  rlc number;
  sentry number := 0;
  --------------------------------------------------------------------------------------------------------
  function Real_installments_cost ( p_i number ) return number is 
  res number;
  t   binary_integer;
  begin
    res := 0;
    t := installments.first;
    while t is not null loop
       res := res +  ( installments ( t ).amount /  power( (1+p_i),installments ( t ).period)  );    
       t := installments.next(t); 
    end loop;
   
   return res;
  end;
  --------------------------------------------------------------------------------------------------------
  function Real_loans_cost ( p_i number ) return number is 
  res number;
  t   binary_integer;
  begin
    res := 0;
    t := loans.first;
    while t is not null loop
       res := res +  ( loans ( t ).amount /  power( (1+p_i),loans ( t ).period)  );    
       t := loans.next(t); 
    end loop;
   
   return res;
  end;
  --------------------------------------------------------------------------------------------------------
  procedure debug ( m varchar2 ) is
  begin
    null;
    --select * from xxmsztools_eventlog where module_name = 'RRSO'  order by id 
    --delete from xxmsztools_eventlog where module_name = 'RRSO';
    --xxmsz_tools.insertIntoEventLog(m,'I','RRSO');
  end;
--------------------------------------------------------------------------------------------------------
begin
 if loans.first is null then raise_application_error(-20000, 'At least one loan has to be delivered'); end if;  
 if installments.first is null then raise_application_error(-20000, 'At least one installment has to be delivered'); end if;  
 -- idea: 
 -- i1..i5 : next approximations
 --0                                                                i1
 --0                              i2                                i1
 --0                              i2               i3               i1
 --0                              i2      i4       i3               i1
 --0                              i2      i4   i5  i3               i1
 --                                            * - this is correct value
 loop
   prior_i := i;
   i := min_i + abs(max_i - min_i) / 2;   
   --
   ric := Real_installments_cost ( i );
   rlc := Real_loans_cost ( i );
   debug('min_i='||min_i||'     max_i='||max_i||'     i='||i||'     ric='||ric);
   --
   if abs(prior_i - i) < acceptableAccurance then
     --i is correct. contratulations !
     return round ( i, rsso_precision);    
   elsif ric < rlc then
     min_i := min_i;
     max_i := i; 
   elsif ric > rlc then
     min_i := i;
     max_i := max_i;
   end if;
   --
   sentry := sentry + 1;
   if sentry > maxInterations then raise_application_error(-20000, 'Result was not calculated but max approximation steps was exeeded'); end if;
 end loop;
end;
  
end;
/

