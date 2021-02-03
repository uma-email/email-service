package org.acme.emailservice.exception;

public class RecordNotFound extends Exception {

    private static final long serialVersionUID = 1L;

    public RecordNotFound() {
    }

    public RecordNotFound(String string) {
        super(string);
    }

    public RecordNotFound(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }

    public RecordNotFound(Throwable thrwbl) {
        super(thrwbl);
    }

    public RecordNotFound(String string, Throwable thrwbl, boolean bln, boolean bln1) {
        super(string, thrwbl, bln, bln1);
    }
    
}
