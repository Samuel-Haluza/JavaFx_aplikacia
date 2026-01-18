package com.example.skuska;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.PrintWriter;
import java.sql.*;
import java.time.LocalDate;

public class HelloController {

    @FXML private ComboBox<Room> cbRoom;
    @FXML private TextField txtFirstName;
    @FXML private TextField txtLastName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;
    @FXML private Spinner<Integer> spinnerGuestCount;
    @FXML private DatePicker dpFrom;
    @FXML private DatePicker dpTo;
    @FXML private ComboBox<Service> cbService;
    @FXML private ListView<Service> listSelectedServices;

    @FXML private TableView<Reservation> tableReservations;
    @FXML private TableColumn<Reservation, Integer> colId;
    @FXML private TableColumn<Reservation, String> colRoom;
    @FXML private TableColumn<Reservation, LocalDate> colFrom;
    @FXML private TableColumn<Reservation, LocalDate> colTo;
    @FXML private TableColumn<Reservation, String> colGuest;
    @FXML private TableColumn<Reservation, Integer> colGuestsCount;
    @FXML private TableColumn<Reservation, String> colStatus;

    private ObservableList<Reservation> reservations = FXCollections.observableArrayList();
    private ObservableList<Service> selectedServices = FXCollections.observableArrayList();

    private Reservation currentReservation = null;

    private static final String URL = "jdbc:mysql://localhost:3306/hotel_reservations";
    private static final String USER = "root";
    private static final String PASS = "";

