package cm.xd.oopdate.implementation

import cm.xd.oopdate.annotations.OOPField
import cm.xd.oopdate.annotations.OOPIdentityField
import cm.xd.oopdate.annotations.OOPTable
import cm.xd.oopdate.mapping.OOPMapping
import cm.xd.oopdate.query.Query
import cm.xd.oopdate.query.QueryType
import org.slf4j.LoggerFactory
import java.io.StringWriter
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException

/**
 * Actual class performing work, needs an {@link OOPMapping} to function.
 *
 * @author deepy
 */
class OOPDate {

    private fun OOPDate() {
        // Should not be instantiated.
    }
    
    companion object {
        /**
         * Takes an Annotated kotlin.Any and updates it using JDBC.
         * The List of {@link Query} kotlin.Anys provided will take precedence over the annotations.
         * @param mapper Mapper to use for SQL queries.
         * @param obj Annotated kotlin.Any to update.
         * @param ql List of {@link Query} to also include.
         * @param con Connection to use.
         * @throws SQLException upon SQL failures
         * @throws IllegalAccessException upon reflection failure
         */
        @JvmStatic
        @Throws(SQLException::class, IllegalAccessException::class)
        fun magicks(mapper: OOPMapping, obj: Any, ql: List<Query>?, con: Connection)
        {
            val ql = ql?.toMutableList() ?: mutableListOf()

            val queries = mutableListOf<Query>()

            val table = obj.javaClass.getAnnotation(OOPTable::class.java)
            if (table != null && table.name != null) {
                val fields = obj.javaClass.declaredFields

                fields.forEach {f ->
                    val o = f.getAnnotation(OOPField::class.java)
                    val i = f.getAnnotation(OOPIdentityField::class.java)

                    f.setAccessible(true);
                    if (o != null && f.get(obj) != null) {
                        val q = Query()
                        q.queryType = QueryType.DATA
                        if (o.name.isEmpty()) {
                            q.name = f.name
                        } else {
                            q.name = o.name
                        }
                        q.value = f.get(obj)
                        queries.add(q);
                    }

                    if (i != null && f.get(obj) != null) {
                        val q = Query()
                        q.queryType = QueryType.IDENTITY
                        if (i.name.isEmpty()) {
                            q.name = f.name
                        } else {
                            q.name = i.name
                        }
                        q.value = f.get(obj)
                        queries.add(q);
                    }
                }

                // Go through the list, sort out the ones that already exist in provided Query List.
                ql.forEach {q ->
                    val toDelete = mutableListOf<Query>()
                    queries.forEach {toAdd ->
                    if (q.name.equals(toAdd.name)) {
                        toDelete.add(toAdd);
                    }
                }
                    queries.removeAll(toDelete);
                }

                // Merge the completed lists and update.
                ql.addAll(queries);
                _magicks(mapper, table.name, ql, con)
            }
        }

        @Throws(SQLException::class, IllegalAccessException::class)
        private fun _magicks(mapper: OOPMapping, table: String?, ql: List<Query>, con: Connection)
        {
            val additional: List<Query> = ql
                    .filter {e -> e.queryType == QueryType.DATA}.toList()
            val identity: List<Query>  = ql
                    .filter {e -> e.queryType == QueryType.IDENTITY}.toList()
            val logger = LoggerFactory.getLogger("cm.xd.oopdate.magicks")
            if (table != null) {
                val where = StringWriter()
                val sw = StringWriter()
                sw.write("UPDATE ")
                sw.write(table)
                sw.write(" SET ")

                additional.forEach {q ->
                    sw.write(q.name)
                    sw.write(" = ")
                    handleCast(sw, q)
                    sw.write(", ")

                }

                identity.forEach {q ->
                    if (where.toString().isNotEmpty()) {
                        where.write(" AND ")
                    }
                    where.write(q.name)
                    where.write(" = ")
                    handleCast(where, q)
                }

                // Cut off the last ,
                sw.buffer.delete(sw.toString().length - 2, sw.toString().length)
                sw.write(" WHERE ")
                sw.write(where.toString())

                val ps = con.prepareStatement (sw.toString())

                var counter = 1

                additional.forEach {q ->
                    counter = setParameter(mapper, ps, counter, q)
                }

                identity.forEach {q ->
                    counter = setParameter(mapper, ps, counter, q)
                }

                logger.debug("Executing statement: " + ps.toString())
                ps.execute()
                ps.close()
            }
        }

        private fun handleCast(sw: StringWriter, q: Query)
        {
            if (q.castType != null) {
                sw.write("?::")
                sw.write(q.castType)
            } else {
                sw.write("?")
            }
        }

        @Throws(IllegalAccessException::class, SQLException::class)
        private fun setParameter(mapper: OOPMapping, ps: PreparedStatement, counter: kotlin.Int, q: Query): kotlin.Int
        {
            var next = counter

            val logger = LoggerFactory . getLogger ("cm.xd.oopdate.magicks.setParameter")
            if (q.castType == null) {
                next = mapper.invoke(ps, counter, q.value, logger)
            } else {
                ps.setString(next++, q.value.toString())
            }
            return next
        }
    }
}
