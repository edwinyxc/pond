package pond.common;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import static pond.common.S._assert;

public class PATH {

  /**
   * get the path of specified input class
   */
  @SuppressWarnings("rawtypes")
  public static String get(Class clazz) {
    String path = clazz.getResource("").getPath();
    return new File(path).getAbsolutePath();
  }


  public static String classpathRoot() {
    return classpathRoot(S.class);
  }

  public static String classpathRoot(Class clazz) {
    S._assert(clazz);
    URL resource = clazz.getResource("/");
    if (resource == null)
      throw new RuntimeException(
          "cannot get resource / of " + clazz.getCanonicalName());
    return resource.getPath();
  }

  /**
   * <p>
   * Normally return the source dir path under the current project
   * <br>
   * <strong>It is important to put the build folder under the project root directly
   * </strong>
   * </p>
   *
   * @return the source dir path under the current project
   */
  @Deprecated
  public static String detectWebRootPath() {
    try {
      String path = S.class.getResource("/").toURI().getPath();

      return new File(path).getParentFile().getParentFile().getCanonicalPath();
    } catch (URISyntaxException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static Boolean isAbsolute(String path) {
    _assert(path);
    //unix & windows platform
    return path.startsWith("/") || path.indexOf(":") == 1;
  }

}
