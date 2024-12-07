package org.alpermelkeli.Rest;

import org.alpermelkeli.model.Device;
import org.alpermelkeli.model.Machine;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;


public interface DeviceRestApiInterface {
    @GetMapping("/{deviceId}/turnOn")
    public String turnOnRelay(@PathVariable String deviceId, @RequestParam String RelayNo);

    @GetMapping("/{deviceId}/turnOff")
    public String turnOffRelay(@PathVariable String deviceId, @RequestParam String RelayNo);

    @GetMapping("/{deviceId}/reset")
    public String resetDevice(@PathVariable String deviceId);

    @GetMapping("/getDevices")
    public List<Device> getDevices();

    @GetMapping("/{companyId}/getDevices")
    public List<Device> getDevicesByCompany(@PathVariable String companyId);

    @GetMapping("/getMachine")
    public Machine getMachine(@RequestParam String companyId, @RequestParam String deviceId, @RequestParam String machineId);

    @GetMapping("/{deviceId}/setConnected")
    public String setDeviceConnected(@PathVariable String deviceId);
}
