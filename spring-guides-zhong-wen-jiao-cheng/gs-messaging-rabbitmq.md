# 使用 RabbitMQ 发布和订阅消息

本指南将指导您如何配置 RabbitMQ AMQP 服务，来发布和订阅消息。

## 目标

您将构建一个应用程序，该应用程序使用 Spring AMQP的 `RabbitTemplate` 发布消息，并使用 `MessageListenerAdapter` 订阅  [POJO](https://spring.io/understanding/POJO) 上的消息。


## 准备工作

* 大约15分钟
* 一个最喜欢的文本编辑器或IDE
* JDK 1.8 或 更高版本
* gradle 4 或 Maven 3.2
* 你还可以导入代码直接进入你的IDE：
  * Spring Tool Suite \(STS\)
  * IntelliJ IDEA

## 如何完成该指南

和其他 Spring 入门指南一样， 你可以跟着教程一步步完成操作，你也可以跳过下面的基本教程。 通过其他方法获取本教程所有代码。

* 跟着教程一步步学习，教程以 Maven 为示例
* 跳过基本教程，通过以下方式获取源码：
* 通过以下地址下载并解压本教程源代码，或者用Git克隆一份代码到本地：

    `git clone https://github.com/whaty/spring-guides.git`

  * 将导出的项目导入到开发工具即可

## Maven构建项目

* 创建一个普通 Maven 项目
* 按照以下目录结构创建目录

```text
└── src
    └── main
        └── java
            └── hello
```

**pom.xml**文件如下

```text
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.springframework</groupId>
    <artifactId>gs-messaging-rabbitmq</artifactId>
    <version>0.1.0</version>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.0.5.RELEASE</version>
    </parent>

    <properties>
        <java.version>1.8</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-amqp</artifactId>
        </dependency>
    </dependencies>


    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>
```

Spring Boot Maven 插件提供了很多方面的特性:

* 该插件收集 classpath 下所有的 jar 包，最终打包成一个执行简单、传递方便的 jar 包。
* 该插件收集查找 `public static void main()` 方法，标识出执行项目的类。
* 该插件内置依赖解决方案，可以根据 spring boot 版本 自动选择依赖 jar 的版本。你可以覆盖默认提供的版本，但是默认情况下，系统自动选择依赖版本。

## 配置 RabbitMQ broker

在构建消息传递应用程序之前，需要设置处理接收和发送消息的服务器。

RabbitMQ  是 AMQP 服务的一种。该服务器可从 http://www.rabbitmq.com/download.html 免费获得。你可以手动下载，也可以用Mac电脑自带的命令下载:
 
`brew install rabbitmq`

一旦你解压 RabbitMQ，你可以用默认设置启动它。

`rabbitmq-server`

你将看到如下信息：

```text
            RabbitMQ 3.1.3. Copyright (C) 2007-2013 VMware, Inc.
##  ##      Licensed under the MPL.  See http://www.rabbitmq.com/
##  ##
##########  Logs: /usr/local/var/log/rabbitmq/rabbit@localhost.log
######  ##        /usr/local/var/log/rabbitmq/rabbit@localhost-sasl.log
##########
            Starting broker... completed with 6 plugins.
  
```

如果 Docker 在本地运行，还可以使用 [Docker Compose](https://docs.docker.com/compose/) 快速启动 RabbitMQ 服务器。这里有一个 `docker-compose.yml` 。它很简单:

`docker-compose.ymml`

```text
rabbitmq:
  image: rabbitmq:management
  ports:
    - "5672:5672"
    - "15672:15672"
    
```

使用这个文件，您可以运行 `docker-compose up` 来让 RabbitMQ 在容器中运行。



## 创建 RabbitMQ 消息接收器

对于任何基于消息的应用程序，您都需要创建一个接收方来响应发布的消息。

`src/main/java/hello/Receiver.java`

```text
package hello;

import java.util.concurrent.CountDownLatch;
import org.springframework.stereotype.Component;

@Component
public class Receiver {

    private CountDownLatch latch = new CountDownLatch(1);

    public void receiveMessage(String message) {
        System.out.println("Received <" + message + ">");
        latch.countDown();
    }

    public CountDownLatch getLatch() {
        return latch;
    }

}
```

`Receiver` 是一个简单的POJO，它定义了一个接收消息的方法。当您注册它以接收消息时，您可以将其命名为任何您想要的名称。

> 为了方便起见，这款POJO还有一个 `CountDownLatch`。这允许它发出接收到消息的信号。这是您不太可能在生产应用程序中实现的。(后半句真不知道怎么翻译好)

## 注册监听并发送消息

Spring AMQP 的 `RabbitTemplate` 提供了使用 RabbitMQ 发送和接收消息所需的所有组件。具体来说，您需要配置:
*  消息侦听器容器
*  声明队列、交换和它们之间的绑定
*  发送一些消息以测试侦听器的组件

Spring引导自动创建一个连接工厂和一个RabbitTemplate，减少了必须编写的代码量。


您将使用 RabbitTemplate 发送消息，并注册一个使用消息监听容器接受消息的 `Receiver `。连接工厂驱动这两者，允许它们连接到 RabbitMQ 服务器。


`src/main/java/hello/Application.java`

```text
package hello;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

    static final String topicExchangeName = "spring-boot-exchange";

    static final String queueName = "spring-boot";

    @Bean
    Queue queue() {
        return new Queue(queueName, false);
    }

    @Bean
    TopicExchange exchange() {
        return new TopicExchange(topicExchangeName);
    }

    @Bean
    Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("foo.bar.#");
    }

    @Bean
    SimpleMessageListenerContainer container(ConnectionFactory connectionFactory,
            MessageListenerAdapter listenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(queueName);
        container.setMessageListener(listenerAdapter);
        return container;
    }

    @Bean
    MessageListenerAdapter listenerAdapter(Receiver receiver) {
        return new MessageListenerAdapter(receiver, "receiveMessage");
    }

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(Application.class, args).close();
    }

}
```

`listenerAdapter` 方法中定义的 bean ,注册为在 `container()` 中定义的消息侦听器容器中的消息侦听器，并将侦听 “spring-boot” 队列上的消息。因为 `Receiver` 类是POJO，它需要封装在 `MessageListenerAdapter` 中，在这里指定它来调用 `receiveMessage` 。

> JMS 队列和 AMQP 队列具有不同的语义。例如，JMS 只向一个使用者发送排队消息。当 AMQP 队列做同样的事情时，AMQP生产者不会直接向队列发送消息。相反，消息被发送到交换器，交换器可以转到单个队列，也可以转到多个队列，模拟 JMS 主题的概念。有关更多信息，请参见 [Understanding AMQP](https://spring.io/understanding/AMQP)  。

您只需要消息侦听器容器和接收方bean来侦听消息。要发送消息，还需要一个 Rabbit template。

`queue()` 方法的作用是:创建一个AMQP队列。`exchange()` 方法创建一个主题交换。`binding()` 方法将这两者绑定在一起，定义了当 RabbitTemplate 发布到 exchange 时发生的行为。

> Spring AMQP 要求将 Queue、TopicExchange 和绑定声明为顶级 Spring bean，以便正确设置。

在本例中，我们使用主题交换，队列与路由键 `foo.bar.#` 绑定。这意味着任何以 `foo.bar.` 开头的路由键发送的消息。将被路由到队列。

## 发送测试信息

测试消息由“CommandLineRunner”发送，它还等待 receiver 中的锁存器并关闭应用程序上下文:

`src/main/java/hello/Runner.java`

```text
package hello;

import java.util.concurrent.TimeUnit;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class Runner implements CommandLineRunner {

    private final RabbitTemplate rabbitTemplate;
    private final Receiver receiver;

    public Runner(Receiver receiver, RabbitTemplate rabbitTemplate) {
        this.receiver = receiver;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Sending message...");
        rabbitTemplate.convertAndSend(Application.topicExchangeName, "foo.bar.baz", "Hello from RabbitMQ!");
        receiver.getLatch().await(10000, TimeUnit.MILLISECONDS);
    }

}
```

注意，模板使用 `foo.bar.baz` 的路由键将消息路由到匹配绑定的 exchange。

runner  可以在测试中模拟出来，这样 receiver 可以单独测试。

## 执行程序

`main()` 方法通过创建 Spring 应用程序上下文启动该过程。这将启动消息侦听器容器，该容器将开始侦听消息。然后会自动执行一个 `Runner` bean:它从应用程序上下文中检索 `RabbitTemplate`，并在“spring-boot”队列上发送“Hello from RabbitMQ!”最后，它关闭Spring应用程序上下文并结束应用程序。

## 构建可执行 jar

您可以使用Gradle或Maven从命令行运行应用程序。或者您可以构建一个包含所有必需依赖项、类和资源的可执行JAR文件，并运行它。这使得在整个开发生命周期中、在不同的环境中，以应用程序的形式发布、版本化和部署服务变得很容易。

您可以使用 spring-boot:run 运行应用程序。或者您可以使用 clean package 构建JAR文件。然后您可以运行JAR文件:

```text
java -jar target/gs-messaging-rabbitmq-0.1.0.jar
```

> 上面的过程将创建一个可运行的JAR。您也可以选择构建一个经典的WAR文件。
>
> 要构建既可执行又可部署到外部容器中的war文件，需要将嵌入式容器依赖项标记为“provided”，如下面的示例所示:

```text
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <!-- ... -->
    <packaging>war</packaging>
    <!-- ... -->
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-tomcat</artifactId>
            <scope>provided</scope>
        </dependency>
        <!-- ... -->
    </dependencies>
</project>
```

启动项目，你将看到如下消息：

```text
 Sending message...
 Received <Hello from RabbitMQ!>
 
```



## 总结

恭喜你!您刚刚使用 Spring 和 RabbitMQ 开发了一个简单的发布和订阅应用程序。对于 Spring 和 RabbitMQ，[您可以做的比这里介绍的更多](https://docs.spring.io/spring-amqp/reference/html/_introduction.html#quick-tour)，但这应该提供一个良好的开端。

原文链接：[Messaging with RabbitMQ](https://spring.io/guides/gs/messaging-rabbitmq/)