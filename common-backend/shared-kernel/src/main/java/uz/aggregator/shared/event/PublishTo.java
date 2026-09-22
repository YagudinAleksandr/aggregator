package uz.aggregator.shared.event;

import java.lang.annotation.*;

/**
 * Topic annotation for integration event
 *
 * @author Aleksandr Yagudin
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PublishTo {
    /**
     * Topic name
     *
     * @return name
     */
    String topic();

    /**
     * Model version
     *
     * @return version
     */
    int version() default 1;
}
