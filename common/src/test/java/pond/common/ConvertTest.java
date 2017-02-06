package pond.common;

import org.junit.Assert;
import org.junit.Test;


public class ConvertTest {

  @Test
  public void testToInt() throws Exception {
    Assert.assertEquals(true, 1 == Convert.toInt("1"));
    Assert.assertEquals(true, 1 == Convert.toInt(1));
  }

  @Test
  public void testToLong() throws Exception {
    Assert.assertEquals(true, 1 == Convert.toLong("1"));
    Assert.assertEquals(true, 1 == Convert.toLong(1));
  }

  @Test
  public void testToDouble() throws Exception {
    Assert.assertEquals(true, 2 == Convert.toDouble("2.000"));
    Assert.assertEquals(true, 2 == Convert.toDouble(2.000f));
    Assert.assertEquals(true, 2 == Convert.toDouble(2));
  }

  @Test
  public void testNullJoinArray() throws Exception {
    Object[] t = {null, null, null};
    String[] s  = S._for(t).<String>map(String::valueOf).join();

    Assert.assertEquals("null", s[0]);
    Assert.assertEquals("null", s[1]);
    Assert.assertEquals("null", s[2]);

//    Object[] _t = {};
//    s  = S._for(_t).<String>map(String::valueOf).join();
//    Assert.assertEquals(s.length, 0);
//    Assert.assertEquals(s.getClass(), "L[]");

  }

  @Test
  public void testToDate() throws Exception {

  }

  @Test
  public void testToDate1() throws Exception {

  }

  @Test
  public void testToString() throws Exception {

  }

  @Test
  public void testDateToLong() throws Exception {

  }

  @Test
  public void testToUnsigned() throws Exception {

  }

  @Test
  public void testToPrimitiveArray() throws Exception {

  }
}