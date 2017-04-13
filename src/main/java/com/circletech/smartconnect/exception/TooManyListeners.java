package com.circletech.smartconnect.exception;

public class TooManyListeners extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TooManyListeners() {}

	@Override
	public String toString() {
		return "Excessive number of serial listening class! Add operation failed!";
	}
	
}
