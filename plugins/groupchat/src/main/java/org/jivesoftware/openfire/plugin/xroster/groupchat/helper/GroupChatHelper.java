package org.jivesoftware.openfire.plugin.xroster.groupchat.helper;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by yanrc on 15-5-6.
 */
public abstract class GroupChatHelper {

    public static final Set<String> getPids(Set<Object> revsubs) {
        Set<String> pids = new HashSet<String>(revsubs.size());
        String pid = null;
        for (Object obj : revsubs) {
            pid = obj.toString().split("\\/")[0];
            pids.add(pid);
        }
        return pids;
    }


}
