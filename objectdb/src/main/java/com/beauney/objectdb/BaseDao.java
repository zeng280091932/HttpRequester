package com.beauney.objectdb;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;


import com.beauney.objectdb.annotation.DbField;
import com.beauney.objectdb.annotation.DbTable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zengjiantao
 * @since 2020-07-31
 */
public abstract class BaseDao<T> implements IBaseDao<T> {
    private static final String TAG = "BaseDao";

    protected SQLiteDatabase mSQLiteDatabase;

    /**
     * 实例类的引用
     */
    private Class<T> mClazz;

    /**
     * 维护表名称
     */
    private String mTableName;


    /**
     * 确保之初始化一次
     */
    private boolean mIsInit = false;

    protected synchronized void init(Class<T> clazz, SQLiteDatabase sqLiteDatabase) {
        if (!mIsInit) {
            mClazz = clazz;
            mSQLiteDatabase = sqLiteDatabase;
            //初始化表名称
            DbTable dbTable = clazz.getAnnotation(DbTable.class);
            if (dbTable != null) {
                mTableName = dbTable.value();
            } else {
                mTableName = clazz.getSimpleName();
            }

            if (!mSQLiteDatabase.isOpen()) {
                return;
            }

            //创建数据库表
            if (!TextUtils.isEmpty(createTable())) {
                mSQLiteDatabase.execSQL(createTable());
            }
            mIsInit = true;
        }
    }

    protected abstract String createTable();

    @Override
    public long insert(T entity) {
        ContentValues values = createContentValues(entity);
        Log.d(TAG, "values------>" + values);
        return mSQLiteDatabase.insert(mTableName, null, values);
    }

    @Override
    public int update(T entity, T where) {
        ContentValues values = createContentValues(entity);
        Condition condition = new Condition(mClazz, where);
        return mSQLiteDatabase.update(mTableName, values, condition.getWhereClause(), condition.getWhereArgs());
    }

    @Override
    public int delete(T where) {
        Condition condition = new Condition(mClazz, where);
        return mSQLiteDatabase.delete(mTableName, condition.getWhereClause(), condition.getWhereArgs());
    }

    @Override
    public List<T> query(T where) {
        return query(where, null, null, null);
    }

    @Override
    public List<T> query(T where, String orderBy, Integer startIndex, Integer limit) {
        String limitString = null;
        if (startIndex != null && limit != null) {
            limitString = startIndex + " , " + limit;
        }
        Condition condition = new Condition(mClazz, where);
        Cursor cursor = mSQLiteDatabase.query(mTableName, null, condition.getWhereClause(), condition.getWhereArgs(), null, orderBy, limitString);
        List<T> list = new ArrayList<>();
        try {
            if (cursor != null) {
//                Object item;
                T item;
                while (cursor.moveToNext()) {
                    item = mClazz.newInstance();
//                    item = mClazz.newInstance();
                    Field[] fields = mClazz.getDeclaredFields();
                    for (Field field : fields) {
                        field.setAccessible(true);
                        DbField dbField = field.getAnnotation(DbField.class);
                        String columnName;
                        if (dbField != null) {
                            columnName = dbField.value();
                        } else {
                            columnName = field.getName();
                        }
                        int columnIndex = cursor.getColumnIndex(columnName);
                        Class type = field.getType();
                        if (type.equals(String.class)) {
                            field.set(item, cursor.getString(columnIndex));
                        } else if (type.equals(Integer.class)) {
                            field.set(item, cursor.getInt(columnIndex));
                        } else if (type.equals(Long.class)) {
                            field.set(item, cursor.getLong(columnIndex));
                        } else if (type.equals(Double.class)) {
                            field.set(item, cursor.getDouble(columnIndex));
                        } else if (type.equals(byte[].class)) {
                            field.set(item, cursor.getBlob(columnIndex));
                        }
                    }
                    list.add(item);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "查询数据库出错" + e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return list;
    }

    class Condition {
        private String whereClause;

        private String[] whereArgs;

        public Condition(Class<T> clazz, T entity) {
            if (entity != null) {
                List<String> args = new ArrayList<>();
                Field[] fields = clazz.getDeclaredFields();
                StringBuilder builder = new StringBuilder();
                builder.append("1=1");
                for (Field field : fields) {
                    field.setAccessible(true);
                    Object object = null;
                    try {
                        object = field.get(entity);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    if (object == null) {
                        continue;
                    }
                    DbField dbField = field.getAnnotation(DbField.class);
                    String columnName;
                    if (dbField != null) {
                        columnName = dbField.value();
                    } else {
                        columnName = field.getName();
                    }
                    builder.append(" and ").append(columnName).append("=?");
                    args.add(object.toString());
                }
                whereClause = builder.toString();
                whereArgs = args.toArray(new String[args.size()]);
                Log.d(TAG, "whereClause------>" + whereClause);
                Log.d(TAG, "whereArgs------>" + whereArgs.toString());
            }
        }

        public String getWhereClause() {
            return whereClause;
        }

        public String[] getWhereArgs() {
            return whereArgs;
        }
    }

    private ContentValues createContentValues(T entity) {
        ContentValues contentValues = new ContentValues();
        Field[] fields = mClazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            Object object = null;
            try {
                object = field.get(entity);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            if (object == null) {
                continue;
            }
            DbField dbField = field.getAnnotation(DbField.class);
            String columnName;
            if (dbField != null) {
                columnName = dbField.value();
            } else {
                columnName = field.getName();
            }
            contentValues.put(columnName, object.toString());
        }
        return contentValues;
    }

    public String getTableName() {
        return mTableName;
    }
}
