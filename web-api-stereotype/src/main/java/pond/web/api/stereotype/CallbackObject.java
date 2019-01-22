package pond.web.api.stereotype;

import java.util.Map;

public class CallbackObject {

    String _expr;
    PathItemObject _value;


    /**
     * myWebhook: <-- [_expr : _value]
     *   'http://notificationServer.com?transactionId={$request.body#/id}&email={$request.body#/email}':
     *     post:
     *       requestBody:
     *         description: Callback payload
     *         content:
     *           'application/json':
     *             schema:
     *               $ref: '#/components/schemas/SomePayload'
     *       responses:
     *         '200':
     *           description: webhook successfully processed and no retries will be performed
     */
}
