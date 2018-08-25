package com.averi.worldscribe.dropbox;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.averi.worldscribe.GenericFileProvider;
import com.averi.worldscribe.R;
import com.averi.worldscribe.utilities.FileRetriever;
import com.averi.worldscribe.utilities.LogErrorTask;
import com.dropbox.core.DbxException;
import com.dropbox.core.InvalidAccessTokenException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.CreateFolderErrorException;
import com.dropbox.core.v2.files.WriteMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
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

public class UploadToDropboxTask extends AsyncTask<Object, Void, Boolean> {
    private DbxClientV2 dbxClient;
    private File file;
    private DropboxActivity activity;
    private File currentFileBeingUploaded;

    public UploadToDropboxTask(DbxClientV2 dbxClient, File file, DropboxActivity activity) {
        this.dbxClient = dbxClient;
        this.file = file;
        this.activity = activity;
    }

    @Override
    protected Boolean doInBackground(Object[] params) {
        boolean uploadSuccessful = false;

        try {
            activity.onDropboxUploadStart();
            uploadRecursive(file);
            uploadSuccessful = true;
        } catch (InvalidAccessTokenException invalidAccessTokenException) {
            activity.onDropboxNeedsAuthentication();
        } catch (Exception exception) {
            Log.e("WorldScribe", exception.getMessage());
            activity.onDropboxUploadFailure(exception, currentFileBeingUploaded.getAbsolutePath());
        }

        return uploadSuccessful;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            activity.onDropboxUploadSuccess();
        }
    }

    /**
     * Upload the given file to Dropbox; if it is a directory, its contents will be recursively
     * uploaded.
     * @param fileBeingUploaded The file to upload to the client's Dropbox account
     * @throws DbxException If an error occurs with accessing the client's Dropbox account
     * @throws IOException If an error occurs with file uploading
     */
    private void uploadRecursive(File fileBeingUploaded) throws DbxException, IOException  {
        if (fileBeingUploaded.exists()) {
            this.currentFileBeingUploaded = fileBeingUploaded;
            String dropboxPath = getDropboxPath(fileBeingUploaded);
            if (dropboxPath == null) {
                throw new IOException("The Dropbox path ended up being 'null' for the following " +
                        "file: '" + fileBeingUploaded.getAbsolutePath() + "'");
            }

            if (fileBeingUploaded.isDirectory()) {
                try {
                    dbxClient.files().createFolder(dropboxPath);
                } catch (CreateFolderErrorException ex) {
                    // Checks if the exception was thrown because the folder already exists.
                    // That case isn't an error (as it just means we can skip over creating
                    // that folder), so we only want to throw the exception for other cases.
                    if (!(ex.errorValue.isPath() && ex.errorValue.getPathValue().isConflict())) {
                        throw ex;
                    }
                }

                File[] files = fileBeingUploaded.listFiles();
                for (File childFile : files) {
                    uploadRecursive(childFile);
                }

            } else {
                InputStream inputStream = new FileInputStream(fileBeingUploaded);
                dbxClient.files().uploadBuilder(dropboxPath)
                    .withMode(WriteMode.OVERWRITE)
                    .uploadAndFinish(inputStream);
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
}
