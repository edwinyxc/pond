package com.shuimin.jtiny.codec.upload;

import com.shuimin.base.S;
import com.shuimin.base.f.Callback;
import com.shuimin.base.f.Function;
import com.shuimin.jtiny.core.AbstractMiddleware;
import com.shuimin.jtiny.core.ExecutionContext;
import com.shuimin.jtiny.core.Global;
import com.shuimin.jtiny.core.Server;
import com.shuimin.jtiny.core.exception.YException;
import com.shuimin.jtiny.core.http.Request;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.UploadContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.shuimin.base.S.*;
import static com.shuimin.jtiny.core.ExecutionContext.RESP;
import static com.shuimin.jtiny.core.Interrupt.kill;
import static com.shuimin.jtiny.core.Interrupt.render;
import static com.shuimin.jtiny.core.Server.G.debug;
import static com.shuimin.jtiny.core.misc.Renderable.text;

/**
 * Created by ed on 2014/4/22.
 */

public class FileUploadServer extends AbstractMiddleware {


    private File getTmpDir() {
        File ret = new File(webRoot.apply() + File.separator + "tmp_upload");
        if (!ret.exists()) {
            ret.mkdirs();
        }
        if (!ret.isDirectory()) {
            throw new RuntimeException(ret + "is not a dir");
        }
        if (!ret.canWrite()) {
            throw new RuntimeException(ret + "cant write");
        }
        return ret;
    }

    private File getUploadDir() {
        File ret = new File(webRoot.apply() + File.separator + "upload");
        if (!ret.exists()) {
            ret.mkdirs();
        }
        if (!ret.isDirectory()) {
            throw new RuntimeException(ret + "is not a dir");
        }
        if (!ret.canWrite()) {
            throw new RuntimeException(ret + "cant write");
        }
        return ret;
    }

    /***
     * providers
     */

    private Function._0<File> uplaodDir = this::getUploadDir;

    private Function._0<File> tmpDir = this:: getTmpDir;


    public int maxUploadSize = 1024 * 1024 * 200;

    public Set<String> allowedFileExts = S.collection.set.hashSet(
        new String[]{"jpg", "zip", "txt", "ppt", "pptx", "doc", "docx", "xls", "gif"});

    private Callback._0 onInvalidType= () ->{
        RESP().write("only " + dump(allowedFileExts) + " are allowed");
        RESP().send(403);
        kill();
    };
    
    public FileUploadServer onInvalidType(Callback._0 cb) {
        this.onInvalidType = cb;
        return this;
    }

    private Function._0<String> webRoot = () -> (String)Server.config(Global.ROOT);

    private Callback._2<String, InputStream> onUploadFile = (name, in) -> {
        debug("Upload filename : " + name);
        File f;
        try (FileOutputStream out = new FileOutputStream((f = new File(uplaodDir.apply(), name)))) {
            S.stream.write(in, out);
            render(text("File[" + name + "] uploaded, location:" + f.getAbsolutePath()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    };


    private Callback._2<String, String> onNormal = (name, val) -> {
        debug("received name = " + name + " value = " + val);
    };

    public FileUploadServer upload(Callback._2<String, InputStream> onUpload) {
        this.onUploadFile = onUpload;
        return this;
    }

    public FileUploadServer allowedExts(String... x) {
        allowedFileExts.addAll(Arrays.asList(x));
        return this;
    }

    public FileUploadServer normal(Callback._2<String, String> cb) {
        this.onNormal = cb;
        return this;
    }


    public FileUploadServer uploadDir(String str) {
        String absPath;

        if (str.startsWith("/") || str.indexOf(":") == 1) {
            absPath = str;
        } else {
            absPath = webRoot.apply() + File.separator + str;
        }

        File f = new File(absPath);

        if (f == null || !f.exists() || !f.canWrite()) {
            throw new YException(this) {
                @Override
                public String brief() {
                    return "File[" + absPath + "] not valid";
                }
            };
        }
        this.uplaodDir = ()-> f;
        return this;
    }


    private class ReqCtx implements UploadContext{
        String encoding;
        String contentType;
        long contentLength;
        InputStream in;

        public ReqCtx(Request req) {
            this.encoding = _notNullElse(req.characterEncoding(),"UTF-8");
            this.contentType = req.header("Content-Type")[0];
            this.contentLength = S.parse.toLong(req.header("Content-Length")[0]);
            try {
                this.in = req.in();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public String getCharacterEncoding() {
            return encoding;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public int getContentLength() {
            return (int)contentLength;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return in;
        }

        @Override
        public String toString() {
            return "$ReqCtx{" +
                "encoding='" + encoding + '\'' +
                ", contentType='" + contentType + '\'' +
                ", contentLength=" + contentLength +
                '}';
        }

        @Override
        public long contentLength() {
            return contentLength;
        }
    }

    @Override
    public ExecutionContext handle(ExecutionContext ctx) {
        Request req = ctx.req();
        echo(req.headers());
        RequestContext reqCtx = new ReqCtx(req);

        debug(reqCtx.toString());

        if (!ServletFileUpload.isMultipartContent(reqCtx)) {
            return ctx;
        }
        DiskFileItemFactory factory =
            new DiskFileItemFactory();

        factory.setSizeThreshold(4 * 1024);

        factory.setRepository(tmpDir.apply());

        ServletFileUpload upload = new ServletFileUpload(factory);

        upload.setHeaderEncoding("UTF-8");

        upload.setFileSizeMax(maxUploadSize);

        try {

            List<FileItem> items = upload.parseRequest(reqCtx);

            for (FileItem item : items) {
                if (item.isFormField()) {
                    String inputName = item.getFieldName();
                    String inputValue = item.getString("UTF-8");
                    onNormal.apply(inputName, inputValue);
                } else {
                    String fileName = item.getName();
                    if (!fileName.trim().equals("")) {
                        fileName = S.file.fileNameFromPath(fileName);

                        String ext = S.file.fileExt(fileName);

                        if (!allowedFileExts.contains(ext)) {
                            onInvalidType.apply();
                            return ctx;
                        }

                        try (InputStream in = item.getInputStream()) {
                            onUploadFile.apply(fileName, in);
                        } finally {
                            item.delete();
                        }
                    }
                }
            }

        } catch (FileUploadException | IOException e) {
            debug(e);
            _throw(e);
        }

        return ctx;

    }
}
