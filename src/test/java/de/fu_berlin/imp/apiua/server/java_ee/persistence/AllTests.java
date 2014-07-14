package de.fu_berlin.imp.apiua.server.java_ee.persistence;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
		de.fu_berlin.imp.apiua.server.java_ee.persistence.db.AllTests.class,
		DoclogFileTest.class, DoclogPersistenceTest.class,
		MappingDoclogPersistenceTest.class,
		MappingDoclogPersistenceParametrizedTest.class })
public class AllTests {

}
