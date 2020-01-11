## Java 代码热部署总结

#### 本工程提供的方案

* `JDK`动态编译，并整合进`Spring`架构，用户无需关注具体实现方式

  用法：把自己的热点代码写进一个单独的非`source folders`的文件夹（本工程是`script`），然后用`maven`的`maven-resourczes-plugin`把该文件夹当成`resources`编译进运行目录。

  具体看这个测试代码吧`com.ariescat.hotswap.test.TestSpringInject`



* todo 
  * 基于`agentmain`的函数体级别更热
  * 基于`agentmain`的匿名内部类新增



* 以上三点应该可以满足大部分生产环境上的bug修复。

  

#### 本人探索到的一些热更方式

* `JDK`提供

  * 通过 `premain` 或`agentmain`（推荐后者）获取到 `Instrumentation` 这个类的实例， 调用`retransformClass/redefineClass`进行函数体级别的字节码更新 

    基于`Attach`机制实现的热更新，更新类需要与原来的类在包名，类名，修饰符上完全一致，否则在`classRedefine`过程中会产生`classname don't match` 的异常。

    例如显示这样的报错：`redefineClasses exception class redefinition failed: attempted to delete a method.`

    具体来说，`JVM`热更新局限总结：

    1. 函数参数格式不能修改，只能修改函数内部的逻辑
    2. 不能增加类的函数或变量
    3. 函数必须能够退出，如果有函数在死循环中，无法执行更新类（笔者实验发现，死循环跳出之后，再执行类的时候，才会是更新类）

  * 通过`Instrumentation#appendToSystemClassLoaderSearch`来增加一个`classpath`，可以实现新增匿名内部类。

    See：[AgentAddAnonymousInnerClass](https://github.com/Ariescat/Metis/blob/82838045ceda1d70df594f0628c1a110ac7ae2a8/agent/src/main/java/com/agent/AgentAddAnonymousInnerClass.java)

  * 定义不同的`classloader`

    该方式必须要使用新的`ClassLoader`实例来创建类的对象，运行新对象的方法。

    Tomcat的动态部署就是监听`war`变化，然后调用`StandardContext.reload()`，用新的`WebContextClassLoader`实例来加载`war`，然后初始化`servlet`来实现。类似的实现还有`OSGi`等。

  * `JavaCompiler` 动态编译（ `JDK` 1.6 开始引入 ）

* 脚本

  * `java`结合`groovy`，把热点代码写进脚本里

* 第三方

  * `Github` [HotswapAgent](https://github.com/HotswapProjects/HotswapAgent)

    该工程基于`dcevm`，需要给`jvm`打上补丁（也就是要修改原生的`jvm`），该做法存在风险（自己团队没有在生产环境上跑过这种补丁，是否会存在未知风险？）

  * 阿里`arthas`

     使用`Arthas`三个命令就可以搞定热更新 ：

    ```shell
    jad --source-only com.example.demo.arthas.user.UserController > /tmp/UserController.java
    
    mc /tmp/UserController.java -d /tmp
    
    redefine /tmp/com/example/demo/arthas/user/UserController.class
    ```

    * 这个工具还可以协助完成下面这些事情(转自官网)：
      1. 这个类是从哪个`jar`包加载而来的？
      2. 为什么会报各种类相关的`Exception`？
      3. 线上遇到问题无法`debug`好蛋疼，难道只能反复通过增加`System.out`或通过加日志再重新发布吗？
      4. 线上的代码为什么没有执行到这里？是由于代码没有`commit`？还是搞错了分支？
      5. 线上遇到某个用户的数据处理有问题，但线上同样无法 `debug`，线下无法重现。
      6. 是否有一个全局视角来查看系统的运行状况？
      7. 有什么办法可以监控到`JVM`的实时运行状态？