package platformIndependentCore.tables;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import platformIndependentCore.core.AutomatedObject;
import platformIndependentCore.core.AutomationHelper;
import platformIndependentCore.core.CriteriaObject.REGEX;
import platformIndependentCore.core.Search;
import platformIndependentCore.core.Search.CONDITION;
import platformIndependentCore.core.ToolManager;
import platformIndependentCore.exceptions.AmbiguousObjectException;
import platformIndependentCore.exceptions.InvalidDataException;
import platformIndependentCore.exceptions.ObjectNotFoundException;

/**
 * Contains common methods for interacting with all tables.
 *
 * @author VBAAUSTAYLOL
 */
public abstract class CustomTable extends ToolManager {
	/** Log instance for debug output */
	static Logger log = LogManager.getLogger(CustomTable.class.getName());
	/** Level for custom TableLogger */
	Level tableLevelLog = Level.forName("TableLogger", 250);

	/** Hold a reference to the table object this custom table manages */
	private TableObject table;
	/** List of classes that will need to be reset when this table is reset */
	private List<String> otherClassesToReset = new ArrayList<String>();
	/** a cache of the row objects within this table. */
	HashMap<String, AutomatedObject> rowCache = new HashMap<String, AutomatedObject>();
	/** a cache of the cell Indexes for their associated rows. */
	HashMap<String, ArrayList<AutomatedObject>> rowCellsCache = new HashMap<String, ArrayList<AutomatedObject>>();

	/**
	 * Constructor requires both the table object and the column headers that will
	 * be used for row identification
	 *
	 * To aid in customization, there is a storeColumnHeaders method that can be
	 * overridden if a table needs to calculate the column headers different from
	 * the default: <br>
	 * <b>storeColumnHeaders</b> - which will set up the column header/index
	 * mappings <br>
	 *
	 * Initialize is called first from inside of the TableObject class, then
	 * storeColumnHeaders is called from CustomTable to allow for customization <br>
	 *
	 *
	 * @param tableInstance      - An AutomatedObject representation of the table
	 * @param identifyingColumns - The column headers for values that will be used
	 *                           to identify rows
	 */
	protected CustomTable(AutomatedObject tableInstance, String... identifyingColumns) {
		// Set the storeColumnHeaders to false because we always want to do that after
		// initial table creation when working with CustomTables to allow extending
		// classes to customize storeColumnHeaders, and to prevent column headers from
		// being calculated twice
		table = getAutomationTool().getTableObject(tableInstance, identifyingColumns);
		storeColumnHeaders();
		table.storeIdColumnHeaders(identifyingColumns);
	}

	/**
	 * Method will click the cell matching the rowId and columnToClickHeader
	 *
	 * @param rowId               to identify desired row
	 * @param columnToClickHeader to click
	 */
	protected void clickCell(RowIdentifier rowId, String columnToClickHeader) {
		AutomatedObject cell = getCell(rowId, columnToClickHeader);
		cell.click();
	}

	@Override
	protected Search getSearch() {
		return getTableObject().getSearch();
	}

	/**
	 * Returns the AutomatedObject for this table
	 *
	 * @return AutomatedObject representation for the table
	 */
	protected AutomatedObject getAutomatedObject() {
		return getTableObject().getAutomatedObject();
	}

	/**
	 * Method will return the matching cell using the rowId to locate the row and
	 * the cellColumn to locate the desired column
	 *
	 * @param rowId      identifying criteria for the row
	 * @param cellColumn specifies desired column
	 * @return AutomatedObject cell
	 */
	protected AutomatedObject getCell(RowIdentifier rowId, String cellColumn) {
		List<AutomatedObject> cells = getCellsForRow(rowId);

		AutomatedObject cell = null;
		int columnIndex = getColumnIndex(cellColumn);
		if (columnIndex < cells.size() && columnIndex > -1) {
			// The columnIndex is within range of the number of columnHeaders
			cell = cells.get(columnIndex);

		} else {
			throw new InvalidDataException("Unable to locate column for " + cellColumn);
		}

		if (getSearch().isThrowObjectNotFound() && cell == null) {
			throw new ObjectNotFoundException(
					"Unable to locate cell using ROW_ID=" + rowId.toString() + ", and COLUMN=" + cellColumn);
		}
		return cell;
	}

	/**
	 * Will return a list of Cell objects for the given Row
	 *
	 * @param row object
	 *
	 * @return ArrayList{AutomatedObject} for cells
	 */
	protected ArrayList<AutomatedObject> getCellsForRow(AutomatedObject row) {
		// We don't have the actual rowId, but can create a String version as an id
		String rowStringAsId = row.toString();
		ArrayList<AutomatedObject> cells = rowCellsCache.get(rowStringAsId);
		if (cells == null || cells.isEmpty()) {
			Search cellSearch = getSearch();
			cellSearch.addCriteria("tag", "td");
			cellSearch.setParent(row);
			cellSearch.setThrowObjectNotFound(false);

			// System.out.println("===================== GET CELLS FOR ROW
			// =================================");
			// getAutomationTool().printObjectsForSearch(cellSearch, true);
			// System.out.println("===================== END GET CELLS FOR ROW
			// =================================");

			cells = getObjects(cellSearch);
			rowCellsCache.put(rowStringAsId, cells);
		}
		return cells;
	}

