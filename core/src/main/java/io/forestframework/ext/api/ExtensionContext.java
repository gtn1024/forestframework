package io.forestframework.ext.api;

import io.forestframework.core.config.ConfigProvider;
import io.vertx.core.Vertx;
import org.apiguardian.api.API;

import java.util.List;

@API(status = API.Status.STABLE, since = "1.0")
public class ExtensionContext {
    private final Vertx vertx;
    private final Class<?> applicationClass;
    private final ConfigProvider configProvider;
    private final List<Class<?>> componentClasses;

    public ExtensionContext(Vertx vertx, Class<?> applicationClass, ConfigProvider configProvider, List<Class<?>> componentClasses) {
        this.vertx = vertx;
        this.applicationClass = applicationClass;
        this.configProvider = configProvider;
        this.componentClasses = componentClasses;
    }

    public Vertx getVertx() {
        return vertx;
    }

    public Class<?> getApplicationClass() {
        return applicationClass;
    }

    public ConfigProvider getConfigProvider() {
        return configProvider;
    }

    public List<Class<?>> getComponentClasses() {
        return componentClasses;
    }
}
