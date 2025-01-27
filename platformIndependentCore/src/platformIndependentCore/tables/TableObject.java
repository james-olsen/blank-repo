package platformIndependentCore.tables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import platformIndependentCore.core.AutomatedObject;
import platformIndependentCore.core.Search;
import platformIndependentCore.core.ToolManager;
import platformIndependentCore.exceptions.InvalidDataException;
import platformIndependentCore.exceptions.InvalidStateException;

/**
 * Platform Independent object that gives a standard interface/api for
 * interacting with table objects across multiple testing platforms. This object
 * should always be used within a CustomTable object which will wrap the
 * AutomationTool specific implementations (SeleniumTable, RftTable, etc) and
 * allow for customizations needed within CustomTable code
 *
 * @author VBAAUSTAYLOL
 *
 */
public abstract class TableObject extends ToolManager {
	/** A cache of the row Indexes for their associated ids */
	HashMap<String, Integer> rowIndexCache = new HashMap<String, Integer>();
	/**
	 * A cache of the column Indexes for each header value. Using a TreeMap to
	 * preserve order
	 */
	Map<String, Integer> columnIndexCache = new TreeMap<String, Integer>();

	/** Need to keep track of id columns and their indexes */
	CellIdentifier[] idColumns = new CellIdentifier[0];

	/**
	 * Create an empty default Search object that can be customized by implementing
	 * classes
	 */
	Search defaultSearch = super.getSearch();

	/** Hold on to the AutomatedObject instance of this TableObject */
	private AutomatedObject tableObject;
	/** Does this table have a THEAD? Default to TRUE */
	boolean thead = true;
	/** Default value for a BLANK header value */
	protected String blank_header = "<BLANK>";
	/** Log instance for debug output */
	Logger log = LogManager.getLogger(TableObject.class.getName());

	/**
	 * Constructor is protected so that only extending classes can call it (as
	 * super(...)) Instances of TableObject should only be obtained using the static
	 * createInstance method, and should always be used from inside of a CustomTable
	 *
	 * To aid in customization, there is an initialize method from the constructor
	 * which can be used for any necessary set up <br>
	 * <br>
	 * <b>storeColumnHeaders</b> - which will set up the column header/index
	 * mappings is called from CustomTable to make sure that any customizations are
	 * picked up before trying to work with the header columns<br>
	 * <br>
	 * Descending classes may override these methods as needed. Initialize is called
	 * first, then storeColumnHeaders, and finally storeIdColumnHeaders are called
	 * from the CustomTable constructor after this object is created<br>
	 *
	 * @param object                instance of the table
	 * @param identifyingCellValues list of columns used to identify rows in the
	 *                              table
	 *
	 */
	protected TableObject(AutomatedObject object, String... identifyingCellValues) {
		tableObject = object;
		// Call initialize to all descending classes to do any necessary set up
		initialize(object, identifyingCellValues);
	}

	/**
	 * Sets the default Search object to the one provided.
	 * <p>
	 * This allows deriving Table classes to customize their Default Search for the
	 * project specifics
	 *
	 * @param search Default Search for this table
	 */
	protected void setDefaultSearch(Search search) {
		defaultSearch = search;
	}

	/**
	 * Returns a Search instance with the default values, setting this table as the
	 * parent unless the default Search has been set to use an alternate parent
	 *
	 * @return Search with default values set
	 */
	@Override
	protected Search getSearch() {
		Search newSearch = new Search(defaultSearch);
		if (defaultSearch.getParent() == null) {
			newSearch.setParent(getAutomatedObject());
		} else {
			newSearch.setParent(defaultSearch.getParent());
		}
		return newSearch;
	}

	/**
	 * Will allow descending classes to implement set up code to be executed prior
	 * to storing the header cache <br>
	 * <br>
	 * Will default to a no-op method unless a descending class overrides it <br>
	 * <br>
	 * This code is called from the constructor prior to storeColumnHeaders
	 *
	 * @param object             table object
	 * @param identifyingColumns list of columns used to identify rows in the table
	 */
	protected void initialize(AutomatedObject object, String... identifyingColumns) {

	}

