package org.example;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.BoolVar;
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
                    // cannot work on the same shift for more than two days
                    if (j % 2 == 0){
                        allDifferentExcept0(model, roster[j][i],roster[j+1][i]);
                    }
                }
            }
        }

        // user constraints
        BoolVar dayoff = model.arithm(roster[3][0], "=", 0).reify();
        BoolVar dayoff2 = model.arithm(roster[3][1], "=", 0).reify();
        BoolVar dayoff3 = model.arithm(roster[3][2], "=", 0).reify();

        Solver s = model.getSolver();
        s.showSolutions();
        s.solve();

        for (int j = 0; j < numEmployees; j++){
            System.out.print("Employee" + (j+1)+": ");
            for (int i =0; i < period; i++){
                System.out.print(shifts[roster[i][j].getValue()] + " ");
            }
            System.out.print("\n");
        }
        System.out.print(dayoff);
        System.out.print(dayoff2);
        System.out.print(dayoff3);
    }

    public static void allDifferentExcept0(Model model, IntVar... vars){
        model.allDifferentExcept0(vars).post();
    }
}