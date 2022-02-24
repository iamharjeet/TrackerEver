package com.harjeet.trackerever.Structures;

public class RequestStructure {
    String mobile;
    String status;

    RequestStructure(){

    }
    public RequestStructure(String mobile, String status) {
        this.mobile = mobile;
        this.status = status;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
