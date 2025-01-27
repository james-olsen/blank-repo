package platformIndependentCore.datafiles;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import platformIndependentCore.exceptions.InvalidDataException;

public class CsvRow {
	static Logger log = LogManager.getLogger(CsvRow.class.getName());

	String origRow = "";
	String[] cells;
	String[] strippedCells;
	String DELIMITER = "~";
	String FORCE_TEXT = "`";

	public CsvRow(String row) {
		cells = row.split(DELIMITER, -1);
		origRow = row;
		setStrippedCells();
	}

	public CsvRow(String row, String delimiter) {
		DELIMITER = delimiter;
		cells = row.split(DELIMITER, -1);
		origRow = row;
		setStrippedCells();
	}

	/**
	 * Store an array of cell values without the FORCE_STOP character
	 */
	private void setStrippedCells() {
		strippedCells = new String[cells.length];
		for (int i = 0; i < cells.length; i++) {
			String s = cells[i];
			s = s.replace(FORCE_TEXT, "");
			strippedCells[i] = s;
		}
	}

	/**
	 * Will return a String representation of this row
	 */
	@Override
	public String toString() {
		String value = origRow;
		if (value.isEmpty()) {
			value = ">> ";
			for (String s : cells) {
				value += FORCE_TEXT + s + DELIMITER;
			}
		}
		return value;
	}

	/**
	 * Returns the cell value at the given index. This method will also remove any
	 * pre-pended characters before returning
	 *
	 * @param index - location of the cell in the row
	 * @return String value of the cell
	 */
	public String getCell(int index) {
		String value = "";

		if (index < strippedCells.length) {
			if (FORCE_TEXT.isEmpty()) {
				value = cells[index];
			} else {
				value = strippedCells[index];
			}
			if (value == null) {
				value = "";
			}
		} else {
			log.error("ERROR READING ROW: " + toString());
			throw new InvalidDataException("Unable to get cell with index: " + index + " for ROW=" + toString());
		}
		return value;

	}

	/**
	 * Returns an array of Cell values for this row without the FORCE_TEXT character
	 *
	 * @return String[] represents String values for each cell in the row
	 */
	public String[] getCells() {
		return FORCE_TEXT.isEmpty() ? cells : strippedCells;
	}

	/**
	 * Returns the number of Columns (cells) in this row
	 *
	 * @return int
	 */
	public int getColumnCount() {
		return cells.length;
	}
}
