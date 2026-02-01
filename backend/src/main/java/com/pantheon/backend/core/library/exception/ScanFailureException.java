package com.pantheon.backend.core.library.exception;

import java.io.IOException;

public class ScanFailureException extends IOException {

    public ScanFailureException() {
        super();
    }

    public ScanFailureException(String message) {
        super(message);
    }

    public ScanFailureException(String message, Throwable cause) {
        super(message, cause);
    }

    public ScanFailureException(Throwable cause) {
        super(cause);
    }

}
