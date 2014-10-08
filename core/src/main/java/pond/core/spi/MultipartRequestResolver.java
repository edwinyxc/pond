package pond.core.spi;

import pond.common.f.Tuple;
import pond.core.Request;

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
     * @param req request
     * @return resolved map
     */
    public Map<String, Object> resolve(Request req);

    /**
     * Decide if a form has enctype:multipart/form-data
     * @param req
     * @return
     */
    public boolean isMultipart(Request req);

    public List<String> multipartParamNames(Request req);

    /**
     * Returns uploaded attachment with the inputstream and the filename
     * @param req request
     * @param name fieldname
     * @return inputstream and filename
     * @throws IOException
     */
    public Tuple<String,InputStream> getUpload(Request req, String name)
            throws IOException;
}

