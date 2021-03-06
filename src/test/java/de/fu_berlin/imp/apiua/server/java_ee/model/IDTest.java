package de.fu_berlin.imp.apiua.server.java_ee.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.fu_berlin.imp.apiua.server.java_ee.model.Fingerprint;
import de.fu_berlin.imp.apiua.server.java_ee.model.ID;

@RunWith(value = Parameterized.class)
public class IDTest {

	@Parameters(name = "{0}")
	public static Collection<Object[]> data() {
		Object[][] data = new Object[][] {
				{ null, false },
				{ "", false },
				{ "null", false },
				{ "undefined", false },
				{ "error", false },
				{ "exception", false },
				{ "invalid", false },
				{ "id", false },
				{ "fingerprint", false },
				{ "Null", false },
				{ "Undefined", false },
				{ "Error", false },
				{ "Exception", false },
				{ "Invalid", false },
				{ "Id", false },
				{ "Fingerprint", false },
				{ "id", false },
				{ "a_", false },
				{ "a$", false },
				{ "!a", false },
				{ "a%", false },
				{ "a", false },
				{ "fingerprint", false },
				{ "a", true },
				{ "ab", true },
				{ "IamAID20", true },
				{
						"FXzQgQw33Jvp0wr596P25zQ26cJRmbvw1VmwENEHo59uvHwjJfjLa1mywhiuLoPvJ6rXM3gBfKu4Jd5x1m73ushCLmnDqT6cGKyqev0t6yF1OcwwN46xqdHIk8ELNRCgZKOAZq0c6Wh4",
						true } };
		return Arrays.asList(data);
	}

	private String input;
	private boolean expectedValid;

	public IDTest(String input, boolean expectedValid) {
		this.input = input;
		this.expectedValid = expectedValid;
	}

	@Test
	public void test() {
		assertEquals(expectedValid, ID.isValid(input));

		if (expectedValid) {
			assertFalse(Fingerprint.isValid(input));
			assertNotNull(new ID(input));
		} else {
			try {
				new ID(input);
				fail(input + " was not expected to be a valid "
						+ ID.class.getSimpleName());
			} catch (IllegalArgumentException e) {
				assertTrue(true);
			}
		}
	}
}
