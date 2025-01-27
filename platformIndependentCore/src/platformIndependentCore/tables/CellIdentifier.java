package platformIndependentCore.tables;

import platformIndependentCore.exceptions.ObjectNotFoundException;

/**
 * This class can be used for tables where the standard header index code in the
 * Core will not work. This allows for a user to specify the column index for a
 * value instead of the column header.
 *
 * When using this, please ensure you are calculating the column index in custom
 * code and not just hardcoding values.
 *
 * @author vbaaustaylol
 *
 */
public class CellIdentifier {
	/** Column Index to locate cell in */
	int columnIndex = -1;
	/** Text value to match to cell */
	String cellValue = "";

	/**
	 * Constructor with the value/index pair to be stored
	 *
	 * @param value     of the cell
	 * @param cellIndex of the cell
	 */
	public CellIdentifier(String value, int cellIndex) {
		setColumnIndex(cellIndex);
		setCellValue(value);
	}

	/**
	 * Returns the index for the desired column
	 *
	 * @return index of the cell
	 */
	public int getColumnIndex() {
		return columnIndex;
	}

	/**
	 * Sets the column index
	 *
	 * @param columnIndex for the cell
	 */
	public void setColumnIndex(int columnIndex) {
		if (columnIndex < 0) {
			throw new ObjectNotFoundException("Invalid column index.");
		}
		this.columnIndex = columnIndex;
	}

	/**
	 * Returns the text value for the cell
	 *
	 * @return String value of the cell
	 */
	public String getCellValue() {
		return cellValue;
	}

	/**
	 * Sets the required text value for the cell
	 *
	 * @param cellValue to set for this cellIdentifier
	 */
	public void setCellValue(String cellValue) {
		this.cellValue = cellValue;
	}

	@Override
	public String toString() {
		return "[" + getColumnIndex() + "]" + "=" + getCellValue();
	}
}
