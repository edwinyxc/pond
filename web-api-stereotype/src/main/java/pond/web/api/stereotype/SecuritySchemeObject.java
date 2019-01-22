package pond.web.api.stereotype;

public class SecuritySchemeObject {
    /**
     * REQUIRED the type of the security scheme. Valid values are
     * "apiKey", "http", "oauth2", "openIdConnect"
     */
    String type;

    String description;

    //apiKey
    String name;
    String in;

    //http
    String scheme;
    String bearerFormat;

    //oauth2
    //[OAuthFlowObject]
    String flows;

    //openIdConnect
    String openIdConnectUrl;


}
