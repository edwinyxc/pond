0.2.1 /2016-01-13
==================


  * [Core] 增加了Context.current();

      - 用于在任何地方获取最近的Context
      
  * [Security] 增加了2模块 RBAC（基于角色的身份验证）和 BA（Http Basic Authentication)    
        
       - RBAC
       
       
       - BA
       
        HttpBasicAuth ba = new HttpBasicAuth("basic")
            .validator((user, pass) -> "user".equals(user) && "pass".equals(pass)
        );
        
        Pond.init().debug(HttpBasicAuth.class).cleanAndBind( 
            p -> p.get("/secret", ba.auth, (req, resp) -> {
                resp.send(200, "Welcome " + ba.user(req.ctx()));
            })
        ).listen(9090);
       
       
     
       
0.2.0 / 2015-12-07
==================

  * [Core] 增加了pond-core模块

      - 该模块提供了一套抽象的 服务,执行,上下文 基础类用来实现一些高层次的执行控制

      - 例如权限， 工作流等

  * [Web] 删除了pond-web-acl模块

      - 没用的东西

  * [Web] 现在pond-web模块依赖于pond-core模块

      - mid 可以方便地转换成 core-service

  * [Security] 增加了pond-security模块

      - 依赖于pond-core

0.1.6 / 2015-10-04
==================

  * [Web] Normalize path-pattern to regexp

      - Path-like strings will be matched by a universal regexp-path matcher correctly.

  * [Web] Add a new SPI: PathToRegCompiler

      - Path-like strings will be pre-compiled by a user-defined PathToRegCompiler.
      - We provide a default implementation, ExpressPathToRegCompiler , using the same syntax to Express.js.
      - Also, a fall-back implementation,  FallbackPathToRegCompiler is still available for those remaining projects using old Pond( ver <= 0.1.6 ).
      - You can always using a raw reg-exp in your path definition.

0.1.5 / 2015-9-20
==================

  * [Global] Rename core to web
  * [Web] Stabilise http server implements with netty
  * [Web] Change Routing algorithms

      - Now the Ctx-Executor will exec one and only one middleware at once in each thread.
        Executor does not have any information of whether there is a next or not.
        The pipeline is configured by the middleware itself.

      - The Router is now a regular middleware.
        Routers handle the executing chain by the state of Ctx.

