package com.circletech.smartconnect;

import com.circletech.smartconnect.config.CustomConfig;
import com.circletech.smartconnect.data.*;
import com.circletech.smartconnect.exception.*;
import com.circletech.smartconnect.parser.SystemInfoParser;
import com.circletech.smartconnect.parser.TransducerDataParser;
import com.circletech.smartconnect.sender.*;
import com.circletech.smartconnect.service.*;
import com.circletech.smartconnect.util.*;
import gnu.io.SerialPort;
import java.util.ArrayList;
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
    private static final int FLAG_SENSORDATA = 2;//for device

    //receive data wrapper for three kind of comm data
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


    @Override
    public void run() {
        try {

            //get the serial object with specific port and baudrate
            serialPort = SerialUtil.openPort(serialPortName, baudrate);
            LoggerUtil.getInstance().info("open serial name:" + serialPortName);

            //add listener on the serail object
            SerialUtil.addListener(serialPort, new com.circletech.smartconnect.util.SerialListener.SerialListenerBuilder()
                    .deviceSystemInfoService(deviceSystemInfoService)
                    .deviceTransducerDataService(deviceTransducerDataService)
                    .deviceTransducerDataData(transducerDataData)
                    .commDataProcessor(this)
                    .sensorDataBuffer(sensorDataBuffer)
                    .systemInfoBuffer(systemInfoBuffer)
                    .serialPort(serialPort)
                    .build());


            ExecutorService cachedThreadPool = ThreadPoolUtil.getInstance();
            int numofconsumer = this.customConfig.getConsumerThreadSize();
            for (int i = 0; i < numofconsumer; i++){
                cachedThreadPool.execute(new TransducerDataParser.TransducerDataParserBuilder()
                .dataBuffer(sensorDataBuffer)
                .outputTransducerBuffer(outputTransducerBuffer)
                .build());
            }

            for(int i = 0; i < numofconsumer; i++){
                cachedThreadPool.execute(new SystemInfoParser.SystemInfoParserBuilder()
                .dataBuffer(systemInfoBuffer)
                .outputSystemInfoBuffer(outputSystemInfoBuffer)
                .build());
            }

            int numofsender = this.customConfig.getSenderthreadsize();
            for(int i = 0; i < numofsender; i++){
                cachedThreadPool.execute(new TransducerSender.TransducerSenderBuilder()
                .outputTransducerBuffer(outputTransducerBuffer)
                .deviceTransducerData(transducerDataData)
                .deviceTransducerDataService(deviceTransducerDataService)
                .commDataProcessor(this)
                .build());
            }

            for(int i = 0; i < 1; i++) {
                cachedThreadPool.execute(new SystemInfoSender.SystemInfoSenderBuilder()
                .outputSystemInfoBuffer(outputSystemInfoBuffer)
                .deviceSystemInfoData(deviceSystemInfoData)
                .deviceSystemInfoService(deviceSystemInfoService)
                .build());
            }

            //four timer for clear data
            ScheduledExecutorService service = Executors
                    .newSingleThreadScheduledExecutor();
            service.scheduleAtFixedRate(new ScheduledTransducerSender.ScheduledTransducerSenderBuilder()
                    .commDataProcessor(this)
                    .deviceTransducerData(transducerDataData)
                    .deviceTransducerDataService(deviceTransducerDataService)
                    .outputTransducerBuffer(outputTransducerBuffer).build(), ConstantUtil.SCHEDULE_CLEAR_INIT + 1, ConstantUtil.SCHEDULE_SENDER, TimeUnit.MILLISECONDS);

            service.scheduleAtFixedRate(new ScheduledSystemInfoSender.ScheduledSystemInfoSenderBuilder()
                    .deviceSystemInfoService(deviceSystemInfoService)
                    .deviceSystemInfoData(deviceSystemInfoData)
                    .outputSystemInfoBuffer(outputSystemInfoBuffer).build(), ConstantUtil.SCHEDULE_CLEAR_INIT + 2, ConstantUtil.SCHEDULE_SENDER, TimeUnit.MILLISECONDS);

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
