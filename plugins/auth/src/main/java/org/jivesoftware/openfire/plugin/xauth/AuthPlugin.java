package org.jivesoftware.openfire.plugin.xauth;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.jivesoftware.database.DbConnectionManager;
import org.jivesoftware.of.common.plugin.PluginAdaptor;
import org.jivesoftware.of.common.service.RestService;
import org.jivesoftware.openfire.auth.AuthorizationManager;
import org.jivesoftware.openfire.auth.AuthorizationPolicy;
import org.jivesoftware.openfire.auth.DefaultAuthProvider;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.user.DefaultUserProvider;
import org.jivesoftware.util.Encryptor;
import org.jivesoftware.util.JiveGlobals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class AuthPlugin extends PluginAdaptor implements Plugin {
	private Logger logger = LoggerFactory.getLogger(AuthPlugin.class);
	private String updatePropertirySQL = "UPDATE ofProperty SET propValue=? WHERE name=?";
	private String providerAuthClassNameKey = "provider.auth.className";
	private ArrayList<AuthorizationPolicy> defaultAuthorizationPolicies = Lists.newArrayList();
	private String providerUserClassNameKey = "provider.user.className";

	private String defaultProviderUserClassName = JiveGlobals.getProperty(providerUserClassNameKey);
	private String defaultProviderAuthClassname = JiveGlobals.getProperty(providerAuthClassNameKey);;

	public void initializePlugin(PluginManager manager, File pluginDirectory) {
		defaultAuthorizationPolicies.addAll(AuthorizationManager.getAuthorizationPolicies());
		AuthorizationManager.getAuthorizationPolicies().clear();
		AuthorizationManager.getAuthorizationPolicies().add(new XAuthorizationPolicy());

		JiveGlobals.setProperty(providerUserClassNameKey, XUserProvider.class.getName());

		String providerAuthClassname = XAuthProvider.class.getName();
		JiveGlobals.setProperty(providerAuthClassNameKey, providerAuthClassname);

		if (StringUtils.isNotBlank(defaultProviderUserClassName)) {
			updateProperty(providerAuthClassNameKey, defaultProviderUserClassName);//DefaultAuthProvider.class.getName()
		}

		if (StringUtils.isNotBlank(defaultProviderAuthClassname)) {
			updateProperty(providerUserClassNameKey, defaultProviderAuthClassname);//DefaultUserProvider.class.getName()
		}
		
		System.out.println("auth init succeed!");
	}

	public void destroyPlugin() {
		AuthorizationManager.getAuthorizationPolicies().clear();
		AuthorizationManager.getAuthorizationPolicies().addAll(defaultAuthorizationPolicies);

		JiveGlobals.setProperty(providerUserClassNameKey, defaultProviderUserClassName);
		JiveGlobals.setProperty(providerAuthClassNameKey, defaultProviderAuthClassname);
	}

	private void updateProperty(String name, String value) {
		Encryptor encryptor = getEncryptor();
		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			con = DbConnectionManager.getConnection();
			pstmt = con.prepareStatement(updatePropertirySQL);
			pstmt.setString(1, JiveGlobals.isPropertyEncrypted(name) ? encryptor.encrypt(value) : value);
			pstmt.setString(2, name);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			DbConnectionManager.closeConnection(pstmt, con);
		}
	}

	private Encryptor getEncryptor() {
		return JiveGlobals.getPropertyEncryptor();
	}

	@Override
	public boolean register(String serviceName, RestService service) {
		return false;
	}

	@Override
	public boolean unregister() {
		return false;
	}

}
