<<<<<<< HEAD
//package pond.web;
//
//import pond.common.STREAM;
//
//import java.io.IOException;
//
///**
// * Created by ed on 2/3/16.
// */
//public class ManualTestForHttpMultipart {
//
//  public static void main(String[] args){
//    Pond.init(
//        p -> {
//          p.post("/bodyAsMultipart", (req, resp) -> {
//            Request.UploadFile f = req.file("content");
//            try {
//              STREAM.pipe(f.bodyAsInputStream(), resp.out());
//            } catch (IOException e) {
//              e.printStackTrace();
//            }
//
//            resp.send(200);
//          }).get("/*",p._static("www"));
//        }
//    ).debug().listen();
//  }
//
//}
=======
//package pond.web;
//
//import pond.common.STREAM;
//
//import java.io.IOException;
//
///**
// * Created by ed on 2/3/16.
// */
//public class ManualTestForHttpMultipart {
//
//  public static void main(String[] args){
//    Pond.init(
//        p -> {
//          p.post("/bodyAsMultipart", (req, resp) -> {
//            Request.UploadFile f = req.file("content");
//            try {
//              STREAM.pipe(f.bodyAsInputStream(), resp.out());
//            } catch (IOException e) {
//              e.printStackTrace();
//            }
//
//            resp.send(200);
//          }).getEntry("/*",p._static("www"));
//        }
//    ).debug().listen();
//  }
//
//}
>>>>>>> 75d7834a3a3764cae57a5abfee30e9ce2f88590c
