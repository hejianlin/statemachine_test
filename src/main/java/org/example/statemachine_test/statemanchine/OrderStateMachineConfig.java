package org.example.statemachine_test.statemanchine;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.*;
import org.springframework.statemachine.persist.DefaultStateMachinePersister;
import org.springframework.statemachine.support.DefaultStateMachineContext;

import java.util.EnumSet;

/**
 * 这里是状态机的配置，继承了spring的StateMachineConfigurerAdapter类，这个类接受两个泛型，在这里第一个就是状态枚举，第二个就是状态变化事件枚举
 * 订单状态机配置
 */
@Slf4j
@Configuration
@EnableStateMachine(name = "orderStateMachine")
public class OrderStateMachineConfig extends StateMachineConfigurerAdapter<OrderStatus, OrderStatusChangeEvent> {


    /**
     * 配置状态
     * @param states
     * @throws Exception
     */
    @Override
    public void configure(StateMachineStateConfigurer<OrderStatus, OrderStatusChangeEvent> states) throws Exception {
        EnumSet<OrderStatus> orderStatuses = EnumSet.allOf(OrderStatus.class);
        log.info("状态枚举的集合：" + orderStatuses);
        //在这里是初始化状态机，包括设置初始状态，设置状态机的所有状态集合
        states.withStates().initial(OrderStatus.WAIT_DELIVER).states(orderStatuses);
    }


    /**
     * 配置状态和事件的对应关系
     * @param transitions
     * @throws Exception
     */
    @Override
    public void configure(StateMachineTransitionConfigurer<OrderStatus, OrderStatusChangeEvent> transitions) throws Exception {
        transitions
                //外部流转
                .withExternal()
                //待支付流转到待发货, 这中间涉及到的事件是支付
                .source(OrderStatus.WAIT_PAYMENT).target(OrderStatus.WAIT_DELIVER).event(OrderStatusChangeEvent.PAYED)
                .and()
                .withExternal()
                //待发货流转到待收货, 这中间涉及到的事件是发货
                .source(OrderStatus.WAIT_DELIVER).target(OrderStatus.WAIT_RECEIVE).event(OrderStatusChangeEvent.DELIVERY)
                .and()
                .withExternal()
                //待收货流转到订单结束，这中间涉及到的事件是确认收货
                .source(OrderStatus.WAIT_RECEIVE).target(OrderStatus.FINISH).event(OrderStatusChangeEvent.RECEIVED);
    }


    /**
     * 持久化配置
     * @return
     */
    @Bean
    public DefaultStateMachinePersister persister(){
        return new DefaultStateMachinePersister(new StateMachinePersist<OrderStatus,OrderStatusChangeEvent,Order>() {

            @Override
            public void write(StateMachineContext<OrderStatus, OrderStatusChangeEvent> stateMachineContext, Order order) throws Exception {
                //这里并没有做持久化操作
                log.info("持久化：当前读取的订单{},对应的状态：{}",order.getId(),order.getStatus());
            }

            @Override
            public StateMachineContext<OrderStatus, OrderStatusChangeEvent>  read(Order order) throws Exception {
                log.info("当前读取的订单{},对应的状态：{}",order.getId(),order.getStatus());
                //这里直接取Order中的状态，其实并没有进行持久的读取操作
                return new DefaultStateMachineContext(order.getStatus(),null,null,null);
            }
        });
    }

}


