package de.fu_berlin.imp.apiua.server.java_ee.integration.APIUAsrv;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ BlockedContentTest.class, DoclogRESTTest.class,
		FingerprintRESTTest.class })
public class AllTests {

}
