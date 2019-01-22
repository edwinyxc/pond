package pond.web.api.stereotype;

import java.util.Map;

public class MediaTypeObject {
    SchemaObject schemaObject;

    /**
     * Example of the media type. The example object SHOULD
     * be in the correct format as specified by the media type.
     * The example field is mutually exclusive if the examples field.
     * Furthermore, if referencing a schema which contains an example,
     * the example value SHALL override the example provided by the schema.
     */
    Object example;

    Map<String, ExampleObject> examples;

    Map<String, EncodingObject> encoding;

}
