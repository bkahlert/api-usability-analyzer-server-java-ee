package de.fu_berlin.imp.seqan.usability_analyzer.srv.model;

/**
 * This factory can construct {@link IIdentifier}s.
 * 
 * @author bkahlert
 * 
 */
public class IdentifierFactory {
	/**
	 * Constructs a new {@link IIdentifier} from a string.
	 * 
	 * @param string
	 * @return null if the string does not present a valid {@link IIdentifier}.
	 */
	public static IIdentifier createFrom(String string) {
		if (ID.isValid(string)) {
			return new ID(string);
		}
		if (Fingerprint.isValid(string)) {
			return new Fingerprint(string);
		}
		return null;
	}
}
