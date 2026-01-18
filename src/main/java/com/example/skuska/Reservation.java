package com.example.skuska;

import javafx.beans.property.*;
import java.time.LocalDate;

public class Reservation {
    private SimpleIntegerProperty id = new SimpleIntegerProperty();
    private SimpleIntegerProperty roomId = new SimpleIntegerProperty();
    private SimpleIntegerProperty responsibleGuestId = new SimpleIntegerProperty();
    private SimpleIntegerProperty guestCount = new SimpleIntegerProperty();
    private ObjectProperty<LocalDate> dateFrom = new SimpleObjectProperty<>();
    private ObjectProperty<LocalDate> dateTo = new SimpleObjectProperty<>();
    private SimpleStringProperty status = new SimpleStringProperty();
    private SimpleStringProperty roomNumber = new SimpleStringProperty();
    private SimpleStringProperty guestName = new SimpleStringProperty();

    public Reservation() {}

    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    public int getRoomId() { return roomId.get(); }
    public void setRoomId(int value) { roomId.set(value); }
    public IntegerProperty roomIdProperty() { return roomId; }

    public int getResponsibleGuestId() { return responsibleGuestId.get(); }
    public void setResponsibleGuestId(int value) { responsibleGuestId.set(value); }
    public IntegerProperty responsibleGuestIdProperty() { return responsibleGuestId; }

    public int getGuestCount() { return guestCount.get(); }
    public void setGuestCount(int value) { guestCount.set(value); }
    public IntegerProperty guestCountProperty() { return guestCount; }

    public LocalDate getDateFrom() { return dateFrom.get(); }
    public void setDateFrom(LocalDate value) { dateFrom.set(value); }
    public ObjectProperty<LocalDate> dateFromProperty() { return dateFrom; }

    public LocalDate getDateTo() { return dateTo.get(); }
    public void setDateTo(LocalDate value) { dateTo.set(value); }
    public ObjectProperty<LocalDate> dateToProperty() { return dateTo; }

    public String getStatus() { return status.get(); }
    public void setStatus(String value) { status.set(value); }
    public StringProperty statusProperty() { return status; }

    public String getRoomNumber() { return roomNumber.get(); }
    public void setRoomNumber(String value) { roomNumber.set(value); }
    public StringProperty roomNumberProperty() { return roomNumber; }

    public String getGuestName() { return guestName.get(); }
    public void setGuestName(String value) { guestName.set(value); }
    public StringProperty guestNameProperty() { return guestName; }
}
