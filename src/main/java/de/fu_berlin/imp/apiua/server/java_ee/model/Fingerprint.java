package de.fu_berlin.imp.apiua.server.java_ee.model;

import java.util.Comparator;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.collections.comparators.NullComparator;
import org.apache.commons.lang.ArrayUtils;

@XmlRootElement(namespace = "de.fu_berlin.imp.apiua.server.java_ee")
public class Fingerprint implements IIdentifier {
	public static final Pattern PATTERN = Pattern.compile("^![A-Za-z\\d]+$");

	public static final String[] invalidNames = new String[] { "!null",
			"!undefined", "!error", "!exception", "!invalid", "!id",
			"!fingerprint" };

	public static final boolean isValid(String fingerprint) {
		if (fingerprint == null
				|| ArrayUtils.contains(invalidNames, fingerprint.toLowerCase())) {
			return false;
		}
		return PATTERN.matcher(fingerprint).find();
	}

	private static final NullComparator COMPARATOR = new NullComparator(
			new Comparator<Fingerprint>() {
				@Override
				public int compare(Fingerprint fingerprint1,
						Fingerprint fingerprint2) {
					return fingerprint1.getIdentifier().compareTo(
							fingerprint2.getIdentifier());
				}
			});

	private String fingerprint;

	@SuppressWarnings("unused")
	private Fingerprint() {
		this.fingerprint = null;
	}

	public Fingerprint(String fingerprint) {
		super();
		if (!isValid(fingerprint)) {
			throw new IllegalArgumentException(
					Fingerprint.class.getSimpleName() + " " + fingerprint
							+ " must only contain alphanumeric characters");
		}
		this.fingerprint = fingerprint;
	}

	@Override
	@XmlAttribute
	public String getIdentifier() {
		return this.fingerprint;
	}

	@Override
	public int compareTo(Object obj) {
		return COMPARATOR.compare(this,
				obj instanceof Fingerprint ? (Fingerprint) obj : null);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((this.fingerprint == null) ? 0 : this.fingerprint.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		return this.compareTo(obj) == 0;
	}

	@Override
	public String toString() {
		return this.getIdentifier();
	}

}
