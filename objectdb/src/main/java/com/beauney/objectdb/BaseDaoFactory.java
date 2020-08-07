package com.beauney.objectdb;

import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

/**
 * @author zengjiantao
 * @since 2020-07-31
 */
public class BaseDaoFactory {
    private String mSQLiteDatabasePath;

    private SQLiteDatabase mSQLiteDatabase;

    private static BaseDaoFactory sInstance = new BaseDaoFactory();

    public static BaseDaoFactory getInstance() {
        return sInstance;
    }

    private BaseDaoFactory() {
        mSQLiteDatabasePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/download.db";
        openDatabase();
    }

    private void openDatabase() {
        mSQLiteDatabase = SQLiteDatabase.openOrCreateDatabase(mSQLiteDatabasePath, null);
    }

    public synchronized <T extends BaseDao<M>,M> T getDataHelper(Class<T> clazz, Class<M> entityClazz) {
        BaseDao baseDao = null;
        try {
            baseDao = clazz.newInstance();
            baseDao.init(entityClazz, mSQLiteDatabase);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return (T) baseDao;
    }
}
