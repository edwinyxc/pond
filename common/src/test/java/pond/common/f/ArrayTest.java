package pond.common.f;

import org.junit.Test;
import pond.common.Convert;
import pond.common.S;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ArrayTest {

  @Test
  public void testJoin() throws Exception {
    Integer[] joined = S.array(12, 2, 3, 14, 15).join();
    assertArrayEquals(new int[]{12, 2, 3, 14, 15},
                      (int[]) Convert.toPrimitiveArray(joined));

    joined = S.array(12, 2, 3, 14, 15).join(0);
    assertArrayEquals(new int[]{12, 0, 2, 0, 3, 0, 14, 0, 15},
                      (int[]) Convert.toPrimitiveArray(joined));
  }


  @Test
  public void testMap() throws Exception {
    Array<String> arr = S.array("This", "is", "A", "GOOD", "Day");
    assertArrayEquals(arr.map(str -> str.toUpperCase()).join(),
                      S.array("THIS", "IS", "A", "GOOD", "DAY").join());
  }

  @Test
  public void testReduce() throws Exception {
    Array<Integer> arr = S.array(1, 23, 4, 5, 6, 7, 8, 9);
    assertEquals((long) arr.reduce((acc, cur) -> acc + cur), (1 + 23 + 4 + 5 + 6 + 7 + 8 + 9));
  }

  @Test
  public void testFilter() throws Exception {
    Array<Integer> arr = S.array(1, 23, 4, 5, 6, 7, 8, 9);
    assertArrayEquals(new int[]{23}, (int[]) Convert.toPrimitiveArray(arr.filter(x -> x > 20).join()));
  }

  @Test
  public void testEach() throws Exception {
    Array<Integer> arr = S.array(1, 2, 3, 4, 5, 6, 7, 8, 9);
    AtomicInteger sum = new AtomicInteger(0);
    arr.each(sum::getAndAdd);
    assertEquals(45, (long) sum.get());
  }

  @Test
  public void testReverse() throws Exception {
    Array<Integer> arr = S.array(1, 2, 3, 4, 5, 6, 7, 8, 9);
    assertArrayEquals(new int[]{9, 8, 7, 6, 5, 4, 3, 2, 1}, (int[]) Convert.toPrimitiveArray(arr.reverse().join()));
  }

  @Test
  public void testConcat() throws Exception {
    Array<Integer> arr1 = S.array(1, 2, 3, 4, 5, 6);
    Array<Integer> arr2 = S.array(7, 8, 9);
    assertArrayEquals(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9}, (int[]) Convert.toPrimitiveArray(arr1.concat(arr2).join()));
  }

  @Test
  public void testFirst() throws Exception {
    Array<Integer> arr = S.array(1, 2, 3, 4, 5, 6, 7, 8, 9);
    assertEquals((int) arr.first(), 1);
  }


  @Test
  public void testLimit() throws Exception {
    Array<Integer> arr = S.array(1, 2, 3, 4, 5, 6, 7, 8, 9);
    assertArrayEquals(new int[]{1, 2, 3}, (int[]) Convert.toPrimitiveArray(arr.limit(3).join()));
  }

  @Test
  public void testFind() throws Exception {
    Array<Integer> arr = S.array(1, 2, 3, 4, 5, 6, 7, 8, 9);
    assertEquals((int) arr.find(x -> x > 2), 3);
  }

  @Test
  public void testSome() throws Exception {
    Array<Integer> arr = S.array(1, 2, 3, 4, 5, 6, 7, 8, 9);
    assertEquals(false, arr.some(x -> x > 10));
  }

  @Test
  public void testEvery() throws Exception {
    Array<Integer> arr = S.array(1, 2, 3, 4, 5, 6, 7, 8, 9);
    assertEquals(true, arr.every(x -> x >= 1));
    assertEquals(false, arr.every(x -> x > 1));
    assertEquals(false, arr.every(x -> x > 4));

    Array<Integer> arr2 = S.array(1, 2);

    assertEquals(false, arr2.every(x -> x > 4));
    assertEquals(true, arr2.every(x -> x > 0));
  }

  @Test
  public void testPartition() {
    Array<Integer> arr = S.array(1, 2, 3, 4, 5, 6, 7, 8, 9);
    FIterable.Partition<Integer> p = arr.partition(x -> x > 5);
    assertArrayEquals((int[]) Convert.toPrimitiveArray(p._true().join()), new int[]{6, 7, 8, 9});
    assertArrayEquals((int[]) Convert.toPrimitiveArray(p._false().join()), new int[]{1, 2, 3, 4, 5});
  }
}