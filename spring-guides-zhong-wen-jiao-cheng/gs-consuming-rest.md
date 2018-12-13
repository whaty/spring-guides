# 基于spring构建消费web RESTful 的服务

> 本教程带你创建一个消费 web RESTful 服务的应用程序 .

## 目标

> 您将构建一个应用程序，该应用程序使用Spring的 `RestTemplate` 在 http://gturnquist-quoters.cfapps.io/api/random 检索一个随机的 Spring 引用。


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
    <artifactId>gs-consuming-rest</artifactId>
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
            <groupId>org.springframework</groupId>
            <artifactId>spring-web</artifactId>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
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

## 获取REST服务


> 项目设置完成后，您可以创建一个消费RESTful服务的简单应用程序。


> 在 http://gturnquist-quoters.cfapps.io/api/random 上已经建立了一个 RESTful 服务。它随机获取有关Spring Boot的引用，并将其作为JSON文档返回。


>如果您通过web浏览器或curl请求该URL，您将收到一个JSON文档，它看起来是这样的:

```text
{
   type: "success",
   value: {
      id: 10,
      quote: "Really loving Spring Boot, makes stand alone Spring apps easy."
   }
}
```


> 非常简单，但是在通过浏览器或curl获取时用处不大。


> 消费 REST web 服务的更有用的方法是通过编程。为了帮助您完成这项任务，Spring提供了一个名为 `RestTemplate` 的方便模板类。`RestTemplate` 使与大多数 RESTful 服务的交互编的简单。它甚至可以将数据绑定到自定义域类型。

首先，创建一个类来包含您需要的数据。




    src/main/java/hello/Quote.java
    

```text
package hello;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Quote {

    private String type;
    private Value value;

    public Quote() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Quote{" +
                "type='" + type + '\'' +
                ", value=" + value +
                '}';
    }
}
```

如您所见，这是一个简单的Java类，具有一些属性和匹配的getter方法。它是由Jackson JSON处理库中的`@JsonIgnoreProperties` 注释的，以表明任何未绑定在该类型中的属性都应该被忽略。

为了将数据直接绑定到定制类型，需要指定与API返回的JSON文档中的键完全相同的变量名。如果JSON文档中的变量名和键不匹配，则需要使用 `@JsonProperty` 注释指定JSON文档的确切键。

需要一个额外的类来嵌入内部引用本身。

	src/main/java/hello/Value.java
 
 ```text
 package hello;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Value {

    private Long id;
    private String quote;

    public Value() {
    }

    public Long getId() {
        return this.id;
    }

    public String getQuote() {
        return this.quote;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    @Override
    public String toString() {
        return "Value{" +
                "id=" + id +
                ", quote='" + quote + '\'' +
                '}';
    }
}
```

> 它使用相同的注释，但只是映射到其他数据字段。


## 执行应用程序


> 虽然调度任务可以嵌入到web应用程序和WAR文件中，但是下面演示的更简单的方法创建了一个独立的应用程序。您可以将所有内容打包到一个单独的、可执行的JAR文件中，该文件由一个很好的老Java main()方法驱动。

现在您可以编写使用 `RestTemplate` 从 Spring Boot 标语库获取数据。

    src/main/java/hello/Application.java

```text
package hello;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.client.RestTemplate;

public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String args[]) {
        RestTemplate restTemplate = new RestTemplate();
        Quote quote = restTemplate.getForObject("http://gturnquist-quoters.cfapps.io/api/random", Quote.class);
        log.info(quote.toString());
    }

}
```
> 由于Jackson JSON处理库位于类路径中，`RestTemplate` 将使用它(通过消息转换器)将传入的JSON数据转换为  `Quote` 对象。`Quote` 对象的内容将被记录到控制台。

> 在这里，您只使用 `RestTemplate` 发出HTTP GET请求。但是RestTemplate还支持其他HTTP请求，如POST、PUT和DELETE。

## 使用 Spring Boot 管理应用程序生命周期

> 到目前为止，我们还没有在我们的应用程序中使用Spring Boot，但是这样做有一些优点，而且并不难做到。其中一个优点是，我们可能希望让Spring Boot管理 `RestTemplate` 中的消息转换器，这样可以方便地以声明方式添加定制。为此，我们在主类上使用 `@SpringBootApplication` 并转换主方法来启动它，就像在任何Spring Boot应用程序中一样。最后，我们将 `RestTemplate` 移动到 `CommandLineRunner` 回调，以便在启动时由Spring Boot执行:

	src/main/java/hello/Application.java

```text
package hello;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class Application {

	private static final Logger log = LoggerFactory.getLogger(Application.class);

	public static void main(String args[]) {
		SpringApplication.run(Application.class);
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}

	@Bean
	public CommandLineRunner run(RestTemplate restTemplate) throws Exception {
		return args -> {
			Quote quote = restTemplate.getForObject(
					"http://gturnquist-quoters.cfapps.io/api/random", Quote.class);
			log.info(quote.toString());
		};
	}
}
```
> `RestTemplateBuilder` 是由Spring注入的，如果您使用它来创建 `RestTemplate`，Spring Boot中发生的所有自动配置都可以使用消息转换器。我们还将 `RestTemplate` 提取到 `@Bean` 中，以使它更容易测试(通过这种方式可以更容易地模拟它)。

`@SpringBootApplication` 是一个方便的注释，它添加了以下所有内容：

* `@Configuration` 将类标记为应用程序上下文bean定义的源。
* `@EnableAutoConfiguration` 告诉Spring Boot开始基于类路径设置、其他bean和各种属性设置添加bean。通常情况下，您会为Spring MVC应用程序添加 `@EnableWebMvc`，但是当在类路径中看到Spring-webmvc时，Spring Boot会自动添加@EnableWebMvc。这将应用程序标记为web应用程序，并激活关键行为，如设置 `DispatcherServlet`。
* `@ComponentScan` 告诉Spring在 `hello` 包中查找其他组件、配置和服务，从而允许它查找控制器。

`@EnableScheduling` 确保创建后台任务执行器。没有它，就无法创建计划。

## 建立可执行 jar

> 您可以使用Gradle或Maven从命令行运行应用程序。或者您可以构建一个包含所有必需依赖项、类和资源的可执行JAR文件，并运行它。这使得在整个开发生命周期中、在不同的环境中，以应用程序的形式发布、版本化和部署服务变得很容易。

您可以使用 spring-boot:run 运行应用程序。或者您可以使用 clean package 构建JAR文件。然后您可以运行JAR文件:

```text
java -jar target/gs-consuming-rest-0.1.0.jar
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
启动项目，您应该会看到如下输出，带有一个随机引用:

```text
2018-12-13 17:30:52.789  INFO 24508 --- [           main] hello.Application                        : Quote{type='success', value=Value{id=4, quote='Previous to Spring Boot, I remember XML hell, confusing set up, and many hours of frustration.'}}
```

## 总结

恭喜你!您刚刚使用Spring开发了一个 简单的REST客户端 服务。

原文链接：[Consuming a RESTful Web Service](https://spring.io/guides/gs/consuming-rest/)