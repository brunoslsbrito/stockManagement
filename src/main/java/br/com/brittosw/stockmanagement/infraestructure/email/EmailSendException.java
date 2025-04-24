package br.com.brittosw.stockmanagement.infraestructure.email;

public class EmailSendException extends RuntimeException {
    public EmailSendException(String message, Throwable cause) {
        super(message, cause);
    }
}
