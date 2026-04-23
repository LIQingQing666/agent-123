# 动态代理模式扩展线程池拒绝策略

**拒绝策略在什么时候执行？**

当客户端提交任务到线程池时，执行execute()过程中，满足以下任意条件，就会调用reject()发起拒绝任务：

1. 当前线程池的状态非运行状态（线程池已经触发了停止行为）

2. 阻塞队列已满，并且线程池中已经创建了最大线程数的线程（线程都在运行中）

   ```
   final void reject(Runnable command) {
       handler.rejectedExecution(command, this);
   }
   ```

   `reject` 方法内部是通过调用成员变量 `handler` 的 `rejectedExecution` 方法来执行具体的拒绝策略。这个 `handler` 是 `RejectedExecutionHandler` 接口的实例，包括`CallerRunsPolicy、AbortPolicy、DiscardPolicy、DiscardOldestPolicy`。

**为什么线程池拒绝策略需要扩展额外功能？扩展哪些额外功能？**

在高并发、高吞吐量的极限情况下，平常稳定运行的线程池可能会变得不稳定，作为线程池任务执行策略中兜底的拒绝策略显得格外重要。

所以，针对线程池拒绝任务的扩展对于业务是很有必要，因为线程池抛出拒绝策略意味着 **业务受到影响** 或者 **线程池参数设置不合理**。

而默认的拒绝策略中以AbortPolicy为例，其rejectedExecution()只是抛了异常。我们期望线程池在拒绝提交的任务时，**还能扩展如下这些行为**

1. 发送线程池报警消息或邮件，通知到相关负责人。
2. 统计线程池拒绝任务的次数，方便后续统计时采集到关键指标。
3. ......

```
    public static class AbortPolicy implements RejectedExecutionHandler {
       
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            throw new RejectedExecutionException("Task " + r.toString() +
                                                 " rejected from " +
                                                 e.toString());
        }
    }
```

**如何实现扩展拒绝策略？**

然而，在阅读 JDK 线程池源码的时候，会发现**线程池 API 中并不支持通过继承重写实现拒绝策略的扩展。**体现在：

在 `ThreadPoolExecutor` 中，`reject` 方法的定义如下：

```
final void reject(Runnable command) {
    handler.rejectedExecution(command, this);
}
```

- **`final` 的作用**：reject方法被声明为 `final`，意味着任何子类都不能覆盖它。这样设计很可能是为了保证线程池拒绝任务时的核心流程不被篡改，避免子类因重写该方法而破坏线程池的内部状态一致性（例如：拒绝后未正确清理队列或更新计数器）。
- **扩展的阻碍**：如果我们想在所有拒绝行为前后添加统一的功能（如日志、统计），**无法通过继承 `ThreadPoolExecutor` 并重写 `reject` 来实现**。因此，必须另寻途径。

我们发现扩展线程池拒绝策略这一功能的实现正好符合**代理模式**的设计原则：通过一个代理类包装原始 `handler`，在调用 `rejectedExecution` 前后织入额外逻辑。

下面依次介绍代理模式的基本知识以及代理模式如何应用在扩展线程池拒绝策略。



## 代理模式
**代理模式**是Java中常用的设计模式之一，它的核心思想是通过一个代理对象来间接访问目标对象，从而在不改变目标对象的前提下增加额外的功能或者控制对目标对象的访问。代理模式分为**静态代理**和**动态代理**两种形式。

静态代理

在静态代理中，代理类在编译时就已经确定，需要手动编写代理类代码。代理类和目标对象实现相同的接口，并在代理类中调用目标对象的方法。静态代理的一个缺点是每次添加新功能时都需要创建新的代理类，这会导致类数量的增加和代码的冗余。静态代理的实现步骤包括定义接口及其实现类，创建代理类实现相同的接口，并在代理类中调用目标类的方法。

动态代理

动态代理相比静态代理更加灵活，它在运行时动态生成代理类，不需要为每个类手动编写代理类。Java中的动态代理主要依赖于*InvocationHandler*接口和*Proxy*类。动态代理可以在运行时决定代理哪个对象以及如何代理，常用于实现AOP（面向切面编程）的功能，如日志记录、性能监控等。

动态代理又分为JDK动态代理和CGLIB动态代理两种实现方式。

JDK动态代理

JDK动态代理只能代理实现了接口的类。它通过*Proxy*类提供的*newProxyInstance*方法动态创建代理对象。这个方法需要接收三个参数：类加载器、一组接口以及一个*InvocationHandler*实例。当代理对象的方法被调用时，实际上会转发到*InvocationHandler*的*invoke*方法。

CGLIB动态代理

