package pond.web.api.stereotype;

import java.util.Map;

public class EncodingObject {

    String contentType;

    Map<String, HeaderObject> headers;

    /**
     * Describes how a specific property value will be serialized depending on its type.
     * See Parameter Object for details on the style property.
     * The behavior follows the same values as query parameters,
     * including default values.
     * This property SHALL be ignored if the request body media type
     * is not application/x-www-form-urlencoded.
     */
    String style;

    /**
     * When this is true, property values of type array or object generate separate
     * parameters for each value of the array,
     * or key-value-pair of the map.
     * For other types of properties this property has no effect.
     * When style is form, the default value is true.
     * For all other styles, the default value is false.
     * This property SHALL be ignored if the request body media type is not application/x-www-form-urlencoded.
     */
    boolean explode;

    /**
     * Determines whether the parameter value SHOULD allow reserved characters,
     * as defined by RFC3986 :/?#[]@!$&'()*+,;= to be included without percent-encoding.
     * The default value is false.
     * This property SHALL be ignored if the request body media type is not application/x-www-form-urlencoded.
     */
    boolean allowReserved;

}