	/**
	 * Will return a list of Cell objects for the given Row
	 *
	 * @param rowId to Identify row
	 * @return ArrayList{AutomatedObject} for cells
	 */
	protected ArrayList<AutomatedObject> getCellsForRow(RowIdentifier rowId) {
		ArrayList<AutomatedObject> cells = rowCellsCache.get(rowId.toString());
		if (cells == null || cells.isEmpty()) {
			cells = getCellsForRow(getRow(rowId));
			rowCellsCache.put(rowId.toString(), cells);
		}
		return cells;
	}

	/**
	 * Will return the column index for the matching column
	 *
	 * @param columnName to get index of
	 * @return int index
	 */
	protected int getColumnIndex(String columnName) {
		// check and see if a reset has cleared the header cache, if so
		// need to re-populate it from CustomTable to pick up any customizations
		if (getColumnHeaderCache().isEmpty()) {
			storeColumnHeaders();
		}
		return getTableObject().getColumnIndex(columnName);
	}

	/**
	 * Returns the IDENTIFYING_COLUMNS for this table
	 *
	 * @return String[] columns used to identify matching rows in the table
	 */
	private CellIdentifier[] getIdentifyingColumns() {
		return getTableObject().getIdentifyingColumns();
	}

	/**
	 * Will return a String representation to identify the specified row. <br>
	 * If there are ID columns created for the table, return just those cell values,
	 * if there are not ID columns specified for the table, return all the cell
	 * values
	 *
	 * @param row to get a String id for
	 * @return String with the row's cell values to identify it
	 */
	protected String getStringIdForRow(AutomatedObject row) {
		List<AutomatedObject> cellsInRow = getCellsForRow(row);
		CellIdentifier[] headerIdCells = getIdentifyingColumns();
		String id = "";
		if (headerIdCells.length < 1) {
			// If there are no IDs columns for this table, go ahead and return all the cell
			// values for the row
			Map<String, Integer> headers = getTableObject().getColumnIndexCache();
			Set<String> keySet = headers.keySet();
			for (String key : keySet) {
				if (!id.isEmpty()) {
					id += ", ";
				}
				id += "[" + key + "]=" + cellsInRow.get(headers.get(key)).readText();
			}
		} else {
			// If there are ID columns for the table, return just the cell values that
			// correspond with those columns
			for (CellIdentifier c : headerIdCells) {
				String header = c.getCellValue();
				int headerIndex = c.getColumnIndex();
				// make sure the index does not go outside the range of the cellsInRow
				if (headerIndex < cellsInRow.size()) {
					if (!id.isEmpty()) {
						id += ", ";
					}
					id += "[" + header + "]=" + cellsInRow.get(headerIndex).readText();
				}
			}
		}
		return id;
	}

	/**
	 * Will return a list of TR row objects for the table
	 *
	 * @return ArrayList{AutomatedObject} of rows
	 */
	protected ArrayList<AutomatedObject> getRows() {

		Search rowSearch = getTableObject().getSearch();
		rowSearch.addCriteria("tag", "tr");
		rowSearch.setParent(getTableObject().getAutomatedObject());
		rowSearch.setThrowObjectNotFound(false);

		return getObjects(rowSearch);
	}

	/**
	 * Will return a list of TR row objects for the table <br>
	 * Will only return direct children TR objects of the table unless this table
	 * has a TBODY object, then it returns direct children of the TBODY
	 *
	 * @return ArrayList{AutomatedObject} of rows
	 */
	protected ArrayList<AutomatedObject> getDataRows() {
		// default the parent to the table
		AutomatedObject parent = getTableObject().getAutomatedObject();
		// check to see if there is a tbody
		Search tbodySearch = getSearch();
		tbodySearch.addCriteria("tag", "tbody");
		if (isObjectPresent(tbodySearch)) {
			parent = getObject(tbodySearch);
		}

		Search rowSearch = getSearch();
		rowSearch.addCriteria("tag", "tr");
		rowSearch.setDirectChild(true);
		rowSearch.setParent(parent);
		rowSearch.setThrowObjectNotFound(false);
		return getObjects(rowSearch);
	}

	/**
	 * The search criteria used to find a cell within the desired row to help
	 * identify matching rows. This will be the first search to locate potential
	 * matches, additional verification will be done for all cell/column values
	 * before a match is made. Developers may override this if customization is
	 * necessary
	 *
	 * @param defaultSearch to specify basic search settings (such as
	 *                      ThrowsExceptions)
	 * @param text          to match in the cell
	 * @return Search object with specified criteria
	 */
	protected Search getCellSearch(Search defaultSearch, String text) {
		// create a search based on the default. Will copy settings like
		// window/url/iFrame criteria
		Search cellSearch = defaultSearch;
		// Add all of the Criteria for the Search using setCellSearchCriteria
		cellSearch = setCellSearchCriteria(cellSearch, text);
		cellSearch.setThrowObjectNotFound(false);

		return cellSearch;
	}

	/**
	 * Will return a RowIdentifier matching specified values
	 *
	 * @param identifyingCellValues contains a list of identifying cell values to
	 *                              locate the row
	 * @return RowIdentifier rowId contains all the criteria to find the row
	 */
	protected RowIdentifier getRowIdentifier(String... identifyingCellValues) {
		return getTableObject().getRowIdentifier(identifyingCellValues);
	}

