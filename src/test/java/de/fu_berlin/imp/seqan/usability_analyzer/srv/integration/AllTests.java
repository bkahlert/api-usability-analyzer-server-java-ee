package de.fu_berlin.imp.seqan.usability_analyzer.srv.integration;

import org.junit.ClassRule;
import org.junit.rules.ExternalResource;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.fu_berlin.imp.seqan.usability_analyzer.srv.utils.rules.StartTestSiteIfNeededRule;

@RunWith(Suite.class)
@SuiteClasses({
		de.fu_berlin.imp.seqan.usability_analyzer.srv.integration.SUAsrv.AllTests.class,
		de.fu_berlin.imp.seqan.usability_analyzer.srv.integration.SUAclt.AllTests.class })
public class AllTests {
	@ClassRule
	public static ExternalResource testSiteRule = new StartTestSiteIfNeededRule();
}
