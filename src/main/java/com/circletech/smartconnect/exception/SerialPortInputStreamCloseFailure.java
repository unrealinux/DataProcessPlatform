package com.circletech.smartconnect.exception;

public class SerialPortInputStreamCloseFailure extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SerialPortInputStreamCloseFailure() {}

	@Override
	public String toString() {
		return "Error closing serial port object input stream (InputStream)!";
	}
	
	
}
