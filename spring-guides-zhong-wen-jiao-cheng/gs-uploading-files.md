# 基于 Spring 实现文件上传

本指南将指导您创建一个可以接收 HTTP multi-part 文件的服务器应用程序。

## 目标

您将创建一个接受文件上传的 Spring Boot web应用程序。您还将构建一个简单的HTML界面来上传测试文件。


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
    <artifactId>gs-uploading-files</artifactId>
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
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
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

## 创建 Application 类

要启动一个 Spring Boot MVC 应用程序，我们首先需要一个 starter;在这里，已经添加了 `spring-boot-starter-thymeleaf` 和 `spring-boot-starter-web` 作为依赖项。要使用 Servlet 容器上传文件，您需要注册一个 `MultipartConfigElement` 类(在web.xml中是 `<multipart-config>` )。多亏了Spring Boot，一切都是为您自动配置的!

开始使用此应用程序所需的全部内容是以下 `Application ` 类。


`src/main/java/hello/Application.java`
    

```text
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

作为自动配置Spring MVC的一部分，Spring Boot将创建一个 `MultipartConfigElement` bean，并为文件上传做好准备。

## 创建文件上传 controller
初始应用程序已经包含了一些类来处理在磁盘上存储和加载上传的文件;它们都位于 `hello.storage` 包下。我们将在新创建的 `FileUploadController` 中使用这些。

`src/main/java/hello/FileUploadController.java`


```text
package hello;

import java.io.IOException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import hello.storage.StorageFileNotFoundException;
import hello.storage.StorageService;

@Controller
public class FileUploadController {

    private final StorageService storageService;

    @Autowired
    public FileUploadController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/")
    public String listUploadedFiles(Model model) throws IOException {

        model.addAttribute("files", storageService.loadAll().map(
                path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
                        "serveFile", path.getFileName().toString()).build().toString())
                .collect(Collectors.toList()));

        return "uploadForm";
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

        Resource file = storageService.loadAsResource(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @PostMapping("/")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) {

        storageService.store(file);
        redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded " + file.getOriginalFilename() + "!");

        return "redirect:/";
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

}
```

这个类是用 `@Controller` 注解的，所以 Spring MVC 可以选择它并查找路由。每个方法都被标记为 `@GetMapping` 或 `@PostMapping` ，将路径和HTTP操作绑定到特定的 Controller 操作。

在这种案例中:
* `GET /` 查找从 `StorageService` 上传的当前文件列表，并将其加载到 Thymeleaf 模板中。它使用 `MvcUriComponentsBuilder` 计算指向到实际资源的链接
* `GET /files/{filename}` 加载资源(如果存在)，并使用 `“content - dispose”` 响应头将其发送到浏览器下载
* `POST /` 用于处理 multi-part `file`，并将其交给 `StorageService` 保存

> 在生产场景中，您更可能将文件存储在临时位置、数据库或NoSQL存储(比如Mongo的GridFS)中。最好不要用内容加载应用程序的文件系统。

> 上面这句话，后半段真不知道怎么翻译，没有理解到其真实意思


您需要为控制器提供一个StorageService来与存储层(例如文件系统)交互。接口是这样的:

`src/main/java/hello/storage/StorageService.java`

```text
package hello.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.stream.Stream;

public interface StorageService {

    void init();

    void store(MultipartFile file);

    Stream<Path> loadAll();

    Path load(String filename);

    Resource loadAsResource(String filename);

    void deleteAll();

}
```

示例应用程序中有一个接口的示例实现。如果您想节省时间，可以复制和粘贴它。

## 创建简单的 HTML 模板
为了构建一些有趣的东西，下面的Thymeleaf模板是上传文件以及显示上传内容的一个很好的例子。
`src/main/resources/templates/uploadForm.html`

```text
<html xmlns:th="http://www.thymeleaf.org">
<body>

	<div th:if="${message}">
		<h2 th:text="${message}"/>
	</div>

	<div>
		<form method="POST" enctype="multipart/form-data" action="/">
			<table>
				<tr><td>File to upload:</td><td><input type="file" name="file" /></td></tr>
				<tr><td></td><td><input type="submit" value="Upload" /></td></tr>
			</table>
		</form>
	</div>

