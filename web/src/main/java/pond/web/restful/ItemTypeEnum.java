package pond.web.restful;

/**
 * Created by edwin on 3/12/2017.
 */
enum ItemTypeEnum {
    STRING("string"),
    NUMBER("number"),
    INTEGER("integer"),
    BOOLEAN("boolean"),
    ARRAY("array");

    final String val;

    ItemTypeEnum(String val) {
        this.val = val;
    }
}
