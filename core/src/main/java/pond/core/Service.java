package pond.core;

import pond.common.f.Callback;

/**
 * Services are basic procedures provide atomic functions.
 *
 */
public class Service {

  final static int ST_NUL = 0;
  final static int ST_INIT  = 1;
  final static int ST_DONE = 9;

  final Callback<Context> main;
  String name = this.getClass().getCanonicalName();

  protected Context ctx;
  int state = ST_NUL;

  public Service(Callback<Context> main){
    this.main = main;
  }

  public String name(){
    return this.name;
  }

  public void name(String name) {
    this.name = name;
  }

  void init(Context ctx) {
    this.ctx = ctx;
    this.state = ST_INIT;
  }

  void call() {
    main.apply(ctx);
    this.state = ST_DONE;
  }

  @Override
  public String toString() {
    return "Service{" +
        "name='" + name + '\'' +
        ", state=" + state +
        '}';
  }
}
