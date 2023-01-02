package org.example;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;


public class Main {
    public static void main(String[] args) {
        int period = 8;
        int numEmployees = 18;

        RosterModel rosterModel = new RosterModel(period, numEmployees, 4, 4, 4, 4);
        rosterModel.postAllHardConstraints();
        rosterModel.addRequest(0,3,3,1);
        rosterModel.addRequest(1,3,3,1);
        rosterModel.addRequest(2,3,3,1);
        rosterModel.addRequest(3,3,3,1);
        rosterModel.addRequest(4,3,3,1);
        rosterModel.addRequest(5,3,3,1);
        rosterModel.addRequest(6,3,3,1);
        rosterModel.postAllUserConstraints();
        Model model = rosterModel.getModel();
        ArrayList<Request> requestList = rosterModel.getRequestList();
        Solver s = model.getSolver();
        s.limitTime("2s");
        Solution solution = new Solution(model);
        while (s.solve()) {
            solution.record();
        }
        int numberOfGranted = solution.getIntVal(rosterModel.getNumberOfGranted());
        IntVar [][] roster = rosterModel.getRoster();
        IntVar[] daysOffGranted = rosterModel.getDayOffGranted();
        new PrintRoster(solution, roster, numberOfGranted, daysOffGranted, requestList);
        s.printStatistics();
    }
}