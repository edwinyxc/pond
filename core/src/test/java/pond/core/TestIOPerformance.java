package pond.core;

import pond.common.S;
import pond.common.STREAM;

import java.io.*;
import java.nio.channels.FileChannel;

public class TestIOPerformance {
  public static void main(String[] args) throws Exception {

    for (PipeTestCase testCase : testCases) {
      System.out.println(testCase.getApproach());
      InputStream is = new FileInputStream("/Users/ed/Desktop/Red_Hat_Enterprise_Linux-6-Security-Enhanced_Linux-en-US.pdf");
      OutputStream os = new FileOutputStream("/tmp/out.pdf");

      long start = System.currentTimeMillis();
      testCase.pipe(is, os);
      long end = System.currentTimeMillis();

      System.out.println("Execution Time = " + (end - start) + " millis");
      System.out.println("============================================");

      is.close();
      os.close();
    }

  }

  private static PipeTestCase[] testCases = {

      new PipeTestCase("Fixed Buffer Read") {
        @Override
        public void pipe(InputStream is, OutputStream os) throws IOException {
          byte[] buffer = new byte[131072];
          while (is.read(buffer) > -1) {
            os.write(buffer);
          }
        }
      },

      new PipeTestCase("STREAM.pipe") {
        @Override
        public void pipe(InputStream is, OutputStream os) throws IOException {
          STREAM.pipe(is, os);
        }
      },

      new PipeTestCase("AKKA pipe") {
        @Override
        public void pipe(InputStream is, OutputStream os) throws IOException {


        }
      },

      new PipeTestCase("MultiThread pipe") {
        @Override
        public void pipe(InputStream is, OutputStream os) throws IOException {

          long time_thread = System.currentTimeMillis();
          PipedInputStream pin = new PipedInputStream();
          PipedOutputStream pout = new PipedOutputStream();
          pin.connect(pout);

          time_thread = System.currentTimeMillis() - time_thread;
          S.echo("Pipes creation usage:" + time_thread);

          time_thread = System.currentTimeMillis();

          new Thread(() -> {
            byte[] buf = new byte[1024];
            int len;
            try {
              // when data come in
              while ((len = pin.read(buf)) > 0) {
                os.write(buf, 0, len);
              }

            } catch (IOException e) {
              e.printStackTrace();
            }
          }).start();
          time_thread = System.currentTimeMillis() - time_thread;
          S.echo("Thread creation usage:" + time_thread);

          //this thread
          byte[] buffer = new byte[1024];
          int length;
          try{
            while ((length = is.read(buffer)) > 0) {
              pout.write(buffer, 0, length);
            }
          }catch (IOException e){
            e.printStackTrace();
          }finally {
            pin.close();
            pout.close();
          }
        }
      },

      new PipeTestCase("dynamic Buffer Read") {
        @Override
        public void pipe(InputStream is, OutputStream os) throws IOException {
          byte[] buffer = new byte[is.available()];
          while (is.read(buffer) > -1) {
            os.write(buffer);
            buffer = new byte[is.available() + 1];
          }
        }
      },

      new PipeTestCase("Byte Read") {
        @Override
        public void pipe(InputStream is, OutputStream os) throws IOException {
          int c;
          while ((c = is.read()) > -1) {
            os.write(c);
          }
        }
      },

      new PipeTestCase("NIO Read") {
        @Override
        public void pipe(InputStream is, OutputStream os) throws IOException {
          FileChannel source = ((FileInputStream) is).getChannel();
          FileChannel destnation = ((FileOutputStream) os).getChannel();
          destnation.transferFrom(source, 0, source.size());
        }
      },

  };
}


abstract class PipeTestCase {
  private String approach;

  public PipeTestCase(final String approach) {
    this.approach = approach;
  }

  public String getApproach() {
    return approach;
  }

  public abstract void pipe(InputStream is, OutputStream os) throws IOException;
}
