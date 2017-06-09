package com.circletech.smartconnect.network;

/**
 * Created by Administrator on 2017/5/23.
 */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaSender {
    @Autowired
    private KafkaTemplate kafkaTemplate;
    private Gson gson = new GsonBuilder().create();

    public void sendSensorMessage(TransportTransducerData transportTransducerData){
        kafkaTemplate.send("transducer", gson.toJson(transportTransducerData));
    }
}
