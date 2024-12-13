package org.alpermelkeli.Rest;

import org.alpermelkeli.model.Device;
import org.alpermelkeli.model.Machine;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.util.Map;


/**
 * DeviceRestApiInterface provides a set of REST API endpoints for managing and interacting with devices
 * and their respective components, such as relays and machines.
 *
 * It defines methods to perform operations like turning on/off relays, resetting devices, fetching device and machine details,
 * and updating machine time using HTTP POST and GET requests.
 */
public interface DeviceRestApiInterface {
    
    
    @PostMapping("/turnOn")
    String turnOnRelay(@RequestParam String companyId, @RequestParam String deviceId, @RequestParam String relayNo, @RequestParam String time);

    @PostMapping("/turnOff")
    String turnOffRelay(@RequestParam String companyId, @RequestParam String deviceId, @RequestParam String relayNo);

    @GetMapping("/{deviceId}/reset")
    String resetDevice(@PathVariable String deviceId);

    @GetMapping("/getDevices")
    Map<String, List<Device>> getDevices();

    @GetMapping("/{companyId}/getDevices")
    List<Device> getDevicesByCompany(@PathVariable String companyId);

    @GetMapping("/getMachine")
    Machine getMachine(@RequestParam String companyId, @RequestParam String deviceId, @RequestParam String machineId);

    @PostMapping("/increaseMachineTime")
    String increaseMachineTime(@RequestParam String companyId, @RequestParam String deviceId, @RequestParam String machineId, @RequestParam String time);
}
