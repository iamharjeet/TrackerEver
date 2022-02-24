package com.harjeet.trackerever.Structures;

public class ProfileDataStructure {
    String image;
    String name;
    String mobile;
    String gender;
    String token;

    public ProfileDataStructure(){
    }
    public ProfileDataStructure(String image, String name, String mobile, String gender,String token) {
        this.image = image;
        this.name = name;
        this.mobile = mobile;
        this.gender = gender;
        this.token=token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
