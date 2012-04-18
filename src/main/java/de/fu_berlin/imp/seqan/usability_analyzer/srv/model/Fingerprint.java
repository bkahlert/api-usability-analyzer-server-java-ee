package de.fu_berlin.imp.seqan.usability_analyzer.srv.model;

import java.util.regex.Pattern;

public class Fingerprint implements Comparable<Fingerprint> {

	public static final Pattern PATTERN = Pattern.compile("^![A-Za-z\\d]+$");

	public static final boolean isValid(String id) {
		if (id == null)
			return false;
		return PATTERN.matcher(id).find();
	}

	private String fingerprint;

	public Fingerprint(String fingerprint) {
		super();
		if (!isValid(fingerprint))
			throw new InvalidParameterException(
					Fingerprint.class.getSimpleName()
							+ " must only contain alphanumeric characters");
		this.fingerprint = fingerprint;
	}

	@Override
	public String toString() {
		return this.fingerprint;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((fingerprint == null) ? 0 : fingerprint.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Fingerprint other = (Fingerprint) obj;
		if (fingerprint == null) {
			if (other.fingerprint != null)
				return false;
		} else if (!fingerprint.equals(other.fingerprint))
			return false;
		return true;
	}

	public int compareTo(Fingerprint fingerprint) {
		return this.toString().compareTo(fingerprint.toString());
	}

}
