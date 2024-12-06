package org.alpermelkeli.MQTT;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.nio.charset.StandardCharsets;

public class MQTTController {
    private static final String mqttBroker = "tcp://35.239.219.113:1883";
    private static final String clientId = "SpringBootClient";
    private MqttClient mqttClient;

    public MQTTController(){

        setup();

        startSubscription();

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

    private void startSubscription(){
        try {
            mqttClient.subscribe("devices/#", (topic, message) -> {
                String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
                System.out.println("MQTT message received: " + topic + " - " + payload);
            });

        } catch (MqttException e) {
            throw new RuntimeException("MQTT connection failed!", e);
        }
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
}
