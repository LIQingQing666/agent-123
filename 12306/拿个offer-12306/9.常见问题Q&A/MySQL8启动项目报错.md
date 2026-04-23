# MySQL8启动项目报错

在 MySQL JDBC 参数后面加上 `&allowPublicKeyRetrieval=true` 尝试下，MySQL8 的数据库密钥验证方式和 5.7 有点不一样。

示例如下：

```java
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root
    password: root
    url: jdbc:mysql://127.0.0.1:3306/12306_ticket?characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&transformedBitIsBoolean=true&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true
    hikari:
      connection-test-query: select 1
      connection-timeout: 20000
      idle-timeout: 300000
      maximum-pool-size: 5
      minimum-idle: 5
```



> 更新: 2024-10-28 11:05:26  
> 原文: <https://www.yuque.com/magestack/12306/cmu0h0kkv6a1dhom>