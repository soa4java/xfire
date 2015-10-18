package org.jivesoftware.of.common.version;

import org.xmpp.packet.JID;

/**
 * Created by yanrc on 15-9-6.
 */
public class VersionSetEvent {
    private String version;
    private JID jid;

    public VersionSetEvent(){

    }

    public VersionSetEvent(String version, JID jid) {
        this.version = version;
        this.jid = jid;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public JID getJid() {
        return jid;
    }

    public void setJid(JID jid) {
        this.jid = jid;
    }
}