	<div>
		<ul>
			<li th:each="file : ${files}">
				<a th:href="${file}" th:text="${file}" />
			</li>
		</ul>
	</div>

</body>
</html>
```

该模板由三部分组成:
* 顶部的可选消息，其中 Spring MVC 编写 flash-scoped 的消息。
* 允许用户上传文件的表单
* 从后端提供的文件列表


## 设置文件上传限制
在配置文件上传时，对文件大小设置限制通常很有用。想象一下，试图处理一个5GB的文件上传!使用 Spring Boot，我们可以通过一些属性设置来调优其自动配置的 `MultipartConfigElement` 。

添加以下配置到到配置文件中：
`src/main/resources/application.properties`

```text
spring.servlet.multipart.max-file-size=128KB
spring.servlet.multipart.max-request-size=128KB
spring.http.multipart.enabled=false
```

multipart 设置受如下约束:
* `spring.http.multipart.max-file-size` 设置为128KB，这意味着总文件大小不能超过128KB。
* `spring.http.multipart.max-request-size` 设置为128KB，这意味着 `multipart/form-data` 的请求总大小不能超过128KB。

## 让运用程序可执行

尽管可以将此服务打包为部署到外部应用服务器的传统WAR文件，但是下面演示的更简单的方法将创建一个独立的应用程序。您可以将所有内容打包到一个单独的、可执行的JAR文件中，该文件由一个很好的老Java `main()` 方法驱动。在此过程中，您将使用Spring内嵌的Tomcat servlet容器为HTTP运行时的支持，而不是部署到外部实例。

您还需要一个目标文件夹来上传文件，所以让我们增强基本的 `Application`，并添加一个 Boot `CommandLineRunner`，在启动时删除和重新创建该文件夹:

`src/main/java/hello/Application.java`

```text
package hello;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import hello.storage.StorageProperties;
import hello.storage.StorageService;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    CommandLineRunner init(StorageService storageService) {
        return (args) -> {
            storageService.deleteAll();
            storageService.init();
        };
    }
}
```


`@SpringBootApplication` 是一个方便的注释，它添加了以下所有内容：

* `@Configuration` 将类标记为应用程序上下文bean定义的源。
* `@EnableAutoConfiguration` 告诉Spring Boot开始基于类路径设置、其他bean和各种属性设置添加bean。通常情况下，您会为Spring MVC应用程序添加 `@EnableWebMvc`，但是当在类路径中看到Spring-webmvc时，Spring Boot会自动添加@EnableWebMvc。这将应用程序标记为web应用程序，并激活关键行为，如设置 `DispatcherServlet`。
* `@ComponentScan` 告诉Spring在 `hello` 包中查找其他组件、配置和服务，从而允许它查找控制器。

main()方法使用Spring Boot的SpringApplication.run()方法启动应用程序。您注意到没有一行XML吗?没 web.xml 文件。这个web应用程序是100%纯Java的，您不必配置任何管道或基础设施。


## 建立可执行 jar

您可以使用Gradle或Maven从命令行运行应用程序。或者您可以构建一个包含所有必需依赖项、类和资源的可执行JAR文件，并运行它。这使得在整个开发生命周期中、在不同的环境中，以应用程序的形式发布、版本化和部署服务变得很容易。

您可以使用 spring-boot:run 运行应用程序。或者您可以使用 clean package 构建JAR文件。然后您可以运行JAR文件:

```text
java -jar target/gs-uploading-files-0.1.0.jar
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

它运行接收文件上传的服务器端部分。显示日志输出。服务应该在几秒钟内启动并运行。

运行服务器后，需要打开浏览器并访问http://localhost:8080/查看上传表单。选择一个(小的)文件并按“upload”键，您应该会看到来自控制器的成功页面。选择一个太大的文件，你会得到一个难看的错误页面。

您应该在浏览器窗口中看到如下内容:

`You successfully uploaded <name of your file>!`

## 测试服务
在我们的应用程序中，有多种方法可以测试这个特性。这里有一个利用 `MockMvc` 的例子，所以它不需要启动Servlet容器:

`src/test/java/hello/FileUploadTests.java`

```text
package hello;

import java.nio.file.Paths;
import java.util.stream.Stream;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import hello.storage.StorageFileNotFoundException;
import hello.storage.StorageService;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class FileUploadTests {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private StorageService storageService;

    @Test
    public void shouldListAllFiles() throws Exception {
        given(this.storageService.loadAll())
                .willReturn(Stream.of(Paths.get("first.txt"), Paths.get("second.txt")));

        this.mvc.perform(get("/")).andExpect(status().isOk())
                .andExpect(model().attribute("files",
                        Matchers.contains("http://localhost/files/first.txt",
                                "http://localhost/files/second.txt")));
    }

    @Test
    public void shouldSaveUploadedFile() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt",
                "text/plain", "Spring Framework".getBytes());
        this.mvc.perform(fileUpload("/").file(multipartFile))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/"));

        then(this.storageService).should().store(multipartFile);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void should404WhenMissingFile() throws Exception {
        given(this.storageService.loadAsResource("test.txt"))
                .willThrow(StorageFileNotFoundException.class);

        this.mvc.perform(get("/files/test.txt")).andExpect(status().isNotFound());
    }

}
```

在这些测试中，我们使用各种模拟来设置与控制器和 `StorageService` 的交互，还使用`MockMultipartFile` 与 Servlet 容器本身的交互。

有关集成测试的示例，请查看 `FileUploadIntegrationTests` 类。


## 总结

恭喜你!您刚刚编写了一个使用 Spring 处理文件上传的web应用程序。

原文链接：[Uploading Files](https://spring.io/guides/gs/uploading-files/)

