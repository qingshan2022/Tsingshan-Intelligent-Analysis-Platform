# 基于 Spring Boot + MQ + AIGC的智能数据分析平台。

目前使用科大讯飞星火大模型v3.5；

## 可以上传Excel表格和分析需求生成可视化图表

### 同步则等待AI分析结束后再右边出现结果

![](https://web-hehe-wocao.oss-cn-beijing.aliyuncs.com/bifor/image-20240405211739258.png)



### 异步分析则通过线程池/rabbitmq消息队列

两种方式实现异步：线程池/rabbitmq

介于AI接口的调用是有限的，因此需要控制用户使用系统的次数，以避免超支，比如给不同等级的用户分配不同的调用次数，防止用户过度使用系统造成破产但限制用户调用次数仍存在一定风险，用户仍有可能通过疯狂调用来刷量，从而导致系统成本过度消耗。
假设系统就一台服务器，能同时处理的用户对话数量是有限的，比如系统最多只能支持 10 个用户同时对话，如果某个用户一秒内使用 10 个账号登录，那么其他用户就无法使用系统。现在要做一个解决方案，就是限流。

因此调用 rateLimiter.trySetRate() 方法配置限流规则。

#### 一.线程池参数设置

现有条件：比如 AI 生成能力的并发是只允许 4 个任务同时去执行，AI 能力允许 20 个任务排队。 corePoolSize（核心线程数 => 正式员工数）：正常情况下，可以设置为 2 - 4  maximumPoolSize：设置为极限情况，设置为 <= 4 keepAliveTime（空闲线程存活时间）：一般设置为秒级或者分钟级 TimeUnit unit（空闲线程存活时间的单位）：分钟、秒 workQueue（工作队列）：结合实际请况去设置，可以设置为 20 threadFactory（线程工厂）：控制每个线程的生成、线程的属性（比如线程名） RejectedExecutionHandler（拒绝策略）：抛异常，标记数据库的任务状态为 “任务满了已拒绝”

#### 二.RabbitMq消息队列

具体步骤如下：
1.将任务的提交方式改为向消息队列发送消息。
2.编写一个专门用于接收消息并处理任务的程序。
3.如果程序中断，消息未被确认，消息队列将会重新发送这些消息，确保任务不会丢失。
4.现在，所有的消息都集中发送到消息队列中，你可以部署多个后端程序，它们都从同一个消息队列中获取任务，从而实现了分布式负载均衡的效果。
通过这样的改进，我们实现了一种更可靠的任务处理方式。任务不再依赖于线程池，而是通过消息队列来进行分发和处理，即使程序中断或出现故障，任务也能得到保证并得到正确处理。同时，我们还可以通过部署多个后端程序来实现负载均衡，提高系统的处理能力和可靠性。

实现步骤：
1创建交换机和队列
2将线程池中的执行代码移到消费者类中
3根据消费者的需求来确认消息的格式（chartId）
4将提交线程池改造为发送消息到队列

### 异步的结果可以直接在我的图表页面查看分析结果

![](https://web-hehe-wocao.oss-cn-beijing.aliyuncs.com/bifor/image-20240405213002747.png)

### 图片分析模块，可以实现自动生成AI对图片描述

![](https://web-hehe-wocao.oss-cn-beijing.aliyuncs.com/bifor/image-20240405213205180.png)

### 文字识别模块，可以实现自动生成AI对图片内所有文字的识别

### ![](https://web-hehe-wocao.oss-cn-beijing.aliyuncs.com/bifor/image-20240405213321933.png)

AI方面使用redisson

### 鉴权方面：

#### 用户端有鉴权功能，管理员有修改用户参数和删除用户的功能

#### ![](https://web-hehe-wocao.oss-cn-beijing.aliyuncs.com/bifor/image-20240405211529676.png)

#### 而普通用户不具备这个界面

![](https://web-hehe-wocao.oss-cn-beijing.aliyuncs.com/bifor/image-20240405213518528.png)

### 所有用户都具备修改自己信息的功能

![](https://web-hehe-wocao.oss-cn-beijing.aliyuncs.com/bifor/image-20240405213551016.png)

### 项目框架图

![](https://i0.hdslb.com/bfs/new_dyn/e5dc1d77eb9469904c5a7e9943722be533872539.png@616w_560h_1e_1c.avif)