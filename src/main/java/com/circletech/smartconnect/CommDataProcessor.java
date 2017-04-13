package com.circletech.smartconnect;

import com.circletech.smartconnect.config.CustomConfig;
import com.circletech.smartconnect.data.*;
import com.circletech.smartconnect.exception.*;
import com.circletech.smartconnect.parser.SystemInfoParser;
import com.circletech.smartconnect.parser.TransducerDataParser;
import com.circletech.smartconnect.sender.*;
import com.circletech.smartconnect.service.*;
import com.circletech.smartconnect.util.ConstantUtil;
import com.circletech.smartconnect.util.LoggerUtil;
import com.circletech.smartconnect.util.SerialUtil;
import com.circletech.smartconnect.util.ThreadPoolUtil;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Observable;
import java.util.concurrent.*;
import com.circletech.smartconnect.network.CommInfo;

/**
 * Created by xieyingfei on 2016/12/7.
 */
public class CommDataProcessor extends Observable implements Runnable{

    private CommDataProcessor() {
    }
    private static class SingletonInstance {
        private static final CommDataProcessor INSTANCE = new CommDataProcessor();
    }
    public static CommDataProcessor getInstance() {
        return CommDataProcessor.SingletonInstance.INSTANCE;
    }

    private SerialPort serialPort = null; //store comm object
    private String serialPortName;//open comm name
    private int baudrate;//open comm baudrate

    //sample data store service involve position information
    private DeviceTransducerDataService deviceTransducerDataService;
    private DeviceSystemInfoService deviceSystemInfoService;

    //sample wrapper data for the comm data in observer thread
    private DeviceTransducerDataData transducerDataData;
    private DeviceSystemInfoData deviceSystemInfoData;

    //data type for observered data
    private static final int FLAG_POSDATA = 1;//for position
    private static final int FLAG_SENSORDATA = 2;//for device

    //receive data wrapper for three kind of comm data
    private BlockingQueue<ParserDataTask> posDataBuffer;
    private BlockingQueue<ParserDataTask> sensorDataBuffer;
    private BlockingQueue<ParserDataTask> systemInfoBuffer;

    //The queue for three comm data
    private OutputTransducerBuffer outputTransducerBuffer;
    private OutputSystemInfoBuffer outputSystemInfoBuffer;

    //custom configuration information
    private CustomConfig customConfig;

    public CustomConfig getCustomConfig() {
        return customConfig;
    }

    public void setCustomConfig(CustomConfig customConfig) {
        this.customConfig = customConfig;
    }

    private boolean isActive;

    public void constructor(DeviceTransducerDataService deviceTransducerDataService,
                            DeviceSystemInfoService deviceSystemInfoService,
                            DeviceTransducerDataData deviceTransducerDataData,
                            DeviceSystemInfoData deviceSystemInfoData,
                            CustomConfig customConfig){
        this.deviceTransducerDataService = deviceTransducerDataService;
        this.deviceSystemInfoService = deviceSystemInfoService;

        this.transducerDataData = deviceTransducerDataData;
        this.deviceSystemInfoData = deviceSystemInfoData;

        this.customConfig = customConfig;
        int maxsize = this.customConfig.getDataQueueSize();

        this.posDataBuffer = new LinkedBlockingQueue<ParserDataTask>(maxsize);
        this.sensorDataBuffer = new LinkedBlockingQueue<ParserDataTask>(maxsize);
        this.systemInfoBuffer = new LinkedBlockingQueue<ParserDataTask>(maxsize);

        this.outputTransducerBuffer = new OutputTransducerBuffer(maxsize);
        this.outputSystemInfoBuffer = new OutputSystemInfoBuffer(maxsize);

        isActive = false;
    }

    public DeviceTransducerDataService getDeviceTransducerDataService() {
        return deviceTransducerDataService;
    }

    public void setDeviceTransducerDataService(DeviceTransducerDataService deviceTransducerDataService) {
        this.deviceTransducerDataService = deviceTransducerDataService;
    }

    public DeviceSystemInfoService getDeviceSystemInfoService() {
        return deviceSystemInfoService;
    }

    public void setDeviceSystemInfoService(DeviceSystemInfoService deviceSystemInfoService) {
        this.deviceSystemInfoService = deviceSystemInfoService;
    }

    public DeviceTransducerDataData getTransducerDataData() {
        return transducerDataData;
    }

    public void setTransducerDataData(DeviceTransducerDataData transducerDataData) {
        this.transducerDataData = transducerDataData;

        setChanged();
        notifyObservers(FLAG_SENSORDATA);//transducerdata data change to observer
    }

    public void setSerialPortName(String serialPortName){
        this.serialPortName=serialPortName;
    }

    public String getSerialPortName(){
        return this.serialPortName;
    }

    public static ArrayList<CommInfo> findAvailablePort() {
        return SerialUtil.findAvailablePorts();
    }

    public int getBaudrate() {
        return baudrate;
    }

    public void setBaudrate(int baudrate) {
        this.baudrate = baudrate;
    }

