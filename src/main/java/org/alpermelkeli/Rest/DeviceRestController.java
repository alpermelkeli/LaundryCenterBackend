package org.alpermelkeli.Rest;

import org.alpermelkeli.MQTT.MQTTControllerService;
import org.alpermelkeli.TimeController.DeviceTimeController;
import org.alpermelkeli.firebase.FirebaseFirestoreService;
import org.alpermelkeli.model.Device;
import org.alpermelkeli.model.Machine;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * DeviceDeviceRestController is a REST controller for managing devices and their components.
 * It provides endpoints for operations such as turning relays on/off, resetting devices,
 * retrieving device and machine details, updating machine time, and obtaining company-specific pricing information.
 *
 * This controller implements the {@link DeviceRestApiInterface} and relies on injected services
 * (MQTTControllerService, FirebaseFirestoreService, DeviceTimeController) to perform the underlying logic
 * and communication with external systems.
 *
 * Constructor:
 * Initializes the controller with required service dependencies.
 *
 * Methods:
 * - turnOnRelay: Sends a command to turn on a relay, updates machine status and time, and starts-monitoring relay.
 * - turnOffRelay: Sends a command to turn off a relay and updates its status.
 * - resetDevice: Resets all relays of a device to the OFF state with a timed sequence.
 * - getDevices: Fetches a map of all devices.
 * - getDevicesByCompany: Fetches a list of devices belonging to a specific company.
 * - getMachine: Fetches machine details for a given company, device, and machine.
 * - increaseMachineTime: Increases the operating time for a specific machine and updates related tracking processes.
 * - getPrice: Retrieves the price information associated with a specific company.
 *
 * This controller facilitates communication between clients and services while maintaining synchronized device states.
 */
@RestController
@RequestMapping("/api/devices")
class DeviceRestController implements DeviceRestApiInterface {

    private final MQTTControllerService mqttControllerService;
    private final FirebaseFirestoreService firebaseFirestoreService;
    private final DeviceTimeController deviceTimeController;
    /*Service injections */
    public DeviceRestController(FirebaseFirestoreService firebaseFirestoreService, MQTTControllerService mqttControllerService, DeviceTimeController deviceTimeController) {
        this.firebaseFirestoreService = firebaseFirestoreService;
        this.mqttControllerService = mqttControllerService;
        this.deviceTimeController = deviceTimeController;
    }

    @Override
    public String turnOnRelay(@RequestParam String companyId, @RequestParam String deviceId, @RequestParam String relayNo, @RequestParam String time) {
        mqttControllerService.sendTurnOnOffCommand(deviceId, relayNo, "ON");
        firebaseFirestoreService.refreshMachineStatus(companyId, deviceId, relayNo, true);
        long currentTimeMillis = System.currentTimeMillis();
        firebaseFirestoreService.refreshMachineTime(companyId, deviceId, relayNo, currentTimeMillis, Long.parseLong(time));
        deviceTimeController.startWatchingDevice(companyId, deviceId, relayNo, currentTimeMillis, Long.parseLong(time), (cId, dId, mId) -> {
            mqttControllerService.sendTurnOnOffCommand(dId, mId, "OFF");
            firebaseFirestoreService.refreshMachineStatus(cId, dId, mId, false);
        });
        return "Success";
    }

    @Override
    public String turnOffRelay(@RequestParam String companyId, @RequestParam String deviceId, @RequestParam String relayNo) {
        mqttControllerService.sendTurnOnOffCommand(deviceId, relayNo, "OFF");
        firebaseFirestoreService.refreshMachineStatus(companyId, deviceId, relayNo, false);
        return "Success";
    }

    @Override
    public String resetDevice(@PathVariable String deviceId) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

        for (int i = 1; i < 5; i++) {
            int relayNumber = i;
            scheduler.schedule(() -> mqttControllerService.sendTurnOnOffCommand(deviceId, String.valueOf(relayNumber), "OFF"),
                    i, TimeUnit.SECONDS);
        }
        scheduler.shutdown();

        return "Device reset successfully: " + deviceId;
    }

    @Override
    public Map<String, List<Device>> getDevices(){
        return firebaseFirestoreService.getDevices();
    }

    @Override
    public List<Device> getDevicesByCompany(@PathVariable String companyId){
        return firebaseFirestoreService.getDevicesByCompany(companyId);
    }

    @Override
    public Machine getMachine(@RequestParam String companyId, @RequestParam String deviceId, @RequestParam String machineId) {
        return firebaseFirestoreService.getMachine(companyId, deviceId, machineId);
    }

    @Override
    public String increaseMachineTime(String companyId, String deviceId, String machineId, String time) {
        if (companyId == null || companyId.isEmpty() || deviceId == null || deviceId.isEmpty() || machineId == null || machineId.isEmpty() || time == null || time.isEmpty()) {
            throw new IllegalArgumentException("Geçersiz giriş parametreleri sağlandı.");
        }

        Map<String, Long> response;
        try {
            response = firebaseFirestoreService.increaseMachineTime(companyId, deviceId, machineId, Integer.parseInt(time)).get();
        } catch (Exception e) {
            throw new RuntimeException("Makine süresi artırılamadı: " + e.getMessage(), e);
        }

        deviceTimeController.startWatchingDevice(
                companyId,
                deviceId,
                machineId,
                response.get("startTime"),
                response.get("newTime"),
                (cId, dId, mId) -> {
                    mqttControllerService.sendTurnOnOffCommand(dId, mId, "OFF");
                    firebaseFirestoreService.refreshMachineStatus(cId, dId, mId, false);
                }
        );

        return "Increased to " + response.get("time") + " From start " + response.get("start");
    }

    @Override
    public double getPrice(String companyId) {
        return firebaseFirestoreService.getCompanyPrice(companyId);
    }


}
