package org.alpermelkeli.firebase;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.protobuf.Api;
import org.alpermelkeli.model.Device;
import org.alpermelkeli.model.Machine;
import org.alpermelkeli.model.State;
import org.alpermelkeli.model.User;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Service for interacting with the Firebase Firestore database. Provides methods to
 * retrieve and manage data related to users, companies, devices, and machines.
 */
@Service
public class FirebaseFirestoreService {

    private final Firestore firestore = FirebaseInitializer.getFirestore();

    public User getUser(){
        return null;
    }

    public Map<String, List<Device>> getDevices() {
        Map<String, List<Device>> devicesByCompany = new HashMap<>();

        try {
            ApiFuture<QuerySnapshot> companyFuture = firestore.collection("Company").get();
            QuerySnapshot companySnapshot = companyFuture.get();

            for (DocumentSnapshot companyDoc : companySnapshot.getDocuments()) {
                String companyName = companyDoc.getId();
                List<Device> devices = new ArrayList<>();

                ApiFuture<QuerySnapshot> devicesFuture = companyDoc.getReference().collection("Devices").get();
                QuerySnapshot deviceSnapshot = devicesFuture.get();

                for (DocumentSnapshot deviceDoc : deviceSnapshot.getDocuments()) {
                    Device device = new Device();
                    device.setId(deviceDoc.getId());

                    String stateValue = deviceDoc.getString("status");
                    device.setState(stateValue != null && stateValue.equalsIgnoreCase("connected")
                            ? State.CONNECTED
                            : State.DISCONNECTED);

                    ApiFuture<QuerySnapshot> machinesFuture = deviceDoc.getReference().collection("Machines").get();
                    QuerySnapshot machineSnapshot = machinesFuture.get();

                    List<Machine> machines = new ArrayList<>();
                    for (DocumentSnapshot machineDoc : machineSnapshot.getDocuments()) {
                        Machine machine = new Machine(
                                machineDoc.getBoolean("active"),
                                machineDoc.getId(),
                                machineDoc.getString("name"),
                                machineDoc.getLong("start"),
                                machineDoc.getLong("time")
                        );
                        machines.add(machine);
                    }
                    device.setMachines(machines);

                    devices.add(device);
                }

                devicesByCompany.put(companyName, devices);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return devicesByCompany;
    }

    public List<Device> getDevicesByCompany(String companyId) {
        List<Device> devices = new ArrayList<>();

        try {
                ApiFuture<DocumentSnapshot> companyDoc = firestore.collection("Company").document(companyId).get();
                ApiFuture<QuerySnapshot> devicesFuture = companyDoc.get().getReference().collection("Devices").get();
                QuerySnapshot deviceSnapshot = devicesFuture.get();
                for (DocumentSnapshot deviceDoc : deviceSnapshot.getDocuments()) {
                    Device device = new Device();
                    device.setId(deviceDoc.getId());

                    String stateValue = deviceDoc.getString("status");
                    device.setState(stateValue != null && stateValue.equalsIgnoreCase("connected")
                            ? State.CONNECTED
                            : State.DISCONNECTED);

                    ApiFuture<QuerySnapshot> machinesFuture = deviceDoc.getReference().collection("Machines").get();
                    QuerySnapshot machineSnapshot = machinesFuture.get();

                    List<Machine> machines = new ArrayList<>();
                    for (DocumentSnapshot machineDoc : machineSnapshot.getDocuments()) {
                        Machine machine = new Machine(
                                machineDoc.getBoolean("active"),
                                machineDoc.getId(),
                                machineDoc.getString("name"),
                                machineDoc.getLong("start"),
                                machineDoc.getLong("time")
                        );
                        machines.add(machine);
                    }
                    device.setMachines(machines);

                    devices.add(device);
                }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return devices;
    }

    public Machine getMachine(String companyId, String deviceId, String machineId){
        try {
            ApiFuture<DocumentSnapshot> companyDoc = firestore.collection("Company")
                    .document(companyId)
                    .collection("Devices")
                    .document(deviceId)
                    .collection("Machines")
                    .document(machineId)
                    .get();

            DocumentSnapshot document = companyDoc.get();

            if (document.exists()) {
                Machine machine = new Machine(
                        document.getBoolean("active"),
                        document.getId(),
                        document.getString("name"),
                        document.getLong("start"),
                        document.getLong("time")
                );
                return machine;
            }
            else {
                return null;
            }

        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public void refreshDeviceStatus(String deviceId, boolean active){
        try {
            ApiFuture<QuerySnapshot> companiesFuture = firestore.collection("Company").get();
            List<QueryDocumentSnapshot> companyDocuments = companiesFuture.get().getDocuments();
            for (QueryDocumentSnapshot companyDoc : companyDocuments) {
                CollectionReference devicesCollection = companyDoc.getReference().collection("Devices");
                ApiFuture<QuerySnapshot> devicesFuture = devicesCollection.get();
                List<QueryDocumentSnapshot> deviceDocuments = devicesFuture.get().getDocuments();

                for (QueryDocumentSnapshot deviceDoc : deviceDocuments) {
                    if (deviceDoc.getId().equals(deviceId)) {
                        String status = active ? "connected" : "disconnected";
                        ApiFuture<WriteResult> updateFuture = deviceDoc.getReference().update("status", status);

                        System.out.println("Device " + deviceId + " status updated to: " + status);
                        updateFuture.get();
                        return;
                    }
                }
            }
            System.out.println("Device with ID " + deviceId + " not found.");
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error updating device status: " + e.getMessage());
        }
    }

    public void refreshMachineStatus(String companyId, String deviceId, String relayNo, boolean active){
        DocumentReference machineRef = firestore
                .collection("Company")
                .document(companyId)
                .collection("Devices")
                .document(deviceId)
                .collection("Machines")
                .document(relayNo);

        Map<String, Object> updates = new HashMap<>();

        /*Make selected machine active*/
        updates.put("active", active);

        machineRef.update(updates);


    }

    public void refreshMachineTime(String companyId, String deviceId, String relayNo, long currenttime ,long time){
        DocumentReference machineRef = firestore
                .collection("Company")
                .document(companyId)
                .collection("Devices")
                .document(deviceId)
                .collection("Machines")
                .document(relayNo);

        Map<String, Object> updates = new HashMap<>();
        /*Make selected machine active*/

        updates.put("time", time);
        updates.put("start", currenttime);
        /*Update time and start*/

        ApiFuture<WriteResult> writeResult = machineRef.update(updates);

        try {
            System.out.println("Güncelleme zamanı: " + writeResult.get().getUpdateTime());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Güncelleme sırasında hata oluştu: " + e.getMessage());
        }
    }

    public Map<String, Double> getUserBalance(String userId) {
        DocumentReference userRef = firestore
                .collection("Users")
                .document(userId);
        Map<String, Double> result = new HashMap<>();
        ApiFuture<DocumentSnapshot> userDoc = userRef.get();
        try {
            DocumentSnapshot document = userDoc.get();

            if (document.exists()) {
                if (document.contains("balance")) {
                    Double balance = document.getDouble("balance");
                    result.put("balance", balance);
                } else {
                    System.out.println("Balance alanı bulunamadı.");
                }
            } else {
                System.out.println("Kullanıcı bulunamadı.");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    /*TODO check this function return type etc..*/
    public CompletableFuture<Map<String, Long>> increaseMachineTime(String companyId, String deviceId, String machineId, int time) {
        DocumentReference machineRef = firestore.collection("Company")
                .document(companyId)
                .collection("Devices")
                .document(deviceId)
                .collection("Machines")
                .document(machineId);

        CompletableFuture<Map<String, Long>> futureResult = new CompletableFuture<>();

        machineRef.get().addListener(() -> {
            try {
                DocumentSnapshot document = machineRef.get().get();

                if (document.exists()) {
                    Long currentTime = document.getLong("time");
                    Long startTime = document.getLong("start");
                    if (currentTime == null) {
                        currentTime = 0L;
                    }
                    long newTime = currentTime + time;

                    machineRef.update("time", newTime).addListener(() -> {
                        Map<String, Long> response = new HashMap<>();
                        response.put("newTime", newTime);
                        response.put("startTime", startTime);
                        futureResult.complete(response);
                    }, Runnable::run);

                } else {
                    futureResult.completeExceptionally(new Exception("Document does not exist"));
                }
            } catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
                futureResult.completeExceptionally(e);
            }
        }, Runnable::run);

        return futureResult;
    }

    public double getCompanyPrice(String companyId){
        try {
            ApiFuture<DocumentSnapshot> companyDoc = firestore.collection("Company")
                    .document(companyId)
                    .get();

            DocumentSnapshot document = companyDoc.get();

            if (document.exists()) {

                return document.getDouble("price") != null ? document.getDouble("price") : 0.0;
            }
            else {
                return 0.0;
            }

        }
        catch (Exception e){
            e.printStackTrace();
            return 0.0;
        }
    }
}






