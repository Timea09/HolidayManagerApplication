package com.example.HolidayManager.util.exceptions;

public class UserNotFoundException extends Exception{
    public UserNotFoundException() {}

    // Constructor that accepts a message
    public UserNotFoundException(String message)
    {
        super(message);
    }
}
