package com.baijiahulian.bjvideoplayerdemo;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.IntDef;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.DecimalFormat;

public class StorageUtil {

    public static final String VIDEO_DIRECTORY = "/v/"; // 视频目录
    public static final String MATERIAL_DIRECTORY = "/m/"; // 课件目录
    public static final String SANDBOX_DIRECTORY = "/Android/data/%s/files";
    public static final String EXTERNAL_DIRECTORY = "/genshuixue";

    public final static int UNIT_STORAGE_GB = 0;
    public final static int UNIT_STORAGE_MB = 1;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({UNIT_STORAGE_GB, UNIT_STORAGE_MB})
    public @interface UnitStorage {
    }

    /**
     * 获取SdCard卡根目录
     *
     * @return 有SdCard卡, 返回根目录, 没有返回null.
     */
    public static String getSdCardPath() {
        String path = System.getenv("SECONDARY_STORAGE");
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        try {
            StatFs statFs = new StatFs(path);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                if (statFs.getBlockSizeLong() == 0 || statFs.getBlockCountLong() == 0) {
                    return null;
                }
            } else {
                if (statFs.getBlockSize() == 0 || statFs.getBlockCount() == 0) {
                    return null;
                }
            }
        } catch (IllegalArgumentException e) {
            return null;
        }
        return path;
    }

    /**
     * 获取存储目录
     *
     * @return 返回应用的沙箱files目录(内置存储卡或机身存储)
     */
    public static String getStoragePath() {
        // 扩展卡可用, 或者不可能被移除
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable()) {
            return Environment.getExternalStorageDirectory().getPath();
        } else {
            return Environment.getDataDirectory().getPath();
        }
    }

    /**
     * 获取应用文件存储目录
     *
     * @param context
     * @return 返回应用的沙箱files目录(内置存储卡或机身存储)
     */
    public static String getStorageFilePath(Context context) {
        // 扩展卡可用, 或者不可能被移除
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable()) {
            return context.getExternalFilesDir(null).getPath();
        } else {
            return context.getFilesDir().getPath();
        }
    }

    public static String getSaveDirector(Context context) {
        // SDCard沙盒目录
        String dir = StorageUtil.getSdCardPath();
        if (dir != null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                dir = dir + EXTERNAL_DIRECTORY;
                File file = new File(dir);
                if (file != null) {
                    if (file.exists() && canCreateFile(dir)) {
                        return dir;
                    } else if (file.mkdirs() && canCreateFile(dir)) {
                        return dir;
                    }
                }
            } else {
                dir = dir + String.format(SANDBOX_DIRECTORY, context.getPackageName());
                if (isExists(dir) && canCreateFile(dir)) {
                    return dir;
                }
            }
        }

        // 机身扩展存储沙盒目录
        if (context.getExternalFilesDir(null) != null) {
            dir = context.getExternalFilesDir(null).getPath();
            if (!TextUtils.isEmpty(dir)) {
                if (isExists(dir) && canCreateFile(dir)) {
                    return dir;
                }
            }
        }

        // 机身扩展存储根目录
        dir = Environment.getExternalStorageDirectory().getPath();
        if (!TextUtils.isEmpty(dir)) {
            dir = dir + EXTERNAL_DIRECTORY;
            File dirFile = new File(dir);
            if (dirFile != null) {
                // 文件夹存在
                if (dirFile.exists() && canCreateFile(dir)) {
                    return dir;
                }
                // 创建文件夹成功
                else if (dirFile.mkdir() && canCreateFile(dir)) {
                    return dir;
                }
            }
        }

        // 系统DATA目录
        dir = context.getFilesDir().getPath();
        if (dir != null) {
            if (isExists(dir) && canCreateFile(dir)) {
                return dir;
            }
        }
        return null;
    }

    private static boolean isExists(String path) {
        File file = new File(path);
        return file != null && file.exists();
    }

    private static boolean canCreateFile(String path) {
        boolean result = false;
        path = path + "/temp";
        File file = new File(path);
        try {
            result = file.createNewFile();
            if (result) {
                file.delete();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return result;
        }
    }

    public static boolean sufficiencyStorage(String director, long size) {
        long blockSize;
        long surplusCount;
        StatFs statFs = new StatFs(director);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSize = statFs.getBlockSizeLong();
            surplusCount = statFs.getAvailableBlocksLong();
        } else {
            blockSize = statFs.getBlockSize();
            surplusCount = statFs.getAvailableBlocks();
        }

//        Log.e("s_tag", "StorageUtil.sufficiencyStorage-> surplus=" + (blockSize * surplusCount) + " / size=" + size);
        if (blockSize * surplusCount > size) {
            return true;
        }
        return false;
    }

    public static double getSurplusStorageSpace(Context context, @UnitStorage int unitStorage) {
        String mStoragePath = getSaveDirector(context);
        StatFs statFs = new StatFs(mStoragePath);
        switch (unitStorage) {
            case UNIT_STORAGE_GB:
                if (statFs != null) {
                    return statFs.getAvailableBlocks() * (statFs.getBlockSize() / (1024f * 1024f * 1024));
                }
                break;
            default:
                if (statFs != null) {
                    return statFs.getAvailableBlocks() * (statFs.getBlockSize() / (1024f * 1024f));
                }
                break;
        }
        return 0.0f;
    }

    public static String formatFileSizeUnit(double arg) {
        DecimalFormat df = new DecimalFormat("#.##");
        double temp = arg / 1024;
        if (temp > 1024) {
            temp = temp / 1024;
            if (temp > 1024) {
                temp = temp / 1024;
                temp = Double.parseDouble(df.format(temp));
                return temp + " GB";
            } else {
                temp = Double.parseDouble(df.format(temp));
                return temp + " MB";
            }
        } else {
            temp = Double.parseDouble(df.format(temp));
            return temp + " KB";
        }
    }
}
