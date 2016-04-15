#Pond

A flexible Java develop framework.

##pond-web

    //
    Pond.init( app -> {
        //responsive
        app.post("/echo_json", (req, resp) -> {
            resp.render(Render.json(req.toMap()));
        });
        //session
        app.get("/test", session, (req, resp) -> resp.send(200, Session.get(req).id));

        //use come controllers
        app.use("/ctrl/*", new DemoController());

        //middleware design inspired by Express.js
        app.get("/*, mid_a, mid_b, (req, resp) -> resp.header("x-powered-by", "pond"),
                //simple static file server
                app._static("www")
        );

    }).listen(8080);

##pond-web-security

    HttpBasicAuth ba = new HttpBasicAuth("basic")
        .validator((user, pass) -> "user".equals(user) && "pass".equals(pass)
        );

    Pond.init()
    .debug(HttpBasicAuth.class)
    .cleanAndBind(
        p -> p.get("/secret", ba.auth, (req, resp) -> {
          resp.send(200, "Welcome " + ba.user(req.ctx()));
        })
    ).listen(9090);

##pond-db

    DataSource ds = ConnectionPool.c3p0(ConnectionPool.local("test"));

    //create a DB abstraction
    DB db = new DB(ds);

    //easy Record template
    class someModel extends Model{
        {
            id("id");
            table("t_some_table")
            field(".//..")

            //reactive getter & setter
            field("age").init(new Date())
            .db(d -> d.getTime().toString())
            .view(s -> new Date(s))
        }
    }

    //do some simple query
    List result = db.get("select * from t_some_table");

    //batch some DDLs
    db.batch("DROP TABLE IF EXISTS p_test",
    "CREATE TABLE p_test (id varchar(60), percent varchar(60), title varchar(60))");

    //full-featured query builder
    SqlSelect select=Sql.select("*").from(someModel.class)
                .where("sn", Criterion.EQ, sn)
                .where("model",Criterion.EQ, model)
                .where("version",Criterion.BETWEEN, "sa", "sz")
                .orderBy("date desc")

    //Simple JDBCTmpl and nothing else
    db.post(t -> t.recordInsert(Record));


    //Painless transactions
    db.get(t -> {
        t.raw("USE DB_1");
        list_from_db_1 = t.query("select * from test_1");
        t.raw("USE DB_2");
        list_from_db_2 = t.query("select * from test_2");
        t.raw("USE DB_1");
    });

    db.post(t -> {
            //a post is a transaction
            List<A> listA = t.query(A.class, someSql);
            List<B> listB = t.query(B.class, someSql);
            S._for(listA)
                .filter(listB::contains)
                .map(a -> a.set("user_id", xxx))
                .forEach(a -> t.recordInsert(a));
    });

    //dialects support (MySql by default)
    db = new DB(ds);
    db = new DB(ds, Dialect.h2);
    db = new DB(ds, Dialect.oracle);
    db = new DB(ds, Dialect.mssql);


##pond-web-restful

    //fast restful includes
    // GET --> /test?field=&a=btn,from,to&offset=4&limit=5&sord=+user,-a,-b
    // POST --> /test?
    // PUT --> /test?id=id&field=newValue
    // DELETE --> /test?id=id
    app = Pond.init(p -> {
      p.use("/test/*", new RestfulRouter<>(TestModel.class, db));
    }).listen(9091);









