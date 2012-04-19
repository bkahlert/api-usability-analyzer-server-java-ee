package de.fu_berlin.imp.seqan.usability_analyzer.srv.model;

import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = "de.fu_berlin.imp.seqan.usability_analyzer.srv")
public class ID implements Comparable<ID> {

	public static final Pattern PATTERN = Pattern.compile("^[A-Za-z\\d]+$");

	public static final boolean isValid(String id) {
		if (id == null)
			return false;
		return PATTERN.matcher(id).find();
	}

	private String id;

	public ID(String id) {
		super();
		if (!isValid(id))
			throw new InvalidParameterException(ID.class.getSimpleName()
					+ " must only contain alphanumeric characters");
		this.id = id;
	}

	@Override
	public String toString() {
		return this.id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		ID other = (ID) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public int compareTo(ID id) {
		return this.toString().compareTo(id.toString());
	}
}