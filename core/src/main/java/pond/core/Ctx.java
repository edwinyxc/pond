package pond.core;

import pond.common.S;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static pond.common.S._for;

/**
 * Execution Context, attached to a single thread.
 */
public class Ctx extends HashMap<String, Object> {
    Request req;
    Response resp;
    Pond pond;
    Route route;
    boolean handled = false;

    LinkedList<Mid> stack = new LinkedList<>();

    public Ctx(Request req,
               Response resp,
               Pond pond,
               LinkedList<Mid> mids) {
        this.req = req;
        this.resp = new ResponseWrapper(resp);
        this.pond = pond;

        for (Mid mid : mids) {
            stack.add(mid);
        }

        S._debug(Pond.logger, log -> {
            log.debug("Main ctx route: " + String.join("->",
                    _for(mids).map(Object::toString).join()));
            this.put("_start_time", S.now());
            log.debug("ctx starts at: " + this.get("_start_time"));
        });
    }

    public Pond pond(){
        return pond;
    }

    public Route route(){
        return route;
    }

    void setHandled(boolean b) {
        this.handled = b;
    }

    public Request req() {
        return req;
    }

    public Response resp() {
        return resp;
    }

    public void addMids(List<Mid> midList) {
        stack.addAll(0, midList);
    }

    public Mid getMid() {
        if (stack.size() > 0)
            return stack.pop();
        return null;
    }

}
