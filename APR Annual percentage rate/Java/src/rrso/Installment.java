/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rrso;

import java.util.Date;

/**
 *
 * @author admin
 */
public class Installment {
     public int    installmentNo;
     public Date   dueDate;
     public double interestAmount;
     public double capitalAmount;
     public double installmentAmount;
     public double capitalBefore;
     public double capitalAfter;
     public double rrsoTime;
     public char   graceFlag;
     public String comments;
}
