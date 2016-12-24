package com.averi.worldscribe.dropbox;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.averi.worldscribe.R;
import com.averi.worldscribe.utilities.FileRetriever;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.CreateFolderErrorException;
import com.dropbox.core.v2.files.WriteMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

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
        return androidFilePath.replace(appFilePath, "");
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);

        progressDialog.dismiss();

        if (uploadSuccessful) {
            Toast.makeText(context, context.getString(R.string.dropboxUploadSuccess),
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, context.getString(R.string.dropboxUploadFailure),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
