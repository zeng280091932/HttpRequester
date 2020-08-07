package com.beauney.objectdb;

import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zengjiantao
 * @since 2020-07-31
 */
public class BaseDaoFactory {
    private String mSQLiteDatabasePath;

    private SQLiteDatabase mSQLiteDatabase;

    private Map<String, BaseDao> mMap = Collections.synchronizedMap(new HashMap<String, BaseDao>());

    private static BaseDaoFactory sInstance = new BaseDaoFactory();

    public static BaseDaoFactory getInstance() {
        return sInstance;
    }

    private BaseDaoFactory() {
        File file = new File(Environment.getExternalStorageDirectory(), "Databases");
        if (!file.exists()) {
            file.mkdirs();
        }
        mSQLiteDatabasePath = file.getAbsolutePath() + "/download.db";
        openDatabase();
    }

    private void openDatabase() {
        mSQLiteDatabase = SQLiteDatabase.openOrCreateDatabase(mSQLiteDatabasePath, null);
    }

    public synchronized <T extends BaseDao<M>, M> T getDataHelper(Class<T> clazz, Class<M> entityClazz) {
        BaseDao baseDao = null;
        if (mMap.get(clazz.getSimpleName()) != null) {
            return (T) mMap.get(clazz.getSimpleName());
        }
        try {
            baseDao = clazz.newInstance();
            baseDao.init(entityClazz, mSQLiteDatabase);
            mMap.put(clazz.getSimpleName(), baseDao);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return (T) baseDao;
    }
}
