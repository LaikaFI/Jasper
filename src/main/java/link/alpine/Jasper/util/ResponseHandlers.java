package link.alpine.Jasper.util;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

/**
 * Basic ResponseHandler Class
 * Intended for GET requests from online API's
 * Such as, Steam, DDG, ProtonDB, CatApi, etc.
 */
public class ResponseHandlers {
    /**
     * @apiNote Returns GET response in a uniformed string. Use JSONArray to convert it into JSON.
     **/
    public static final ResponseHandler<String> STRING_RESPONSE_HANDLER = response -> {
        int status = response.getStatusLine().getStatusCode();
        if (status >= 200 && status < 300) {
            HttpEntity entity = response.getEntity();
            return entity != null ? EntityUtils.toString(entity) : null;
        } else {
            throw new ClientProtocolException("Unexpected response status: " + status);
        }
    };

}