	/**
	 * Stores the cell value and the cell index for the header row
	 *
	 * This code will be called last (after initialize and storeColumnHeaders) from
	 * the constructor
	 *
	 * @param identifyingColumns list of columns used to identify rows in the table
	 */
	protected void storeIdColumnHeaders(String... identifyingColumns) {
		idColumns = new CellIdentifier[identifyingColumns.length];
		for (int i = 0; i < identifyingColumns.length; i++) {
			String id = identifyingColumns[i];
			idColumns[i] = new CellIdentifier(identifyingColumns[i], getColumnIndex(id));
		}
	}

	/**
	 * Stores the cell value and the cell index for the header row
	 *
	 * This code will be called (after initialize) from the constructor
	 */
	protected void storeColumnHeaders() {
		if (!isColumnIndexCache()) {
			Search search = getSearch();
			search.setParent(getAutomatedObject());
			search.addCriteria("tag", "TH");
			int colIndex = 0;
			List<AutomatedObject> ths = getAutomationTool().getAutomatedObjects(search);
			for (AutomatedObject th : ths) {
				String header = th.readText().trim().isEmpty() ? blank_header : th.readText();
				log.log(Level.forName("TableLogger", 250), "TH [" + colIndex + "] header=" + header);

				putColumnIndexInHashMap(header, colIndex);
				colIndex++;
			}
		}
	}

	/**
	 * Stores the mappings from column headers to the cell index for those columns
	 *
	 * @param columns mapping of header text to cell index
	 */
	protected void setColumnHeaderCache(Map<String, Integer> columns) {
		columnIndexCache = columns;
	}

	/**
	 * Sets the mappings of header text to cell indexes to values provided in the
	 * list of cell identifiers
	 *
	 * @param columnCellIds list of cell values to cell indexes
	 */
	protected void setColumnHeaderCells(CellIdentifier... columnCellIds) {
		log.log(Level.forName("TableLogger", 250),
				"setColumnHeaderCells cellIds=" + Arrays.deepToString(columnCellIds));

		// reset the columnIndexCache to clear out any previous values
		columnIndexCache.clear();
		for (CellIdentifier cell : columnCellIds) {
			columnIndexCache.put(cell.getCellValue(), cell.getColumnIndex());
		}
	}

	/**
	 * Returns whether the columnIndexCache contains values
	 *
	 * @return true if the column index cache (header cell values to indexes) has
	 *         values; false otherwise
	 */
	protected boolean isColumnIndexCache() {
		return columnIndexCache.size() > 0;
	}

	/**
	 * Returns a row identifier object for the specified values
	 *
	 * @param identifyingCellValues a list of values to match the row on
	 * @return RowIdentifier object to represent the required cell values for the
	 *         row
	 */
	public RowIdentifier getRowIdentifier(String... identifyingCellValues) {
		CellIdentifier[] idColumns = this.getIdentifyingColumns();
		int numColumns = idColumns.length;
		if (numColumns <= 0) {
			throw new InvalidStateException("No identifying columns found.");
		}
		// if id columns were specified for the table, make sure there are the same
		// number of cell ids
		if (idColumns.length > 0 && idColumns.length != identifyingCellValues.length) {
			throw new InvalidDataException("The specified rowID (" + Arrays.toString(identifyingCellValues)
					+ ") does not have the required number of values" + Arrays.toString(idColumns));
		}
		CellIdentifier[] cellIds = new CellIdentifier[identifyingCellValues.length];
		for (int i = 0; i < identifyingCellValues.length; i++) {
			int columnIndex = idColumns[i].getColumnIndex();

			CellIdentifier cellId = new CellIdentifier(identifyingCellValues[i], columnIndex);
			cellIds[i] = cellId;
		}
		log.log(Level.forName("TableLogger", 250), "getRowIdentifier cellIds=" + Arrays.deepToString(cellIds));

		RowIdentifier rowIdentifier = new RowIdentifier(cellIds);
		return rowIdentifier;
	}

