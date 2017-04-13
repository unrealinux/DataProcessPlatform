package com.circletech.smartconnect.exception;

import com.circletech.smartconnect.util.LoggerUtil;

public class NoSuchPort extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NoSuchPort() {
		LoggerUtil.getInstance().info("No serial device matching the port name was found! Failed to open serial port operation!");
	}

	@Override
	public String toString() {
		return "No serial device matching the port name was found! Failed to open serial port operation!";
	}
	
}
