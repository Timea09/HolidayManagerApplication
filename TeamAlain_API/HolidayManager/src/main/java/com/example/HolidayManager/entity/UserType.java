package com.example.HolidayManager.entity;

public enum UserType {

    ADMIN {
        public String toString() {
            return "Admin";
        }
    },
    TEAM_LEAD {
        public String toString() {
            return "Team Lead";
        }
    },
    EMPLOYEE{
        public String toString() {
            return "Employee";
        }
    }
}
