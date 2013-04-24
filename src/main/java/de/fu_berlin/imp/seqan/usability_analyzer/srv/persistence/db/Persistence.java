package de.fu_berlin.imp.seqan.usability_analyzer.srv.persistence.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.DoclogAction;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.DoclogRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.srv.model.Rectangle;

public class Persistence {

	private final DerbyDatabase db;

	public Persistence(DerbyDatabase db) throws PersistenceException {
		this.db = db;
		try {
			this.db.start();
		} catch (SQLException e) {
			throw new PersistenceException("Could not start "
					+ DerbyDatabase.class.getSimpleName(), e);
		}
	}

	public Long add(DoclogRecord doclogRecord) throws SQLException {
		Connection conn = this.db.getConnection();
		PreparedStatement pstmt = null;
		try {
			pstmt = conn
					.prepareStatement(
							"INSERT INTO doclogrecord (url, ip, proxyIp, action, actionParam, dateTime, x, y, w, h)"
									+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
							Statement.RETURN_GENERATED_KEYS);
			pstmt.setString(1, doclogRecord.getUrl());
			pstmt.setString(2, doclogRecord.getIp());
			pstmt.setString(3, doclogRecord.getProxyIp());
			pstmt.setString(4, doclogRecord.getAction().toString());
			pstmt.setString(5, doclogRecord.getActionParameter());
			pstmt.setString(6, doclogRecord.getDateTime().toString());
			pstmt.setInt(7, doclogRecord.getBounds().getX());
			pstmt.setInt(8, doclogRecord.getBounds().getY());
			pstmt.setInt(9, doclogRecord.getBounds().getWidth());
			pstmt.setInt(10, doclogRecord.getBounds().getHeight());
			pstmt.executeUpdate();
			List<Long> keys = DerbyDatabase.getGeneratedKeys(pstmt
					.getGeneratedKeys());
			return keys.size() > 0 ? keys.get(0) : null;
		} catch (SQLException e) {
			if (pstmt != null)
				pstmt.close();
			throw e;
		}
	}

	public DoclogRecord get(Long id) throws SQLException {
		Connection conn = this.db.getConnection();
		PreparedStatement pstmt = null;
		try {
			pstmt = conn
					.prepareStatement("SELECT url, ip, proxyIp, action, actionParam, dateTime, x, y, w, h FROM doclogrecord"
							+ " WHERE id=?");
			pstmt.setLong(1, id);
			ResultSet rs = pstmt.executeQuery();
			if (!rs.next())
				return null;
			return new DoclogRecord(
					rs.getString("url"),
					rs.getString("ip"),
					rs.getString("proxyIp"),
					DoclogAction.valueOf(rs.getString("action")),
					rs.getString("actionParam"),
					DoclogRecord.ISO8601.parseDateTime(rs.getString("dateTime")),
					new Rectangle(rs.getInt("x"), rs.getInt("y"), rs
							.getInt("w"), rs.getInt("h")));
		} catch (SQLException e) {
			if (pstmt != null)
				pstmt.close();
			throw e;
		}
	}

	// TODO disconnection

	public boolean remove(Long key) throws SQLException {
		Connection conn = this.db.getConnection();
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement("DELETE FROM doclogrecord"
					+ " WHERE id=?", Statement.RETURN_GENERATED_KEYS);
			pstmt.setLong(1, key);
			return pstmt.executeUpdate() == 1;
		} catch (SQLException e) {
			if (pstmt != null)
				pstmt.close();
			throw e;
		}
	}

}
