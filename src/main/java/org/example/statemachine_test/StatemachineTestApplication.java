package org.example.statemachine_test;

import lombok.extern.slf4j.Slf4j;
import org.example.statemachine_test.statemanchine.IOrderService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
@Slf4j
@SpringBootApplication
public class StatemachineTestApplication {

    public static void main(String[] args) {
        log.info("开始测试状态机");
        Thread.currentThread().setName("主线程");
        ConfigurableApplicationContext context = SpringApplication.run(StatemachineTestApplication.class,args);

        IOrderService orderService = (IOrderService)context.getBean("orderService");
        //创建两个订单
        orderService.create();
        orderService.create();

        //第一个订单进行支付
        orderService.pay(1);

        new Thread("客户线程"){
            @Override
            public void run() {
                //第一个订单发货
                orderService.deliver(1);
                //第一个订单收货
                orderService.receive(1);
            }
        }.start();

        //第二个订单支付、发货、收货∂
        orderService.pay(2);
        orderService.deliver(2);
        orderService.receive(2);

        System.out.println("全部订单状态：" + orderService.getOrders());
    }

}
