package cm.xd.oopdate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cm.xd.oopdate.annotations.OOPField;
import cm.xd.oopdate.annotations.OOPIdentityField;
import cm.xd.oopdate.annotations.OOPTable;
import cm.xd.oopdate.query.Query;
import cm.xd.oopdate.query.QueryType;

import java.io.StringWriter;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OOPDate {

    /**
     * Takes an Annotated Object and updates it using JDBC.
     * @param obj Annotated Object to update.
     * @param con Connection to use.
     * @throws SQLException
     * @throws IllegalAccessException
     */
    public static void magicks(Object obj, Connection con) throws SQLException, IllegalAccessException {
        magicks(obj, null, con);
    }

    /**
     * Takes an Annotated Object and updates it using JDBC.
     * The List of {@link Query} objects provided will take precedence over the annotations.
     * @param obj Annotated Object to update.
     * @param ql List of {@link Query} to also include.
     * @param con Connection to use.
     * @throws SQLException
     * @throws IllegalAccessException
     */
    public static void magicks(Object obj, List<Query> ql, Connection con) throws SQLException, IllegalAccessException {
        if (ql == null) {
            ql = new ArrayList<>();
        }

        List<Query> queries = new ArrayList<>();

        OOPTable table = obj.getClass().getAnnotation(OOPTable.class);
        if (table != null && table.name() != null) {
            Field[] fields = obj.getClass().getDeclaredFields();

            for (Field f : fields) {
                OOPField o = f.getAnnotation(OOPField.class);
                OOPIdentityField i = f.getAnnotation(OOPIdentityField.class);

                f.setAccessible(true);
                if (o != null && f.get(obj) != null) {
                    Query q = new Query();
                    q.setQueryType(QueryType.DATA);
                    if ("".equals(o.name())) {
                        q.setName(f.getName());
                    } else {
                        q.setName(o.name());
                    }
                    q.setValue(f.get(obj));
                    queries.add(q);
                }

                if (i != null && f.get(obj) != null) {
                    Query q = new Query();
                    q.setQueryType(QueryType.IDENTITY);
                    if ("".equals(i.name())) {
                        q.setName(f.getName());
                    } else {
                        q.setName(i.name());
                    }
                    q.setValue(f.get(obj));
                    queries.add(q);
                }
            }

            // Go through the list, sort out the ones that already exist in provided Query List.
            for (Query q : ql) {
                List<Query> toDelete = new ArrayList<>();
                for (Query toAdd : queries) {
                    if (q.getName().equals(toAdd.getName())) {
                        toDelete.add(toAdd);
                    }
                }
                queries.removeAll(toDelete);
            }

            // Merge the completed lists and update.
            ql.addAll(queries);
            magicks(table.name(), ql, con);
        }
    }


    private static void magicks(String table, List<Query> ql, Connection con) throws SQLException, IllegalAccessException {
        List<Query> additional = ql.stream().filter(e -> e.getQueryType() == QueryType.DATA ).collect(Collectors.toList());
        List<Query> identity = ql.stream().filter(e -> e.getQueryType() == QueryType.IDENTITY ).collect(Collectors.toList());
        Logger logger = LoggerFactory.getLogger("cm.xd.oopdate.magicks");
        if (table != null) {
            StringWriter where = new StringWriter();
            StringWriter sw = new StringWriter();
            sw.write("UPDATE ");
            sw.write(table);
            sw.write(" SET ");

            if (additional != null && !additional.isEmpty()) {
                for (Query q : additional) {
                    sw.write(q.getName());
                    sw.write(" = ");
                    handleCast(sw, q);
                    sw.write(", ");

                }
            }

            if (identity != null && !identity.isEmpty()) {
                for (Query q : identity) {
                    if (where.toString().length() > 0)
                        where.write(" AND ");
                    where.write(q.getName());
                    where.write(" = ");
                    handleCast(where, q);
                }
            }

            // Cut off the last ,
            sw.getBuffer().delete(sw.toString().length() - 2, sw.toString().length());
            sw.write(" WHERE ");
            sw.write(where.toString());

            PreparedStatement ps = con.prepareStatement(sw.toString());

            int counter = 1;

            if (additional != null && !additional.isEmpty()) {
                for (Query q : additional) {
                    counter = setParameter(ps, counter, q);
                }
            }

            if (identity != null && !identity.isEmpty()) {
                for (Query q : identity) {
                    counter = setParameter(ps, counter, q);
                }
            }

            logger.debug("Executing statement: " + ps.toString());
            ps.execute();
        }
    }

    private static void handleCast(StringWriter sw, Query q) {
        if (q.getCastType() != null) {
            switch (q.getCastType()) {
                case DATERANGE:
                    sw.write("?::daterange");
                    break;
            }
        } else {
            sw.write("?");
        }
    }

    private static int setParameter(PreparedStatement ps, int counter, Object o) throws SQLException, IllegalAccessException {
        Logger logger = LoggerFactory.getLogger("cm.xd.oopdate.magicks.setParameter");

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


    private static int setParameter(PreparedStatement ps, int counter, Query q) throws IllegalAccessException, SQLException {
        Logger logger = LoggerFactory.getLogger("cm.xd.oopdate.magicks.setParameter");
        if (q.getCastType() == null) {
            counter = setParameter(ps, counter, q.getValue());
        } else {
            switch (q.getCastType()) {
                case DATERANGE:
                    ps.setString(counter++, (String) q.getValue() );
                    break;
                default:
                    logger.debug("Got unknown CastType: "+q.getCastType().toString());
                    throw new IllegalAccessException("Unknown CastType: "+q.getCastType().toString());
            }
        }
        return counter;
    }
}
