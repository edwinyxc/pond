package com.shuimin.pond.codec.upload;

import com.shuimin.common.S;
import com.shuimin.common.f.Callback;
import com.shuimin.common.f.Function;
import com.shuimin.pond.core.AbstractMiddleware;
import com.shuimin.pond.core.ExecutionContext;
import com.shuimin.pond.core.Global;
import com.shuimin.pond.core.Server;
import com.shuimin.pond.core.exception.HttpException;
import com.shuimin.pond.core.exception.UnexpectedException;
import com.shuimin.pond.core.http.Request;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.UploadContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.shuimin.common.S.*;
import static com.shuimin.pond.core.ExecutionContext.RESP;
import static com.shuimin.pond.core.Interrupt.kill;
import static com.shuimin.pond.core.Interrupt.render;
import static com.shuimin.pond.core.Server.G.debug;
import static com.shuimin.pond.core.misc.Renderable.text;

/**
 * Created by ed on 2014/4/22.
 */

public class FileUploadServer extends AbstractMiddleware {

    private final Set<String> allowedTypes = S.collection.set.hashSet(
        new String[]{"jpg", "zip", "txt", "ppt", "pptx", "doc", "docx", "xls", "gif"});


    /**
     * <p>添加允许的文件类型后缀 [jpg,zip,...]</p>
     * @param s
     * @return
     */
    public FileUploadServer allow(String... s) {
        this.allowedTypes.addAll(Arrays.asList(s));
        return this;
    }

    /**
     * configs
     */
    private int maxUploadSize = 1024 * 1024 * 200;

    public FileUploadServer maxUpload_Mb(int size) {
        maxUploadSize = size * 1024 * 1024;
        return this;
    }


    /*
    *Exceptions
    */
    private Function<HttpException,String> E403_INVALID_FILE_TYPE = s ->
        new HttpException(403,"invalid file type "+s+", only "+dump(allowedTypes)+" are allowed.");
    private Function<HttpException,Exception> E500_INNER_EXCEPTION = e ->
        new HttpException(500,e.getMessage());

    /**
     *
     * @return
     */


    /***
     * providers
     */

    private Function._0<File> uploadDirProvider = this::getUploadDir;

    private Function._0<File> tmpDirProvider = this:: getTmpDir;

    private Function._0<String> webRootProvider = () -> (String)Server.config(Global.ROOT);

    /**
     * <p>注入root目录的提供器</p>
     * @param provider provider
     * @return root path
     */
    public FileUploadServer root(Function._0<String> provider) {
        webRootProvider = provider;
        return this;
    }

    /***
     * callbacks
     */

    private Callback<HttpException> onErr = e -> {
        RESP().write(e.toString()).send(e.code());
        kill();
    };

    public FileUploadServer onErr(Callback<HttpException> cb){
        this.onErr = cb;
        return this;
    }

    private Callback._2<String,String> onNormal = (name,value) ->
        debug("you`ve received "+name+" = "+value);

    public FileUploadServer onNormal(Callback._2<String,String> cb) {
        this.onNormal = cb;
        return this;
    }

    private Function._2<File,String,InputStream> onFileUpload = (name,in) -> {
        debug("Upload filename : " + name);
        File f = null;
        try (FileOutputStream out = new FileOutputStream((f = new File(uploadDirProvider.apply(), name)))) {
            S.stream.write(in, out);
            return f;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    };

    private Callback<File> onFileUploadFinished = (file) ->
        render(text("File[" + file.getName()+ "] uploaded, location:" + file.getAbsolutePath()));

    public FileUploadServer onFileUpload(Function._2<File,String,InputStream> cb ) {
        this.onFileUpload = cb;
        return this;
    }

    public FileUploadServer onFileUploadFinished(Callback<File> finishedCb ) {
        this.onFileUploadFinished = finishedCb;
        return this;
    }

    public FileUploadServer uploadDir(String str) {
        String absPath;

        if (str.startsWith("/") || str.indexOf(":") == 1) {
            absPath = str;
        } else {
            absPath = webRootProvider.apply() + File.separator + str;
        }

        File f = new File(absPath);

        if (!f.exists() || !f.canWrite()) {
            throw new UnexpectedException(this) {
                @Override
                public String brief() {
                    return "File[" + absPath + "] not valid";
                }
            };
        }
        this.uploadDirProvider = ()-> f;
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

        factory.setRepository(tmpDirProvider.apply());

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

                        if (!allowedTypes.contains(ext)) {
                            throw E403_INVALID_FILE_TYPE.apply(ext);
                        }

                        try (InputStream in = item.getInputStream()) {
                            File f = onFileUpload.apply(fileName, in);
                            onFileUploadFinished.apply(f);
                        } finally {
                            item.delete();
                        }
                    }
                }
            }

        } catch (HttpException e) {
            onErr.apply(e);
        } catch (Exception e) {
            onErr.apply(E500_INNER_EXCEPTION.apply(e));
        }

        return ctx;

    }

    /**
     * default gets
     */


    private File getTmpDir() {
        File ret = new File(webRootProvider.apply() + File.separator + "tmp_upload");
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
        File ret = new File(webRootProvider.apply() + File.separator + "upload");
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
}