	/**
	 * Returns the header row for the table
	 *
	 * @return AutomatedObject representing the header row
	 */
	public abstract AutomatedObject getHeaderRow();

	/**
	 * Returns the cell for the specified column
	 *
	 * ** Please note that the parent set in search config MUST be a row object
	 *
	 * @param columHeaderToGet identifies desired column
	 * @param search           criteria to find matching row
	 * @return AutomatedObject cell object for matching row and column
	 */
	protected abstract AutomatedObject getCell(String columHeaderToGet, Search search);

	/**
	 * Returns the row with the matching id. Uses default search settings.
	 *
	 * @param rowId criteria
	 * @return AutomatedObject row matching provided row identifier criteria
	 */
	public AutomatedObject getRow(RowIdentifier rowId) {
		return getRow(rowId, getSearch());
	}

	/**
	 * Returns the row with the matching id, will use settings from the provided
	 * search
	 *
	 * @param rowId  criteria
	 * @param search criteria to find object
	 * @return AutomatedObject row matching criteria
	 */
	public abstract AutomatedObject getRow(RowIdentifier rowId, Search search);

	/**
	 * Returns the row index for the row with the matching id
	 *
	 * @param rowId  criteria
	 * @param search criteria to find object
	 * @return int index of matching row
	 */
	protected abstract int getRowIndex(RowIdentifier rowId, Search search);

	/**
	 * Returns the row with the matching id
	 *
	 * @param rowId  criteria
	 * @param search criteria to find object
	 * @return AutomatedObject[] matching rows
	 */
	protected abstract AutomatedObject[] getRows(RowIdentifier rowId, Search search);

	/**
	 * Reads and returns the value in the specified cell
	 *
	 * @param cell to read value of
	 * @return String value of the cell
	 */
	protected abstract String readValue(AutomatedObject cell);

	/**
	 * Returns TRUE if the table has a THEAD object, false otherwise
	 *
	 * @return boolean true if the table has a THEAD object
	 */
	public boolean isThead() {
		return thead;
	}

	/**
	 * Sets the thead variable that specifies if this table object contained a THEAD
	 *
	 * @param thead set to true if the table should use a THEAD object
	 */
	public void setThead(boolean thead) {
		this.thead = thead;
	}

	/**
	 * Returns the AutomatedObject for this table
	 *
	 * @return AutomatedObject representation for the table
	 */
	public AutomatedObject getAutomatedObject() {
		return tableObject;
	}

	/**
	 * Returns the IDENTIFYING_COLUMNS for this table
	 *
	 * @return CellIdentifier[] columns used to identify matching rows in the table
	 */
	protected CellIdentifier[] getIdentifyingColumns() {
		return idColumns;
	}

	/**
	 * Returns the column index cache that maps column headers to column indexes for
	 * this table
	 *
	 * @return Map<String, Integer> map of column headers to column indexes
	 */
	Map<String, Integer> getColumnIndexCache() {
		return columnIndexCache;
	}

	/**
	 * Returns the Column Index for the specified Column Name Will store it in the
	 * hashmap for future use
	 *
	 * @param columnName to locate cell in row
	 * @return int column index where the matching header column name is located
	 */
	public abstract int getColumnIndex(String columnName);

	/**
	 * Returns the cell for the specified column
	 *
	 * ** Please note that the parent set in search config MUST be a row object
	 *
	 * @param rowId            contains required values to match the row
	 * @param columHeaderToGet identifies the desired column
	 * @return AutomatedObject cell matching criteria
	 */
	protected AutomatedObject getCell(RowIdentifier rowId, String columHeaderToGet) {
		AutomatedObject row = getRow(rowId, getSearch());
		int columnIndex = this.getColumnIndex(columHeaderToGet);
		Search cellSearch = getSearch();
		cellSearch.addCriteria("tag", "TD");
		cellSearch.setParent(row);
		ArrayList<AutomatedObject> cells = getAutomationTool().getAutomatedObjects(cellSearch); // AutomationHelper.findObjects(cellSearch);

		AutomatedObject cell = null;
		if (cells != null && cells.size() > columnIndex) {
			cell = cells.get(columnIndex);
		}
		return cell;
	}

