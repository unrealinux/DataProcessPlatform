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
 * Serial service class, to open, close the serial port, read, send serial data services (using a single design model)
 *
 */
public class SerialUtil {
	
	private static SerialUtil serialUtil = null;
	
	static {
		//Initializes a SerialTool object when the class is loaded by ClassLoader
		if (serialUtil == null) {
            serialUtil = new SerialUtil();
		}
	}
	
	//The construction method of the private SerialTool class does not allow other classes to generate SerialTool objects
	private SerialUtil() {}

	//Processing Java for unsigned integers not supported
	public static int getUnsignedByte(byte data){
	    return data&0xFF;
    }
	
	/**
	 * Gets the SerialUtil object that provides the service
	 * @return serialUtil
	 */
	public static SerialUtil getSerialUtil() {
		if (serialUtil == null) {
            serialUtil = new SerialUtil();
		}
		return serialUtil;
	}


	/**
	 * Find all available ports
	 * @return Available port name list
	 */
	public static final ArrayList<CommInfo> findAvailablePorts() {

        //Adds the available serial name to List and returns the List
        ArrayList<CommInfo> portCommList = new ArrayList<>();

        //Get all currently available serial ports
        Enumeration<CommPortIdentifier> portList = CommPortIdentifier.getPortIdentifiers();
        while (portList.hasMoreElements()) {
            CommPortIdentifier commPortIdentifier =  portList.nextElement();
            portCommList.add(new CommInfo(commPortIdentifier.getName(), (long)commPortIdentifier.getPortType()));
        }

        return portCommList;

    }
    
    /**
     * open serial
     * @param portName
     * @param baudrate
     * @return serial object
     * @throws SerialPortParameterFailure Failed to set serial port parameters
     * @throws NotASerialPort Port pointing device is not a serial port type
     * @throws NoSuchPort There is no corresponding serial port device
     * @throws PortInUse Port is occupied
     */
    public static final SerialPort openPort(String portName, int baudrate) throws SerialPortParameterFailure, NotASerialPort, NoSuchPort, PortInUse {

        try {

            //Port name
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);

            //Open the port, and give the port name and a timeout (open operation timeout)
            CommPort commPort = portIdentifier.open(portName, ConstantUtil.OPEN_PORT_TIMESPAN);

            //Determine whether the serial port
            if (commPort instanceof SerialPort) {
            	
                SerialPort serialPort = (SerialPort) commPort;
                
                try {                    	
                    //Set the serial baud rate and other parameters
                    serialPort.setSerialPortParams(baudrate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);                              
                } catch (UnsupportedCommOperationException e) {  
                	throw new SerialPortParameterFailure();
                }

                LoggerUtil.getInstance().info("Open " + portName + " sucessfully !");

                return serialPort;
            
            }        
            else {
            	//not a serial
            	throw new NotASerialPort();
            }
        } catch (NoSuchPortException e1) {
          throw new NoSuchPort();
        } catch (PortInUseException e2) {
        	throw new PortInUse();
        }
    }
    
    /**
     * Close serial
     * @paramSerial object to be closed
     */
    public static void closePort(SerialPort serialPort) {
    	if (serialPort != null) {
    		serialPort.close();
    		serialPort = null;
    	}
    }

    /**
     * Send data to the serial port
     * @param serialPort Serial object
     * @param order	Data to be transmitted
     * @throws SendDataToSerialPortFailure Failed to send data to serial port
     * @throws SerialPortOutputStreamCloseFailure Error closing the output stream of serial objects
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
     * Add listener
     * @param port     Serial object
     * @param listener Serial listener
     * @throws TooManyListeners Too many listener objects
     */
    public static void addListener(SerialPort port, SerialPortEventListener listener) throws TooManyListeners {

        try {
        	
            //Add a listener to the serial port
            port.addEventListener(listener);
            //Set up the monitor receive thread when there is data arrival
            port.notifyOnDataAvailable(true);
            //Sets the interrupt thread when communication is interrupted
            port.notifyOnBreakInterrupt(true);

        } catch (TooManyListenersException e) {
        	throw new TooManyListeners();
        }
    }
    
    
}
