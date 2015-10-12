package sitong.thinker.of.plugin.groupchat.processor;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.jivesoftware.openfire.XMPPServer;

public abstract class AbstractProcessor {

	String localDomain = XMPPServer.getInstance().getServerInfo().getXMPPDomain();

	public void appendReportedEle(Element reportedEle, String varAttrVal, String typeAttrVal, String labelAttrVal) {
		Element field = reportedEle.addElement("field");
		field.addAttribute("var", varAttrVal);
		field.addAttribute("type", typeAttrVal);
		field.addAttribute("label", labelAttrVal);
	}

	public void appendFiledEle(Element ele, String attrName, String attrVal, String value) {
		Element field = ele.addElement("field");
		field.addAttribute(attrName, attrVal);
		field.addElement("value").addText(value);
	}

	public void appendItem(Element ele, String fullJid, String nick) {
		Element itemEle = ele.addElement("item");
		itemEle.addAttribute("jid", fullJid);
		if (StringUtils.isNotBlank(nick)) {
			itemEle.addAttribute("nick", nick);
		}
	}

	public void appendItem(Element ele, String fullJid, String affiliationCode, String roleCode) {
		Element itemEle = ele.addElement("item");
		itemEle.addAttribute("jid", fullJid);
		if (StringUtils.isNotBlank(affiliationCode)) {
			itemEle.addAttribute("affiliation", affiliationCode);
		}
		if (StringUtils.isNotBlank(roleCode)) {
			itemEle.addAttribute("role", roleCode);
		}
	}

	public void appendStatus(Element ele, String code, String value) {
		Element statusEle = ele.addElement("status");
		statusEle.addAttribute("code", code);
		statusEle.addAttribute("text", value);
	}

}
