package de.fu_berlin.imp.apiua.server.java_ee.model;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = "de.fu_berlin.imp.apiua.server.java_ee")
public class Doclog extends ArrayList<DoclogRecord> {

	private static final long serialVersionUID = -6095442166531252268L;

	public static final Pattern ID_FILE_PATTERN = Pattern
			.compile("^([A-Za-z\\d]+)\\.doclog$");
	public static final Pattern FINGERPRINT_FILE_PATTERN = Pattern
			.compile("^(![A-Za-z\\d]+)\\.doclog$");

	public static IIdentifier getIdentifier(File file) {
		IIdentifier key = getId(file);
		if (key != null)
			return key;
		key = getFingerprint(file);
		return key;
	}

	public static ID getId(File file) {
		ID id = null;
		Matcher matcher = ID_FILE_PATTERN.matcher(file.getName());
		if (matcher.find())
			id = new ID(matcher.group(1));
		return id;
	}

	public static Fingerprint getFingerprint(File file) {
		Fingerprint fingerprint = null;
		Matcher matcher = FINGERPRINT_FILE_PATTERN.matcher(file.getName());
		if (matcher.find())
			fingerprint = new Fingerprint(matcher.group(1));
		return fingerprint;
	}

	public static Token getToken(File file) {
		Pattern surveyEntryPattern = Pattern
				.compile("\tsurvey-([A-Za-z0-9]+)\t"); // action type
		Pattern surveyQueryPattern = Pattern
				.compile("[\\?|&]token=([A-Za-z0-9]+)"); // token in url

		DataInputStream in = null;
		BufferedReader br = null;
		try {
			FileInputStream fstream = new FileInputStream(file);
			in = new DataInputStream(fstream);
			br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				Matcher surveyEntryMatcher = surveyEntryPattern
						.matcher(strLine);
				if (surveyEntryMatcher.find())
					return new Token(surveyEntryMatcher.group(1));

				Matcher surveyQueryMatcher = surveyQueryPattern
						.matcher(strLine);
				if (surveyQueryMatcher.find())
					return new Token(surveyQueryMatcher.group(1));
			}
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		} finally {
			try {
				br.close();
				in.close();
			} catch (IOException e) {
			}
		}
		return null;
	}

	private IIdentifier identifier;

	@SuppressWarnings("unused")
	private Doclog() {
		super();
	}

	public Doclog(IIdentifier identifier) {
		if (identifier == null)
			throw new IllegalArgumentException("identifier must not be null");
		this.identifier = identifier;
	}

	@XmlAttribute
	public IIdentifier getIdentifier() {
		return identifier;
	}

	public void setIdentifier(IIdentifier id) {
		this.identifier = id;
	}

	@XmlElement(name = "doclogRecord")
	public List<DoclogRecord> getDoclogRecords() {
		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(Doclog.class.getSimpleName() + "(");
		sb.append(ID.class.getSimpleName() + ": " + identifier);
		sb.append("; count: " + size() + ")");
		return sb.toString();
	}

}
