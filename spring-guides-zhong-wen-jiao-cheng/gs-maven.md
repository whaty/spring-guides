# 使用 maven 构建 java 项目

本教程带你用 maven 创建一个简单的 java 项目 .

## 目标

你将创建一个应用，来获取一天中的时间，然后用 maven 来构建该应用：


## 准备工作

* 大约15分钟
* 一个最喜欢的文本编辑器或IDE
* JDK 1.8 或 更高版本


## 如何完成该指南

和其他 Spring 入门指南一样， 你可以跟着教程一步步完成操作，你也可以跳过下面的基本教程。 通过其他方法获取本教程所有代码。

* 跟着教程一步步学习，教程以 Maven 为示例
* 跳过基本教程，通过以下方式获取源码：
  * 通过以下地址下载并解压本教程源代码，或者用Git克隆一份代码到本地：

    `git clone https://github.com/whaty/spring-guides.git`

  * 将导出的项目导入到开发工具即可

## 新建一个项目

首先，您需要为Maven构建一个Java项目。为了把关注点放在 Maven 上，现在让项目尽可能地简单。在您选择的项目文件夹中创建此结构。


### 创建如下目录结构

```text
└── src
    └── main
        └── java
            └── hello
```

在 `src/main/java/hello` 目录中，您可以创建任何您想要的java类。为了保持与本指南其余部分的一致性，请创建这两个类: `HelloWorld.java` 和 `Greeter.java` 。

`src/main/java/hello/HelloWorld.java`

```text
package hello;

public class HelloWorld {
    public static void main(String[] args) {
        Greeter greeter = new Greeter();
        System.out.println(greeter.sayHello());
    }
}

```

`src/main/java/hello/Greeter.java`

```text
package hello;

public class Greeter {
    public String sayHello() {
        return "Hello world!";
    }
}
```

现在您已经有了一个可以使用Maven构建的项目，下一步是安装Maven。

Maven可以作为zip文件从 http://maven.apache.org/download.cgi 下载。只需要二进制文件，所以请寻找到 `apache-maven-{version}-bin.zip` 或 `apache-maven-{version}-bin.tar.gz` 进行下载。

下完之后，解压文件到电脑任意目录，并配置maven环境变量

测试maven是否安装成功，在命令窗口使用命令 `mvn`，如下：

`mvn -v`

 如果一切正常，在命令行窗口将看到如下信息：

```text
Apache Maven 3.3.9 (bb52d8502b132ec0a5a3f4c09453c07478323dc5; 2015-11-10T16:41:47+00:00)
Maven home: /home/dsyer/Programs/apache-maven
Java version: 1.8.0_152, vendor: Azul Systems, Inc.
Java home: /home/dsyer/.sdkman/candidates/java/8u152-zulu/jre
Default locale: en_GB, platform encoding: UTF-8
OS name: "linux", version: "4.15.0-36-generic", arch: "amd64", family: "unix"
```
 
恭喜你，到此，你已经成功安装了maven！
 
 ## 定义一个简单的 Maven 构建
 
安装了Maven之后，您需要创建Maven项目定义。Maven项目是用一个名为pom.xml的XML文件定义的。除此之外，这个文件还提供了项目的名称、版本以及它对外部库的依赖关系。
 
创建一个名为pom的文件。项目根目录下的xml(即放在src文件夹旁边)，并给出如下内容:
 

**pom.xml**文件如下

```text
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.springframework</groupId>
    <artifactId>gs-maven</artifactId>
    <packaging>jar</packaging>
    <version>0.1.0</version>

    <properties>
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
    </properties>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>2.1</version>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>shade</goal>
                        </goals>
                        <configuration>
                            <transformers>
                                <transformer
                                    implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                                    <mainClass>hello.HelloWorld</mainClass>
                                </transformer>
                            </transformers>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

除了可选的 `<packaging>` 元素，这是最简单的构建Java项目所需的pom.xml文件。它包括项目配置的以下细节:

* `<modelVersion>` - POM 模块版本。
* `<groupId>` - 项目所属组或者所属组织机构。通常采用反过来的网址域名，比如：com.google。
* `<artifactId>` - 项目打包之后的名字。
* `<version>` - 项目构建版本。
* `<packaging>` - 项目打包成什么类型的包，默认 "jar" 打包成 JAR 包。也可以使用 "war" 打包成 WAR 包。


至此，您已经定义了一个最小但功能强大的Maven项目。

## 构建 java 代码

Maven现在已经准备好构建项目。现在可以使用Maven执行几个构建生命周期目标，包括编译项目代码、创建库包(例如JAR文件)和在本地Maven依赖存储库中安装库。

要尝试构建，在命令行发出以下命令:

`mvn complie`

这将运行Maven，告诉它执行编译目标。当它完成时，您应该在 `src/classes` 目录中找到编译后的.class文件。

由于您不太可能希望直接分发或使用.class文件，所以您可能希望运行包目标:

`mvn package`

packge 命令将编译Java代码，运行任何测试，最后将代码打包到target目录中的JAR文件中。JAR文件的名称将基于项目的 `<artifactId>` 和 `<version>` 。例如，给定的pom.xml文件，JAR文件将命名为gs-maven-0.1.0.jar。

>如果您将 `<packaging>` 的值从“jar”更改为“war”，结果将在target目录中生成war文件，而不是jar文件。

Maven还在本地机器上(通常在主目录中的.m2/repository目录中)维护一个依赖项存储库，以便快速访问项目依赖项。如果您想将项目的JAR文件安装到本地存储库，那么应该调用 `install` 命令:

`mnv install`

install 将compllie、test、package项目代码，然后将其复制到本地依赖存储库，以便另一个项目将其作为依赖引用。

说到依赖关系，现在是在Maven构建中声明依赖关系的时候了。


## 声明依赖

简单的Hello World示例是完全自包含的，不依赖于任何其他库。然而，大多数应用程序依赖于外部库来处理常见和复杂的功能。

例如，假设除了输出“Hello World!”，您希望应用程序打印当前日期和时间。虽然可以在本地Java库中使用日期和时间工具，但是可以通过使用Joda时间库使事情变得更有趣。

首先,改变HelloWorld。java看起来是这样的:

`src/main/java/hello/HelloWorld.java`

```text
package hello;

