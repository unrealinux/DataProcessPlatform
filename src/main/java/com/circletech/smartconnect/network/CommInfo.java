package com.circletech.smartconnect.network;

/**
 * Created by xieyingfei on 2016/12/14. //Comm information entity
 */
public class CommInfo {

    private String commName;
    private Long   commType;

    private int baudrate;
    private String dataw;
    private String jiaoy;
    private String liuk;
    private String stopw;

    public CommInfo() {
    }

    public CommInfo(String commName, Long commType) {
        this.commName = commName;
        this.commType = commType;
    }

    public CommInfo(String commName, Long commType, int baudrate, String dataw, String jiaoy, String liuk, String stopw) {
        this.commName = commName;
        this.commType = commType;
        this.baudrate = baudrate;
        this.dataw = dataw;
        this.jiaoy = jiaoy;
        this.liuk = liuk;
        this.stopw = stopw;
    }


    public int getBaudrate() {
        return baudrate;
    }

    public void setBaudrate(int baudrate) {
        this.baudrate = baudrate;
    }

    public String getDataw() {
        return dataw;
    }

    public void setDataw(String dataw) {
        this.dataw = dataw;
    }

    public String getJiaoy() {
        return jiaoy;
    }

    public void setJiaoy(String jiaoy) {
        this.jiaoy = jiaoy;
    }

    public String getLiuk() {
        return liuk;
    }

    public void setLiuk(String liuk) {
        this.liuk = liuk;
    }

    public String getStopw() {
        return stopw;
    }

    public void setStopw(String stopw) {
        this.stopw = stopw;
    }

    public String getCommName() {
        return commName;
    }

    public void setCommName(String commName) {
        this.commName = commName;
    }

    public Long getCommType() {
        return commType;
    }

    public void setCommType(Long commType) {
        this.commType = commType;
    }
}
