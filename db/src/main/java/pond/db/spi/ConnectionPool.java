package pond.db.spi;

import java.sql.Connection;
import java.util.Properties;
/**
 * Created by ed on 6/6/14.
 * A SPI for DB module to get connection.
 * System do not need to know which or how the
 * connection established. Just get.
 */
public interface ConnectionPool{

    public static String DRIVER = "ConnectionPool.driver";
    public static String URL= "ConnectionPool.url";
    public static String PASSWORD = "ConnectionPool.password";
    public static String USERNAME = "ConnectionPool.username";
    public static String MAXSIZE = "ConnectionPool.maxsize";
    
    Connection getConnection();
    
    void init(  String maxsize,
                String driver, 
                String url, 
                String username, 
                String password );
    
    void init(Properties p);

}
