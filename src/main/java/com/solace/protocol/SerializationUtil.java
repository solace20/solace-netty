package com.solace.protocol;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: Solace
 * @Description: 序列化工具
 * @date: 2018/5/17
 * @CopyRight: lengbar.cn
 */
public class SerializationUtil {
    private static Map<Class<?>,Schema<?>> cachedSchema = new ConcurrentHashMap<>();

    private static Objenesis objenesis = new ObjenesisStd(true);

    /**
     * 构建schema的过程可能会比较耗时，因此希望使用过的类对应的schema能被缓存起来
     * @param cls
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    private static <T> Schema<T> getSchema(Class<T> cls){
        Schema<T> schema = (Schema<T>) cachedSchema.get(cls);
        if (schema == null){
            schema = RuntimeSchema.createFrom(cls);
            if(schema!=null){
                cachedSchema.put(cls,schema);
            }
        }
        return schema;
    }

    /**
     * 序列化
     * @param obj
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> byte[] serialize(T obj){
        Class<T> cls = (Class<T>) obj.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            Schema<T> schema = getSchema(cls);
            return ProtostuffIOUtil.toByteArray(obj,schema,buffer);
        }catch (Exception e){
            throw new IllegalStateException(e.getMessage(),e);
        }finally {
            buffer.clear();
        }
    }

    /**
     * 反序列化
     * @param data
     * @param cls
     * @param <T>
     * @return
     */
    public static <T> T deserialize(byte[] data,Class<T> cls){
        try {
            T message = (T) objenesis.newInstance(cls);
            Schema<T> schema = getSchema(cls);
            ProtostuffIOUtil.mergeFrom(data,message,schema);
            return message;
        }catch (Exception e){
            throw new IllegalStateException(e.getMessage(),e);
        }
    }


}
