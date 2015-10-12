package org.jivesoftware.of.common.domain;

import org.jivesoftware.of.common.enums.JidResourceEnum;
import org.jivesoftware.of.common.enums.StateEnum;

public class DomainNodeJid implements Comparable<DomainNodeJid> {

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

	/**
	 * nodeId,
	 */
	private String nid;

	public DomainNodeJid() {
	}

	public DomainNodeJid(String pid, String dm, String rs, String ss, String so, String ns, String nid, long timestamp) {
		super();
		this.pid = pid;
		this.dm = dm;
		this.ss = ss;
		this.so = so;
		this.ns = ns;
		this.rs = rs;
		this.nid = nid;
		this.ts = timestamp;
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

	public String getNid() {
		return nid;
	}

	public void setNid(String nid) {
		this.nid = nid;
	}

	public int compareTo(DomainNodeJid that) {
		int thisLevel = StateEnum.fromCode(ss).getLevel();
		int thatLevel = StateEnum.fromCode(that.getSs()).getLevel();

		if (thisLevel > thatLevel) {
			return 1;
		} else if (thisLevel < thatLevel) {
			return -1;
		}
		//thisLevel == thatLevel

		int thisResourceLevel = JidResourceEnum.fromValue(rs).getLevel();
		int thatResourceLevel = JidResourceEnum.fromValue(that.getRs()).getLevel();

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
