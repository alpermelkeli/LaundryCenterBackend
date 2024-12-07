package org.alpermelkeli.firebase;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import org.alpermelkeli.model.Device;
import org.alpermelkeli.model.Machine;
import org.alpermelkeli.model.State;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class FirebaseFirestoreService {

    private final Firestore firestore = FirebaseInitializer.getFirestore();

    public List<Device> getDevices() {
        List<Device> devices = new ArrayList<>();

        try {
            ApiFuture<QuerySnapshot> companyFuture = firestore.collection("Company").get();
            QuerySnapshot companySnapshot = companyFuture.get();

            for (DocumentSnapshot companyDoc : companySnapshot.getDocuments()) {
                ApiFuture<QuerySnapshot> devicesFuture = companyDoc.getReference().collection("Devices").get();
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
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return devices;
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

}






