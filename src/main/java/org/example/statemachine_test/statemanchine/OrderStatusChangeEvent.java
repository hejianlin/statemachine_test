package org.example.statemachine_test.statemanchine;

/**
 * 订单状态改变事件
 */
public enum OrderStatusChangeEvent {
    //支付，发货，确认收货
    PAYED, DELIVERY, RECEIVED;
}