package org.alpermelkeli.firebase;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.alpermelkeli.model.Device;
import org.alpermelkeli.model.Machine;
import org.alpermelkeli.model.State;
import org.alpermelkeli.model.User;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

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

                    String stateValue = deviceDoc.getString("state");
                    device.setState(stateValue != null && stateValue.equalsIgnoreCase("CONNECTED")
                            ? State.CONNECTED
                            : State.DISCONNECTED);

                    ApiFuture<QuerySnapshot> machinesFuture = deviceDoc.getReference().collection("Machines").get();
                    QuerySnapshot machineSnapshot = machinesFuture.get();

                    List<Machine> machines = new ArrayList<>();
                    for (DocumentSnapshot machineDoc : machineSnapshot.getDocuments()) {
                        Machine machine = new Machine(
                                machineDoc.getBoolean("active"),
                                machineDoc.getId(),
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
}






