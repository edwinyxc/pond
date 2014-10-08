package pond.core.http;

import pond.common.f.Tuple;

import java.io.InputStream;

/**
 * Created by ed on 6/24/14.
 */
public class UploadFile extends Tuple<String, InputStream> {
    public UploadFile(String s, InputStream inputStream) {
        super(s, inputStream);
    }

    public InputStream inputStream() {
        return super._b;
    }

    public String name() {
        return super._a;
    }
}
