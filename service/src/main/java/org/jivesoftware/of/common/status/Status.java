package org.jivesoftware.of.common.status;

public class Status {

	private String node;
	private String domain;
	private String resource;
	private String status;
	private String show;
	private String netstatus;
	private int priority;
	private long timestamp;

	public Status() {

	}

	public Status(String node, String domain, String resource, String status, String show, String netstatus,
			int priority, long timestamp) {
		super();
		this.node = node;
		this.domain = domain;
		this.resource = resource;
		this.status = status;
		this.show = show;
		this.netstatus = netstatus;
		this.priority = priority;
		this.timestamp = timestamp;
	}

	public String getNode() {
		return node;
	}

	public void setNode(String node) {
		this.node = node;
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getShow() {
		return show;
	}

	public void setShow(String show) {
		this.show = show;
	}

	public String getNetstatus() {
		return netstatus;
	}

	public void setNetstatus(String netstatus) {
		this.netstatus = netstatus;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

}
