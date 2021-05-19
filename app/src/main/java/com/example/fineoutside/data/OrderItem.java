package com.example.fineoutside.data;

import android.os.Parcel;
import android.os.Parcelable;

public class OrderItem implements Parcelable {

    public static final Creator<OrderItem> CREATOR = new Creator<OrderItem>() {
        @Override
        public OrderItem createFromParcel(Parcel in) {
            return new OrderItem(in);
        }

        @Override
        public OrderItem[] newArray(int size) {
            return new OrderItem[size];
        }
    };
    private String name, description;
    private int quantity, resource;
    private double price;

    public OrderItem() {
    }

    public OrderItem(String name, String description, int quantity, int resource, double price) {
        this.name = name;
        this.description = description;
        this.quantity = quantity;
        this.resource = resource;
        this.price = price;
    }

    protected OrderItem(Parcel in) {
        name = in.readString();
        description = in.readString();
        quantity = in.readInt();
        resource = in.readInt();
        price = in.readDouble();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getResource() {
        return resource;
    }

    public void setResource(int resource) {
        this.resource = resource;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(description);
        dest.writeInt(quantity);
        dest.writeInt(resource);
        dest.writeDouble(price);
    }
}
