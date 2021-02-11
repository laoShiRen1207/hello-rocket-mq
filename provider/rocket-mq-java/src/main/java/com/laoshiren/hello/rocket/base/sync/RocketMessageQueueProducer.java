package com.laoshiren.hello.rocket.base.sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;

import java.util.UUID;

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

    @SneakyThrows
    public static void main(String[] args) {
        // 实例化生产者
        DefaultMQProducer producer = new DefaultMQProducer(UUID.randomUUID().toString());
        /*
        *   定义服务器地址
        *   集群模式 producer.setNamesrvAddr("120.79.0.210:9876;120.79.0.210:9877");
        * */
        producer.setNamesrvAddr("120.79.0.210:9876");
        producer.start();

        ObjectMapper objectMapper = new ObjectMapper();
        /*
        *   创建消息 指定 topic tag 和message
        *   tags 对于消息的快速过滤
        * */
        Message message = new Message("topic","tag2","hello-world - java - sync".getBytes(RemotingHelper.DEFAULT_CHARSET));
        System.err.println(objectMapper.writeValueAsString(message));
        /*
        {
        	"topic": "topic",
        	"flag": 0,
        	"properties": {
        		"WAIT": "true",
        		"TAGS": "tag"
        	},
        	"body": "aGVsbG8td29ybGQgLSBqYXZhIC0gc3luYw==",
        	"transactionId": null,
        	"tags": "tag",
        	"keys": null,
        	"waitStoreMsgOK": true,
        	"delayTimeLevel": 0,
        	"buyerId": null
        }
         */
        // 发送到broker上
        SendResult send = producer.send(message);
        System.err.println(objectMapper.writeValueAsString(send));
        /*
        {
            "sendStatus": "SEND_OK",
            "msgId": "C0A8087D156800B4AAC28D6C8FE30000",
            "messageQueue": {
                "topic": "topic",
                "brokerName": "broker-a",
                "queueId": 0
            },
            "queueOffset": 1,
            "transactionId": null,
            "offsetMsgId": "784F00D200002A9F00000000000002D8",
            "regionId": "DefaultRegion",
            "traceOn": true
        }
         */
        // 关闭 生产者
        producer.shutdown();
    }

}
