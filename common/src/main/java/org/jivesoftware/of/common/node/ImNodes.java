package org.jivesoftware.of.common.node;

import org.jivesoftware.util.JiveGlobals;

public abstract class ImNodes {

	public static String nodeIp;
	public static String nodeName;
	
	static {
		nodeName = JiveGlobals.getXMLProperty("imserver.node.name");
		nodeIp = JiveGlobals.getXMLProperty("imserver.node.ip");
	}
	
}
