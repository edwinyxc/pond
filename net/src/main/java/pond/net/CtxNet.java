package pond.net;

import pond.core.Ctx;
import pond.core.CtxBase;

public interface CtxNet extends Ctx {

    static CtxBase build(){
        return new CtxBase(){
        };
    }
}
