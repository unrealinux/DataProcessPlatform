package com.circletech.smartconnect.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.sql.Timestamp;

/**
 * Created by xieyingfei on 2016/12/12.
 */
@Entity
public class DeviceSystemInfo {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;
    //private String id;

    private Long deviceId;
    private Long infoCode;
    private String infoContent;

    private Timestamp inTime;
    private Timestamp modifyTime;

    public Timestamp getInTime() {
        return inTime;
    }

    public void setInTime(Timestamp inTime) {
        this.inTime = inTime;
    }

    public Timestamp getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Timestamp modifyTime) {
        this.modifyTime = modifyTime;
    }

    protected DeviceSystemInfo(){}

    public DeviceSystemInfo(Long deviceId, Long infoCode, String infoContent, Timestamp inTime, Timestamp modifyTime) {
        this.deviceId = deviceId;
        this.infoCode = infoCode;
        this.infoContent = infoContent;

        this.inTime = inTime;
        this.modifyTime = modifyTime;

        //this.id = UUID.randomUUID().toString();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public Long getInfoCode() {
        return infoCode;
    }

    public void setInfoCode(Long infoCode) {
        this.infoCode = infoCode;
    }

    public String getInfoContent() {
        return infoContent;
    }

    public void setInfoContent(String infoContent) {
        this.infoContent = infoContent;
    }

    @Override
    public String toString() {
        return "DeviceSystemInfo{" +
                "id=" + id +
                ", deviceId=" + deviceId +
                ", infoCode=" + infoCode +
                ", infoContent='" + infoContent + '\'' +
                '}';
    }
}
