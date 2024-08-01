package com.example.adminnhatro360.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;

public class Room implements Parcelable {
    private String id;
    private String title;
    private String address;
    private String price;
    private String area;
    private Timestamp timePosted;
    private List<String> images; // List of image URLs
    private List<Boolean> utilities;
    private String phone;
    private String host;
    private String detail;
    private Integer roomType;
    private Integer postType;
    private Integer avatar;
    private Integer status;

    public Room() {
    }

    public Room(String id, String title, String address, String price, String area, Timestamp timePosted, List<String> images, List<Boolean> utilities, String phone, String host, String detail, Integer roomType, Integer postType, Integer avatar, Integer status) {
        this.id = id;
        this.title = title;
        this.address = address;
        this.price = price;
        this.area = area;
        this.timePosted = timePosted;
        this.images = images;
        this.utilities = utilities;
        this.phone = phone;
        this.host = host;
        this.detail = detail;
        this.roomType = roomType;
        this.postType = postType;
        this.avatar = avatar;
        this.status = status;
    }

    public Room(List<String> images, List<Boolean> utitlities, Integer roomType, Integer postType, Integer avatar) {
        this.images = images;
        this.utilities = utitlities;
        this.roomType = roomType;
        this.postType = postType;
        this.avatar = avatar;
    }

    protected Room(Parcel in) {
        id = in.readString();
        title = in.readString();
        address = in.readString();
        price = in.readString();
        area = in.readString();
        timePosted = in.readParcelable(Timestamp.class.getClassLoader());
        images = in.createStringArrayList();
        utilities = new ArrayList<>();
        in.readList(utilities, Boolean.class.getClassLoader());
        phone = in.readString();
        host = in.readString();
        detail = in.readString();
        if (in.readByte() == 0) {
            roomType = null;
        } else {
            roomType = in.readInt();
        }
        if (in.readByte() == 0) {
            postType = null;
        } else {
            postType = in.readInt();
        }
        if (in.readByte() == 0) {
            avatar = null;
        } else {
            avatar = in.readInt();
        }
    }

    public static final Creator<Room> CREATOR = new Creator<Room>() {
        @Override
        public Room createFromParcel(Parcel in) {
            return new Room(in);
        }

        @Override
        public Room[] newArray(int size) {
            return new Room[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(address);
        dest.writeString(price);
        dest.writeString(area);
        dest.writeParcelable(timePosted, flags);
        dest.writeStringList(images);
        dest.writeList(utilities);
        dest.writeString(phone);
        dest.writeString(host);
        dest.writeString(detail);
        if (roomType == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(roomType);
        }
        if (postType == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(postType);
        }
        if (avatar == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(avatar);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public Timestamp getTimePosted() {
        return timePosted;
    }

    public void setTimePosted(Timestamp timePosted) {
        this.timePosted = timePosted;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public List<Boolean> getUtilities() {
        return utilities;
    }

    public void setUtilities(List<Boolean> utilities) {
        this.utilities = utilities;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public Integer getRoomType() {
        return roomType;
    }

    public void setRoomType(Integer roomType) {
        this.roomType = roomType;
    }

    public Integer getPostType() {
        return postType;
    }

    public void setPostType(Integer postType) {
        this.postType = postType;
    }

    public Integer getAvatar() {
        return avatar;
    }

    public void setAvatar(Integer avatar) {
        this.avatar = avatar;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
