package pond.common.cui;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestRichLayout {
  @Test
  public void main() {
    assertEquals(RichLayout.horizontal(
            new Rect(new String[]{
                "123123", "-----------", "sdsds"}),
            new Rect(new String[]{
                "123123sdsd", "-----------", "sdsdas", "23123", "sdas"}),
            new Rect(new String[]{"123123sdsd", "---xxxxsdsd",
                "sdasd56^&*("})
        ).toString(),

        "123123     123123sdsd 123123sdsd \n"
            + "-------------------------xxxxsdsd\n"
            + "sdsds      sdsdas     sdasd56^&*(\n"
            + "           23123                 \n"
            + "           sdas                  \n"
    );
  }
}
