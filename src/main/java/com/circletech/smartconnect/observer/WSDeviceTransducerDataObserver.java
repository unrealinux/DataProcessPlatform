package com.circletech.smartconnect.observer;

import com.circletech.smartconnect.CommDataProcessor;
import com.circletech.smartconnect.data.DeviceTransducerDataData;
import com.circletech.smartconnect.model.DeviceTransducerData;
import com.circletech.smartconnect.network.TransportTransducerData;
import com.circletech.smartconnect.util.ConstantUtil;
import com.circletech.smartconnect.util.LoggerUtil;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by xieyingfei on 2016/12/15.
 Observe changes in sensor data Web version
 */
public class WSDeviceTransducerDataObserver implements Observer {

    private SimpMessagingTemplate simpMessagingTemplate;

    public WSDeviceTransducerDataObserver(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @Override
    public void update(Observable o, Object arg) {
        CommDataProcessor CommDataProcessor = (CommDataProcessor)o;
        if (((Integer) arg).intValue() == ConstantUtil.TRANSDUCERDATA_INFO){
            DeviceTransducerDataData deviceTransducerDataData = CommDataProcessor.getTransducerDataData();
            DeviceTransducerData deviceTransducerData = deviceTransducerDataData.get();

            TransportTransducerData transportTransducerData =TransportTransducerData.getInstance();
            transportTransducerData.setDeviceId(deviceTransducerData.getDeviceId());
            transportTransducerData.setCode(deviceTransducerData.getCode());
            transportTransducerData.setTransType(deviceTransducerData.getTransType());
            transportTransducerData.setTransValue(deviceTransducerData.getTransValue());

            simpMessagingTemplate.setDefaultDestination("/topic/senordata");
            try {
                simpMessagingTemplate.convertAndSend(transportTransducerData);
            }catch (MessagingException e){
                e.printStackTrace();
                LoggerUtil.getInstance().info(e.getMessage());
            }
        }else{
            LoggerUtil.getInstance().info("web client data error");
        }

    }
}
