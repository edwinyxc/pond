##Pond - 灵活，快速的web开发框架 （基于java 8)

###pond-common 提供了一些函数式编程特性和一些有用的工具类

* 函数式编程语言特性 -- 鉴于java坑大,并不打算严格的套用FP原理和概念,所以只是部分加入，够用就行
* 字符串处理
* 路径处理
* 常用数据结构  -- Matrix, Tree等
* 创新的debug配置模式 -- 脱离具体实现，在更高的层次上控制Logger
* 集成了Apache HttpClient 并做简单封装

Code Example:

    //debug
    S._debug(logger, log -> {
        Thread.sleep(3000) // do some heavy job
        log.debug("xxxxx");
    }

    //PATH
    assertEquals(PATH.classpathRoot(), S.class.getResource("/").getPath());

    //S.array -- easy creation
    Array<String> arr = S.array("This", "is", "A", "GOOD", "Day");
    assertArrayEquals(arr.map(str -> str.toUpperCase()).join(),
                      S.array("THIS", "IS", "A", "GOOD", "DAY").join());

    //Array#reduce -- fold function
    Array<Integer> arr = S.array(1, 23, 4, 5, 6, 7, 8, 9);
    assertEquals((long) arr.reduce((acc, cur) -> acc + cur), (1 + 23 + 4 + 5 + 6 + 7 + 8 + 9));

    //Array#filter -- filter function
    Array<Integer> arr = S.array(1, 23, 4, 5, 6, 7, 8, 9);
    assertArrayEquals(new int[]{23}, (int[]) Convert.toPrimitiveArray(arr.filter(x -> x > 20).join()));


###pond-web 面向web的快速开发工具

 * 来自express.js的API
 * 高度可制定 -- 您几乎可以在任何地方进行所谓aop编程
 * 默认采用Netty作为BaseServer -- netty 的并发性能足够好
 * 提供一个简单的静态文件服务 -- 不要使用在生产环境
 * 超级简单的filter

Code Example:

    //basic & config
    Pond.init(p -> p.loadConfig(new Properties()),
              p -> p.get("/123", (req, res) -> res.send("123"))
                  .get("/234", (req, res) -> res.send("234")),
              p -> p.get("/.*", p._static("www"))
    ).listen();

    //static
    Pond.init(p -> {
      p.get("/static/.*", p._static("www"));
    }).listen(9090);

    //uploadfile
    System.setProperty(BaseServer.PORT, "9090");
    Pond.init(p -> {
      p.post("/multipart", (req, resp) -> {
        Request.UploadFile f = req.file("content");
        resp.render(json("<pre>" + dump(req) + "</pre><br><pre>" + dump(f) + "</pre>"));
        resp.send(200, "OK");
      }).get("/.*", p._static("www"));
    }).listen();

    //debug
    S._debug_on(Pond.class, BaseServer.class);
    System.setProperty(BaseServer.PORT, "9090");
    Pond.init(p -> {
      p.post("/utf8test", (req, resp) -> {
        S.echo(S.dump(req.params()));
        resp.render(Render.dump(req.params()));
      }).get("/.*", p._static("www"));
    }).listen();

    //Session
    Mid session = Session.install();
    app.cleanAndBind(p -> {

      p.get("/require",
            session,
            Mid.wrap((req, resp) -> resp.send(200, "pass")).require(session)
      );

      p.get("/requireFail",
            Mid.wrap((req, resp) -> resp.send(200, "pass")).require(session)
      );

    });

    TestUtil.assertContentEqualsForGet("pass", "http://localhost:9090/require");
    HTTP.get("http://localhost:9090/requireFail", http -> {
      assertNotSame(200, http.getStatusLine().getStatusCode());
    });



###pond-db - 数据库处理相关（默认提供mysql)

 * 自带简易链接池
 * 不强加模型，只针对表和记录进行抽象 -- 可以非常快速的自定义模型
 * 自带Sql类，可以快速生成sql
 * 高并发性能和可配置的缓存机制

Code Example:

    //Model Definition
      public class BonusService extends Model{
          {
              db(App.getInstance().getDb());
              table("t_bonus_service");
              id("id");
              field("name");
              field("price");

          }
     //...

    //Model-based data retrieving
    List<Model> m = db.get(t -> t.query(Model.class, "select * from t_model"));

    //Concurrent Test
    Holder.AccumulatorInt val = new Holder.AccumulatorInt(0);
    ExecutorService executorService = Executors.newFixedThreadPool(20);
    List<CompletableFuture> futures = new ArrayList<>();

    long s = S.now();
    S._repeat(() -> {
      futures.add(CompletableFuture.runAsync(
          () -> {
            db.post(tmpl -> {
              tmpl.exec("USE POND_DB_TEST;");
              for (int i = 0; i < 400; i++)
                tmpl.exec("INSERT INTO test values(?,?)",
                    String.valueOf(Math.random()), String.valueOf(val.accum()));
            });

          }, executorService));
    }, 1000);

    try {
      CompletableFuture.allOf(S._for(futures).join()).thenRun(() -> {
        long beforeSelect = S.now();
        S._for((List<Record>) db.get(t -> {
          t.exec("USE POND_DB_TEST;");
          return t.query("SELECT * FROM test");
        }));
        S.echo("Query time:" + (S.now() - beforeSelect));
        S.echo("ALL FINISHED : time usage " + (S.now() - s));
      }).get();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    }


    
###详情请参考项目内测试代码和注释
遇到坑了 ==> 请联系 edwinyxc@outlook.com  QQ && WEIXIN: 326436387

欢迎大家一起来提修改意见 :) QQ群：206693019
