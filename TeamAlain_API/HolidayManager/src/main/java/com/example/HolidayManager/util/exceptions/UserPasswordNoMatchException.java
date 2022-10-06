package com.example.HolidayManager.util.exceptions;

public class UserPasswordNoMatchException extends Exception{
    public UserPasswordNoMatchException() {}

    // Constructor that accepts a message
    public UserPasswordNoMatchException(String message)
    {
        super(message);
    }
}
