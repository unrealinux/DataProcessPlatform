package com.circletech.smartconnect.observer;

import com.circletech.smartconnect.CommDataProcessor;
import com.circletech.smartconnect.data.DeviceTransducerDataData;
import com.circletech.smartconnect.model.DeviceTransducerData;
import com.circletech.smartconnect.network.KafkaSender;
import com.circletech.smartconnect.network.TransportTransducerData;
import com.circletech.smartconnect.util.ConstantUtil;
import com.circletech.smartconnect.util.LoggerUtil;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Observable;
import java.util.Observer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by xieyingfei on 2016/12/22.
 */
public class MobileDeviceTransducerDataObserver implements Observer {

    private RabbitTemplate rabbitTemplate;
    private String exchangeName;
    private String routingKey;
    private KafkaSender kafkaSender;

    public MobileDeviceTransducerDataObserver(KafkaSender kafkaSender, RabbitTemplate rabbitTemplate,
                                              String exchangeName,
                                              String routingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
        this.kafkaSender = kafkaSender;
    }

    @Override
    public void update(Observable o, Object arg) {
        CommDataProcessor CommDataProcessor = (CommDataProcessor) o;
        if (((Integer) arg).intValue() == ConstantUtil.TRANSDUCERDATA_INFO) {
            DeviceTransducerDataData deviceTransducerDataData = CommDataProcessor.getTransducerDataData();
            DeviceTransducerData deviceTransducerData = deviceTransducerDataData.get();

            TransportTransducerData transportTransducerData = TransportTransducerData.getInstance();
            transportTransducerData.setDeviceId(deviceTransducerData.getDeviceId());
            transportTransducerData.setCode(deviceTransducerData.getCode());
            transportTransducerData.setTransType(deviceTransducerData.getTransType());
            transportTransducerData.setTransValue(deviceTransducerData.getTransValue());

            try {
                if(rabbitTemplate != null){
                    rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
                    rabbitTemplate.convertAndSend(exchangeName, routingKey, transportTransducerData);
                }

                if(kafkaSender != null){
                    kafkaSender.sendSensorMessage(transportTransducerData);
                }
            }catch (AmqpException e){
                e.printStackTrace();
                LoggerUtil.getInstance().info(e.getMessage());
            }

        } else {
            LoggerUtil.getInstance().info("mobile client data error");
        }
    }
}
