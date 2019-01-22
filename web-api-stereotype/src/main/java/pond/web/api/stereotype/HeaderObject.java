package pond.web.api.stereotype;

import java.lang.annotation.Annotation;

public class HeaderObject extends ParameterObject
{
    public HeaderObject(String name, Class type, ParameterInContract in, Annotation in_anno, ParameterSchemaContract schema, Annotation schema_anno) {
        super(name, type, in, in_anno, schema, schema_anno);
    }
}
