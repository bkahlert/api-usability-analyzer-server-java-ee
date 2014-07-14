package de.fu_berlin.imp.apiua.server.java_ee.integration;

import org.junit.ClassRule;
import org.junit.rules.ExternalResource;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.fu_berlin.imp.apiua.server.java_ee.utils.rules.StartTestSiteIfNeededRule;

@RunWith(Suite.class)
@SuiteClasses({
		de.fu_berlin.imp.apiua.server.java_ee.integration.APIUAsrv.AllTests.class,
		de.fu_berlin.imp.apiua.server.java_ee.integration.APIUAclt.AllTests.class })
public class AllTests {
	@ClassRule
	public static ExternalResource testSiteRule = new StartTestSiteIfNeededRule();
}
