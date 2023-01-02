package org.example;

import org.chocosolver.solver.Solution;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;

public class PrintRoster {
    private final String[] shifts = {"O", "M", "A", "N"};
    private final IntVar[][] roster;
    private final int numberOfGranted;
    private final IntVar[] daysOffGranted;
    private final ArrayList<Request> requestList;
    private final Solution solution;

    public PrintRoster(Solution solution, IntVar[][] roster, int numberOfGranted, IntVar[] daysOffGranted, ArrayList<Request> requestList) {
        this.solution = solution;
        this.roster = roster;
        this.numberOfGranted = numberOfGranted;
        this.daysOffGranted = daysOffGranted;
        this.requestList = requestList;
        printTheRoster();
    }

    private void printTheRoster(){
        int numEmployees = roster[0].length;
        for (int j = 0; j < numEmployees; j++) {
            System.out.print("Employee" + (j + 1) + ": ");
            for (IntVar[] intVars : roster) {
                System.out.print(shifts[solution.getIntVal(intVars[j])] + " ");
            }
            System.out.print("\n");
        }
        System.out.print("Number of user requests granted: " + numberOfGranted + "\n");
        System.out.println("Not Granted:");
        for (Request request: requestList) {
            int index = requestList.indexOf(request);
            if (solution.getIntVal(daysOffGranted[index]) < 1){
                System.out.println("Index: " + index + " Employee: " + request.getEmployeeNumber() +
                        " dayOffRangeStart: " + request.getDayOffRangeStart() +
                        " dayOffRangeEnd: " + request.getDayOffRangeEnd() + " minNum: " + request.getMinNumOff());
            }
        }
    }
}
