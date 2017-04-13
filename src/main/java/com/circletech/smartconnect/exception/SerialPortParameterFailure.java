package com.circletech.smartconnect.exception;

public class SerialPortParameterFailure extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SerialPortParameterFailure() {}

	@Override
	public String toString() {
		return "Failed to set serial port parameters! Open serial operation is not complete!";
	}
	
}
