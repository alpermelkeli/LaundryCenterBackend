package org.alpermelkeli.TimeController;

import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DeviceTimeController {

    private final Map<String, ScheduledExecutorService> deviceTimers = new ConcurrentHashMap<>();

    /**
     * Makinenin start ve time alanlarına göre zamanlayıcı başlatır.
     *
     * @param companyId Şirket ID'si
     * @param deviceId Cihaz ID'si
     * @param machineId Makine ID'si
     * @param startTimeInMillis Makinenin başlatıldığı zaman (milisaniye cinsinden)
     * @param durationInMillis Makinenin çalışması gereken süre (saniye cinsinden)
     * @param callback Süre dolduğunda çalışacak callback
     */
    public void startWatchingDevice(String companyId, String deviceId, String machineId, long startTimeInMillis, long durationInMillis, OnTimeIsDoneCallback callback) {
        stopWatchingDevice(deviceId); // Aynı cihaz için var olan zamanlayıcıyı durdur

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        deviceTimers.put(deviceId, scheduler);

        long currentTimeInMillis = System.currentTimeMillis();
        long remainingTimeInMillis = (startTimeInMillis + durationInMillis) - currentTimeInMillis;

        if (remainingTimeInMillis > 0) {
            scheduler.schedule(() -> {
                stopWatchingDevice(deviceId);
                System.out.println("Device " + deviceId + " turned off after " + remainingTimeInMillis + " milliseconds.");
                callback.onTimeIsDone(companyId, deviceId, machineId);
            }, remainingTimeInMillis, TimeUnit.MILLISECONDS);
        } else {
            stopWatchingDevice(deviceId);
            System.out.println("Device " + deviceId + " should already have been turned off.");
            callback.onTimeIsDone(companyId, deviceId, machineId);
        }
    }

    /**
     * Belirtilen cihaz için zamanlayıcıyı durdurur.
     */
    public void stopWatchingDevice(String deviceId) {
        ScheduledExecutorService scheduler = deviceTimers.remove(deviceId);
        if (scheduler != null) {
            scheduler.shutdownNow();
            System.out.println("Device " + deviceId + " tracking stopped.");
        }
    }

    /**
     * Süre dolduğunda çalışacak callback arayüzü.
     */
    public interface OnTimeIsDoneCallback {
        void onTimeIsDone(String companyId, String deviceId, String machineId);
    }
}
