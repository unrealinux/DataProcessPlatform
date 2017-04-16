package com.circletech.smartconnect.util;

/**
 * Created by Administrator on 2017/4/14.
 */

import com.circletech.smartconnect.ParserDataTask;
import com.circletech.smartconnect.CommDataProcessor;
import com.circletech.smartconnect.data.DeviceTransducerDataData;
import com.circletech.smartconnect.exception.ReadDataFromSerialPortFailure;
import com.circletech.smartconnect.exception.SerialPortInputStreamCloseFailure;
import com.circletech.smartconnect.service.*;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import javafx.util.Builder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;

public class SerialListener implements SerialPortEventListener {

    //数据存储服务
    private DeviceTransducerDataService deviceTransducerDataService;
    private DeviceSystemInfoService deviceSystemInfoService;

    //加入写保护的具体数据类
    private DeviceTransducerDataData deviceTransducerDataData;

    //作为被观察者传入监控上面具体数据类的变化
    private CommDataProcessor commDataProcessor;

    private boolean isActive;
    private SerialPort serialPort = null; //保存串口对象

    //三种信息的单独二进制数据对象任务队列
    private BlockingQueue<ParserDataTask> sensorDataBuffer;
    private BlockingQueue<ParserDataTask> systemInfoBuffer;

    private byte[] buffer = null;
    private byte[] newbuffer = null;

    private SerialListener(SerialListenerBuilder builder){

        super();

        this.deviceTransducerDataService = builder.deviceTransducerDataService;
        this.deviceSystemInfoService = builder.deviceSystemInfoService;

        this.deviceTransducerDataData = builder.deviceTransducerDataData;

        this.commDataProcessor = builder.commDataProcessor;

        this.serialPort = builder.serialPort;

        this.sensorDataBuffer = builder.sensorDataBuffer;
        this.systemInfoBuffer = builder.systemInfoBuffer;
    }

    public static class SerialListenerBuilder implements Builder<SerialListener> {

        //数据存储服务
        private DeviceTransducerDataService deviceTransducerDataService;
        private DeviceSystemInfoService deviceSystemInfoService;

        //加入写保护的具体数据类
        private DeviceTransducerDataData deviceTransducerDataData;

        //作为被观察者传入监控上面具体数据类的变化
        private CommDataProcessor commDataProcessor;

        private boolean isActive;
        private SerialPort serialPort = null; //保存串口对象

        //三种信息的单独二进制数据对象任务队列
        private BlockingQueue<ParserDataTask> sensorDataBuffer;
        private BlockingQueue<ParserDataTask> systemInfoBuffer;


        public SerialListenerBuilder deviceTransducerDataService(DeviceTransducerDataService deviceTransducerDataService){
            this.deviceTransducerDataService = deviceTransducerDataService;
            return this;
        }

        public SerialListenerBuilder deviceSystemInfoService(DeviceSystemInfoService deviceSystemInfoService){
            this.deviceSystemInfoService = deviceSystemInfoService;
            return this;
        }

        public SerialListenerBuilder deviceTransducerDataData(DeviceTransducerDataData deviceTransducerDataData){
            this.deviceTransducerDataData = deviceTransducerDataData;
            return this;
        }

        public SerialListenerBuilder commDataProcessor(CommDataProcessor commDataProcessor){
            this.commDataProcessor = commDataProcessor;
            return this;
        }

        public SerialListenerBuilder isActive(boolean isActive){
            this.isActive = isActive;
            return this;
        }

        public SerialListenerBuilder sensorDataBuffer(BlockingQueue<ParserDataTask> sensorDataBuffer){
            this.sensorDataBuffer = sensorDataBuffer;
            return this;
        }

        public SerialListenerBuilder systemInfoBuffer(BlockingQueue<ParserDataTask> systemInfoBuffer){
            this.systemInfoBuffer = systemInfoBuffer;
            return this;
        }

        public SerialListenerBuilder serialPort(SerialPort serialPort){
            this.serialPort = serialPort;
            return this;
        }

        public SerialListener build(){
            return new SerialListener(this);
        }
    }

    private byte GetSum(byte[] data, int start, int length)
    {
        short sum = 0;
        //累加求和
        for (int i = start; i < start + length; i++)
        {
            sum += data[i];
        }
        //和验证
        return (byte)(sum % 256);
    }

    /**
     * 处理监控到的串口事件
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
                            int bufflenth = in.available();		//获取buffer里的数据长度

                            while (bufflenth != 0) {

                                byte[] bytes = null;
                                bytes = new byte[bufflenth];	//初始化byte数组为buffer中数据的长度
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
                                    if (buffer == null || buffer.length < 1) {//检查数据是否读取正确
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
