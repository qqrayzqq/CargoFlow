package com.github.qqrayzqq.cargoflow.domain.enums;

import java.util.Set;

public enum ShipmentStatus {
    CREATED,
    PICKED_UP,
    IN_TRANSIT,
    AT_HUB,
    OUT_FOR_DELIVERY,
    DELIVERED,
    FAILED_ATTEMPT,
    RETURNED,
    CANCELLED;

    private Set<ShipmentStatus> allowedNext;

    static{
        CREATED.allowedNext = Set.of(PICKED_UP, CANCELLED);
        PICKED_UP.allowedNext = Set.of(IN_TRANSIT, CANCELLED);
        IN_TRANSIT.allowedNext = Set.of(CANCELLED, AT_HUB);
        AT_HUB.allowedNext = Set.of(CANCELLED, OUT_FOR_DELIVERY);
        OUT_FOR_DELIVERY.allowedNext = Set.of(FAILED_ATTEMPT, DELIVERED);
        DELIVERED.allowedNext = Set.of();
        FAILED_ATTEMPT.allowedNext = Set.of(OUT_FOR_DELIVERY, RETURNED);
        RETURNED.allowedNext = Set.of();
        CANCELLED.allowedNext = Set.of();
    }

    public boolean canTransitionTo(ShipmentStatus next){
        return allowedNext.contains(next);
    }
}
