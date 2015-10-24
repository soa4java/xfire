package org.jivesoftware.openfire.plugin.xroster.internal.listener;

import net.yanrc.web.xweb.uic.api.UserApi;

import org.jivesoftware.of.common.spring.SpringContextHolder;
import org.jivesoftware.of.common.utils.SessionUtils;
import org.jivesoftware.openfire.event.SessionEventListener;
import org.jivesoftware.openfire.session.Session;

/**
 * Created by yanrc on 2015/4/24.
 */
public class InterRosterResourceBindListener implements SessionEventListener {

	UserApi userApi;

	public InterRosterResourceBindListener() {
		userApi = SpringContextHolder.getBean(UserApi.class);
	}

	@Override
	public void resourceBound(Session session) {
		SessionUtils.setTopGroupId(session, userApi.getTopGroupId(session.getAddress().getNode()).getModel());
	}

	@Override
	public void sessionCreated(Session session) {

	}

	@Override
	public void sessionDestroyed(Session session) {

	}

	@Override
	public void anonymousSessionCreated(Session session) {

	}

	@Override
	public void anonymousSessionDestroyed(Session session) {

	}

}
