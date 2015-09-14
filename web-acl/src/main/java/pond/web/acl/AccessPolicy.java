package pond.web.acl;

import pond.common.f.Function;
import pond.common.f.Tuple;
import pond.web.Ctx;
import pond.web.Route;
import pond.web.http.HttpMethod;

/**
 * Created by ed on 9/11/15.
 */
public class AccessPolicy implements Function.F5<Tuple<Boolean, String>, String, String, Route, HttpMethod, Ctx> {
  Function.F5<Tuple<Boolean, String>, String, String, Route, HttpMethod, Ctx> inner;

  private AccessPolicy(Function.F5<Tuple<Boolean, String>, String, String, Route, HttpMethod, Ctx> p) {
    inner = p;
  }

  public static AccessPolicy absolute(Function.F0<Tuple<Boolean, String>> abs) {
    return new AccessPolicy((user, group, route, method, ctx) -> abs.apply());
  }

  public static AccessPolicy forUser(Function<Tuple<Boolean, String>, String> userOnly) {
    return new AccessPolicy((user, group, route, method, ctx) -> userOnly.apply(user));
  }

  public static AccessPolicy forGroup(Function<Tuple<Boolean, String>, String> groupOnly) {
    return new AccessPolicy((user, group, route, method, ctx) -> groupOnly.apply(group));
  }

  public static AccessPolicy forUserAndGroup(Function.F2<Tuple<Boolean, String>, String, String> userAndGroup) {
    return new AccessPolicy((user, group, route, method, ctx) -> userAndGroup.apply(user, group));
  }

  public static AccessPolicy raw(Function.F5<Tuple<Boolean, String>, String, String, Route, HttpMethod, Ctx> p) {
    return new AccessPolicy(p);
  }

  @Override
  public Tuple<Boolean, String> apply(String s, String s2, Route route, HttpMethod method, Ctx ctx) {
    return inner.apply(s, s2, route, method, ctx);
  }
}

