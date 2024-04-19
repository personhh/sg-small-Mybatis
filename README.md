# 手写Mybatis源码——一次深度探索源码的心得

曾几何时，我一度热爱阅读源码，我总保持一个观点，对于技术，光会用远远不够，深度探索它为什么这么用才是提升进阶的关键！！

## 工程结构：

！！工程分为四部分，从基础搭建->基础功能点实现->工程解藕->扩展实现

##项目意义
Mybatis框架作为最主流的数据库开发的轻量级开发框架，需要较深入的学习和使用，通过手写框架，提高了对Mybatis底层原理的理解，同时也学习了Mybatis工程的结构以及使用的设计模式，加强设计思维和编码能力，从而可以更好的使用Mybatis解决日常场景的问题、处理业务逻辑，更好更高效的工作。

**————第一部分：基础搭建工程**

sg-small-mybatis-01：搭建初步映射器代理工厂

sg-small-mybatis-02：实现映射器的注册和使用

sg-small-mybatis-03：对Mapper XML解析和注册使用

**————第二部分：初步实现工程功能点**

sg-small-mybatis-04：创建数据源、解析、使用

sg-small-mybatis-05：数据源池化技术实现

sg-small-mybatis-06：sql执行器的定义和实现

sg-small-mybatis-07：反射获取属性，提供工程灵活性

**————第三部分：流程解藕串联**

sg-small-mybatis-08：细化XML语句构建器，完善静态sql解析过程

sg-small-mybatis-09：策略模式进行工程解藕，调用参数处理器

sg-small-mybatis-10：封装结果集处理器

sg-small-mybatis-11：基础实现增删改查

**————第四部分：扩展实现**

sg-small-mybatis-12：注解配置

sg-small-mybatis-13：ResultMap实现

sg-small-mybatis-14：返回insert操作自增索引值

sg-small-mybatis-15：动态SQL语句实现

sg-small-mybatis-16：Plugin插件扩展

sg-small-mybatis-17：一级缓存

sg-small-mybatis-18：二级缓存


**工程部分流程图
简单概括一下主要的流程便于理解，有补充后续会补上

1.搭建基础工程流程
![image](https://github.com/personhh/sg-small-Mybatis/assets/139620514/79510f5c-7375-404b-a514-a9edac5d5b95)

2.数据源池化技术底层
![image](https://github.com/personhh/sg-small-Mybatis/assets/139620514/52184a49-eef6-4135-9d27-2177bd892baa)

4.反射流程
![image](https://github.com/personhh/sg-small-Mybatis/assets/139620514/3bc6dbc4-e33f-40a8-8f76-ab552b34ada0)

5.基础功能点实现流程
![image](https://github.com/personhh/sg-small-Mybatis/assets/139620514/7b89a8eb-19fb-4af0-ba04-071a69712095)

6.主要扩展点之一二级缓存流程
![image](https://github.com/personhh/sg-small-Mybatis/assets/139620514/d5ceaa08-f398-4554-985d-b05c7d7222a7)

还有很多流程图，为了读者方便，我就贴上几个主要的