CGLIB（Code Generation Library）是一个强大的高性能代码生成库，它允许在运行时扩展Java类和实现Java接口。CGLIB动态代理不需要目标对象实现接口，它是通过生成目标对象的子类来实现代理的。CGLIB动态代理的核心在于*MethodInterceptor*接口和*Enhancer*类，通过*Enhancer*创建代理对象，并在*MethodInterceptor*中定义方法拦截的逻辑。

JDK动态代理与CGLIB动态代理的对比

- **JDK动态代理**只能代理实现了接口的类，而**CGLIB动态代理**可以代理没有实现接口的类。
- **JDK动态代理**在早期版本中性能相对较低，但从JDK 1.8开始，性能得到了显著提升。**CGLIB动态代理**在创建代理对象时开销较大，但一旦创建完成，方法调用的性能通常比JDK动态代理更快。



代理对象Proxy：

目标对象target：RejectedExecutionHandler接口

invoke()







**扩展线程池**

先来创建个自定义线程池，继承原生 `ThreadPoolExecutor`。添加一个拒绝策略次数统计参数，并添加原子自增和查询方法。

```java
public class SupportThreadPoolExecutor extends ThreadPoolExecutor {

    /**
     * 拒绝策略次数统计
     */
    private final AtomicInteger rejectCount = new AtomicInteger();

    public SupportThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    /**
     * 设置 {@link SupportThreadPoolExecutor#rejectCount} 自增
     */
    public void incrementRejectCount() {
        rejectCount.incrementAndGet();
    }

    /**
     * 获取拒绝次数
     *
     * @return
     */
    public int getRejectCount() {
        return rejectCount.get();
    }
}
```



**扩展拒绝策略**

创建增强的公共拒绝策略，其中包含 `拒绝策略次数统计` 以及 `报警推送`，供实际的拒绝策略子类实现。

```java
public interface SupportRejectedExecutionHandler extends RejectedExecutionHandler {

    /**
     * 拒绝策略记录时, 执行某些操作
     *
     * @param executor
     */
    default void beforeReject(ThreadPoolExecutor executor) {
        if (executor instanceof SupportThreadPoolExecutor) {
            SupportThreadPoolExecutor supportExecutor = (SupportThreadPoolExecutor) executor;
            // 发起自增
            supportExecutor.incrementRejectCount();
            // 触发报警...
            System.out.println("线程池触发了任务拒绝...");
        }
    }
}

public class SupportAbortPolicyRejected extends ThreadPoolExecutor.AbortPolicy implements SupportRejectedExecutionHandler {

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        beforeReject(e);
        super.rejectedExecution(r, e);
    }
}
```



测试下上面的代码是否能够满足定的需求。

```java
@SneakyThrows
public static void main(String[] args) {
    SupportThreadPoolExecutor executor = new SupportThreadPoolExecutor(
            1,
            1,
            1024,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue(1),
            // 使用自定义拒绝策略
            new SupportAbortPolicyRejected()
    );

    // 测试流程
    for (int i = 0; i < 3; i++) {
        try {
            // 无限睡眠, 以此触发拒绝策略.(此处有异常, 为了减少无用代码, 省略...)
            executor.execute(() -> Thread.sleep(Integer.MAX_VALUE));
        } catch (Exception ignored) {
        }
    }

    Thread.sleep(50);
    System.out.println(String.format("线程池拒绝策略次数 :: %d", executor.getRejectCount()));
}

/**
 * 日志打印：
 *
 * 线程池触发了任务拒绝...
 * 线程池拒绝策略次数 :: 1
 */
```



根据日至打印得知，我们的扩展需求完整的实现了。当线程池执行任务拒绝行为时，首先会调用 `SupportRejectedExecutionHandler#beforeReject`，然后才是执行真正的拒绝策略行为。

虽然代码需求完成了，但是好与不好，或者说是否可以再优化，咱们继续往下看。

### 1. 静态代理
上面扩展的代码，是一种很典型的设计模式：**静态代理**。建议小伙伴往下阅读前能够在本地运行下，实践出真知。



运行完上述代码，小编画了一张图总结下静态代理的运行模式。

![1671355730968-ee5afe1e-e283-42a4-82cd-1b26561aabf6.png](./img/X2rcK0bEBcUET_Ke/1723976857398-d89a6217-9c92-433d-9b22-91427b8d20a3-447300.png)



通过线程池拒绝策略的实战，让大家对静态代理实现方式有了一定了解。但是这种处理方式真的是优雅的么？



我们来说一下上面代码的缺点，或者说静态代理模式的缺点：

