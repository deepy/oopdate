package cm.xd.oopdate.mapping;

import org.slf4j.Logger;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by deepy on 15/09/16.
 */
@FunctionalInterface
public interface OOPMapping {
    int invoke(PreparedStatement ps, int counter, Object o, Logger logger)
            throws SQLException, IllegalAccessException;
}
