package org.alpermelkeli.MQTT;

import org.alpermelkeli.firebase.FirebaseFirestoreService;
import org.alpermelkeli.util.ConfigUtil;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class MQTTControllerService {
    private final FirebaseFirestoreService firebaseFirestoreService;
    private static final String mqttBroker = ConfigUtil.getProperty("BROKER_ADDRESS");
    private static final String clientId = "SpringBootClient";
    private MqttClient mqttClient;

    public MQTTControllerService(FirebaseFirestoreService firebaseFirestoreService){
        this.firebaseFirestoreService = firebaseFirestoreService;

        setup();

        listenMessages();

        checkStatus(this::refreshStatus);

    }

    private void setup(){
        try {
            mqttClient = new MqttClient(mqttBroker, clientId, new MemoryPersistence());

            MqttConnectOptions options = new MqttConnectOptions();

            options.setCleanSession(true);

            options.setConnectionTimeout(10);

            options.setKeepAliveInterval(20);

            mqttClient.connect(options);

        } catch (MqttException e) {
            throw new RuntimeException("MQTT connection failed!", e);
        }
    }

    private void listenMessages(){
        try {
            mqttClient.subscribe("devices/#", (topic, message) -> {
                String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
                System.out.println("MQTT message received: " + topic + " - " + payload);
            });

        } catch (MqttException e) {
            throw new RuntimeException("MQTT connection failed!", e);
        }
    }

    private void checkStatus(RefreshStatusCallback callback){
        try {
            mqttClient.subscribe("devices/+/status", (topic, message) -> {
                String deviceId = topic.split("/")[1];
                boolean isActive = new String(message.getPayload(), StandardCharsets.UTF_8).equals("connected");
                callback.onStatusReceived(new Status(deviceId, isActive));
            });

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void refreshStatus(Status status){
        firebaseFirestoreService.refreshDeviceStatus(status.deviceId, status.active);
    }

    public String sendCommand(String deviceId, String relayNo, String command) {
        try {
            String topic = "devices/" + deviceId + "/commands";
            String message = command + " " + relayNo;
            mqttClient.publish(topic, new MqttMessage(message.getBytes(StandardCharsets.UTF_8)));
            return "Command sent: " + command + " Roley " + relayNo;
        } catch (MqttException e) {
            return "Failed to send command: " + e.getMessage();
        }
    }

    private interface RefreshStatusCallback{
        void onStatusReceived(Status status);
    }

    private static class Status{
        String deviceId;
        boolean active;
        public Status(String deviceId, boolean active) {
            this.deviceId = deviceId;
            this.active = active;
        }
    }
}

