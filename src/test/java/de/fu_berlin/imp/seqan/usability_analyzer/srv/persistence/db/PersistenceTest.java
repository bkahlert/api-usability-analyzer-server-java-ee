package de.fu_berlin.imp.seqan.usability_analyzer.srv.persistence.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.fu_berlin.imp.seqan.usability_analyzer.srv.utils.Utils;

public class PersistenceTest {

	@Rule
	public final TemporaryFolder temporaryFolder = new TemporaryFolder();

	private Persistence ps;

	@Before
	public void before() throws PersistenceException, IOException {
		File folder = temporaryFolder.newFolder();
		folder.delete();
		DerbyDatabase db = new DerbyDatabase(folder);
		this.ps = new Persistence(db);
	}

	@After
	public void after() {
	}

	@Test
	public void testAdd() throws Exception {
		Long key1 = ps.add(Utils.DOCLOG_RECORD);
		assertNotNull(key1);

		Long key2 = ps.add(Utils.DOCLOG_RECORD2);
		assertNotNull(key2);

		assertTrue(key2 > key1);

		// TODO: Datenablage durch DB austauschen
		// TODO: Derbey home setzen, damit log nicht im work dir entsteht
	}

	@Test
	public void testGet() throws Exception {
		for (int i = 0, n = 20; i < n; i++)
			ps.add(Utils.DOCLOG_RECORD);

		Long key1 = ps.add(Utils.DOCLOG_RECORD2);
		assertEquals(Utils.DOCLOG_RECORD2, ps.get(key1));

		for (int i = 0, n = 20; i < n; i++)
			ps.add(Utils.DOCLOG_RECORD3);

		Long key2 = ps.add(Utils.DOCLOG_RECORD4);
		assertEquals(Utils.DOCLOG_RECORD4, ps.get(key2));
	}

	@Test
	public void testRemove() throws Exception {
		for (int i = 0, n = 20; i < n; i++)
			ps.add(Utils.DOCLOG_RECORD);

		Long key = ps.add(Utils.DOCLOG_RECORD2);
		assertEquals(Utils.DOCLOG_RECORD2, ps.get(key));

		for (int i = 0, n = 20; i < n; i++)
			ps.add(Utils.DOCLOG_RECORD3);

		assertTrue(ps.remove(key));
		assertNull(ps.get(key));
	}
}
