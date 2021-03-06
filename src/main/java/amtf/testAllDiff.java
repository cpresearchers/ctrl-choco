package amtf;

import amtf.parser.XCSPParser;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.variables.ImpactBased;
import org.chocosolver.solver.variables.IntVar;

import java.util.Arrays;
import java.util.Comparator;

import static java.lang.System.out;
import static org.chocosolver.solver.search.strategy.Search.*;


public class testAllDiff {

    public static void main(String[] args) {
        float IN_SEC = 1000 * 1000 * 1000f;

        String[] instances = new String[]{
//                "G:/X3Benchmarks/alldiff/GracefulGraph/GracefulGraph-m1-s1/GracefulGraph-K03-P05.xml",
//                "G:/X3Benchmarks/alldiff/Langford/Langford-m1-k2/Langford-2-08.xml",
//                "G:/X3Benchmarks/alldiff/Langford/Langford-m1-k4/Langford-4-07.xml",
//                "F:\\chenj\\data\\XCSP3\\Queens-m1-s1\\Queens-0050-m1.xml",
//                "G:\\X3Benchmarks\\alldiff\\Queens\\Queens-m1-s1\\Queens-0004-m1.xml",
//                "F:\\chenj\\data\\XCSP3\\AllDiff\\LatinSquare-xcsp2-bqwh15-106\\bqwh-15-106-01_X2.xml",
//                "F:\\chenj\\data\\XCSP3\\AllDiff\\LatinSquare-xcsp2-bqwh15-106\\bqwh-15-106-02_X2.xml",
//                "F:\\chenj\\data\\XCSP3\\AllDiff\\LatinSquare-xcsp2-bqwh15-106\\bqwh-15-106-03_X2.xml",
//                "F:\\chenj\\data\\XCSP3\\AllDiff\\LatinSquare-xcsp2-bqwh18-141\\bqwh-18-141-01_X2.xml",
//                "F:\\chenj\\data\\XCSP3\\AllDiff\\LatinSquare-xcsp2-bqwh18-141\\bqwh-18-141-02_X2.xml",
//                "F:\\chenj\\data\\XCSP3\\AllDiff\\LatinSquare-xcsp2-bqwh18-141\\bqwh-18-141-03_X2.xml",
//                "F:\\chenj\\data\\XCSP3\\AllDiff\\LatinSquare-m1-gp\\qwh-o30-h374-01.xml",
//                "F:\\chenj\\data\\XCSP3\\AllDiff\\LatinSquare-m1-gp\\qwh-o30-h374-02.xml",
//                "F:\\chenj\\data\\XCSP3\\AllDiff\\LatinSquare-m1-gp\\qwh-o30-h374-03.xml",
//                "F:\\chenj\\data\\XCSP3\\AllDiff\\LatinSquare-m1-gp\\qwh-o30-h374-04.xml",
//                "F:\\chenj\\data\\XCSP3\\AllDiff\\LatinSquare-m1-gs\\qwh-o010-h100.xml",
//                "F:\\chenj\\data\\XCSP3\\AllDiff/ColouredQueens-m1-s1/ColouredQueens-03.xml",
//                "F:\\chenj\\data\\XCSP3\\AllDiff/ColouredQueens-m1-s1/ColouredQueens-05.xml",
//                "F:\\chenj\\data\\XCSP3\\AllDiff/ColouredQueens-m1-s1/ColouredQueens-06.xml",
//                "F:\\chenj\\data\\XCSP3\\AllDiff/ColouredQueens-m1-s1/ColouredQueens-07.xml",
//                "G:/X3Benchmarks/alldiff/ColouredQueens/ColouredQueens-m1-s1/ColouredQueens-09.xml",
//                "G:/X3Benchmarks/alldiff/DistinctVectors/DistinctVectors-m1-s1/DistinctVectors-30-010-02.xml",
//                "F:\\chenj\\data\\XCSP3\\AllDiff\\SchurrLemma-mod-s1\\SchurrLemma-012-9-mod.xml",
//                "F:\\chenj\\data\\XCSP3\\AllDiff\\SchurrLemma-mod-s1\\SchurrLemma-015-9-mod.xml",
//                "F:\\chenj\\data\\XCSP3\\AllDiff\\SchurrLemma-mod-s1\\SchurrLemma-020-9-mod.xml",
//                "F:\\chenj\\data\\XCSP3\\AllDiff\\SchurrLemma-mod-s1\\SchurrLemma-030-9-mod.xml",
//                "C:\\bench\\X3\\Queens\\Queens-0004-m1.xml",
//                "C:\\bench\\X3\\SportsScheduling\\SportsScheduling-08.xml",
//                "C:\\bench\\X3\\SportsScheduling\\SportsScheduling-08.xml",
                "/Users/lizhe/allDiff_Series/Queens/Queens-m1-s1/Queens-0008-m1.xml",
//                "/Users/lizhe/allDiff_Series/ColouredQueens/ColouredQueens-m1-s1/ColouredQueens-05.xml",
//                "F:\\X3Benchmarks\\alldiff\\Queens-m1-s1\\Queens-0050-m1.xml"
//                "F:/X3Benchmarks/alldiff/ColouredQueens-m1-s1/ColouredQueens-05.xml"
        };
        XCSPParser parser = new XCSPParser();
        String[] algorithms = new String[]{
                "AC",
                "ACFair",
//                "AC2",
//                "ACZhang18",
//                "ACZhang18M",
//                "ACZhang20",
//                "ACNaive",
//                "BC",
        };
        int runNum = 1;

        for (String ins : instances) {
            out.println(ins);
            for (String algo : algorithms) {
                out.println(algo + "====>");
                for (int i = 0; i < runNum; i++) {
                    Measurer.initial();
                    Model model = new Model();
                    try {
                        parser.model(model, ins, algo);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    IntVar[] decVars = (IntVar[]) model.getHook("decisions");
                    if (decVars == null) {
                        decVars = parser.mvars.values().toArray(new IntVar[0]);
                    }
                    Arrays.sort(decVars, Comparator.comparingInt(IntVar::getId));
                    Solver solver = model.getSolver();
//                    solver.setSearch(Search.defaultSearch(model));
                    solver.setSearch(Search.activityBasedSearch(decVars));

//                    solver.setSearch(Search.minDomLBSearch(decVars));
//                    solver.setSearch(new ImpactBased(decVars, true));
//                    solver.setSearch(Search.domOverWDegSearch(decVars));

//                    solver.setSearch(intVarSearch(new FirstFail(model), new IntDomainMin(), decVars));
//                solver.setSearch(intVarSearch();

                    if (solver.solve()) {
                        if (i == runNum - 1) {
                            out.print("solution: ");
                            for (IntVar v : decVars) {
                                out.printf("%d ", v.getValue());
                            }
                            out.println();
                        }
                    }
                    if (i == runNum - 1) {
                        out.println("node: " + solver.getNodeCount());
                        out.println("time: " + solver.getTimeCount() + "s");
                        out.println("find matching time: " + Measurer.matchingTime / IN_SEC + "s");
                        out.println("filter time: " + Measurer.filterTime / IN_SEC + "s");
                        out.println("scc time: " + Measurer.checkSCCTime / IN_SEC + "s");
                        out.println("numProp: " + Measurer.numProp);
                        out.println("numNone: " + Measurer.numNone);
                        out.println("numSkip: " + Measurer.numSkip);
                        out.println("numP1: " + Measurer.numP1);
                        out.println("numP2: " + Measurer.numP2);
                        out.println("numP1AndP2: " + Measurer.numP1AndP2);
//                        solver.printStatistics();
                    }
                }
            }
        }
    }
}
