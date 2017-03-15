package pond.web.restful;

import java.util.HashMap;

/**
 * Created by edwin on 3/12/2017.
 */
class Item extends HashMap<String, Object> {

    Item(ItemTypeEnum typeEnum) {
        this.put("type", typeEnum.val);
    }

    public Item format(String format) {
        this.put("format", format);
        return this;
    }

    public Item collectionFormat(CollectionFormatEnum c) {
        this.put("collectionFormat", c.val);
        return this;
    }

    public Item maximum(Object d) {
        this.put("maximum", d);
        return this;
    }

    public Item exclusiveMaximum(Boolean b) {
        this.put("exclusiveMaximum", b);
        return this;
    }

    public Item minimum(Object m) {
        this.put("minimum", m);
        return this;
    }

    public Item exclusiveMinimum(Boolean b) {
        this.put("exclusiveMinimum", b);
        return this;
    }

    public Item maxLength(Integer max) {
        this.put("maxLength", max);
        return this;
    }

    public Item minLength(Integer min) {
        this.put("minLength", min);
        return this;
    }

    public Item pattern(String pattern) {
        this.put("pattern", pattern);
        return this;
    }

    public Item maxItems(Integer maxItems) {
        this.put("maxItems", maxItems);
        return this;
    }

    public Item minItems(Integer minItems) {
        this.put("minItems", minItems);
        return this;
    }

    public Item uniqueItems(Boolean b) {
        this.put("uniqueItems", b);
        return this;
    }

    public Item multipleOf(Object number) {
        this.put("multipleOf", number);
        return this;
    }
}
