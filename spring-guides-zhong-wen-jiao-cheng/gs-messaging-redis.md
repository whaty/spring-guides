# 使用 Redis 发布和订阅消息

本指南将指导您如何通过 Redis 使用 Spring Data Redis 发布和订阅消息。

## 目标

您将构建一个使用 `StringRedisTemplate` 发布字符串消息的应用程序，并使用 `MessageListenerAdapter` 为其提供 [POJO](https://spring.io/understanding/POJO) 订阅。

> 使用 Spring Data Redis 作为发布消息的方法可能听起来很奇怪，但是您将发现，Redis 不仅提供NoSQL 数据存储，而且还提供消息传递系统。

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
    <artifactId>gs-messaging-redis</artifactId>
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
            <artifactId>spring-boot-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
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

## 启动一个 Redis 服务

在构建消息传递应用程序之前，需要设置处理接收和发送消息的服务器。

Redis是一个开源的、bsd-licensed的键值数据存储，它还附带一个消息传递系统。该服务器可从http://redis.io/download 免费获得。你可以手动下载，也可以用自带的Mac电脑下载:
 
`brew install redis`

一旦你解压Redis，你可以用默认设置启动它。

`redis-server`

你将看到如下信息：

```text
[35142] 01 May 14:36:28.939 # Warning: no config file specified, using the default config. In order to specify a config file use redis-server /path/to/redis.conf
[35142] 01 May 14:36:28.940 * Max number of open files set to 10032
                _._
              _.-``__ ''-._
        _.-``    `.  `_.  ''-._           Redis 2.6.12 (00000000/0) 64 bit
    .-`` .-```.  ```\/    _.,_ ''-._
  (    '      ,       .-`  | `,    )     Running in stand alone mode
  |`-._`-...-` __...-.``-._|'` _.-'|     Port: 6379
  |    `-._   `._    /     _.-'    |     PID: 35142
    `-._    `-._  `-./  _.-'    _.-'
  |`-._`-._    `-.__.-'    _.-'_.-'|
  |    `-._`-._        _.-'_.-'    |           http://redis.io
    `-._    `-._`-.__.-'_.-'    _.-'
  |`-._`-._    `-.__.-'    _.-'_.-'|
  |    `-._`-._        _.-'_.-'    |
    `-._    `-._`-.__.-'_.-'    _.-'
        `-._    `-.__.-'    _.-'
            `-._        _.-'
                `-.__.-'

[35142] 01 May 14:36:28.941 # Server started, Redis version 2.6.12
[35142] 01 May 14:36:28.941 * The server is now ready to accept connections on port 6379
```

## 创建 Redis 消息接收器

在任何基于消息的应用程序中，都有消息发布者和消息接收者。要创建消息接收器，请使用响应消息的方法实现接收器:

`src/main/java/hello/Receiver.java`

```text
package hello;

import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class Receiver {
    private static final Logger LOGGER = LoggerFactory.getLogger(Receiver.class);

    private CountDownLatch latch;

    @Autowired
    public Receiver(CountDownLatch latch) {
        this.latch = latch;
    }

    public void receiveMessage(String message) {
        LOGGER.info("Received <" + message + ">");
        latch.countDown();
    }
}
```

`Receiver` 是一个简单的 POJO，它定义了一个接收消息的方法。正如您将 `Receiver` 注册为消息侦听器时将看到的，您可以任意命名消息处理方法。

> 出于演示目的，它由其构造函数注入 `CountDownLatch `。这样，它就可以在收到消息时发出信号。

## 注册监听并发送消息

Spring Data Redis 提供了使用Redis发送和接收消息所需的所有组件。具体来说，您需要配置:
*  连接工厂
*  消息侦听器容器
*  Redis template


您将使用 Redis 模板发送消息，并将 `Receiver ` 注册到消息侦听器容器中，以便它接收消息。连接工厂驱动模板和消息侦听器容器，使它们能够连接到 Redis 服务器。

本例使用 Spring Boot 的默认 `RedisConnectionFactory`，这是 `JedisConnectionFactory` 的一个实例，它基于`Jedis Redis` 库。连接工厂被注入到消息侦听器容器和Redis模板中。

`src/main/java/hello/Application.java`

```text
package hello;

import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@SpringBootApplication
public class Application {

	private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

	@Bean
	RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
			MessageListenerAdapter listenerAdapter) {

		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.addMessageListener(listenerAdapter, new PatternTopic("chat"));

		return container;
	}

	@Bean
	MessageListenerAdapter listenerAdapter(Receiver receiver) {
		return new MessageListenerAdapter(receiver, "receiveMessage");
	}

	@Bean
	Receiver receiver(CountDownLatch latch) {
		return new Receiver(latch);
	}

	@Bean
	CountDownLatch latch() {
		return new CountDownLatch(1);
	}

	@Bean
	StringRedisTemplate template(RedisConnectionFactory connectionFactory) {
		return new StringRedisTemplate(connectionFactory);
	}

	public static void main(String[] args) throws InterruptedException {

		ApplicationContext ctx = SpringApplication.run(Application.class, args);

		StringRedisTemplate template = ctx.getBean(StringRedisTemplate.class);
		CountDownLatch latch = ctx.getBean(CountDownLatch.class);

		LOGGER.info("Sending message...");
		template.convertAndSend("chat", "Hello from Redis!");

		latch.await();

		System.exit(0);
	}
}

```

`listenerAdapter` 方法中定义的 bean ,注册为在 `container` 中定义的消息侦听器容器中的消息侦听器，并将侦听 “chat” 主题上的消息。因为 `Receiver` 类是POJO，所以它需要包装在实现`addMessageListener()` 所需的 ` MessageListener` 接口的消息侦听器适配器中。消息侦听器适配器还配置来用于，在消息到达 `Receiver ` 时调用 `receiveMessage()` 方法。

您只需要连接工厂和消息侦听器容器bean来侦听消息。要发送消息，还需要一个 Redis 模板。在这里，它是一个配置为 `StringRedisTemplate` 的bean,它是 `RedisTemplate` 的一个实现，主要关注 Redis 的常用用法，并且键和值都是“String”的。


`main()` 方法通过创建一个Spring应用程序上下文来启动所有操作。然后应用程序上下文启动消息侦听器容器、消息侦听器容器bean，开始侦听消息。`main()` 方法然后从应用程序上下文中检索` StringRedisTemplate`  bean，并使用它在“chat”主题上发送“Hello from Redis!”最后，它关闭Spring应用程序上下文，应用程序结束。


## 构建可执行 jar

您可以使用Gradle或Maven从命令行运行应用程序。或者您可以构建一个包含所有必需依赖项、类和资源的可执行JAR文件，并运行它。这使得在整个开发生命周期中、在不同的环境中，以应用程序的形式发布、版本化和部署服务变得很容易。

您可以使用 spring-boot:run 运行应用程序。或者您可以使用 clean package 构建JAR文件。然后您可以运行JAR文件:

```text
java -jar target/gs-messaging-redis-0.1.0.jar
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
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (v2.0.5.RELEASE)

2018-12-27 15:40:37.165  INFO 16596 --- [           main] hello.Application                        : Starting Application on DESKTOP-E78D5K1 with PID 16596 (C:\idea_project\spring-guides\gs-messaging-redis\target\classes started by lc in C:\idea_project\spring-guides)
2018-12-27 15:40:37.169  INFO 16596 --- [           main] hello.Application                        : No active profile set, falling back to default profiles: default
2018-12-27 15:40:37.213  INFO 16596 --- [           main] s.c.a.AnnotationConfigApplicationContext : Refreshing org.springframework.context.annotation.AnnotationConfigApplicationContext@1b68b9a4: startup date [Thu Dec 27 15:40:37 CST 2018]; root of context hierarchy
2018-12-27 15:40:37.686  INFO 16596 --- [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode!
2018-12-27 15:40:38.473  INFO 16596 --- [           main] o.s.j.e.a.AnnotationMBeanExporter        : Registering beans for JMX exposure on startup
2018-12-27 15:40:38.480  INFO 16596 --- [           main] o.s.c.support.DefaultLifecycleProcessor  : Starting beans in phase 2147483647
2018-12-27 15:40:38.586  INFO 16596 --- [    container-1] io.lettuce.core.EpollProvider            : Starting without optional epoll library
2018-12-27 15:40:38.588  INFO 16596 --- [    container-1] io.lettuce.core.KqueueProvider           : Starting without optional kqueue library
2018-12-27 15:40:39.458  INFO 16596 --- [           main] hello.Application                        : Started Application in 2.598 seconds (JVM running for 3.204)
2018-12-27 15:40:39.460  INFO 16596 --- [           main] hello.Application                        : Sending message...
2018-12-27 15:40:39.475  INFO 16596 --- [    container-2] hello.Receiver                           : Received <Hello from Redis!>
2018-12-27 15:40:39.476  INFO 16596 --- [       Thread-2] s.c.a.AnnotationConfigApplicationContext : Closing org.springframework.context.annotation.AnnotationConfigApplicationContext@1b68b9a4: startup date [Thu Dec 27 15:40:37 CST 2018]; root of context hierarchy
2018-12-27 15:40:39.477  INFO 16596 --- [       Thread-2] o.s.c.support.DefaultLifecycleProcessor  : Stopping beans in phase 2147483647
2018-12-27 15:40:39.480  INFO 16596 --- [       Thread-2] o.s.j.e.a.AnnotationMBeanExporter        : Unregistering JMX-exposed beans on shutdown
```



## 总结

恭喜你!您刚刚使用 Spring 、Redis 开发了一个简单的发布和订阅应用程序。

原文链接：[Messaging with Redis](https://spring.io/guides/gs/messaging-redis/)

