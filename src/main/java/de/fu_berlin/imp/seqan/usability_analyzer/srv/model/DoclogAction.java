package de.fu_berlin.imp.seqan.usability_analyzer.srv.model;

public enum DoclogAction {
	READY, UNLOAD, SCROLL, LINK, SURVEY, FOCUS, BLUR, TYPING, RESIZE, UNKNOWN;

	public static DoclogAction getByString(String doclogActionString) {
		for (DoclogAction doclogAction : DoclogAction.values()) {
			if (doclogAction.toString().equalsIgnoreCase(doclogActionString))
				return doclogAction;
		}
		return UNKNOWN;
	}
}
