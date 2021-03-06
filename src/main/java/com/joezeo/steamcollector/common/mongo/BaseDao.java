package com.joezeo.steamcollector.common.mongo;

import dev.morphia.Datastore;
import dev.morphia.query.Query;
import dev.morphia.query.experimental.filters.Filter;
import dev.morphia.query.experimental.filters.Filters;
import dev.morphia.query.experimental.updates.UpdateOperator;
import dev.morphia.query.experimental.updates.UpdateOperators;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Dao类对象的基类
 * 所有dao都应继承此类
 *
 * 泛型：
 *      K：表示集合_id属性的类型
 *      T：表示数据库集合对应的vo对象类
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2020/9/2 18:32
 */
public abstract class BaseDao<K,T extends DBDocument<K>> {
    {
        classOf();
    }

    @Autowired
    protected Datastore datastore;

    protected Class<T> clazz;

    /**please use @method:of(Class<T> clazz) in the override method to init the Document class*/
    abstract protected void classOf();

    protected void classOf(Class<T> clazz) {
        this.clazz = clazz;
    }

    /** add */
    public boolean save(DBDocument<K> document) {
        try {
            this.datastore.save(document);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /** delete */
    public boolean delete(K id) {
        try {
            Query<T> query = query();
            Filter filter = Filters.eq("_id", id);
            query.filter(filter);
            query.delete();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /** update */
    public boolean update(DBDocument<K> document) {
        try {
            Query<T> query = query();
            Filter filter = Filters.eq("_id", document.getId());
            query.filter(filter);

            Class<? extends DBDocument> dClass = document.getClass();
            List<UpdateOperator> operators = new ArrayList<>();
            Arrays.stream(dClass.getDeclaredFields()).forEach(field -> {
                field.setAccessible(true);
                String fieldName = field.getName();
                try {
                    Object val = field.get(document);
                    UpdateOperator operator = UpdateOperators.set(fieldName, val);
                    operators.add(operator);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    throw new RuntimeException("<错误> 反射设置值发生错误，更新失败");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /** query */
    public DBDocument<K> query(K id) {
        Query<T> query = query();
        Filter filter = Filters.eq("_id", id);
        query.filter(filter);
        return query.first();
    }

    protected Query<T> query() {
        return datastore.find(clazz);
    }
}
