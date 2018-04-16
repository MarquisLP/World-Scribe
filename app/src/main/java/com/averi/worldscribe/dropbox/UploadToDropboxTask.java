package com.averi.worldscribe.dropbox;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.averi.worldscribe.R;
import com.averi.worldscribe.utilities.FileRetriever;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.CreateFolderErrorException;
import com.dropbox.core.v2.files.WriteMode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by mark on 24/12/16.
 *
 * <p>
 *     Code copied and modified from SitePoint:<br>
 *     https://www.sitepoint.com/adding-the-dropbox-api-to-an-android-app/
 * </p>
 *
 * <p>
 *     This AsyncTask handles file uploading to a Dropbox account. A Toast with an appropriate
 *     message is displayed at the end of file operations, depending on whether the upload was
 *     successful or not.
 * </p>
 */

public class UploadToDropboxTask extends AsyncTask {
    private DbxClientV2 dbxClient;
    private File file;
    private Context context;
    private boolean uploadSuccessful = true;
    private ProgressDialog progressDialog;

    public UploadToDropboxTask(DbxClientV2 dbxClient, File file, Context context) {
        this.dbxClient = dbxClient;
        this.file = file;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        showProgressDialog();
    }

    /**
     * Displays a loading dialog that will stay on-screen while uploading occurs.
     */
    private void showProgressDialog() {
        String title = context.getString(R.string.dropboxUploadProgressTitle);
        String message = context.getString(R.string.dropboxUploadProgressMessage);
        progressDialog = ProgressDialog.show(context, title, message);
    }

    @Override
    protected Object doInBackground(Object[] params) {
        try {
            uploadRecursive(file);
        } catch (DbxException | IOException e) {
            Log.e("WorldScribe", e.getMessage());
            uploadSuccessful = false;
        }
        return null;
    }

    /**
     * This function was written by user6038288 on
     * <a href="https://stackoverflow.com/a/48007001">StackOverflow</a>.
     * @param context The Context from which this function is being called
     */
    private static void sendLog(Context context) {
        //set a file
        Date datum = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        String fullName = df.format(datum) + "appLog.txt";
        File file = new File(FileRetriever.getAppDirectory(), fullName);

        //clears a previous log
        if (file.exists()) {
            file.delete();
        }
        //write log to file
        int pid = android.os.Process.myPid();
        try {
            String command = String.format("logcat -d -v threadtime *:*");
            Process process = Runtime.getRuntime().exec(command);

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder result = new StringBuilder();
            String currentLine = null;
            while ((currentLine = reader.readLine()) != null) {
                if (currentLine != null && currentLine.contains(String.valueOf(pid))) {
                    result.append(currentLine);
                    result.append("\n");
                }
            }
            FileWriter out = new FileWriter(file);
            out.write(result.toString());
            out.close();
            sendEmail(context, file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //clear the log
        try {
            Runtime.getRuntime().exec("logcat -c");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * This function was written by user6038288 on
     * <a href="https://stackoverflow.com/a/48007001">StackOverflow</a>.
     * @param context The Context from which this function is being called
     * @param file The file that will be attached to the email
     */
    private static void sendEmail(Context context, File file) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"averistudios@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Log Report");
        intent.putExtra(Intent.EXTRA_TEXT, "Add description:");
        if (!file.exists() || !file.canRead()) {
            Toast.makeText(context, "Attachment Error", Toast.LENGTH_SHORT).show();
            return;
        }
        Uri uri = Uri.parse("file://" + file);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        context.startActivity(Intent.createChooser(intent, "Send email..."));
    }

    /**
     * Upload the given file to Dropbox; if it is a directory, its contents will be recursively
     * uploaded.
     * @param originalFile The file to upload to the client's Dropbox account
     * @throws DbxException If an error occurs with accessing the client's Dropbox account
     * @throws IOException If an error occurs with file uploading
     */
    private void uploadRecursive(File originalFile) throws DbxException, IOException  {
        if (file.exists()) {
            File[] files = originalFile.listFiles();

            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String dropboxPath = getDropboxPath(file);
                if (file.isDirectory()) {
                    try {
                        dbxClient.files().createFolder(dropboxPath);
                    } catch (CreateFolderErrorException ex) {
                    }
                    uploadRecursive(file);
                } else {
                    InputStream inputStream = new FileInputStream(file);
                    dbxClient.files().uploadBuilder(dropboxPath)
                            .withMode(WriteMode.OVERWRITE)
                            .uploadAndFinish(inputStream);
                }
            }
        }
    }

    /**
     * Returns the path of the given Android file on the client's Dropbox account.
     * @param file The file whose Dropbox path will be retrieved
     * @return The Dropbox file path of file
     */
    private String getDropboxPath(File file) {
        String androidFilePath = file.getAbsolutePath();

        String appFilePath = FileRetriever.getAppDirectory().getAbsolutePath();
        String dropboxPath = androidFilePath.replace(appFilePath, "");

        // Dropbox will not upload files that have a "." prefix.
        // To get around this, we upload those files without the "." prefix.
        String fileName = file.getName();
        if (fileName.startsWith(".")) {
            String fileNameWithoutDotPrefix = fileName.substring(1);
            dropboxPath = dropboxPath.replace(fileName, fileNameWithoutDotPrefix);
        }

        return dropboxPath;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);

        progressDialog.dismiss();
        showOutcomeDialog();
    }

    /**
     * Displays an AlertDialog telling the user whether or not the upload was successful.
     */
    private void showOutcomeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String message;

        if (uploadSuccessful) {
            message = context.getString(R.string.dropboxUploadSuccess);
        } else {
            message = context.getString(R.string.dropboxUploadFailure);
        }

        builder.setMessage(message);
        builder.setPositiveButton(context.getString(R.string.dismissDropboxUploadOutcome), null);
        builder.create().show();
    }
}
