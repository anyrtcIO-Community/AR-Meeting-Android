package org.ar.common.utils;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by elena on 2017/9/4.
 */

public class LogcatHelper {

    private static LogcatHelper INSTANCE = null;
    private static String PATH_LOGCAT;
    private LogDumper mLogDumper = null;
    private int mPId;

    public enum LogLevel {
        TRACE_NONE(0x0000),
        TRACE_DEBUG(0x0001),
        TRACE_INFO(0x0002),
        TRACE_WARNING(0x0003),
        TRACE_ERROR(0x0004);

        public final int level;
        LogLevel(int level) {
            this.level = level;
        }
    }

    /**
     * 初始化目录
     */
    public void init(Context context) {
        // 优先保存到SD卡中
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            PATH_LOGCAT = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + File.separator + "rtmax_log";
        } else {// 如果SD卡不存在，就保存到本应用的目录下
            PATH_LOGCAT = context.getFilesDir().getAbsolutePath()
                    + File.separator + "rtmax_log";
        }
        File file = new File(PATH_LOGCAT);
        if (!file.exists()) {
            file.mkdirs();
        }

    }

    public static LogcatHelper getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new LogcatHelper(context);
        }
        return INSTANCE;
    }

    private LogcatHelper(Context context) {
        init(context);
        mPId = android.os.Process.myPid();
    }

    public void start(LogLevel logLevel, String tag) {
        if (mLogDumper == null) {
            mLogDumper = new LogDumper(String.valueOf(mPId), PATH_LOGCAT, logLevel, tag);
        }
        mLogDumper.start();
    }

    public void stop() {
        if (mLogDumper != null) {
            mLogDumper.stopLogs();
            mLogDumper = null;
        }
    }

    private class LogDumper extends Thread {

        private Process logcatProc;
        private BufferedReader mReader = null;
        private boolean mRunning = true;
        String cmds = null;
        private String mPID;
        private FileOutputStream out = null;

        public LogDumper(String pid, String dir, LogLevel logLevel, String tag) {
            mPID = pid;
            try {
                out = new FileOutputStream(new File(dir, "log-"
                        + getFileName() + ".log"));
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            /**
             *
             * 日志等级：*:v , *:d , *:i, *:w , *:e , *:f , *:s
             *
             * 显示当前mPID程序的 E和W等级的日志.
             *
             * */

            // cmds = "logcat *:e *:w | grep \"(" + mPID + ")\"";
            // cmds = "logcat  | grep \"(" + mPID + ")\"";//打印所有日志信息
            // cmds = "logcat -s way";//打印标签过滤信息
            //cmds = "logcat *:e *:i | grep \"(" + mPID + ")\"";
            //cmds = "logcat *:e | grep \"(" + mPID + ")\"";
            if(null == logLevel) {
                cmds = "logcat  | grep \"(" + mPID + ")\"";//打印所有日志信息
            } else if(logLevel.level == LogLevel.TRACE_NONE.level) {
                cmds = "logcat  | grep \"(" + mPID + ")\"";//打印所有日志信息
            } else if(logLevel.level == LogLevel.TRACE_DEBUG.level) {
                cmds = "logcat *:d  | grep \"(" + mPID + ")\"";
            } else if(logLevel.level == LogLevel.TRACE_INFO.level) {
                cmds = "logcat *:i  | grep \"(" + mPID + ")\"";
            } else if(logLevel.level == LogLevel.TRACE_WARNING.level) {
                cmds = "logcat *:w  | grep \"(" + mPID + ")\"";
            } else if(logLevel.level == LogLevel.TRACE_ERROR.level) {
                cmds = "logcat *:e  | grep \"(" + mPID + ")\"";
            } else {
                //打印所有日志信息
                cmds = "logcat  | grep \"(" + mPID + ")\"";
            }

            if(null != tag) {
                //打印标签过滤信息
                cmds = "logcat -s " + tag;
            }

        }

        public void stopLogs() {
            mRunning = false;
        }

        @Override
        public void run() {
            try {
                logcatProc = Runtime.getRuntime().exec(cmds);
                mReader = new BufferedReader(new InputStreamReader(
                        logcatProc.getInputStream()), 102400);
                String line = null;
                while (mRunning && (line = mReader.readLine()) != null) {
                    if (!mRunning) {
                        break;
                    }
                    if (line.length() == 0) {
                        continue;
                    }
                    if (out != null && line.contains(mPID)) {
                        out.write((line + "\n").getBytes());
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (logcatProc != null) {
                    logcatProc.destroy();
                    logcatProc = null;
                }
                if (mReader != null) {
                    try {
                        mReader.close();
                        mReader = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    out = null;
                }

            }

        }

    }

    public String getFileName() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String date = format.format(new Date(System.currentTimeMillis()));
        // 2012年10月03日 23:41:31
        return date;
    }

//        public  String getDateEN() {
//            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            String date1 = format1.format(new Date(System.currentTimeMillis()));
//            return date1;// 2012-10-03 23:41:31
//        }
}