	/**
	 * Returns the row with the matching id. Will use default search settings
	 *
	 * @param rowId criteria
	 * @return AutomatedObject row matching criteria
	 */
	protected AutomatedObject getRow(RowIdentifier rowId) {
		return getRow(rowId, getSearch());
	}

	/**
	 * Method will return the row matching the row id. If paging is implemented, it
	 * will search all pages of the table
	 *
	 * @param rowId  to identify row
	 * @param search default search criteria
	 * @return AutomatedObject matching row on the current page
	 */
	protected final AutomatedObject getRow(RowIdentifier rowId, Search search) {
		// Since objectNotFound can be a valid state if this is a paging table,
		// we need to set ThrowObjectNotFound to false in order to perform our
		// search. After searching, examine this original value of
		// ThrowObjectNotFound and throw an error if no row is found and
		// not a paging table
		boolean isThrowObjectNotFound = search.isThrowObjectNotFound();
		// for paging, do not want exceptions thrown.
		search.setThrowObjectNotFound(false);
		String id = rowId.toString();
		AutomatedObject row = null;

		if (rowCache.containsKey(id)) {
			row = rowCache.get(id);
		} else {
			/**
			 * Get Row IDs and Cell IDs for text
			 *
			 * The RowIdentifier contains a set of CellIdentifiers that are required to
			 * locate a matching row. These CellIdentifiers contain the logic for matching
			 * cell text and column position in the row
			 */
			CellIdentifier[] cellIds = rowId.getCellIdentifiers();
			log.log(tableLevelLog, "RowIdentifier cellIds=" + Arrays.deepToString(cellIds));

			String text = "";
			if (cellIds != null && cellIds.length > 0) {
				text = cellIds[0].getCellValue();
			}

			// Set cell search criteria once for reuse
			search = getCellSearch(search, text);

			/**
			 * Ensure the search begins on the First/Only page. This is protecting from the
			 * possibility the script manually navigates to the last page and bypasses the
			 * check, or misses the page containing the row sought.
			 *
			 * Note: Row Cache prevents this Else branch and associated navigation from
			 * happening multiple times with multiple reads from a previously located row.
			 */
			goToFirstPageOfResults();

			/**
			 * Search the current page. Could be the First/Only page.
			 */
			row = getRowOnCurrentPage(rowId, search);
		}

		/**
		 * Check current page and row status. Search multiple pages if needed.
		 *
		 * Note: The isOnLastPage check should prevent non-paging tables from entering
		 * the loop as the default value returned is True, and the method is overridden
		 * for paging tables as required.
		 */
		while (row == null && !isOnLastPage()) {
			if (goToNextPageOfResults()) {
				row = getRowOnCurrentPage(rowId, search);
			}
		}

		if (row == null && isThrowObjectNotFound) {
			throw new ObjectNotFoundException("Unable to locate row matching: " + rowId.toString());
		}
		rowCache.put(rowId.toString(), row);
		return row;
	}

	/**
	 * There are two methods for locating a row on the current page, which one you
	 * use is dependent on how you application behaves. We are defaulting to the
	 * getRowOnCurrentPageByMatchingCell which is good for tables where you can
	 * locate by matching text in a cell. If you find there are too many cell
	 * matches, or you can not read the text in a cell, you override this method to
	 * call the getRowOnCurrentPageByMatchingRow which will locate the row by
	 * matching text on the row first. Both methods are provided in the Core code,
	 * but only one should be used. You can also override these methods if needed
	 * within your table code, just make sure your project calls the desire
	 * implementation from this method
	 *
	 * @param rowId  to identify row
	 * @param search default search criteria
	 * @return AutomatedObject matching row on the current page
	 */
	protected AutomatedObject getRowOnCurrentPage(RowIdentifier rowId, Search search) {
		return getRowOnCurrentPageByMatchingCell(rowId, search);
		// If matching by row, override this method and use the line below
		// return getRowOnCurrentPageByMatchingRow(rowId, search);
	}

	/**
	 * This method is to be used when the table will match rows by first matching
	 * text on the row (instead of a cell object) This is an alternative for tables
	 * that have performance issues or trouble reading text from the cells for
	 * whatever reason. Matching on the cell was the original implementation, so is
	 * staying as the default unless later testing on all existing projects prove
	 * this is a better implementation for all cases, not just some. This method can
	 * also be customized for individual projects if needed
	 *
	 * @param rowId  to identify row
	 * @param search default search criteria
	 * @return AutomatedObject matching row
	 */
	protected AutomatedObject getRowOnCurrentPageByMatchingRow(RowIdentifier rowId, Search search) {
		AutomatedObject row = null;
		boolean isThrowObjectNotFound = search.isThrowObjectNotFound();
		// for paging, do not want exceptions thrown.
		search.setThrowObjectNotFound(false);

		// The RowIdentifier contains a set of CellIdentifiers that are
		// required to locate a matching row. These CellIdentifiers
		// contain the logic for matching cell text and column position in the row
		CellIdentifier[] cellIds = rowId.getCellIdentifiers();

		if (cellIds != null && cellIds.length > 0) {
			String firstText = cellIds[0].getCellValue();

			ArrayList<AutomatedObject> rows = getRowsContainingText(search, firstText);
			if (rows.size() > 0) {
				for (AutomatedObject currentRow : rows) {
					if (isRowMatch(cellIds, currentRow)) {
						if (row == null) {
							row = currentRow;
						} else if (search.isThrowAmbiguousObject()) {
							throw new AmbiguousObjectException("More than one row matched criteria: " + rowId);
						} else {
							// go ahead and break to return the matching row if they are not worried about
							// Ambiguous Object, no need to keep searching
							break;
						}
					}
				}
			}
		}
		// If row is null and we are configured to throw exceptions, throw
		// ObjectNotFound
		if (row == null && isThrowObjectNotFound) {
			throw new ObjectNotFoundException("Unable to locate row matching: " + rowId.toString());
		}
		return log.traceExit(row);
	}

