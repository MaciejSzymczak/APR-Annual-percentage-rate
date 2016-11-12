/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rrso.table;

import java.io.Serializable;

/**
 *
 * @author mlisieck
 */
public class QuotationRow implements Serializable{
    public static final long serialVersionUID = 1L;
    private static int columnCount = 10;
    Integer installmentNo;
    String dueDate;
    Double interestAmount;
    Double capitalAmount;
    Double installmentAmount;
    Double capitalBefore;
    Double capitalAfter;
    Double rrsoTime;
    String graceFlag;
    String comments;

    @Override
    public String toString() {
        return 
          "installmentNo: "+installmentNo+
          "| dueDate: "+dueDate+
          "| interestAmount: "+interestAmount+
          "| capitalAmount: "+capitalAmount+
          "| installmentAmount: "+installmentAmount+
          "| capitalBefore: "+capitalBefore+
          "| capitalAfter: "+capitalAfter+
          "| rrsoTime: "+rrsoTime+
          "| graceFlag: "+graceFlag+
          "| comments: "+comments;
    }




    public QuotationRow(
      int installmentNo,
      String dueDate,
      Double interestAmount,
      Double capitalAmount,
      Double installmentAmount,
      Double capitalBefore,
      Double capitalAfter,
      Double rrsoTime,
      String graceFlag,
      String comments
            ) {
      this.installmentNo     = installmentNo;
      this.dueDate           = dueDate;
      this.interestAmount    = interestAmount;
      this.capitalAmount     = capitalAmount;
      this.installmentAmount = installmentAmount;
      this.capitalBefore     = capitalBefore;
      this.capitalAfter      = capitalAfter;
      this.rrsoTime          = rrsoTime;
      this.graceFlag         = graceFlag;
      this.comments          = comments;
    }

    public String get(int i) {
        switch (i) {
            case 0:
                return installmentNo+"";
            case 1:
                return dueDate;
            case 2:
                return interestAmount.toString();
            case 3:
                return capitalAmount.toString();
            case 4:
                return installmentAmount.toString();
            case 5:
                return capitalBefore.toString();
            case 6:
                return capitalAfter.toString();
            case 7:
                return rrsoTime.toString();
            case 8:
                return graceFlag;
            case 9:
                return comments;
            default:
                return null;
        }
    }

    public void set(int i, Object value){
        switch (i) {
            case 0:
                installmentNo = Integer.parseInt((String)value);
                break;
            case 1:
                dueDate = (String)value;
                break;
            case 2:
                interestAmount = Double.parseDouble( (String)value);
                break;
            case 3:
                capitalAmount = Double.parseDouble( (String)value);
                break;
            case 4:
                installmentAmount = Double.parseDouble( (String)value);
                break;
            case 5:
                capitalBefore = Double.parseDouble( (String)value);
                break;
            case 6:
                capitalAfter = Double.parseDouble( (String)value);
                break;
            case 7:
                rrsoTime = Double.parseDouble( (String)value);
                break;
            case 8:
                graceFlag = (String)value;
                break;
            case 9:
                comments = (String)value;
                break;
        }
    }

    


    /**
     * @return the columnCount
     */
    public static int getColumnCount() {
        return columnCount;
    }
}
