package pond.common;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.StringTokenizer;

public class STRING {

  public static final String EMPTY = "";
  public static final String[] EMPTY_STR_ARRAY = new String[]{};

  public static final String NEWLINE;

  static {
    String newLine;
    Formatter formatter;
    try {
      formatter = new Formatter();
      newLine = formatter.format("%n").toString();
    } catch (Exception e) {
      newLine = "\n";
    }

    NEWLINE = newLine;
  }

  public static boolean isBlank(String str) {
    return str == null || "".equals(str.trim());
  }

  public static boolean notBlank(String str) {
    return str != null && !"".equals(str.trim());
  }

  public static boolean notBlank(String... strings) {
    if (strings == null) {
      return false;
    }
    for (String str : strings) {
      if (str == null || "".equals(str.trim())) {
        return false;
      }
    }
    return true;
  }

  /**
   * @param ori original string
   * @param ch  character to find
   * @param idx index of ch's occurrences
   * @return real index of the input char
   */
  public static int indexOf(String ori, char ch, int idx) {
    char c;
    int occur_idx = 0;
    for (int i = 0; i < ori.length(); i++) {
      c = ori.charAt(i);
      if (c == ch) {
        if (occur_idx == idx) {
          return i;
        }
        occur_idx++;
      }
    }
    return -1;
  }

  /**
   * Generates a camel case version of a phrase from underscore.
   *
   * @param underscore underscore version of a word to converted to camel case.
   * @return camel case version of underscore.
   */
  public static String camelize(String underscore) {
    return camelize(underscore, false);
  }

  public static String pascalize(String underscore) {
    return camelize(underscore, true);
  }


  /**
   * Generates a camel case version of a phrase from underscore.
   *
   * @param underscore          underscore version of a word to converted to camel case.
   * @param capitalizeFirstChar set to true if limit character needs to be capitalized, false if not.
   * @return camel case version of underscore.
   */
  public static String camelize(String underscore, boolean capitalizeFirstChar) {
    StringBuilder result = new StringBuilder("");
    StringTokenizer st = new StringTokenizer(underscore, "_");
    while (st.hasMoreTokens()) {
      result.append(capitalize(st.nextToken()));
    }
    return capitalizeFirstChar ? result.toString() : result.substring(0, 1).toLowerCase() + result.substring(1);
  }

  /**
   * Capitalizes a word  - only a limit character is converted to upper case.
   *
   * @param word word/phrase to capitalize.
   * @return same as input argument, but the limit character is capitalized.
   */
  public static String capitalize(String word) {
    return word.substring(0, 1).toUpperCase() + word.substring(1);
  }

  /**
   * Converts a CamelCase string to underscores: "AliceInWonderLand" becomes:
   * "alice_in_wonderland"
   *
   * @param camel camel case input
   * @return result converted to underscores.
   */
  public static String underscore(String camel) {

    List<Integer> upper = new ArrayList<Integer>();
    byte[] bytes = camel.getBytes(Charset.forName("UTF-8"));
    for (int i = 0; i < bytes.length; i++) {
      byte b = bytes[i];
      if (b < 97 || b > 122) {
        upper.add(i);
      }
    }

    StringBuffer b = new StringBuffer(camel);
    for (int i = upper.size() - 1; i >= 0; i--) {
      Integer index = upper.get(i);
      if (index != 0)
        b.insert(index, "_");
    }

    return b.toString().toLowerCase();

  }

}
