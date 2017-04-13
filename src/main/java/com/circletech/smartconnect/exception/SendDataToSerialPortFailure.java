package com.circletech.smartconnect.exception;

public class SendDataToSerialPortFailure extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SendDataToSerialPortFailure() {}

	@Override
	public String toString() {
		return "Failed to send data to serial port!";
	}
	
}
