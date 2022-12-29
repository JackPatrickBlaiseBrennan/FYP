package org.example;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;


public class Main {
    public static void main(String[] args) {
        int period = 8;
        int numEmployees = 18;
        String[] shifts = {"O", "M", "A", "N"};

        RosterModel rosterModel = new RosterModel(period, numEmployees, 4, 4, 4, 4);
        rosterModel.postAllHardConstraints();
        rosterModel.postAllUserConstraints();
        Model model = rosterModel.getModel();
        Solver s = model.getSolver();
        s.limitTime("2s");

        while (s.solve()) { //print the solution
            IntVar numberOfGranted = rosterModel.getNumberOfGranted();
            IntVar [][] roster = rosterModel.getRoster();
            System.out.println("Solution " + s.getSolutionCount() + ":");
            for (int j = 0; j < numEmployees; j++) {
                System.out.print("Employee" + (j + 1) + ": ");
                for (int i = 0; i < period; i++) {
                    System.out.print(shifts[roster[i][j].getValue()] + " ");
                }
                System.out.print("\n");
            }
            System.out.print("Number of user requests granted: " + numberOfGranted.getValue() + "\n");
        }
        s.printStatistics();
    }
}