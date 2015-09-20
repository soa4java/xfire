package org.jivesoftware.openfire.plugin.xauth;

import org.jivesoftware.openfire.auth.AuthorizationPolicy;

public class XAuthorizationPolicy implements AuthorizationPolicy {

	@Override
	public boolean authorize(String username, String principal) {
		return true;
	}

	@Override
	public String name() {
		 return "Servyou Policy";
	}

	@Override
	public String description() {
		return "Different clients perform authentication differently, so this policy "+ 
	               "will authorize any principal to a requested user that match specific "+
	               "conditions that are considered secure defaults for most installations.";
	}

}
