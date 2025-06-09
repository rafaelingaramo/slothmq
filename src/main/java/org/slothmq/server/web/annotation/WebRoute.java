package org.slothmq.server.web.annotation;

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

    /**
     * If the method to be executed needs to be authenticated
     * @return true if it needs to be authenticated/false if not
     */
    boolean needsAuthentication() default false;

    /**
     * The necessary groups for authorizing the method if needed
     * Only checked if needsAuthentication = true
     * @return the comma separated groups that can access the method
     * e.g: viewer,admin
     */
    String authorizationGroups() default "";
}
