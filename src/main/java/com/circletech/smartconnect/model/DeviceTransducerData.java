package com.circletech.smartconnect.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.sql.Timestamp;

/**
 * Created by xieyingfei on 2016/12/9.
 */
@Entity
public class DeviceTransducerData {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;
    //private String id;

    private Long deviceId;
    private Long code;
    private Long transType;
    private Double transValue;

    private Timestamp inTime;
    private Timestamp modifyTime;

    protected DeviceTransducerData() {
    }

    public DeviceTransducerData(Long deviceId, Long code, Long transType, Double transValue, Timestamp inTime, Timestamp modifyTime) {
        this.deviceId = deviceId;
        this.code = code;
        this.transType = transType;
        this.transValue = transValue;
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

    public Long getCode() {
        return code;
    }

    public void setCode(Long code) {
        this.code = code;
    }

    public Long getTransType() {
        return transType;
    }

    public void setTransType(Long transType) {
        this.transType = transType;
    }

    public Double getTransValue() {
        return transValue;
    }

    public void setTransValue(Double transValue) {
        this.transValue = transValue;
    }

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

    @Override
    public String toString() {
        return "DeviceTransducerData{" +
                "id=" + id +
                ", sbCode='" + deviceId + '\'' +
                ", code='" + code + '\'' +
                ", transType='" + transType + '\'' +
                ", transValue=" + transValue +
                ", inTime=" + inTime +
                ", modifyTime=" + modifyTime +
                '}';
    }
}
