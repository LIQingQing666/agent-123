# 幂等组件HTTP应用场景是什么？

## 问题提问
<font style="color:rgb(47, 48, 52);">看了一遍 12306 里面幂等的实现。 </font>

<font style="color:rgb(47, 48, 52);">马哥给了四种实现。 Param、SpELRest、SpELMQ、Token。 </font>

<font style="color:rgb(47, 48, 52);">我对幂等的理解是同一份数据只能处理一次。 而前两种在 handler 方法里的实现都是加锁。</font>

<font style="color:rgb(47, 48, 52);">这样的话，假如我是一个 insert 操作，接口调用间隔长点，数据依然会被插入多次。这就不是幂等了。</font>

## <font style="color:rgb(47, 48, 52);">问题回答</font>
先说第一种和第二种，本质上都是通过加锁解决。锁定的是一个周期，防止的是短时间内一个操作被用户误操作点了两次，造成的两次请求幂等，比如防重复提交场景。至于你说的间隔长点，数据依然被插入多次。设想一下，你在淘宝买两个一模一样的商品，同样的规格和数量，难道不能让你购买么？

Token 方式，此处需要再优化下，比如 Token 申请后，5 秒内有效，在有效期内，将无法再申请新的 Token。其次，如果每次调用业务接口前拿一次，这不正是解决重复提交幂等的方案么？



幂等在我认为分为两种，一种是防重复提交，一种是幂等。<font style="color:rgb(47, 48, 52);"> Param、SpELRest</font>、<font style="color:rgb(47, 48, 52);">Token</font> 都是前者，SpELMQ 是后者。只不过后者因为存储压力，它也只能防止一定时间的幂等，不过这也够了，因为真正的判断幂等一定是业务中有相关兜底方案，比如你插入一条数据，会判断是否存在。因为我们已经有前置条件了，所以不会有并发问题。这样是不是就能回答你的问题。



问题地址：

[https://t.zsxq.com/10siVRGiJ](https://t.zsxq.com/10siVRGiJ)



> 更新: 2023-07-29 13:36:04  
> 原文: <https://www.yuque.com/magestack/12306/zcz1zmdu3wxg84tl>