package pond.web.api.stereotype;

import java.util.List;

public class SecurityRequirementObject {
    String _name;
    /**
     * Each name MUST correspond to a security scheme which is declared in the Security Schemes under the Components Object. If the security scheme is of type "oauth2" or "openIdConnect",
     * then the value is a list of scope names required for the execution.
     * For other security scheme types, the array MUST be empty.
     */
    List<String> _values;
}
