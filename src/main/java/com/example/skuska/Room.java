package com.example.skuska;

import javafx.beans.property.*;

public class Room {
    private SimpleIntegerProperty id = new SimpleIntegerProperty();
    private SimpleStringProperty roomNumber = new SimpleStringProperty();
    private SimpleStringProperty type = new SimpleStringProperty();
    private SimpleDoubleProperty pricePerNight = new SimpleDoubleProperty();
    private SimpleIntegerProperty capacity = new SimpleIntegerProperty();

    public Room() {}

    public Room(int id, String roomNumber, String type, double pricePerNight, int capacity) {
        this.id.set(id);
        this.roomNumber.set(roomNumber);
        this.type.set(type);
        this.pricePerNight.set(pricePerNight);
        this.capacity.set(capacity);
    }

    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    public String getRoomNumber() { return roomNumber.get(); }
    public void setRoomNumber(String roomNumber) { this.roomNumber.set(roomNumber); }
    public StringProperty roomNumberProperty() { return roomNumber; }

    public String getType() { return type.get(); }
    public void setType(String type) { this.type.set(type); }
    public StringProperty typeProperty() { return type; }

    public double getPricePerNight() { return pricePerNight.get(); }
    public void setPricePerNight(double pricePerNight) { this.pricePerNight.set(pricePerNight); }
    public DoubleProperty pricePerNightProperty() { return pricePerNight; }

    public int getCapacity() { return capacity.get(); }
    public void setCapacity(int capacity) { this.capacity.set(capacity); }
    public IntegerProperty capacityProperty() { return capacity; }

    @Override
    public String toString() {
        return roomNumber.get() + " - " + type.get() + " - €" + pricePerNight.get() + "/night";
    }
}
