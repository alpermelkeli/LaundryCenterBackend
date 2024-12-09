package org.alpermelkeli.Rest;

import org.alpermelkeli.model.Device;
import org.alpermelkeli.model.Machine;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.util.Map;


public interface DeviceRestApiInterface {
    @GetMapping("/{deviceId}/turnOn")
    String turnOnRelay(@PathVariable String deviceId, @RequestParam String RelayNo);

    @GetMapping("/{deviceId}/turnOff")
    String turnOffRelay(@PathVariable String deviceId, @RequestParam String RelayNo);

    @GetMapping("/{deviceId}/reset")
    String resetDevice(@PathVariable String deviceId);

    @GetMapping("/getDevices")
    Map<String, List<Device>> getDevices();

    @GetMapping("/{companyId}/getDevices")
    List<Device> getDevicesByCompany(@PathVariable String companyId);

    @GetMapping("/getMachine")
    Machine getMachine(@RequestParam String companyId, @RequestParam String deviceId, @RequestParam String machineId);

}
