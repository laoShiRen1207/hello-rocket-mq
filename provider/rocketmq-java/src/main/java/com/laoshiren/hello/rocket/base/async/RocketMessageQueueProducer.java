package com.laoshiren.hello.rocket.base.async;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * ProjectName:     hello-rocketmq
 * Package:         com.laoshiren.hello.rocket.sync
 * ClassName:       RocketMessageQueueProducer
 * Author:          laoshiren
 * Git:             xiangdehua@pharmakeyring.com
 * Description:
 * Date:            2021/1/26 15:21
 * Version:         1.0.0
 */
public class RocketMessageQueueProducer {

    static final CountDownLatch countDownLatch = new CountDownLatch(3);

    @SneakyThrows
    public static void main(String[] args) {
        DefaultMQProducer producer = new DefaultMQProducer(UUID.randomUUID().toString());
        producer.setNamesrvAddr("120.79.0.210:9876");
        producer.start();
        // 在异步模式下声明发送失败之前在内部执行的最大重试次数
        producer.setRetryTimesWhenSendAsyncFailed(0);
        for (int i = 0; i <3 ; i++) {
            Message message = new Message("topic",
                    "tag-async",
                    (i+"hello-world - java - async").getBytes(RemotingHelper.DEFAULT_CHARSET));
            /*
                异步发送消息给代理。
                此方法立即返回。 发送完成后，将执行sendCallback。
             */
            producer.send(message, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    // 发送成功 回调函数
                    try {
                        System.out.printf("%s %n",new ObjectMapper().writeValueAsString(sendResult));
                    } catch (JsonProcessingException ignore) { }
                    countDownLatch.countDown();
                }
                @Override
                public void onException(Throwable e) {
                    countDownLatch.countDown();
                    e.printStackTrace();
                }
            });
        }
        countDownLatch.await(3, TimeUnit.SECONDS);
        producer.shutdown();
    }

}
