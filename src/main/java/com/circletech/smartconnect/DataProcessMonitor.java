package com.circletech.smartconnect;

import com.circletech.smartconnect.config.CustomConfig;
import com.circletech.smartconnect.util.ConstantUtil;
import com.circletech.smartconnect.util.LoggerUtil;
import com.circletech.smartconnect.util.MonitorWatcher;
import com.circletech.smartconnect.util.ThreadPoolUtil;

import java.util.concurrent.ExecutorService;

/**
 * Created by xieyingfei on 2016/12/7.
 */
public class DataProcessMonitor implements Runnable{

    //static inner class Singleton pattern
    private DataProcessMonitor() {
    }
    private static class SingletonInstance {
        private static final DataProcessMonitor INSTANCE = new DataProcessMonitor();
    }
    public static DataProcessMonitor getInstance() {
        return DataProcessMonitor.SingletonInstance.INSTANCE;
    }

    private CommDataProcessor currentSensorReceiver;

    public CommDataProcessor getCurrentSensorReceiver() {
        return currentSensorReceiver;
    }

    public void setCurrentSensorReceiver(CommDataProcessor currentSensorReceiver) {
        this.currentSensorReceiver = currentSensorReceiver;
    }

    private CustomConfig customConfig;

    public CustomConfig getCustomConfig() {
        return customConfig;
    }

    public void setCustomConfig(CustomConfig customConfig) {
        this.customConfig = customConfig;
    }

    private MonitorWatcher receiverWatchdog;
    private Boolean receiverifRunning = true;

    /*
    public DataProcessMonitor(CommDataProcessor receiver) {

        //actual data process thread
        currentSensorReceiver = receiver;

        this.deviceDistanceService = receiver.getDeviceDistanceService();
        this.devicePositionService = receiver.getDevicePositionService();
        this.deviceTransducerDataService = receiver.getDeviceTransducerDataService();
        this.deviceSystemInfoService = receiver.getDeviceSystemInfoService();
        this.basePositionService = receiver.getBasePositionService();

        this.customConfig = receiver.getCustomConfig();

        //watch receive process tool
        receiverWatchdog = new MonitorWatcher(customConfig.getMonitorperiod());
    }
    */

    public void openReceiver(){
        ExecutorService cachedThreadPool = ThreadPoolUtil.getInstance();
        cachedThreadPool.execute(currentSensorReceiver);

        LoggerUtil.getInstance().info("openReceiver");
    }

    public void closeReceiver(){
        currentSensorReceiver.close();

        LoggerUtil.getInstance().info("closeReceiver");
    }

    @Override
    public void run() {
        //watch receive process tool
        receiverWatchdog = new MonitorWatcher(customConfig.getMonitorperiod());
        receiverWatchdog.feed();

        ExecutorService cachedThreadPool = ThreadPoolUtil.getInstance();

        int reinvokeprocess = customConfig.getReinvokeprocess();
        while (receiverifRunning) {
            //If the thread haven't update the alive flag for 5 secs
            if (reinvokeprocess == 1
                    && !receiverWatchdog.checkAlive()
                    && currentSensorReceiver != null
                    && !currentSensorReceiver.isActive()) {
                //stop current thread. Drop current pointer to the garbage collector. Init another thread.
                currentSensorReceiver.close();

                currentSensorReceiver = CommDataProcessor.getInstance();
                cachedThreadPool.execute(currentSensorReceiver);

                receiverWatchdog.feed();

                LoggerUtil.getInstance().info("Receiver Restarted");
            }

            try{
                Thread.sleep(ConstantUtil.THREAD_SLEEP_SPAN);
            }catch (InterruptedException e){
                e.printStackTrace();

            }
        }
    }

    public void shutdown() {
        this.receiverifRunning = false;
    }
}
