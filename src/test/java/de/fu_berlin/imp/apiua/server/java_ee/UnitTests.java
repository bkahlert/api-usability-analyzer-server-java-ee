package de.fu_berlin.imp.apiua.server.java_ee;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
		de.fu_berlin.imp.apiua.server.java_ee.model.AllTests.class,
		de.fu_berlin.imp.apiua.server.java_ee.persistence.AllTests.class })
public class UnitTests {
}
