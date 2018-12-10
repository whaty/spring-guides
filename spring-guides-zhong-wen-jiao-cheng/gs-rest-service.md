# 基于spring构建RESTful Web服务

> 本教程带你用创建一个 “hello world” 级别的 RESTful Web 服务 .

## 目标

> 你将创建一个接受 HTTP GET 请求的服务：

```text
http://localhost:8080/greeting
```

> 并且该服务可以响应一个JSON格式的持久化对象 “greeting” ：

```text
{"id":1,"content":"Hello, World!"}
```

> 你可以带参数“name”请求接口：

```text
http://localhost:8080/greeting?name=User
```

> 这个name参数值会覆盖默认值“World”并且返回给响应结果：

```text
{"id":1,"content":"Hello, User!"}
```

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

    `git clone https://github.com/whaty/spring-guides.git`

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
    <artifactId>gs-rest-service</artifactId>
    <version>0.1.0</version>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.0.5.RELEASE</version>
    </parent>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.jayway.jsonpath</groupId>
            <artifactId>json-path</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <properties>
        <java.version>1.8</java.version>
    </properties>


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

## 创建持久化类

> 现在你已经建立了项目，您可以创建您的Web服务。
>
> 该服务将处理 GET 请求 `/greeting`，name 作为查询字符串参数。这个请求成功应该返回一个 `200 OK` ，响应内容为一个 JSON 格式的 `Greeting` 对象。它应该是这个样子：

```text
{
    "id": 1,
    "content": "Hello, World!"
}
```

这个`id` 是 greeting 的唯一标识, `content` 是 greeting 的文本内容

**Greeting.java**

```text
src/main/java/hello/Greeting.java

package hello;

public class Greeting {

    private final long id;
    private final String content;

    public Greeting(long id, String content) {
        this.id = id;
        this.content = content;
    }

    public long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }
}
```

> Spring 使用 Jackson JSON library 将实体类 `Greeting` 转换为 JSON

接下来创建资源控制器。

## 创建资源控制器

> 用Spring的方法来构建RESTful Web服务，HTTP请求是由 Controller 处理。这些组件是由 `@RestController` 注释识别，下面的 `GreetingController` 处理 `GET` 请求 `/greeting` ，最终返回一个 `Greeting` 类

```text
src/main/java/hello/GreetingController.java

package hello;

import java.util.concurrent.atomic.AtomicLong;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @RequestMapping("/greeting")
    public Greeting greeting(@RequestParam(value="name", defaultValue="World") String name) {
        return new Greeting(counter.incrementAndGet(),
                            String.format(template, name));
    }
}
```

这个控制器简洁而简单，但是它的内部有很多东西。让我们一步一步来分析。

> `@RequestMapping` 注释确保对 `/greeting` 的HTTP请求映射到 `greeting()` 方法。
>
> 上面的示例没有指定 `GET、PUT、POST` 等等，因为 @`RequestMapping` 默认映射所有HTTP操作。使用 `@RequestMapping(method=GET)` 缩小映射范围。
>
> `@RequestParam` 将查询字符串参数名的值绑定到 `greeting()` 方法的name参数中。如果请求中缺少 `name` 参数，则使用“World”作为默认参数值。
>
> 传统MVC控制器和上述RESTful web服务控制器之间的一个关键区别是创建HTTP响应体的方式。这种 RESTful web服务控制器并不依赖于视图技术来将问候数据在服务器端呈现为HTML，而是简单地填充并返回一个 `Greeting` 对象。对象数据将作为JSON直接写入HTTP响应。
>
> 这段代码使用了 Spring 4 的新 `@RestController` 注释，它将类标记为控制器，其中每个方法返回一个域对象，而不是视图。它是`@Controller` 和 `@ResponseBody` 的合并写法。
>
> `Greeting` 对象自动转换为 JSON。由于 Spring 对 HTTP 消息转换器的支持，您不需要手动进行此转换。因为 `Jackson2` 在类路径上，所以会自动选择 Spring 的 `MappingJackson2HttpMessageConverter` 将 `Greeting` 实例转换为 JSON。

## 让运用程序可执行

> 尽管可以将此服务打包为部署到外部应用服务器的传统WAR文件，但是下面演示的更简单的方法将创建一个独立的应用程序。您可以将所有内容打包到一个单独的、可执行的JAR文件中，该文件由一个很好的老Java `main()` 方法驱动。在此过程中，您将使用Spring内嵌的Tomcat servlet容器为HTTP运行时的支持，而不是部署到外部实例。

```text
src/main/java/hello/Application.java

package hello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

@SpringBootApplication是一个方便的注释，它添加了以下所有内容：

* `@Configuration` 将类标记为应用程序上下文bean定义的源。
* `@EnableAutoConfiguration` 告诉Spring Boot开始基于类路径设置、其他bean和各种属性设置添加bean。通常情况下，您会为Spring MVC应用程序添加 `@EnableWebMvc`，但是当在类路径中看到Spring-webmvc时，Spring Boot会自动添加@EnableWebMvc。这将应用程序标记为web应用程序，并激活关键行为，如设置 `DispatcherServlet`。
* `@ComponentScan` 告诉Spring在 `hello` 包中查找其他组件、配置和服务，从而允许它查找控制器。

## 建立可执行 jar

> 您可以使用Gradle或Maven从命令行运行应用程序。或者您可以构建一个包含所有必需依赖项、类和资源的可执行JAR文件，并运行它。这使得在整个开发生命周期中、在不同的环境中，以应用程序的形式发布、版本化和部署服务变得很容易。

您可以使用 spring-boot:run 运行应用程序。或者您可以使用 clean package 构建JAR文件。然后您可以运行JAR文件:

```text
java -jar target/gs-rest-service-0.1.0.jar
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

> 现在访问服务 [http://localhost:8080/greeting](http://localhost:8080/greeting) ，你将会看到：

```text
{"id":1,"content":"Hello, World!"}
```

> 使用 [http://localhost:8080/greeting?name=User](http://localhost:8080/greeting?name=User) 提供名称查询字符串参数。注意内容属性的值如何从“Hello, World!”变为“Hello, User!”

```text
{"id":2,"content":"Hello, User!"}
```

这个变化说明了 `@RequestParam` 在 `GreetingController` 中的工作方式是按照预期工作的。name参数的默认值是“World”，但是始终可以通过查询字符串显式地覆盖它。

还要注意id属性如何从1更改为2。这证明您正在跨多个请求处理相同的 `GreetingController` 实例，并且它的计数器字段在每次调用时都按预期增加。

## 总结

恭喜你!您刚刚使用Spring开发了一个 RESTful web 服务。

原文链接：[Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)

