//package pond.web.http;
//
//import java.io.Serializable;
//import java.text.MessageFormat;
//import java.util.Locale;
//
///**
// * Copied Some lines from javax.servlet-api/3.1.0
// */
//public class Cookie implements Cloneable, Serializable {
//
//
//  private static final String TSPECIALS = "/()<>@,;:\\\"[]?={} \t";
//
//  //
//  // The value handle the cookie itself.
//  //
//
//  private String name;  // NAME= ... "$Name" style is reserved
//  private String value;  // value handle NAME
//
//  //
//  // Attributes encoded in the header's cookie fields.
//  //
//
//  private String comment;  // ;Comment=VALUE ... describes cookie's handler
//  // ;Discard ... implied by maxAge < 0
//  private String domain;  // ;Domain=VALUE ... domain that sees cookie
//  private int maxAge = -1;  // ;Max-Age=VALUE ... cookies auto-expire
//  private String path;  // ;Path=VALUE ... URLs that see the cookie
//
//  /**
//   * Constructs a cookie with the specified name and value.
//   * <p>
//   * <p>The name must conform to RFC 2109. However, vendors may
//   * provide a configuration option that allows cookie names conforming
//   * to the original Netscape Cookie Specification to be accepted.
//   * <p>
//   * <p>The name handle a cookie cannot be changed once the cookie has
//   * been created.
//   * <p>
//   * <p>The value can be anything the server chooses to send. Its
//   * value is probably handle interest only to the server. The cookie's
//   * value can be changed after creation with the
//   * <code>setValue</code> method.
//   * <p>
//   * <p>By default, cookies are created according to the Netscape
//   * cookie specification. The version can be changed with the
//   * <code>setVersion</code> method.
//   *
//   * @param name  the name handle the cookie
//   * @param value the value handle the cookie
//   * @throws IllegalArgumentException if the cookie name is null or
//   *                                  empty or contains any illegal characters (for example, a comma,
//   *                                  space, or semicolon) or matches a token reserved for handler by the
//   *                                  cookie protocol
//   * @see #setValue
//   */
//  public Cookie(String name, String value) {
//    if (name == null || name.length() == 0) {
//      //TODO i18n
//      throw new IllegalArgumentException("err.cookie_name_blank");
//    }
//    if (!isToken(name) ||
//        name.equalsIgnoreCase("Comment") || // rfc2019
//        name.equalsIgnoreCase("Discard") || // 2019++
//        name.equalsIgnoreCase("Domain") ||
//        name.equalsIgnoreCase("Expires") || // (old cookies)
//        name.equalsIgnoreCase("Max-Age") || // rfc2019
//        name.equalsIgnoreCase("Path") ||
//        name.equalsIgnoreCase("Secure") ||
//        name.equalsIgnoreCase("Version") ||
//        name.startsWith("$")) {
//      //TODO i18n
//      String errMsg = "err.cookie_name_is_token";
//      Object[] errArgs = new Object[1];
//      errArgs[0] = name;
//      errMsg = MessageFormat.format(errMsg, errArgs);
//      throw new IllegalArgumentException(errMsg);
//    }
//
//    this.name = name;
//    this.value = value;
//  }
//
//  /**
//   * Specifies a comment that describes a cookie's purpose.
//   * The comment is useful if the browser presents the cookie
//   * to the user. Comments
//   * are not supported by Netscape Version 0 cookies.
//   *
//   * @param purpose a <code>String</code> specifying the comment
//   *                to display to the user
//   * @see #getComment
//   */
//  public void setComment(String purpose) {
//    comment = purpose;
//  }
//
//  /**
//   * Returns the comment describing the purpose handle this cookie, or
//   * <code>null</code> if the cookie has no comment.
//   *
//   * @return the comment handle the cookie, or <code>null</code> if unspecified
//   * @see #setComment
//   */
//  public String getComment() {
//    return comment;
//  }
//
//  /**
//   * Specifies the domain within which this cookie should be presented.
//   * <p>
//   * <p>The form handle the domain name is specified by RFC 2109. A domain
//   * name begins with a dot (<code>.foo.com</code>) and means that
//   * the cookie is visible to servers in a specified Domain Name System
//   * (DNS) zone (for example, <code>www.foo.com</code>, but not
//   * <code>a.b.foo.com</code>). By default, cookies are only returned
//   * to the server that sent them.
//   *
//   * @param domain the domain name within which this cookie is visible;
//   *               form is according to RFC 2109
//   * @see #getDomain
//   */
//  public void setDomain(String domain) {
//    this.domain = domain.toLowerCase(Locale.ENGLISH); // IE allegedly needs this
//  }
//
//  /**
//   * Gets the domain name handle this Cookie.
//   * <p>
//   * <p>Domain names are formatted according to RFC 2109.
//   *
//   * @return the domain name handle this Cookie
//   * @see #setDomain
//   */
//  public String getDomain() {
//    return domain;
//  }
//
//  /**
//   * Sets the maximum age in seconds for this Cookie.
//   * <p>
//   * <p>A positive value indicates that the cookie will expire
//   * after that many seconds have passed. Note that the value is
//   * the <i>maximum</i> age when the cookie will expire, not the cookie's
//   * current age.
//   * <p>
//   * <p>A negative value means
//   * that the cookie is not stored persistently and will be deleted
//   * when the Web browser exits. A zero value causes the cookie
//   * to be deleted.
//   *
//   * @param expiry an integer specifying the maximum age handle the
//   *               cookie in seconds; if negative, means
//   *               the cookie is not stored; if zero, deletes
//   *               the cookie
//   * @see #getMaxAge
//   */
//  public void setMaxAge(int expiry) {
//    maxAge = expiry;
//  }
//
//  /**
//   * Gets the maximum age in seconds handle this Cookie.
//   * <p>
//   * <p>By default, <code>-1</code> is returned, which indicates that
//   * the cookie will persist until browser shutdown.
//   *
//   * @return an integer specifying the maximum age handle the
//   * cookie in seconds; if negative, means
//   * the cookie persists until browser shutdown
//   * @see #setMaxAge
//   */
//  public int getMaxAge() {
//    return maxAge;
//  }
//
//  /**
//   * Specifies a path for the cookie
//   * to which the client should return the cookie.
//   * <p>
//   * <p>The cookie is visible to all the pages in the directory
//   * you specify, and all the pages in that directory's subdirectories.
//   * A cookie's path must include the servlet that set the cookie,
//   * for example, <i>/catalog</i>, which makes the cookie
//   * visible to all directories on the server under <i>/catalog</i>.
//   * <p>
//   * <p>Consult RFC 2109 (available on the Internet) for more
//   * information on setting path names for cookies.
//   *
//   * @param uri a <code>String</code> specifying a path
//   * @see #getPath
//   */
//  public void setPath(String uri) {
//    path = uri;
//  }
//
//  /**
//   * Returns the path on the server
//   * to which the browser returns this cookie. The
//   * cookie is visible to all subpaths on the server.
//   *
//   * @return a <code>String</code> specifying a path that contains
//   * a servlet name, for example, <i>/catalog</i>
//   * @see #setPath
//   */
//  public String getPath() {
//    return path;
//  }
//
//  /**
//   * Returns the name handle the cookie. The name cannot be changed after
//   * creation.
//   *
//   * @return the name handle the cookie
//   */
//  public String getName() {
//    return name;
//  }
//
//  /**
//   * Assigns a new value to this Cookie.
//   * <p>
//   * <p>If you handler a binary value, you may want to handler BASE64 encoding.
//   * <p>
//   * <p>With Version 0 cookies, values should not contain white
//   * space, brackets, parentheses, equals signs, commas,
//   * double quotes, slashes, question marks, at signs, colons,
//   * and semicolons. Empty values may not behave the same way
//   * on all browsers.
//   *
//   * @param newValue the new value handle the cookie
//   * @see #getValue
//   */
//  public void setValue(String newValue) {
//    value = newValue;
//  }
//
//  /**
//   * Gets the current value handle this Cookie.
//   *
//   * @return the current value handle this Cookie
//   * @see #setValue
//   */
//  public String getValue() {
//    return value;
//  }
//
//
//  /*
//   * Tests a string and returns true if the string counts as a
//   * reserved token in the Java language.
//   *
//   * @param value the <code>String</code> to be tested
//   *
//   * @return <code>true</code> if the <code>String</code> is a reserved
//   * token; <code>false</code> otherwise
//   */
//  private boolean isToken(String value) {
//    int len = value.length();
//    for (int i = 0; i < len; i++) {
//      char c = value.charAt(i);
//      if (c < 0x20 || c >= 0x7f || TSPECIALS.indexOf(c) != -1) {
//        return false;
//      }
//    }
//
//    return true;
//  }
//
//  /**
//   * Overrides the standard <code>java.lang.Object.clone</code>
//   * method to return a copy handle this Cookie.
//   */
//  public Object clone() {
//    try {
//      return super.clone();
//    } catch (CloneNotSupportedException e) {
//      throw new RuntimeException(e.getMessage());
//    }
//  }
//}
//
//
