# 基于 Spring 使用 JDBC 访问关系型数据

本教程带你用 Spring 访问关系型数据

## 目标

您将构建一个应用程序，该应用程序使用 Spring 的 `JdbcTemplate` 来访问存储在关系型数据库中的数据。


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
    <artifactId>gs-relational-data-access</artifactId>
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
            <artifactId>spring-boot-starter-jdbc</artifactId>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
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

## 创建一个 Customer 对象

下面将使用的简单数据访问逻辑管理客户的姓和名。要在应用程序级别表示此数据，请创建一个 `Customer` 类。

    src/main/java/hello/Customer.java
    

```text
package hello;

public class Customer {
    private long id;
    private String firstName, lastName;

    public Customer(long id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Override
    public String toString() {
        return String.format(
                "Customer[id=%d, firstName='%s', lastName='%s']",
                id, firstName, lastName);
    }

    // getters & setters omitted for brevity
}
```

##存储和检索数据
Spring提供了一个名为JdbcTemplate的模板类，它使使用SQL关系数据库和JDBC变得很容易。

其他大多数JDBC代码都陷入了资源获取、连接管理、异常处理和一般错误检查的泥潭中，而这些与代码的目的完全无关。JdbcTemplate为您处理所有这些。你所要做的就是专注于手头的工作。

`src/main/java/hello/Application.java`

```text
package hello;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
public class Application implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String args[]) {
        SpringApplication.run(Application.class, args);
    }

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... strings) throws Exception {

        log.info("Creating tables");

        jdbcTemplate.execute("DROP TABLE customers IF EXISTS");
        jdbcTemplate.execute("CREATE TABLE customers(" +
                "id SERIAL, first_name VARCHAR(255), last_name VARCHAR(255))");

        // Split up the array of whole names into an array of first/last names
        List<Object[]> splitUpNames = Arrays.asList("John Woo", "Jeff Dean", "Josh Bloch", "Josh Long").stream()
                .map(name -> name.split(" "))
                .collect(Collectors.toList());

        // Use a Java 8 stream to print out each tuple of the list
        splitUpNames.forEach(name -> log.info(String.format("Inserting customer record for %s %s", name[0], name[1])));

        // Uses JdbcTemplate's batchUpdate operation to bulk load data
        jdbcTemplate.batchUpdate("INSERT INTO customers(first_name, last_name) VALUES (?,?)", splitUpNames);

        log.info("Querying for customer records where first_name = 'Josh':");
        jdbcTemplate.query(
                "SELECT id, first_name, last_name FROM customers WHERE first_name = ?", new Object[] { "Josh" },
                (rs, rowNum) -> new Customer(rs.getLong("id"), rs.getString("first_name"), rs.getString("last_name"))
        ).forEach(customer -> log.info(customer.toString()));
    }
}
```

`@SpringBootApplication` 是一个方便的注释，它添加了以下所有内容：

* `@Configuration` 将类标记为应用程序上下文bean定义的源。
* `@EnableAutoConfiguration` 告诉Spring Boot开始基于类路径设置、其他bean和各种属性设置添加bean。通常情况下，您会为Spring MVC应用程序添加 `@EnableWebMvc`，但是当在类路径中看到Spring-webmvc时，Spring Boot会自动添加@EnableWebMvc。这将应用程序标记为web应用程序，并激活关键行为，如设置 `DispatcherServlet`。
* `@ComponentScan` 告诉Spring在 `hello` 包中查找其他组件、配置和服务，从而允许它查找控制器。

main()方法使用Spring Boot的SpringApplication.run()方法启动应用程序。您注意到没有一行XML吗?没 web.xml 文件。这个web应用程序是100%纯Java的，您不必配置任何管道或基础设施。

Spring Boot支持内存中的关系数据库引擎 **H2**，并自动创建连接。因为我们使用的是 Spring-jdbc ，所以Spring引导会自动创建一个 `JdbcTemplate`。`@Autowired JdbcTemplate` 字段自动加载它并使其可用。

这个应用程序类实现了Spring Boot的 `CommandLineRunner`，这意味着它将在加载应用程序上下文之后执行 `run()` 方法。

首先，使用 `JdbcTemplate.execute` 方法安装一些DDL。

其次，获取字符串列表并使用Java 8流，将它们在Java数组中分成姓/名对。

然后使用 `JdbcTemplate.batchUpdate`方法在新创建的表中安装一些记录。方法调用的第一个参数是查询字符串，最后一个参数(对象s的数组)保存要替换到查询中的变量，其中“?””字符。

>对于单个insert语句，`JdbcTemplate.insert` 方法很好。但是对于多个插入，最好使用 `batchUpdate`。

>使用`?`用于指示JDBC绑定变量以避免SQL注入攻击的参数。

最后，使用 `query` 方法搜索表，查找与条件匹配的记录。你又用了"?"参数为查询创建参数，在调用时传入实际值。最后一个参数是Java 8 lambda，用于将每个结果行转换为新的 `Customer` 对象。

>Java 8 lambdas很好地映射到单个方法接口上，比如Spring的 `RowMapper`。如果您使用的是Java 7或更早的版本，那么您可以很容易地插入一个匿名接口实现，并拥有与lambda expresion的体所包含的方法体相同的方法体，而且它不会受到Spring的干扰。

## 建立可执行 jar

您可以使用Gradle或Maven从命令行运行应用程序。或者您可以构建一个包含所有必需依赖项、类和资源的可执行JAR文件，并运行它。这使得在整个开发生命周期中、在不同的环境中，以应用程序的形式发布、版本化和部署服务变得很容易。

您可以使用 spring-boot:run 运行应用程序。或者您可以使用 clean package 构建JAR文件。然后您可以运行JAR文件:

```text
java -jar target/gs-relational-data-access-0.1.0.jar
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
启动项目，您将看到如下信息：

```text
2018-12-19 10:25:10.849  INFO 32744 --- [           main] hello.Application                        : Inserting customer record for John Woo
2018-12-19 10:25:10.849  INFO 32744 --- [           main] hello.Application                        : Inserting customer record for Jeff Dean
2018-12-19 10:25:10.849  INFO 32744 --- [           main] hello.Application                        : Inserting customer record for Josh Bloch
2018-12-19 10:25:10.849  INFO 32744 --- [           main] hello.Application                        : Inserting customer record for Josh Long
2018-12-19 10:25:10.862  INFO 32744 --- [           main] hello.Application                        : Querying for customer records where first_name = 'Josh':
2018-12-19 10:25:10.875  INFO 32744 --- [           main] hello.Application                        : Customer[id=3, firstName='Josh', lastName='Bloch']
2018-12-19 10:25:10.875  INFO 32744 --- [           main] hello.Application                        : Customer[id=4, firstName='Josh', lastName='Long']
```

## 总结

恭喜你!您刚刚使用Spring开发了一个简单的JDBC客户端。

原文链接：[Accessing Relational Data using JDBC with Spring](https://spring.io/guides/gs/relational-data-access/)

