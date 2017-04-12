package com.circletech.smartconnect;

import com.circletech.smartconnect.config.CustomConfig;
import com.circletech.smartconnect.data.*;
import com.circletech.smartconnect.exception.*;
import com.circletech.smartconnect.CommInfo;
import com.circletech.smartconnect.parser.DistanceParser;
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
    private DeviceDistanceService deviceDistanceService;
    private DevicePositionService devicePositionService;
    private DeviceTransducerDataService deviceTransducerDataService;
    private DeviceSystemInfoService deviceSystemInfoService;
    private BasePositionService basePositionService;
    private DeviceBadDistanceService deviceBadDistanceService;

    //sample wrapper data for the comm data in observer thread
    private DevicePositionData positionData;
    private DeviceTransducerDataData transducerDataData;
    private DeviceSystemInfoData deviceSystemInfoData;
    private DeviceDistanceData deviceDistanceData;

    //data type for observered data
    private static final int FLAG_POSDATA = 1;//for position
    private static final int FLAG_SENSORDATA = 2;//for device

    //receive data wrapper for three kind of comm data
    private BlockingQueue<ParserDataTask> posDataBuffer;
    private BlockingQueue<ParserDataTask> sensorDataBuffer;
    private BlockingQueue<ParserDataTask> systemInfoBuffer;

    //The tag for one parsed distance in the location queue
    private DataTag dataTag;

    //The queue for three comm data
    private OutputPositionBuffer outputPositionBuffer;
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

    public void constructor(DeviceDistanceService deviceDistanceService,
                            DevicePositionService devicePositionService,
                            DeviceTransducerDataService deviceTransducerDataService,
                            DeviceSystemInfoService deviceSystemInfoService,
                            BasePositionService basePositionService,
            DevicePositionData devicePositionData, DeviceTransducerDataData deviceTransducerDataData,
                            DeviceSystemInfoData deviceSystemInfoData,
                            CustomConfig customConfig,
                            DeviceBadDistanceService deviceBadDistanceService){
        this.deviceDistanceService = deviceDistanceService;
        this.devicePositionService = devicePositionService;
        this.deviceTransducerDataService = deviceTransducerDataService;
        this.deviceSystemInfoService = deviceSystemInfoService;
        this.basePositionService = basePositionService;

        this.positionData = devicePositionData;
        this.transducerDataData = deviceTransducerDataData;
        this.deviceSystemInfoData = deviceSystemInfoData;

        this.customConfig = customConfig;
        int maxsize = this.customConfig.getDataQueueSize();

        this.posDataBuffer = new LinkedBlockingQueue<ParserDataTask>(maxsize);
        this.sensorDataBuffer = new LinkedBlockingQueue<ParserDataTask>(maxsize);
        this.systemInfoBuffer = new LinkedBlockingQueue<ParserDataTask>(maxsize);


        this.dataTag = new DataTag();
        this.deviceDistanceData = new DeviceDistanceData();

        this.outputPositionBuffer = new OutputPositionBuffer(maxsize);
        this.outputTransducerBuffer = new OutputTransducerBuffer(maxsize);
        this.outputSystemInfoBuffer = new OutputSystemInfoBuffer(maxsize);

        this.deviceBadDistanceService = deviceBadDistanceService;

        isActive = false;
    }

    public DeviceDistanceService getDeviceDistanceService() {
        return deviceDistanceService;
    }

    public void setDeviceDistanceService(DeviceDistanceService deviceDistanceService) {
        this.deviceDistanceService = deviceDistanceService;
    }

    public DevicePositionService getDevicePositionService() {
        return devicePositionService;
    }

    public void setDevicePositionService(DevicePositionService devicePositionService) {
        this.devicePositionService = devicePositionService;
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

    public BasePositionService getBasePositionService() {
        return basePositionService;
    }

    public void setBasePositionService(BasePositionService basePositionService) {
        this.basePositionService = basePositionService;
    }

    public DevicePositionData getPositionData() {
        return positionData;
    }

    public void setPositionData(DevicePositionData positionData) {
        this.positionData = positionData;

        setChanged();
        notifyObservers(FLAG_POSDATA);//position data change to observer
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
        private DeviceDistanceService deviceDistanceService;
        private DeviceTransducerDataService deviceTransducerDataService;
        private DeviceSystemInfoService deviceSystemInfoService;
        private DevicePositionService devicePositionService;
        private BasePositionService basePositionService;

        //wrapper for comm data
        private DevicePositionData devicePositionData;
        private DeviceTransducerDataData deviceTransducerDataData;

        //data observer
        private CommDataProcessor CommDataProcessor;

        private byte[] buffer = null;
        private byte[] newbuffer = null;

        byte last2 = 0;
        byte last3 = 0;
        byte last5 = 0;
        byte last6 = 0;

        long lasttimestamp = 0;

        public SerialListener(DeviceDistanceService deviceDistanceService,
                              DeviceTransducerDataService deviceTransducerDataService,
                              DevicePositionService devicePositionService,
                              DeviceSystemInfoService deviceSystemInfoService,
                              BasePositionService basePositionService,
                              DevicePositionData devicePositionData,
                              DeviceTransducerDataData deviceTransducerDataData,
                              CommDataProcessor CommDataProcessor){
            super();

            this.deviceDistanceService = deviceDistanceService;
            this.deviceTransducerDataService = deviceTransducerDataService;
            this.deviceSystemInfoService = deviceSystemInfoService;
            this.devicePositionService = devicePositionService;
            this.basePositionService = basePositionService;

            this.devicePositionData = devicePositionData;
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

                case SerialPortEvent.BI: // 10 通讯中断
                    break;

                case SerialPortEvent.OE: // 7 溢位（溢出）错误

                case SerialPortEvent.FE: // 9 帧错误

                case SerialPortEvent.PE: // 8 奇偶校验错误

                case SerialPortEvent.CD: // 6 载波检测

                case SerialPortEvent.CTS: // 3 清除待发送数据

                case SerialPortEvent.DSR: // 4 待发送数据准备好了

                case SerialPortEvent.RI: // 5 振铃指示

                case SerialPortEvent.OUTPUT_BUFFER_EMPTY: // 2 输出缓冲区已清空
                    break;
                case SerialPortEvent.DATA_AVAILABLE: // 1 串口存在可用数据
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
                                        //自定义解析过程
                                        if (buffer == null || buffer.length < 1) {//check whether the data is right
                                            if (buffer == null) {
                                                LoggerUtil.getInstance().info("buffer is null");
                                            }
                                            LoggerUtil.getInstance().info("buffer.length:" + buffer.length);
                                        } else {

                                            while (buffer.length >= 4) //开始位、长度位、校验位、结束位
                                            {
                                                //1.1 查找数据头
                                                if (buffer[0] == (byte) 0x51) //传输数据有帧头，用于判断
                                                {
                                                    if (buffer[1] == (byte) 0xFF && buffer[2] == (byte) 0x00 && buffer[3] == (byte) 0xF0) {
                                                        buffer = Arrays.copyOfRange(buffer, 4, buffer.length);
                                                        continue;
                                                    }

                                                    int len = buffer[1];
                                                    if(len <= 0){//无效的长度数据
                                                        buffer = Arrays.copyOfRange(buffer, 1, buffer.length);
                                                        continue;
                                                    }

                                                    if (buffer.length < len + 4) //数据区尚未接收完整
                                                    {
                                                        break;
                                                    }
                                                    //获取和校验的值
                                                    byte jiaoyan = GetSum(buffer, 2, len);
                                                    if (jiaoyan != buffer[2 + len]) //和校验
                                                    {
                                                        buffer = Arrays.copyOfRange(buffer, len + 4, buffer.length);
                                                        continue;
                                                    }
                                                    if (buffer[2 + len + 1] != (byte) 0xF0)//判断结束位
                                                    {
                                                        buffer = Arrays.copyOfRange(buffer, len + 4, buffer.length);
                                                        continue;
                                                    }
                                                    if (buffer[4] != (byte) 0x01 && buffer[4] != (byte) 0x02 && buffer[4] != (byte) 0x03)//判断数据类型
                                                    {
                                                        buffer = Arrays.copyOfRange(buffer, len + 4, buffer.length);
                                                        continue;
                                                    }

                                                    byte[] b;
                                                    b = Arrays.copyOfRange(buffer, 0, len + 4);

                                                    buffer = Arrays.copyOfRange(buffer, len + 4, buffer.length);

                                                    //数据处理
                                                    switch (b[4]) {
                                                        case 0x01:
                                                            //启动位置信息处理线程
                                                            try {
                                                                long timestamp = 0;

                                                                //确保连续的四个基站的数据具有相同的时间戳
                                                                if(last2 == 0 && last3 == 0 && last5 == 0 && last6 == 0){
                                                                    last2 = b[2];
                                                                    last3 = b[3];
                                                                    last5 = b[5];
                                                                    last6 = b[6];

                                                                    timestamp = System.currentTimeMillis();
                                                                }else{
                                                                    if((last2 == b[2] && last3 == b[3]) && last5 != b[5]||last6 != b[6]){
                                                                        timestamp = lasttimestamp;
                                                                    }else{
                                                                        timestamp = System.currentTimeMillis();

                                                                        last2 = b[2];
                                                                        last3 = b[3];
                                                                        last5 = b[5];
                                                                        last6 = b[6];
                                                                    }
                                                                }

                                                                ParserDataTask task = new ParserDataTask((long) b[4], b, timestamp);
                                                                posDataBuffer.put(task);
                                                                lasttimestamp = timestamp;


                                                            } catch (InterruptedException e) {
                                                                e.printStackTrace();
                                                            }
                                                            break;
                                                        case 0x02:
                                                            //启动传感器数据信息处理线程
                                                            try {
                                                                ParserDataTask task = new ParserDataTask((long) b[4], b, System.currentTimeMillis());
                                                                sensorDataBuffer.put(task);
                                                            } catch (InterruptedException e) {
                                                                e.printStackTrace();
                                                            }
                                                            break;
                                                        case 0x03:
                                                            //启动系统信息处理线程
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
                                                } else {//帧头不正确时，清除该byte
                                                    LoggerUtil.getInstance().info("帧头不正确时，清除该byte");
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
                            LoggerUtil.getInstance().info("串口对象初始化失败");
                        }

                    } catch (ReadDataFromSerialPortFailure | SerialPortInputStreamCloseFailure e) {
                        isActive = false;
                        e.printStackTrace();
                        LoggerUtil.getInstance().info(e.getMessage());
                        LoggerUtil.getInstance().info("发生读取错误时显示错误信息");
                    }
                }
                break;

            }

        }

    }

    @Override
    public void run() {
        try {

            //获取指定端口名及波特率的串口对象
            serialPort = SerialUtil.openPort(serialPortName, baudrate);
            LoggerUtil.getInstance().info("open serial name:" + serialPortName);

            //在该串口对象上添加监听器
            //串口数据生产者
            SerialUtil.addListener(serialPort, new SerialListener(this.deviceDistanceService,
                    this.deviceTransducerDataService,
                    this.devicePositionService,
                    this.deviceSystemInfoService,
                    this.basePositionService,
                    this.positionData,
                    this.transducerDataData,
                    this
            ));


            //串口二进制数据消费者
            ExecutorService cachedThreadPool = ThreadPoolUtil.getInstance();
            int numofconsumer = this.customConfig.getConsumerThreadSize();
            for(int i = 0; i < numofconsumer; i++){
                cachedThreadPool.execute(new DistanceParser(
                        deviceDistanceService, basePositionService,
                        this.positionData,
                        devicePositionService,
                        this,
                        this.posDataBuffer,
                        dataTag,
                        deviceDistanceData,
                        outputPositionBuffer,
                        deviceBadDistanceService

                ));
            }

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

            //串口数据发送和存储者
            int numofsender = this.customConfig.getSenderthreadsize();
            for(int i = 0; i < numofsender; i++){
                cachedThreadPool.execute(new PositionSender(
                        this.devicePositionService,
                        this,
                        this.outputPositionBuffer,
                        this.positionData
                ));
            }

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

            //四个定时任务做清理数据
            ScheduledExecutorService service = Executors
                    .newSingleThreadScheduledExecutor();
            // 第二个参数为首次执行的延时时间，第三个参数为定时执行的间隔时间
            service.scheduleAtFixedRate(new ScheduledPositionSender(this.devicePositionService,
                    this,
                    this.outputPositionBuffer,
                    this.positionData), ConstantUtil.SCHEDULE_CLEAR_INIT, ConstantUtil.SCHEDULE_SENDER, TimeUnit.MILLISECONDS);

            service.scheduleAtFixedRate(new ScheduledTransducerSender(this.deviceTransducerDataService,
                    this,
                    this.outputTransducerBuffer,
                    this.transducerDataData), ConstantUtil.SCHEDULE_CLEAR_INIT + 1, ConstantUtil.SCHEDULE_SENDER, TimeUnit.MILLISECONDS);

            service.scheduleAtFixedRate(new ScheduledSystemInfoSender(this.deviceSystemInfoService,
                    this.outputSystemInfoBuffer,
                    this.deviceSystemInfoData), ConstantUtil.SCHEDULE_CLEAR_INIT + 2, ConstantUtil.SCHEDULE_SENDER, TimeUnit.MILLISECONDS);

            service.scheduleAtFixedRate(new ScheduledRemoveDataTag(this.dataTag),
                    ConstantUtil.SCHEDULE_CLEAR_INIT + 3, ConstantUtil.CLEAR_LOSS_DISTANCE_DATA_SPAN, TimeUnit.MILLISECONDS);



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