	/**
	 * This method will match rows by first matching text in a cell object. This is
	 * current default for tables. If you experience performance issues or your
	 * table has trouble locating cells by text value, you can try using
	 * getRowOnCurrentPageByMatchingRow. This method can also be customized for
	 * individual projects if needed
	 *
	 * @param rowId  to identify row
	 * @param search default search criteria
	 * @return AutomatedObject matching row
	 */
	protected AutomatedObject getRowOnCurrentPageByMatchingCell(RowIdentifier rowId, Search search) {
		AutomatedObject row = null;
		boolean isThrowObjectNotFound = search.isThrowObjectNotFound();
		// for paging, do not want exceptions thrown.
		search.setThrowObjectNotFound(false);

		/**
		 * Get Row IDs and Cell IDs for text
		 *
		 * The RowIdentifier contains a set of CellIdentifiers that are required to
		 * locate a matching row. These CellIdentifiers contain the logic for matching
		 * cell text and column position in the row
		 */
		CellIdentifier[] cellIds = rowId.getCellIdentifiers();
		log.log(tableLevelLog, "RowIdentifier cellIds=" + Arrays.deepToString(cellIds));

		if (cellIds != null && cellIds.length > 0) {

			// Using the cell Search object, get all of the cells
			ArrayList<AutomatedObject> cells = getMatchingCells(search);
			if (cells.size() > 0) {
				for (AutomatedObject cell : cells) {
					// Call method to return the row this cell resides in
					AutomatedObject currRow = getRowFromCellMatch(cell);
					// if match is still equal to true, then the current row met all cell criteria
					// and we found a match
					if (isRowMatch(cellIds, currRow)) {
						if (row == null) {
							row = currRow;
						} else if (search.isThrowAmbiguousObject()) {
							// Need to check if this is the same row as we already found (just a different
							// cell), if so, don't worry about it, we can keep looping.
							// I suppose if there are two completely identical rows
							// this could suppress an Ambiguous object, but I think the risk is small
							if (!currRow.toString().equals(row.toString())) {
								// This matching cell was in a row that did not match the row already found
								// Need to throw an AmbiguousObjectException
								throw new AmbiguousObjectException("More than one row matched criteria: " + rowId);
							}
						} else {
							// go ahead and break to return the matching row if they are not worried about
							// Ambiguous Object, no need to keep searching
							break;
						}
					}
				}
			}

		}

		// If row is null and we are configured to throw exceptions, throw
		// ObjectNotFound
		if (row == null && isThrowObjectNotFound) {
			throw new ObjectNotFoundException("Unable to locate row matching: " + rowId.toString());
		}
		return log.traceExit(row);
	}

	/**
	 * Will return a row object given the AutomatedObject found in the cell match
	 * search. By default, the cellMatch would typically return a TD object, but for
	 * some tables, a different object may be found so calculating the row from the
	 * non-TD object would need to be customized
	 *
	 * @param cellMatch within the row
	 * @return AutomatedObject row
	 */
	protected AutomatedObject getRowFromCellMatch(AutomatedObject cellMatch) {
		AutomatedObject row = cellMatch.getParent();
		while (!row.getPropertyValue("tag").equalsIgnoreCase("tr")) {
			row = row.getParent();
		}

		return row;
	}

	/**
	 * Will return the cell objects that match the text criteria
	 *
	 * @param defaultSearch search criteria for locating cells with matching text
	 * @return ArrayList{AutomatedObject} matching cells
	 */
	protected ArrayList<AutomatedObject> getMatchingCells(Search defaultSearch) {
		return getObjects(defaultSearch);
	}

	/**
	 * Will return a search to locate the rows that contain the provided text.
	 *
	 * @param text          to locate rows
	 * @param defaultSearch default Search
	 * @return Search to locate rows
	 */
	protected Search getMatchingTableRowSearch(String text, Search defaultSearch) {
		Search trSearch = new Search(defaultSearch);
		// Set the parent to table
		trSearch.setParent(getAutomatedObject());
		trSearch.addCriteria("tag", "tr");
		trSearch.addCriteria("text", text, REGEX.CONTAINS);
		return trSearch;
	}

	/**
	 * Will return all rows that contain the provided text
	 *
	 * @param defaultSearch to locate row objects
	 * @param text          to use to locate rows
	 * @return ArrayList{AutomatedObject} matching rows
	 */
	private ArrayList<AutomatedObject> getRowsContainingText(Search defaultSearch, String text) {
		ArrayList<AutomatedObject> matchingRows = new ArrayList<AutomatedObject>();
		ArrayList<AutomatedObject> rows = getObjects(getMatchingTableRowSearch(text, defaultSearch));
		for (AutomatedObject row : rows) {
			if (row.readText().contains(text)) {
				matchingRows.add(row);
			}
		}
		return matchingRows;
	}

