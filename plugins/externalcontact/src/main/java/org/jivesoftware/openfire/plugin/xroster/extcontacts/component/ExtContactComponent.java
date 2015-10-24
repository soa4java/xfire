package org.jivesoftware.openfire.plugin.xroster.extcontacts.component;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.jivesoftware.of.common.utils.JidUtil;
import org.jivesoftware.of.common.utils.XmppMessageUtils;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.plugin.xroster.extcontacts.constants.ExtContactConstants;
import org.jivesoftware.openfire.plugin.xroster.extcontacts.enums.ExtContactCause;
import org.jivesoftware.openfire.plugin.xroster.extcontacts.enums.ExtContactNextAction;
import org.jivesoftware.openfire.plugin.xroster.extcontacts.processor.ExtContactIQProcessor;
import org.jivesoftware.util.JiveGlobals;
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
import org.xmpp.packet.PacketError;
import org.xmpp.packet.Presence;

import com.google.common.collect.Maps;

/**
 * Component for external contact person
 *
 * @author yanrc
 */
public class ExtContactComponent implements Component {

    private final Logger LOG = LoggerFactory.getLogger(ExtContactComponent.class);
    private ExtContactIQProcessor iqProcessor;

    @Override
    public void start() {

    }

    @Override
    public void processPacket(Packet fromPackage) {

        if (LOG.isInfoEnabled()) {
            LOG.info(String.format("incoming package: %s", fromPackage.toXML()));
        }

        if (fromPackage instanceof Message) {
            // Respond to incoming messages
            Message fromMessage = (Message) fromPackage;
            processMessage(fromMessage);
        } else if (fromPackage instanceof Presence) {
            // Respond to presence subscription request or presence probe
            Presence fromPresence = (Presence) fromPackage;
            processPresence(fromPresence);
        } else if (fromPackage instanceof IQ) {
            IQ fromIQ = (IQ) fromPackage;
            // Ignore IQs of type ERROR or RESULT
            if (IQ.Type.error == fromIQ.getType() || IQ.Type.result == fromIQ.getType()) {
                return;
            }
            processIQ(fromIQ);
        }

    }

    /**
     * process IQ package sended to externalcontact.servyou
     *
     * @param from
     */

    private void processIQ(IQ from) {
        IQ reply = null;
        try {
            Element childElement = from.getChildElement();
            String namespaceURI = childElement.getNamespaceURI();

            // 构造返回dom
            reply = IQ.createResultIQ(from);
            Element childEleCopy = childElement.createCopy();
            reply.setChildElement(childEleCopy);

            // process different IQ by IQ protocal namespace URI
            if (ExtContactProtocalEnum.IQ_DISCO_INFO.is(namespaceURI)) {

                iqProcessor.discoInfo(from, reply);

            } else if (ExtContactProtocalEnum.CLOSE_SESSION_URI.is(namespaceURI)) {
                // send close session message to agent
                XMPPServer.getInstance().getMessageRouter().route(genSessionCloseMessage(from));
            } else if (ExtContactProtocalEnum.IQ_LIST_CHAT_MSGS_URI.is(namespaceURI)) {
                iqProcessor.listChatMsgRecords(from, reply);

            } else {
                LOG.error("EcpComponent->processIQ() can't identify ! iq_XML={}", from.toXML());
                reply.setError(PacketError.Condition.unexpected_request);
            }

            ComponentManagerFactory.getComponentManager().sendPacket(this, reply);

            if (LOG.isInfoEnabled()) {
                LOG.info(String.format("outing IQ package: %s", reply.toXML()));
            }

        } catch (Exception e) {
            LOG.error("EcpComponent->processIQ() error! iq_XML={}", from.toXML(), e);
        } finally {
            if (LOG.isInfoEnabled()) {
                LOG.info("from: " + from.toXML() + " ,reply: " + reply.toXML());
            }
        }

    }

    private Message genSessionCloseMessage(IQ from) {
        String fromNode = from.getFrom().getNode();
        String agent = from.getElement().element(ExtContactConstants.X).element(ExtContactConstants.AGENT).getText();
        String consulter = from.getElement().element(ExtContactConstants.X).element(ExtContactConstants.CONSULTER).getText();
        String to = null;
        // 说明是坐席发送的IQ，将Message消息发给咨询人
        if (StringUtils.equals(fromNode, agent)) {
            to = JidUtil.nodeToPcJid(consulter);
        } else if (StringUtils.equals(fromNode, consulter)) {
            to = JidUtil.nodeToPcJid(agent);
        } else {
            LOG.error("I can't decide who I should send the message to. iq from :[], agent: [], consulter :[].", fromNode, agent, consulter);
            return null;
        }

        Map eleAndTextMap = Maps.newHashMap();
        eleAndTextMap.put(ExtContactConstants.CAUSE, ExtContactCause.PROTOCOL.getCode());
        eleAndTextMap.put(ExtContactConstants.CONSULTER, consulter);
        eleAndTextMap.put(ExtContactConstants.AGENT, agent);
        eleAndTextMap.put(ExtContactConstants.ECSID, from.getElement().element(ExtContactConstants.X).element(ExtContactConstants.ECSID).getText());
        eleAndTextMap.put(ExtContactConstants.ECID, from.getElement().element(ExtContactConstants.X).element(ExtContactConstants.ECID).getText());
        eleAndTextMap.put(ExtContactConstants.NEXT_ACTION, ExtContactNextAction.NONE.getCode());

        return XmppMessageUtils.createMsg(ExtContactConstants.COMP_SUBDOMAIN + "." + JidUtil.DOMAIN,
                to,
                null,
                true,
                ExtContactProtocalEnum.CLOSE_SESSION_URI.getUri(),
                eleAndTextMap
        );
    }

    /**
     * process Presence package sended to externalcontact.servyou
     *
     * @param presence
     */
    private void processPresence(Presence presence) {

    }

    /**
     * process Message package sended to externalcontact.servyou
     *
     * @param message
     */
    private void processMessage(Message message) {

    }

    @Override
    public String getName() {
        return JiveGlobals.getProperty(ExtContactConstants.COMP_DISO_KEY, ExtContactConstants.COMP_SUBDOMAIN);
    }

    @Override
    public String getDescription() {
        return "a component for external contact";
    }

    /**
     * start EcGroupScanThread
     */
    @Override
    public void initialize(JID jid, ComponentManager componentManager) throws ComponentException {

        iqProcessor = ExtContactIQProcessor.getInstance();
        iqProcessor.init();
    }

    /**
     * stop EcGroupScanThread
     */
    @Override
    public void shutdown() {

        iqProcessor.destroy();
        iqProcessor = null;
    }

}
