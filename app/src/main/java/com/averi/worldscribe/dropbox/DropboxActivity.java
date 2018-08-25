package com.averi.worldscribe.dropbox;

public interface DropboxActivity {
    void onDropboxNeedsAuthentication();
    void onDropboxUploadStart();
    void onDropboxUploadSuccess();
    void onDropboxUploadFailure(Exception e, String lastFileBeingUploaded);
}
