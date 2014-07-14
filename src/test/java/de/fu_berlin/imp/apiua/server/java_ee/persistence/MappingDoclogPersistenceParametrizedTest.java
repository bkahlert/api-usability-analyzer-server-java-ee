package de.fu_berlin.imp.apiua.server.java_ee.persistence;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.fu_berlin.imp.apiua.server.java_ee.model.Fingerprint;
import de.fu_berlin.imp.apiua.server.java_ee.model.ID;
import de.fu_berlin.imp.apiua.server.java_ee.model.IIdentifier;
import de.fu_berlin.imp.apiua.server.java_ee.persistence.DoclogPersistence;
import de.fu_berlin.imp.apiua.server.java_ee.persistence.MappingDoclogPersistence;
import de.fu_berlin.imp.apiua.server.java_ee.persistence.DoclogPersistence.DoclogPersistenceException;
import de.fu_berlin.imp.apiua.server.java_ee.utils.Utils;

@RunWith(value = Parameterized.class)
public class MappingDoclogPersistenceParametrizedTest {

	private static class Argument {
		public IIdentifier key;
		public ID id;

		public Argument(IIdentifier key, ID id) {
			super();
			this.key = key;
			this.id = id;
		}

		@Override
		public String toString() {
			return "log(" + this.key + ", " + this.id + ")";
		}
	}

	private static class CallCheck {
		public int keyNumber;
		public int idNumber;

		public CallCheck(int keyNumber, int idNumber) {
			this.keyNumber = keyNumber;
			this.idNumber = idNumber;
		}
	}

	private static class Check {
		private List<CallCheck> postWriteCallNumRecords = new ArrayList<CallCheck>();
		public Class<? extends Throwable> expectedThrowableClass;

		public Check(List<CallCheck> values,
				Class<? extends Throwable> expectedThrowableClass) {
			postWriteCallNumRecords.addAll(values);
			this.expectedThrowableClass = expectedThrowableClass;
		}

		public void check(DoclogPersistence persistence, List<Argument> argument)
				throws DoclogPersistenceException {
			assertEquals(postWriteCallNumRecords.size(), argument.size());

			for (int i = 0; i < postWriteCallNumRecords.size(); i++) {
				assertEquals(postWriteCallNumRecords.get(i).keyNumber,
						persistence.getNumRecords(argument.get(i).key));
				assertEquals(postWriteCallNumRecords.get(i).idNumber,
						persistence.getNumRecords(argument.get(i).id));
			}
		}

		public void runAndCheck(MappingDoclogPersistence persistence,
				List<Argument> arguments, int i)
				throws DoclogPersistenceException {
			assertEquals(postWriteCallNumRecords.size(), arguments.size());

			Argument argument = arguments.get(i);
			try {
				persistence.write(argument.key, argument.id,
						Utils.DOCLOG_RECORD);
				if (expectedThrowableClass != null) {
					Assert.fail(expectedThrowableClass + " expected");
				}
			} catch (Throwable e) {
				if (expectedThrowableClass == null) {
					Assert.fail("No exception expected, but thrown: " + e);
				} else {
					Assert.assertTrue(expectedThrowableClass + " expected but "
							+ e.getClass() + " thrown",
							expectedThrowableClass == e.getClass());
				}
			}
			this.check(persistence, arguments);
		}
	}

	private List<Argument> arguments = new ArrayList<Argument>();
	private List<Check> checks = new ArrayList<Check>();

	private final static Fingerprint f1 = Utils.getTestFingerprint();
	private final static Fingerprint f2 = Utils.getTestFingerprint();
	private final static ID i1 = Utils.getTestID();
	private final static ID i2 = Utils.getTestID();
	private final static ID nl = null;

