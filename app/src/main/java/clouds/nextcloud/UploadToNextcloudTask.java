package clouds.nextcloud;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.averi.worldscribe.activities.ThemedActivity;
import clouds.CloudActivity;
import com.averi.worldscribe.utilities.FileRetriever;
import com.owncloud.android.lib.common.*;
import com.owncloud.android.lib.common.operations.OnRemoteOperationListener;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.CreateFolderRemoteOperation;
import com.owncloud.android.lib.resources.files.UploadFileRemoteOperation;

import java.io.File;
import java.io.IOException;

public class UploadToNextcloudTask extends AsyncTask<Object, Void, Boolean> implements OnRemoteOperationListener {
    private OwnCloudClient client;
    private Handler handler = new Handler();
    private File currentFile = null;
    private Object notifier = new Object();
    private RemoteOperationResult.ResultCode resultCode;
    private CloudActivity activitiy;


    public UploadToNextcloudTask(OwnCloudClient client, CloudActivity activity, File file) {
        //This exception only occurs for the developer not for the user, because the user can't changed the code at runtime.
        if(!(activity instanceof ThemedActivity))
            throw new IllegalArgumentException("The activity object must derived from ThemedActivity.");

        this.activitiy = activity;
        this.client = client;
        this.currentFile = file;
    }

    /**
     * Waits for the upload process to finish and checks the errors.
     * @throws InterruptedException
     */
    private void waitForFinishUpload() throws InterruptedException {
        synchronized (UploadToNextcloudTask.this.notifier)
        {
            UploadToNextcloudTask.this.notifier.wait();

            if(resultCode != RemoteOperationResult.ResultCode.OK &&
               resultCode != RemoteOperationResult.ResultCode.OK_SSL &&
               resultCode != RemoteOperationResult.ResultCode.OK_NO_SSL &&
               resultCode != RemoteOperationResult.ResultCode.FOLDER_ALREADY_EXISTS)
            {
                throw new RuntimeException("Error during upload. Resultcode: " + resultCode.name());
            }
        }
    }

    /**
     * Upload the given file to Nextcloud; if it is a directory, its contents will be recursively
     * uploaded.
     * @param uploadObject The object to upload to the client's Nextcloud account
     * @throws IOException If an error occurs with file uploading
     */
    private void uploadRecursive(File uploadObject) throws InterruptedException, IOException {
        if (uploadObject.exists()) {
            this.currentFile = uploadObject;
            String nextcloudPath = getNextcloudPath(uploadObject);
            if (nextcloudPath == null) {
                throw new IOException("The Nextcloud path ended up being 'null' for the following " +
                        "file: '" + uploadObject.getAbsolutePath() + "'");
            }

            if (uploadObject.isDirectory()) {
                CreateFolderRemoteOperation dir = new CreateFolderRemoteOperation(nextcloudPath, false);
                dir.execute(client, this, this.handler);

                waitForFinishUpload();

                File[] files = uploadObject.listFiles();
                for (File childFile : files) {
                    uploadRecursive(childFile);
                }

            } else {
                Long timeStampLong = uploadObject.lastModified() / 1000;
                String timeStamp = timeStampLong.toString();

                UploadFileRemoteOperation file = new UploadFileRemoteOperation(uploadObject.getAbsolutePath(), nextcloudPath, "application/octet-stream", timeStamp);
                file.execute(this.client, this, this.handler);
                waitForFinishUpload();
            }
        }
    }

    @Override
    public void onRemoteOperationFinish(RemoteOperation caller, RemoteOperationResult result) {
        //Tells the uploadRecursive that the upload or folder creation is finished and then checks the errors.
        synchronized (UploadToNextcloudTask.this.notifier)
        {
            UploadToNextcloudTask.this.resultCode = result.getCode();
            UploadToNextcloudTask.this.notifier.notify();
        }
    }

    @Override
    protected Boolean doInBackground(Object... objects) {
        boolean uploadSuccessful = false;

        try {
            activitiy.onCloudUploadStart();
            uploadRecursive(currentFile);
            uploadSuccessful = true;
        } catch (Exception exception) {
            Log.e("WorldScribe", exception.getMessage());
            activitiy.onCloudUploadFailure(exception, currentFile.getAbsolutePath());
        }

        return uploadSuccessful;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            activitiy.onCloudUploadSuccess();
        }
    }

    /**
     * Returns the path of the given Android file on the client's Nextcloud account.
     * @param file The file whose Nextcloud path will be retrieved
     * @return The Nextcloud file path of file
     */
    private String getNextcloudPath(File file) {
        String androidFilePath = file.getAbsolutePath();

        String appFilePath = FileRetriever.getAppDirectory().getAbsolutePath();
        String nextcloudPath = androidFilePath.replace(appFilePath, "");

        // Nextcloud will not upload files that have a "." prefix.
        // To get around this, we upload those files without the "." prefix.
        String fileName = file.getName();
        if (fileName.startsWith(".")) {
            String fileNameWithoutDotPrefix = fileName.substring(1);
            nextcloudPath = nextcloudPath.replace(fileName, fileNameWithoutDotPrefix);
        }

        return nextcloudPath;
    }
}
