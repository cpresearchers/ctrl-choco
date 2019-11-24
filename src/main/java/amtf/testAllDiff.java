package amtf;

import amtf.parser.XCSPParser;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.FirstFail;
import org.chocosolver.solver.variables.IntVar;

import java.util.Arrays;
import java.util.Comparator;

import static java.lang.System.out;
import static org.chocosolver.solver.search.strategy.Search.activityBasedSearch;
import static org.chocosolver.solver.search.strategy.Search.intVarSearch;


public class testAllDiff {

    public static void main(String[] args) {
        float IN_SEC = 1000 * 1000 * 1000f;

        String[] instances = new String[]{
//                "E:\\X3Benchmarks\\alldiff\\GracefulGraph\\GracefulGraph-m1-s1\\GracefulGraph-K03-P05.xml",
//                "E:\\X3Benchmarks\\alldiff\\Langford\\Langford-m1-k2\\Langford-2-09.xml",
//                "E:\\X3Benchmarks\\alldiff\\Langford\\Langford-m1-k3\\Langford-3-06.xml",
                "E:\\X3Benchmarks\\alldiff\\Queens\\Queens-m1-s1\\Queens-0080-m1.xml",
//                "E:\\X3Benchmarks\\alldiff\\LatinSquare\\LatinSquare-xcsp2-bqwh15-106\\bqwh-15-106-01_X2.xml",
//                "E:\\X3Benchmarks\\alldiff\\LatinSquare\\LatinSquare-xcsp2-bqwh15-106\\bqwh-15-106-02_X2.xml",
//                "E:\\X3Benchmarks\\alldiff\\LatinSquare\\LatinSquare-xcsp2-bqwh15-106\\bqwh-15-106-03_X2.xml",
//                "E:\\X3Benchmarks\\alldiff\\LatinSquare\\LatinSquare-xcsp2-bqwh18-141\\bqwh-18-141-01_X2.xml",
//                "E:\\X3Benchmarks\\alldiff\\LatinSquare\\LatinSquare-xcsp2-bqwh18-141\\bqwh-18-141-02_X2.xml",
//                "E:\\X3Benchmarks\\alldiff\\LatinSquare\\LatinSquare-xcsp2-bqwh18-141\\bqwh-18-141-03_X2.xml",
//                "E:\\X3Benchmarks\\alldiff\\ColouredQueens\\ColouredQueens-m1-s1\\ColouredQueens-08.xml",
//                "E:\\X3Benchmarks\\alldiff\\ColouredQueens\\ColouredQueens-m1-s1\\ColouredQueens-09.xml",
//                "E:\\X3Benchmarks\\alldiff\\DistinctVectors\\DistinctVectors-m1-s1\\DistinctVectors-30-010-02.xml",
//                "E:\\X3Benchmarks\\alldiff\\SchurrLemma\\SchurrLemma-mod-s1\\SchurrLemma-012-9-mod.xml",
//                "E:\\X3Benchmarks\\alldiff\\SchurrLemma\\SchurrLemma-mod-s1\\SchurrLemma-015-9-mod.xml",
//                "E:\\X3Benchmarks\\alldiff\\SchurrLemma\\SchurrLemma-mod-s1\\SchurrLemma-020-9-mod.xml",
//                "E:\\X3Benchmarks\\alldiff\\SchurrLemma\\SchurrLemma-mod-s1\\SchurrLemma-030-9-mod.xml",
        };
        XCSPParser parser = new XCSPParser();
        String allDiffConsistency;
        int runNum = 1;

        for (String ins : instances) {
//
//            allDiffConsistency = "AC";
//            for (int i = 0; i < runNum; i++) {
//                TimeCount.initial();
//                out.println(ins);
//                out.println(allDiffConsistency + "====>");
//                Model model = new Model();
//                try {
//                    parser.model(model, ins, allDiffConsistency);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                IntVar[] decVars = (IntVar[]) model.getHook("decisions");
//                ;
//                if (decVars == null) {
//                    decVars = parser.mvars.values().toArray(new IntVar[parser.mvars.size()]);
//                }
//                Arrays.sort(decVars, Comparator.comparingInt(IntVar::getId));
//                Solver solver = model.getSolver();
//                solver.setSearch(activityBasedSearch(decVars));
////                solver.setSearch(Search.defaultSearch(model));
//
//                if (solver.solve()) {
//                    out.printf("solution: ");
//                    for (IntVar v : decVars) {
//                        out.printf("%d ", v.getValue());
//                    }
//                    out.println();
//                }
//                out.println("node: " + solver.getNodeCount());
//                out.println("time: " + solver.getTimeCount() + "s");
//                out.println("find matching time: " + TimeCount.matchingTime / IN_SEC + "s");
//                out.println("filter time: " + TimeCount.filterTime / IN_SEC + "s");
//            }
//
            allDiffConsistency = "ACFast";
            for (int i = 0; i < runNum; i++) {
                TimeCount.initial();
                out.println(ins);
                out.println(allDiffConsistency + "====>");
                Model model = new Model();
                try {
                    parser.model(model, ins, allDiffConsistency);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                IntVar[] decVars = (IntVar[]) model.getHook("decisions");
                ;
                if (decVars == null) {
                    decVars = parser.mvars.values().toArray(new IntVar[parser.mvars.size()]);
                }
                Arrays.sort(decVars, Comparator.comparingInt(IntVar::getId));
                Solver solver = model.getSolver();
                solver.setSearch(activityBasedSearch(decVars));
//                solver.setSearch(Search.defaultSearch(model));
//                solver.setSearch(intVarSearch(new FirstFail(model), new IntDomainMin(), decVars));

                if (solver.solve()) {
                    out.printf("solution: ");
                    for (IntVar v : decVars) {
                        out.printf("%d ", v.getValue());
                    }
                    out.println();
                }
                out.println("node: " + solver.getNodeCount());
                out.println("time: " + solver.getTimeCount() + "s");
                out.println("find matching time: " + TimeCount.matchingTime / IN_SEC + "s");
                out.println("filter time: " + TimeCount.filterTime / IN_SEC + "s");
            }

            allDiffConsistency = "ACFastbit2";
            for (int i = 0; i < runNum; i++) {
                TimeCount.initial();
                out.println(ins);
                out.println(allDiffConsistency + "====>");
                Model model = new Model();
                try {
                    parser.model(model, ins, allDiffConsistency);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                IntVar[] decVars = (IntVar[]) model.getHook("decisions");
                ;
                if (decVars == null) {
                    decVars = parser.mvars.values().toArray(new IntVar[parser.mvars.size()]);
                }
                Arrays.sort(decVars, Comparator.comparingInt(IntVar::getId));
                Solver solver = model.getSolver();
                solver.setSearch(activityBasedSearch(decVars));
//                solver.setSearch(Search.defaultSearch(model));
//                solver.setSearch(intVarSearch(new FirstFail(model), new IntDomainMin(), decVars));

                if (solver.solve()) {
                    out.printf("solution: ");
                    for (IntVar v : decVars) {
                        out.printf("%d ", v.getValue());
                    }
                    out.println();
                }
                out.println("node: " + solver.getNodeCount());
                out.println("time: " + solver.getTimeCount() + "s");
                out.println("find matching time: " + TimeCount.matchingTime / IN_SEC + "s");
                out.println("filter time: " + TimeCount.filterTime / IN_SEC + "s");
            }

            allDiffConsistency = "ACNaive";
//            allDiffConsistency = "ACFastbit2";
            for(int i = 0; i < runNum; i++) {
                TimeCount.initial();
                out.println(ins);
                out.println(allDiffConsistency + "====>");
                Model model = new Model();
                try {
                    parser.model(model, ins, allDiffConsistency);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                IntVar[] decVars = (IntVar[]) model.getHook("decisions");;
                if(decVars == null){
                    decVars = parser.mvars.values().toArray(new IntVar[parser.mvars.size()]);
                }
                Arrays.sort(decVars, Comparator.comparingInt(IntVar::getId));
                Solver solver = model.getSolver();
                solver.setSearch(activityBasedSearch(decVars));

                if (solver.solve()) {
                    out.printf("solution: ");
                    for (IntVar v: decVars) {
                        out.printf("%d ", v.getValue());
                    }
                    out.println();
                }
                out.println("node: " + solver.getNodeCount());
                out.println("time: " + solver.getTimeCount() + "s");
                out.println("find matching time: " + TimeCount.matchingTime / IN_SEC + "s");
                out.println("filter time: " + TimeCount.filterTime / IN_SEC + "s");
            }
//
//            allDiffConsistency = "ACFastbit1";
//            for (int i = 0; i < runNum; i++) {
//                TimeCount.initial();
//                out.println(ins);
//                out.println(allDiffConsistency + "====>");
//                Model model = new Model();
//                try {
//                    parser.model(model, ins, allDiffConsistency);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                IntVar[] decVars = (IntVar[]) model.getHook("decisions");
//                ;
//                if (decVars == null) {
//                    decVars = parser.mvars.values().toArray(new IntVar[parser.mvars.size()]);
//                }
//                Arrays.sort(decVars, Comparator.comparingInt(IntVar::getId));
//                Solver solver = model.getSolver();
//                solver.setSearch(activityBasedSearch(decVars));
////                solver.setSearch(Search.defaultSearch(model));
//
//
//                if (solver.solve()) {
//                    out.printf("solution: ");
//                    for (IntVar v : decVars) {
//                        out.printf("%d ", v.getValue());
//                    }
//                    out.println();
//                }
//                out.println("node: " + solver.getNodeCount());
//                out.println("time: " + solver.getTimeCount() + "s");
//                out.println("find matching time: " + TimeCount.matchingTime / IN_SEC + "s");
//                out.println("filter time: " + TimeCount.filterTime / IN_SEC + "s");
//            }
        }
    }
}
