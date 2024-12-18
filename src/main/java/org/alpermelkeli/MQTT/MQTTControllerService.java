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
import java.util.Timer;
import java.util.TimerTask;

/**
 * MQTTControllerService is responsible for managing the MQTT interactions
 * within the application. It establishes and maintains a connection to the MQTT broker,
 * subscribes to relevant topics, processes received messages, and provides functionality
 * to send commands to specific devices.
 *
 * The service supports automatic reconnection in case the connection to the broker is lost
 * and manages device statuses with the help of the FirebaseFirestoreService.
 *
 * The main responsibilities of the service include:
 * - Establishing and maintaining a connection to the MQTT broker.
 * - Subscribing to topics to listen for messages and device status updates.
 * - Processing received MQTT messages and delegating actions to FirebaseFirestoreService.
 * - Sending control commands to devices via MQTT topics.
 * - Automatically resubscribing to topics after reconnection.
 */
@Service
public class MQTTControllerService {
    private final FirebaseFirestoreService firebaseFirestoreService;
    private static final String mqttBroker = ConfigUtil.getProperty("BROKER_ADDRESS");
    private static final String clientId = "SpringBootClient";
    private MqttClient mqttClient;
    private final Timer reconnectTimer = new Timer(true);


    public MQTTControllerService(FirebaseFirestoreService firebaseFirestoreService){
        this.firebaseFirestoreService = firebaseFirestoreService;

        setup();

        listenMessages();

        checkDeviceStatus(this::refreshDeviceStatus);

        startReconnectTask();
    }

    private void setup() {
        try {
            mqttClient = new MqttClient(mqttBroker, clientId, new MemoryPersistence());
            connect();
        } catch (MqttException e) {
            throw new RuntimeException("MQTT client setup failed!", e);
        }
    }

    private void connect() {
        try {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            options.setKeepAliveInterval(20);

            mqttClient.connect(options);

            System.out.println("MQTT connection established!");

        } catch (MqttException e) {
            System.err.println("MQTT connection failed: " + e.getMessage());
        }
    }

    private void startReconnectTask() {
        reconnectTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!mqttClient.isConnected()) {
                    System.out.println("MQTT connection lost. Attempting to reconnect...");
                    connect();
                    if (mqttClient.isConnected()) {
                        try {
                            listenMessages();
                            checkDeviceStatus(MQTTControllerService.this::refreshDeviceStatus);
                            System.out.println("Reconnected to MQTT broker.");
                        } catch (Exception e) {
                            System.err.println("Error resubscribing to topics: " + e.getMessage());
                        }
                    }
                }
                else{
                    System.out.println("Connected...");
                }
            }
        }, 0, 20000);
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

    private void checkDeviceStatus(RefreshStatusCallback callback){
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

    private void refreshDeviceStatus(Status status){
        firebaseFirestoreService.refreshDeviceStatus(status.deviceId, status.active);
    }

    public String sendTurnOnOffCommand(String deviceId, String relayNo, String command) {
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

