import lombok.Data;
import cm.xd.oopdate.annotations.OOPField;
import cm.xd.oopdate.annotations.OOPIdentityField;
import cm.xd.oopdate.annotations.OOPTable;

@OOPTable(name="mytable")
@Data
public class SimpleFieldTest {
    @OOPField(name="myname")
    String name;

    @OOPField
    @OOPIdentityField
    String country;

    @OOPIdentityField
    String id;

    @OOPField(name="woof")
    Integer woof;
}