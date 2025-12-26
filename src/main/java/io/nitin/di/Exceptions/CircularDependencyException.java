package io.nitin.di.Exceptions;

public class CircularDependencyException extends RuntimeException {
    public CircularDependencyException(String msg) {
        super(msg);
    }
}