package com.cricarena.backend.dto.response;

public class DeliveryResultResponse {

    private DeliveryResponse delivery;
    private InningsResponse innings;

    public DeliveryResultResponse() {
    }

    public DeliveryResultResponse(DeliveryResponse delivery, InningsResponse innings) {
        this.delivery = delivery;
        this.innings = innings;
    }

    public DeliveryResponse getDelivery() {
        return delivery;
    }

    public void setDelivery(DeliveryResponse delivery) {
        this.delivery = delivery;
    }

    public InningsResponse getInnings() {
        return innings;
    }

    public void setInnings(InningsResponse innings) {
        this.innings = innings;
    }
}
