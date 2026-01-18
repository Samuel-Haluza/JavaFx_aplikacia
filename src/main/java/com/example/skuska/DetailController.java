package com.example.skuska;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.*;

public class DetailController {

    @FXML private Label lblResponsible;
    @FXML private Label lblGuestCount;
    @FXML private Label lblRoomPrice;
    @FXML private Label lblServicesPrice;
    @FXML private Label lblTotalPrice;

    @FXML private TableView<Service> tableServices;
    @FXML private TableColumn<Service, String> colServiceName;
    @FXML private TableColumn<Service, Double> colServicePrice;
    @FXML private TableColumn<Service, Integer> colServiceQuantity;

    private ObservableList<Service> services = FXCollections.observableArrayList();

    private int reservationId;

    private static final String URL = "jdbc:mysql://localhost:3306/hotel_reservations";
    private static final String USER = "root";
    private static final String PASS = "";

    public void setReservationId(int id) {
        this.reservationId = id;
        loadDetail();
    }

    @FXML
    public void initialize() {
        colServiceName.setCellValueFactory(data -> data.getValue().nameProperty());
        colServicePrice.setCellValueFactory(data -> data.getValue().priceProperty().asObject());
        colServiceQuantity.setCellValueFactory(data -> data.getValue().quantityProperty().asObject());

        tableServices.setItems(services);
    }

    private void loadDetail() {
        services.clear();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {

            String sql = "SELECT r.guest_count, CONCAT(g.first_name,' ',g.last_name) as guest_name, ro.price_per_night " +
                    "FROM reservation r " +
                    "JOIN guest g ON g.id = r.responsible_guest_id " +
                    "JOIN room ro ON ro.id = r.room_id " +
                    "WHERE r.id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, reservationId);
            ResultSet rs = ps.executeQuery();

            double roomPrice = 0;
            int guestCount = 0;
            int nights = 0;

            if (rs.next()) {
                lblResponsible.setText(rs.getString("guest_name"));
                guestCount = rs.getInt("guest_count");
                lblGuestCount.setText(String.valueOf(guestCount));
                roomPrice = rs.getDouble("price_per_night");
            }

            sql = "SELECT DATEDIFF(date_to, date_from) as nights FROM reservation WHERE id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, reservationId);
            rs = ps.executeQuery();
            if (rs.next()) {
                nights = rs.getInt("nights");
                double totalRoomPrice = roomPrice * nights;
                lblRoomPrice.setText(String.format("%.2f € (%.2f € x %d nocí)", totalRoomPrice, roomPrice, nights));
            }

            sql = "SELECT s.id, s.name, s.price, rs.quantity FROM service s " +
                    "JOIN reservation_service rs ON s.id = rs.service_id " +
                    "WHERE rs.reservation_id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, reservationId);
            rs = ps.executeQuery();

            double servicesTotal = 0;

            while (rs.next()) {
                Service s = new Service();
                s.setId(rs.getInt("id"));
                s.setName(rs.getString("name"));
                s.setPrice(rs.getDouble("price"));
                s.setQuantity(rs.getInt("quantity"));
                services.add(s);
                servicesTotal += s.getPrice() * s.getQuantity();
            }

            double roomTotal = roomPrice * nights;
            lblServicesPrice.setText(String.format("%.2f €", servicesTotal));
            lblTotalPrice.setText(String.format("%.2f €", roomTotal + servicesTotal));

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Chyba načítania detailu: " + e.getMessage());
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) tableServices.getScene().getWindow();
        stage.close();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(msg);
        alert.show();
    }


}
