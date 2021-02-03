package org.acme.emailservice.exception;

public class UpdateNotAllowedException extends Exception {

    private static final long serialVersionUID = 1L;

    public UpdateNotAllowedException() {
    }

    public UpdateNotAllowedException(String string) {
        super(string);
    }

    public UpdateNotAllowedException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }

    public UpdateNotAllowedException(Throwable thrwbl) {
        super(thrwbl);
    }

    public UpdateNotAllowedException(String string, Throwable thrwbl, boolean bln, boolean bln1) {
        super(string, thrwbl, bln, bln1);
    }
    
}
