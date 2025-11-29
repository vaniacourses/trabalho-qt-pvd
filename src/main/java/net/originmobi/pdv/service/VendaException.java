package net.originmobi.pdv.service;

public class VendaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public VendaException(String message) {
        super(message);
    }

    public VendaException(String message, Throwable cause) {
        super(message, cause);
    }
}
