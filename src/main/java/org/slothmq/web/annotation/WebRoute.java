package org.slothmq.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WebRoute {
    /**
     * Route Regexp you need to write a regexp that matches the URI
     * @return the route regexp
     */
    String routeRegexp();

    /**
     * The HTTP Method used by the method to be invoked by reflection
     * e.g: GET, HEAD, OPTIONS, POST, PUT, DELETE
     * @return the method
     */
    String method();
}
