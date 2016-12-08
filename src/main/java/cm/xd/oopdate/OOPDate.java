package cm.xd.oopdate;

import cm.xd.oopdate.mapping.DefaultMapping;
import cm.xd.oopdate.mapping.OOPMapping;
import cm.xd.oopdate.query.Query;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Basic implementation of {@link cm.xd.oopdate.implementation.OOPDate}
 * using {@link DefaultMapping} for mapping.
 *
 * @author deepy
 */
public class OOPDate {

    private static final OOPMapping mapper;

    static {
        mapper = new DefaultMapping();
    }

    private OOPDate() {
        // Should not be instantiated.
    }

    /**
     * Takes an Annotated Object and updates it using JDBC.
     * @param obj Annotated Object to update.
     * @param con Connection to use.
     * @throws SQLException upon SQL failure
     * @throws IllegalAccessException upon reflection failure
     */
    public static void magicks(Object obj, Connection con) throws SQLException, IllegalAccessException {
        cm.xd.oopdate.implementation.OOPDate.magicks(mapper, obj, null, con);
    }

    /**
     * Takes an Annotated Object and updates it using JDBC.
     * The List of {@link Query} objects provided will take precedence over the annotations.
     * @param obj Annotated Object to update.
     * @param ql List of {@link Query} to also include.
     * @param con Connection to use.
     * @throws SQLException upon SQL failure
     * @throws IllegalAccessException upon reflection failure
     */
    public static void magicks(Object obj, List<Query> ql, Connection con) throws SQLException, IllegalAccessException {
        cm.xd.oopdate.implementation.OOPDate.magicks(mapper, obj, ql, con);
    }
}