	/**
	 * Method will verify if a row exists in the table that meets all criteria in
	 * the provided RowIdentifier
	 *
	 * @param rowId criteria to locate row
	 * @return boolean TRUE if found, FALSE if not
	 */
	protected boolean isRowPresent(RowIdentifier rowId) {
		// Attempt a reference to the row
		Search search = getSearch();
		search.setThrowObjectNotFound(false);
		search.setThrowAmbiguousObject(false);
		search.setWaitTimeInSeconds(1);
		AutomatedObject row = getRow(rowId, search);

		return row == null ? false : true;
	}

	/**
	 * Will return the Search criteria to find the First button
	 *
	 * This should be implemented in the extending Base Table class (returning a
	 * default value of null) to properly handle non-paging tables, and uniquely
	 * implemented in extending paging tables to return a Search object capable of
	 * locating the First button for the paging table.
	 *
	 * @return Search to locate the First button
	 */
	protected abstract Search getFirstButtonSearch();

	/**
	 * Will return the First Button if a button exists and is locatable. Otherwise
	 * it will return null.
	 *
	 * @return AutomatedObject first button
	 */
	protected AutomatedObject getFirstButton() {
		Search firstSearch = getFirstButtonSearch();
		AutomatedObject firstButton = null;

		if (firstSearch != null) {
			if (isObjectPresent(firstSearch)) {
				firstButton = getObject(firstSearch);
			}
		}

		return firstButton;
	}

	/**
	 * Will move the table to the first page of results. Implementation can be
	 * overridden in the application page class code as finding the first page of
	 * results can be application specific.
	 *
	 * In order to use this method in the core, the concrete class must implement
	 * getFirstButtonSearch. To use this method in the concrete class it must be
	 * overridden directly.
	 *
	 * @return boolean TRUE if the button was clicked, FALSE if not
	 */
	protected boolean goToFirstPageOfResults() {
		return clickNavigationButton(getFirstButton());
	}

	/**
	 * Will return the Search criteria to find the Next button
	 *
	 * This should be implemented in the extending Base Table class (returning a
	 * default value of null) to properly handle non-paging tables, and uniquely
	 * implemented in extending paging tables to return a Search object capable of
	 * locating the Next button for the paging table.
	 *
	 * @return Search to locate the Next button
	 */
	protected abstract Search getNextButtonSearch();

	/**
	 * Will return the Next Button if a button exists and is locatable. Otherwise it
	 * will return null.
	 *
	 * @return AutomatedObject next button
	 */
	protected AutomatedObject getNextButton() {
		Search nextSearch = getNextButtonSearch();
		AutomatedObject nextButton = null;

		if (nextSearch != null) {
			if (isObjectPresent(nextSearch)) {
				nextButton = getObject(nextSearch);
			}
		}

		return nextButton;
	}

	/**
	 * Will move the table to the next page of results. Implementation can be
	 * overridden in the application page class code as finding the next page of
	 * results can be application specific
	 *
	 * In order to use this method in the core, the concrete class must implement
	 * getNextButtonSearch. To use this method in the concrete class it must be
	 * overridden directly.
	 *
	 * @return boolean TRUE if the button was clicked, FALSE if not
	 */
	protected boolean goToNextPageOfResults() {
		return clickNavigationButton(getNextButton());
	}

	/**
	 * Will check if the provided button is not null and enabled. If so, it will
	 * click the button and reset the table
	 *
	 * @param button to click for navigation
	 * @return boolean TRUE if the button was clicked, FALSE if not
	 */
	protected boolean clickNavigationButton(AutomatedObject button) {
		boolean navigationClicked = false;
		if (button != null && button.isEnabled()) {
			button.click();
			// change the page, wait and clear the rowCache
			waitForPageLoad();
			navigationClicked = true;
		}
		return navigationClicked;
	}

	/**
	 * Returns indicator of being on the last page of the table. True if on the last
	 * page, False if not on the last page.
	 *
	 * Some paging tables may contain numeric page links, others may just have
	 * previous and next buttons, and still more may contain both. Buttons and links
	 * may or may not be enabled/disabled based on current page or status. Sometimes
	 * the only indicators may be attribute values a page or button contains.
	 *
	 * This should be implemented in the extending Base Table class (returning a
	 * default value of True) and uniquely implemented in extending paging tables to
	 * return a proper boolean value upon determining whether it is on the last
	 * page.
	 *
	 * @return boolean indicator of being on the last page of the table. True if on
	 *         the last page; False otherwise.
	 */
	protected abstract boolean isOnLastPage();

	/**
	 * Method will use the provided cell identifiers to determine if the row matches
	 * the criteria
	 *
	 * @param cellIds cell identifiers to match on row
	 * @param row     row instance to check
	 * @return boolean TRUE if row matches the cell identifiers, FALSE if not
	 */
	protected boolean isRowMatch(CellIdentifier[] cellIds, AutomatedObject row) {
		boolean match = true;
		// Now get all the cells for the row
		ArrayList<AutomatedObject> rowCells = getCellsForRow(row);

		// Loop through all the CellIdentifiers required for a row match
		// and for each, verify current value with required value
		// if they are not the same, the row is not a match
		for (CellIdentifier cellId : cellIds) {
			// Get the required text for the cell
			String cellIdText = cellId.getCellValue();
			// Get the required column index (position in the row) for the cell
			int cellIdIndex = cellId.getColumnIndex();
			log.log(tableLevelLog, "Locate cell for TEXT=" + cellIdText + ", INDEX=" + cellIdIndex);
			if (rowCells.size() <= cellIdIndex) {
				throw new InvalidDataException("Unable to located cell for index=" + cellIdIndex + ".");
			}
			// Read the actual cell text for the cell at the required column index
			// TODO: NWG #179 - The value of the cell we are trying to check for match
			// is being trimmed here, but the cell value we are trying to match
			// against is not trimmed when being put into a CellIdentifier. What
			// testing do we need to do to verify this isn't going to break
			// any existing tables (particularly ch33)? I Suppose the trim
			// should be added to setValue method of CellIdentifier?
			String rowCellText = readCellText(rowCells.get(cellIdIndex)).trim();
			// If the actual text of the cell does not match the required text, then the row
			// does not match the criteria
			if (!rowCellText.equals(cellIdText)) {
				match = false;
				break;
			}

		}
		return match;
	}

