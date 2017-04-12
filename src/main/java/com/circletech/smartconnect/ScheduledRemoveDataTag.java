package com.circletech.smartconnect;

import com.circletech.smartconnect.util.LoggerUtil;

/**
 * Created by xieyingfei on 2017/2/14.
 */
public class ScheduledRemoveDataTag implements Runnable{

    private  DataTag dataTag;

    public DataTag getDataTag() {
        return dataTag;
    }

    public void setDataTag(DataTag dataTag) {
        this.dataTag = dataTag;
    }

    ScheduledRemoveDataTag(DataTag dataTag){
        this.dataTag = dataTag;
    }

    @Override
    public void run() {

        dataTag.removeTag();
        LoggerUtil.getInstance().info("Delete the not enough 4 distance data");
    }
}
