package de.fu_berlin.imp.apiua.server.java_ee.persistence;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import de.fu_berlin.imp.apiua.server.java_ee.model.Doclog;
import de.fu_berlin.imp.apiua.server.java_ee.model.DoclogKeyMap;
import de.fu_berlin.imp.apiua.server.java_ee.model.DoclogRecord;
import de.fu_berlin.imp.apiua.server.java_ee.model.Fingerprint;
import de.fu_berlin.imp.apiua.server.java_ee.model.FinterprintAlreadyMappedException;
import de.fu_berlin.imp.apiua.server.java_ee.model.ID;
import de.fu_berlin.imp.apiua.server.java_ee.model.IIdentifier;
import de.fu_berlin.imp.apiua.server.java_ee.utils.LockRing;

public class MappingDoclogPersistence extends DoclogPersistence {

	private static final Logger LOGGER = Logger
			.getLogger(MappingDoclogPersistence.class);

	private File doclogKeyMapFile;
	private DoclogKeyMap doclogKeyMap;

	public MappingDoclogPersistence(File directory) {
		super(directory);
		this.doclogKeyMapFile = new File(this.directory, "mapping.xml");
		try {
			this.doclogKeyMap = DoclogKeyMap.load(this.doclogKeyMapFile);
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException("Cannot load "
					+ DoclogKeyMap.class, e);
		}
	}

	public void addMapping(Fingerprint fingerprint, ID id)
			throws JAXBException, IOException,
			FinterprintAlreadyMappedException {
		doclogKeyMap.associate(fingerprint, id);
		saveDoclogKeyMap();
	}

	public void removeMapping(Fingerprint fingerprint) throws JAXBException,
			IOException, FinterprintAlreadyMappedException {
		doclogKeyMap.deassociate(fingerprint);
		saveDoclogKeyMap();
	}

	public ID getMapping(Fingerprint fingerprint) {
		return doclogKeyMap.getID(fingerprint);
	}

	private void saveDoclogKeyMap() throws JAXBException, IOException {
		if (doclogKeyMap != null) {
			doclogKeyMap.save(this.doclogKeyMapFile);
		}
	}

	@Override
	public long getNumRecords(IIdentifier identifier)
			throws DoclogPersistenceException {
		if (identifier instanceof Fingerprint) {
			ID id = getMapping((Fingerprint) identifier);
			if (id != null)
				identifier = id;
		}
		return super.getNumRecords(identifier);
	}

	@Override
	public DoclogRecord getRecord(IIdentifier identifier, int index)
			throws DoclogPersistenceException {
		if (identifier instanceof Fingerprint) {
			ID id = getMapping((Fingerprint) identifier);
			if (id != null)
				identifier = id;
		}
		return super.getRecord(identifier, index);
	}

	@Override
	public boolean deleteRecord(IIdentifier identifier, int index)
			throws DoclogPersistenceException {
		if (identifier instanceof Fingerprint) {
			ID id = getMapping((Fingerprint) identifier);
			if (id != null)
				identifier = id;
		}
		return super.deleteRecord(identifier, index);
	}

	@Override
	public DoclogRecord[] getRecords(IIdentifier identifier)
			throws DoclogPersistenceException {
		if (identifier instanceof Fingerprint) {
			ID id = getMapping((Fingerprint) identifier);
			if (id != null)
				identifier = id;
		}
		return super.getRecords(identifier);
	}

	@Override
	public Doclog getDoclog(IIdentifier identifier)
			throws DoclogPersistenceException {
		if (identifier instanceof Fingerprint) {
			ID id = getMapping((Fingerprint) identifier);
			if (id != null)
				identifier = id;
		}
		return super.getDoclog(identifier);
	}

	/**
	 * Associates an already existing {@link Fingerprint} with a new
	 * {@link Fingerprint}.
	 * <p>
	 * This step will add a mapping between the new {@link Fingerprint} and the
	 * old ones possibly already existing {@link ID} mapping. From this moment
	 * on both {@link Fingerprint}s can be used to add a {@link DoclogRecord}.
	 * 
	 * @param oldFingerprint
	 * @param newFingerprint
	 */
	public void associateFingerprints(Fingerprint oldFingerprint,
			Fingerprint newFingerprint) {
		if (oldFingerprint == null || newFingerprint == null)
			throw new IllegalArgumentException("Both "
					+ Fingerprint.class.getSimpleName() + " must not be null");

		if (oldFingerprint.equals(newFingerprint)) {
			LOGGER.info("Both fingerprints are already the same: "
					+ oldFingerprint);
			return;
		}

		LOGGER.info("Associating from " + oldFingerprint + " to "
				+ newFingerprint);

		final LockRing lockRing = new LockRing();

		ID id = null;
		try {
			lockRing.addAndLock(getWriteLock(oldFingerprint),
					oldFingerprint.toString());
			lockRing.addAndLock(getWriteLock(newFingerprint),
					newFingerprint.toString());

			id = getMapping(oldFingerprint);
			if (id != null) {
				lockRing.addAndLock(getWriteLock(id), id.toString());
				try {
					addMapping(newFingerprint, id);
					removeMapping(oldFingerprint);
					LOGGER.info("Associated from " + oldFingerprint + " to "
							+ newFingerprint);
					return;
				} catch (FinterprintAlreadyMappedException e) {
					LOGGER.error(
							"An attempt to associate "
									+ Fingerprint.class.getSimpleName()
									+ " "
									+ newFingerprint
									+ " ("
									+ ID.class.getSimpleName()
									+ ": "
									+ id
									+ ") with new "
									+ Fingerprint.class.getSimpleName()
									+ " "
									+ newFingerprint
									+ " was made the latter is already mapped to "
									+ ID.class.getSimpleName() + " "
									+ e.getOldID(), e);
				}
			} else {
				getDoclogFile(newFingerprint).merge(
						getDoclogFile(oldFingerprint));
			}
		} catch (JAXBException e) {
			LOGGER.error(e);
		} catch (IOException e) {
			LOGGER.error(e);
		} finally {
			lockRing.unlockAll();
		}
	}

