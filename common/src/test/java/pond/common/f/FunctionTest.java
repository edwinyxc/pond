package pond.common.f;

import org.junit.Test;
import pond.common.S;

import java.util.List;

import static org.junit.Assert.*;

public class FunctionTest {


  Function<Double, Integer> itod = i -> ((double)i) /3.0;

  void echo(List<Integer> input, Function<List<Double>, List<Integer>> a){
    S.echo(a.apply(input));
  }

  @Test
  public void testListProj() throws Exception {
    List<Integer> input = S.array(1,2,3,4,5,6,7,8,9,10);
    echo(input, itod.listProj());
  }
}