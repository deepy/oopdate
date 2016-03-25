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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.SortedMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OOPDate {

    public static void magicks(Object obj, SortedMap<String, Object> additional, SortedMap<String, Object> identity, Connection con) throws Exception {
        List<Query> ql = null;
        if (additional != null && identity != null) {
            ql =
                    Stream.concat(
                            additional.entrySet().stream()
                                    .map(e -> Query.data(e.getKey(), e.getValue())),
                            identity.entrySet().stream()
                                    .map(e -> Query.identity(e.getKey(), e.getValue()))
                    ).collect(Collectors.toList());
        } else if (additional != null) {
            ql = additional.entrySet().stream()
                    .map(e -> Query.data(e.getKey(), e.getValue())).collect(Collectors.toList());
        } else if (identity != null) {
            ql = identity.entrySet().stream()
                    .map(e -> Query.identity(e.getKey(), e.getValue())).collect(Collectors.toList());
        }
        magicks(obj, ql, con);
    }

    public static void magicks(Object obj, List<Query> ql, Connection con) throws Exception {
        List<Query> additional = null;
        List<Query> identity = null;
        if (ql != null) {
            additional = ql.stream().filter(e -> e.getQueryType() == QueryType.DATA ).collect(Collectors.toList());
            identity = ql.stream().filter(e -> e.getQueryType() == QueryType.IDENTITY ).collect(Collectors.toList());
        }
        Logger logger = LoggerFactory.getLogger("cm.xd.oopdate.magicks");
        OOPTable table = obj.getClass().getAnnotation(OOPTable.class);
        if (table != null && table.name() != null) {
            StringWriter where = new StringWriter();
            StringWriter sw = new StringWriter();
            sw.write("UPDATE ");
            sw.write(table.name());
            sw.write(" SET ");

            Field[] fields = obj.getClass().getDeclaredFields();

            for (Field f : fields) {
                OOPField o = f.getAnnotation(OOPField.class);
                OOPIdentityField i = f.getAnnotation(OOPIdentityField.class);

                f.setAccessible(true);
                if (o != null && f.get(obj) != null) {
                    if ("".equals(o.name())) {
                        sw.write(f.getName());
                    } else {
                        sw.write(o.name());
                    }
                    sw.write(" = ?, ");
                }

                if (i != null && f.get(obj) != null) {
                    if (where.toString().length() > 0)
                        where.write(" AND ");
                    if ("".equals(i.name())) {
                        where.write(f.getName());
                    } else {
                        where.write(i.name());
                    }
                    where.write(" = ?");
                }
            }

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
            obj.getClass();

            for (Field f : fields) {
                Class type = f.getType();
                OOPField o = f.getAnnotation(OOPField.class);
                if (o != null) {
                    counter = setParameter(ps, counter, f.get(obj), type.toString());
                }
            }

            if (additional != null && !additional.isEmpty()) {
                for (Query q : additional) {
                    Class type = q.getValue().getClass();
                    counter = setParameter(ps, counter, q, type.toString());
                }
            }

            for (Field f : fields) {
                Class type = f.getType();
                OOPIdentityField i = f.getAnnotation(OOPIdentityField.class);
                if (i != null) {
                    counter = setParameter(ps, counter, f.get(obj), type.toString());
                }
            }

            if (identity != null && !identity.isEmpty()) {
                for (Query q : identity) {
                    Class type = q.getValue().getClass();
                    counter = setParameter(ps, counter, q, type.toString());
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

    private static int setParameter(PreparedStatement ps, int counter, Object o, String type) throws SQLException, IllegalAccessException {
        Logger logger = LoggerFactory.getLogger("cm.xd.oopdate.magicks.setParameter");
        switch (type) {
            case "class java.lang.String":
                ps.setString(counter++, (String) o);
                break;
            case "class java.lang.Integer":
            case "int":
                ps.setInt(counter++, (Integer) o);
                break;
            case "class java.math.BigDecimal":
                ps.setBigDecimal(counter++, (BigDecimal) o);
                break;
            default:
                logger.debug("Got unknown type: " + type);
                throw new IllegalAccessException("Got unknown type: " + type);
        }
        return counter;
    }


    private static int setParameter(PreparedStatement ps, int counter, Query q, String type) throws IllegalAccessException, SQLException {
        Logger logger = LoggerFactory.getLogger("cm.xd.oopdate.magicks.setParameter");
        if (q.getCastType() == null) {
            counter = setParameter(ps, counter, q.getValue(), type);
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
