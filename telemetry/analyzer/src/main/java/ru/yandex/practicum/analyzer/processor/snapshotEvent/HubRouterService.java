package ru.yandex.practicum.analyzer.processor.snapshotEvent;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.model.Action;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;


import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Component
public class HubRouterService {
    private final Logger log = LoggerFactory.getLogger(HubRouterService.class);
    private final HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient;
    private final long sendTimeotMs;

    public HubRouterService(@GrpcClient("hub-router") HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient,
                            @Value("${grpc.client.hub-router.send-timeout-ms}") Integer sendTimeotMs) {
        this.hubRouterClient = hubRouterClient;
        this.sendTimeotMs = sendTimeotMs;
    }

    public void sendDeviceAction(String hubId, String deviceId, String scenarioName, Action action) {
        ActionTypeProto actionTypeProto = ActionTypeProto.valueOf(action.getType().name());

        DeviceActionProto deviceActionProto = DeviceActionProto.newBuilder()
                .setSensorId(deviceId)
                .setType(actionTypeProto)
                .build();

        Integer actionValue = action.getValue();
        if (actionValue != null) {
            deviceActionProto = DeviceActionProto.newBuilder(deviceActionProto)
                    .setValue(action.getValue()).build();
        }


        Instant now = Instant.now();
        com.google.protobuf.Timestamp gts = com.google.protobuf.Timestamp.newBuilder()
                .setSeconds(now.getEpochSecond())
                .setNanos(now.getNano())
                .build();

        DeviceActionRequest request = DeviceActionRequest.newBuilder()
                .setHubId(hubId)
                .setScenarioName(scenarioName)
                .setAction(deviceActionProto)
                .setTimestamp(gts)
                .build();

        log.info("do action request={}", request);
        hubRouterClient.withDeadlineAfter(sendTimeotMs, TimeUnit.MILLISECONDS).handleDeviceAction(request);
    }


}