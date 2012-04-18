package de.fu_berlin.imp.seqan.usability_analyzer.srv;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ StaticFilterTest.class,
		de.fu_berlin.imp.seqan.usability_analyzer.srv.model.AllTests.class,
		de.fu_berlin.imp.seqan.usability_analyzer.srv.clt.AllTests.class,
		de.fu_berlin.imp.seqan.usability_analyzer.srv.rest.AllTests.class })
public class AllTests {

}
