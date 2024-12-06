package org.alpermelkeli.Rest;

import org.alpermelkeli.MQTT.MQTTController;
import org.alpermelkeli.firebase.FirebaseController;
import org.alpermelkeli.model.Device;
import org.alpermelkeli.model.Machine;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@RestController
@RequestMapping("/api/devices")
class DeviceRestController implements RestApiInterface{

    private MQTTController mqttController;
    private FirebaseController firebaseController;

    public DeviceRestController() {
        mqttController = new MQTTController();
        firebaseController = new FirebaseController();
    }

    @Override
    public String turnOnRelay(@PathVariable String deviceId, @RequestParam String RelayNo) {
        return mqttController.sendCommand(deviceId, RelayNo, "ON");
    }

    @Override
    public String turnOffRelay(@PathVariable String deviceId, @RequestParam String RelayNo) {
        return mqttController.sendCommand(deviceId, RelayNo, "OFF");
    }

    @Override
    public String resetDevice(@PathVariable String deviceId) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

        for (int i = 1; i < 5; i++) {
            int relayNumber = i;
            scheduler.schedule(() -> mqttController.sendCommand(deviceId, String.valueOf(relayNumber), "OFF"),
                    i, TimeUnit.SECONDS);
        }
        scheduler.shutdown();

        return "Device reset successfully: " + deviceId;
    }

    @Override
    public List<Device> getDevices(){
        return firebaseController.getDevices();
    }

    @Override
    public List<Device> getDevicesByCompany(@PathVariable String companyId){
        return firebaseController.getDevicesByCompany(companyId);
    }

    @Override
    public Machine getMachine(@RequestParam String companyId, @RequestParam String deviceId, @RequestParam String machineId) {
        return firebaseController.getMachine(companyId, deviceId, machineId);
    }

    @Override
    public String setDeviceConnected(String deviceId) {
        return "";
    }


}
