package pond.common;

import pond.common.config.Config;

/**
 * Created by ed on 8/22/15.
 */
public class TestConfig {


  public static void main(String[] args) {

    S.config.set(S.class, "maxSize", "10");
    S.config.set(S.class, "maxSize", "11");
    S.config.set(S.class, "maxSize", "11");
    S.config.set(S.class, "ClassName", "S");


    S.echo(JSON.stringify(Config.system.all()));
    S.echo(JSON.stringify(Config.system.all("file")));
    S.echo(JSON.stringify(Config.system.all("js")));
    S.echo(JSON.stringify(Config.system.all(java.io.File.class)));
    S.echo(JSON.stringify(Config.system.all(S.class)));

  }

}
