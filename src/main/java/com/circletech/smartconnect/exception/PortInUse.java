package com.circletech.smartconnect.exception;

import com.circletech.smartconnect.util.LoggerUtil;

public class PortInUse extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PortInUse() {
		LoggerUtil.getInstance().info("端口已被占用！打开串口操作失败！");
	}

	@Override
	public String toString() {
		return "端口已被占用！打开串口操作失败！";
	}
	
}
