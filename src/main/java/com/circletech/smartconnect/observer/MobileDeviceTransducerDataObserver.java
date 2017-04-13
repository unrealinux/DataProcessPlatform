package com.circletech.smartconnect.observer;

import com.circletech.smartconnect.CommDataProcessor;
import com.circletech.smartconnect.data.DeviceTransducerDataData;
import com.circletech.smartconnect.model.DeviceTransducerData;
import com.circletech.smartconnect.network.TransportTransducerData;
import com.circletech.smartconnect.util.ConstantUtil;
import com.circletech.smartconnect.util.LoggerUtil;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by xieyingfei on 2016/12/22.
 */
public class MobileDeviceTransducerDataObserver implements Observer {

    private RabbitTemplate rabbitTemplate;
    private String exchangeName;
    private String routingKey;

    public MobileDeviceTransducerDataObserver(RabbitTemplate rabbitTemplate, String exchangeName, String routingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
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

            rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
            try {
                rabbitTemplate.convertAndSend(exchangeName, routingKey, transportTransducerData);
            }catch (AmqpException e){
                e.printStackTrace();
                LoggerUtil.getInstance().info(e.getMessage());
            }
        } else {
            LoggerUtil.getInstance().info("mobile client data error");
        }
    }
}
