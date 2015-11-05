package org.jivesoftware.of.common.domain;

import java.io.Serializable;

import org.jivesoftware.of.common.enums.PresenceStatus;
import org.jivesoftware.of.common.enums.Resource;

/**
 * 用户票据信息(身份)
 * @author yanrc
 *
 */
public class UserTicket implements Comparable<UserTicket>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5849597949346562938L;
	/**
	 * 
	 */
	private String pid;//personId
	private String dm;//domain
	private String ss;//status
	private String so;//show
	private String ns;//netstatus
	private String rs;//resource
	private long ts; //timestamp 时间戳
	private String tenantId;

	/**
	 * nodeId,
	 */
	private String nodeName;//nodeId

	public UserTicket() {
	}

	public UserTicket(String personId, String domain, String resource, String status, String show, String netstatus, String nodeName, long timestamp,
			String tenantId) {
		super();
		this.pid = personId;
		this.dm = domain;
		this.ss = status;
		this.so = show;
		this.ns = netstatus;
		this.rs = resource;
		this.nodeName = nodeName;
		this.ts = timestamp;
		this.tenantId = tenantId;
	}

	public synchronized String getTenantId() {
		return tenantId;
	}

	public synchronized void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public String getDm() {
		return dm;
	}

	public void setDm(String dm) {
		this.dm = dm;
	}

	public String getSs() {
		return ss;
	}

	public void setSs(String ss) {
		this.ss = ss;
	}

	public String getSo() {
		return so;
	}

	public void setSo(String so) {
		this.so = so;
	}

	public String getNs() {
		return ns;
	}

	public void setNs(String ns) {
		this.ns = ns;
	}

	public String getRs() {
		return rs;
	}

	public void setRs(String rs) {
		this.rs = rs;
	}

	public long getTs() {
		return ts;
	}

	public void setTs(long ts) {
		this.ts = ts;
	}

	

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public int compareTo(UserTicket that) {
		int thisLevel = PresenceStatus.fromCode(ss).getLevel();
		int thatLevel = PresenceStatus.fromCode(that.getSs()).getLevel();

		if (thisLevel > thatLevel) {
			return 1;
		} else if (thisLevel < thatLevel) {
			return -1;
		}
		//thisLevel == thatLevel

		int thisResourceLevel = Resource.fromCode(rs).getLevel();
		int thatResourceLevel = Resource.fromCode(that.getRs()).getLevel();

		if (thisResourceLevel > thatResourceLevel) {
			return 1;
		} else if (thisResourceLevel < thatResourceLevel) {
			return -1;
		}

		//时间越早越优先
		if (ts < that.getTs()) {
			return 1;
		} else if (ts > that.getTs()) {
			return -1;
		}
		return 0;
	}
}
