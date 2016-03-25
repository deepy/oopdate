import org.junit.Before;
import org.junit.Test;
import cm.xd.oopdate.OOPDate;
import cm.xd.oopdate.query.Query;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OOPFieldTest {
    Connection con;

    public Connection getConnection() {
        try {
            Connection con = DriverManager.getConnection("jdbc:h2:mem:");
            return con;
        } catch (Exception e) {
            return null;
        }
    }


    @Before
    public void setUp() throws Exception {
        con = getConnection();
        con.prepareCall("CREATE TABLE mytable (id int, myname varchar, country varchar, woof INT, " +
                "int INT, str VARCHAR" +
                ")").execute();
    }

    @Test
    public void fieldTest() throws Exception {
        HashMap<String, Object> hm = new HashMap<>();
        List<Query> ql = new ArrayList<>();
        ql.add(Query.data("int", new Integer(1)));
        ql.add(Query.data("str", "String"));
        hm.put("int", new Integer(1));
        hm.put("str", "String");

        SimpleFieldTest test = new SimpleFieldTest();
        test.setName("Bacon");
        test.setCountry("Denmark");

        test.setId("woof");
        test.setWoof(4);

        OOPDate.magicks(test, ql, con);
    }

}
