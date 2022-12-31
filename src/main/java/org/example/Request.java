package org.example;

public class Request {
    private final int employeeNumber; private final int dayOffRangeStart; private final int dayOffRangeEnd;
    private final int minNumOff; private final int maxNumOff;
    public Request(int employeeNumber, int dayOffRangeStart, int dayOffRangeEnd, int minNumOff) {
        this.employeeNumber = employeeNumber;
        this.dayOffRangeStart = dayOffRangeStart;
        this.dayOffRangeEnd = dayOffRangeEnd;
        this.minNumOff = minNumOff;
        this.maxNumOff = dayOffRangeEnd - dayOffRangeStart + 1;
    }

    public int getEmployeeNumber() {
        return employeeNumber;
    }

    public int getDayOffRangeStart() {
        return dayOffRangeStart;
    }

    public int getDayOffRangeEnd() {
        return dayOffRangeEnd;
    }
    public int getMaxNumOff() {
        return maxNumOff;
    }
    public int getMinNumOff() {
        return minNumOff;
    }
}
