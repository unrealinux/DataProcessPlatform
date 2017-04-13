package com.circletech.smartconnect.config;

/**
 * Created by xieyingfei on 2017/2/15. //JSON datasource configuration
 */
public class Database {

    /**
     * The Name.
     */
    private String name;
    /**
     * The Url.
     */
    private String url;
    /**
     * The Username.
     */
    private String username;
    /**
     * The Password.
     */
    private String password;
    /**
     * The Drive class name.
     */
    private String driveClassName;

    public Database() {
    }

    public Database(String name, String url, String username, String password, String driveClassName) {
        this.name = name;
        this.url = url;
        this.username = username;
        this.password = password;
        this.driveClassName = driveClassName;
    }

    @Override
    public String toString() {
        return "Database{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", driveClassName='" + driveClassName + '\'' +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDriveClassName() {
        return driveClassName;
    }

    public void setDriveClassName(String driveClassName) {
        this.driveClassName = driveClassName;
    }
}
