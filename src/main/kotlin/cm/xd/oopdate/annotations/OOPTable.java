package cm.xd.oopdate.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Table name, required for OOPDate to work on the class.
 *
 * @see OOPField
 * @see OOPIdentityField
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface OOPTable {
    String name();
}
