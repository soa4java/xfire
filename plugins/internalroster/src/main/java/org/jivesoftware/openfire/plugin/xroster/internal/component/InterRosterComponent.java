package org.jivesoftware.openfire.plugin.xroster.internal.component;

import org.dom4j.Element;
import org.jivesoftware.of.common.constants.XConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.component.Component;
import org.xmpp.component.ComponentException;
import org.xmpp.component.ComponentManager;
import org.xmpp.component.ComponentManagerFactory;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;

public class InterRosterComponent implements Component {
    static final Logger LOG = LoggerFactory.getLogger(InterRosterComponent.class);

    @Override
    public void processPacket(Packet packet) {
        if (packet instanceof Message) {
            Message message = (Message) packet;
            processMessage(message);
        } else if (packet instanceof Presence) {
            Presence presence = (Presence) packet;
            processPresence(presence);
        } else if (packet instanceof IQ) {
            IQ iq = (IQ) packet;
            if (IQ.Type.error == iq.getType() || IQ.Type.result == iq.getType()) {
                return;
            }
            processIQ(iq);
        }

    }

    private void processIQ(IQ iq) {
        long start = System.currentTimeMillis();
        Element childElement = iq.getChildElement();
        String namespace = childElement.getNamespaceURI();

        // 构造返回dom
        IQ reply = IQ.createResultIQ(iq);
        Element childElementCopy = childElement.createCopy();
        reply.setChildElement(childElementCopy);

        if (XConstants.PROTOCOL_DISCO_INFO.equals(namespace)) {
            generateDisco(childElementCopy);// 构造disco反馈信息
            if (LOG.isInfoEnabled()) {
                LOG.info("[spend time:{}ms],reqId: {},IRComponent服务发现,response:{}", System.currentTimeMillis() - start, iq.getID(), reply.toXML());
            }

        }

        try {
            ComponentManagerFactory.getComponentManager().sendPacket(this, reply);
        } catch (Throwable t) {
            LOG.error("[spend time:{}ms],reqId: {},IRComponent IQ处理异常! iq:{},replay:{}", System.currentTimeMillis() - start, iq.getID(), reply.toXML(), t);
        }

    }

    private void processMessage(Message message) {

    }

    private void processPresence(Presence presence) {

    }

    private void generateDisco(Element childElement) {
        // Return service identity and features
        Element identity = childElement.addElement("identity");
        identity.addAttribute("category", "component");
        identity.addAttribute("type", "generic");
        identity.addAttribute("name", "internalRoster service");
        childElement.addElement("feature").addAttribute("var", XConstants.PROTOCOL_DISCO_INFO);
        childElement.addElement("feature").addAttribute("var", XConstants.GET_PRESENCE_BY_ORG_ID);
        childElement.addElement("feature").addAttribute("var", XConstants.GET_PRESENCE_BY_DEP_ID);
        childElement.addElement("feature").addAttribute("var", XConstants.GET_PRESENCE_BY_JIDS);
    }

    @Override
    public void start() {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public String getDescription() {
        return "the component for internalRoster";
    }

    @Override
    public String getName() {
        return "internalroster commponet";
    }

    @Override
    public void initialize(JID jid, ComponentManager componentManager) throws ComponentException {
    }
}
