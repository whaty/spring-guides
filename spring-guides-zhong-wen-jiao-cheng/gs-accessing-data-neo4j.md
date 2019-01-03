# 使用 Neo4j 访问数据

本指南将向您介绍如何使用 [Spring Data Neo4j](https://spring.io/projects/spring-data-neo4j) 构建一个应用程序，该应用程序将数据存储在 Neo4j 中，并从基于图形的数据库 [Neo4j](https://neo4j.com/) 检索数据。

## 目标

您将使用 Neo4j 的基于 [NoSQL](https://spring.io/understanding/NoSQL) 的图形数据存储来构建嵌入式 Neo4j 服务器、存储实体和关系，并开发查询。

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
	<artifactId>gs-accessing-data-neo4j</artifactId>
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
			<artifactId>spring-boot-starter-data-neo4j</artifactId>
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

## 配置 Neo4j 服务

在构建此应用程序之前，需要设置一个 Neo4j 服务器。

Neo4j 有一个开源服务器，你可以免费安装:

在Mac电脑上，只需输入:

`brew install neo4j`

有关其电脑，请访问 https://neo4j.com/download/community-edition/

一旦你安装完毕，你可以用默认设置启动它。

`neo4j start`

你将看到如下信息：

```text
Starting Neo4j.
Started neo4j (pid 96416). By default, it is available at http://localhost:7474/
There may be a short delay until the server is ready.
See /usr/local/Cellar/neo4j/3.0.6/libexec/logs/neo4j.log for current status.
```

在默认情况下，Neo4j 的用户名/密码是 Neo4j / Neo4j。但是，它要求更改新帐户密码。为此，请执行以下命令:

` curl -v -u neo4j:neo4j -X POST localhost:7474/user/neo4j/password -H "Content-type:application/json" -d "{\"password\":\"secret\"}"`

这将密码从 neo4j 更改为 secret (在生产环境中不能这样做!)完成之后，您就可以运行本指南了。

> 如果您无法下载，请通过以下地址下载：

`https://pan.baidu.com/s/1qDfUCrionfCHdx40cPuW0w`

> 下载完成后，解压到任意目录，进入bin目录，执行以下命令：

`neo4j console`

> 浏览器访问，http://localhost:7474，即可

或者，将neo4j设置为服务

安装和卸载服务：

```text
bin\neo4j install-service
bin\neo4j uninstall-service
```

启动服务，停止服务，重启服务和查询服务的状态：

```text
bin\neo4j start
bin\neo4j stop
bin\neo4j restart
bin\neo4j status
```

## 定义一个简单的实体

Neo4j 捕获实体及其关系，这两个方面同等重要。假设您正在建模一个系统，其中存储每个人的记录。但是您还希望跟踪某人的同事(在本例中是 `teammates`)。使用 Neo4j，您可以通过一些简单的注释来获取所有这些信息。

`src/main/java/hello/Person.java`

```text
package hello;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity
public class Person {

	@Id @GeneratedValue private Long id;

	private String name;

	private Person() {
		// Empty constructor required as of Neo4j API 2.0.5
	};

	public Person(String name) {
		this.name = name;
	}

	/**
	 * Neo4j doesn't REALLY have bi-directional relationships. It just means when querying
	 * to ignore the direction of the relationship.
	 * https://dzone.com/articles/modelling-data-neo4j
	 */
	@Relationship(type = "TEAMMATE", direction = Relationship.UNDIRECTED)
	public Set<Person> teammates;

	public void worksWith(Person person) {
		if (teammates == null) {
			teammates = new HashSet<>();
		}
		teammates.add(person);
	}

	public String toString() {

		return this.name + "'s teammates => "
			+ Optional.ofNullable(this.teammates).orElse(
					Collections.emptySet()).stream()
						.map(Person::getName)
						.collect(Collectors.toList());
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
```
这里有一个 `Person` 类，它只有一个属性 `name`。

> 在本指南中，为了简洁起见，省略了典型的 getter 和 setter。

`Person` 类被注释为 `@NodeEntity`。当Neo4j存储它时，它会导致一个新节点的创建。这个类还有一个 `id` 标记为 `@GraphId`。Neo4j 在内部使用 `@GraphId 跟踪数据。

下一个重要部分是 `teammates` 的集合。它是一个简单的 `Set<Person>`，但是标记为 `@Relationship`。这意味着该集合的每个成员也将作为单独的 `Person` 节点存在。注意方向是如何设置为 `UNDIRECTED` 。这意味着当您查询 `TEAMMATE ` 关系时，Spring Data Neo4j 将忽略关系的方向。

使用 `worksWith()` 方法，您可以轻松地将人们联系在一起。

最后，您还有一个方便的 `toString()` 方法来打印此人的姓名和此人的同事。


## 创建简单的查询

Spring Data Neo4j 专注于在 Neo4j 中存储数据。但是它继承了 Spring Data Commons项目的功能，包括派生查询的能力。本质上，您不必学习 Neo4j 的查询语言，但是可以简单地编写一些方法，并且查询是为您编写的。

要了解这是如何工作的，请创建查询 `Person` 节点的接口。

`src/main/java/hello/PersonRepository.java`

```text
package hello;

import org.springframework.data.repository.CrudRepository;

public interface PersonRepository extends CrudRepository<Person, Long> {

    Person findByName(String name);
}
```

`PersonRepository` 扩展了 `GraphRepository` 接口并插入了它所操作的类型: `Person` 。开箱即用的界面提供了许多操作，包括标准的CRUD(创建-读取-更新-删除)操作。

但是您可以通过简单地声明它们的方法签名来根据需要定义其他查询。在本例中，您添加了 `findByName`，它查找 `Person` 类型的节点并找到与名称匹配的节点。您还可以使用 `findByTeammatesName` ，它查找 `Person` 节点，深入到 `teammates` 字段的每个条目，并根据 `teammates` 的名称进行匹配。

## 访问 Neo4j 的权限

Neo4j Community Edition 需要凭据来访问它。可以用几个属性配置它。

```text
spring.data.neo4j.username=neo4j
spring.data.neo4j.password=secret
```

这包括我们前面选择的默认用户名 `neo4j` 和新设置的密码 `secret`。

>不要在源存储库中存储真正的凭据。相反，在运行时使用 Spring Boot 的属性覆盖来配置它们。

有了这个，让我们把它连接起来看看它是什么样子!

## 创建 Application 类

在这里，您将创建一个包含所有组件的应用程序类。

`src/main/java/hello/Application.java`

```text
package hello;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

@SpringBootApplication
@EnableNeo4jRepositories
public class Application {

	private final static Logger log = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) throws Exception {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	CommandLineRunner demo(PersonRepository personRepository) {
		return args -> {

			personRepository.deleteAll();

			Person greg = new Person("Greg");
			Person roy = new Person("Roy");
			Person craig = new Person("Craig");

			List<Person> team = Arrays.asList(greg, roy, craig);

			log.info("Before linking up with Neo4j...");

			team.stream().forEach(person -> log.info("\t" + person.toString()));

			personRepository.save(greg);
			personRepository.save(roy);
			personRepository.save(craig);

			greg = personRepository.findByName(greg.getName());
			greg.worksWith(roy);
			greg.worksWith(craig);
			personRepository.save(greg);

			roy = personRepository.findByName(roy.getName());
			roy.worksWith(craig);
			// We already know that roy works with greg
			personRepository.save(roy);

			// We already know craig works with roy and greg

			log.info("Lookup each person by name...");
			team.stream().forEach(person -> log.info(
					"\t" + personRepository.findByName(person.getName()).toString()));
		};
	}

}
```

`@SpringBootApplication` 是一个方便的注释，它添加了以下所有内容：

* `@Configuration` 将类标记为应用程序上下文bean定义的源。
* `@EnableAutoConfiguration` 告诉Spring Boot开始基于类路径设置、其他bean和各种属性设置添加bean。通常情况下，您会为Spring MVC应用程序添加 `@EnableWebMvc`，但是当在类路径中看到Spring-webmvc时，Spring Boot会自动添加@EnableWebMvc。这将应用程序标记为web应用程序，并激活关键行为，如设置 `DispatcherServlet`。
* `@ComponentScan` 告诉Spring在 `hello` 包中查找其他组件、配置和服务，从而允许它查找控制器。

main()方法使用Spring Boot的SpringApplication.run()方法启动应用程序。您注意到没有一行XML吗?没 web.xml 文件。这个web应用程序是100%纯Java的，您不必配置任何管道或基础设施。

Spring Boot 将自动处理这些存储库，只要它们包含在 `@SpringBootApplication` 类的相同包(或子包)中。对于注册过程的更多控制，您可以使用 `@ enableneo4jrepository` 注释。

默认情况下，`@enableneo4jrepository` 将扫描当前包中任何扩展 Spring Data 的存储库接口的接口。`basePackageClasses = MyRepository.class` 使用它来安全地告诉 Spring Data Neo4j 按类型扫描不同的根包，如果您的项目布局有多个项目，并且没有找到存储库。

显示日志输出。服务应该在几秒钟内启动并运行。

您可以自动连接前面定义的 `PersonRepository` 实例。Spring Data Neo4j 将动态实现该接口，并插入所需的查询代码以满足接口的义务。

`public static void main` 使用 Spring Boot 的 `SpringApplication.run()` 来启动应用程序并调用构建关系的 `CommandLineRunner` 。

在本例中，您创建了三个本地人员，Greg、Roy和Craig。最初，它们只存在于内存中。同样重要的是，(目前)没有人是任何人的队友。

一开始，你找到 Greg ，并表示他与Roy和Craig合作，然后实例化他。记住，队友关系被标记为无方向性的，也就是双向的。这意味着Roy 和Craig 也会得到更新。

这就是为什么当您需要更新Roy时，首先从 Neo4j 获取记录是非常重要的。在把 Craig 加到名单上之前，你需要知道Roy队友的最新情况。

为什么没有获取Craig并添加任何关系的代码?因为你已经拥有了!Greg 早些时候把Craig 列为队友，Roy也是。这意味着没有必要再更新Craig的关系。您可以在遍历每个团队成员并将其信息打印到控制台时看到它。

最后，回顾一下另一个问题，回答“谁和谁一起工作?”


## 构建可执行 jar

您可以使用Gradle或Maven从命令行运行应用程序。或者您可以构建一个包含所有必需依赖项、类和资源的可执行JAR文件，并运行它。这使得在整个开发生命周期中、在不同的环境中，以应用程序的形式发布、版本化和部署服务变得很容易。

您可以使用 spring-boot:run 运行应用程序。或者您可以使用 clean package 构建JAR文件。然后您可以运行JAR文件:

```text
java -jar target/gs-accessing-data-neo4j-0.1.0.jar
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
Before linking up with Neo4j...
	Greg's teammates => []
	Roy's teammates => []
	Craig's teammates => []

Lookup each person by name...
	Greg's teammates => [Roy, Craig]
	Roy's teammates => [Greg, Craig]
	Craig's teammates => [Roy, Greg]
```



## 总结

恭喜你!您刚设置一个嵌入式 Neo4j 服务器，存储一些简单的相关实体，并开发一些快速查询。

> 如果您对使用基于超媒体的 RESTful 前端公开 Neo4j 存储库感兴趣，那么您可能希望[使用 REST 读取访问Neo4j数据](https://spring.io/guides/gs/accessing-neo4j-data-rest/)。

原文链接：[Accessing Data with Neo4j](https://spring.io/guides/gs/accessing-data-neo4j/)