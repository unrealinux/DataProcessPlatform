package com.circletech.smartconnect.parser;

import com.circletech.smartconnect.ParserDataTask;
import com.circletech.smartconnect.data.OutputSystemInfoBuffer;
import com.circletech.smartconnect.data.SystemInfoTimeStampData;
import com.circletech.smartconnect.model.DeviceSystemInfo;
import com.circletech.smartconnect.util.ConstantUtil;
import com.circletech.smartconnect.util.LoggerUtil;
import com.circletech.smartconnect.util.SerialUtil;
import javafx.util.Builder;

import java.sql.Timestamp;
import java.util.concurrent.BlockingQueue;

/**
 * Created by xieyingfei on 2016/12/15.
 */
public class SystemInfoParser implements Runnable{
    private byte[] mData;
    private BlockingQueue<ParserDataTask> dataBuffer;
    private OutputSystemInfoBuffer outputSystemInfoBuffer;

    public static class SystemInfoParserBuilder implements Builder<SystemInfoParser>{
        private BlockingQueue<ParserDataTask> dataBuffer;
        private OutputSystemInfoBuffer outputSystemInfoBuffer;


        public SystemInfoParserBuilder dataBuffer(BlockingQueue<ParserDataTask> dataBuffer){
            this.dataBuffer = dataBuffer;
            return this;
        }

        public SystemInfoParserBuilder outputSystemInfoBuffer(OutputSystemInfoBuffer outputSystemInfoBuffer){
            this.outputSystemInfoBuffer = outputSystemInfoBuffer;
            return this;
        }

        public SystemInfoParser build(){
            return new SystemInfoParser(this);
        }
    }

    public SystemInfoParser(SystemInfoParserBuilder builder){
        this.dataBuffer = builder.dataBuffer;
        this.outputSystemInfoBuffer = builder.outputSystemInfoBuffer;
    }

    public void run(){

        while (true){

            try{
                    ParserDataTask task = dataBuffer.take();
                    mData = task.getData();
                    //系统信息代码
                    int deviceID = SerialUtil.getUnsignedByte(mData[2]) * 256 + SerialUtil.getUnsignedByte(mData[3]);
                    int infoCode = SerialUtil.getUnsignedByte(mData[5]);
                    String infoContent = "";

                    switch (infoCode) {
                        case 0x00:
                            infoContent = "设备未知错误";
                            break;
                        case 0x01:
                            infoContent = "设备启动";
                            break;
                        case 0x02:
                            infoContent = "设备电量低";
                            break;
                        case 0x03:
                            infoContent = "设备无法正常连接传感器:" + deviceID;
                            break;
                        case 0x04:
                            infoContent = "设备无法定位";
                            break;
                        case 0xEE:
                            infoContent = "设备文字消息";
                            break;
                    }

                    DeviceSystemInfo deviceSystemInfo = new DeviceSystemInfo((long)deviceID, (long)infoCode, infoContent, new Timestamp(task.getTimestamp()), new Timestamp(task.getTimestamp()));

                    outputSystemInfoBuffer.add(new SystemInfoTimeStampData(deviceSystemInfo, task.getTimestamp()));

                    try{
                        Thread.sleep(ConstantUtil.THREAD_SLEEP_SPAN);
                    }catch (InterruptedException e){
                        e.printStackTrace();

                    }

            }catch (InterruptedException e){
                e.printStackTrace();
                LoggerUtil.getInstance().info(e.getMessage());
            }
        }
    }
}
