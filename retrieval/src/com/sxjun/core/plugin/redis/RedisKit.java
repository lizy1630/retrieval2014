package com.sxjun.core.plugin.redis;


import java.util.List;

import org.apache.log4j.Logger;

import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Transaction;

import com.jfinal.plugin.ehcache.IDataLoader;


/**
 * User: sxjun
 * Date: 13-10-11
 * Time: 上午7:21
 */
public class RedisKit {
    private static final Logger logger = Logger.getLogger(RedisKit.class);
    private static volatile RedisManager redisManager;


    static void init(RedisManager redisManager) {
        RedisKit.redisManager = redisManager;
    }


    public static RedisManager getRedisManager() {
        return redisManager;
    }


    public static void put(String cacheName, Object key, Object value) {
        RedisManager.getJedisCache(cacheName, redisManager).put(key,value);
    }

    public static Transaction mutil(String cacheName) {
        return RedisManager.getJedisCache(cacheName, redisManager).mutil();
    }
    
    public static <T> List<T> exec(String cacheName,Transaction transaction) {
        return (List<T>) RedisManager.getJedisCache(cacheName, redisManager).exec(transaction);
    }
    
    public Pipeline pipeline(String cacheName){
    	return RedisManager.getJedisCache(cacheName, redisManager).pipeline();
    }
    
    public void pipelineSync(String cacheName, Pipeline p){
    	RedisManager.getJedisCache(cacheName, redisManager).pipelineSync(p);
    }


    @SuppressWarnings("unchecked")
    public static <T> T get(String cacheName, Object key) {
        Object obj = RedisManager.getJedisCache(cacheName, redisManager).get(key);
        return obj == null ? null : (T)obj;
    }


    public static List getKeys(String cacheName) {
        return RedisManager.getJedisCache(cacheName, redisManager).getKeys();
    }


    public static void remove(String cacheName, Object key) {
        RedisManager.getJedisCache(cacheName, redisManager).remove(key);
    }


    public static void removeAll(String cacheName) {
        RedisManager.getJedisCache(cacheName, redisManager).removeAll();
    }


    @SuppressWarnings("unchecked")
    public static <T> T get(String cacheName, Object key, IDataLoader dataLoader) {
        Object data = get(cacheName, key);
        if (data == null) {
            data = dataLoader.load();
            put(cacheName, key, data);
        }
        return (T)data;
    }


    @SuppressWarnings("unchecked")
    public static <T> T get(String cacheName, Object key, Class<? extends IDataLoader> dataLoaderClass) {
        Object data = get(cacheName, key);
        if (data == null) {
            try {
                IDataLoader dataLoader = dataLoaderClass.newInstance();
                data = dataLoader.load();
                put(cacheName, key, data);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return (T)data;
    }
    
    
    


    public static void main(String[] args) {
        RedisPlugin redisPlugin = new RedisPlugin("127.0.0.1", 6379, 0);
        redisPlugin.start();
        RedisKit.put("user", "name", "Michael");
        String name = RedisKit.get("user", "name");
        System.out.println(name);
        RedisKit.remove("user", "name");
        String name1 = RedisKit.get("user", "name");
        System.out.println(name1);
    }

}