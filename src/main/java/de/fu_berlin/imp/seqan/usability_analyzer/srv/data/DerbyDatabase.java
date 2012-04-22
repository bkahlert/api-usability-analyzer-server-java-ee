package de.fu_berlin.imp.seqan.usability_analyzer.srv.data;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public class DerbyDatabase {
	private static Logger LOGGER = Logger.getLogger(DerbyDatabase.class);

	public static List<Long> getGeneratedKeys(ResultSet rs) throws SQLException {
		List<Long> keys = new ArrayList<Long>();
		while (rs.next())
			keys.add(rs.getLong(1));
		return keys;
	}

	private void loadDriver(String driver) {
		try {
			Class.forName(driver).newInstance();
		} catch (ClassNotFoundException e) {
			LOGGER.fatal("Unable to load the JDBC driver " + driver, e);
		} catch (InstantiationException e) {
			LOGGER.fatal("Unable to instantiate the JDBC driver " + driver);
		} catch (IllegalAccessException e) {
			LOGGER.fatal("Not allowed to access the JDBC driver " + driver);
		}
	}

	private final String location;
	private Connection connection = null;
	private boolean tablesCreated = false;

	public DerbyDatabase(File location) {
		this(location.getAbsolutePath());
	}

	public DerbyDatabase(String location) {
		this.location = location;
	}

	void start() throws SQLException {
		try {
			loadDriver("org.apache.derby.jdbc.EmbeddedDriver");
			this.connection = DriverManager.getConnection("jdbc:derby:"
					+ this.location + ";create=true");
			// this.connection.setAutoCommit(false);
			this.generateTables();
			// this.connection.commit();
		} catch (SQLException e) {
			if (this.connection != null)
				this.connection.close();
			throw e;
		}
	}

	Connection getConnection() {
		return this.connection;
	}

	private void generateTables() throws SQLException {
		if (tablesCreated)
			return;

		String sql;
		try {
			sql = IOUtils.toString(DerbyDatabase.class
					.getResourceAsStream("doclogrecord.sql"));
		} catch (IOException e) {
			throw new SQLException("Could not read SQL file", e);
		}
		Statement statement = null;
		try {
			statement = getConnection().createStatement();
			try {
				statement.execute("SELECT COUNT(*) FROM doclogrecord");
				tablesCreated = true;
			} catch (SQLException e) {
				tablesCreated = false;
			}
			if (!tablesCreated) {
				statement.execute(sql);
			}
		} catch (SQLException e) {
			if (statement != null)
				statement.close();
			throw e;
		}
	}

	public void shutdown() throws SQLException {
		try {
			this.connection.close();
		} catch (SQLException e) {
			throw (e);
		} finally {
			try {
				DriverManager.getConnection("jdbc:derby:;shutdown=true");
			} catch (SQLException e) {
				if (((e.getErrorCode() == 50000) && ("XJ015".equals(e
						.getSQLState())))) {
					// we got the expected exception
					this.connection = null;
				} else {
					throw new SQLException("Derby did not shut down normally",
							e);
				}
			}
		}
	}
}
