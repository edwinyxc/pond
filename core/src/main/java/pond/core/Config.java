package pond.core;

import java.util.Properties;
import java.io.File;
import java.io.FileInputStream;
import pond.common.S;
import static pond.common.S.*;

/**
 * Created by ed on 2014/4/22.
 */
public class Config {

//    public final static String DEFAULT_CONTENT_TYPE = "g.default_content_type";


    public final static String ROOT = "g.root";

    public final static String ROOT_WEB = "g.root_web";


    /***** for app.set    ***/


    /**
     * The view directory path, defaulting to ${ROOT}/views
     */
    public final static String VIEWS_PATH = "g.views_path";

    /**
     * default to views
     */
    public final static String VIEWS_NAME = "g.views_name";

    /**
     * The public attachment i.e.js,css,img,font,txt ...
     * directory path, defaulting to ${ROOT}/www
     */
    public final static String WWW_PATH = "g.www_path";

    /**
     * default to www 
     */
    public final static String WWW_NAME = "g.www_name";

    /**
     * X-POWERED-BY Http header, defaulting to "Pond"
     */
    public final static String X_POWERED_BY = "g.x_powered_by";

    /***** for app.enable ***/

    public final static String CASE_SENSITIVE_ROUTING = "g.case_sensitive_routing";

    /**
     * enable strict routing
     * by default, /foo & /foo/ are treated the same by the router
     */
    public final static String STRICT_ROUTING= "g.strict_routing";

    /**
     * TODO:
     * Enables view template compilation caching,
     * closed in debug mode
     */
    public final static String VIEW_CACHE = "g.view_cache";

    
    public final static String CONFIG_FILE_NAME = "pond.conf";
    
    

    
}
