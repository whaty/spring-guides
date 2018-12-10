# 基于spring构建调度服务

> 本教程带你用创建一个任务调度服务 .

## 目标

> 您将构建一个应用程序，该应用程序使用 Spring 的 `@schedule` 注释每五秒钟打印一次当前时间。


## 准备工作

* 大约15分钟
* 一个最喜欢的文本编辑器或IDE
* JDK 1.8 或 更高版本
* gradle 4 或 Maven 3.2
* 你还可以导入代码直接进入你的IDE：
  * Spring Tool Suite \(STS\)
  * IntelliJ IDEA

## 如何完成该指南

> 和其他 Spring 入门指南一样， 你可以跟着教程一步步完成操作，你也可以跳过下面的基本教程。 通过其他方法获取本教程所有代码。

* 跟着教程一步步学习，教程以 Maven 为示例
* 跳过基本教程，通过以下方式获取源码：
  * 通过以下地址下载并解压本教程源代码，或者用Git克隆一份代码到本地：

    `git clone https://github.com/whaty/spring-guides/gs-scheduling-tasks.git`

  * 将导出的项目导入到开发工具即可

## Maven构建项目

> * 创建一个普通 Maven 项目
> * 按照以下目录结构创建目录

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
    <artifactId>gs-scheduling-tasks</artifactId>
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

> Spring Boot Maven 插件提供了很多方面的特性:

* 该插件收集 classpath 下所有的 jar 包，最终打包成一个执行简单、传递方便的 jar 包。
* 该插件收集查找 `public static void main()` 方法，标识出执行项目的类。
* 该插件内置依赖解决方案，可以根据 spring boot 版本 自动选择依赖 jar 的版本。你可以覆盖默认提供的版本，但是默认情况下，系统自动选择依赖版本。

## 创建调度任务

> 现在你已经建立了项目，您可以创建一个调度任务。

    src/main/java/hello/ScheduledTasks.java
    

```text
package hello;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Scheduled(fixedRate = 5000)
    public void reportCurrentTime() {
        log.info("The time is now {}", dateFormat.format(new Date()));
    }
}
```

> `Scheduled` 注释定义了特定方法运行的时间。注意:本例使用 `fixedRate`，它指定从每次调用开始时测量的方法调用之间的间隔。还有其他选项，比如 `fixedDelay`，它指定从任务完成到调用之间的间隔。您还可以使用 `@Scheduled(cron="…")` 表达式进行更复杂的任务调度。

**赋予调度能力**

> 虽然调度任务可以嵌入到web应用程序和WAR文件中，但是下面演示的更简单的方法创建了一个独立的应用程序。您可以将所有内容打包到一个单独的、可执行的JAR文件中，该文件由一个很好的老Java main()方法驱动。

    src/main/java/hello/Application.java

```text
package hello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }
}
```

`@SpringBootApplication` 是一个方便的注释，它添加了以下所有内容：

* `@Configuration` 将类标记为应用程序上下文bean定义的源。
* `@EnableAutoConfiguration` 告诉Spring Boot开始基于类路径设置、其他bean和各种属性设置添加bean。通常情况下，您会为Spring MVC应用程序添加 `@EnableWebMvc`，但是当在类路径中看到Spring-webmvc时，Spring Boot会自动添加@EnableWebMvc。这将应用程序标记为web应用程序，并激活关键行为，如设置 `DispatcherServlet`。
* `@ComponentScan` 告诉Spring在 `hello` 包中查找其他组件、配置和服务，从而允许它查找控制器。

`@EnableScheduling` 确保创建后台任务执行器。没有它，就无法创建计划。

## 建立可执行 jar

> 您可以使用Gradle或Maven从命令行运行应用程序。或者您可以构建一个包含所有必需依赖项、类和资源的可执行JAR文件，并运行它。这使得在整个开发生命周期中、在不同的环境中，以应用程序的形式发布、版本化和部署服务变得很容易。

您可以使用 spring-boot:run 运行应用程序。或者您可以使用 clean package 构建JAR文件。然后您可以运行JAR文件:

```text
java -jar target/gs-scheduling-tasks-0.1.0.jar
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

## 测试服务
启动项目，控制台每五秒打印一条当前时间：

```text
2018-12-10 15:22:55.501  INFO 13396 --- [pool-1-thread-1] hello.ScheduledTasks                     : The time is now 15:22:55
2018-12-10 15:23:00.500  INFO 13396 --- [pool-1-thread-1] hello.ScheduledTasks                     : The time is now 15:23:00
2018-12-10 15:23:05.500  INFO 13396 --- [pool-1-thread-1] hello.ScheduledTasks                     : The time is now 15:23:05
2018-12-10 15:23:10.500  INFO 13396 --- [pool-1-thread-1] hello.ScheduledTasks                     : The time is now 15:23:10
```

## 总结

恭喜你!您刚刚使用Spring开发了一个 任务调度 服务。

原文链接：[Scheduling Tasks](https://spring.io/guides/gs/scheduling-tasks/)

