package org.jivesoftware.openfire.plugin.xroster.extcontacts.groupchat.helper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.jivesoftware.of.common.constants.XConstants;
import org.jivesoftware.of.common.domain.UserTicket;
import org.jivesoftware.of.common.domain.utils.DomainNodeJidCacheUtils;
import org.jivesoftware.of.common.domain.utils.DomainNodeJidPacketDuplicator;
import org.jivesoftware.openfire.RoutingTable;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.plugin.xroster.extcontacts.groupchat.enums.ProtocolEnum;
import org.jivesoftware.util.JiveProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;

public abstract class GroupChatCrossDomainHelper {
    static final Logger LOG = LoggerFactory.getLogger(XConstants.LOG_CROSS_DOMAIN);
    static RoutingTable routingTable = XMPPServer.getInstance().getRoutingTable();
    static String localDomainName = XMPPServer.getInstance().getServerInfo().getXMPPDomain();

    @SuppressWarnings("unchecked")
    public static Map<String, UserTicket> getPidAndDomainMapForAll(Set<Object> pids) {

        if (!DomainNodeJidPacketDuplicator.crossDomainEnable) {
            return MapUtils.EMPTY_MAP;
        }

        return DomainNodeJidCacheUtils.multiFetch(pids);
    }



    /**
     * 返回在其他域中的所有用户
     * @param pids
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Map<String, UserTicket> getPidAndDomainMapInOther(Set<String> pids) {
        long start = System.currentTimeMillis();
        try {
            //不启用跨域就不做处理
            if (CollectionUtils.isEmpty(pids)) {
                return MapUtils.EMPTY_MAP;
            }

            if (!DomainNodeJidPacketDuplicator.crossDomainEnable) {
                return MapUtils.EMPTY_MAP;
            }

            Map<String, UserTicket> domainMap = DomainNodeJidCacheUtils.multiFetch(new HashSet<Object>(pids));
            if (MapUtils.isEmpty(domainMap)) {
                return MapUtils.EMPTY_MAP;
            }

            Map<String, UserTicket> map = new HashMap<String, UserTicket>(domainMap.size());
            Iterator<String> itr = domainMap.keySet().iterator();
            String pid = null;
            UserTicket domain = null;
            while (itr.hasNext()) {
                pid = itr.next();
                domain = domainMap.get(pid);

                if (StringUtils.equals(localDomainName, domain.getDm())) {//本域组用户不处理
                    continue;
                }
                map.put(pid, domain);
            }
            return map;
        } catch (Throwable t) {
            LOG.error("GroupChatCrossDomainHelper.getPidAndDomainMapForOther error!", t);
            return MapUtils.EMPTY_MAP;
        } finally {
            if (LOG.isInfoEnabled()) {
                LOG.info("[spend time:{},ms], GroupChatCrossDomainHelper.getPidAndDomainMapForOther(Set({}))", System.currentTimeMillis() - start, pids.size());
            }
        }

    }

    public static Packet duplicateForGroupChat(Packet packet) {

        //不启用跨域就不做处理
        if (!DomainNodeJidPacketDuplicator.crossDomainEnable || packet.getFrom() == null) {
            return null;
        }

        if (packet instanceof IQ) {//如果是iq以下协议不需要转发
            IQ iq = (IQ) packet;

            if (IQ.Type.error == iq.getType() || IQ.Type.result == iq.getType()) {
                return null;
            }

            Element childElement = iq.getChildElement();
            String namespaceURI = childElement.getNamespaceURI();
            if (ProtocolEnum.IQ_DISCO_INFO.getUrl().equals(namespaceURI) //服务发现
                    || ProtocolEnum.IQ_GET_CHAT_GROUPS.getUrl().equals(namespaceURI)//获取组列表
                    || ProtocolEnum.IQ_GET_GROUP_MEMBERS.getUrl().equals(namespaceURI) //获取组成员
                    || ProtocolEnum.IQ_GET_GROUPS_EXISTS.getUrl().equals(namespaceURI)//判断组是否出现
                    ) {
                return null;
            }
        }

        JID fromJID = packet.getFrom();

        //跨域过来的不处理
        if (!StringUtils.equals(localDomainName, fromJID.getDomain())) {
            return null;
        }

		/*Session session = SessionManager.getInstance().getSession(fromJID);
		if (session != null && session.getStatus() != Session.STATUS_AUTHENTICATED) {
			return null;
		}*/

        JID toJID = packet.getTo();
        //发给组聊组件的才处理
        if (toJID.getDomain().endsWith(localDomainName)) {
            // 发送者的domain信息和当前服务器名称相同，说明是本地客户端发送的iq，需要转发到远程域服务器
            String corssDomain = JiveProperties.getInstance().getProperty("cross.domains", "");
            if (StringUtils.isNotBlank(corssDomain)) {
                String[] corssDomains = corssDomain.split(",");
                for (String domain : corssDomains) {
                    if (!domain.equals(localDomainName)) {
                        Packet copy = packet.createCopy();
                        // 设置to jid为远程domain
                        String componentDomain = toJID.getDomain().replace(localDomainName, domain);
                        copy.setTo(new JID(toJID.getNode(), componentDomain, toJID.getResource()));
                        return copy;
                    }
                }
            }
        }
        return null;
    }
}
