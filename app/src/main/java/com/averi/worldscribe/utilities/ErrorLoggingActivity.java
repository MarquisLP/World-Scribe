package com.averi.worldscribe.utilities;

import java.io.File;

public interface ErrorLoggingActivity {
    void onErrorLoggingCompletion(String errorMessage, File errorLogFile);
}
