# Windows系统启动聚合服务报错 Command line is too long Shorten the command line and rerun

该问题仅 Windows 系统电脑会出现。

问题现状：

![1691204207160-af495b10-eb96-4ea4-976f-f2bbc4575324.png](./img/Pz6Puzw3QpZ2tBUo/1691204207160-af495b10-eb96-4ea4-976f-f2bbc4575324-581700.png)



如何解决？

1）打开服务控制器。

![1691204374074-dea7afc5-8e11-416f-9e2f-5571d874ff70.png](./img/Pz6Puzw3QpZ2tBUo/1691204374074-dea7afc5-8e11-416f-9e2f-5571d874ff70-576484.png)



选择聚合服务 `AggregationServiceApplication`，点击 `Modify options`，点击 `shorten command line`。

选择 `JAR manifest`，问题解决。

![1691204461897-6422440d-99e9-4cf5-9677-949474fb22d6.png](./img/Pz6Puzw3QpZ2tBUo/1691204461897-6422440d-99e9-4cf5-9677-949474fb22d6-878880.png)



> 更新: 2023-08-05 11:01:27  
> 原文: <https://www.yuque.com/magestack/12306/vrn49fm2afegp0ug>