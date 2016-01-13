package pond.core;

import org.junit.Assert;
import org.junit.Test;
import pond.common.S;

public class TestExecution {

  Service simple_add = new Service(ctx -> {
    int a = (int) ctx.get("$1");
    int b = (int) ctx.get("$2");
    ctx.push(a + b);
  });

  Service fold_add_ten = new Service(ctx -> {
    int last = (int) ctx.pop();
    ctx.push(last + 10);
  });


  @Test
  public void register() {
    Context ctx = new Context("user");

    Services.add("fold_add_ten", fold_add_ten);
    Services.add("simple_add", simple_add);

    //dynamic execution procedure
    S.echo("time usage:" + S.time(() -> {
      //test fold
      ctx.push(10);
      S.echo(ctx.exec(
          Services.get("fold_add_ten"),
          Services.get("fold_add_ten"),
          Services.get("fold_add_ten"),
          new Service(c -> {
            c.set("$1", 12);
            c.set("$2", 14);
          }),
          Services.get("simple_add"),
          new Service(c -> {
            int add_result = (int) c.pop();
            S.echo(add_result);
            int other_result = (int) c.pop();
            S.echo(other_result);
            c.push(add_result + other_result);
          })
      ));
    }));

  }

  @Test
  public void localCurrentCtx() {

    Context ctx = new Context("tmp");
    new Thread(() -> {
      ctx.push(10);
      ctx.exec(fold_add_ten, fold_add_ten, new Service(c -> {
        Assert.assertEquals(ctx, Context.current());
      }));
    }).run();
  }

  @Test
  public void basic() {

    Context ctx = new Context("user");
    //dynamic execution procedure
    S.echo("time usage:" + S.time(() -> {
      //test fold
      ctx.push(10);
      S.echo(ctx.exec(
          fold_add_ten,
          fold_add_ten,
          fold_add_ten,
          fold_add_ten,
          new Service(c -> {
            c.set("$1", 12);
            c.set("$2", 14);
          }),
          simple_add,
          new Service(c -> {
            int add_result = (int) c.pop();
            S.echo(add_result);
            int other_result = (int) c.pop();
            S.echo(other_result);
            c.push(add_result + other_result);
          })
      ));
    }));
  }

}
