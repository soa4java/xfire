package org.jivesoftware.openfire.plugin.xroster.internal.listener;

import java.io.Serializable;

/**
 * Created by yanrc on 2015/4/23.
 */
public class SubRelationLifecycle implements Serializable {

    private String status;
    private long inactivityTime;
    private String fullJid;

    private Terminal terminal;

    public long getInactivityTime() {
        return inactivityTime;
    }

    public void setInactivityTime(long inactivityTime) {
        this.inactivityTime = inactivityTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFullJid() {
        return fullJid;
    }

    public void setFullJid(String fullJid) {
        this.fullJid = fullJid;
    }

    public Terminal getTerminal() {
        return terminal;
    }

    public void setTerminal(Terminal terminal) {
        this.terminal = terminal;
    }

    public static class Terminal {
        private String personId;
        private String resource;

        public String getPersonId() {
            return personId;
        }

        public void setPersonId(String personId) {
            this.personId = personId;
        }

        public String getResource() {
            return resource;
        }

        public void setResource(String resource) {
            this.resource = resource;
        }
    }

}