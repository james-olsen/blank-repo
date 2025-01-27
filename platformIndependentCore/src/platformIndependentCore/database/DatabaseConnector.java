package platformIndependentCore.database;

import java.security.InvalidParameterException;
import java.sql.Array;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import platformIndependentCore.exceptions.InvalidStateException;

/**
 * <b>Name :</b> DatabaseConnector.java
 * <p>
 * <b>Generated :</b> Feb 16, 2022
 * <p>
 * <b>Description :</b> A database connection middleware that handles all the
 * reading and storing of the connection and its different methods to access a
 * result if an SQL script is executed
 * <p>
 *
 * @since Feb 16, 2022
 * @author OITBAYTjoarN
 */
public abstract class DatabaseConnector {
	/** Connection object to hold the connection it puts up */
	protected static Connection conn = null;
	/** Logger for our code */
	protected static Logger log = LogManager.getLogger(DatabaseConnector.class.getName());
	/** Result set of the database we get back from the query we use */
	private static ResultSet rs = null;
	/** Stores the database URI to use */
	private final String dbUri;
	/** Stores the database username of the account */
	private final String dbUser;
	/** Stores the database password of the account */
	private final String dbPassword;

	/**
	 * Constructor
	 *
	 * @param uri      uri of the database to be used
	 * @param user     Username to be used
	 * @param password password to be used
	 */
	public DatabaseConnector(String uri, String user, String password) {
		this.dbUri = uri;
		this.dbUser = user;
		this.dbPassword = password;
	}

	/**
	 * Creates and opens a connection to the database
	 */
	private void openConnection() {
		conn = null;
		Properties connectionProps = new Properties();
		connectionProps.put("user", dbUser);
		connectionProps.put("password", dbPassword);
		try {
			conn = DriverManager.getConnection(dbUri, connectionProps);
		} catch (SQLException e) {
			closeConnection();
			e.printStackTrace();
			throw new InvalidParameterException("Unable to connect to database: " + dbUri);
		}
		log.debug("Connected to database: " + dbUri);
	}

	/**
	 * Executes a Query and returns a result set should there exist a result set to
	 * return
	 *
	 * @param query SQL query to run
	 * @return ResultSet
	 */
	public ResultSet executeQuery(String query) {
		if (conn == null) {
			openConnection();
		}
		Statement sqlStatement = null;

		// Execute the actual query we are trying to get
		try {
			sqlStatement = conn.createStatement();
			rs = sqlStatement.executeQuery(query);
		} catch (SQLException e) {
			closeConnection();
			e.printStackTrace();
			throw new InvalidParameterException("Unable to execute query on database: " + query);
		}

		return rs;
	}

	/**
	 * This method will execute the provided SQL and return the number of rows that
	 * were impacted or found by that SQL statement. If this SQL statement is an
	 * UPDATE statement, the number of rows that were updated will be returned. If
	 * this SQL statement is an INSERT statement, the number of rows that were
	 * inserted will be returned. If this SQL statement is a SELECT, the number of
	 * rows returned from the SQL will be returned
	 *
	 * @param query SQL query to execute
	 * @return int - num of effected rows
	 */
	public int executeQueryAndCount(String query) {
		if (conn == null) {
			openConnection();
		}

		int updateCount = -1;
		Statement sqlStatement = null;
		if (conn != null) {
			try {
				sqlStatement = conn.createStatement();
				if (query.toUpperCase().trim().startsWith("SELECT")) {
					String countSQL = "select COUNT(*)  as resultSetCount FROM (" + query + ")";

					// Only append this last AS statement if this is NOT Oracle
					if (!dbUri.toLowerCase().contains("oracle")) {
						countSQL += " as RESULT_SET_COUNT";
					}

					ResultSet resultSet = null;
					try {
						log.debug("Getting SQL Query count");
						resultSet = sqlStatement.executeQuery(countSQL);
						if (resultSet.next()) {
							updateCount = resultSet.getInt("resultSetCount");
						}
					} catch (SQLException e) {
						closeConnection();
						e.printStackTrace();
						// log a warning
						log.warn("SQLException communicating with the Database: " + e.getMessage());
					} finally {
						if (resultSet != null) {
							resultSet.close();
						}
					}
				} else {
					sqlStatement.execute(query);
					updateCount = sqlStatement.getUpdateCount();
				}

			} catch (SQLException e) {
				closeConnection();
				e.printStackTrace();
				log.warn("SQLException communicating with the Database: " + e.getMessage());
			}
		} else {
			throw new RuntimeException("Unable to establish connection");
		}

		return updateCount;
	}

