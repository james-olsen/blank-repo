package rftCore;

import platformIndependentCore.core.AutomatedObject;
import platformIndependentCore.core.Search;
import platformIndependentCore.exceptions.MissingAutomationToolLibrariesException;
import platformIndependentCore.tables.RowIdentifier;
import platformIndependentCore.tables.TableObject;

public class RftTable extends TableObject {

	public RftTable(AutomatedObject object, String... cols) {
		super(object, cols);
	}

	@Override
	protected String readValue(AutomatedObject cell) {
		throw new MissingAutomationToolLibrariesException("RFT");
	}

	@Override
	public AutomatedObject getCell(String columHeaderToGet, Search searchConfig) {
		throw new MissingAutomationToolLibrariesException("RFT");
	}

	// @Override
	// protected int getRowIndex(String rowId, SearchConfig search) {
	// throw new MissingPlatformLibrariesException("RFT");
	// }
	//
	// @Override
	// public AutomatedObject getRow(String rowId, SearchConfig search) {
	// throw new MissingPlatformLibrariesException("RFT");
	// }
	//
	// @Override
	// protected int getRowIndex(String[] rowId, SearchConfig search) {
	// throw new MissingPlatformLibrariesException("RFT");
	// }
	//
	// @Override
	// public AutomatedObject getRow(String[] rowId, SearchConfig search) {
	// throw new MissingPlatformLibrariesException("RFT");
	// }
	//
	// @Override
	// public AutomatedObject[] getRows(String[] rowId, SearchConfig search) {
	// throw new MissingPlatformLibrariesException("RFT");
	// }

	@Override
	public AutomatedObject getRow(RowIdentifier rowId, Search search) {
		throw new MissingAutomationToolLibrariesException("RFT");
	}

	@Override
	protected int getRowIndex(RowIdentifier rowId, Search search) {
		throw new MissingAutomationToolLibrariesException("RFT");
	}

	@Override
	public AutomatedObject[] getRows(RowIdentifier rowId, Search search) {
		throw new MissingAutomationToolLibrariesException("RFT");
	}

	@Override
	public RowIdentifier getRowIdentifier(String... rowID) {
		throw new MissingAutomationToolLibrariesException("RFT");
	}

	@Override
	public AutomatedObject getHeaderRow() {
		throw new MissingAutomationToolLibrariesException("RFT");
	}

	@Override
	public int getColumnIndex(String columnName) {
		throw new MissingAutomationToolLibrariesException("RFT");
	}

}
