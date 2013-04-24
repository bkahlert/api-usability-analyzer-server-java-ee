package de.fu_berlin.imp.seqan.usability_analyzer.srv.utils;

import java.util.concurrent.Callable;

public class AsyncTester {
	private Thread thread;
	private volatile Error error;
	private volatile Exception runtimeExc;

	public AsyncTester(final Callable<Void> callable) {
		thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					callable.call();
				} catch (Error e) {
					error = e;
				} catch (Exception e) {
					runtimeExc = e;
				}
			}
		});
	}

	public void start() {
		thread.start();
	}

	public void join() throws Exception {
		thread.join();
		if (error != null)
			throw error;
		if (runtimeExc != null)
			throw runtimeExc;
	}
}