	/**
	 * Moves the result set to the next row should we need to
	 *
	 * @return boolean
	 */
	public boolean moveToNextRow() {
		if (rs == null) {
			throw new InvalidStateException("You must execute an SQL query first");
		}

		try {
			return rs.next();
		} catch (SQLException e) {
			closeConnection();
			e.printStackTrace();
			throw new InvalidParameterException("Unable to get next row");
		}
	}

	/**
	 * Moves to specific row as specified
	 *
	 * @param row row number to move to
	 * @return boolean
	 */
	public boolean moveToSpecificRow(int row) {
		if (rs == null) {
			throw new InvalidStateException("You must execute an SQL query first");
		}

		try {
			return rs.absolute(row);
		} catch (SQLException e) {
			closeConnection();
			e.printStackTrace();
			throw new InvalidParameterException("Unable to move to row");
		}
	}

	/**
	 * Gets and returns the Array data of a column based on its index
	 *
	 * @param colLabel Index of the column label to return
	 * @return Array
	 */
	public Array getArray(int colLabel) {
		if (rs == null) {
			throw new InvalidStateException("You must execute an SQL query first");
		}

		try {
			return rs.getArray(colLabel);
		} catch (SQLException e) {
			closeConnection();
			e.printStackTrace();
			throw new InvalidParameterException("Unable to convert data into Array");
		}
	}

	/**
	 * Gets and returns the Array data of a column based on its index
	 *
	 * @param colLabel Title of the column label to return
	 * @return Array
	 */
	public Array getArray(String colLabel) {
		if (rs == null) {
			throw new InvalidStateException("You must execute an SQL query first");
		}

		try {
			return rs.getArray(colLabel);
		} catch (SQLException e) {
			closeConnection();
			e.printStackTrace();
			throw new InvalidParameterException("Unable to convert data into Array");
		}
	}

	/**
	 * Gets and returns the Boolean data of a column based on its index
	 *
	 * @param colLabel Index of the column label to return
	 * @return Boolean
	 */
	public Boolean getBoolean(int colLabel) {
		if (rs == null) {
			throw new InvalidStateException("You must execute an SQL query first");
		}

		try {
			return rs.getBoolean(colLabel);
		} catch (SQLException e) {
			closeConnection();
			e.printStackTrace();
			throw new InvalidParameterException("Unable to convert data into Boolean");
		}
	}

	/**
	 * Gets and returns the Boolean data of a column based on its index
	 *
	 * @param colLabel Title of the column label to return
	 * @return Boolean
	 */
	public Boolean getBoolean(String colLabel) {
		if (rs == null) {
			throw new InvalidStateException("You must execute an SQL query first");
		}

		try {
			return rs.getBoolean(colLabel);
		} catch (SQLException e) {
			closeConnection();
			e.printStackTrace();
			throw new InvalidParameterException("Unable to convert data into Boolean");
		}
	}

	/**
	 * Gets and returns the Date data of a column based on its index
	 *
	 * @param colLabel Index of the column label to return
	 * @return Date
	 */
	public Date getDate(int colLabel) {
		if (rs == null) {
			throw new InvalidStateException("You must execute an SQL query first");
		}

		try {
			return rs.getDate(colLabel);
		} catch (SQLException e) {
			closeConnection();
			e.printStackTrace();
			throw new InvalidParameterException("Unable to convert data into Date");
		}
	}

	/**
	 * Gets and returns the Date data of a column based on its index
	 *
	 * @param colLabel Title of the column label to return
	 * @return Date
	 */
	public Date getDate(String colLabel) {
		if (rs == null) {
			throw new InvalidStateException("You must execute an SQL query first");
		}

		try {
			return rs.getDate(colLabel);
		} catch (SQLException e) {
			closeConnection();
			e.printStackTrace();
			throw new InvalidParameterException("Unable to convert data into Date");
		}
	}

	/**
	 * Gets and returns the Double data of a column based on its index
	 *
	 * @param colLabel Index of the column label to return
	 * @return Double
	 */
	public Double getDouble(int colLabel) {
		if (rs == null) {
			throw new InvalidStateException("You must execute an SQL query first");
		}

		try {
			return rs.getDouble(colLabel);
		} catch (SQLException e) {
			closeConnection();
			e.printStackTrace();
			throw new InvalidParameterException("Unable to convert data into Double");
		}
	}

