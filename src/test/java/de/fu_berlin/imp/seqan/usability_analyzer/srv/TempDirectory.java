package de.fu_berlin.imp.seqan.usability_analyzer.srv;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.commons.lang.math.RandomUtils;

public class TempDirectory extends File {

	private static final long serialVersionUID = 1887091568726734763L;

	public TempDirectory() throws FileNotFoundException {
		this(RandomUtils.nextLong() + "");
	}

	public TempDirectory(String prefix) throws FileNotFoundException {
		super(new File(System.getProperty("java.io.tmpdir"), prefix + "-"
				+ Long.toString(System.nanoTime())).getAbsolutePath());

		this.mkdirs();
		this.deleteOnExit();
	}

}
