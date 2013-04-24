package de.fu_berlin.imp.seqan.usability_analyzer.srv.persistence;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
		de.fu_berlin.imp.seqan.usability_analyzer.srv.persistence.db.AllTests.class,
		DoclogFileTest.class, DoclogPersistenceTest.class,
		MappingDoclogPersistenceTest.class,
		MappingDoclogPersistenceParametrizedTest.class })
public class AllTests {

}