	/**
	 * Gets and returns the Double data of a column based on its index
	 *
	 * @param colLabel Title of the column label to return
	 * @return Double
	 */
	public Double getDouble(String colLabel) {
		if (rs == null) {
			throw new InvalidStateException("You must execute an SQL query first");
		}

		try {
			return rs.getDouble(colLabel);
		} catch (SQLException e) {
			closeConnection();
			e.printStackTrace();
			throw new InvalidParameterException("Unable to convert data into Double");
		}
	}

	/**
	 * Gets and returns the Float data of a column based on its index
	 *
	 * @param colLabel Index of the column label to return
	 * @return Float
	 */
	public Float getFloat(int colLabel) {
		if (rs == null) {
			throw new InvalidStateException("You must execute an SQL query first");
		}

		try {
			return rs.getFloat(colLabel);
		} catch (SQLException e) {
			closeConnection();
			e.printStackTrace();
			throw new InvalidParameterException("Unable to convert data into Float");
		}
	}

	/**
	 * Gets and returns the Float data of a column based on its index
	 *
	 * @param colLabel Title of the column label to return
	 * @return Float
	 */
	public Float getFloat(String colLabel) {
		if (rs == null) {
			throw new InvalidStateException("You must execute an SQL query first");
		}

		try {
			return rs.getFloat(colLabel);
		} catch (SQLException e) {
			closeConnection();
			e.printStackTrace();
			throw new InvalidParameterException("Unable to convert data into Float");
		}
	}

	/**
	 * Gets and returns the integer data of a column based on its index
	 *
	 * @param colLabel Index of the column label to return
	 * @return InputStream
	 */
	public int getInt(int colLabel) {
		if (rs == null) {
			throw new InvalidStateException("You must execute an SQL query first");
		}

		try {
			return rs.getInt(colLabel);
		} catch (SQLException e) {
			closeConnection();
			e.printStackTrace();
			throw new InvalidParameterException("Unable to convert data into int");
		}
	}

	/**
	 * Gets and returns the integer data of a column based on its index
	 *
	 * @param colLabel Title of the column label to return
	 * @return int
	 */
	public int getInt(String colLabel) {
		if (rs == null) {
			throw new InvalidStateException("You must execute an SQL query first");
		}

		try {
			return rs.getInt(colLabel);
		} catch (SQLException e) {
			closeConnection();
			e.printStackTrace();
			throw new InvalidParameterException("Unable to convert data into int");
		}
	}

	/**
	 * Gets and returns the String data of a column based on its index
	 *
	 * @param colLabel Index of the column label to return
	 * @return String
	 */
	public String getString(int colLabel) {
		if (rs == null) {
			throw new InvalidStateException("You must execute an SQL query first");
		}

		try {
			return rs.getString(colLabel);
		} catch (SQLException e) {
			closeConnection();
			e.printStackTrace();
			throw new InvalidParameterException("Unable to convert data into String");
		}
	}

	/**
	 * Gets and returns the String data of a column based on its index
	 *
	 * @param colLabel Title of the column label to return
	 * @return String
	 */
	public String getString(String colLabel) {
		if (rs == null) {
			throw new InvalidStateException("You must execute an SQL query first");
		}

		try {
			return rs.getString(colLabel);
		} catch (SQLException e) {
			closeConnection();
			e.printStackTrace();
			throw new InvalidParameterException("Unable to convert data into String");
		}
	}

	/**
	 * If there are more functions from the result set a user wishes to invoke, they
	 * can use this function to get their current result set
	 *
	 * @return ResultSet
	 */
	public ResultSet getResultSet() {
		if (rs == null) {
			throw new InvalidStateException("You must execute an SQL query first");
		}

		return rs;
	}

	/**
	 * Closes the connection to the database, should be called at the end of every
	 * script to remove all connections and result sets that are open to free memory
	 */
	public void closeConnection() {
		try {
			if (conn != null && !conn.isClosed()) {
				conn.close();
				conn = null;
			}
			if (rs != null && !rs.isClosed()) {
				rs.close();
				rs = null;
			}
		} catch (Exception e) {
			throw new InvalidParameterException("Unable to close connection with database");
		}
	}
}
