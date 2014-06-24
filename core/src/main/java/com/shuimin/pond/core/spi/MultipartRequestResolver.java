package com.shuimin.pond.core.spi;

import com.shuimin.pond.core.Request;
import com.shuimin.pond.core.db.UploadFile;

import java.io.IOException;
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
     * @param req request
     * @return resolved map
     * @throws IOException
     */
    public Map<String, Object> resolve(Request req) throws IOException;

    /**
     * Decide if a form has enctype:multipart/form-data
     * @param req
     * @return
     */
    public boolean isMultipart(Request req);

    public List<String> multipartParamNames(Request req);

    /**
     * Returns uploaded file with the inputstream and the filename
     * @param req request
     * @param name fieldname
     * @return inputstream and filename
     * @throws IOException
     */
    public UploadFile getUpload(Request req, String name)
            throws IOException;
}

