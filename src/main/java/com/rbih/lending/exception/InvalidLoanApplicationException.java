package com.rbih.lending.exception;

public class InvalidLoanApplicationException extends RuntimeException {
    public InvalidLoanApplicationException(String message) {
        super(message);
    }
}