	/**
	 * Returns a reference to the actual table object
	 *
	 * @return TableObject a reference to the actual table object
	 */
	TableObject getTableObject() {
		return table;
	}

	/**
	 * Will read the text value of the cell.
	 *
	 * Developers may override this if customization is necessary
	 *
	 * @param cell object to read value of
	 * @return String text value of cell
	 */
	protected String readCellText(AutomatedObject cell) {
		String text = cell.readText();
		if (text == null || text.isEmpty()) {
			// check to see if there are children objects in the cell
			AutomatedObject[] children = cell.getChildren();

			for (AutomatedObject child : children) {
				// getAutomationTool().printObjectProperties(child, false, false);
				text = child.readText();
				if (!text.isEmpty()) {
					break;
				}
			}
		}
		return text;
	}

	/**
	 * Will read the cell value from a table row.
	 *
	 * @param identifyingValue   cell value in the primary index column used to
	 *                           identify row
	 * @param columnToReadHeader desired column to read value from
	 * @return String value identified by the identifying value and column header
	 */
	protected String readValue(String identifyingValue, String columnToReadHeader) {
		RowIdentifier rowId = getRowIdentifier(identifyingValue);
		return readValue(rowId, columnToReadHeader);
	}

	/**
	 * Will read the cell value from a table row.
	 *
	 * @param rowId              object which holds all cell values required to
	 *                           identify row
	 * @param columnToReadHeader desired column to read value from
	 * @return String value identified by the row ID and column header
	 */
	protected String readValue(RowIdentifier rowId, String columnToReadHeader) {
		String value = "";
		AutomatedObject cell = getCell(rowId, columnToReadHeader);
		if (cell != null) {
			value = readCellText(cell);
		}
		return value;
	}

	/**
	 * Resets all cached data for the table
	 */
	@Override
	protected void reset() {
		// Reset everything stored in ToolManager
		super.reset();
		// Reset otherClassesToReset if needed.
		/**
		 * Must create File objects to indicate what classes need resetting, as the
		 * classes needing resetting will be out of scope
		 */
		if (!otherClassesToReset.isEmpty()) {
			createResetFiles(otherClassesToReset.toArray(new String[otherClassesToReset.size()]));
		}
		// Reset the TableObject associated with this Custom Table
		getTableObject().reset();
		rowCache.clear();
		rowCellsCache.clear();
	}

	/**
	 * Will use the addCriteria method to add required search criteria to the Search
	 * object. The search criteria used to find a cell within the desired row to
	 * help identify matching rows. This will be the first search to locate
	 * potential matches, additional verification will be done for all cell/column
	 * values before a match is made.
	 *
	 * Developers may override this method to customize the Search
	 *
	 * @param cellSearch search object to update
	 * @param text       cell text criteria
	 * @return Search search object with updated Criteria
	 */
	protected Search setCellSearchCriteria(Search cellSearch, String text) {

//		ArrayList<CriteriaObject> searchCriteria = getSearchCriteriaArray(cellSearch);
//		boolean hasCellCriteria = false;
//
//		/**
//		 * TODO: Update to check and make sure a criteria is added if somehow only one
//		 * was specified
//		 */
//		// Check existing criteria to see if cell search criteria already exists
//		if (!searchCriteria.isEmpty()) {
//			for (CriteriaObject crit : searchCriteria) {
//				if (crit.getCriteriaType().equalsIgnoreCase("tag")
//						&& crit.getCriteriaValue().toString().equalsIgnoreCase("td")
//						|| crit.getCriteriaType().equalsIgnoreCase("text")
//								&& crit.getCriteriaValue().toString().equalsIgnoreCase(text)) {
//					hasCellCriteria = true;
//				}
//			}
//		}
//
//		// If cell search criteria wasn't found, add it here
//		if (!hasCellCriteria) {
		cellSearch.addCriteria("tag", "td");
		cellSearch.addCriteria("text", text);
//		}

		return cellSearch;
	}

	/**
	 * Allows the user to set default search information the table may need to know
	 * such as window title, url, and iframe, so that it may be used when locating
	 * objects such as cells or rows within the table.
	 *
	 * @param search the search object with which to set default values
	 */
	protected void setDefaultSearch(Search search) {
		getTableObject().setDefaultSearch(search);
	}

