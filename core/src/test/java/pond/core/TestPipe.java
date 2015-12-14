package pond.core;

import pond.common.S;

import java.io.*;

/**
 * Created by ed on 11/12/15.
 */
public class TestPipe {

  /*
  在单机上使用管道没有什么实际意思，我们可以使用更高级的IPC。
  私以为unix的管道概念更应该用于逻辑上的不同services，这些services有可能
  分布在不同的机器上，最终的实现形态有可能是这样的：
    0) service 共享一种通用的流格式，比如json 流
    1）service 按照不同的URL存取
    2）一个service的输出可以是另一个的输入

  另外一个显而易见的问题是：这是一个trivial的生产-消费模式，一旦消费的速率 < 生产的速率,
  很可能会产生死锁。
  **/

  public static void main(String[] args) {
    PipedInputStream pin = new PipedInputStream();
    PipedOutputStream pout = new PipedOutputStream();
    try {
      pin.connect(pout);

      new Thread(() -> {
        Reader reader = new InputStreamReader(pin);
        char[] buffer = new char[1024];
        int len;
        StringBuffer stringBuffer = new StringBuffer();
        try {
          while ((len = reader.read(buffer)) > 0) {
            stringBuffer.append(buffer, 0, len);

            int last_index_of_n = stringBuffer.lastIndexOf("\n");
            String[] raw_strings = stringBuffer
                .substring(0, last_index_of_n)
                .split("\n");

            S._for(raw_strings)
                .map(Double::parseDouble)
                .filter(d -> d > 0.9)
                .each(d -> System.out.println(d));

            String tail = stringBuffer.substring(last_index_of_n + 1);
            stringBuffer.setLength(0);
            stringBuffer.append(tail);
          }
        } catch (IOException e) {
          e.printStackTrace();
        }

      }).start();

      new Thread(() -> {
        while (true) {
          try {
            byte[] buf = String.valueOf(Math.random()).getBytes();
            pout.write(buf);
            pout.write('\n');
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }).start();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
