package de.fu_berlin.imp.seqan.usability_analyzer.srv.model;

import java.util.Comparator;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.collections.comparators.NullComparator;
import org.apache.commons.lang.ArrayUtils;

@XmlRootElement(namespace = "de.fu_berlin.imp.seqan.usability_analyzer.srv")
public class ID implements IIdentifier {

	public static final Pattern PATTERN = Pattern.compile("^[A-Za-z\\d]+$");

	public static final String[] invalidNames = new String[] { "null",
			"undefined", "error", "exception", "invalid", "id", "fingerprint" };

	public static final boolean isValid(String id) {
		if (id == null || ArrayUtils.contains(invalidNames, id.toLowerCase())) {
			return false;
		}
		return PATTERN.matcher(id).find();
	}

	private static final NullComparator COMPARATOR = new NullComparator(
			new Comparator<ID>() {
				@Override
				public int compare(ID id1, ID id2) {
					return id1.getIdentifier().compareTo(id2.getIdentifier());
				}
			});

	private String id;

	@SuppressWarnings("unused")
	private ID() {
		this.id = null;
	}

	public ID(String id) {
		super();
		if (!isValid(id)) {
			throw new IllegalArgumentException(ID.class.getSimpleName()
					+ " must only contain alphanumeric characters");
		}
		this.id = id;
	}

	@Override
	@XmlAttribute
	public String getIdentifier() {
		return this.id;
	}

	@Override
	public int compareTo(Object obj) {
		return COMPARATOR.compare(this, obj instanceof ID ? (ID) obj : null);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
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
