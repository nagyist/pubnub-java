package com.github.pubnubjtools.pusher;

import com.github.pubnubjtools.pusher.model.*;
import com.github.pubnubjtools.pusher.transport.Transport;

import java.io.IOException;
import java.util.Objects;

public class Pusher {

    private final Transport transport;
    private final PubnubConfig pubnubConfig;
    private final WallClock wallClock;
    private final RequestHelper requestHelper;

    public Pusher(Transport transport, PubnubConfig pubnubConfig) {
        this(transport, pubnubConfig, WallClock.INSTANCE, RequestHelper.INSTANCE);
    }

    Pusher(Transport transport, PubnubConfig pubnubConfig, WallClock wallClock, RequestHelper requestHelper) {
        this.transport = Objects.requireNonNull(transport);
        this.pubnubConfig = Objects.requireNonNull(pubnubConfig);
        this.wallClock = Objects.requireNonNull(wallClock);
        this.requestHelper = Objects.requireNonNull(requestHelper);
    }

    public DeliveryResult push(Message message) throws PubnubException {
        long startMillis = wallClock.currentTimeMillis();

        Request request = requestHelper.createRequest(message, pubnubConfig);
        DeliveryResult deliveryResult;
        try {
            final Response response = transport.executeHttpGetRequest(request);
            deliveryResult = new DeliveryResult(message, request, wallClock.currentTimeMillis() - startMillis, response);
        } catch (IOException exception) {
            deliveryResult = new DeliveryResult(message, request, wallClock.currentTimeMillis() - startMillis, exception);
        }
        if (deliveryResult.isFailed()) {
            throw new PubnubException(deliveryResult);
        }
        return deliveryResult;
    }

    public Transport getTransport() {
        return transport;
    }

    public PubnubConfig getPubnubConfig() {
        return pubnubConfig;
    }

}
