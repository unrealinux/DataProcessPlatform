package com.circletech.smartconnect.network;

import com.circletech.smartconnect.CommDataProcessor;
import com.circletech.smartconnect.observer.*;
import com.circletech.smartconnect.util.LoggerUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Controller;

import java.util.concurrent.CountDownLatch;

/**
 * Created by xieyingfei on 2016/12/6.
 */
@Controller
public class AmqpMessageController {

    private String amqpExchangeName;

    private CountDownLatch latch = new CountDownLatch(1);

    private RabbitTemplate rabbitTemplate;

    private MobileDeviceTransducerDataObserver androidSensorObserver;
    private MobileDeviceTransducerDataObserver iosSensorObserver;

    public AmqpMessageController(RabbitTemplate rabbitTemplate){

        this.rabbitTemplate = rabbitTemplate;

    }

    public void constructor(String amqpExchangeName){
        this.amqpExchangeName = amqpExchangeName;

        androidSensorObserver = new MobileDeviceTransducerDataObserver(null, rabbitTemplate, this.amqpExchangeName, "android-gps-sensor-data");
        iosSensorObserver = new MobileDeviceTransducerDataObserver(null, rabbitTemplate, this.amqpExchangeName, "ios-gps-sensor-data");
    }

    public void receiveMessage(byte[] body){

        try{
            String message = new String(body, "UTF-8");
            LoggerUtil.getInstance().info("Received <" + message + ">");
            CommDataProcessor commDataProcessor = CommDataProcessor.getInstance();

            if(message.equals("android-open-sensor")){
                commDataProcessor.addObserver(androidSensorObserver);
            }else if(message.equals("android-close-sensor")){
                commDataProcessor.deleteObserver(androidSensorObserver);
            }else if(message.equals("ios-open-sensor")){
                commDataProcessor.addObserver(iosSensorObserver);
            }else if(message.equals("ios-close-sensor")){
                commDataProcessor.deleteObserver(iosSensorObserver);
            }else{
                //for other interface
            }
        }
        catch(Exception e){
            e.printStackTrace();
            LoggerUtil.getInstance().info(e.getMessage());
        }

        latch.countDown();
    }

    public CountDownLatch getLatch(){
        return latch;
    }
}
