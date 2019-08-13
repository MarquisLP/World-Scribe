package com.averi.worldscribe.utilities;

import android.os.AsyncTask;
import androidx.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogErrorTask extends AsyncTask {
    private static final String ERROR_LOG_FILE_NAME = "%s_applog.txt";
    private static final String EXCEPTION_LOG_MESSAGE = "Exception:\n%s\nStack Trace:\n";

    ErrorLoggingActivity activity;
    private String openingMessage;
    private Exception exception;

    public LogErrorTask(ErrorLoggingActivity activity, String openingMessage,
                        @Nullable  Exception exception) {
        this.activity = activity;
        this.openingMessage = openingMessage;
        this.exception = exception;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        File errorLogFile = generateErrorLogFile();

        try {
            PrintWriter errorLogPrintStream = new PrintWriter(errorLogFile);

            errorLogPrintStream.print(openingMessage + "\n");

            if (exception != null) {
                errorLogPrintStream.print("\n" + String.format(EXCEPTION_LOG_MESSAGE,
                        exception.getMessage()));
                exception.printStackTrace(errorLogPrintStream);
            }

            errorLogPrintStream.close();
            activity.onErrorLoggingCompletion(openingMessage, errorLogFile);
        } catch (IOException exception) {
            //TODO: Handle this exception more elegantly
            Log.e("WorldScribe", exception.getMessage());
        }

        return errorLogFile;
    }

    /**
     * Generates an empty error log file for Dropbox error logging purposes.
     * <p>
     *     This new file will overwrite any existing error log files that were created on the
     *     same day.
     * </p>
     * @return An empty error log file whose file name is based on the current date
     */
    private File generateErrorLogFile() {
        Date datum = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        String fullName = String.format(ERROR_LOG_FILE_NAME, df.format(datum));

        File errorLogFile = new File(FileRetriever.getAppDirectory(), fullName);
        if (errorLogFile.exists()) {
            errorLogFile.delete();
        }
        try {
            errorLogFile.getParentFile().mkdirs();
            errorLogFile.createNewFile();
        } catch (IOException e) {
            //TODO: Handle this exception more elegantly
            Log.e("WorldScribe", e.getMessage());
        }

        return errorLogFile;
    }
}
