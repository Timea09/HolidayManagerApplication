package com.example.HolidayManager.util.exceptions;

public class TeamNameAlreadyExistsException extends Exception{

    public TeamNameAlreadyExistsException() {}

    // Constructor that accepts a message
    public TeamNameAlreadyExistsException(String message)
    {
        super(message);
    }

}
