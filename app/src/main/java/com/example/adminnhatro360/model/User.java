package com.example.adminnhatro360.model;

import java.util.List;

public class User {
    private String id, fullName, email, phone;
    private List<String> listSavedRoom;
    private List<String> listPostedRoom;

    public User() {
    }

    public User(String fullName, String email, String phone, List<String> listSavedRoom, List<String> listPostedRoom) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.listSavedRoom = listSavedRoom;
        this.listPostedRoom = listPostedRoom;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<String> getListSavedRoom() {
        return listSavedRoom;
    }

    public void setListSavedRoom(List<String> listSavedRoom) {
        this.listSavedRoom = listSavedRoom;
    }

    public List<String> getListPostedRoom() {
        return listPostedRoom;
    }

    public void setListPostedRoom(List<String> listPostedRoom) {
        this.listPostedRoom = listPostedRoom;
    }
}
