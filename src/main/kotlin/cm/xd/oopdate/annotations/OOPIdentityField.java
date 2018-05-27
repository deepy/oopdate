package cm.xd.oopdate.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Identity Field gets added to the WHERE part of the query.
 *
 * @see OOPField
 * @see OOPTable
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface OOPIdentityField {
    String name() default "";
}