	/**
	 * Will set the check box for the cell specified by the rowID and columnHeader
	 * to the desired status
	 *
	 * @param rowId                specifies row
	 * @param checkBoxColumnHeader column header containing check box
	 * @param checked              true for CHECKED, false for UNCHECKED
	 */
	protected void setCheckBoxInRow(RowIdentifier rowId, String checkBoxColumnHeader, boolean checked) {
		AutomatedObject cbCell = getCell(rowId, checkBoxColumnHeader);
		AutomatedObject cb = getCheckBoxInCell(cbCell);
		setCheckBoxInCell(cb, checked);
	}

	/**
	 * Will set the check box object that resides in the cell to the desired status
	 *
	 * @param checkBox that is inside of the cell
	 * @param checked  true for CHECKED, false for UNCHECKED
	 */
	protected void setCheckBoxInCell(AutomatedObject checkBox, boolean checked) {
		checkBox.setValue(checked);
	}

	/**
	 * Will return the Check box object that has the provided cell as a parent.
	 *
	 * Developers can override if necessary
	 *
	 * @param checkBoxCell parent of check box
	 * @return AutomatedObject check box
	 */
	protected AutomatedObject getCheckBoxInCell(AutomatedObject checkBoxCell) {
		Search cbSearch = getSearch();
		AutomatedObject cb = getCheckBoxInCell(checkBoxCell, cbSearch);

		// Always throw error if object does not exist to avoid unhandled null objects
		if (cb == null) {
			throw new ObjectNotFoundException("Checkbox not found");
		}
		return cb;
	}

	/**
	 * Will return the Check box object that has the provided cell as a parent. If
	 * no object is found, will return a null object.
	 *
	 * @param checkBoxCell parent of check box
	 * @param cbSearch     Search object to determine if exceptions are thrown
	 * @return AutomatedObject check box; can be null
	 */
	private AutomatedObject getCheckBoxInCell(AutomatedObject checkBoxCell, Search cbSearch) {
		Search checkBoxSearch = new Search(cbSearch);
		checkBoxSearch.setParent(checkBoxCell);
		checkBoxSearch.addCriteria("type", "checkbox");
		// RSD - Don't throw exception in order to perform 2nd search
		checkBoxSearch.setThrowObjectNotFound(false);
		AutomatedObject cb = getObject(checkBoxSearch);

		if (cb == null) {
			Search inputSearch = new Search(cbSearch);
			inputSearch.setParent(checkBoxCell);
			inputSearch.addCriteria("tag", "input");
			inputSearch.setCondition(CONDITION.VISIBLE);
			inputSearch.setThrowObjectNotFound(false);
			cb = getObject(inputSearch);
		}

		// Can return null
		return cb;
	}

	/**
	 * If a table requires customization to how columns are identified and indexed,
	 * your descending class can create a hashmap with the appropriate values and
	 * pass it back to the super class.
	 *
	 * @param columns hashmap correlating header values with indexes
	 */
	protected void setColumnHeaderCache(Map<String, Integer> columns) {
		log.traceEntry("TableLogger");
		columns.entrySet().forEach(entry -> {
			log.log(tableLevelLog, "setColumnHeaderCache: " + entry.getKey() + "=" + entry.getValue());
		});

		getTableObject().setColumnHeaderCache(columns);

		// check if the id columns need to be re-indexed
		CellIdentifier[] cellIds = getTableObject().getIdentifyingColumns();
		String[] ids = new String[cellIds.length];
		for (int i = 0; i < cellIds.length; i++) {
			ids[i] = cellIds[i].getCellValue();
		}
		getTableObject().storeIdColumnHeaders(ids);
	}

	/**
	 * Method to allow a descendant class to get the current column header cache
	 * incase there is a need to modify (and reset)
	 *
	 * @return {@code Map<String, Integer>} column header cache
	 */
	protected Map<String, Integer> getColumnHeaderCache() {
		return getTableObject().getColumnIndexCache();
	}

	/**
	 * Sets the text field in the specified column on the specified row to the
	 * passed in value
	 *
	 * @param rowId      RowIdentifier object to identify the row
	 * @param columnName name of column you wish to set
	 * @param valueToSet value to set in the text field
	 */
	protected void setTextFieldInRow(RowIdentifier rowId, String columnName, String valueToSet) {
		AutomatedObject cell = getCell(rowId, columnName);

		Search search = getSearch();
		search.addCriteria("tag", "input");
		search.setParent(cell);
		AutomatedObject textField = getObject(search);

		textField.setValue(valueToSet);
		// Clear out the caches since a value has changed in the table
		reset();
	}

	/**
	 * Custom generates the header cache for tables that have headers with hidden
	 * text.
	 */
	protected void storeColumnHeaders() {
		storeColumnHeaders(getAutomatedObject());
	}

	/**
	 * Custom generates the header cache for tables that have headers with hidden
	 * text.
	 *
	 * @param parent that contains the header cells (can be a table or row)
	 */
	protected void storeColumnHeaders(AutomatedObject parent) {
		HashMap<String, Integer> columnIndexCache = new HashMap<String, Integer>();
		// Some column headers
		String expression = "[a-zA-Z0-9_/].*";
		Search search = getSearch();
		search.setParent(parent);
		search.addCriteria("tag", "TH");
		int colIndex = 0;
		List<AutomatedObject> ths = getObjects(search);
		for (AutomatedObject th : ths) {
			String header = getInnerText(th, expression);
			columnIndexCache.put(header, colIndex);
			colIndex++;
		}

		setColumnHeaderCache(columnIndexCache);
	}

