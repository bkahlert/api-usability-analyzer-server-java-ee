package de.fu_berlin.imp.seqan.usability_analyzer.srv.model;

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

@RunWith(value = Parameterized.class)
public class FingerprintTest {

	@Parameters(name = "{0}")
	public static Collection<Object[]> data() {
		Object[][] data = new Object[][] {
				{ null, false },
				{ "", false },
				{ "!", false },
				{ "!null", false },
				{ "!undefined", false },
				{ "!error", false },
				{ "!exception", false },
				{ "!invalid", false },
				{ "!id", false },
				{ "!fingerprint", false },
				{ "!Null", false },
				{ "!Undefined", false },
				{ "!Error", false },
				{ "!Exception", false },
				{ "!Invalid", false },
				{ "!Id", false },
				{ "!fingerprint", false },
				{ "!a_", false },
				{ "!a$", false },
				{ "!!a", false },
				{ "!a%", false },
				{ "!a", false },
				{ "!a", true },
				{ "!ab", true },
				{ "IamAFingerprint20", false },
				{ "!IamAFingerprint20", true },
				{
						"!FXzQgQw33Jvp0wr596P25zQ26cJRmbvw1VmwENEHo59uvHwjJfjLa1mywhiuLoPvJ6rXM3gBfKu4Jd5x1m73ushCLmnDqT6cGKyqev0t6yF1OcwwN46xqdHIk8ELNRCgZKOAZq0c6Wh4",
						true } };
		return Arrays.asList(data);
	}

	private String input;
	private boolean expectedValid;

	public FingerprintTest(String input, boolean expectedValid) {
		this.input = input;
		this.expectedValid = expectedValid;
	}

	@Test
	public void test() {
		assertEquals(expectedValid, Fingerprint.isValid(input));

		if (expectedValid) {
			assertFalse(ID.isValid(input));
			assertNotNull(new Fingerprint(input));
		} else {
			try {
				new Fingerprint(input);
				fail(input + " was not expected to be a valid "
						+ Fingerprint.class.getSimpleName());
			} catch (IllegalArgumentException e) {
				assertTrue(true);
			}
		}
	}
}
