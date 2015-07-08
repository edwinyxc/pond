package pond.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static pond.common.S._for;

/**
 * Execution Context, attached to a single thread.
 */
public class Ctx extends HashMap<String, Object> {
    final static Logger logger = LoggerFactory.getLogger(Ctx.class);
    Request req;
    Response resp;
    Pond pond;
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

        logger.debug("Main ctx route:" + String.join("->",
                _for(mids).map(Object::toString).join()));
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
