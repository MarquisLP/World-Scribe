package clouds;

public interface CloudActivity {
    enum CloudType {
      Dropbox,
      Nextcloud
    }

    void onDropboxNeedsAuthentication();
    void onCloudUploadStart();
    void onCloudUploadSuccess();
    void onCloudUploadFailure(Exception e, String lastFileBeingUploaded);
}