import org.joda.time.LocalTime;

public class HelloWorld {
	public static void main(String[] args) {
		LocalTime currentTime = new LocalTime();
		System.out.println("The current local time is: " + currentTime);
		Greeter greeter = new Greeter();
		System.out.println(greeter.sayHello());
	}
}
```

这里HelloWorld使用Joda Time的LocalTime类获取并打印当前时间。

如果您现在运行mvn compile来构建项目，那么构建将失败，因为您没有在构建中声明Joda Time作为编译依赖项。您可以通过向pom.xml添加以下代码行来修复这个问题(在<project>元素中):

```text
<dependencies>
		<dependency>
			<groupId>joda-time</groupId>
			<artifactId>joda-time</artifactId>
			<version>2.9.2</version>
		</dependency>
</dependencies>
```

这段XML声明了项目的依赖项列表。具体来说，它为Joda时间库声明了一个依赖项。在<dependency>元素中，依赖关系坐标由三个子元素定义:

* `<groupId>` - 依赖项所属的组或组织。
* `<artifactId>` - 需要的库。
* `<version>` - 需要的库的指定版本。

默认情况下，所有依赖项的作用域都是编译依赖项。也就是说，它们应该在编译时可用(如果您正在构建WAR文件，包括在WAR的/WEB-INF/libs文件夹中)。此外，您可以指定<scope>元素来指定以下范围之一:

* `provided ` - 编译项目代码所需要该依赖项，但在运行时将由运行代码的容器提供依赖项(例如，Java Servlet API)。
* `test  ` - 用于编译和运行测试，但不用于构建或运行项目运行时代码的依赖项。

现在，如果您运行mvn编译或mvn包，Maven应该从Maven中央存储库解决Joda时间依赖性，构建将会成功。

## 编写测试
首先，将JUnit作为依赖项添加到pom.xml，在 `test` 范围内:

```text
<dependency>
	<groupId>junit</groupId>
	<artifactId>junit</artifactId>
	<version>4.12</version>
	<scope>test</scope>
</dependency>
```

接着创建测试用例，如下：
`src/test/java/hello/GreeterTest.java`

```text
package hello;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

import org.junit.Test;

public class GreeterTest {

	private Greeter greeter = new Greeter();

	@Test
	public void greeterSaysHello() {
		assertThat(greeter.sayHello(), containsString("Hello"));
	}

}
```

Maven使用一个名为“surefire”的插件来运行单元测试。这个插件的默认配置是编译并运行 `src/test/java` 中的所有类，这些类的名称与 `*Test` 匹配。您可以像这样在命令行上运行测试

`mvn test`

或者只使用 `mvn install` 步骤，就像我们前面已经展示的那样(有一个生命周期定义，其中“test”作为“install”中的一个阶段包含在其中)。

这是完整的pom.xml文件:

```text
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
	<modelVersion>4.0.0</modelVersion>

	<groupId>org.springframework</groupId>
	<artifactId>gs-maven</artifactId>
	<packaging>jar</packaging>
	<version>0.1.0</version>

	<properties>
		<maven.compiler.source>1.8</maven.compiler.source>
		<maven.compiler.target>1.8</maven.compiler.target>
	</properties>

	<dependencies>
		<!-- tag::joda[] -->
		<dependency>
			<groupId>joda-time</groupId>
			<artifactId>joda-time</artifactId>
			<version>2.9.2</version>
		</dependency>
		<!-- end::joda[] -->
		<!-- tag::junit[] -->
		<dependency>
			<groupId>junit</groupId>
			<artifactId>junit</artifactId>
			<version>4.12</version>
			<scope>test</scope>
		</dependency>
		<!-- end::junit[] -->
	</dependencies>

	<build>
		<plugins>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-shade-plugin</artifactId>
				<version>2.1</version>
				<executions>
					<execution>
						<phase>package</phase>
						<goals>
							<goal>shade</goal>
						</goals>
						<configuration>
							<transformers>
								<transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
									<mainClass>hello.HelloWorld</mainClass>
								</transformer>
							</transformers>
						</configuration>
					</execution>
				</executions>
			</plugin>
		</plugins>
	</build>

</project>
```

>完成的pom.xml文件使用Maven Shade插件，以便使JAR文件可执行。本指南的重点是从Maven开始，而不是使用这个特定的插件。


## 总结

恭喜你!您已经为构建Java项目创建了一个简单而有效的Maven项目定义。。

原文链接：[BBuilding Java Projects with Maven](https://spring.io/guides/gs/maven/)

