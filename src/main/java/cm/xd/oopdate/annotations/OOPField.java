package cm.xd.oopdate.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Fields get added to to the update query.
 *
 * @see OOPIdentityField
 * @see OOPTable
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface OOPField {
    String name() default "";
}
