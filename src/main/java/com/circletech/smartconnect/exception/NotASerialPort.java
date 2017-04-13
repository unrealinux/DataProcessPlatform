package com.circletech.smartconnect.exception;

import com.circletech.smartconnect.util.LoggerUtil;

public class NotASerialPort extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NotASerialPort() {
		LoggerUtil.getInstance().info("Port pointing device is not a serial port type! Failed to open serial port operation!");
	}

	@Override
	public String toString() {
		return "Port pointing device is not a serial port type! Failed to open serial port operation!";
	}
	
	
}
