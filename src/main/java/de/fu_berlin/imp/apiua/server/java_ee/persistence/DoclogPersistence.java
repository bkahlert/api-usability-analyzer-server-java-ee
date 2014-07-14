package de.fu_berlin.imp.apiua.server.java_ee.persistence;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import de.fu_berlin.imp.apiua.server.java_ee.model.Doclog;
import de.fu_berlin.imp.apiua.server.java_ee.model.DoclogRecord;
import de.fu_berlin.imp.apiua.server.java_ee.model.Fingerprint;
import de.fu_berlin.imp.apiua.server.java_ee.model.ID;
import de.fu_berlin.imp.apiua.server.java_ee.model.IIdentifier;

public class DoclogPersistence {

	private static final Logger LOGGER = Logger
			.getLogger(DoclogPersistence.class);

	public class DoclogPersistenceException extends Exception {
		private static final long serialVersionUID = 1L;

		public DoclogPersistenceException(String message, Throwable e) {
			super(message);
		}
	}

	protected File directory;

	public DoclogPersistence(File directory) {
		if (directory == null) {
			throw new IllegalArgumentException("Directory must not be null");
		}
		if (!directory.isDirectory()) {
			throw new IllegalArgumentException(directory.getAbsolutePath()
					+ " is no valid directory.");
		}
		if (!directory.canWrite()) {
			throw new IllegalArgumentException("Can't write to "
					+ directory.getAbsolutePath());
		}
		this.directory = directory;
	}

	private File getDoclogLocation(IIdentifier identifier) {
		return new File(directory, identifier.toString() + ".doclog");
	}

	protected DoclogFile getDoclogFile(IIdentifier identifier)
			throws IOException {
		DoclogFile doclogFile = new DoclogFile(getDoclogLocation(identifier));
		return doclogFile;
	}

	protected ReadLock getReadLock(IIdentifier identifier) throws IOException {
		return this.getDoclogFile(identifier).getReadLock();
	}

	protected WriteLock getWriteLock(IIdentifier identifier) throws IOException {
		return this.getDoclogFile(identifier).getWriteLock();
	}

	protected void write(IIdentifier identifier, DoclogRecord doclogRecord)
			throws DoclogPersistenceException {
		if (identifier == null)
			throw new IllegalArgumentException(
					IIdentifier.class.getSimpleName() + " must not be null");
		if (doclogRecord == null)
			throw new IllegalArgumentException(
					DoclogRecord.class.getSimpleName() + " must not be null");
		try {
			DoclogFile doclogFile = getDoclogFile(identifier);
			doclogFile.write(doclogRecord);
		} catch (IOException e) {
			throw new DoclogPersistenceException("Could not write "
					+ DoclogRecord.class.getSimpleName(), e);
		}
	}

	protected long getNumRecords(IIdentifier identifier)
			throws DoclogPersistenceException {
		if (identifier == null)
			return 0l;
		try {
			DoclogFile doclogFile = getDoclogFile(identifier);
			return doclogFile.getNumRecords();
		} catch (IOException e) {
			throw new DoclogPersistenceException(
					"Could not calculate the number of included "
							+ DoclogRecord.class.getSimpleName() + "s of "
							+ identifier, e);
		}
	}

	protected DoclogRecord getRecord(IIdentifier identifier, int index)
			throws DoclogPersistenceException {
		if (identifier == null)
			return null;
		try {
			DoclogFile doclogFile = getDoclogFile(identifier);
			return doclogFile.getRecord(index);
		} catch (IOException e) {
			throw new DoclogPersistenceException("Could not return the "
					+ index + "-th " + DoclogRecord.class.getSimpleName()
					+ " of " + identifier, e);
		}
	}

	protected boolean deleteRecord(IIdentifier identifier, int index)
			throws DoclogPersistenceException {
		if (identifier == null)
			return false;
		try {
			DoclogFile doclogFile = getDoclogFile(identifier);
			return doclogFile.deleteRecord(index);
		} catch (IOException e) {
			throw new DoclogPersistenceException("Could not delete the "
					+ index + "-th " + DoclogRecord.class.getSimpleName()
					+ " of " + identifier, e);
		}
	}

	protected DoclogRecord[] getRecords(IIdentifier identifier)
			throws DoclogPersistenceException {
		if (identifier == null)
			return new DoclogRecord[0];
		try {
			DoclogFile doclogFile = getDoclogFile(identifier);
			return doclogFile.getRecords();
		} catch (IOException e) {
			throw new DoclogPersistenceException(
					"Could not return the "
							+ DoclogRecord.class.getSimpleName() + "s of "
							+ identifier, e);
		}
	}

	public IIdentifier[] getIdentifiers() {
		List<IIdentifier> identifiers = new ArrayList<IIdentifier>();
		for (String name : this.directory.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return FilenameUtils.getExtension(name).equals("doclog");
			}
		})) {
			IIdentifier identifier;
			String basename = FilenameUtils.getBaseName(name);
			if (ID.isValid(basename))
				identifier = new ID(basename);
			else if (Fingerprint.isValid(basename))
				identifier = new Fingerprint(basename);
			else {
				LOGGER.warn("File in " + this.directory
						+ " found that has no valid "
						+ IIdentifier.class.getSimpleName() + ": " + name);
				continue;
			}
			identifiers.add(identifier);
		}
		return identifiers.toArray(new IIdentifier[0]);
	}

	protected Doclog getDoclog(IIdentifier identifier)
			throws DoclogPersistenceException {
		Doclog doclog = new Doclog(identifier);
		doclog.addAll(Arrays.asList(getRecords(identifier)));
		return doclog;
	}
}
