package com.circletech.smartconnect.parser;

import com.circletech.smartconnect.ParserDataTask;
import com.circletech.smartconnect.CommDataProcessor;
import com.circletech.smartconnect.data.DeviceTransducerDataData;
import com.circletech.smartconnect.data.OutputTransducerBuffer;
import com.circletech.smartconnect.data.TransducerTimeStampData;
import com.circletech.smartconnect.model.DeviceTransducerData;
import com.circletech.smartconnect.service.DeviceTransducerDataService;
import com.circletech.smartconnect.util.ConstantUtil;
import com.circletech.smartconnect.util.LoggerUtil;
import com.circletech.smartconnect.util.SerialUtil;

import java.sql.Timestamp;
import java.util.concurrent.BlockingQueue;

/**
 * Created by xieyingfei on 2016/12/15.
 */
public class TransducerDataParser implements Runnable{
    private byte[] mData;
    private DeviceTransducerDataService deviceTransducerDataService;
    private DeviceTransducerDataData deviceTransducerDataData;

    private CommDataProcessor CommDataProcessor;

    private BlockingQueue<ParserDataTask> dataBuffer;

    private OutputTransducerBuffer outputTransducerBuffer;


    public TransducerDataParser(DeviceTransducerDataService deviceTransducerDataService,
                                DeviceTransducerDataData deviceTransducerDataData,
                                CommDataProcessor CommDataProcessor,
                                BlockingQueue<ParserDataTask> dataBuffer,
                                OutputTransducerBuffer outputTransducerBuffer){

        this.deviceTransducerDataService = deviceTransducerDataService;
        this.deviceTransducerDataData = deviceTransducerDataData;
        this.CommDataProcessor = CommDataProcessor;

        this.dataBuffer = dataBuffer;

        this.outputTransducerBuffer = outputTransducerBuffer;
    }

    public void run(){

        while (true) {

            try{
                    ParserDataTask task = dataBuffer.take();

                    mData = task.getData();
                    //(id高位 + id低位 + 传感器id + 传感器类型, messagelength 从设备id高位开始起算)
                    int messageLength = SerialUtil.getUnsignedByte(mData[1]);

                    int deviceID = SerialUtil.getUnsignedByte(mData[2])*256+ SerialUtil.getUnsignedByte(mData[3]);
                    //传感器ID
                    int sensorID = SerialUtil.getUnsignedByte(mData[5]);
                    //传感器类型
                    int sensorType = SerialUtil.getUnsignedByte(mData[6]);

                    //传感器数据内容
                    int length = messageLength - 5;
                    byte[] sensorDataContent = new byte[length];
                    for (int i = 0; i < length; i++) {
                        sensorDataContent[i] = mData[5+i+2];
                    }

                    Double sensorValue = 0.0;

                    //Hardcode
                    if((sensorType!=1&&sensorType!=2&&sensorType!=3)||messageLength!=7)
                    {
                        return;
                    }

                    if(sensorType==1) //光线：Highbit X 256 + LowBit
                        sensorValue = (double)SerialUtil.getUnsignedByte(sensorDataContent[0])*256 + SerialUtil.getUnsignedByte(sensorDataContent[1]);
                    if(sensorType==2) //温度：(Highbit X 256 + LowBit)/100
                        sensorValue = ((SerialUtil.getUnsignedByte(sensorDataContent[0])*256 + SerialUtil.getUnsignedByte(sensorDataContent[1]))/100.0);
                    if(sensorType==3) //湿度：(Highbit X 256 + LowBit)/10
                        sensorValue = ((SerialUtil.getUnsignedByte(sensorDataContent[0])*256 + SerialUtil.getUnsignedByte(sensorDataContent[1]))/10.0);



                    DeviceTransducerData tempTrans = new DeviceTransducerData((long)deviceID, (long)sensorID,
                            (long)sensorType, sensorValue, new Timestamp(task.getTimestamp()), new Timestamp(task.getTimestamp()));

                    outputTransducerBuffer.add(new TransducerTimeStampData(tempTrans, task.getTimestamp()));

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
