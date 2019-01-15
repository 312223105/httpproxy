package sdt;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by xuzhe on 2019/1/15.
 */
public class Domains {
    private static final Map<String, InetSocketAddress> map = new HashMap<>();
    static {
        refresh();
    }
    public static void refresh() {
        synchronized (map) {
            map.clear();
            try {
                JSONObject object = JSON.parseObject(FileUtils.readFileToString(
                        new File("./domains.json"), "utf-8"));
                for(Map.Entry<String, Object> obj : object.getJSONObject("domains").entrySet()) {
                    String value = obj.getValue().toString();
                    String[] items = value.split(":");
                    map.put(obj.getKey(), new InetSocketAddress(items[0], Integer.valueOf(items[1])));
                }
            } catch (IOException e) {
                throw new IOError(e);
            }
        }
    }

    public static InetSocketAddress get(String domainName) {
        synchronized (map) {
            return map.get(domainName);
        }
    }
}
