package de.fu_berlin.imp.seqan.usability_analyzer.srv.persistence.db;

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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class DerbyDatabaseTest {

	@Rule
	public final TemporaryFolder temporaryFolder = new TemporaryFolder();

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
		File folder = temporaryFolder.newFolder();
		folder.delete();
		startShutdown(folder);
	}

	@Test
	public void testMultipleStartShutdown() throws Exception {
		File folder = temporaryFolder.newFolder();
		folder.delete();
		startShutdown(folder);
		startShutdown(folder);
		startShutdown(folder);
	}
}
