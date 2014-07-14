package de.fu_berlin.imp.apiua.server.java_ee.integration.APIUAclt.web;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
		de.fu_berlin.imp.apiua.server.java_ee.integration.APIUAclt.web.host.AllTests.class,
		ClientProvisionTest.class, WebDataCollectionTest.class })
public class AllTests {
}
