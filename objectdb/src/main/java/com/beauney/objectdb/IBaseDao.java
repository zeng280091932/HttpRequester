package com.beauney.objectdb;

import java.util.List;

/**
 * @author zengjiantao
 * @since 2020-07-31
 */
public interface IBaseDao<T> {
    long insert(T entity);

    int update(T entity, T where);

    int delete(T where);

    List<T> query(T where);

    List<T> query(T where, String orderBy, Integer startIndex, Integer limit);
}
