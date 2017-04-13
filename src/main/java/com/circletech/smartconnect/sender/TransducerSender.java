package com.circletech.smartconnect.sender;

import com.circletech.smartconnect.CommDataProcessor;
import com.circletech.smartconnect.data.DeviceTransducerDataData;
import com.circletech.smartconnect.data.OutputTransducerBuffer;
import com.circletech.smartconnect.service.DeviceTransducerDataService;
import com.circletech.smartconnect.util.ConstantUtil;

/**
 * Created by xieyingfei on 2017/1/13.
 */
public class TransducerSender implements Runnable {

    private DeviceTransducerDataService deviceTransducerDataService;
    private CommDataProcessor CommDataProcessor;
    private OutputTransducerBuffer outputTransducerBuffer;
    private DeviceTransducerDataData deviceTransducerData;

    public DeviceTransducerDataService getDeviceTransducerDataService() {
        return deviceTransducerDataService;
    }

    public void setDeviceTransducerDataService(DeviceTransducerDataService deviceTransducerDataService) {
        this.deviceTransducerDataService = deviceTransducerDataService;
    }

    public CommDataProcessor getCommDataProcessor() {
        return CommDataProcessor;
    }

    public void setCommDataProcessor(CommDataProcessor CommDataProcessor) {
        this.CommDataProcessor = CommDataProcessor;
    }

    public OutputTransducerBuffer getOutputTransducerBuffer() {
        return outputTransducerBuffer;
    }

    public void setOutputTransducerBuffer(OutputTransducerBuffer outputTransducerBuffer) {
        this.outputTransducerBuffer = outputTransducerBuffer;
    }

    public DeviceTransducerDataData getDeviceTransducerData() {
        return deviceTransducerData;
    }

    public void setDeviceTransducerData(DeviceTransducerDataData deviceTransducerData) {
        this.deviceTransducerData = deviceTransducerData;
    }

    public TransducerSender(DeviceTransducerDataService deviceTransducerDataService, CommDataProcessor CommDataProcessor, OutputTransducerBuffer outputTransducerBuffer, DeviceTransducerDataData deviceTransducerData) {
        this.deviceTransducerDataService = deviceTransducerDataService;
        this.CommDataProcessor = CommDataProcessor;
        this.outputTransducerBuffer = outputTransducerBuffer;
        this.deviceTransducerData = deviceTransducerData;
    }

    @Override
    public void run() {

        while (true){
            outputTransducerBuffer.output(deviceTransducerDataService, deviceTransducerData, CommDataProcessor);
            try{
                Thread.sleep(ConstantUtil.THREAD_SLEEP_SPAN);
            }catch (InterruptedException e){
                e.printStackTrace();

            }
        }
    }

}
