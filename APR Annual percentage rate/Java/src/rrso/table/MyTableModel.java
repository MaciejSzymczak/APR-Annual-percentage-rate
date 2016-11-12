/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rrso.table;

import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author mlisieck
 */
public class MyTableModel extends AbstractTableModel {
    private static final String[] colNames = new String[]{"Nr", "Data", "Odsetki", "Kapitał", "Rata", "Kap. przed", "Kap. po", "Czas RRSO", "Karencja", "Komentarz"};

    private static final Class[] colClasses = new Class[]{
    Integer.class
    ,String.class
    ,Number.class
    ,Number.class
    ,Number.class
    ,Number.class
    ,Number.class
    ,Number.class
    ,String.class
    ,String.class  };

    @Override
    public String getColumnName(int i) {
        return colNames[i];
    }

    @Override
    public boolean isCellEditable(int i, int i1) {
       return true;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        //return Double.class;
        //return colClasses[columnIndex];
        return super.getColumnClass(columnIndex);
    }


    ArrayList<QuotationRow> rowData = new ArrayList<QuotationRow>();

    public MyTableModel() {
        //rowData.add(new QuotationRow("1", "2", "3","4","5","6","7","8","9","10"));
        rowData.add(new QuotationRow(
             0 //InstallmentNo
           , "2010/01/01" //dueDate
           , 0.0 //interestAmount
           , 0.0 //capitalAmount
           , 110.0 //installmentAmount
           , 0.0 //capitalBefore
           , 0.0 //capitalAfter
           , 1.0 //rrsoTime
           , "0" //graceFlag
           , "Manual installment" ));


    }

    public int getRowCount() {
        return rowData.size();
    }

    public int getColumnCount() {
        return QuotationRow.getColumnCount();
    }

    public Object getValueAt(int row, int col) {
        return rowData.get(row).get(col);
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        rowData.get(row).set(col, value);
    }

    public void addRow(QuotationRow row){
        rowData.add(row);
        fireTableRowsInserted(rowData.size()-1, rowData.size()-1);
    }

    public void deleteRow(int rowNo){
        rowData.remove(rowNo);
        fireTableRowsDeleted(rowNo, rowNo);
    }
}
