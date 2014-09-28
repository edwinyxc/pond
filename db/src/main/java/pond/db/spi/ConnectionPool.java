package pond.db.spi;

import java.sql.Connection;

/**
 * Created by ed on 6/6/14.
 * A SPI for DB module to get connection.
 * System do not need to know which or how the
 * connection established. Just get.
 */
public interface ConnectionPool{

    Connection getConnection();

}
