package com.circletech.smartconnect.network;

import com.circletech.smartconnect.CommDataProcessor;
import com.circletech.smartconnect.DataProcessMonitor;
import com.circletech.smartconnect.config.CustomConfig;
import com.circletech.smartconnect.data.DeviceSystemInfoData;
import com.circletech.smartconnect.data.DeviceTransducerDataData;
import com.circletech.smartconnect.observer.WSDeviceTransducerDataObserver;
import com.circletech.smartconnect.service.*;
import com.circletech.smartconnect.util.LoggerUtil;
import com.circletech.smartconnect.util.ThreadPoolUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import com.circletech.smartconnect.network.CommInfo;
import com.circletech.smartconnect.CommDataProcessor;

import java.util.List;
import java.util.concurrent.ExecutorService;

@Controller
public class WebSocketMessageController {

    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;
    @Autowired
    private DeviceTransducerDataService deviceTransducerDataService;
    @Autowired
    private DeviceSystemInfoService deviceSystemInfoService;

    @Autowired
    private CustomConfig customConfig;

    @MessageMapping("/init")
    public void initComm() throws Exception {
        simpMessagingTemplate.setDefaultDestination("/topic/comm");

        List<CommInfo> commlist;
        CommDataProcessor commDataProcessor = CommDataProcessor.getInstance();
        commlist = commDataProcessor.findAvailablePort();

        try {
            simpMessagingTemplate.convertAndSend(commlist);
        }catch (MessagingException e){
            e.printStackTrace();
            LoggerUtil.getInstance().info(e.getMessage());
        }
    }

    @MessageMapping("/open")
    public void transportData(CommInfo comminfo) throws Exception{

        //start data process thread
        ExecutorService cachedThreadPool = ThreadPoolUtil.getInstance();
        CommDataProcessor commDataProcessor = CommDataProcessor.getInstance();

        if(!commDataProcessor.isActive()){
            commDataProcessor.constructor(deviceTransducerDataService,deviceSystemInfoService,
                    new DeviceTransducerDataData(),
                    new DeviceSystemInfoData(),
                    customConfig);
            commDataProcessor.setSerialPortName(comminfo.getCommName());
            commDataProcessor.setBaudrate(comminfo.getBaudrate());
            //add web observer
            commDataProcessor.addObserver(new WSDeviceTransducerDataObserver(simpMessagingTemplate));

            DataProcessMonitor.getInstance().setCurrentSensorReceiver(commDataProcessor);
            DataProcessMonitor.getInstance().openReceiver();
        }

    }

    @MessageMapping("/close")
    public void closeComm() throws Exception {
        DataProcessMonitor.getInstance().closeReceiver();
    }

}
