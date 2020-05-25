package org.forestframework.http;

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Route;

import java.lang.reflect.Method;
import java.util.List;

public class RegexPathRouting extends AbstractRouting {
    public RegexPathRouting(String path, List<HttpMethod> methods, Class<?> handlerClass, Method handlerMethod, Object handlerInstance) {
        super(path, methods, handlerClass, handlerMethod, handlerInstance);
    }

    @Override
    protected Route configurePath(Route route) {
        return route.pathRegex(getPath());
    }
}
