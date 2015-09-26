package org.jivesoftware.openfire.plugin.xauth;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import net.yanrc.app.common.error.Message;
import net.yanrc.app.common.result.Result;
import net.yanrc.web.xweb.uic.api.UserApi;
import net.yanrc.web.xweb.uic.api.validate.UserValidatePojo;

import org.apache.commons.lang.StringUtils;
import org.jivesoftware.database.DbConnectionManager;
import org.jivesoftware.of.common.spring.SpringContextHolder;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.auth.AuthProvider;
import org.jivesoftware.openfire.auth.ConnectionException;
import org.jivesoftware.openfire.auth.InternalUnauthenticatedException;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.jivesoftware.util.JiveProperties;
import org.jivesoftware.util.PropertyEventDispatcher;
import org.jivesoftware.util.PropertyEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XAuthProvider implements AuthProvider, PropertyEventListener {

	private Logger logger = LoggerFactory.getLogger(XAuthProvider.class);
	private String account_verify_enable = "account.verify.enable";
	private boolean verifyEnable;

	private UserApi userApi;

	{
		verifyEnable = JiveProperties.getInstance().getBooleanProperty(account_verify_enable, true);
		PropertyEventDispatcher.addListener(this);
	}

	public XAuthProvider() {
		userApi = SpringContextHolder.getBean("userApi", UserApi.class);
	}

	@Override
	public void authenticate(String username, String password) throws UnauthorizedException, ConnectionException,
			InternalUnauthenticatedException {

		if (username == null || password == null) {
			throw new UnauthorizedException();
		}

		username = getUserNameForAuth(username);

		Result<Message> result;
		if (verifyEnable) {
			try {
				result = userApi.validate(new UserValidatePojo(username, password));
				
				if( !result.isSuccess() ){
					if(StringUtils.equalsIgnoreCase(password, getPasswordValue(username))){
						return;
					}
				}
				
			} catch (Throwable t) {
				logger.error("remote service invoke error!", t);
				throw new UnauthorizedException("service error!");
			}

			if (!result.isSuccess()) {
				logger.error("UserApi.validate fail({},{}),caurse:{}", username, password, result.getMessage()
						.getText());
				throw new UnauthorizedException(result.getMessage().getCode());
			}

		} else {

			String psd = null;

			try {
				psd = getPasswordValue(username);
			} catch (UserNotFoundException e) {
				logger.error("user not found by name", e);
				throw new UnauthorizedException("user not found");
			}

			if (!StringUtils.equalsIgnoreCase(password, psd)) {
				throw new UnauthorizedException("name or password  error!");
			}
		}

	}

	@Override
	public void authenticate(String username, String token, String digest) throws UnauthorizedException,
			ConnectionException, InternalUnauthenticatedException {

	}

	@Override
	public String getPassword(String username) throws UserNotFoundException, UnsupportedOperationException {
		return null;
	}

	@Override
	public void setPassword(String username, String password) throws UserNotFoundException,
			UnsupportedOperationException {

	}

	@Override
	public boolean supportsPasswordRetrieval() {
		return false;
	}

	private String getUserNameForAuth(String username) throws UnauthorizedException {
		username = username.trim().toLowerCase();
		if (username.contains("@")) {
			// Check that the specified domain matches the server's domain
			int index = username.indexOf("@");
			String domain = username.substring(index + 1);
			if (domain.equals(XMPPServer.getInstance().getServerInfo().getXMPPDomain())) {
				username = username.substring(0, index);
			} else {
				// Unknown domain. Return authentication failed.
				throw new UnauthorizedException();
			}
		}
		return username;
	}

	private String getPasswordValue(String username) throws UserNotFoundException {
		String password = null;
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		if (username.contains("@")) {
			// Check that the specified domain matches the server's domain
			int index = username.indexOf("@");
			String domain = username.substring(index + 1);
			if (domain.equals(XMPPServer.getInstance().getServerInfo().getXMPPDomain())) {
				username = username.substring(0, index);
			} else {
				// Unknown domain.
				throw new UserNotFoundException();
			}
		}

		try {
			con = DbConnectionManager.getConnection();
			pstmt = con.prepareStatement("select PLAINPASSWORD from OFUSER t where t.username=?");
			pstmt.setString(1, username);
			rs = pstmt.executeQuery();
			if (rs == null || !rs.next()) {
				throw new UserNotFoundException();
			}
			password = rs.getString(1);
		} catch (SQLException e) {
			logger.error("Exception in JDBCAuthProvider", e);
			throw new UserNotFoundException();
		} finally {
			DbConnectionManager.closeConnection(rs, pstmt, con);
		}
		return password;
	}

	@Override
	public boolean isScramSupported() {
		return false;
	}

	public void propertySet(String property, Map<String, Object> params) {
		if (account_verify_enable.equals(property)) {
			String value = (String) params.get("value");
			if (value != null) {
				verifyEnable = Boolean.parseBoolean(value);
			}
		}

	}

	public void propertyDeleted(String property, Map<String, Object> params) {
		if (account_verify_enable.equals(property)) {
			verifyEnable = false;
		}
	}

	public void xmlPropertySet(String property, Map<String, Object> params) {
		// Do nothing
	}

	public void xmlPropertyDeleted(String property, Map<String, Object> params) {
		// Do nothing
	}

	@Override
	public boolean isPlainSupported() {
		return true;
	}

	@Override
	public boolean isDigestSupported() {
		return false;
	}
}
