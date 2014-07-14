package de.fu_berlin.imp.seqan.usability_analyzer.srv.persistence;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import javax.xml.bind.JAXBException;

import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.DoclogRecord;

/**
 * Instances of this interface can save {@link DoclogRecord}s.
 * 
 * @author bkahlert
 * 
 */
public interface IDoclogFile {

	ReadLock getReadLock();

	WriteLock getWriteLock();

	File getFile();

	/**
	 * Returns the number of {@link DoclogRecord} contained in this
	 * {@link IDoclogFile}.
	 * <p>
	 * 
	 * @return
	 * @throws IOException
	 * @ThreadSafe
	 */
	public long getNumRecords() throws IOException;

	/**
	 * Adds the specified {@link DoclogRecord} to this {@link IDoclogFile}.
	 * 
	 * @param doclogRecord
	 * @throws IOException
	 * @ThreadSafe
	 */
	public void write(DoclogRecord doclogRecord) throws IOException;

	/**
	 * Returns all {@link DoclogRecord} contained in this {@link IDoclogFile}.
	 * 
	 * @return
	 * @throws IOException
	 * @ThreadSafe
	 */
	public DoclogRecord[] getRecords() throws IOException;

	/**
	 * Returns a {@link DoclogRecord} from this {@link IDoclogFile}.
	 * 
	 * @param index
	 *            which record to return; if negative the records are counted
	 *            from the most recent one (e.g. -2 would mean the second recent
	 *            one)
	 * @return
	 * @throws IOException
	 * @throws JAXBException
	 */
	public DoclogRecord getRecord(int index) throws IOException;

	/**
	 * Deletes the index-th {@link DoclogRecord} from this {@link IDoclogFile}.
	 * 
	 * @param index
	 * @throws IOException
	 * @return true the {@link DoclogRecord} could be deleted; false otherwise
	 */
	public boolean deleteRecord(int index) throws IOException;

	/**
	 * Reads the given {@link IDoclogFile}'s contents, adds it to this
	 * {@link IDoclogFile} and deletes the given {@link IDoclogFile}.
	 * 
	 * @param doclogFile
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @ThreadSafe
	 */
	public void merge(IDoclogFile doclogFile) throws FileNotFoundException,
			IOException;

}