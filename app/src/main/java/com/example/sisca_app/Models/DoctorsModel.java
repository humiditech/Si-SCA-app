package com.example.sisca_app.Models;

public class DoctorsModel {

    String addr, age, emailAddr,fName,imageURL,nName,role,uid,status;

    public DoctorsModel() {
    }

    public DoctorsModel(String addr, String age, String emailAddr, String fName, String imageURL, String nName, String role, String uid, String status) {
        this.addr = addr;
        this.age = age;
        this.emailAddr = emailAddr;
        this.fName = fName;
        this.imageURL = imageURL;
        this.nName = nName;
        this.role = role;
        this.uid = uid;
        this.status = status;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getEmailAddr() {
        return emailAddr;
    }

    public void setEmailAddr(String emailAddr) {
        this.emailAddr = emailAddr;
    }

    public String getfName() {
        return fName;
    }

    public void setfName(String fName) {
        this.fName = fName;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getnName() {
        return nName;
    }

    public void setnName(String nName) {
        this.nName = nName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
