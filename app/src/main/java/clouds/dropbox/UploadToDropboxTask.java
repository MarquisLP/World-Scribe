package clouds.dropbox;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import com.averi.worldscribe.utilities.FileRetriever;
import com.dropbox.core.DbxException;
import com.dropbox.core.InvalidAccessTokenException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.CreateFolderErrorException;
import com.dropbox.core.v2.files.WriteMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import clouds.CloudActivity;

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
    private DocumentFile file;
    private CloudActivity activity;
    private DocumentFile currentFileBeingUploaded;
    private Context context;

    public UploadToDropboxTask(DbxClientV2 dbxClient, DocumentFile file, CloudActivity activity, Context context) {
        this.dbxClient = dbxClient;
        this.file = file;
        this.activity = activity;
        this.context = context;
    }

    @Override
    protected Boolean doInBackground(Object[] params) {
        boolean uploadSuccessful = false;

        try {
            activity.onCloudUploadStart();
            uploadRecursive(file);
            uploadSuccessful = true;
        } catch (InvalidAccessTokenException invalidAccessTokenException) {
            activity.onDropboxNeedsAuthentication();
        } catch (Exception exception) {
            Log.e("WorldScribe", exception.getMessage());
            activity.onCloudUploadFailure(exception, currentFileBeingUploaded.getUri().getPath());
        }

        return uploadSuccessful;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            activity.onCloudUploadSuccess();
        }
    }

    /**
     * Upload the given file to Dropbox; if it is a directory, its contents will be recursively
     * uploaded.
     * @param fileBeingUploaded The file to upload to the client's Dropbox account
     * @throws DbxException If an error occurs with accessing the client's Dropbox account
     * @throws IOException If an error occurs with file uploading
     */
    private void uploadRecursive(DocumentFile fileBeingUploaded) throws DbxException, IOException  {
        if (fileBeingUploaded.exists()) {
            this.currentFileBeingUploaded = fileBeingUploaded;
            String dropboxPath = getDropboxPath(fileBeingUploaded);
            if (dropboxPath == null) {
                throw new IOException("The Dropbox path ended up being 'null' for the following " +
                        "file: '" + fileBeingUploaded.getUri().getPath() + "'");
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

                DocumentFile[] files = fileBeingUploaded.listFiles();
                for (DocumentFile childFile : files) {
                    uploadRecursive(childFile);
                }

            } else {
                InputStream inputStream = context.getContentResolver().openInputStream(fileBeingUploaded.getUri());
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
    private String getDropboxPath(DocumentFile file) {
        String androidFilePath = file.getUri().getPath();

        String appFilePath = FileRetriever.getAppDirectory(context, false).getUri().getPath();
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
