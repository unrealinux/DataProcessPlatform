package com.circletech.smartconnect.exception;

import com.circletech.smartconnect.util.LoggerUtil;

public class NotASerialPort extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NotASerialPort() {
		LoggerUtil.getInstance().info("端口指向设备不是串口类型！打开串口操作失败！");
	}

	@Override
	public String toString() {
		return "端口指向设备不是串口类型！打开串口操作失败！";
	}
	
	
}
