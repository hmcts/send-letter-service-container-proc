package uk.gov.hmcts.reform.sendletter.exceptions;

public class UnableToZipException extends RuntimeException {

    private static final long serialVersionUID = -3484283017479516646L;

    public UnableToZipException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
