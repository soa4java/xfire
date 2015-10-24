package org.jivesoftware.openfire.plugin.xroster.extcontacts.processor;

import org.dom4j.Element;
import org.jivesoftware.openfire.PacketRouter;
import org.jivesoftware.openfire.plugin.xroster.extcontacts.component.ExtContactProtocalEnum;
import org.jivesoftware.openfire.plugin.xroster.extcontacts.constants.ExtContactConstants;
import org.jivesoftware.util.JiveGlobals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.IQ;
import org.xmpp.packet.PacketError;

public class ExtContactIQProcessor implements ExtContactProcessor {

	private final Logger LOG = LoggerFactory.getLogger(ExtContactIQProcessor.class);

	private final String KEY = "ec.default.pagesize";

	private static ExtContactIQProcessor instance = new ExtContactIQProcessor();

	private PacketRouter router;

	private ExtContactIQProcessor() {

	}

	public static ExtContactIQProcessor getInstance() {
		if (instance == null) {
			synchronized (ExtContactIQProcessor.class) {
				if (instance == null) {
					instance = new ExtContactIQProcessor();
				}
			}
		}
		return instance;
	}

	@Override
	public void init() {
	}

	@Override
	public void destroy() {
	}

	/**
	 * 服务发现
	 * 
	 * @param fromIQ
	 * @param replyIQ
	 */
	public void discoInfo(IQ fromIQ, IQ replyIQ) {

		Element xChildElement = replyIQ.getChildElement();

		if (fromIQ.getType() != IQ.Type.get) {
			replyIQ.setError(PacketError.Condition.service_unavailable);
			return;
		}
		xChildElement.addElement("feature").addAttribute("var", ExtContactProtocalEnum.IQ_EC_URI.getUri());
		Element ele = xChildElement.addElement("x", "jabber:x:data");
		ele.addAttribute("type", "result");
		Element field = ele.addElement("field");
		field.addAttribute("var", "autoCloseSessionWindowTime");
		field.addElement("value").setText(JiveGlobals.getProperty(ExtContactConstants.AUTO_CLOSE_SESSION_WIN_TIME, "2"));

		if (LOG.isInfoEnabled()) {
			LOG.info(String.format("discoInfo  fromIQ %s replyIQ %s", fromIQ.toXML(), replyIQ.toXML()));
		}
	}

	/**
	 * 获取聊天消息记录
	 * 
	 * @param fromIQ
	 * @param replyIQ
	 */
	public void listChatMsgRecords(IQ fromIQ, IQ replyIQ) {

		/*Element childElement = replyIQ.getChildElement();

		if (fromIQ.getType() == IQ.Type.get) {

			List<String> lst = null;

			String consulterJid = childElement.elementText(EcConstants.CONSULTER);
			String agentJid = childElement.elementText(EcConstants.AGENT);
			String ecsid = childElement.elementText(EcConstants.ECSID);

			if (StringUtils.isBlank(consulterJid) || StringUtils.isBlank(agentJid)) {
				LOG.warn("consulterJid and  agentJid can't be blank!");
				replyIQ.setChildElement(createError(fromIQ, replyIQ, ECBizErrorEnum.PARAM_ERRO.getErrorCode(),
						ECBizErrorEnum.PARAM_ERRO.getDescription()));
				return;
			}

			String consulterAccount = new JID(consulterJid).getNode();
			String agentAccount = new JID(agentJid).getNode();

			String pageNo = EcConstants.ZERO;
			String pageSize = defaultPageSize;

			Element page = childElement.element(EcConstants.PAGE);
			if (page != null) {

				pageNo = StringUtils.defaultIfBlank(page.elementText(EcConstants.PAGE_NO), EcConstants.ZERO);
				pageSize = StringUtils.defaultIfBlank(page.elementText(EcConstants.PAGE_SIZE), defaultPageSize);
				page.addElement(EcConstants.RECORD_COUNT).setText(
						consulterRecordMapper.selectPageCount(consulterAccount, agentAccount, ecsid).toString());

				lst = consulterRecordMapper.selectPage(consulterAccount, agentAccount, ecsid, pageNo, pageSize);

			} else {

				lst = consulterRecordMapper.selectPage(consulterAccount, agentAccount, ecsid, EcConstants.ZERO,
						defaultPageSize);

			}

			if (lst == null || lst.isEmpty()) {
				return;
			}

			for (String record : lst) {
				childElement.addElement("item").setText("<![CDATA[" + StringUtils.defaultIfBlank(record, "") + "]]>");
			}

		} else {
			replyIQ.setError(PacketError.Condition.service_unavailable);
		}*/
	}
	
	/**
	 * 关闭咨询会话
	 * 
	 * @param fromIQ
	 * @param replyIQ
	 */
	public void closeSession(IQ fromIQ, IQ replyIQ) {

	}
}
