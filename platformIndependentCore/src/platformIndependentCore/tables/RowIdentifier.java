package platformIndependentCore.tables;

/**
 * Class to hold on to all the values required to identify a row
 *
 * @author VBAAUSTAYLOL
 *
 */
public class RowIdentifier {
	/** Value of Cell to locate in row */
	String cellValue;
	/** Is this a "Starts With" criteria? */
	boolean startsWith;
	/** Array of all CellIdentifiers required to locate the row */
	private CellIdentifier[] cellIds = new CellIdentifier[0];

	/**
	 * Constructor will take an unspecified number of Strings that will be used to
	 * match cells in the table.
	 *
	 * @param cellIdentifiers used to match rows
	 */
	public RowIdentifier(CellIdentifier... cellIdentifiers) {
		this.cellIds = cellIdentifiers;
	}

	/**
	 * Returns all the cell identifier requirements
	 *
	 * @return list of required cell identifiers
	 */
	public CellIdentifier[] getCellIdentifiers() {
		return cellIds;
	}

	/**
	 * Sets the cell ids to provided list
	 *
	 * @param cellIdentifiers list of cell identifiers for this row id
	 */
	public void setCellIdentifiers(CellIdentifier... cellIdentifiers) {
		this.cellIds = cellIdentifiers;
	}

	/**
	 * Returns a String array containing the identifying values
	 *
	 * @return String[] identifying values
	 */
	public String[] getIdentifyingValues() {
		String[] columns = new String[cellIds.length];
		for (int i = 0; i < cellIds.length; i++) {
			CellIdentifier cell = cellIds[i];
			columns[i] = cell.getCellValue();

		}

		return columns;
	}

	@Override
	public String toString() {
		String val = "";
		if (cellIds.length > 0) {
			for (CellIdentifier id : cellIds) {
				val += id + ":";
			}
		}
		return val;
	}
}