    @FXML
    public void initialize() {

        colId.setCellValueFactory(data -> data.getValue().idProperty().asObject());
        colRoom.setCellValueFactory(data -> data.getValue().roomNumberProperty());
        colFrom.setCellValueFactory(data -> data.getValue().dateFromProperty());
        colTo.setCellValueFactory(data -> data.getValue().dateToProperty());
        colGuest.setCellValueFactory(data -> data.getValue().guestNameProperty());
        colGuestsCount.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getGuestCount()));
        colStatus.setCellValueFactory(data -> data.getValue().statusProperty());

        spinnerGuestCount.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1));
        spinnerGuestCount.setEditable(true);

        loadRooms();
        loadServices();
        loadReservations();

        listSelectedServices.setItems(selectedServices);

        cbRoom.setOnAction(e -> adjustSpinnerForSelectedRoom());

        tableReservations.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Reservation sel = tableReservations.getSelectionModel().getSelectedItem();
                if (sel != null) openDetailWindow(sel.getId());
            }
        });

        tableReservations.getSelectionModel().selectedItemProperty().addListener((obs, old, nw) -> {
            if (nw != null) loadReservationToForm(nw);
        });
    }

    @FXML
    private void handleExportCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exportovať rezervácie do CSV");
        fileChooser.setInitialFileName("rezervacie.csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV súbory (*.csv)", "*.csv"));
        File file = fileChooser.showSaveDialog(tableReservations.getScene().getWindow());

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {

                writer.println("ID;Izba;Hosť;Od;Do;Počet hostí;Stav");

                for (Reservation res : reservations) {
                    writer.printf("%d;%s;%s;%s;%s;%d;%s%n",
                            res.getId(),
                            res.getRoomNumber(),
                            res.getGuestName(),
                            res.getDateFrom().toString(),
                            res.getDateTo().toString(),
                            res.getGuestCount(),
                            res.getStatus());
                }
                showInfo("Export do CSV bol úspešný.");
            } catch (Exception e) {
                showError("Chyba pri exporte do CSV: " + e.getMessage());
            }
        }
    }

    private void adjustSpinnerForSelectedRoom() {
        Room room = cbRoom.getValue();
        if (room != null) {
            int capacity = room.getCapacity();
            int currentValue = spinnerGuestCount.getValue();

            int newValue = Math.min(currentValue, capacity);
            if (newValue < 1) newValue = 1;

            SpinnerValueFactory.IntegerSpinnerValueFactory factory =
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(1, capacity, newValue);
            factory.setWrapAround(false);
            spinnerGuestCount.setValueFactory(factory);
            spinnerGuestCount.setEditable(true);
        }
    }

    private void loadRooms() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            ObservableList<Room> rooms = FXCollections.observableArrayList();

            String sql = "SELECT * FROM room ORDER BY room_number";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Room room = new Room();
                room.setId(rs.getInt("id"));
                room.setRoomNumber(rs.getString("room_number"));
                room.setType(rs.getString("type"));
                room.setPricePerNight(rs.getDouble("price_per_night"));
                room.setCapacity(rs.getInt("capacity"));
                rooms.add(room);
            }

            cbRoom.setItems(rooms);

        } catch (SQLException e) {
            showError("Chyba načítania izieb: " + e.getMessage());
        }
    }

    private void loadServices() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            ObservableList<Service> services = FXCollections.observableArrayList();

            String sql = "SELECT * FROM service ORDER BY name";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Service service = new Service();
                service.setId(rs.getInt("id"));
                service.setName(rs.getString("name"));
                service.setPrice(rs.getDouble("price"));
                services.add(service);
            }

            cbService.setItems(services);

        } catch (SQLException e) {
            showError("Chyba načítania služieb: " + e.getMessage());
        }
    }

    private void loadReservations() {
        reservations.clear();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            String sql = "SELECT r.id, r.room_id, r.responsible_guest_id, r.guest_count, r.date_from, r.date_to, r.status, " +
                    "ro.room_number, CONCAT(g.first_name, ' ', g.last_name) as guest_name " +
                    "FROM reservation r " +
                    "JOIN room ro ON r.room_id = ro.id " +
                    "JOIN guest g ON r.responsible_guest_id = g.id " +
                    "ORDER BY r.date_from DESC";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Reservation res = new Reservation();
                res.setId(rs.getInt("id"));
                res.setRoomId(rs.getInt("room_id"));
                res.setResponsibleGuestId(rs.getInt("responsible_guest_id"));
                res.setGuestCount(rs.getInt("guest_count"));
                res.setDateFrom(rs.getDate("date_from").toLocalDate());
                res.setDateTo(rs.getDate("date_to").toLocalDate());
                res.setStatus(rs.getString("status"));
                res.setRoomNumber(rs.getString("room_number"));
                res.setGuestName(rs.getString("guest_name"));
                reservations.add(res);
            }

            tableReservations.setItems(reservations);

        } catch (SQLException e) {
            showError("Chyba načítania rezervácií: " + e.getMessage());
        }
    }

    private void loadReservationToForm(Reservation res) {
        currentReservation = res;

        for (Room room : cbRoom.getItems()) {
            if (room.getId() == res.getRoomId()) {
                cbRoom.setValue(room);
                break;
            }
        }

        loadGuestData(res.getResponsibleGuestId());

        dpFrom.setValue(res.getDateFrom());
        dpTo.setValue(res.getDateTo());

        if (spinnerGuestCount.getValueFactory() != null) {
            spinnerGuestCount.getValueFactory().setValue(res.getGuestCount());
        } else {
            spinnerGuestCount.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, res.getGuestCount()));
        }

        loadReservationServices(res.getId());
    }

    private void loadGuestData(int guestId) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            String sql = "SELECT first_name, last_name, email, phone FROM guest WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, guestId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                txtFirstName.setText(rs.getString("first_name"));
                txtLastName.setText(rs.getString("last_name"));
                txtEmail.setText(rs.getString("email"));
                txtPhone.setText(rs.getString("phone"));
            }
        } catch (SQLException e) {
            showError("Chyba načítania hosťa: " + e.getMessage());
        }
    }

    private void loadReservationServices(int reservationId) {
        selectedServices.clear();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            String sql = "SELECT s.id, s.name, s.price, rs.quantity FROM service s " +
                    "JOIN reservation_service rs ON s.id = rs.service_id " +
                    "WHERE rs.reservation_id = ? " +
                    "ORDER BY s.name";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, reservationId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Service service = new Service();
                service.setId(rs.getInt("id"));
                service.setName(rs.getString("name"));
                service.setPrice(rs.getDouble("price"));
                service.setQuantity(rs.getInt("quantity"));
                selectedServices.add(service);
            }

        } catch (SQLException e) {
            showError("Chyba načítania služieb: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddService() {
        Service selected = cbService.getValue();
        if (selected == null) {
            showError("Najprv vyberte službu!");
            return;
        }

        Service existingService = null;
        for (Service s : selectedServices) {
            if (s.getId() == selected.getId()) {
                existingService = s;
                break;
            }
        }

        if (existingService != null) {
            existingService.setQuantity(existingService.getQuantity() + 1);
            listSelectedServices.refresh();
            showInfo("Služba '" + selected.getName() + "' pridaná! (Počet: " + existingService.getQuantity() + ")");
        } else {
            Service newService = new Service(selected.getId(), selected.getName(), selected.getPrice());
            newService.setQuantity(1);
            selectedServices.add(newService);
            showInfo("Služba '" + selected.getName() + "' pridaná!");
        }
        cbService.setValue(null);
    }

    @FXML
    private void handleRemoveService() {
        Service selected = listSelectedServices.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selectedServices.remove(selected);
        } else {
            showError("Najprv vyberte službu na odobratie!");
        }
    }

    @FXML
    private void handleAdd() {
        if (!validateForm()) {
            return;
        }

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {

            String sql = "INSERT INTO guest (first_name, last_name, email, phone) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, txtFirstName.getText());
            ps.setString(2, txtLastName.getText());
            ps.setString(3, txtEmail.getText());
            ps.setString(4, txtPhone.getText());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            rs.next();
            int guestId = rs.getInt(1);


            sql = "INSERT INTO reservation (room_id, responsible_guest_id, guest_count, date_from, date_to, status) VALUES (?, ?, ?, ?, ?, 'CREATED')";
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, cbRoom.getValue().getId());
            ps.setInt(2, guestId);
            ps.setInt(3, spinnerGuestCount.getValue());
            ps.setDate(4, Date.valueOf(dpFrom.getValue()));
            ps.setDate(5, Date.valueOf(dpTo.getValue()));
            ps.executeUpdate();

            rs = ps.getGeneratedKeys();
            rs.next();
            int reservationId = rs.getInt(1);


            for (Service service : selectedServices) {
                sql = "INSERT INTO reservation_service (reservation_id, service_id, quantity) VALUES (?, ?, ?)";
                ps = conn.prepareStatement(sql);
                ps.setInt(1, reservationId);
                ps.setInt(2, service.getId());
                ps.setInt(3, service.getQuantity());
                ps.executeUpdate();
            }

            showInfo("Rezervácia vytvorená!");
            clearForm();
            loadReservations();

        } catch (Exception e) {
            showError("Chyba: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        if (currentReservation == null) {
            showError("Najprv vyberte rezerváciu!");
            return;
        }

        if (!validateForm()) {
            return;
        }

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {

            String sql = "UPDATE guest SET first_name = ?, last_name = ?, email = ?, phone = ? WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, txtFirstName.getText());
            ps.setString(2, txtLastName.getText());
            ps.setString(3, txtEmail.getText());
            ps.setString(4, txtPhone.getText());
            ps.setInt(5, currentReservation.getResponsibleGuestId());
            ps.executeUpdate();


            sql = "UPDATE reservation SET room_id = ?, guest_count = ?, date_from = ?, date_to = ? WHERE id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, cbRoom.getValue().getId());
            ps.setInt(2, spinnerGuestCount.getValue());
            ps.setDate(3, Date.valueOf(dpFrom.getValue()));
            ps.setDate(4, Date.valueOf(dpTo.getValue()));
            ps.setInt(5, currentReservation.getId());
            ps.executeUpdate();


            sql = "DELETE FROM reservation_service WHERE reservation_id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, currentReservation.getId());
            ps.executeUpdate();


            for (Service service : selectedServices) {
                sql = "INSERT INTO reservation_service (reservation_id, service_id, quantity) VALUES (?, ?, ?)";
                ps = conn.prepareStatement(sql);
                ps.setInt(1, currentReservation.getId());
                ps.setInt(2, service.getId());
                ps.setInt(3, service.getQuantity());
                ps.executeUpdate();
            }

            showInfo("Rezervácia aktualizovaná!");
            clearForm();
            loadReservations();

        } catch (Exception e) {
            showError("Chyba: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        if (currentReservation == null) {
            showError("Najprv vyberte rezerváciu!");
            return;
        }

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            String sql = "DELETE FROM reservation WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, currentReservation.getId());
            ps.executeUpdate();

            showInfo("Rezervácia vymazaná!");
            clearForm();
            loadReservations();

        } catch (Exception e) {
            showError("Chyba: " + e.getMessage());
        }
    }


    private void clearForm() {
        currentReservation = null;
        cbRoom.setValue(null);
        txtFirstName.clear();
        txtLastName.clear();
        txtEmail.clear();
        txtPhone.clear();
        if (spinnerGuestCount.getValueFactory() != null) spinnerGuestCount.getValueFactory().setValue(1);
        dpFrom.setValue(null);
        dpTo.setValue(null);
        selectedServices.clear();
        cbService.setValue(null);
        tableReservations.getSelectionModel().clearSelection();
    }

    private void openDetailWindow(int reservationId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/skuska/detail-view.fxml"));
            Parent root = loader.load();

            DetailController controller = loader.getController();
            controller.setReservationId(reservationId);

            Stage stage = new Stage();
            stage.setTitle("Detail rezervácie #" + reservationId);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root, 600, 400));
            stage.show();

        } catch (Exception e) {
            showError("Chyba otvorenia detailu: " + e.getMessage());
        }
    }

    private boolean validateForm() {
        String firstName = txtFirstName.getText().trim();
        String lastName = txtLastName.getText().trim();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();

        if (firstName.isEmpty() || lastName.isEmpty()) {
            showError("Meno a priezvisko sú povinné polia.");
            return false;
        }

        if (email.isEmpty()) {
            showError("Email je povinné pole.");
            return false;
        }

        if (!email.contains("@")) {
            showError("Email musí obsahovať znak @");
            return false;
        }

        if (phone.isEmpty()) {
            showError("Telefón je povinné pole.");
            return false;
        }

        if (phone.length() < 10) {
            showError("Telefón musí mať minimálne 10 znakov");
            return false;
        }

        if (dpFrom.getValue() == null) {
            showError("Dátum od je povinné pole.");
            return false;
        }

        if (dpTo.getValue() == null) {
            showError("Dátum do je povinné pole.");
            return false;
        }

        if (dpTo.getValue().isBefore(dpFrom.getValue())) {
            showError("Dátum do nemôže byť pred dátumom od.");
            return false;
        }

        return true;
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Chyba");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.show();
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Info");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.show();
    }
}
