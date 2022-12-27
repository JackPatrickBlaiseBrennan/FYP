package org.example;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;

import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.Arrays;


public class Main {
    public static void main(String[] args) {
        Model model = new Model("Scheduler");
        // Variable Declaration
        String[] shifts = {"O", "M", "A", "N"};
        final int Off = 0;
        final int Morning = 1;
        final int Afternoon = 2;
        final int Night = 3;
        int numShifts = shifts.length-1;
        int period = 8;
        int numEmployees = 18;

        IntVar OffDaysRange = model.intVar(1, 4);
        int numSubPeriods = (int) Math.ceil((double)period / 4);
        int subPeriodLength = (int) Math.ceil((double)period / numSubPeriods);

        IntVar numberOfGranted = model.intVar("numberOfGranted", 0, numEmployees*period);
        IntVar[][] roster = model.intVarMatrix("roster", period, numEmployees, 0, numShifts);

        // Constraints
        for(int i=0; i < numEmployees; i++){
            // every 4 days at least 1 holidays
            IntVar[] cols = ArrayUtils.getColumn(roster, i);
            for (int j = 0; j < numSubPeriods; j++) {
                IntVar[] subCols = Arrays.copyOfRange(cols, subPeriodLength * j,
                        (subPeriodLength * j) + subPeriodLength-1);
                model.count(Off, subCols, OffDaysRange).post();
            }
            for (int j = 0; j < period; j++){
                // Each shift has 4 allocations
                IntVar MorningNum = model.intVar(4);
                model.count(Morning, roster[j], MorningNum).post();
                IntVar AfterNum = model.intVar(4);
                model.count(Afternoon, roster[j], AfterNum).post();
                IntVar NightNum = model.intVar(4);
                model.count(Night, roster[j], NightNum).post();

                if (j < period - 1){
                    // no Morning after Late
                    roster[j][i].eq(3).imp(roster[j+1][i].ne(1)).post();
                    // cannot work on the same shift for more than three days
                    if (j < period - 3){
                        roster[j][i].eq(roster[j+1][i]).eq(roster[j+2][i]).imp(roster[j+3][i].ne(roster[j+1][i])).post();
                    }
                }
            }
        }

        IntVar[] dayOffGranted = model.intVarArray("dayOffGranted", 7, 0, 1);

//        for (int i = 0; i < 3; i++){
//            model.ifOnlyIf(model.arithm(dayOffGranted[i], ">", 0), model.arithm(roster[3][i], "=", 0));
//        }
        model.ifOnlyIf(model.arithm(dayOffGranted[0], ">", 0), model.arithm(roster[3][0], "=", 0));
        model.ifOnlyIf(model.arithm(dayOffGranted[1], ">", 0), model.arithm(roster[3][1], "=", 0));
        model.ifOnlyIf(model.arithm(dayOffGranted[2], ">", 0), model.arithm(roster[3][2], "=", 0));
        model.ifOnlyIf(model.arithm(dayOffGranted[3], ">", 0), model.arithm(roster[3][3], "=", 0));
        model.ifOnlyIf(model.arithm(dayOffGranted[4], ">", 0), model.arithm(roster[3][4], "=", 0));
        model.ifOnlyIf(model.arithm(dayOffGranted[5], ">", 0), model.arithm(roster[3][5], "=", 0));
        model.ifOnlyIf(model.arithm(dayOffGranted[6], ">", 0), model.arithm(roster[3][6], "=", 0));
        model.sum(dayOffGranted,"=",numberOfGranted).post();

        model.setObjective(Model.MAXIMIZE, numberOfGranted);

        Solver s = model.getSolver();
        s.limitTime("2s");
//        s.showSolutions();

        while (s.solve()) { //print the solution
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