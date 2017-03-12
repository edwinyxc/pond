package pond.web.restful;

import pond.common.f.Tuple;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by edwin on 3/12/2017.
 */
class Operation extends HashMap<String, Object> {

    public Operation() {
    }

    public Operation tags(List<String> tags) {
        this.put("tags", tags);
        return this;
    }

    public Operation summary(String summary) {
        this.put("summary", summary);
        return this;
    }

    public Operation description(String description) {
        this.put("description", description);
        return this;
    }

    public Operation operationId(String id) {
        this.put("operationId", id);
        return this;
    }

    public Operation consumes(Set<String> consumes) {
        this.put("consumes", consumes);
        return this;
    }

    public Operation produces(Set<String> produces) {
        this.put("produces", produces);
        return this;
    }


    public Operation parameters(List<Parameter> parameters) {
        this.put("parameters", parameters);
        return this;
    }

    public static class Response extends HashMap<String, Object> {
        public Response(String description) {
            this.put("description", description);
        }

        //TODO Schema Object
        public Response schema(HashMap<String, Object> s) {
            this.put("schema", s);
            return this;
        }

        public static class Header extends HashMap<String, Object> {
            public Header(String desc, String type) {
                this.put("description", desc);
                this.put("type", type);
            }
        }

        public Response headers(Map<String, Header> headers) {
            this.put("headers", headers);
            return this;
        }

        public Response examples(List<HashMap<String, Object>> examples) {
            this.put("examples", examples);
            return this;
        }

    }

    public Operation responses(List<Tuple<Integer, Response>> responses) {
        this.put("responses", new HashMap<String, Object>() {{
            for (Tuple<Integer, Response> t : responses) {
                if (t._a != null) {
                    this.put(String.valueOf(t._a), t._b);
                } else {
                    this.put("default", t._b);
                }
            }
        }});
        return this;
    }

    /**
     * http, https, ws, wss
     *
     * @param schemes
     * @return
     */
    public Operation schemes(List<String> schemes) {
        this.put("schemes", schemes);
        return this;
    }

    public Operation deprecated() {
        this.put("deprecated", Boolean.TRUE);
        return this;
    }

    public Operation security(SecurityRequirement securityRequirement) {
        this.put("security", securityRequirement);
        return this;
    }
}
