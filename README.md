# RocketMQ 4.5.2


## 消息队列

### 什么是消息队列

`MQ (Message Queue)`，消息队列可以理解为一种在`TCP `协议之上构建的一个 **简单的协议**，但它又不是具体的通信协议，而是更高层次的 **通信模型** 即 **生产者 / 消费者模型**，通过定义自己的生产者和消费者实现消息通信从而屏蔽复杂的底层通信协议；它为分布式应用系统提供异步解耦和削峰填谷的能力，同时也具备互联网应用所需的海量消息堆积、高吞吐、可靠重试等特性。

消息队列采用 **FIFO** 的方式，即 **先进先出** 的数据结构。

![](https://bkimg.cdn.bcebos.com/pic/8601a18b87d6277f8774fd792b381f30e924fc09?x-bce-process=image/watermark,image_d2F0ZXIvYmFpa2U2MA==,g_7,xp_5,yp_5)

### 消息队列类型

一般分为有`Broker`和没有`Broker`，然而主流的`MQ`都是有`Broker`的，通常有一台服务器作为`Broker`，所有的消息都通过它中转。生产者把消息发送给它就结束自己的任务了，`Broker`则把消息主动推送给消费者（或者消费者主动轮询）。然而此文章就简单的写一下有`Broker`的`RocketMQ`的玩法。



## 2 Deployment

[安装链接(官网 http://rocketmq.apache.org/docs/rmq-deployment/)](http://rocketmq.apache.org/docs/rmq-deployment/)

docker-compose

~~~yaml
version: '3.5'
services:
  rmqnamesrv:
    image: foxiswho/rocketmq:server
    container_name: rmqnamesrv
    ports:
      - 9876:9876
    volumes:
      - ./data/logs:/opt/logs
      - ./data/store:/opt/store
    networks:
        rmq:
          aliases:
            - rmqnamesrv

  rmqbroker:
    image: foxiswho/rocketmq:broker
    container_name: rmqbroker
    ports:
      - 10909:10909
      - 10911:10911
    volumes:
      - ./data/logs:/opt/logs
      - ./data/store:/opt/store
      - ./data/brokerconf/broker.conf:/etc/rocketmq/broker.conf
    environment:
        NAMESRV_ADDR: "rmqnamesrv:9876"
        JAVA_OPTS: " -Duser.home=/opt"
        JAVA_OPT_EXT: "-server -Xms128m -Xmx128m -Xmn128m"
    command: mqbroker -c /etc/rocketmq/broker.conf
    depends_on:
      - rmqnamesrv
    networks:
      rmq:
        aliases:
          - rmqbroker

  rmqconsole:
    image: styletang/rocketmq-console-ng
    container_name: rmqconsole
    ports:
      - 8999:8080
    environment:
        JAVA_OPTS: "-Drocketmq.namesrv.addr=rmqnamesrv:9876 -Dcom.rocketmq.sendMessageWithVIPChannel=false"
    depends_on:
      - rmqnamesrv
    networks:
      rmq:
        aliases:
          - rmqconsole

networks:
  rmq:
    name: rmq
    driver: bridge
~~~

写入配置文件

~~~shell
mkdir data
cd data 

mkdir brokerconf
cd brokerconf

vi broker.conf
~~~

~~~conf
brokerClusterName=DefaultCluster

# broker 名字，注意此处不同的配置文件填写的不一样，如果在 broker-a.properties 使用: broker-a,
# 在 broker-b.properties 使用: broker-b
brokerName=broker-a

# 0 表示 Master，> 0 表示 Slave
brokerId=0

# nameServer地址，分号分割
# namesrvAddr=rocketmq-nameserver1:9876;rocketmq-nameserver2:9876

# 启动IP,如果 docker 报 com.alibaba.rocketmq.remoting.exception.RemotingConnectException: connect to <192.168.0.120:10909> failed
# 解决方式1 加上一句 producer.setVipChannelEnabled(false);，解决方式2 brokerIP1 设置宿主机IP，不要使用docker 内部IP
brokerIP1=120.79.0.210

# 在发送消息时，自动创建服务器不存在的topic，默认创建的队列数
defaultTopicQueueNums=4

# 是否允许 Broker 自动创建 Topic，建议线下开启，线上关闭 ！！！这里仔细看是 false，false，false
autoCreateTopicEnable=true

# 是否允许 Broker 自动创建订阅组，建议线下开启，线上关闭
autoCreateSubscriptionGroup=true

# Broker 对外服务的监听端口
listenPort=10911

# 删除文件时间点，默认凌晨4点
deleteWhen=04

# 文件保留时间，默认48小时
fileReservedTime=120

# commitLog 每个文件的大小默认1G
mapedFileSizeCommitLog=1073741824

# ConsumeQueue 每个文件默认存 30W 条，根据业务情况调整
mapedFileSizeConsumeQueue=300000

# destroyMapedFileIntervalForcibly=120000
# redeleteHangedFileInterval=120000
# 检测物理文件磁盘空间
diskMaxUsedSpaceRatio=88
# 存储路径
# storePathRootDir=/home/ztztdata/rocketmq-all-4.1.0-incubating/store
# commitLog 存储路径
# storePathCommitLog=/home/ztztdata/rocketmq-all-4.1.0-incubating/store/commitlog
# 消费队列存储
# storePathConsumeQueue=/home/ztztdata/rocketmq-all-4.1.0-incubating/store/consumequeue
# 消息索引存储路径
# storePathIndex=/home/ztztdata/rocketmq-all-4.1.0-incubating/store/index
# checkpoint 文件存储路径
# storeCheckpoint=/home/ztztdata/rocketmq-all-4.1.0-incubating/store/checkpoint
# abort 文件存储路径
# abortFile=/home/ztztdata/rocketmq-all-4.1.0-incubating/store/abort
# 限制的消息大小
maxMessageSize=65536

# flushCommitLogLeastPages=4
# flushConsumeQueueLeastPages=2
# flushCommitLogThoroughInterval=10000
# flushConsumeQueueThoroughInterval=60000

# Broker 的角色
# - ASYNC_MASTER 异步复制Master
# - SYNC_MASTER 同步双写Master
# - SLAVE
brokerRole=ASYNC_MASTER

# 刷盘方式
# - ASYNC_FLUSH 异步刷盘
# - SYNC_FLUSH 同步刷盘
flushDiskType=ASYNC_FLUSH

# 发消息线程池数量
# sendMessageThreadPoolNums=128
# 拉消息线程池数量
# pullMessageThreadPoolNums=128
~~~

启动

~~~shell
docker-compose up -d
~~~

访问`ip:8999`

![](https://img-blog.csdnimg.cn/20210128104057646.png)

## 3 Development

### 3.1  java-client

#### 3.1.1 简单同步发送

~~~java
 @SneakyThrows
 public static void main(String[] args) {
     // 实例化生产者
     DefaultMQProducer producer = new DefaultMQProducer(UUID.randomUUID().toString());
     /*
     *   定义服务器地址
     *   集群模式 producer.setNamesrvAddr("ip:9876;ip:9877");
     * */
     producer.setNamesrvAddr("ip:9876");
     producer.start();
     /*
     *   创建消息 指定 topic tag 和message
     *   tags 对于消息的快速过滤
     * */
     Message message = new Message("topic","tag","hello-world - java - sync".getBytes(RemotingHelper.DEFAULT_CHARSET));
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
~~~

~~~java
@SneakyThrows
public static void main(String[] args) {
    // 创建消息消费者
    DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(UUID.randomUUID().toString());
    /*
     * 定义服务器地址
     */
    consumer.setNamesrvAddr("ip:9876");
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
~~~

#### 3.1.2 异步发送

~~~java
static final CountDownLatch countDownLatch = new CountDownLatch(3);

@SneakyThrows
public static void main(String[] args) {
    DefaultMQProducer producer = new DefaultMQProducer(UUID.randomUUID().toString());
    producer.setNamesrvAddr("ip:9876");
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
~~~





### 3.2 spring-boot