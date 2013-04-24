package de.fu_berlin.imp.seqan.usability_analyzer.srv.persistence;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.Doclog;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.DoclogRecord;

/**
 * Instances of this class loosely wrap {@link File}s and allows to treat them
 * as containers for {@link DoclogRecord}s.
 * <p>
 * Multiple {@link DoclogFile}s may work this the same underlying file since
 * this implementation synchronizes accesses to this file.
 * 
 * @author bkahlert
 * 
 */
public class DoclogFile implements IDoclogFile {

	private static final Logger LOGGER = Logger.getLogger(DoclogFile.class);

	private static ConcurrentMap<File, ReentrantReadWriteLock> LOCKS = new ConcurrentHashMap<File, ReentrantReadWriteLock>();

	private static ReentrantReadWriteLock getLock(File file) {
		LOCKS.putIfAbsent(file, new ReentrantReadWriteLock());
		return LOCKS.get(file);
	}

	private File file;

	public DoclogFile(File file) throws IOException {
		if (file == null)
			throw new IllegalArgumentException("file must not be null");
		if (file.exists()) {
			if (!file.isFile()) {
				throw new IllegalArgumentException(file + " is no "
						+ File.class.getSimpleName());
			}
			if (!file.canRead())
				throw new IllegalArgumentException("Can't read "
						+ file.getAbsolutePath());
			if (!file.canWrite())
				throw new IllegalArgumentException("Can't write "
						+ file.getAbsolutePath());
		}
		this.file = file;
	}

	@Override
	public File getFile() {
		return this.file;
	}

	@Override
	public ReadLock getReadLock() {
		return getLock(file).readLock();
	}

	@Override
	public WriteLock getWriteLock() {
		return getLock(file).writeLock();
	}

	/**
	 * @see <a
	 *      href="http://stackoverflow.com/questions/453018/number-of-lines-in-a-file-in-java">http://stackoverflow.com/questions/453018/number-of-lines-in-a-file-in-java</a>
	 */
	@Override
	public long getNumRecords() throws IOException {
		if (!this.file.exists())
			return 0;

		getReadLock().lock();
		InputStream is = new BufferedInputStream(new FileInputStream(file));
		try {
			byte[] c = new byte[1024];
			long count = 0;
			int readChars = 0;
			boolean empty = true;
			while ((readChars = is.read(c)) != -1) {
				empty = false;
				for (int i = 0; i < readChars; ++i) {
					if (c[i] == '\n') {
						++count;
					}
				}
			}
			return (count == 0 && !empty) ? 1 : count;
		} finally {
			is.close();
			getReadLock().unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fu_berlin.imp.seqan.usability_analyzer.srv.persistence.IDoclogRecord
	 * #write(de.fu_berlin.imp.seqan.usability_analyzer.srv.model.DoclogRecord)
	 */
	@Override
	public void write(DoclogRecord doclogRecord) throws IOException {
		if (doclogRecord == null)
			throw new IllegalArgumentException();

		getWriteLock().lock();
		PrintWriter pw = null;
		try {
			if (!this.file.exists())
				this.file.createNewFile();
			pw = new PrintWriter(new FileWriter(file, true));
			pw.println(doclogRecord.toString());
			pw.flush();
		} finally {
			if (pw != null) {
				pw.close();
			}
			getWriteLock().unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fu_berlin.imp.seqan.usability_analyzer.srv.persistence.IDoclogRecord
	 * #getRecords()
	 */
	@Override
	public DoclogRecord[] getRecords() throws IOException {
		if (!this.file.exists())
			return new DoclogRecord[0];
		getReadLock().lock();
		try {
			List<String> lines = (List<String>) FileUtils.readLines(file,
					"UTF-8");
			List<DoclogRecord> doclogRecords = new ArrayList<DoclogRecord>();
			for (String line : lines) {
				DoclogRecord doclogRecord = new DoclogRecord(line);
				doclogRecords.add(doclogRecord);
			}
			return doclogRecords.toArray(new DoclogRecord[0]);
		} finally {
			getReadLock().unlock();
		}
	}

	@Override
	public DoclogRecord getRecord(int index) throws IOException {
		if (!this.file.exists())
			return null;

		BufferedReader br = new BufferedReader(new FileReader(file));
		int numRow = 0;
		try {
			String line = null;
			while ((line = br.readLine()) != null) {
				if (numRow == index) {
					try {
						return new DoclogRecord(line);
					} catch (IllegalArgumentException e) {
						LOGGER.error("Invalid " + DoclogRecord.class
								+ " encountered: " + line, e);
					}
				}
				++numRow;
			}
		} finally {
			br.close();
		}
		if (index < 0 && numRow >= -index)
			return getRecord(numRow + index);
		return null;
	}

	@Override
	public boolean deleteRecord(int index) throws IOException {
		if (!this.file.exists())
			return false;

		getWriteLock().lock();

		try {
			File tempFile = new File(file.getAbsolutePath() + ".tmp");

			BufferedReader br = new BufferedReader(new FileReader(file));
			PrintWriter pw = new PrintWriter(new FileWriter(tempFile));

			int numRow = 0;
			String line = null;
			boolean skippedLine = false;
			while ((line = br.readLine()) != null) {
				if (numRow != index) {
					pw.println(line);
					pw.flush();
				} else {
					skippedLine = true;
				}
				++numRow;
			}
			pw.close();
			br.close();

			if (!file.delete()) {
				throw new IOException(file + " could not be deleted.");
			}

			if (!tempFile.renameTo(file)) {
				throw new IOException(tempFile
						+ " could not be renamed back to " + file + ".");
			}

			if (!skippedLine) {
				if (index < 0 && numRow >= -index) {
					return deleteRecord(numRow + index);
				}

				LOGGER.warn("Line " + index + " of " + file
						+ " could not be removed since it only contains "
						+ numRow + " lines.");
				return false;
			}

			if (numRow == 1 && skippedLine) {
				if (!file.delete()) {
					throw new IOException(file
							+ " is now empty but could not be deleted.");
				}
			}
		} catch (IOException e) {
			LOGGER.error(
					"Error deleting a line from "
							+ Doclog.class.getSimpleName() + " " + file, e);
			return false;
		} finally {
			getWriteLock().unlock();
		}
		return true;
	}

	@Override
	public void merge(IDoclogFile sourceDoclogFile) throws IOException {
		if (!sourceDoclogFile.getFile().exists())
			return;

		try {
			this.getWriteLock().lock();
			sourceDoclogFile.getWriteLock().lock();

			if (!this.file.exists())
				this.file.createNewFile();

			BufferedReader srcReader = new BufferedReader(new FileReader(
					this.file));
			PrintWriter destAppender = new PrintWriter(new FileWriter(
					sourceDoclogFile.getFile(), true));

			String line = null;
			while ((line = srcReader.readLine()) != null) {
				destAppender.println(line);
				destAppender.flush();
			}
			destAppender.close();
			srcReader.close();

			if (!this.file.delete()) {
				LOGGER.error(this.file + " could not be deleted.");
				return;
			}

			FileUtils.moveFile(sourceDoclogFile.getFile(), this.file);

			LOGGER.debug("Merged " + sourceDoclogFile + " into " + this);
		} finally {
			sourceDoclogFile.getWriteLock().unlock();
			this.getWriteLock().unlock();
		}
	}

	@Override
	public String toString() {
		return DoclogRecord.class.getSimpleName() + " (" + file.toString()
				+ ")";
	}
}
