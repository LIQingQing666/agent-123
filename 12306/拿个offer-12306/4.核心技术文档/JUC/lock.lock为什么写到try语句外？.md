# lock.lock为什么写到try语句外？

<font style="color:rgb(51, 51, 51);">面试官：小伙子，JUC 并发包下的可重入锁 ReentrantLock 在代码里实际使用过么。</font>

<font style="color:rgb(51, 51, 51);">混子：用过，</font>**<font style="color:rgb(51, 51, 51);">ReentrantLock 是 JDK 提供的可重入的锁</font>**<font style="color:rgb(51, 51, 51);">。提供对 </font>**<font style="color:rgb(51, 51, 51);">共享资源的独占访问</font>**<font style="color:rgb(51, 51, 51);">，一次只能有一个线程可以获取该锁。</font>

<font style="color:rgb(51, 51, 51);">面试官：你觉得，</font>**<font style="color:rgb(51, 51, 51);">ReentrantLock#lock 方法写到 try 语句外面还是里面。</font>**

<font style="color:rgb(51, 51, 51);">混子：我......</font>

<font style="color:rgb(51, 51, 51);">面试官：我们不合适，你走吧。</font>

> <font style="color:rgb(119, 119, 119);">先给出结论，lock.lock() 最规范的写法是写到 try 语句的外面。</font>
>

## <font style="color:rgb(51, 51, 51);">lock.lock()</font>
<font style="color:rgb(51, 51, 51);">默认小伙伴对 ReentrantLock 和 AQS 相关的知识是掌握的。</font>

<font style="color:rgb(51, 51, 51);">Oracle 文档中在介绍锁的使用时有一段代码，我们以 </font>**<font style="color:rgb(51, 51, 51);">ReentrantLock 举例</font>**<font style="color:rgb(51, 51, 51);">，代码如下所示：</font>

```java
ReentrantLock lock = new ReentrantLock();
lock.lock();
try {
    // access the resource protected by this lock
} finally {
    lock.unlock();
}
```

<font style="color:rgb(51, 51, 51);"></font>

<font style="color:rgb(51, 51, 51);">Q：为什么要把 </font>**<font style="color:rgb(51, 51, 51);">lock.unlock()</font>**<font style="color:rgb(51, 51, 51);"> 放到 finally 语句块？</font>

<font style="color:rgb(51, 51, 51);">A：为了保证当前线程执行过程中出现异常时，锁依然能被释放掉，</font>**<font style="color:rgb(51, 51, 51);">避免死锁的产生。</font>**

<font style="color:rgb(51, 51, 51);">我们来改动一下上面的代码，看看会产生什么样的影响。</font>

```plain
ReentrantLock lock = new ReentrantLock();
try {
    lock.lock();
    // access the resource protected by this lock
} finally {
    lock.unlock();
}
```

<font style="color:rgb(51, 51, 51);">看着没问题呀，为啥文章开始不建议这么用？先说下可能会存在的问题。</font>

### <font style="color:rgb(51, 51, 51);">异常堆栈丢失</font>
**<font style="color:rgb(51, 51, 51);">假设在 lock.lock 方法中加锁异常（千万不要杠）</font>**<font style="color:rgb(51, 51, 51);">，那么会进入 finally 语句块中进行解锁。</font>

<font style="color:rgb(51, 51, 51);">继续跟进，看一下 lock.unlock() 源码中是如何处理的。</font>

![1690007958932-1b1ae86b-1901-4ddb-97de-0b3192b950da.png](./img/SQgZDpszCpJ-LLWV/1690007958932-1b1ae86b-1901-4ddb-97de-0b3192b950da-000637.png)

<font style="color:rgb(51, 51, 51);"></font>

<font style="color:rgb(51, 51, 51);">lock.lock() 抛出异常有可能还没获取到锁，那么 </font>**<font style="color:rgb(51, 51, 51);">解锁源码中将当前线程比较拥有锁线程肯定是不相等的</font>**<font style="color:rgb(51, 51, 51);">，所以会抛出 IMSE （IllegalMonitorStateException）异常。</font>

<font style="color:rgb(51, 51, 51);">我重写了 ReentrantLock 加锁代码的逻辑，在里面抛出了异常，一起看下会出现什么情况：</font>

```java
final void lock() {
    // 模拟加锁未成功就抛出异常
    if (true) {
        throw new RuntimeException("报错啦！！！");
    }
    if (compareAndSetState(0, 1))
        setExclusiveOwnerThread(Thread.currentThread());
    else
        acquire(1);
}
```

<font style="color:rgb(51, 51, 51);"></font>

<font style="color:rgb(51, 51, 51);">根据下图可以看出 </font>**<font style="color:rgb(51, 51, 51);">加锁时异常堆栈被 "吞掉了"</font>**<font style="color:rgb(51, 51, 51);">，悄无声息的就没了。当然这只是举例，但是谁能保证加锁未成功时不会抛出异常呢。</font>

![1690007958964-80a9c2b1-6dc0-4ae8-aa16-7b1de9e7db44.png](./img/SQgZDpszCpJ-LLWV/1690007958964-80a9c2b1-6dc0-4ae8-aa16-7b1de9e7db44-420161.png)

### <font style="color:rgb(51, 51, 51);">真实存在的 BUG</font>
<font style="color:rgb(51, 51, 51);">上面代码示例中都是在 try 的第一行写 lock，给大家提供一个反面教材，千万千万不要有这种类似行为。</font>

![1690007958958-cad92b3c-6667-4212-8dcb-a6c74df6c1df.png](./img/SQgZDpszCpJ-LLWV/1690007958958-cad92b3c-6667-4212-8dcb-a6c74df6c1df-670738.png)

<font style="color:rgb(51, 51, 51);"></font>

<font style="color:rgb(51, 51, 51);">示例代码中把 lock 放到了 try 语句块里，然后 </font>**<font style="color:rgb(51, 51, 51);">lock 加锁前面还有可能会产生异常的代码</font>**<font style="color:rgb(51, 51, 51);">，这种就凉了，谁用谁凉的那种。</font>

## <font style="color:rgb(51, 51, 51);">文末总结</font>
<font style="color:rgb(51, 51, 51);">所以关于要不要把 </font>**<font style="color:rgb(51, 51, 51);">lock.lock() 写到 try 语句块里</font>**<font style="color:rgb(51, 51, 51);">，文章的结论是：</font>

1. <font style="color:rgb(51, 51, 51);">最好是把 lock.lock() 加锁方法写到 try 外面，</font>**<font style="color:rgb(51, 51, 51);">这是一种规范，而不是强制。</font>**
2. <font style="color:rgb(51, 51, 51);">如果你非要写到 try 里面，那么 </font>**<font style="color:rgb(51, 51, 51);">请写到 try 语句块的第一行</font>**<font style="color:rgb(51, 51, 51);">，或者 lock 加锁方法前面不会存在可能出现异常的代码。</font>
3. <font style="color:rgb(51, 51, 51);">最后，</font>**<font style="color:rgb(51, 51, 51);">如果你代码中加锁放到了 try 语句里，麻烦参考第 1 点。</font>**



> 更新: 2023-07-22 14:47:19  
> 原文: <https://www.yuque.com/magestack/12306/pu52u29i6eb1c5wh>