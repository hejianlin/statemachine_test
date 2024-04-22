package org.example.statemachine_test.statemanchine;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service("orderService")
public class OrderServiceImpl implements IOrderService {

    //装配状态机的配置，来源于OrderStateMachineConfig的配置
    @Autowired
    private StateMachine<OrderStatus, OrderStatusChangeEvent> orderStateMachine;

    //装配状态机持久化的配置，来源于OrderStateMachineConfig的配置
    @Autowired
    private StateMachinePersister<OrderStatus, OrderStatusChangeEvent, Order> persister;

    private int id = 1;
    private Map<Integer, Order> orders = new HashMap<>();

    /**
     * 构建消息对象，采用spring的消息构造器
     * @param event
     * @param order
     * @return
     */
    private Message geneMessage(OrderStatusChangeEvent event,Order order){
        return MessageBuilder.withPayload(event).setHeader("order",order).build();
    }


    public Order create() {
        //创建订单对象，设置初始状态为待支付，将订单对象设置在map中
        Order order = new Order();
        order.setStatus(OrderStatus.WAIT_PAYMENT);
        order.setId(id++);
        orders.put(order.getId(), order);
        return order;
    }

    public Order pay(int id) {
        //根据订单ID取得订单对象
        Order order = orders.get(id);
        System.out.println("线程名称：" + Thread.currentThread().getName() + " 尝试支付，订单号：" + id);
        //发送支付事件
        Message message = geneMessage(OrderStatusChangeEvent.PAYED,order);
        if (!sendEvent(message, order)) {
            System.out.println("线程名称：" + Thread.currentThread().getName() + " 支付失败, 状态异常，订单号：" + id);
        }
        return orders.get(id);
    }

    public Order deliver(int id) {
        Order order = orders.get(id);
        System.out.println("线程名称：" + Thread.currentThread().getName() + " 尝试发货，订单号：" + id);
        //发送发货事件
        Message message = geneMessage(OrderStatusChangeEvent.DELIVERY,order);
        if (!sendEvent(message, orders.get(id))) {
            System.out.println("线程名称：" + Thread.currentThread().getName() + " 发货失败，状态异常，订单号：" + id);
        }
        return orders.get(id);
    }

    public Order receive(int id) {
        Order order = orders.get(id);
        System.out.println("线程名称：" + Thread.currentThread().getName() + " 尝试收货，订单号：" + id);
        //发送收货事件
        Message message = geneMessage(OrderStatusChangeEvent.RECEIVED,order);
        if (!sendEvent(message, orders.get(id))) {
            System.out.println("线程名称：" + Thread.currentThread().getName() + " 收货失败，状态异常，订单号：" + id);
        }
        return orders.get(id);
    }


    public Map<Integer, Order> getOrders() {
        return orders;
    }


    /**
     * 发送订单状态转换事件
     *
     * @param message
     * @param order
     * @return
     */
    private synchronized boolean sendEvent(Message<OrderStatusChangeEvent> message, Order order) {
        boolean result = false;
        try {
            //启动状态机
            orderStateMachine.start();
            //尝试恢复状态机状态
            persister.restore(orderStateMachine, order);
            //添加延迟用于线程安全测试
            Thread.sleep(1000);
            //发送事件
            result = orderStateMachine.sendEvent(message);
            //持久化状态机状态
            persister.persist(orderStateMachine, order);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //最终都要关闭状态机
            orderStateMachine.stop();
        }
        return result;
    }
}


