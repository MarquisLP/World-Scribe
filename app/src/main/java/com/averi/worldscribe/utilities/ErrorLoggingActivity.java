package com.averi.worldscribe.utilities;

import androidx.documentfile.provider.DocumentFile;

public interface ErrorLoggingActivity {
    void onErrorLoggingCompletion(String errorMessage, DocumentFile errorLogFile);
}
