package pond.web.restful;

import pond.common.S;

import java.util.HashMap;
import java.util.List;

/**
 * Created by edwin on 3/12/2017.
 */
class Parameter extends HashMap<String, Object> {

    public Parameter name(String name) {
        this.put("name", name);
        return this;
    }

    public String name() {
        return S.avoidNull((String)this.get("name"), "");
    }

    public String in() {
        return S.avoidNull((String) this.get("in"), "");
    }


    public Parameter in(ParamDef.ParamIn in) {
        this.put("in", in.val);
        return this;
    }

    public Parameter description(String d) {
        this.put("description", d);
        return this;
    }

    public Parameter required(Boolean b) {
        this.put("required", b);
        return this;
    }

    //TODO create Type

    /**
     * handler with in
     *
     * @param ref
     * @return
     */
    public Parameter schema(Object ref) {
        this.put("schema", ref);
        return this;
    }


    public Parameter type(ParamDef.ParamType type) {
        this.put("type", type.val);
        return this;
    }

    public Parameter allowEmptyValue() {
        this.put("allowEmptyValue", Boolean.TRUE);
        return this;
    }


    public Parameter items(List<Item> items) {
        this.put("items", items);
        return this;
    }

    public Parameter collectionFormat(CollectionFormatEnum c) {
        this.put("collectionFormat", c.val);
        return this;
    }

    public Parameter maximum(Object d) {
        this.put("maximum", d);
        return this;
    }

    public Parameter exclusiveMaximum(Boolean b) {
        this.put("exclusiveMaximum", b);
        return this;
    }

    public Parameter minimum(Object m) {
        this.put("minimum", m);
        return this;
    }

    public Parameter exclusiveMinimum(Boolean b) {
        this.put("exclusiveMinimum", b);
        return this;
    }

    public Parameter maxLength(Integer max) {
        this.put("maxLength", max);
        return this;
    }

    public Parameter minLength(Integer min) {
        this.put("minLength", min);
        return this;
    }

    public Parameter pattern(String pattern) {
        this.put("pattern", pattern);
        return this;
    }

    public Parameter maxItems(Integer maxItems) {
        this.put("maxItems", maxItems);
        return this;
    }

    public Parameter minItems(Integer minItems) {
        this.put("minItems", minItems);
        return this;
    }

    public Parameter uniqueItems(Boolean b) {
        this.put("uniqueItems", b);
        return this;
    }

    public Parameter multipleOf(Object number) {
        this.put("multipleOf", number);
        return this;
    }

}
