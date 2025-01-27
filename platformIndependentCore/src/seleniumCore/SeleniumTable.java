package seleniumCore;

import platformIndependentCore.core.AutomatedObject;
import platformIndependentCore.core.Search;
import platformIndependentCore.exceptions.MissingAutomationToolLibrariesException;
import platformIndependentCore.tables.RowIdentifier;
import platformIndependentCore.tables.TableObject;

public class SeleniumTable extends TableObject {

	public SeleniumTable(AutomatedObject object, String... cols) {
		super(object, cols);
	}

	@Override
	protected String readValue(AutomatedObject cell) {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}

	@Override
	public AutomatedObject getCell(String columHeaderToGet, Search searchConfig) {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}

	@Override
	protected int getRowIndex(RowIdentifier rowId, Search search) {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}

	@Override
	public AutomatedObject getRow(RowIdentifier rowId, Search search) {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}

	@Override
	public AutomatedObject[] getRows(RowIdentifier rowId, Search search) {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}

	@Override
	public RowIdentifier getRowIdentifier(String... identifyingCellValues) {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}

	@Override
	public AutomatedObject getHeaderRow() {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}

	@Override
	public int getColumnIndex(String columnName) {
		throw new MissingAutomationToolLibrariesException("SELENIUM");
	}
}
