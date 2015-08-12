package pond.core;


import pond.common.S;

import java.util.HashMap;
import java.util.Properties;

import static pond.common.S._for;

/**
 *
 */
public class Config extends HashMap<String,String>{

//    public final static String DEFAULT_CONTENT_TYPE = "g.default_content_type";


    public final static String ROOT = "g.root";

    public final static String ROOT_WEB = "g.root_web";


    /***** for app.load    ***/


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

    public final static String CONFIG_FILE = "g.config_file";


    public final static String PORT = "g.port";

    public final static String ALLOW_MEMORY_FILE_MAPPING = "g.allow_file_memory_mapping";

    public final static String ALLOW_LIST_DIRECTORY = "g.allow_list_directory";

    //by default
    public final static String CONFIG_FILE_NAME = "pond.conf";

    /*default*/

    String config_file_name = CONFIG_FILE_NAME;

    public Integer getInt(String k){
        return Integer.parseInt(getOrDefault(k, "0"));
    }
    public Boolean getBool(String k) {
        return Boolean.parseBoolean(getOrDefault(k, "false"));
    }

    public void load(Properties p) {
        _for(p.keys()).each(k -> {
            String o = p.getProperty((String)k);
            this.put((String)k, o);
        });
    }
    /**
     * -p[port] -d[the-path-of-www-dir] -c[name-of-the-config-file]
     */
    Config readFromFile(String[] args){
        _for(args).each(arg -> {
            if(arg.startsWith("-p")){
                String _port = arg.substring(2);
                this.put(PORT, _port);
            }else if(arg.startsWith("-d")){
                String _www = arg.substring(2);
                this.put(WWW_PATH,_www);
            }else if(arg.startsWith("-c")){
                String _config_file = arg.substring(2);
                //under the classes-root
                this.load(S.file.loadProperties(_config_file));
            }else{
                throw new RuntimeException("Unknown cmd line parameter:"+arg);
            }
        });
        return this;
    }
}
