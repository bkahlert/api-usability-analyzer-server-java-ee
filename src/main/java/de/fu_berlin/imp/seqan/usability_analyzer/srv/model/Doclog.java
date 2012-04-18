package de.fu_berlin.imp.seqan.usability_analyzer.srv.model;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(namespace = "de.fu_berlin.imp.seqan.usability_analyzer.srv")
public class Doclog extends ArrayList<DoclogRecord> {

	private static final long serialVersionUID = -6095442166531252268L;

	public static final Pattern ID_FILE_PATTERN = Pattern
			.compile("^([A-Za-z\\d]+)\\.doclog$");
	public static final Pattern FINGERPRINT_FILE_PATTERN = Pattern
			.compile("^(![A-Za-z\\d]+)\\.doclog$");

	public static Object getKey(File file) {
		Object key = getId(file);
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
		try {
			FileInputStream fstream = new FileInputStream(file);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

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
			in.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		return null;
	}

	private ID id;
	private Fingerprint fingerprint;
	private Token token;

	public Doclog() {
		super();
	}

	public Doclog(Object key) {
		this();
		if (key instanceof ID) {
			this.id = (ID) key;
			this.fingerprint = null;
		} else if (key instanceof Fingerprint) {
			this.id = null;
			this.fingerprint = (Fingerprint) key;
		} else {
			throw new InvalidParameterException(key + " was not of type "
					+ ID.class.getSimpleName() + " or "
					+ Fingerprint.class.getSimpleName());
		}
	}

	@XmlTransient
	public Object getKey() {
		if (id != null)
			return id;
		return fingerprint;
	}

	@XmlAttribute
	public ID getId() {
		return id;
	}

	public void setId(ID id) {
		this.id = id;
	}

	@XmlAttribute
	public Fingerprint getFingerprint() {
		return fingerprint;
	}

	public void setFingerprint(Fingerprint fingerprint) {
		this.fingerprint = fingerprint;
	}

	public Token getToken() {
		return token;
	}

	public void setToken(Token token) {
		this.token = token;
	}

	@XmlElement(name = "doclogRecord")
	public List<DoclogRecord> getDoclogRecords() {
		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(Doclog.class.getSimpleName() + "(");
		if (id != null)
			sb.append(ID.class.getSimpleName() + ": " + id);
		else
			sb.append(Fingerprint.class.getSimpleName() + ": " + fingerprint);
		sb.append("; count: " + size() + ")");
		return sb.toString();
	}

}
