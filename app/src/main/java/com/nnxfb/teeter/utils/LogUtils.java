package com.nnxfb.teeter.utils;

import android.net.Uri;
import android.util.Log;
import java.io.File;

/* loaded from: classes4.dex */
public class LogUtils {
    private static final String FILE_PATH_LOG_CRITICAL = "/data/data/com.nnxfb.teeter/files/LogCritical";
    private static final String FILE_PATH_LOG_NORMAL = "/data/data/com.nnxfb.teeter/files/LogNormal";
    public static boolean sLogFlagCritical;
    public static boolean sLogFlagNormal;

    static {
        File file = new File(FILE_PATH_LOG_CRITICAL);
        if (file.exists()) {
            sLogFlagNormal = true;
            sLogFlagCritical = true;
        }
        else {
            File file2 = new File(FILE_PATH_LOG_NORMAL);
            if (file2.exists()) {
                sLogFlagNormal = true;
                sLogFlagCritical = false;
            } else {
                sLogFlagNormal = false;
                sLogFlagCritical = false;
            }
        }
    }

    public static void critical(String tag, String msg) {
        if (sLogFlagCritical) {
            Log.v(TTConstants.TEETER, getBracketTag(tag) + msg);
        }
    }

    public static void critical(String tag, String msg, Throwable tr) {
        if (sLogFlagCritical) {
            try {
                Log.v(TTConstants.TEETER, getBracketTag(tag) + msg, tr);
            } catch (Exception e) {
                Log.v(TTConstants.TEETER, getBracketTag(tag) + msg);
            }
        }
    }

    public static void d(String tag, String msg) {
        if (sLogFlagNormal) {
            Log.d(TTConstants.TEETER, getBracketTag(tag) + msg);
        }
    }

    public static void d(String tag, String prefix, String msg) {
        if (sLogFlagNormal) {
            Log.d(TTConstants.TEETER, getBracketTag(tag) + prefix + msg);
        }
    }

    public static void d(String tag, String prefix, int msg) {
        if (sLogFlagNormal) {
            Log.d(TTConstants.TEETER, getBracketTag(tag) + prefix + msg);
        }
    }

    public static void d(String tag, String prefix, long msg) {
        if (sLogFlagNormal) {
            Log.d(TTConstants.TEETER, getBracketTag(tag) + prefix + msg);
        }
    }

    public static void d(String tag, String prefix, boolean msg) {
        if (sLogFlagNormal) {
            Log.d(TTConstants.TEETER, getBracketTag(tag) + prefix + msg);
        }
    }

    public static void d(String tag, String prefix, Uri uri) {
        if (sLogFlagNormal) {
            Log.d(TTConstants.TEETER, getBracketTag(tag) + prefix + uri.toString());
        }
    }

    public static void d(String tag, String msg, Throwable tr) {
        if (sLogFlagNormal) {
            try {
                Log.d(TTConstants.TEETER, getBracketTag(tag) + msg, tr);
            } catch (Exception e) {
                Log.d(TTConstants.TEETER, getBracketTag(tag) + msg);
            }
        }
    }

    public static void i(String tag, String msg) {
        if (sLogFlagNormal) {
            Log.i(TTConstants.TEETER, getBracketTag(tag) + msg);
        }
    }

    public static void i(String tag, String msg, Throwable tr) {
        if (sLogFlagNormal) {
            try {
                Log.i(TTConstants.TEETER, getBracketTag(tag) + msg, tr);
            } catch (Exception e) {
                Log.i(TTConstants.TEETER, getBracketTag(tag) + msg);
            }
        }
    }

    public static void w(String tag, String msg) {
        if (sLogFlagNormal) {
            Log.w(TTConstants.TEETER, getBracketTag(tag) + msg);
        }
    }

    public static void w(String tag, String msg, Throwable tr) {
        if (sLogFlagNormal) {
            try {
                Log.w(TTConstants.TEETER, getBracketTag(tag) + msg, tr);
            } catch (Exception e) {
                Log.w(TTConstants.TEETER, getBracketTag(tag) + msg);
            }
        }
    }

    public static void e(String tag, String msg) {
        Log.e(TTConstants.TEETER, getBracketTag(tag) + msg);
    }

    public static void e(String tag, String msg, Throwable tr) {
        try {
            Log.e(TTConstants.TEETER, getBracketTag(tag) + msg, tr);
        } catch (Exception e) {
            Log.e(TTConstants.TEETER, getBracketTag(tag) + msg);
        }
    }

    public static void analytic(String tag, String msg) {
        Log.i(TTConstants.ANALYTIC_TAG, "[" + tag + "]" + msg);
    }

    public static void toQualityBoard(String tag, boolean isSuccess, String... msgs) {
        if (sLogFlagNormal && msgs.length > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(getBracketTag(tag));
            for (String str : msgs) {
                sb.append(str);
            }
            String type = isSuccess ? "[S]" : "[E]";
            Log.d(TTConstants.QUALITY_BOARD_TAG + type, sb.toString());
        }
    }

    private static String getBracketTag(String tag) {
        return "<" + tag + "> ";
    }
}
