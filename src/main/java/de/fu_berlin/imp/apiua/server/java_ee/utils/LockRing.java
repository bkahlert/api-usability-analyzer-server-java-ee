package de.fu_berlin.imp.apiua.server.java_ee.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import org.apache.commons.collections.iterators.ReverseListIterator;
import org.apache.log4j.Logger;

/**
 * {@link Lock}s can be added to this {@link LockRing} and be released with a
 * simple call.
 * 
 * @author bkahlert
 * 
 */
public class LockRing {

	private static final Logger LOGGER = Logger.getLogger(LockRing.class);

	private List<Lock> locks;
	private Map<Lock, String> lockCaptions;

	public LockRing() {
		this.locks = new ArrayList<Lock>();
		lockCaptions = new HashMap<Lock, String>();
	}

	public void addAndLock(Lock lock, String caption) {
		if (lock == null)
			throw new IllegalArgumentException();
		this.locks.add(lock);
		this.lockCaptions.put(lock, caption);
		LOGGER.debug("Locking " + caption);
		lock.lock();
		LOGGER.debug("Locked " + caption);
	}

	public void unlockAll() {
		ReverseListIterator it = new ReverseListIterator(locks);
		while (it.hasNext()) {
			Lock lock = (Lock) it.next();
			String caption = lockCaptions.get(lock);
			try {
				LOGGER.debug("Unlocking " + caption);
				lock.unlock();
				LOGGER.debug("Unlocked " + caption);
			} catch (Exception e) {
				LOGGER.error("Error unlocking "
						+ lock.getClass().getSimpleName() + " " + lock, e);
			}
		}
	}
}
