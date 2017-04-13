package com.circletech.smartconnect.parser;

import com.circletech.smartconnect.ParserDataTask;
import com.circletech.smartconnect.CommDataProcessor;
import com.circletech.smartconnect.data.OutputSystemInfoBuffer;
import com.circletech.smartconnect.data.SystemInfoTimeStampData;
import com.circletech.smartconnect.model.DeviceSystemInfo;
import com.circletech.smartconnect.service.DeviceSystemInfoService;
import com.circletech.smartconnect.util.ConstantUtil;
import com.circletech.smartconnect.util.LoggerUtil;
import com.circletech.smartconnect.util.SerialUtil;

import java.sql.Timestamp;
import java.util.concurrent.BlockingQueue;

/**
 * Created by xieyingfei on 2016/12/15.
 */
public class SystemInfoParser implements Runnable{
    private byte[] mData;
    private DeviceSystemInfoService deviceSystemInfoService;
    private BlockingQueue<ParserDataTask> dataBuffer;
    private CommDataProcessor CommDataProcessor;

    private OutputSystemInfoBuffer outputSystemInfoBuffer;

    public SystemInfoParser(DeviceSystemInfoService deviceSystemInfoService,
                            BlockingQueue<ParserDataTask> dataBuffer,
                            CommDataProcessor CommDataProcessor,
                            OutputSystemInfoBuffer outputSystemInfoBuffer){
        this.deviceSystemInfoService = deviceSystemInfoService;
        this.dataBuffer = dataBuffer;
        this.CommDataProcessor = CommDataProcessor;

        this.outputSystemInfoBuffer = outputSystemInfoBuffer;
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
                            infoContent = "Device unknown error";
                            break;
                        case 0x01:
                            infoContent = "Device start";
                            break;
                        case 0x02:
                            infoContent = "Low power equipment";
                            break;
                        case 0x03:
                            infoContent = "Device not connected to sensor:" + deviceID;
                            break;
                        case 0x04:
                            infoContent = "Device could not locate";
                            break;
                        case 0xEE:
                            infoContent = "Device text message";
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