	/**
	 * INTEGRATION_TEST 3 consecutive write calls with:<br>
	 * Fingerprint 1: f1<br>
	 * Fingerprint 2: f2<br>
	 * ID 1: i1<br>
	 * ID 2: i2<br>
	 * Null: nl
	 * <p>
	 * A call takes 2 arguments:<br>
	 * argument 1 can be Fingerprint or ID (5 combinations)<br>
	 * argument 2 can be ID (3 combinations) makes 3*5 = 15 combinations
	 * <p>
	 * Because always three calls are done this results in 3*15 = 45
	 * combinations.
	 */
	@Parameters(name = "{0}")
	public static Collection<Object[]> data() {
		Object[][] data = new Object[][] {
				/*
				 * all single call combinations
				 */
				{
						Arrays.asList(new Argument(nl, nl)),
						Arrays.asList(new Check(Arrays.asList(new CallCheck(0,
								0)), IllegalArgumentException.class)) },
				{
						Arrays.asList(new Argument(nl, i1)),
						Arrays.asList(new Check(Arrays.asList(new CallCheck(0,
								1)), null)) },
				{
						Arrays.asList(new Argument(nl, i2)),
						Arrays.asList(new Check(Arrays.asList(new CallCheck(0,
								1)), null)) },

				{
						Arrays.asList(new Argument(f1, nl)),
						Arrays.asList(new Check(Arrays.asList(new CallCheck(1,
								0)), null)) },
				{
						Arrays.asList(new Argument(f1, i1)),
						Arrays.asList(new Check(Arrays.asList(new CallCheck(1,
								1)), null)) },
				{
						Arrays.asList(new Argument(f1, i2)),
						Arrays.asList(new Check(Arrays.asList(new CallCheck(1,
								1)), null)) },

				{
						Arrays.asList(new Argument(f2, nl)),
						Arrays.asList(new Check(Arrays.asList(new CallCheck(1,
								0)), null)) },
				{
						Arrays.asList(new Argument(f2, i1)),
						Arrays.asList(new Check(Arrays.asList(new CallCheck(1,
								1)), null)) },
				{
						Arrays.asList(new Argument(f2, i2)),
						Arrays.asList(new Check(Arrays.asList(new CallCheck(1,
								1)), null)) },

				{
						Arrays.asList(new Argument(i1, nl)),
						Arrays.asList(new Check(Arrays.asList(new CallCheck(1,
								0)), null)) },
				{
						Arrays.asList(new Argument(i1, i1)),
						Arrays.asList(new Check(Arrays.asList(new CallCheck(1,
								1)), null)) },
				{
						Arrays.asList(new Argument(i1, i2)),
						Arrays.asList(new Check(Arrays.asList(new CallCheck(1,
								0)), null)) },

				{
						Arrays.asList(new Argument(i2, nl)),
						Arrays.asList(new Check(Arrays.asList(new CallCheck(1,
								0)), null)) },
				{
						Arrays.asList(new Argument(i2, i1)),
						Arrays.asList(new Check(Arrays.asList(new CallCheck(1,
								0)), null)) },
				{
						Arrays.asList(new Argument(i2, i2)),
						Arrays.asList(new Check(Arrays.asList(new CallCheck(1,
								1)), null)) },

				/*
				 * relevant duo call combinations
				 */
				// second call invalid
				{
						Arrays.asList(new Argument(f1, nl),
								new Argument(nl, nl)),
						Arrays.asList(
								new Check(Arrays.asList(new CallCheck(1, 0),
										new CallCheck(0, 0)), null),
								new Check(Arrays.asList(new CallCheck(1, 0),
										new CallCheck(0, 0)),
										IllegalArgumentException.class)) },
				{
						Arrays.asList(new Argument(i1, nl),
								new Argument(nl, nl)),
						Arrays.asList(
								new Check(Arrays.asList(new CallCheck(1, 0),
										new CallCheck(0, 0)), null),
								new Check(Arrays.asList(new CallCheck(1, 0),
										new CallCheck(0, 0)),
										IllegalArgumentException.class)) },
				{
						Arrays.asList(new Argument(f1, i1),
								new Argument(nl, nl)),
						Arrays.asList(
								new Check(Arrays.asList(new CallCheck(1, 1),
										new CallCheck(0, 0)), null),
								new Check(Arrays.asList(new CallCheck(1, 1),
										new CallCheck(0, 0)),
										IllegalArgumentException.class)) },
				{
						Arrays.asList(new Argument(i1, i2),
								new Argument(nl, nl)),
						Arrays.asList(
								new Check(Arrays.asList(new CallCheck(1, 0),
										new CallCheck(0, 0)), null),
								new Check(Arrays.asList(new CallCheck(1, 0),
										new CallCheck(0, 0)),
										IllegalArgumentException.class)) },

				// start with fingerprint
				{
						Arrays.asList(new Argument(f1, nl),
								new Argument(nl, i1)),
						Arrays.asList(
								new Check(Arrays.asList(new CallCheck(1, 0),
										new CallCheck(0, 0)), null),
								new Check(Arrays.asList(new CallCheck(1, 0),
										new CallCheck(0, 1)), null)) },
				{
						Arrays.asList(new Argument(f1, nl),
								new Argument(f1, nl)),
						Arrays.asList(
								new Check(Arrays.asList(new CallCheck(1, 0),
										new CallCheck(1, 0)), null),
								new Check(Arrays.asList(new CallCheck(2, 0),
										new CallCheck(2, 0)), null)) },
				{
						Arrays.asList(new Argument(f1, nl),
								new Argument(f1, i1)),
						Arrays.asList(
								new Check(Arrays.asList(new CallCheck(1, 0),
										new CallCheck(1, 0)), null),
								new Check(Arrays.asList(new CallCheck(2, 0),
										new CallCheck(2, 2)), null)) },
				{
						Arrays.asList(new Argument(f1, nl),
								new Argument(f2, nl)),
						Arrays.asList(
								new Check(Arrays.asList(new CallCheck(1, 0),
										new CallCheck(0, 0)), null),
								new Check(Arrays.asList(new CallCheck(1, 0),
										new CallCheck(1, 0)), null)) },
				{
						Arrays.asList(new Argument(f1, nl),
								new Argument(f2, i1)),
						Arrays.asList(
								new Check(Arrays.asList(new CallCheck(1, 0),
										new CallCheck(0, 0)), null),
								new Check(Arrays.asList(new CallCheck(1, 0),
										new CallCheck(1, 1)), null)) },
				{
						Arrays.asList(new Argument(f1, nl),
								new Argument(i1, nl)),
						Arrays.asList(
								new Check(Arrays.asList(new CallCheck(1, 0),
										new CallCheck(0, 0)), null),
								new Check(Arrays.asList(new CallCheck(1, 0),
										new CallCheck(1, 0)), null)) },
				{
						Arrays.asList(new Argument(f1, nl),
								new Argument(i1, i1)),
						Arrays.asList(
								new Check(Arrays.asList(new CallCheck(1, 0),
										new CallCheck(0, 0)), null),
								new Check(Arrays.asList(new CallCheck(1, 0),
										new CallCheck(1, 1)), null)) },
				{
						Arrays.asList(new Argument(f1, nl),
								new Argument(i1, i2)),
						Arrays.asList(
								new Check(Arrays.asList(new CallCheck(1, 0),
										new CallCheck(0, 0)), null),
								new Check(Arrays.asList(new CallCheck(1, 0),
										new CallCheck(1, 0)), null)) },

				// start with id as key
				{
						Arrays.asList(new Argument(i1, nl),
								new Argument(nl, i1)),
						Arrays.asList(
								new Check(Arrays.asList(new CallCheck(1, 0),
										new CallCheck(0, 1)), null),
								new Check(Arrays.asList(new CallCheck(2, 0),
										new CallCheck(0, 2)), null)) },
				{
						Arrays.asList(new Argument(i1, nl),
								new Argument(nl, i2)),
						Arrays.asList(
								new Check(Arrays.asList(new CallCheck(1, 0),
										new CallCheck(0, 0)), null),
								new Check(Arrays.asList(new CallCheck(1, 0),
										new CallCheck(0, 1)), null)) },
				{
						Arrays.asList(new Argument(i1, nl),
								new Argument(f1, nl)),
						Arrays.asList(
								new Check(Arrays.asList(new CallCheck(1, 0),
										new CallCheck(0, 0)), null),
								new Check(Arrays.asList(new CallCheck(1, 0),
										new CallCheck(1, 0)), null)) },
				{
						Arrays.asList(new Argument(i1, nl),
								new Argument(f1, i1)),
						Arrays.asList(
								new Check(Arrays.asList(new CallCheck(1, 0),
										new CallCheck(0, 1)), null),
								new Check(Arrays.asList(new CallCheck(2, 0),
										new CallCheck(2, 2)), null)) },
				{
						Arrays.asList(new Argument(i1, nl),
								new Argument(f1, i2)),
						Arrays.asList(
								new Check(Arrays.asList(new CallCheck(1, 0),
										new CallCheck(0, 0)), null),
								new Check(Arrays.asList(new CallCheck(1, 0),
										new CallCheck(1, 1)), null)) },
				{
						Arrays.asList(new Argument(i1, nl),
								new Argument(i1, nl)),
						Arrays.asList(
								new Check(Arrays.asList(new CallCheck(1, 0),
										new CallCheck(1, 0)), null),
								new Check(Arrays.asList(new CallCheck(2, 0),
										new CallCheck(2, 0)), null)) },
				{
						Arrays.asList(new Argument(i1, nl),
								new Argument(i1, i1)),
						Arrays.asList(
								new Check(Arrays.asList(new CallCheck(1, 0),
										new CallCheck(1, 1)), null),
								new Check(Arrays.asList(new CallCheck(2, 0),
										new CallCheck(2, 2)), null)) },
				{
						Arrays.asList(new Argument(i1, nl),
								new Argument(i1, i2)),
						Arrays.asList(
								new Check(Arrays.asList(new CallCheck(1, 0),
										new CallCheck(1, 0)), null),
								new Check(Arrays.asList(new CallCheck(2, 0),
										new CallCheck(2, 0)), null)) },
				{
						Arrays.asList(new Argument(i1, nl),
								new Argument(i2, i1)),
						Arrays.asList(
								new Check(Arrays.asList(new CallCheck(1, 0),
										new CallCheck(0, 1)), null),
								new Check(Arrays.asList(new CallCheck(1, 0),
										new CallCheck(1, 1)), null)) },
				{
						Arrays.asList(new Argument(i1, nl),
								new Argument(i2, i2)),
						Arrays.asList(
								new Check(Arrays.asList(new CallCheck(1, 0),
										new CallCheck(0, 0)), null),
								new Check(Arrays.asList(new CallCheck(1, 0),
										new CallCheck(1, 1)), null)) },

				// start with id as id
				{
						Arrays.asList(new Argument(nl, i1),
								new Argument(nl, i1)),
						Arrays.asList(
								new Check(Arrays.asList(new CallCheck(0, 1),
										new CallCheck(0, 1)), null),
								new Check(Arrays.asList(new CallCheck(0, 2),
										new CallCheck(0, 2)), null)) },
				{
						Arrays.asList(new Argument(nl, i1),
								new Argument(nl, i2)),
						Arrays.asList(
								new Check(Arrays.asList(new CallCheck(0, 1),
										new CallCheck(0, 0)), null),
								new Check(Arrays.asList(new CallCheck(0, 1),
										new CallCheck(0, 1)), null)) },
				{
						Arrays.asList(new Argument(nl, i1),
								new Argument(f1, nl)),
						Arrays.asList(
								new Check(Arrays.asList(new CallCheck(0, 1),
										new CallCheck(0, 0)), null),
								new Check(Arrays.asList(new CallCheck(0, 1),
										new CallCheck(1, 0)), null)) },
				{
						Arrays.asList(new Argument(nl, i1),
								new Argument(f1, i1)),
						Arrays.asList(
								new Check(Arrays.asList(new CallCheck(0, 1),
										new CallCheck(0, 1)), null),
								new Check(Arrays.asList(new CallCheck(0, 2),
										new CallCheck(2, 2)), null)) },
				{
						Arrays.asList(new Argument(nl, i1),
								new Argument(f1, i2)),
						Arrays.asList(
								new Check(Arrays.asList(new CallCheck(0, 1),
										new CallCheck(0, 0)), null),
								new Check(Arrays.asList(new CallCheck(0, 1),
										new CallCheck(1, 1)), null)) },
				{
						Arrays.asList(new Argument(nl, i1),
								new Argument(i1, nl)),
						Arrays.asList(
								new Check(Arrays.asList(new CallCheck(0, 1),
										new CallCheck(1, 0)), null),
								new Check(Arrays.asList(new CallCheck(0, 2),
										new CallCheck(2, 0)), null)) },
				{
						Arrays.asList(new Argument(nl, i1),
								new Argument(i1, i1)),
						Arrays.asList(
								new Check(Arrays.asList(new CallCheck(0, 1),
										new CallCheck(1, 1)), null),
								new Check(Arrays.asList(new CallCheck(0, 2),
										new CallCheck(2, 2)), null)) },
				{
						Arrays.asList(new Argument(nl, i1),
								new Argument(i1, i2)),
						Arrays.asList(
								new Check(Arrays.asList(new CallCheck(0, 1),
										new CallCheck(1, 0)), null),
								new Check(Arrays.asList(new CallCheck(0, 2),
										new CallCheck(2, 0)), null)) },
				{
						Arrays.asList(new Argument(nl, i1),
								new Argument(i2, i1)),
						Arrays.asList(
								new Check(Arrays.asList(new CallCheck(0, 1),
										new CallCheck(0, 1)), null),
								new Check(Arrays.asList(new CallCheck(0, 1),
										new CallCheck(1, 1)), null)) },
				{
						Arrays.asList(new Argument(nl, i1),
								new Argument(i2, i2)),
						Arrays.asList(
								new Check(Arrays.asList(new CallCheck(0, 1),
										new CallCheck(0, 0)), null),
								new Check(Arrays.asList(new CallCheck(0, 1),
										new CallCheck(1, 1)), null)) },

				// start with fingerprint and id
				{
						Arrays.asList(new Argument(f1, i1),
								new Argument(nl, i1)),
						Arrays.asList(
								new Check(Arrays.asList(new CallCheck(1, 1),
										new CallCheck(0, 1)), null),
								new Check(Arrays.asList(new CallCheck(2, 2),
										new CallCheck(0, 2)), null)) },
				{
						Arrays.asList(new Argument(f1, i1),
								new Argument(nl, i2)),
						Arrays.asList(
								new Check(Arrays.asList(new CallCheck(1, 1),
										new CallCheck(0, 0)), null),
								new Check(Arrays.asList(new CallCheck(1, 1),
										new CallCheck(0, 1)), null)) },
				{
						Arrays.asList(new Argument(f1, i1),
								new Argument(f1, nl)),
						Arrays.asList(
								new Check(Arrays.asList(new CallCheck(1, 1),
										new CallCheck(1, 0)), null),
								new Check(Arrays.asList(new CallCheck(2, 2),
										new CallCheck(2, 0)), null)) },
				{
						Arrays.asList(new Argument(f1, i1),
								new Argument(f1, i1)),
						Arrays.asList(
								new Check(Arrays.asList(new CallCheck(1, 1),
										new CallCheck(1, 1)), null),
								new Check(Arrays.asList(new CallCheck(2, 2),
										new CallCheck(2, 2)), null)) },
				{
						Arrays.asList(new Argument(f1, i1),
								new Argument(f1, i2)),
						Arrays.asList(
								new Check(Arrays.asList(new CallCheck(1, 1),
										new CallCheck(1, 0)), null),
								new Check(Arrays.asList(new CallCheck(2, 2),
										new CallCheck(2, 0)), null)) },
				{
						Arrays.asList(new Argument(f1, i1),
								new Argument(i1, nl)),
						Arrays.asList(
								new Check(Arrays.asList(new CallCheck(1, 1),
										new CallCheck(1, 0)), null),
								new Check(Arrays.asList(new CallCheck(2, 2),
										new CallCheck(2, 0)), null)) },
				{
						Arrays.asList(new Argument(f1, i1),
								new Argument(i1, i1)),
						Arrays.asList(
								new Check(Arrays.asList(new CallCheck(1, 1),
										new CallCheck(1, 1)), null),
								new Check(Arrays.asList(new CallCheck(2, 2),
										new CallCheck(2, 2)), null)) },
				{
						Arrays.asList(new Argument(f1, i1),
								new Argument(i1, i2)),
						Arrays.asList(
								new Check(Arrays.asList(new CallCheck(1, 1),
										new CallCheck(1, 0)), null),
								new Check(Arrays.asList(new CallCheck(2, 2),
										new CallCheck(2, 0)), null)) },
				{
						Arrays.asList(new Argument(f1, i1),
								new Argument(i2, i1)),
						Arrays.asList(
								new Check(Arrays.asList(new CallCheck(1, 1),
										new CallCheck(0, 1)), null),
								new Check(Arrays.asList(new CallCheck(1, 1),
										new CallCheck(1, 1)), null)) },
				{
						Arrays.asList(new Argument(f1, i1),
								new Argument(i2, i2)),
						Arrays.asList(
								new Check(Arrays.asList(new CallCheck(1, 1),
										new CallCheck(0, 0)), null),
								new Check(Arrays.asList(new CallCheck(1, 1),
										new CallCheck(1, 1)), null)) },

				/*
				 * relevant triple call combinations
				 */
				{
						Arrays.asList(new Argument(f1, nl),
								new Argument(f2, nl), new Argument(i1, nl)),
						Arrays.asList(
								new Check(Arrays.asList(new CallCheck(1, 0),
										new CallCheck(0, 0),
										new CallCheck(0, 0)), null),
								new Check(Arrays.asList(new CallCheck(1, 0),
										new CallCheck(1, 0),
										new CallCheck(0, 0)), null),
								new Check(Arrays.asList(new CallCheck(1, 0),
										new CallCheck(1, 0),
										new CallCheck(1, 0)), null)) },
				{
						Arrays.asList(new Argument(f1, nl),
								new Argument(f2, i1), new Argument(nl, i1)),
						Arrays.asList(
								new Check(Arrays.asList(new CallCheck(1, 0),
										new CallCheck(0, 0),
										new CallCheck(0, 0)), null),
								new Check(Arrays.asList(new CallCheck(1, 0),
										new CallCheck(1, 1),
										new CallCheck(0, 1)), null),
								new Check(Arrays.asList(new CallCheck(1, 0),
										new CallCheck(2, 2),
										new CallCheck(0, 2)), null)) },
				{
						Arrays.asList(new Argument(f1, nl),
								new Argument(f1, i2), new Argument(nl, i1)),
						Arrays.asList(
								new Check(Arrays.asList(new CallCheck(1, 0),
										new CallCheck(1, 0),
										new CallCheck(0, 0)), null),
								new Check(Arrays.asList(new CallCheck(2, 0),
										new CallCheck(2, 2),
										new CallCheck(0, 0)), null),
								new Check(Arrays.asList(new CallCheck(2, 0),
										new CallCheck(2, 2),
										new CallCheck(0, 1)), null)) },
				{
						Arrays.asList(new Argument(f1, nl),
								new Argument(f1, i1), new Argument(nl, i1)),
						Arrays.asList(
								new Check(Arrays.asList(new CallCheck(1, 0),
										new CallCheck(1, 0),
										new CallCheck(0, 0)), null),
								new Check(Arrays.asList(new CallCheck(2, 0),
										new CallCheck(2, 2),
										new CallCheck(0, 2)), null),
								new Check(Arrays.asList(new CallCheck(3, 0),
										new CallCheck(3, 3),
										new CallCheck(0, 3)), null)) },
				{
						Arrays.asList(new Argument(f1, i1),
								new Argument(nl, i1), new Argument(nl, i2)),
						Arrays.asList(
								new Check(Arrays.asList(new CallCheck(1, 1),
										new CallCheck(0, 1),
										new CallCheck(0, 0)), null),
								new Check(Arrays.asList(new CallCheck(2, 2),
										new CallCheck(0, 2),
										new CallCheck(0, 0)), null),
								new Check(Arrays.asList(new CallCheck(2, 2),
										new CallCheck(0, 2),
										new CallCheck(0, 1)), null)) } };

		return Arrays.asList(data);
	}

	public MappingDoclogPersistenceParametrizedTest(List<Argument> calls,
			List<Check> checks) {
		int numCalls = calls.size();
		assertEquals(numCalls, checks.size());
		for (Check check : checks) {
			assertEquals(numCalls, check.postWriteCallNumRecords.size());
		}
		this.arguments.addAll(calls);
		this.checks.addAll(checks);
	}

	@Rule
	public final TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Test
	public void test() throws FileNotFoundException, DoclogPersistenceException {
		File dir = temporaryFolder.getRoot();
		MappingDoclogPersistence persistence = new MappingDoclogPersistence(dir);

		/*
		 * check if all arguments don't have any records link to them
		 */
		List<CallCheck> callChecks = new ArrayList<CallCheck>();
		for (int i = 0; i < arguments.size(); i++)
			callChecks.add(new CallCheck(0, 0));
		new Check(callChecks, null).check(persistence, arguments);

		for (int i = 0; i < arguments.size(); i++) {
			// run call and check
			checks.get(i).runAndCheck(persistence, arguments, i);

			// check again with other instances to check if changes are
			// persistent
			MappingDoclogPersistence persistence2 = new MappingDoclogPersistence(
					dir);
			checks.get(i).check(persistence2, arguments);
		}

	}
}
