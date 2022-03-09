package com.chen.mytomcat.utils;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Read and parse application.yaml.
 *
 * @author wangguangwu
 */
public class YamlParseUtil {

    @SuppressWarnings("rawtypes")
    private static HashMap properties = new HashMap<>();

    /**
     * 单例
     */
    public static final YamlParseUtil INSTANCE = new YamlParseUtil();


    static {
        Yaml yaml = new Yaml();
        try (InputStream in = YamlParseUtil.class.getClassLoader().getResourceAsStream("application.yml")) {
            properties = yaml.loadAs(in, HashMap.class);
        } catch (Exception e) {
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Object getValueByKey(String key) {
        String separator = ".";
        String[] separatorKeys;
        if (key.contains(separator)) {
            separatorKeys = key.split("\\.");
        } else {
            return properties.get(key);
        }
        Map<Object, Object> finalValue = new HashMap<>(16);
        for (int i = 0; i < separatorKeys.length - 1; i++) {
            if (i == 0) {
                finalValue = (Map) properties.get(separatorKeys[i]);
                continue;
            }
            if (finalValue == null) {
                break;
            }
            finalValue = (Map) finalValue.get(separatorKeys[i]);
        }
        return finalValue == null ? null : finalValue.get(separatorKeys[separatorKeys.length - 1]);
    }
}
