package pond.common;

import org.junit.Test;
import pond.common.f.Array;
import pond.common.f.Holder;

import java.nio.channels.UnsupportedAddressTypeException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class STest {

  @Test
  public void test_tap() {
    assertEquals(S._tap(new HashMap<>(), map -> map.put("test", 1)).get("test"), 1);
  }

  @Test
  public void test_unwrapRuntimeException() {
    RuntimeException e
        = new RuntimeException(
        new RuntimeException(
            new RuntimeException(new Exception("here"))));
    assertEquals("here", S.unwrapRuntimeException(e, true).getMessage());
  }

  @Test
  public void test_repeat() {
    AtomicInteger acc = new AtomicInteger(10);
    S._repeat(acc::incrementAndGet, 10);
    assertEquals(acc.get(), 20);
  }

  @Test
  public void test_getOrSet() throws Exception {
    Map map = new HashMap<>();
    S._getOrSet(map, "e", "set!");
    assertEquals("set!", map.get("e"));
  }

  @Test(expected = Exception.class)
  public void test_fail() throws Exception {
    S._fail();
  }

  @Test
  public void test_avoidNull() throws Exception {
    assertEquals("else", S.avoidNull(null, "else"));
  }

  @Test
  public void test_array_join() {
    String[] arr = S.array.of(new ArrayList<String>() {{
      this.add("a");
      this.add("b");
      this.add("c");
      this.add("d");
    }});
    assertArrayEquals(new String[]{"a", "b", "c", "d"}, arr);
  }

  @Test
  public void test_in() {
    assertEquals(S._in("a", "a", "b", "c"), true);
  }

  @Test(expected = Exception.class)
  public void test_try_ret() {
    String a = S._try_ret(() -> {
      throw new Exception();
    });
  }

  @Test
  public void test_try() {
    assertEquals("a", S._try_ret(() -> "a"));
  }

  @Test
  public void testAuthor() throws Exception {

  }

  @Test
  public void testVersion() throws Exception {

  }

  @Test(expected = RuntimeException.class)
  public void test_assert() throws Exception {
    S._assert(false);
  }

  @Test(expected = RuntimeException.class)
  public void test_assert1() throws Exception {
    S._assert(false, "runtime assert failure");
  }


  @Test(expected = Exception.class)
  public void test_throw() throws Exception {
    S._throw(new UnsupportedAddressTypeException());
  }

  @Test
  public void testAvoidNull() throws Exception {
    assertEquals("a", S.avoidNull(null, "a"));
  }

  @Test
  public void test_tap_nullable() throws Exception {
    String a = S._tap_nullable(null, s -> {
      S.echo("this will not be executed");
      return "a";
    });
    Map map = new HashMap<>();
    map.put("as", "as");
    assertEquals("as", S._tap_nullable(map, m -> m.get("as")));
  }

  @Test
  public void test_range() throws Exception {
    Array<Integer> arr = S.range(1, 5);
    assertArrayEquals((int[]) Convert.toPrimitiveArray(arr.join()),
                      new int[]{1, 2, 3, 4, 5});
  }

  @Test
  public void test_for() throws Exception {

  }

  @Test
  public void test_for1() throws Exception {

  }

  @Test
  public void test_for2() throws Exception {

  }

  @Test
  public void test_for3() throws Exception {

  }

  @Test
  public void test_one() throws Exception {

  }


  @Test
  public void testEcho() throws Exception {

  }

  @Test
  public void testDump() throws Exception {

  }

  @Test
  public void testList() throws Exception {

  }

  @Test
  public void testTime() throws Exception {

  }

  @Test
  public void testNow() throws Exception {

  }

  @Test
  public void testNow_nano() throws Exception {

  }

  @Test
  public void testTime1() throws Exception {

  }

  @Test
  public void testTime_nano() throws Exception {

  }
}