	/**
	 * Method that will generate and set column headers given two header rows <br>
	 * This method is useful for tables that have multiple header rows, where the
	 * top row has cells that span multiple columns.<br>
	 * The resulting header column labels will be a concatenation of the value of
	 * the top header cell with the value of the bottom header row cell value that
	 * is directly below <br>
	 * So a column header that spans 2 rows will be concatenated with two value
	 * below it. <br>
	 * This method should be used by overriding the storeColumnHeaders() <br>
	 * NOTE: This table only works where the bottom row has a single cell that
	 * correlates to the data cells
	 *
	 * @param header parent object that contains the header rows
	 */
	protected void storeColumnHeadersFromTwoRows(AutomatedObject header) {
		// TODO: Would be nice to have an example of the header hierarchy in the javadoc
		Search trSearch = getSearch();
		trSearch.setParent(header);
		trSearch.addCriteria("tag", "tr");
		List<AutomatedObject> headerRows = getObjects(trSearch);
		if (headerRows.size() < 2) {
			throw new ObjectNotFoundException(
					"Unable to locate both header rows for the Custom Table: " + this.getClass().getSimpleName());
		}
		storeColumnHeadersFromTwoRows(headerRows.get(0), headerRows.get(1));
	}

	/**
	 * Method that will generate and set column headers given two header rows <br>
	 * This method is useful for tables that have multiple header rows, where the
	 * top row has cells that span multiple columns.<br>
	 * The resulting header column labels will be a concatenation of the value of
	 * the top header cell with the value of the bottom header row cell value that
	 * is directly below <br>
	 * So a column header that spans 2 rows will be concatenated with two value
	 * below it. <br>
	 * This method should be used by overriding the storeColumnHeaders() method to
	 * locate your header rows, and then call this method. NOTE: This table only
	 * works where the bottom row has a single cell that correlates to the data
	 * cells
	 *
	 * @param headerRow1 the top row of the header
	 * @param headerRow2 the second row of the header
	 */
	protected void storeColumnHeadersFromTwoRows(AutomatedObject headerRow1, AutomatedObject headerRow2) {
		// map for the column headers
		HashMap<String, Integer> columnIndexCache = new HashMap<String, Integer>();
		// Search to locate cells in the header row
		Search thSearch = getSearch();
		thSearch.addCriteria("tag", "th");
		thSearch.setParent(headerRow1);
		// find all the cells in the first row
		ArrayList<AutomatedObject> row1Cells = getObjects(thSearch);
		// reset the parent on the search for the second header row
		thSearch.setParent(headerRow2);
		// find all the cells in the second header row
		ArrayList<AutomatedObject> row2Cells = getObjects(thSearch);

		int totalDataIndex = 0;
		String currTopHeader = "";
		// loop through the top row
		for (AutomatedObject topCell : row1Cells) {
			// pull out the text for the current top header row cell
			currTopHeader = topCell.readText();
			// find the colspan value for this cell
			String colspanText = topCell.getPropertyValue("colspan");
			// if the colspan is empty or not found, then default the colspan to 1
			if (colspanText == null || colspanText.isEmpty() || colspanText.contains("cannot be found")) {
				colspanText = "1";
			}
			// convert the text to an integer value
			int colspan = Integer.valueOf(colspanText);
			// for each column this cell spans, concatenate the cell text from the second
			// header row
			for (int i = 0; i < colspan; i++) {
				// calculate the currDataIndex by incrementing it by the current position in
				// this colspan
				int currDataIndex = totalDataIndex + i;
				// combine the text from the top header with the cell from the second header
				String header = currTopHeader + " " + row2Cells.get(currDataIndex).readText();

				// store the value with the current index (trim the header to remove leading or
				// trailing whitespace)
				columnIndexCache.put(header.trim(), currDataIndex);
			}
			// add the columns for colspan to the totalDataIndex
			totalDataIndex = totalDataIndex + colspan;

		}
		// Set the column header cache to the generated column headers
		setColumnHeaderCache(columnIndexCache);
	}

	/**
	 * Provides Custom table code with easy access to the waitForPageLoad method
	 */
	protected void waitForPageLoad() {
		reset();
		getAutomationTool().waitForPageLoad();
	}

	/**
	 * Will allow a CustomTable instance to call this (typically in the constructor)
	 * and specify which classes may also need a reset() call when a reset() call is
	 * made from the table <br>
	 *
	 * @param classesToBeResetWithTableReset a list of classes that will need to
	 *                                       reset when this table is reset
	 */
	protected void configureOtherClassesToReset(String... classesToBeResetWithTableReset) {
		otherClassesToReset.addAll(Arrays.asList(classesToBeResetWithTableReset));
	}

	/**
	 * Will create a temporary file for each of the pages that need to be reset.
	 * When a page issues a get call to find an object, it will first look for a
	 * reset file for the page, and if one is found, a reset will be issued before
	 * executing the get
	 *
	 * @param classesToReset list of classes that need to be reset()
	 */
	private void createResetFiles(String... classesToReset) {
		for (String classToReset : classesToReset) {
			try {
				String resetFileName = AutomationHelper.getResetFilePath(classToReset);
				File resetFile = new File(resetFileName);

				// check if reset file exists for this class
				if (!resetFile.isFile()) {
					resetFile.createNewFile();
				}
			} catch (IOException e) {
				// suppress, but allow information to be written to the console
				e.printStackTrace();
			}
		}
	}

}
