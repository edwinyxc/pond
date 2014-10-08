package pond.core.spi.multipart;

import pond.common.S;
import pond.core.Request;
import pond.core.http.UploadFile;
import pond.core.spi.MultipartRequestResolver;
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

import static pond.common.S._for;
import static pond.common.S._notNullElse;

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
            this.contentType =_for(req.header("Content-Type")).first();
            this.contentLength = S.parse.toLong(
                    _notNullElse(_for(req.header("Content-Length")).first(),"0"));
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
    public Map<String, Object> resolve(Request req) {
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
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
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
            return _for(list).filter(i -> !i.isFormField())
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
