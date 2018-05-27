import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import cm.xd.oopdate.OOPDate;
import cm.xd.oopdate.query.Query;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OOPFieldTest {
    private Connection con;

    public Connection getConnection() {
        try {
            return DriverManager.getConnection("jdbc:h2:mem:");
        } catch (Exception e) {
            return null;
        }
    }


    @Before
    public void setUp() throws Exception {
        con = getConnection();
        con.prepareCall("CREATE TABLE mytable (id varchar, myname varchar, country varchar, qty INT, )").execute();
    }

    @Test
    public void fieldTest() throws Exception {
        List<Query> ql = new ArrayList<>();
        ql.add(Query.data("country", "Norway"));

        SimpleFieldTest danish = getObject();

        insertFieldTest(danish);

        OOPDate.magicks(danish, ql, con);

        Assert.assertNotEquals(danish, selectFieldTest(danish.getId()));
    }

    @Test
    public void updateIdentityField() throws Exception {
        List<Query> ql = new ArrayList<>();
        SimpleFieldTest danish = getObject();
        insertFieldTest(danish);
        ql.add(Query.identity("id", "not-great"));
        OOPDate.magicks(danish, con);
        SimpleFieldTest changed = selectFieldTest("not-great");
        Assert.assertNotEquals(danish, changed);
    }

    private SimpleFieldTest getObject() {
        return getObject("Bacon", "Denmark", "bad", 4);
    }

    private SimpleFieldTest getObject(String name, String country, String id, int quantity) {
        SimpleFieldTest obj = new SimpleFieldTest();
        obj.setName(name);
        obj.setCountry(country);
        obj.setId(id);
        obj.setQuantity(quantity);
        return obj;
    }

    private void insertFieldTest(SimpleFieldTest danish) throws SQLException {
        PreparedStatement insert = con.prepareStatement("INSERT INTO mytable VALUES(?, ?, ?, ?)");
        insert.setString(1, danish.getId());
        insert.setString(2, danish.getName());
        insert.setString(3, danish.getCountry());
        insert.setInt(4, danish.getQuantity());

        insert.execute();
    }


    private SimpleFieldTest selectFieldTest(String id) throws Exception {
        PreparedStatement ps = con.prepareStatement("SELECT * FROM mytable WHERE id = ?");
        SimpleFieldTest obj = new SimpleFieldTest();
        ps.setString(1, id);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            obj.setName(rs.getString("myname"));
            obj.setId(rs.getString("id"));
            obj.setQuantity(rs.getInt("qty"));
            obj.setCountry(rs.getString("country"));
            return obj;
        }

        return null;
    }

}