	@Override
	public void write(IIdentifier identifier, DoclogRecord doclogRecord)
			throws DoclogPersistenceException {
		if (identifier instanceof Fingerprint) {
			ID id = getMapping((Fingerprint) identifier);
			if (id != null)
				identifier = id;
		}
		super.write(identifier, doclogRecord);
	}

	public void write(IIdentifier key, ID id, DoclogRecord doclogRecord) {
		if (key == null && id == null)
			throw new IllegalArgumentException(
					"You must provide at least one of the two arguments key and id");
		if (doclogRecord == null)
			throw new IllegalArgumentException(
					DoclogRecord.class.getSimpleName() + " must not be null");

		Fingerprint fingerprint = (key instanceof Fingerprint) ? (Fingerprint) key
				: null;
		if (key instanceof ID) {
			if (id != null && !id.equals(key)) {
				LOGGER.error("Request contained different "
						+ ID.class.getSimpleName() + "s. The second "
						+ ID.class.getSimpleName()
						+ " will be ignored.\nused: " + key + "\nignored: "
						+ id);
			}
			id = (ID) key;
		}

		final LockRing lockRing = new LockRing();

		try {
			synchronized (DoclogPersistence.class) {
				if (fingerprint != null) {
					ID mappedID = getMapping(fingerprint);
					if (mappedID != null) {
						if (id != null && !mappedID.equals(id)) {
							LOGGER.error("Request contained a "
									+ Fingerprint.class.getSimpleName()
									+ " that maps to another "
									+ ID.class.getSimpleName()
									+ " than the provided one:\nMapped "
									+ ID.class.getSimpleName() + ": "
									+ mappedID + "\nProvided "
									+ ID.class.getSimpleName() + ": " + id
									+ "\nUsing the mapped "
									+ ID.class.getSimpleName() + " " + mappedID);
						}
						id = mappedID;
					}
				} else {
					key = id;
				}
				try {
					if (fingerprint != null) {
						lockRing.addAndLock(getWriteLock(fingerprint),
								fingerprint.toString());
					}
					if (id != null) {
						lockRing.addAndLock(getWriteLock(id), id.toString());
					}
				} catch (IOException e) {
					LOGGER.error("Error locking files", e);
					return;
				}
			}

			if (fingerprint != null && id != null) {
				try {
					addMapping(fingerprint, id);
					if (getDoclogFile(fingerprint).getNumRecords() > 0) {
						getDoclogFile(id).merge(getDoclogFile(fingerprint));
					} else {
						getDoclogFile(fingerprint).getFile().delete();
					}
					key = id;
				} catch (FinterprintAlreadyMappedException e) {
					LOGGER.error(
							"Fingerprint already maps to "
									+ Fingerprint.class.getSimpleName()
									+ " pointing to multiple "
									+ ID.class.getSimpleName() + "s detected!",
							e);
					if (id != null) {
						if (key instanceof ID) {
							id = (ID) key;
						} else {
							id = getMapping((Fingerprint) key);
						}
					}
				} catch (JAXBException e) {
					LOGGER.fatal("Can't persists mapping for " + key + " -> "
							+ id, e);
				} catch (IOException e) {
					LOGGER.fatal("Can't persists mapping for " + key + " -> "
							+ id, e);
				}
			}

			try {
				DoclogFile file = getDoclogFile(key);
				file.write(doclogRecord);
				LOGGER.info("Appended " + DoclogRecord.class.getSimpleName()
						+ " to " + Doclog.class.getSimpleName() + " " + key
						+ ": " + doclogRecord);
			} catch (Exception e) {
				LOGGER.error(doclogRecord + " could not be added to " + key);
			}
		} finally {
			lockRing.unlockAll();
		}
	}
}
