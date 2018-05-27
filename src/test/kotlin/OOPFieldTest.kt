import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import cm.xd.oopdate.OOPDate;
import cm.xd.oopdate.query.Query;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

class OOPFieldTest {
    private var con: Connection = getConnection()

    private fun getConnection(): Connection {
        return DriverManager.getConnection("jdbc:h2:mem:")
    }

    @Before
    fun setUp() {
        con = getConnection();
        val sql = "CREATE TABLE mytable (id varchar, myname varchar, country varchar, qty INT, )"
        con.prepareCall(sql).execute()
    }

    @Test
    fun fieldTest() {
        val ql = mutableListOf<Query>()
        ql.add(Query.data("country", "Norway"))

        val danish = getObject()

        insertFieldTest(danish)

        OOPDate.magicks(danish, ql, con)

        Assert.assertNotEquals(danish, selectFieldTest(danish.id!!))
    }

    @Test
    fun updateIdentityField() {
        val ql = mutableListOf<Query>()
        val danish = getObject();
        insertFieldTest(danish);
        ql.add(Query.identity("id", "not-great"));
        OOPDate.magicks(danish, con);
        val changed = selectFieldTest("not-great");
        Assert.assertNotEquals(danish, changed);
    }

    private fun getObject(): SimpleField {
        return getObject("Bacon", "Denmark", "bad", 4)
    }

    private fun getObject(name: String, country: String, id: String, quantity: kotlin.Int): SimpleField {
        val obj = SimpleField(quantity)
        obj.name = name
        obj.country = country
        obj.id = id
        return obj
    }

    private fun insertFieldTest(danish: SimpleField) {
        val insert = con.prepareStatement("INSERT INTO mytable VALUES(?, ?, ?, ?)")
        insert.setString(1, danish.id)
        insert.setString(2, danish.name)
        insert.setString(3, danish.country)
        insert.setInt(4, danish.quantity)

        insert.execute()
    }


    private fun selectFieldTest(id: String): SimpleField? {
        val ps = con.prepareStatement("SELECT * FROM mytable WHERE id = ?")
        ps.setString(1, id)
        val rs = ps.executeQuery()
        while (rs.next()) {
            val obj = SimpleField(rs.getInt("qty"))
            obj.name = rs.getString("myname")
            obj.id = rs.getString("id")
            obj.country = rs.getString("country")
            return obj
        }

        return null
    }

}
