package cm.xd.oopdate.mapping;

import org.slf4j.Logger;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by deepy on 15/09/16.
 */
public class DefaultMapping implements OOPMapping {
    @Override
    public int invoke(PreparedStatement ps, int counter, Object o, Logger logger)
            throws SQLException, IllegalAccessException {
        return DefaultMapping.defaultMapping(ps, counter, o, logger);
    }

    public static int defaultMapping(PreparedStatement ps, int counter, Object o, Logger logger)
            throws SQLException, IllegalAccessException {
        if (o.getClass() == String.class) {
            ps.setString(counter++, (String) o);
        } else if (o.getClass() == Integer.class) {
            ps.setInt(counter++, (Integer) o);
        } else if (o.getClass() == BigDecimal.class) {
            ps.setBigDecimal(counter++, (BigDecimal) o);
        } else if (o.getClass() == Date.class) {
            ps.setDate(counter++, (Date) o);
        } else {
            logger.debug("Got unknown type: " + o.getClass().toString());
            throw new IllegalAccessException("Got unknown type: " + o.getClass().toString());
        }
        return counter;
    }
}
