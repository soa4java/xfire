package org.jivesoftware.openfire.plugin.jafka.util;

import java.io.Serializable;

/**
 * Created by yanrc on 2015/4/23.
 */
public class SubRelationLifecycle implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -2970770520158401233L;
	private String status;
    private long inactivityTime;
    private String fullJid;
    private String nodeName;

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

    public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
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
