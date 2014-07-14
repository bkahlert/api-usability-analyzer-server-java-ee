package de.fu_berlin.imp.seqan.usability_analyzer.srv.integration.SUAclt.web;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
		de.fu_berlin.imp.seqan.usability_analyzer.srv.integration.SUAclt.web.host.AllTests.class,
		ClientProvisionTest.class, WebDataCollectionTest.class })
public class AllTests {
}
