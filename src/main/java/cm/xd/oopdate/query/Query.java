package cm.xd.oopdate.query;

import lombok.Data;

@Data
public class Query {
    private CastType castType;
    private QueryType queryType;
    private String name;
    private Object value;

    private static Query getQuery(String name, Object value) {
        Query q = new Query();
        q.setName(name);
        q.setValue(value);
        return q;
    }

    public static Query identity(String name, Object value) {
        Query q = getQuery(name, value);
        q.setQueryType(QueryType.IDENTITY);
        return q;
    }

    public static Query data(String name, Object value) {
        Query q = getQuery(name, value);
        q.setQueryType(QueryType.DATA);
        return q;
    }

    public Query cast(CastType castType) {
        this.castType = castType;
        return this;
    }
}
