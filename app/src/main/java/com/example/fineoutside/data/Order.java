package com.example.fineoutside.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Order implements Parcelable {

    public static final Creator<Order> CREATOR = new Creator<Order>() {
        @Override
        public Order createFromParcel(Parcel in) {
            return new Order(in);
        }

        @Override
        public Order[] newArray(int size) {
            return new Order[size];
        }
    };
    private String order_id, creator_id, phoneNumber, tookOrderUserId;
    private double latitude, longitude;
    private List<OrderItem> orderItemList;
    private long time;
    private int distance;

    public Order() {
    }

    public Order(String order_id, String creator_id, double latitude, double longitude, List<OrderItem> orderItemList, String phoneNumber, String tookOrderUserId) {
        this.order_id = order_id;
        this.creator_id = creator_id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.orderItemList = orderItemList;
        this.time = System.currentTimeMillis();
        this.phoneNumber = phoneNumber;
        this.tookOrderUserId = tookOrderUserId;
    }

    protected Order(Parcel in) {
        order_id = in.readString();
        creator_id = in.readString();
        phoneNumber = in.readString();
        tookOrderUserId = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        orderItemList = in.createTypedArrayList(OrderItem.CREATOR);
        time = in.readLong();
        distance = in.readInt();
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public String getCreator_id() {
        return creator_id;
    }

    public void setCreator_id(String creator_id) {
        this.creator_id = creator_id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public List<OrderItem> getOrderItemList() {
        return orderItemList;
    }

    public void setOrderItemList(List<OrderItem> orderItemList) {
        this.orderItemList = orderItemList;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getTookOrderUserId() {
        return tookOrderUserId;
    }

    public void setTookOrderUserId(String tookOrderUserId) {
        this.tookOrderUserId = tookOrderUserId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(order_id);
        dest.writeString(creator_id);
        dest.writeString(phoneNumber);
        dest.writeString(tookOrderUserId);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeTypedList(orderItemList);
        dest.writeLong(time);
        dest.writeInt(distance);
    }
}
