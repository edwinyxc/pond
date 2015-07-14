Pond - 灵活，快速的web开发框架 （基于java 8)
====

####pond-common 

pond-common 提供了一些函数式编程特性和一些有用的工具类

```java  
```  

* pond-core - 框架核心

> * IOC 容器。
  * 一套完整灵活的api
  * 嵌入式http服务器 (test)
  * 来自express.js的中间件设计，从此远离传统aop
  * 自带logger
  * 基于正则的路由
  * 采用SPI，方便扩展

```java  

    Pond app = Pond.init().debug();
    Router router = new Router();
    router.get("/add", (req, resp) -> resp.send("add"))
            .get("/del", (req, resp) -> resp.send("del"));

    app.get("/", (req, resp) -> {
        resp.send("root");
    }).get("/${id}",
            (req, resp) -> resp.send(req.param("id"))
    ).get("/${id}/text", (req, resp) -> {
        resp.send(S.dump(req));
    }).use("/user", router);

    app.listen(8080);
```
  
* pond-db - 数据库处理相关（默认提供mysql)

> * 简单的连接池
  * DB.fire 快速获取数据
  * Record 针对单表简单封装
  * RecordService 封装简单CURD
  
  
```java  

List<TestRecord> list =  DB.fire(this::createConnection,t ->
                                t.map(TestRecord.class,
                                "select * from t_crm_order"));
                                
echo(dump(_for(list).map(TestRecord::view).toList()));

```

    
###详情请参考项目内测试代码和注释
遇到坑了 ==>请联系 edwinyxc@gmail.com
