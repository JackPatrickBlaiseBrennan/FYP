package org.example;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class RosterModel {
    private final Model model; private IntVar[][] roster; private final IntVar numOfMorningShifts; private final IntVar numOfAfternoonShifts;
    private final IntVar numOfNightShifts; private IntVar OffDaysRange; private IntVar numberOfGranted;
    private int numSubPeriods; private int subPeriodLength; private final int period;
    private final int numEmployees;
    public enum Shift{
        OFF, MORNING, AFTERNOON, NIGHT
    }
    private final ArrayList<Request> requests = new ArrayList<>();
    public RosterModel(int period, int numEmployees, int numOfMorningShifts, int numOfAfternoonShifts, int numOfNightShifts, int periodDivisor) {
        this(period, numEmployees, numOfMorningShifts, numOfAfternoonShifts, numOfNightShifts);
        setPeriodDivisor(periodDivisor);
    }
    public RosterModel(int period, int numEmployees, int numOfMorningShifts, int numOfAfternoonShifts, int numOfNightShifts) {
        this.model = new Model("RosterModel");
        this.period = period;
        this.numEmployees = numEmployees;
        this.numOfMorningShifts = model.intVar(numOfMorningShifts);
        this.numOfAfternoonShifts = model.intVar(numOfAfternoonShifts);
        this.numOfNightShifts = model.intVar(numOfNightShifts);
        setPeriodDivisor(4);
    }
    public void postAllHardConstraints(){
        // Constraints
        IntVar[][] roster = model.intVarMatrix("roster", period, numEmployees, 0, Shift.values().length - 1);
        for(int i=0; i < numEmployees; i++){
            // every 4 days at least 1 holidays
            IntVar[] cols = ArrayUtils.getColumn(roster, i);
            for (int subPeroid = 0; subPeroid < numSubPeriods; subPeroid++) {
                IntVar[] subCols = Arrays.copyOfRange(cols, subPeriodLength * subPeroid,
                        (subPeriodLength * subPeroid) + subPeriodLength-1);
                model.count(Shift.OFF.ordinal(), subCols, OffDaysRange).post();
            }
            for (int day = 0; day < period; day++){
                // Each shift has 4 allocations
                model.count(Shift.MORNING.ordinal(), roster[day], numOfMorningShifts) .post();
                model.count(Shift.AFTERNOON.ordinal(), roster[day], numOfAfternoonShifts).post();
                model.count(Shift.NIGHT.ordinal(), roster[day], numOfNightShifts).post();

                if (day < period - 1){
                    // no Morning after Late
                    roster[day][i].eq(3).imp(roster[day+1][i].ne(1)).post();
                    // cannot work on the same shift for more than three days
                    if (day < period - 3){
                        roster[day][i].eq(roster[day+1][i]).eq(roster[day+2][i]).imp(roster[day+3][i].ne(roster[day+1][i])).post();
                    }
                }
            }
        }
        this.roster = roster;
    }
    public void addRequest(int employeeNumber, int dayOffRangeStart, int dayOffRangeEnd, int minNumOff){
        Request request = new Request(employeeNumber, dayOffRangeStart, dayOffRangeEnd, minNumOff);
        requests.add(request);
    }
    public void postAllUserConstraints(){
        numberOfGranted = model.intVar("numberOfGranted", 0, requests.size());
        IntVar[] dayOffGranted = model.intVarArray("dayOffGranted", requests.size(), 0, 1);
        for (Request request : requests) {
            IntVar[] employeeCol = ArrayUtils.getColumn(roster, request.getEmployeeNumber());
            IntVar[] rangeCol = Arrays.copyOfRange(employeeCol, request.getDayOffRangeStart(), request.getDayOffRangeEnd() + 1);
            IntVar rangeDays = model.intVar(request.getMinNumOff(), request.getMaxNumOff());
            Constraint count = model.count(Shift.OFF.ordinal(), rangeCol, rangeDays);
            model.ifOnlyIf(model.arithm(dayOffGranted[requests.indexOf(request)], ">", 0), count);
        }
        model.sum(dayOffGranted,"=",numberOfGranted).post();
        model.setObjective(Model.MAXIMIZE, numberOfGranted);
    }
    public Model getModel() {
        return model;
    }
    public IntVar[][] getRoster() {
        return roster;
    }
    public IntVar getNumberOfGranted () {return numberOfGranted;}
    private void setPeriodDivisor(int periodDivisor){
        this.OffDaysRange = model.intVar(1, periodDivisor);
        this.numSubPeriods = (int) Math.ceil((double)period / periodDivisor);
        this.subPeriodLength = (int) Math.ceil((double)period / numSubPeriods);
    }

}
