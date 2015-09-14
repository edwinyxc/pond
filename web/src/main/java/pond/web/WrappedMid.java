package pond.web;

import pond.common.S;

public class WrappedMid implements Mid {

  final Mid mid;

  private Class<? extends Mid> requireClass;
  private Mid require;

  public WrappedMid(Mid mid) {
    this.mid = mid;
  }

  public WrappedMid require(Mid mid) {
    this.require = mid;
    return this;
  }

  public WrappedMid require(Class<? extends Mid> midClass) {
    this.requireClass = midClass;
    return this;
  }

  private boolean checkRequire(Ctx ctx) {
    if (requireClass != null) {
      return S._for(ctx.handledMids()).map(Object::getClass).toList().contains(requireClass);
    }
    if (require != null) {
      return S._for(ctx.handledMids()).contains(require);
    }
    return true;
  }

  @Override
  public void apply(Request request, Response response) {
    Ctx ctx = request.ctx();

    if (!checkRequire(ctx)) {
      Mid.NOOP.apply(request, response);
      return;
    }

    mid.apply(request, response);
  }
}
