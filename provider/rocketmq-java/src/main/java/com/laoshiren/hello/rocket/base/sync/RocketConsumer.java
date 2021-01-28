package com.laoshiren.hello.rocket.base.sync;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * ProjectName:     hello-rocketmq
 * Package:         com.laoshiren.hello.rocket.base.sync
 * ClassName:       RocketConsumer
 * Author:          laoshiren
 * Git:             xiangdehua@pharmakeyring.com
 * Description:
 * Date:            2021/1/28 11:12
 * Version:         1.0.0
 */
public class RocketConsumer {

    @SneakyThrows
    public static void main(String[] args) {
        // 创建消息消费者
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(UUID.randomUUID().toString());
        /*
         * 定义服务器地址
         */
        consumer.setNamesrvAddr("120.79.0.210:9876");
        /*
         * 消费者从最早的可消费的信息开始
         */
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        /*
         * 订阅topic  subExpression 表示消费哪些tag 的消息
         */
        consumer.subscribe("topic", "*");
        consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                Map<String,Object> map = new HashMap<>();
                map.put("msgs",msgs);
                map.put("context",context);
                System.err.println(objectMapper.writeValueAsString(map));
                /*
                    {
                    	"msgs": [{
                    		"topic": "topic",
                    		"flag": 0,
                    		"properties": {
                    			"MIN_OFFSET": "0",
                    			"MAX_OFFSET": "2",
                    			"CONSUME_START_TIME": "1611811231036",
                    			"UNIQ_KEY": "C0A8087D04B400B4AAC2841A30610000",
                    			"WAIT": "true",
                    			"TAGS": "tag"
                    		},
                    		"body": "aGVsbG8td29ybGQgLSBqYXZhIC0gc3luYw==",
                    		"transactionId": null,
                    		"queueId": 1,
                    		"storeSize": 182,
                    		"queueOffset": 0,
                    		"sysFlag": 0,
                    		"bornTimestamp": 1611646708834,
                    		"bornHost": "218.81.8.90:54471",
                    		"storeTimestamp": 1611646708905,
                    		"msgId": "C0A8087D04B400B4AAC2841A30610000",
                    		"commitLogOffset": 364,
                    		"bodyCRC": 317426201,
                    		"reconsumeTimes": 0,
                    		"preparedTransactionOffset": 0,
                    		"offsetMsgId": "784F00D200002A9F000000000000016C",
                    		"bornHostBytes": "2lEIWgAA1Mc=",
                    		"storeHostBytes": "eE8A0gAAKp8=",
                    		"keys": null,
                    		"waitStoreMsgOK": true,
                    		"buyerId": null,
                    		"delayTimeLevel": 0,
                    		"tags": "tag"
                    	}],
                    	"context": {
                    		"messageQueue": {
                    			"topic": "topic",
                    			"brokerName": "broker-a",
                    			"queueId": 1
                    		},
                    		"delayLevelWhenNextConsume": 0,
                    		"ackIndex": 2147483647
                    	}
                    }
                 */
            }catch ( JsonProcessingException e) {
                e.printStackTrace();
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
        /*
         *  开启消费者
         */
        consumer.start();
    }

}
