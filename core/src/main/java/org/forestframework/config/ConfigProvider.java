package org.forestframework.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.ImmutableMap;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.redis.client.RedisOptions;
import org.apache.commons.beanutils.FluentPropertyBeanIntrospector;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Provides a Vertx options instance from a key.
 *
 * For example, given
 *
 * <pre>
 *     forest:
 *       http:
 *         port: 8081
 *         compressionSupported: false
 *         compressionLevel: 6
 * </pre>
 *
 * You can inject the port by:
 *
 * <pre>
 * @Named("forest.http.port") Integer port;
 * </pre>
 *
 * Or
 * <pre>
 * @Named("forest.http") HttpServerOptions serverOptions.
 * </pre>
 *
 * If the value doesn't exist in the properties file, then:
 *
 * if the property name can be located to a default option, return that default option or value
 * else return null
 */
public class ConfigProvider {
    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());
    private static final Map<String, Class<?>> SUPPORTED_DEFAULT_OPTIONS = ImmutableMap.<String, Class<?>>builder()
            .put("forest.http", HttpServerOptions.class)
            .put("forest.redis", RedisOptions.class)
            .build();
    private static final FluentPropertyBeanIntrospector FLUENT_PROPERTY_BEAN_INTROSPECTOR = new FluentPropertyBeanIntrospector();
    private final Map<String, Object> model;

//    static BeanUtilsBean getUtils() {
//        BeanUtilsBean beanUtilsBean = new BeanUtilsBean();
//        beanUtilsBean.getPropertyUtils().addBeanIntrospector(FLUENT_PROPERTY_BEAN_INTROSPECTOR);
//        Converter stringToIntLongConverter = new org.apache.commons.beanutils.Converter() {
//            @Override
//            public <T> T convert(Class<T> type, Object value) {
//                String valueString = value.toString();
//                if (type == Long.class || type == long.class) {
//                    return (T) DefaultConverter.INSTANCE.convert(valueString, String.class, Long.class);
//                } else {
//                    return (T) DefaultConverter.INSTANCE.convert(valueString, String.class, Integer.class);
//                }
//            }
//        };
//        beanUtilsBean.getConvertUtils().register(stringToIntLongConverter, Integer.class);
//        beanUtilsBean.getConvertUtils().register(stringToIntLongConverter, Long.class);
//        beanUtilsBean.getConvertUtils().register(stringToIntLongConverter, int.class);
//        beanUtilsBean.getConvertUtils().register(stringToIntLongConverter, long.class);
//        return beanUtilsBean;
//    }


    @SuppressWarnings("unchecked")
    public static ConfigProvider fromYaml(String yaml) {
        try {
            return new ConfigProvider(StringUtils.isBlank(yaml)
                    ? Collections.emptyMap()
                    : (Map<String, Object>) YAML_MAPPER.readValue(yaml, Map.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public ConfigProvider(Map<String, Object> model) {
        this.model = model;
    }

    private static class ConfigObject {
        private String path;
        private Object defaultValue;
        private Object configValue;
        private Object environmentValue;


        public ConfigObject(Map<String, Object> rootModel) {
            this("", null, rootModel, null);
        }

        public ConfigObject(String path, Object defaultValue, Object configValue, Object environmentValue) {
            this.path = path;
            this.defaultValue = defaultValue;
            this.configValue = configValue;
            this.environmentValue = environmentValue;
        }

        public ConfigObject getObject(String key) {
            String childPath = path.isEmpty() ? key : path + "." + key;
            Class<?> klass = SUPPORTED_DEFAULT_OPTIONS.get(childPath);
            Object childDefaultValue = klass == null ? PropertyUtils.getProperty(defaultValue, key) : newInstance(klass);
            Object childConfigValue = PropertyUtils.getProperty(configValue, key);
            Object childEnvironmentValue = PropertyUtils.getProperty(environmentValue, key);
            return new ConfigObject(childPath, childDefaultValue, childConfigValue, childEnvironmentValue);
        }

        private <T> T newInstance(Class<T> klass) {
            try {
                return klass.getConstructor().newInstance();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        public Object getResult(Class<?> klass) {
            Object rawResult = getRawResult();
            if (rawResult == null) {
                return null;
            } else if (isNonNestedPropertyType(klass)) {
                return DefaultConverter.INSTANCE.convert(rawResult, rawResult.getClass(), klass);
            } else {
                // Java bean
                mergeConfigValue();
                return DefaultConverter.INSTANCE.convert(defaultValue, defaultValue.getClass(), klass);
            }
        }

        // BeanUtils.copyProperties() is shallow copy, so we filter out non-nested properties, invoke copyProperties,
        // then manually copy nested fields recursively
        private void mergeConfigValueToDefault(Object destBean, Map<String, Object> srcMap) {
            if (srcMap != null) {
                srcMap.forEach((String key, Object value) -> {
                    if (isNonNestedPropertyType(value.getClass())) {
                        PropertyUtils.setProperty(destBean, key, value);
                    } else {
                        mergeConfigValueToDefault(PropertyUtils.getProperty(destBean, key), (Map<String, Object>) value);
                    }
                });
            }
        }


        private boolean isNonNestedPropertyType(Class<?> klass) {
            return klass.isPrimitive()
                    || klass == Boolean.class
                    || Number.class.isAssignableFrom(klass)
                    || klass == String.class
                    || klass.isEnum()
                    || List.class.isAssignableFrom(klass);
        }

        private Object mergeConfigValue() {
            mergeConfigValueToDefault(defaultValue, (Map<String, Object>) configValue);
            mergeConfigValueToDefault(defaultValue, (Map<String, Object>) environmentValue);
            return defaultValue;
        }

        private Object getRawResult() {
            if (environmentValue != null) {
                return environmentValue;
            } else if (configValue != null) {
                return configValue;
            } else {
                return defaultValue;
            }
        }
    }

    public <T> T getInstance(String key, Class<T> klass) {
        List<String> paths = Arrays.asList(key.split("\\."));
        ConfigObject current = new ConfigObject(model);

        for (String path : paths) {
            current = current.getObject(path);
        }

        return (T) current.getResult(klass);
    }
}