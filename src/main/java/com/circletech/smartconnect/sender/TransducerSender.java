package com.circletech.smartconnect.sender;

import com.circletech.smartconnect.CommDataProcessor;
import com.circletech.smartconnect.data.DeviceTransducerDataData;
import com.circletech.smartconnect.data.OutputTransducerBuffer;
import com.circletech.smartconnect.service.DeviceTransducerDataService;
import com.circletech.smartconnect.util.ConstantUtil;
import javafx.util.Builder;

/**
 * Created by xieyingfei on 2017/1/13.
 */
public class TransducerSender implements Runnable {

    private DeviceTransducerDataService deviceTransducerDataService;
    private CommDataProcessor receiveCommProcessor;
    private OutputTransducerBuffer outputTransducerBuffer;
    private DeviceTransducerDataData deviceTransducerData;

    public DeviceTransducerDataService getDeviceTransducerDataService() {
        return deviceTransducerDataService;
    }

    public void setDeviceTransducerDataService(DeviceTransducerDataService deviceTransducerDataService) {
        this.deviceTransducerDataService = deviceTransducerDataService;
    }

    public CommDataProcessor getReceiveCommProcessor() {
        return receiveCommProcessor;
    }

    public void setReceiveCommProcessor(CommDataProcessor receiveCommProcessor) {
        this.receiveCommProcessor = receiveCommProcessor;
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

    public static class TransducerSenderBuilder implements Builder<TransducerSender> {

        private DeviceTransducerDataService deviceTransducerDataService;
        private CommDataProcessor receiveCommProcessor;
        private OutputTransducerBuffer outputTransducerBuffer;
        private DeviceTransducerDataData deviceTransducerData;

        public TransducerSenderBuilder deviceTransducerDataService(DeviceTransducerDataService deviceTransducerDataService){
            this.deviceTransducerDataService = deviceTransducerDataService;
            return this;
        }

        public TransducerSenderBuilder receiveCommProcessor(CommDataProcessor receiveCommProcessor){
            this.receiveCommProcessor = receiveCommProcessor;
            return this;
        }

        public TransducerSenderBuilder outputTransducerBuffer(OutputTransducerBuffer outputTransducerBuffer){
            this.outputTransducerBuffer = outputTransducerBuffer;
            return this;
        }

        public TransducerSenderBuilder deviceTransducerData(DeviceTransducerDataData deviceTransducerDataData){
            this.deviceTransducerData = deviceTransducerDataData;
            return this;
        }

        public TransducerSender build(){
            return new TransducerSender(this);
        }
    }

    private TransducerSender(TransducerSenderBuilder builder){

        this.deviceTransducerData = builder.deviceTransducerData;
        this.deviceTransducerDataService = builder.deviceTransducerDataService;
        this.outputTransducerBuffer = builder.outputTransducerBuffer;
        this.receiveCommProcessor = builder.receiveCommProcessor;
    }

    @Override
    public void run() {

        while (true){
            outputTransducerBuffer.output(deviceTransducerDataService, deviceTransducerData, receiveCommProcessor);
            try{
                Thread.sleep(ConstantUtil.THREAD_SLEEP_SPAN);
            }catch (InterruptedException e){
                e.printStackTrace();

            }
        }
    }

}
