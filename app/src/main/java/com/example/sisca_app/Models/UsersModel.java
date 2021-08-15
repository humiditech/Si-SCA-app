package com.example.sisca_app.Models;

public class UsersModel {
    String addr, age, dName, emailAddr, fName, nName, role, uid,imageURL,status;

    public UsersModel(String addr, String age, String dName, String emailAddr, String fName, String nName,String role, String uid, String imageURL, String status) {
        this.addr = addr;
        this.age = age;
        this.dName = dName;
        this.emailAddr = emailAddr;
        this.fName = fName;
        this.nName = nName;
        this.role = role;
        this.uid = uid;
        this.imageURL = imageURL;
        this.status = status;
    }

    public UsersModel() {
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

    public String getdName() {
        return dName;
    }

    public void setdName(String dName) {
        this.dName = dName;
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

    public String getnName() {
        return nName;
    }

    public void setnName(String nName) {
        this.nName = nName;
    }

    public String getRole(){return role;}

    public void setRole(String role) {
        this.role = role;
    }

    public String getUid(){
        return uid;
    }

    public void setUid(String uid){
        this.uid = uid;
    }

    public String getImageURL(){return imageURL;}

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

