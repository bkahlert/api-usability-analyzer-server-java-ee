package de.fu_berlin.imp.seqan.usability_analyzer.srv.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;

import org.junit.Test;

import de.fu_berlin.imp.seqan.usability_analyzer.srv.rest.DoclogManagerTest;

public class PersistenceTest {

	public static DerbyDatabase getTempDerby() throws FileNotFoundException {
		return new DerbyDatabase(DerbyDatabaseTest.getTempLocation());
	}

	@Test
	public void testAdd() throws Exception {
		Persistence ps = new Persistence(getTempDerby());

		Long key1 = ps.add(DoclogManagerTest.DOCLOG_RECORD);
		assertNotNull(key1);

		Long key2 = ps.add(DoclogManagerTest.DOCLOG_RECORD2);
		assertNotNull(key2);

		assertTrue(key2 > key1);

		// TODO: Datenablage durch DB austauschen
		// TODO: Derbey home setzen, damit log nicht im work dir entsteht
	}

	@Test
	public void testGet() throws Exception {
		Persistence ps = new Persistence(getTempDerby());

		for (int i = 0, n = 20; i < n; i++)
			ps.add(DoclogManagerTest.DOCLOG_RECORD);

		Long key1 = ps.add(DoclogManagerTest.DOCLOG_RECORD2);
		assertEquals(DoclogManagerTest.DOCLOG_RECORD2, ps.get(key1));

		for (int i = 0, n = 20; i < n; i++)
			ps.add(DoclogManagerTest.DOCLOG_RECORD3);

		Long key2 = ps.add(DoclogManagerTest.DOCLOG_RECORD4);
		assertEquals(DoclogManagerTest.DOCLOG_RECORD4, ps.get(key2));
	}

	@Test
	public void testRemove() throws Exception {
		Persistence ps = new Persistence(getTempDerby());

		for (int i = 0, n = 20; i < n; i++)
			ps.add(DoclogManagerTest.DOCLOG_RECORD);

		Long key = ps.add(DoclogManagerTest.DOCLOG_RECORD2);
		assertEquals(DoclogManagerTest.DOCLOG_RECORD2, ps.get(key));

		for (int i = 0, n = 20; i < n; i++)
			ps.add(DoclogManagerTest.DOCLOG_RECORD3);

		assertTrue(ps.remove(key));
		assertNull(ps.get(key));
	}
}
