//package pond.web;
//
//import io.netty.buffer.ByteBufInputStream;
//import io.netty.handler.codec.http.multipart.FileUpload;
//
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStream;
//
//public class NettyUploadFile implements Request.UploadFile {
//  FileUpload file;
//
//  NettyUploadFile(FileUpload nettyUpload) {
//    file = nettyUpload;
//  }
//
//  @Override
//  public String name() {
//    return file.getName();
//  }
//
//  @Override
//  public String filename() {
//    return file.getFilename();
//  }
//
//  @Override
//  public InputStream inputStream() throws IOException {
//    return new ByteBufInputStream(file.getByteBuf()) {
//      @Override
//      public void close() throws IOException {
//        super.close();
//        if (file.refCnt() > 0) file.release();
//      }
//    };
//  }
//
//  @Override
//  public File file() throws IOException {
//    return file.getFile();
//  }
//
//  @Override
//  public String toString() {
//    StringBuilder dump = new StringBuilder(super.toString()).append("\n");
//    dump.append("ORIGIN FILE NAME: ").append(this.filename()).append("\n");
//    dump.append("NAME: ").append(this.name()).append("\n");
//    try {
//      dump.append("PATH: ").append(this.file().getAbsoluteFile()).append("\n");
//    } catch (IOException e) {
//      BaseServer.logger.error(e.getMessage(), e);
//    }
//    return dump.toString();
//  }
//}
//
