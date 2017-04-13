package com.circletech.smartconnect.exception;

import com.circletech.smartconnect.util.LoggerUtil;

public class PortInUse extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PortInUse() {
		LoggerUtil.getInstance().info("Port is occupied! Failed to open serial port operation!");
	}

	@Override
	public String toString() {
		return "Port is occupied! Failed to open serial port operation!";
	}
	
}
