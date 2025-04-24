package br.com.brittosw.stockmanagement.infraestructure.notification.exception;

import lombok.Getter;
import java.util.List;

@Getter
public class NotificationException extends RuntimeException {
    private final List<Exception> causes;

    public NotificationException(String message) {
        super(message);
        this.causes = null;
    }

    public NotificationException(String message, Throwable cause) {
        super(message, cause);
        this.causes = null;
    }

    public NotificationException(String message, List<Exception> causes) {
        super(message);
        this.causes = causes;
    }

    @Override
    public String getMessage() {
        if (causes == null || causes.isEmpty()) {
            return super.getMessage();
        }

        StringBuilder message = new StringBuilder(super.getMessage())
            .append("\nCausas detalhadas:\n");

        causes.forEach(cause -> 
            message.append(" - ")
                   .append(cause.getMessage())
                   .append("\n")
        );

        return message.toString();
    }
}
