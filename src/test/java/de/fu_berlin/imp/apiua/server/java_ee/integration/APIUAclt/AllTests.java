package de.fu_berlin.imp.apiua.server.java_ee.integration.APIUAclt;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
		de.fu_berlin.imp.apiua.server.java_ee.integration.APIUAclt.diff.AllTests.class,
		de.fu_berlin.imp.apiua.server.java_ee.integration.APIUAclt.web.AllTests.class })
public class AllTests {

}
