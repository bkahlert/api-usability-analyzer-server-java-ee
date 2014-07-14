@XmlJavaTypeAdapters({
		@XmlJavaTypeAdapter(type = IIdentifier.class, value = IdentifierAdapter.class),
		@XmlJavaTypeAdapter(type = ID.class, value = IDAdapter.class),
		@XmlJavaTypeAdapter(type = Fingerprint.class, value = FingerprintAdapter.class),
		@XmlJavaTypeAdapter(type = Token.class, value = TokenAdapter.class),
		@XmlJavaTypeAdapter(type = DateTime.class, value = DateTimeAdapter.class) })
package de.fu_berlin.imp.apiua.server.java_ee.model;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;

import org.joda.time.DateTime;

import de.fu_berlin.imp.apiua.server.java_ee.model.adapters.DateTimeAdapter;
import de.fu_berlin.imp.apiua.server.java_ee.model.adapters.FingerprintAdapter;
import de.fu_berlin.imp.apiua.server.java_ee.model.adapters.IDAdapter;
import de.fu_berlin.imp.apiua.server.java_ee.model.adapters.IdentifierAdapter;
import de.fu_berlin.imp.apiua.server.java_ee.model.adapters.TokenAdapter;

