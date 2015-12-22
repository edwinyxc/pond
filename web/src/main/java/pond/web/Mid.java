package pond.web;

import pond.common.S;
import pond.common.f.Callback;
import pond.common.f.Function;
import pond.core.Service;
import pond.core.Services;

/**
 * A
 */
public interface Mid extends Callback.C2<Request, Response> {

  final static Mid NOOP = (req, resp) -> {};



}
