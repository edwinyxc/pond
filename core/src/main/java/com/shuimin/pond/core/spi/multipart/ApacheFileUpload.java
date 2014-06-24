package com.shuimin.pond.core.spi.multipart;

import com.shuimin.common.S;
import com.shuimin.common.f.Tuple;
import com.shuimin.pond.core.Request;
import com.shuimin.pond.core.db.UploadFile;
import com.shuimin.pond.core.spi.MultipartRequestResolver;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.UploadContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.shuimin.common.S._for;
import static com.shuimin.common.S._notNullElse;

/**
 * Created by ed on 6/24/14.
 */
public class ApacheFileUpload implements MultipartRequestResolver {

    private class ReqCtx implements UploadContext {
        String encoding;
        String contentType;
        long contentLength;
        InputStream in;

        public ReqCtx(Request req) {
            this.encoding = _notNullElse(req.characterEncoding(), "UTF-8");
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
            return (int) contentLength;
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

    private ThreadLocal<List<FileItem>> cache = new ThreadLocal<>();

    private final FileItemFactory factory = new DiskFileItemFactory() {
        {
            this.setSizeThreshold(4 * 1024);

        }
    };
    private int maxUploadSize = 1024 * 1024 * 200;
    private final ServletFileUpload upload = new ServletFileUpload(factory) {
        {
            this.setHeaderEncoding("UTF-8");
            this.setFileSizeMax(maxUploadSize);
        }
    };

    private List<FileItem> _getFileItems(Request req)
            throws FileUploadException {
        if (cache.get() != null) return cache.get();
        List<FileItem> items = upload.parseRequest(new ReqCtx(req));
        cache.set(items);
        return items;
    }


    @Override
    public Map<String, Object> resolve(Request req) throws IOException {
        Map<String,Object> map = new HashMap<>();
        try {
            List<FileItem> list = _getFileItems(req);
            String name;
            Object value;
            for(FileItem i : list) {
                if(i.isFormField()){
                    name = i.getFieldName();
                    value = i.getString("UTF-8");
                }
                else{
                    name = i.getFieldName();
                    value = new UploadFile(i.getName(),i.getInputStream());
                }
                map.put(name,value);
            }
        } catch (FileUploadException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException ignored) {
        }
        return map;
    }

    @Override
    public boolean isMultipart(Request req) {
        return ServletFileUpload.isMultipartContent(new ReqCtx(req));
    }

    @Override
    public List<String> multipartParamNames(Request req) {
        try {
            List<FileItem> list = _getFileItems(req);
            return _for(list).grep( i -> !i.isFormField())
                    .map(FileItem::getFieldName).toList();
        } catch (FileUploadException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }


    @Override
    public UploadFile getUpload(Request req, String name)
            throws IOException {
        try {
            List<FileItem> list = _getFileItems(req);
            for(FileItem i : list){
                if(!i.isFormField() && name.equals(i.getFieldName())){
                    return new UploadFile(i.getName(),i.getInputStream());
                }
            }
        } catch (FileUploadException e) {
            e.printStackTrace();
        }
        return null;
    }
}
