package com.averi.worldscribe.utilities;

import android.content.Context;
import android.os.AsyncTask;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;

import android.util.Log;

import com.balda.flipper.DocumentFileCompat;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogErrorTask extends AsyncTask {
    private static final String ERROR_LOG_FILE_NAME = "%s_applog.txt";
    private static final String EXCEPTION_LOG_MESSAGE = "Exception:\n%s\nStack Trace:\n";

    ErrorLoggingActivity activity;
    private String openingMessage;
    private Context context;
    private Exception exception;

    public LogErrorTask(ErrorLoggingActivity activity, String openingMessage, Context context,
                        @Nullable  Exception exception) {
        this.activity = activity;
        this.openingMessage = openingMessage;
        this.context = context;
        this.exception = exception;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        DocumentFile errorLogFile = generateErrorLogFile();

        try {
            OutputStream outputStream = context.getContentResolver().openOutputStream(errorLogFile.getUri());
            PrintWriter errorLogPrintStream = new PrintWriter(outputStream);

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
    private DocumentFile generateErrorLogFile() {
        Date datum = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        String fullName = String.format(ERROR_LOG_FILE_NAME, df.format(datum));

        DocumentFile appDirectory = FileRetriever.getAppDirectory(context, false);
        DocumentFile errorLogFile = DocumentFileCompat.peekFile(appDirectory, fullName, null);
        if (errorLogFile != null) {
            errorLogFile.delete();
        }
        try {
            errorLogFile = appDirectory.createFile("text/plain", fullName);
        } catch (Exception e) {
            //TODO: Handle this exception more elegantly
            Log.e("WorldScribe", e.getMessage());
        }

        return errorLogFile;
    }
}
