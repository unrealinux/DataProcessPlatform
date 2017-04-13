/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.circletech.smartconnect.util;

import com.circletech.smartconnect.exception.*;
import gnu.io.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.TooManyListenersException;
import com.circletech.smartconnect.network.CommInfo;

/**
 * 串口服务类，提供打开、关闭串口，读取、发送串口数据等服务（采用单例设计模式）
 *
 */
public class SerialUtil {
	
	private static SerialUtil serialUtil = null;
	
	static {
		//在该类被ClassLoader加载时就初始化一个SerialTool对象
		if (serialUtil == null) {
            serialUtil = new SerialUtil();
		}
	}
	
	//私有化SerialTool类的构造方法，不允许其他类生成SerialTool对象
	private SerialUtil() {}

	//处理Java对无符号整数的不支持问题
	public static int getUnsignedByte(byte data){
	    return data&0xFF;
    }
	
	/**
	 * 获取提供服务的SerialUtil对象
	 * @return serialUtil
	 */
	public static SerialUtil getSerialUtil() {
		if (serialUtil == null) {
            serialUtil = new SerialUtil();
		}
		return serialUtil;
	}


	/**
	 * 查找所有可用端口
	 * @return 可用端口名称列表
	 */
	public static final ArrayList<CommInfo> findAvailablePorts() {

        //将可用串口名添加到List并返回该List
        ArrayList<CommInfo> portCommList = new ArrayList<>();

        //获得当前所有可用串口
        Enumeration<CommPortIdentifier> portList = CommPortIdentifier.getPortIdentifiers();
        while (portList.hasMoreElements()) {
            CommPortIdentifier commPortIdentifier =  portList.nextElement();
            portCommList.add(new CommInfo(commPortIdentifier.getName(), (long)commPortIdentifier.getPortType()));
        }

        return portCommList;

    }
    
    /**
     * 打开串口
     * @param portName 端口名称
     * @param baudrate 波特率
     * @return 串口对象
     * @throws SerialPortParameterFailure 设置串口参数失败
     * @throws NotASerialPort 端口指向设备不是串口类型
     * @throws NoSuchPort 没有该端口对应的串口设备
     * @throws PortInUse 端口已被占用
     */
    public static final SerialPort openPort(String portName, int baudrate) throws SerialPortParameterFailure, NotASerialPort, NoSuchPort, PortInUse {

        try {

            //通过端口名识别端口
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);

            //打开端口，并给端口名字和一个timeout（打开操作的超时时间）
            CommPort commPort = portIdentifier.open(portName, ConstantUtil.OPEN_PORT_TIMESPAN);

            //判断是不是串口
            if (commPort instanceof SerialPort) {
            	
                SerialPort serialPort = (SerialPort) commPort;
                
                try {                    	
                    //设置一下串口的波特率等参数
                    serialPort.setSerialPortParams(baudrate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);                              
                } catch (UnsupportedCommOperationException e) {  
                	throw new SerialPortParameterFailure();
                }

                LoggerUtil.getInstance().info("Open " + portName + " sucessfully !");

                return serialPort;
            
            }        
            else {
            	//不是串口
            	throw new NotASerialPort();
            }
        } catch (NoSuchPortException e1) {
          throw new NoSuchPort();
        } catch (PortInUseException e2) {
        	throw new PortInUse();
        }
    }
    
    /**
     * 关闭串口
     * @param待关闭的串口对象
     */
    public static void closePort(SerialPort serialPort) {
    	if (serialPort != null) {
    		serialPort.close();
    		serialPort = null;
    	}
    }

    /**
     * 往串口发送数据
     * @param serialPort 串口对象
     * @param order	待发送数据
     * @throws SendDataToSerialPortFailure 向串口发送数据失败
     * @throws SerialPortOutputStreamCloseFailure 关闭串口对象的输出流出错
     */
    public static void sendToPort(SerialPort serialPort, byte[] order) throws SendDataToSerialPortFailure, SerialPortOutputStreamCloseFailure {

    	OutputStream out = null;
    	
        try {
        	
            out = serialPort.getOutputStream();
            out.write(order);
            out.flush();
            
        } catch (IOException e) {
        	throw new SendDataToSerialPortFailure();
        } finally {
        	try {
        		if (out != null) {
        			out.close();
        			out = null;
        		}				
			} catch (IOException e) {
				throw new SerialPortOutputStreamCloseFailure();
			}
        }
        
    }
    
    /**
     * 添加监听器
     * @param port     串口对象
     * @param listener 串口监听器
     * @throws TooManyListeners 监听类对象过多
     */
    public static void addListener(SerialPort port, SerialPortEventListener listener) throws TooManyListeners {

        try {
        	
            //给串口添加监听器
            port.addEventListener(listener);
            //设置当有数据到达时唤醒监听接收线程
            port.notifyOnDataAvailable(true);
          //设置当通信中断时唤醒中断线程
            port.notifyOnBreakInterrupt(true);

        } catch (TooManyListenersException e) {
        	throw new TooManyListeners();
        }
    }
    
    
}
