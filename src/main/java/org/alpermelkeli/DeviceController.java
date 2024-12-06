package org.alpermelkeli;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;


@RestController
@RequestMapping("/devices")
class DeviceController {

    private final String mqttBroker = "tcp://34.69.200.78:1883";
    private final String clientId = "SpringBootClient";
    private MqttClient mqttClient;

    public DeviceController() {
        try {
            mqttClient = new MqttClient(mqttBroker, clientId, new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setConnectionTimeout(10); // Bağlantı zaman aşımı süresi (saniye)
            options.setKeepAliveInterval(20); // Keep-alive süresi (saniye)
            mqttClient.connect(options);

            mqttClient.subscribe("devices/#", (topic, message) -> {
                String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
                System.out.println("MQTT Mesajı Alındı: " + topic + " - " + payload);
            });

        } catch (MqttException e) {
            e.printStackTrace();
            throw new RuntimeException("MQTT bağlantısı başarısız!", e);
        }
    }

    @GetMapping("/{deviceId}/turnOn/relay")
    public String turnOnRelay(@PathVariable String deviceId, @RequestParam String RelayNo) {
        return sendCommand(deviceId, RelayNo, "ON");
    }

    @GetMapping("/{deviceId}/turnOff/relay")
    public String turnOffRelay(@PathVariable String deviceId, @RequestParam String RelayNo) {
        return sendCommand(deviceId, RelayNo, "OFF");
    }

    private String sendCommand(String deviceId, String relayNo, String command) {
        try {
            String topic = "devices/" + deviceId + "/commands";
            String message = command + " " + relayNo;
            mqttClient.publish(topic, new MqttMessage(message.getBytes(StandardCharsets.UTF_8)));
            return "Komut gönderildi: " + command + " Röle " + relayNo;
        } catch (MqttException e) {
            e.printStackTrace();
            return "Komut gönderimi başarısız: " + e.getMessage();
        }
    }
}
