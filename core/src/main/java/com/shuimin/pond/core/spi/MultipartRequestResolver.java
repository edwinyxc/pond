package com.shuimin.pond.core.spi;

import com.shuimin.pond.core.Request;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Created by ed on 6/24/14.
 */
public interface MultipartRequestResolver {

    /**
     * <pre>
          Resolve request.
          Returns A Map that contains all the upload form
          with String or Tuple &lt;InputStream,String&gt;.
     * </pre>
     *
     * @param req
     * @return
     * @throws IOException
     */
    public Map<String, Object> resolve(Request req) throws IOException;

    public boolean isMultipart(Request req);

    public List<String> multipartParamNames(Request req);

    public InputStream paramUploadFile(Request req, String name) throws IOException;
}

