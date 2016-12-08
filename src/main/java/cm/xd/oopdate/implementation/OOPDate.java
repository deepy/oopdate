package cm.xd.oopdate.implementation;

import cm.xd.oopdate.annotations.OOPField;
import cm.xd.oopdate.annotations.OOPIdentityField;
import cm.xd.oopdate.annotations.OOPTable;
import cm.xd.oopdate.mapping.OOPMapping;
import cm.xd.oopdate.query.Query;
import cm.xd.oopdate.query.QueryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Actual class performing work, needs an {@link OOPMapping} to function.
 *
 * @author deepy
 */
public class OOPDate {

    private OOPDate() {
        // Should not be instantiated.
    }

    /**
     * Takes an Annotated Object and updates it using JDBC.
     * The List of {@link Query} objects provided will take precedence over the annotations.
     * @param mapper Mapper to use for SQL queries.
     * @param obj Annotated Object to update.
     * @param ql List of {@link Query} to also include.
     * @param con Connection to use.
     * @throws SQLException upon SQL failures
     * @throws IllegalAccessException upon reflection failure
     */
    public static void magicks(OOPMapping mapper, Object obj, List<Query> ql, Connection con)
            throws SQLException, IllegalAccessException {
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
            magicks(mapper, table.name(), ql, con);
        }
    }


    private static void magicks(OOPMapping mapper,String table, List<Query> ql, Connection con)
            throws SQLException, IllegalAccessException {
        List<Query> additional = ql.stream()
                .filter(e -> e.getQueryType() == QueryType.DATA ).collect(Collectors.toList());
        List<Query> identity = ql.stream()
                .filter(e -> e.getQueryType() == QueryType.IDENTITY ).collect(Collectors.toList());
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
                    if (where.toString().length() > 0) {
                        where.write(" AND ");
                    }
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
                    counter = setParameter(mapper, ps, counter, q);
                }
            }

            if (identity != null && !identity.isEmpty()) {
                for (Query q : identity) {
                    counter = setParameter(mapper, ps, counter, q);
                }
            }

            logger.debug("Executing statement: " + ps.toString());
            ps.execute();
            ps.close();
        }
    }

    private static void handleCast(StringWriter sw, Query q) {
        if (q.getCastType() != null) {
            sw.write("?::");
            sw.write(q.getCastType());
        } else {
            sw.write("?");
        }
    }

    private static int setParameter(OOPMapping mapper, PreparedStatement ps, int counter, Query q)
            throws IllegalAccessException, SQLException {
        Logger logger = LoggerFactory.getLogger("cm.xd.oopdate.magicks.setParameter");
        if (q.getCastType() == null) {
            counter = mapper.invoke(ps, counter, q.getValue(), logger);
        } else {
            ps.setString(counter++, (String) q.getValue() );
        }
        return counter;
    }
}