    /**
     * inner class for serial listen class
     */
    private class SerialListener implements SerialPortEventListener {

        //data store service
        private DeviceTransducerDataService deviceTransducerDataService;
        private DeviceSystemInfoService deviceSystemInfoService;

        //wrapper for comm data
        private DeviceTransducerDataData deviceTransducerDataData;

        //data observer
        private CommDataProcessor CommDataProcessor;

        private byte[] buffer = null;
        private byte[] newbuffer = null;


        public SerialListener(DeviceTransducerDataService deviceTransducerDataService,
                              DeviceSystemInfoService deviceSystemInfoService,
                              DeviceTransducerDataData deviceTransducerDataData,
                              CommDataProcessor CommDataProcessor){
            super();

            this.deviceTransducerDataService = deviceTransducerDataService;
            this.deviceSystemInfoService = deviceSystemInfoService;

            this.deviceTransducerDataData = deviceTransducerDataData;

            this.CommDataProcessor = CommDataProcessor;
        }

        private byte GetSum(byte[] data, int start, int length)
        {
            short sum = 0;
            //get sum
            for (int i = start; i < start + length; i++)
            {
                sum += data[i];
            }
            //sum check
            return (byte)(sum % 256);
        }

        /**
         * function for monitor process the event
         */
        public void serialEvent(SerialPortEvent serialPortEvent) {

            switch (serialPortEvent.getEventType()) {

                case SerialPortEvent.BI: // 10 interruption of communication
                    break;

                case SerialPortEvent.OE: // 7 Overflow (overflow) error

                case SerialPortEvent.FE: // 9 frame error

                case SerialPortEvent.PE: // 8 parity error

                case SerialPortEvent.CD: // 6 Carrier detection

                case SerialPortEvent.CTS: // 3 Clear data to be sent

                case SerialPortEvent.DSR: // 4 Ready to send data

                case SerialPortEvent.RI: // 5 Ringing indication

                case SerialPortEvent.OUTPUT_BUFFER_EMPTY: // 2 Output buffer is empty
                    break;
                case SerialPortEvent.DATA_AVAILABLE: // 1 Serial port available data
                {
                    LoggerUtil.getInstance().info("comm get data continue");
                    isActive = true;

                    try {
                        InputStream in = null;

                        if (serialPort != null) {
                            try {

                                in = serialPort.getInputStream();
                                int bufflenth = in.available();		//get the length of buffer

                                while (bufflenth != 0) {

                                    byte[] bytes = null;
                                    bytes = new byte[bufflenth];	//init the byte Array
                                    in.read(bytes);

                                    if(buffer != null){
                                        newbuffer = new byte[buffer.length + bufflenth];

                                        for (int i = 0, len = buffer.length; i < len; i++) {
                                            newbuffer[i] = buffer[i];
                                        }

                                        for (int i = 0, len = bufflenth; i < len; i++) {
                                            newbuffer[i + buffer.length] = bytes[i];
                                        }
                                    }else{
                                        newbuffer = new byte[bufflenth];

                                        for (int i = 0, len = bufflenth; i < len; i++) {
                                            newbuffer[i] = bytes[i];
                                        }
                                    }

                                    buffer = newbuffer;
                                    newbuffer = null;

                                    {

                                        if (buffer == null || buffer.length < 1) {//check whether the data is right
                                            if (buffer == null) {
                                                LoggerUtil.getInstance().info("buffer is null");
                                            }
                                            LoggerUtil.getInstance().info("buffer.length:" + buffer.length);
                                        } else {

                                            while (buffer.length >= 4) //begin bit, length bit, check bit, ending bit
                                            {
                                                //search data head
                                                if (buffer[0] == (byte) 0x51) //transport data frame head
                                                {
                                                    if (buffer[1] == (byte) 0xFF && buffer[2] == (byte) 0x00 && buffer[3] == (byte) 0xF0) {
                                                        buffer = Arrays.copyOfRange(buffer, 4, buffer.length);
                                                        continue;
                                                    }

                                                    int len = buffer[1];
                                                    if(len <= 0){//invalid data length
                                                        buffer = Arrays.copyOfRange(buffer, 1, buffer.length);
                                                        continue;
                                                    }

                                                    if (buffer.length < len + 4) //data scope has not been received completely
                                                    {
                                                        break;
                                                    }
                                                    //get sum of check data
                                                    byte jiaoyan = GetSum(buffer, 2, len);
                                                    if (jiaoyan != buffer[2 + len])
                                                    {
                                                        buffer = Arrays.copyOfRange(buffer, len + 4, buffer.length);
                                                        continue;
                                                    }
                                                    if (buffer[2 + len + 1] != (byte) 0xF0)//check ending bit
                                                    {
                                                        buffer = Arrays.copyOfRange(buffer, len + 4, buffer.length);
                                                        continue;
                                                    }
                                                    if (buffer[4] != (byte) 0x01 && buffer[4] != (byte) 0x02 && buffer[4] != (byte) 0x03)//check data type
                                                    {
                                                        buffer = Arrays.copyOfRange(buffer, len + 4, buffer.length);
                                                        continue;
                                                    }

                                                    byte[] b;
                                                    b = Arrays.copyOfRange(buffer, 0, len + 4);

                                                    buffer = Arrays.copyOfRange(buffer, len + 4, buffer.length);

                                                    //data process
                                                    switch (b[4]) {
                                                        case 0x02:
                                                            //start senor data process thread
                                                            try {
                                                                ParserDataTask task = new ParserDataTask((long) b[4], b, System.currentTimeMillis());
                                                                sensorDataBuffer.put(task);
                                                            } catch (InterruptedException e) {
                                                                e.printStackTrace();
                                                            }
                                                            break;
                                                        case 0x03:
                                                            //start system information process thread
                                                            try {
                                                                ParserDataTask task = new ParserDataTask((long) b[4], b, System.currentTimeMillis());
                                                                systemInfoBuffer.put(task);
                                                            } catch (InterruptedException e) {
                                                                e.printStackTrace();
                                                            }
                                                            break;
                                                        default:
                                                            break;
                                                    }
                                                } else {//frame head is not right, clear the byte
                                                    LoggerUtil.getInstance().info("frame head is not right, clear the byte");
                                                    buffer = Arrays.copyOfRange(buffer, 1, buffer.length);
                                                }
                                            }
                                        }
                                    }
                                    bufflenth = in.available();
                                }
                            } catch (IOException e) {
                                throw new ReadDataFromSerialPortFailure();
                            } finally {
                                try {
                                    if (in != null) {
                                        in.close();
                                        in = null;
                                    }
                                } catch(IOException e) {
                                    throw new SerialPortInputStreamCloseFailure();
                                }
                            }
                        }
                        else {
                            isActive = false;
                            LoggerUtil.getInstance().info("comm object initial failed");
                        }

                    } catch (ReadDataFromSerialPortFailure | SerialPortInputStreamCloseFailure e) {
                        isActive = false;
                        e.printStackTrace();
                        LoggerUtil.getInstance().info(e.getMessage());
                        LoggerUtil.getInstance().info("show error information");
                    }
                }
                break;

            }

        }

    }

