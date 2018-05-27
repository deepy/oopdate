import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import cm.xd.oopdate.OOPDate;
import cm.xd.oopdate.query.Query;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

class OOPFieldTest {
    var con: Connection = getConnection()

    fun getConnection(): Connection {
        try {
            return DriverManager.getConnection("jdbc:h2:mem:");
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }


    @Before
    fun setUp() {
        con = getConnection();
        con.prepareCall("CREATE TABLE mytable (id varchar, myname varchar, country varchar, qty INT, )").execute();
    }

    @Test
    fun fieldTest() {
        var ql = mutableListOf<Query>()
        ql.add(Query.data("country", "Norway"))

        var danish = getObject()

        insertFieldTest(danish)

        OOPDate.magicks(danish, ql, con)

        Assert.assertNotEquals(danish, selectFieldTest(danish.id!!))
    }

    @Test
    fun updateIdentityField() {
        val ql = mutableListOf<Query>()
        var danish = getObject();
        insertFieldTest(danish);
        ql.add(Query.identity("id", "not-great"));
        OOPDate.magicks(danish, con);
        var changed = selectFieldTest("not-great");
        Assert.assertNotEquals(danish, changed);
    }

    fun getObject(): SimpleField {
        return getObject("Bacon", "Denmark", "bad", 4)
    }

    fun getObject(name: String, country: String, id: String, quantity: kotlin.Int): SimpleField {
        var obj = SimpleField()
        obj.name = name
        obj.country = country
        obj.id = id
        obj.quantity = quantity
        return obj
    }

    fun insertFieldTest(danish: SimpleField) {
        val insert = con.prepareStatement("INSERT INTO mytable VALUES(?, ?, ?, ?)")
        insert.setString(1, danish.id)
        insert.setString(2, danish.name)
        insert.setString(3, danish.country)
        insert.setInt(4, danish.quantity!!)

        insert.execute()
    }


    fun selectFieldTest(id: String): SimpleField? {
        var ps = con.prepareStatement("SELECT * FROM mytable WHERE id = ?")
        var obj = SimpleField()
        ps.setString(1, id)
        var rs = ps.executeQuery()
        while (rs.next()) {
            obj.name = rs.getString("myname")
            obj.id = rs.getString("id")
            obj.quantity = rs.getInt("qty")
            obj.country = rs.getString("country")
            return obj
        }

        return null
    }

}