1. 静态代理会造成系统设计中类的数量增加。比如：线程池原生的四种拒绝策略，如果想要使用扩展功能，需要创建对应的类实现 `SupportRejectedExecutionHandler` 接口，违背开闭原则；
2. 增加了系统复杂度。项目中所有线程池都要改动拒绝策略的实现；如果新接手项目的同学，可能会忽略这个代理的细节。

说到这里，如何解决静态代理带来的问题呢？答案就是：**动态代理**。

### 2. 动态代理
动态代理采用在 **运行时动态生成代码** 的方式，取消了对被代理类的扩展限制，遵循开闭原则。

说到这里，问题很多的小伙伴就要问了：你说的这个动态代理，和之前面试官问我的 **MyBatis Mapper 接口为什么不需要实现类**，咋这么像？

这里我额外说一下，MyBatis Mapper 接口运用了动态代理来 **规避 JDBC 交互数据库等重复代码行为**。

如何将动态代理代入到线程池拒绝策略呢？文章采用 **JDK 动态代理**的例子和大家说明。



**InvocationHandler**

创建代理 `InvocationHandler`，这个类主要负责代理拒绝策略执行的，也是 JDK 动态代理必不可少的一个环节。

```java
@AllArgsConstructor
public class RejectedExecutionProxyInvocationHandler implements InvocationHandler {

    private RejectedExecutionHandler target;

    private SupportThreadPoolExecutor executor;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 执行拒绝策略前自增拒绝次数 & 发起报警
        executor.incrementRejectCount();
        System.out.println("线程池触发了任务拒绝...");
        return method.invoke(target, args);
    }
}
```



线程池使用代理类进行任务拒绝，测试代码如下：

```java
@SneakyThrows
public static void main(String[] args) {
    // 删除 SupportThreadPoolExecutor 构造方法中的拒绝策略
    SupportThreadPoolExecutor executor = new SupportThreadPoolExecutor(
            1,
            1,
            1024,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue(1)
    );

    ThreadPoolExecutor.AbortPolicy abortPolicy = new ThreadPoolExecutor.AbortPolicy();
    // 创建拒绝策略代理类
    RejectedExecutionHandler rejectedExecutionHandler = (RejectedExecutionHandler) Proxy.newProxyInstance(
            abortPolicy.getClass().getClassLoader(),
            abortPolicy.getClass().getInterfaces(),
            new RejectedExecutionProxyInvocationHandler(abortPolicy, executor)
    );
    // 线程池 set 拒绝策略代理类
    executor.setRejectedExecutionHandler(rejectedExecutionHandler);

    // 测试流程
    for (int i = 0; i < 3; i++) {
        try {
            // 无限睡眠, 以此触发拒绝策略.(此处有异常, 为了减少无用代码, 省略...)
            executor.execute(() -> Thread.sleep(Integer.MAX_VALUE));
        } catch (Exception ex) {
            // ignore
        }
    }

    Thread.sleep(50);
    System.out.println(String.format("线程池拒绝策略次数 :: %d", executor.getRejectCount()));
}

/**
 * 日志打印：
 *
 * 线程池触发了任务拒绝...
 * 线程池拒绝策略次数 :: 1
 */
```











好处就比较显而易见了，我们不用像静态代理一样为 **每个拒绝策略实现类手动创建代理类**，因为动态代理的代理类是 **运行时生成的。**

问题很多的小伙伴可能又要问了，虽然创建动态代理可以解决 **类的数量增加**；但是，**代理类的创建依然需要开发人员操作**，这样上面说的静态代理的第二个缺点依然无法解决。

这个问题很好，发现代码中不合理的存在并且优化掉它，是工程师的一种美德。

我们换一种思路去创建代理拒绝策略类，从外部的创建变更到内部就可以了；这一版选择在线程池的构造方法内部实现代理类。

```java
public class SupportThreadPoolExecutor extends ThreadPoolExecutor {

    // 省略代码...

    public SupportThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);

        RejectedExecutionHandler rejectedExecutionHandler = (RejectedExecutionHandler) Proxy.newProxyInstance(
                handler.getClass().getClassLoader(),
                handler.getClass().getInterfaces(),
                new RejectedExecutionProxyInvocationHandler(handler, this)
        );

        setRejectedExecutionHandler(rejectedExecutionHandler);
    }

    // 省略代码...
}
```

至此，使用动态代理扩展线程池任务拒绝策略的主线讲解就完成了。

简单总结下使用动态代理实现扩展线程池拒绝策略中统计拒绝次数和报警的功能的流程，以及具体如何使用这一代理类。

