package cm.xd.oopdate.query;

class Query {
    var castType: String? = null
    var queryType: QueryType? = null
    var name: String? = null
    var value: kotlin.Any? = null

    companion object {

        @JvmStatic
        private fun getQuery(name: String, value: kotlin.Any): Query {
            val q = Query()
            q.name = name
            q.value = value
            return q
        }

        @JvmStatic
        fun identity(name: String, value: kotlin.Any): Query
        {
            val q = getQuery(name, value)
            q.queryType = QueryType.IDENTITY
            return q
        }

        @JvmStatic
        fun data (name: String, value: kotlin.Any): Query
        {
            val q = getQuery(name, value)
            q.queryType = QueryType.DATA
            return q
        }
    }

    fun cast(castType: String): Query {
        this.castType = castType
        return this
    }

    /**
     * @deprecated use string method instead.
     * @param castType deprecated, CastType to use
     * @return Query
     */
    @Deprecated(message = "Use cast(String) instead")
    fun cast(castType: CastType): Query {
        this.castType = castType.toString().toLowerCase()
        return this
    }
}
