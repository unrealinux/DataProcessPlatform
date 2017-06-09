package com.circletech.smartconnect.network;

/**
 * Created by Administrator on 2017/6/8.
 */

import com.circletech.smartconnect.CommDataProcessor;
import com.circletech.smartconnect.observer.MobileDeviceTransducerDataObserver;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaReceiver {

    private Gson gson = new GsonBuilder().create();

    private MobileDeviceTransducerDataObserver androidSensorObserver;
    private MobileDeviceTransducerDataObserver iosSensorObserver;

    @Autowired
    private KafkaSender kafkaSender;

    @KafkaListener(topics = "test")
    public void processMessage(String content) {

        CommDataProcessor receiveCommProcessor = CommDataProcessor.getInstance();

        if(androidSensorObserver == null){
            androidSensorObserver = new MobileDeviceTransducerDataObserver(kafkaSender,null, "", "android-gps-sensor-data");
        }

        if(iosSensorObserver == null){
            iosSensorObserver = new MobileDeviceTransducerDataObserver(kafkaSender,null, "", "ios-gps-sensor-data");
        }

        if(content.equals("android-open-sensor")){
            receiveCommProcessor.addObserver(androidSensorObserver);
        }else if(content.equals("android-close-sensor")){
            receiveCommProcessor.deleteObserver(androidSensorObserver);
        }
    }
}
