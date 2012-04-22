package de.fu_berlin.imp.seqan.usability_analyzer.srv.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.junit.Test;

import de.fu_berlin.imp.seqan.usability_analyzer.srv.TempDirectory;

public class DerbyDatabaseTest {

	public static File getTempLocation() throws FileNotFoundException {
		return new File(new TempDirectory(), "derby");
	}

	public void startShutdown(File location) throws Exception {
		DerbyDatabase db = new DerbyDatabase(location);
		assertNull(db.getConnection());

		db.start();
		assertNotNull(db.getConnection());

		Connection conn = db.getConnection();

		Statement stmt = conn.createStatement();
		stmt.execute("VALUES 1+1");

		ResultSet rs = stmt.getResultSet();
		assertTrue(rs.next());
		assertEquals(2, rs.getInt(1));
		assertFalse(rs.next());
		db.shutdown();

		assertNull(db.getConnection());
	}

	@Test
	public void testStartShutdown() throws FileNotFoundException, Exception {
		startShutdown(getTempLocation());
	}

	@Test
	public void testMultipleStartShutdown() throws Exception {
		File location = getTempLocation();
		startShutdown(location);
		startShutdown(location);
		startShutdown(location);
	}
}
