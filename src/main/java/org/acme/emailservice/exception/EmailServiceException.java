package org.acme.emailservice.exception;

public class EmailServiceException extends Exception
{
    private static final long serialVersionUID = 1L;

    public EmailServiceException()
    {
        super();
    }

    public EmailServiceException(String message)
    {
        super(message);
    }

    public EmailServiceException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public EmailServiceException(Throwable cause)
    {
        super(cause);
    }
}
