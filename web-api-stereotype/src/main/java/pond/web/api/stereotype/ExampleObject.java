package pond.web.api.stereotype;

public class ExampleObject {
    /**
     * Short description for the example.
     */
    String summary;

    /**
     * Long description for the example.
     */
    String decription;

    Object value;

    /**
     * An URL that points to the literal example.
     * This provides the capability to reference examples that cannot easily be
     * included in JSON or YAML documents.
     * The value field and externalValue field are mutually exclusive.
     */
    String externalValue;
}
