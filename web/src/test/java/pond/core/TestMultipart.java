package pond.core;

import pond.common.S;
import pond.core.spi.BaseServer;

import java.io.File;

import static pond.common.S.dump;

/**
 * Created by ed on 15-7-9.
 */
public class TestMultipart {

    public static void main(String[] args) {
        S._debug_on(Pond.class, BaseServer.class);
        System.setProperty(BaseServer.PORT, "9090");
        Pond.init(p -> {
            p.post("/multipart", (req, resp) -> {
                Request.UploadFile f  = req.file("content");
                //resp.render(Render.json("<pre>"+dump(req)+  "</pre><br><pre>" + dump(f) +"</pre>"));
                resp.send(200,"OK");
            }).get("/.*", p._static("www"));
        }).listen();
    }
}
