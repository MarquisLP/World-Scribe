package com.averi.worldscribe.dropbox;

public interface DropboxActivity {
    enum CloudType {
      Dropbox,
      Nextcloud
    }

    void onDropboxNeedsAuthentication();
    void onDropboxUploadStart();
    void onDropboxUploadSuccess();
    void onDropboxUploadFailure(Exception e, String lastFileBeingUploaded);
}
