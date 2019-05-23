package io.jpress.commons.status;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 状态定义
 *
 * @author Eric.Huang
 * @date 2019-05-23 23:28
 * @package io.jpress.commons.status
 **/

public class BaseStatus {

    private Map<String, String> map = new LinkedHashMap<String, String>();

    /**
     * 添加状态
     * @param key
     * @param desc
     */
    public void add(String key, String desc) {
        map.put(key, desc);
    }

    /**
     * 根据key返回描述
     * @param key
     * @return
     */
    public String desc(String key) {
        return map.get(key);
    }

    /**
     * 根据描述返回key
     * @param desc
     * @return
     */
    public String key(String desc) {
        for (String _key : map.keySet()) {
            if (desc.equals(map.get(_key))) {
                return _key;
            }
        }

        return null;
    }

    /**
     * 返回全部状态
     * @return
     */
    public Map<String, String> all() {
        return map;
    }
}
