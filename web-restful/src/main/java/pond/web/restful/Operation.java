package pond.web.restful;

import pond.common.S;
import pond.common.f.Tuple;

import java.util.*;

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

    public Set<String> consumes() {
        return (Set<String>) this.get("consumes");
    }

    public Operation produces(Set<String> produces) {
        this.put("produces", produces);
        return this;
    }

    public Set<String> produces() {
        return (Set<String>) this.get("produces");
    }

    public Operation parameters(List<Parameter> parameters) {
        this.put("parameters",
                S._tap(S.avoidNull((List<Parameter>) this.get("parameters"), new LinkedList<>()),
                        (List<Parameter> x) -> x.addAll(parameters)
                ));
        return this;
    }

    public List<Parameter> parameters() {
        return (List<Parameter>) this.get("parameters");
    }

    public Map<String, Response> mergeResponse(List<Tuple<Integer, Response>> responses) {
        Map<String, Response> ret = S.avoidNull(this.responses(), new HashMap<>());
        for (Tuple<Integer, Response> t : responses) {
            if (t._a != null) {
                Response r;
                if ((r = ret.get(String.valueOf(t._a))) != null) {
                    r.merge(t._b);
                } else r = t._b;
                ret.put(String.valueOf(t._a), r);
            } else {
                ret.put("default", t._b);
            }
        }
        return ret;
    }

    public Operation responses(Map<String, Response> responses) {
        Map<String, Response> thisMap = responses();
        thisMap.putAll(responses);
        return this;
    }

    public Operation responses(List<Tuple<Integer, Response>> responses) {
        this.put("responses", mergeResponse(responses));
        return this;
    }

    public Map<String, Response> responses() {
        return (Map<String, Response>) this.get("responses");
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

        public Response merge(Response s) {
            this.put("description",
                    String.valueOf(this.get("description")) + " | "
                            + String.valueOf(s.get("description"))
            );
            this.headers(S._tap(S.avoidNull((Map<String, Header>) s.get("headers"), new HashMap<>()),
                    x -> x.putAll((Map<String, Header>) S.avoidNull(s.get("headers"), new HashMap<>()))
            ));
            return this;
        }

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
