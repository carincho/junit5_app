package org.carincho.junit5.app.ejemplos.exceptions;

public class SaldoInsuficiente  extends  RuntimeException {

    public SaldoInsuficiente(String message) {
        super(message);
    }
}