1. 创建 `InvocationHandler`的实现类`RejectedProxyInvocationHandler`，代理的行为也是在这里执行。内部包含了实际的拒绝策略和线程池引用，用来执行拒绝任务行为和拒绝次数自增；

2. 将创建代理的过程封装到工具中，使用 JDK Proxy 创建代理类，通过`Proxy.newProxyInstance()`方法创建代理对象，原理是运行时生成一个新的代理类

   

   将**代理类赋值到线程池**，这样线程池拒绝任务时就会包含代理类中的行为。

   ```
   //创建线程池
   ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 3, 1024, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1));
   //拒绝策略
   ThreadPoolExecutor.AbortPolicy abortPolicy = new ThreadPoolExecutor.AbortPolicy();
   AtomicLong rejectedNum = new AtomicLong();
   //将拒绝策略作为target使用代理创建工具RejectedProxyUtil创建代理proxyRejectedExecutionHandler。
   RejectedExecutionHandler proxyRejectedExecutionHandler = RejectedProxyUtil.createProxy(abortPolicy, rejectedNum);
   //将代理赋值到线程池中
   threadPoolExecutor.setRejectedExecutionHandler(proxyRejectedExecutionHandler);
   for (int i = 0; i < 5; i++) {
       try {
           threadPoolExecutor.execute(() -> ThreadUtil.sleep(100000L));
       } catch (Exception ignored) {
           ignored.printStackTrace();
       }
   }
   ```

   

## 动态代理扩展知识
给大家再扩展一个问题，也是动态代理的精髓所在；面试过程中问的比较多的问题：**MyBatis Mapper 接口为什么不需要实现类？**



问题很多的小伙伴就问了：上面不是已经说了是动态代理，MyBatis Mapper 不需要实现类的问题不就解决了么？

没错，可能很多小伙伴都有相同的疑问，MyBatis 确实使用的动态代理，但是小伙伴疏忽了一个很关键的点。



**咱们上面实现的动态代理，拒绝策略是有实现类的，而 MyBatis Mapper 没有！**

由此引出一个知识点，创建 JDK 动态代理类的方式：

1. 上文描述的是动态代理有接口实现，接口实现为线程池下默认的四种处理策略，或用户自定义的；
2. MyBatis 明显是无接口实现，因为在开发过程中，只有一个 Mapper 接口。



**提前说明下，MyBatis 中使用的动态代理模式在线程池中并不适用，这里仅为了演示操作。**

```java
public class SupportThreadPoolExecutor extends ThreadPoolExecutor {

    // 省略代码...

    public SupportThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);

        RejectedExecutionHandler rejectedExecutionHandler = (RejectedExecutionHandler) Proxy.newProxyInstance(
                RejectedExecutionHandler.class.getClass().getClassLoader(),
                new Class[]{RejectedExecutionHandler.class},
                new RejectedExecutionProxyInvocationHandler(this)
        );

        setRejectedExecutionHandler(rejectedExecutionHandler);
    }

    // 省略代码...
}

@AllArgsConstructor
public class RejectedExecutionProxyInvocationHandler implements InvocationHandler {

    private SupportThreadPoolExecutor executor;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        executor.incrementRejectCount();
        System.out.println("线程池触发了任务拒绝...");

        throw new RejectedExecutionException("Task rejected from.");
    }
}
```



可以看到，对比文初的代码案例，这里对 Proxy#newProxyInstance 方法的参数作出了变化。之前是通过实现类获取所实现接口的 Class 数组，而这里是把接口本身放到 Class 数组中。当然，达到的效果都是一致的。



有接口实现的创建方式和无接口实现的创建方式，所产生的动态代理类有什么区别？

1. 有接口实现是对 InvocationHandler#invoke 方法调用，invoke 方法通过反射调用被代理对象 RejectedExecutionHandler#rejectedExecution
2. 无接口实现则是仅对 InvocationHandler#invoke 产生调用。所以，**有接口实现返回的是被代理对象接口返回值**，**而无实现接口返回的仅是 invoke 方法返回值。**

## 文末总结
文章采用图文并茂的形式，形象的描述了如何通过代理模式对线程池拒绝策略作出扩展，并基于动态代理知识点引申出 MyBatis Mapper 没有接口实现类的问题。

小伙伴可以基于文章所讲的动态代理拒绝策略，扩展到项目中使用的线程池，为线上的线程池任务运行加一份保障。



这里留下一道思考题：线程池拒绝策略除了发送报警和统计拒绝次数，还可以扩展出哪些对业务有帮助的行为？



> 更新: 2023-07-17 15:54:06  
> 原文: <https://www.yuque.com/magestack/12306/bbqxogg1hrqg4zzy>