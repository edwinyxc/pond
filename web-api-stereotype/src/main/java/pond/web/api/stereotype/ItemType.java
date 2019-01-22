package pond.web.api.stereotype;

/**
 * Created by edwin on 3/12/2017.
 */
enum ItemType {
    STRING("string"),
    NUMBER("number"),
    INTEGER("integer"),
    BOOLEAN("boolean"),
    ARRAY("array");

    final String val;

    ItemType(String val) {
        this.val = val;
    }
}
