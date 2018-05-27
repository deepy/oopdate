import cm.xd.oopdate.annotations.OOPField
import cm.xd.oopdate.annotations.OOPIdentityField
import cm.xd.oopdate.annotations.OOPTable

@OOPTable(name = "mytable")
class SimpleFieldTest {
    @OOPField(name = "myname")
    var name: String? = null

    @OOPField
    @OOPIdentityField
    var country: String? = null

    @OOPIdentityField
    var id: String? = null

    @OOPField(name = "qty")
    var quantity: kotlin.Int? = null
}