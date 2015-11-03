package org.jivesoftware.openfire.plugin.jafka.vo;

import java.io.Serializable;

public class ImNode implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7978666338862665897L;

	private String name;
	private String ip;
	private long lastTimestamp;
	
	public ImNode(){
		
	}
	
	public ImNode(String name, String ip, long lastTimestamp) {
		super();
		this.name = name;
		this.ip = ip;
		this.lastTimestamp = lastTimestamp;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public long getLastTimestamp() {
		return lastTimestamp;
	}
	public void setLastTimestamp(long lastTimestamp) {
		this.lastTimestamp = lastTimestamp;
	}
	
	
	
	

}
