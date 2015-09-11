package pond.web;

import pond.common.f.Callback;

/**
 * A
 */
public interface Mid extends Callback.C2<Request, Response> {

  final static Mid NOOP =  (req, resp ) -> {};

  static WrappedMid wrap(Mid mid){
    return new WrappedMid(mid);
  }

}

