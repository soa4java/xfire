package org.jivesoftware.openfire.plugin.jafka.vo;

import java.io.Serializable;

public class UserNode implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3318972760568977466L;

	private String pid;
	private String domain;
	private String resource;
	private String nodeName;
	
	public UserNode(){
		
	}
	
	public UserNode(String pid, String domain, String resource, String nodeName) {
		super();
		this.pid = pid;
		this.domain = domain;
		this.resource = resource;
		this.nodeName = nodeName;
	}
	public String getPid() {
		return pid;
	}
	public void setPid(String pid) {
		this.pid = pid;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public String getResource() {
		return resource;
	}
	public void setResource(String resource) {
		this.resource = resource;
	}
	public String getNodeName() {
		return nodeName;
	}
	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}
	
	
}