    @Override
    public void run() {
        try {

            //get the serial object with specific port and baudrate
            serialPort = SerialUtil.openPort(serialPortName, baudrate);
            LoggerUtil.getInstance().info("open serial name:" + serialPortName);

            //add listener on the serail object
            SerialUtil.addListener(serialPort, new SerialListener(
                    this.deviceTransducerDataService,
                    this.deviceSystemInfoService,
                    this.transducerDataData,
                    this
            ));


            ExecutorService cachedThreadPool = ThreadPoolUtil.getInstance();
            int numofconsumer = this.customConfig.getConsumerThreadSize();
            for (int i = 0; i < numofconsumer; i++){
                cachedThreadPool.execute(new TransducerDataParser(
                        deviceTransducerDataService,
                        this.transducerDataData,
                        this,
                        this.sensorDataBuffer,
                        outputTransducerBuffer
                ));
            }

            for(int i = 0; i < numofconsumer; i++){
                cachedThreadPool.execute(new SystemInfoParser(
                        this.deviceSystemInfoService,
                        this.systemInfoBuffer,
                        this,
                        outputSystemInfoBuffer
                ));
            }

            int numofsender = this.customConfig.getSenderthreadsize();
            for(int i = 0; i < numofsender; i++){
                cachedThreadPool.execute(new TransducerSender(
                        this.deviceTransducerDataService,
                        this,
                        this.outputTransducerBuffer,
                        this.transducerDataData
                ));
            }

            for(int i = 0; i < 1; i++) {
                cachedThreadPool.execute(new SystemInfoSender(
                        this.deviceSystemInfoService,
                        this.outputSystemInfoBuffer,
                        this.deviceSystemInfoData
                ));
            }

            //four timer for clear data
            ScheduledExecutorService service = Executors
                    .newSingleThreadScheduledExecutor();
            service.scheduleAtFixedRate(new ScheduledTransducerSender(this.deviceTransducerDataService,
                    this,
                    this.outputTransducerBuffer,
                    this.transducerDataData), ConstantUtil.SCHEDULE_CLEAR_INIT + 1, ConstantUtil.SCHEDULE_SENDER, TimeUnit.MILLISECONDS);

            service.scheduleAtFixedRate(new ScheduledSystemInfoSender(this.deviceSystemInfoService,
                    this.outputSystemInfoBuffer,
                    this.deviceSystemInfoData), ConstantUtil.SCHEDULE_CLEAR_INIT + 2, ConstantUtil.SCHEDULE_SENDER, TimeUnit.MILLISECONDS);

        } catch (SerialPortParameterFailure | NotASerialPort | NoSuchPort | PortInUse | TooManyListeners e1) {
            isActive = false;
            LoggerUtil.getInstance().info(e1.getMessage());
        }
    }

    public void close() {
        SerialUtil.closePort(serialPort);
        isActive = false;
    }

    public boolean isActive(){
        return isActive;
    }
}
