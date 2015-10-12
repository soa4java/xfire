package org.jivesoftware.of.common.error;

public class ErrorInfo {
	private String code;
	private String text;

	public ErrorInfo() {
	}

	public ErrorInfo(String code, String text) {
		super();
		this.code = code;
		this.text = text;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

}
