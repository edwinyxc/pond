package pond.web.acl;

import pond.common.S;
import pond.web.Session;

public class SessionBasedAccessControl extends AccessControl {

  public SessionBasedAccessControl(String sessionUserLabel, String sessionGroupLabel) {

    this.userTokenGetter(ctx -> {

      Session session = Session.get(ctx);
      return S._tap(session.get(sessionUserLabel), user -> {
        S._debug(logger, log -> log.debug("Get User-Token " + sessionUserLabel + " From Session: " + user));
        ctx.put(INCTX_USER_TOKEN_LABEL, user);
      });

    });

    this.groupTokenGetter(ctx -> {

      Session session = Session.get(ctx);
      return S._tap(session.get(sessionGroupLabel), group -> {
        S._debug(logger, log -> log.debug("Get Group-Token " + sessionGroupLabel + " From Session: " + group));
        ctx.put(INCTX_GROUP_TOKEN_LABEL, group);
      });

    });

  }

}
