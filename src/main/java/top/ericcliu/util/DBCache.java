package top.ericcliu.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.log4j.Log4j2;
import top.ericcliu.tools.DataBaseTools;

import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * @author liubi
 * @date 2019-12-11 21:37
 * 单例+guava cache 访问DB
 **/
@Log4j2
public class DBCache {
    //private static final String databasePath = "D://bioportal.sqlite";
    private volatile static DataBaseTools dataBaseTools;
    private volatile static Connection db;
    private volatile static LoadingCache<String, String> singleton;


    public static String getLast(int key, String dbPath) throws ExecutionException {
        String full = get(key, dbPath);
        String[] split = full.split("/");
        return split[split.length - 1];
    }

    public static String get(int key, String dbPath) throws ExecutionException {
        if (singleton == null) {
            synchronized (DBCache.class) {
                if (singleton == null) {
                    dataBaseTools = new DataBaseTools();
                    db = dataBaseTools.sqliteConect(dbPath);
                    singleton = CacheBuilder
                            .newBuilder()
                            .build(new CacheLoader<String, String>() {
                                // 在load方法中定义value的加载方法；
                                // 这个方法要么返回已经缓存的值,要么使用 CacheLoader 向缓存原子地加载新值
                                @Override
                                public String load(String key) throws Exception {
          /*                          String strProValue = "hello " + key + "!";
                                    return strProValue;*/
                                    return dataBaseTools.printer(db, Integer.parseInt(key));
                                }

                                // 默认情况下,对每个不在缓存中的键,getAll 方法会单独调用 CacheLoader.load 来加载缓存项。
                                // 如果批量的加载比多个单独加载更高效,你可以重载 CacheLoader.loadAll 来利用这一点
                                @Override
                                public Map<String, String> loadAll(Iterable<? extends String> keys) throws Exception {
                                    return super.loadAll(keys);
                                }
                            });
                }
            }
        }
        return singleton.get(String.valueOf(key));
    }


    public static void main(String[] args) throws ExecutionException {
        String dbPath = "D:\\bioportal.sqlite";
        log.info(get(10680, dbPath) + System.getProperty("line.separator") + get(-24, dbPath));
        log.info(getLast(10680, dbPath) + System.getProperty("line.separator") + get(-24, dbPath));
        log.info(Integer.MAX_VALUE);
    }
}
