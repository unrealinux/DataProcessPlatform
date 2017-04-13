package com.circletech.smartconnect.exception;

public class SerialPortOutputStreamCloseFailure extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SerialPortOutputStreamCloseFailure() {}

	@Override
	public String toString() {
		return "Error closing the output stream (OutputStream) of the serial object!";
	}
}
