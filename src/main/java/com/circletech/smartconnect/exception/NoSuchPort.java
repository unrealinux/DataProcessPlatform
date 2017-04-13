package com.circletech.smartconnect.exception;

import com.circletech.smartconnect.util.LoggerUtil;

public class NoSuchPort extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NoSuchPort() {
		LoggerUtil.getInstance().info("没有找到与该端口名匹配的串口设备！打开串口操作失败！");
	}

	@Override
	public String toString() {
		return "没有找到与该端口名匹配的串口设备！打开串口操作失败！";
	}
	
}
