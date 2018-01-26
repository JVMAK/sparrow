package com.sparrow.cache.impl.redis;

import com.sparrow.cache.CacheDataNotFound;
import com.sparrow.cache.CacheSet;
import com.sparrow.constant.cache.KEY;
import com.sparrow.core.TypeConverter;
import com.sparrow.exception.CacheConnectionException;
import com.sparrow.support.Entity;
import redis.clients.jedis.ShardedJedis;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by harry on 2018/1/26.
 */
public class RedisCacheSet extends AbstractCommand implements CacheSet {
    RedisCacheSet(RedisPool redisPool) {
        this.redisPool = redisPool;
    }

    @Override
    public Long getSize(final KEY key) throws CacheConnectionException {
        return redisPool.execute(new Executor<Long>() {
            @Override
            public Long execute(ShardedJedis jedis) {
                return jedis.scard(key.key());
            }
        }, key);
    }

    @Override
    public Long add(final KEY key, final Object value) throws CacheConnectionException {
        return redisPool.execute(new Executor<Long>() {
            @Override
            public Long execute(ShardedJedis jedis) {
                TypeConverter typeConverter = new TypeConverter(String.class);
                return jedis.sadd(key.key(), typeConverter.convert(value).toString());
            }
        }, key);
    }

    @Override
    public Long add(final KEY key, final String... value) throws CacheConnectionException {
        return redisPool.execute(new Executor<Long>() {
            @Override
            public Long execute(ShardedJedis jedis) {
                return jedis.sadd(key.key(), value);
            }
        }, key);
    }

    @Override
    public <T> Integer add(final KEY key, final Iterable<T> values) throws CacheConnectionException {
        return redisPool.execute(new Executor<Integer>() {
            @Override
            public Integer execute(ShardedJedis jedis) {
                int i = 0;
                TypeConverter typeConverter=new TypeConverter(String.class);
                for (Object value : values) {
                    if (value == null) {
                        continue;
                    }
                    i++;
                    jedis.sadd(key.key(), typeConverter.convert(value).toString());
                }
                return i;
            }
        }, key);
    }

    @Override
    public Long remove(final KEY key, final Object value) throws CacheConnectionException {
        return redisPool.execute(new Executor<Long>() {
            @Override
            public Long execute(ShardedJedis jedis) {
                return jedis.srem(key.key(), value.toString());
            }
        }, key);
    }

    @Override
    public Boolean exist(final KEY key, final Object value) throws CacheConnectionException {
        return redisPool.execute(new Executor<Boolean>() {
            @Override
            public Boolean execute(ShardedJedis jedis) {
                return jedis.sismember(key.key(), value.toString());
            }
        }, key);
    }

    @Override
    public Set<String> list(final KEY key) throws CacheConnectionException {
        return this.list(key, String.class);
    }

    @Override
    public <T> Set<T> list(final KEY key, final Class clazz) throws CacheConnectionException {
        return redisPool.execute(new Executor<Set<T>>() {
            @Override
            public Set<T> execute(ShardedJedis jedis) throws CacheConnectionException {
                Set<String> set = jedis.smembers(key.key());
                Set<T> typeSet = new HashSet<T>(set.size());
                TypeConverter typeConverter=new TypeConverter(clazz);
                for (String json : set) {
                    typeSet.add((T) typeConverter.convert(json));
                }
                return typeSet;
            }
        }, key);
    }


    @Override
    public Set<String> list(final KEY key, CacheDataNotFound<Set<String>> hook) {
        return this.list(key, String.class, hook);
    }

    @Override
    public <T> Set<T> list(final KEY key, final Class clazz, final CacheDataNotFound<Set<T>> hook) {
        try {
            return redisPool.execute(new Executor<Set<T>>() {
                @Override
                public Set<T> execute(ShardedJedis jedis) throws CacheConnectionException {
                    Set<String> list = jedis.smembers(key.key());
                    Set<T> typeSet = null;
                    if (list == null || list.size() == 0) {
                        typeSet = hook.read(key);
                        RedisCacheSet.this.add(key, typeSet);
                        return typeSet;
                    }

                    typeSet = new HashSet<T>(list.size());
                    TypeConverter typeConverter=new TypeConverter(clazz);
                    for (String s : list) {
                        typeSet.add((T)typeConverter.convert(s));
                    }
                    return typeSet;
                }
            }, key);
        } catch (CacheConnectionException e) {
            return hook.read(key);
        }
    }
}
