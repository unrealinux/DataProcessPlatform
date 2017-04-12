package com.circletech.smartconnect;

import com.circletech.smartconnect.config.CustomConfig;
import com.circletech.smartconnect.util.ThreadPoolUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;

/**
 * Created by xieyingfei on 2016/12/6.
 */
@Component
public class Runner implements CommandLineRunner {

    @Autowired
    private CustomConfig customConfig;

    public Runner(){
    }

    @Override
    public void run(String... args) throws Exception{

        ExecutorService cachedThreadPool = ThreadPoolUtil.getInstance();
        DataProcessMonitor.getInstance().setCustomConfig(customConfig);
        cachedThreadPool.execute(DataProcessMonitor.getInstance());
    }

}
