# my-tomcat  属于个人学习实践
项目参考how tomcat 那本书
现有功能实现了 响应index页面，响应servlet,支持不通类型的servlet响应

todo 代码还比较乱，后面需要整理,后续会逐渐完善项目

core-server 为tomcat功能(处理静态资源没有经过mvc的dispachservlet，直接服务器响应的，例如图片，html等)
example 为demo ,替换掉springboot内置的tomcat,
由于Springboot内置的tomcat是先构建Spring上下文加载Bean，也就是扫描包，然后再在refreshContext()方法中创建Tomcat容器并启动容器,tomcat
在springboot中启动方法ServletWebServerFactory#getWebServer#initialize  ,
所有我们替换掉tomcat用自己的方法，也需要加载bean，调用启动方法，springboot 内部只提供了三种(tomcat,jetty,undlow)的启动配置，所以这个启动加载的过程只能我们自己做了

目前版本框架支持freemark页面的解析
SNAPSHOT 版本依赖
~~~java
<dependency>
    <groupId>com.chenkuojun</groupId>
    <artifactId>my-tomcat-spring-boot-starter</artifactId>
    <version>1.0.1</version>
</dependency>
~~~
项目使用的springboot的starter机制

需用在启动类上添加一个注解,用于启动时候自动去校验是不是有启动所需要的类
~~~
@EnableMyTomcatsStarter
~~~

升级了jdk版本11

# 打包命令
mvn clean source:jar javadoc:jar deploy -DskipTests
~~~

重新上传jar包出现了 401问题,有可能是settings.xml 文件导致的，
利用 mvn -X 命令查看当前生效的是哪个settings 文件