	/**
	 * Returns the Column Index value that is stored in the hash map for the
	 * specified Column Name
	 *
	 * @param columnName to locate cell in row
	 * @return int column index
	 */
	protected int getColumnIndexFromHashMap(String columnName) {

		columnName = columnName.isEmpty() ? blank_header : columnName;

		Integer index = columnIndexCache.get(columnName);

		if (index == null) {
			index = -1;
		}

		return index;
	}

	/**
	 * Returns the Column Name that is stored in the hash map for the specified
	 * Column Index
	 *
	 * @param columnIndex to read the header value from
	 * @return String text value from cell in header for the desired index
	 */
	protected String getColumnNameFromHashMap(int columnIndex) {
		String name = "";
		// We are looking up the Key given the Value (which backwards from how
		// the data is stored in the hashmap). For this reason, we have to
		// iterate through all the Entries in the hashmap to find the matching
		// Value (columnIndex) and return the desired Key (columnName)
		Set<Entry<String, Integer>> names = columnIndexCache.entrySet();
		Iterator<Entry<String, Integer>> iter = names.iterator();

		while (iter.hasNext()) {
			Entry<String, Integer> entry = iter.next();
			if (entry.getValue() == columnIndex) {
				name = entry.getKey();
				break;
			}
		}

		name = name.equals(blank_header) ? "" : name;

		return name;
	}

	/**
	 * Stores the column index in the Hash Map for the column name
	 *
	 * @param columnName  to locate cell in row
	 * @param columnIndex index to associate the column name with
	 */
	protected void putColumnIndexInHashMap(String columnName, int columnIndex) {

		columnName = columnName.isEmpty() ? blank_header : columnName;
		// replace newline characters with a space
		columnName = columnName.replaceAll("[\\t\\n\\r]+", " ");
		columnIndexCache.put(columnName, columnIndex);

	}

	/**
	 * Looks up and returns the rowIndex for the specified rowID
	 *
	 * @param rowID identifies desired row
	 * @return int index of row matching the specified rowID
	 */
	protected int getRowIndexFromHashMap(RowIdentifier rowID) {
		String rowIndexHashMapKey = getHashMapKey(rowID);
		Integer index = rowIndexCache.get(rowIndexHashMapKey);

		if (index == null) {
			index = -1;
		}

		return index;
	}

	/**
	 * Adds the rowIndex key and value to the rowID
	 *
	 * @param rowID    identifies the row
	 * @param rowIndex index of row
	 */
	protected void putRowIndexInHashMap(RowIdentifier rowID, int rowIndex) {
		String rowIndexHashMapKey = getHashMapKey(rowID);

		rowIndexCache.put(rowIndexHashMapKey, rowIndex);

	}

	/**
	 * Returns the hashmap key for the Row Identifier
	 *
	 * @param rowID to generate hashmap for
	 * @return String hashmap key
	 */
	protected String getHashMapKey(RowIdentifier rowID) {
		CellIdentifier[] rowValues = rowID.getCellIdentifiers(); // rowID.getIdentifyingValues();
		String rowIndexHashMapKey = "";
		for (CellIdentifier value : rowValues) {
			rowIndexHashMapKey += ":" + value;
		}
		return rowIndexHashMapKey;
	}

	/**
	 * Resets all cached data for the table
	 */
	@Override
	protected void reset() {
		// I am not resetting the idColumns or columnIndexCache as their associated
		// values header -> column
		// index should not change, and this is not caching any actual object values
		rowIndexCache.clear();
		// Do not clear out the column Index cache as this should also not change
		// and if it is cleared, will lose any information stored with
		// setColumnHeaderCache
		// If a table does have table header/indexes that change, this method will need
		// to be customized
		// columnIndexCache.clear();
	}
}
