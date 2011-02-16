package com.myapps;

public class Camera implements java.io.Serializable{
	private static final long serialVersionUID = 1L;
	protected String id;
	protected String login;
	protected String pass;
	protected String ip;
	protected int port;
	protected String protocol;

	protected Camera(String id, String login, String pass, String ip,
			int port) {
			//int channel) {
		this.id = id;
		this.login = login;
		this.pass = pass;
		this.ip = ip;
		this.port = port;
		//this.channel = channel;
	}
	protected Camera(String id, String login, String pass, String ip,
			int port, String protocol) {
		this.id = id;
		this.login = login;
		this.pass = pass;
		this.ip = ip;
		this.port = port;
		this.protocol= protocol;
	}
	
	public String toString(){
		return id;
	}
}