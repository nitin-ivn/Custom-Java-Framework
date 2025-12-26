package io.nitin.di.Exceptions;

public class BeanNotFoundException extends RuntimeException{
    public BeanNotFoundException(String msg){
        super(msg);
    }
}
