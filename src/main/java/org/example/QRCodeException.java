package org.example;

public class QRCodeException extends Exception{
    public QRCodeException(String message, Throwable cause) {
        super("Error al generar el qr :\n" + message, cause);
    }

    public QRCodeException(String message) {
        super("Error al generar el qr :\n" + message);
    }
}
