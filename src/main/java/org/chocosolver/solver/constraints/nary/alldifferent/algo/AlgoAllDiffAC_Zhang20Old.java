/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.alldifferent.algo;

import amtf.Measurer;
import gnu.trove.map.hash.TIntIntHashMap;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.util.graphOperations.connectivity.StrongConnectivityFinder;
import org.chocosolver.util.graphOperations.connectivity.StrongConnectivityNewFinder;
import org.chocosolver.util.objects.IntTuple2;
import org.chocosolver.util.objects.graphs.DirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.chocosolver.util.procedure.UnaryIntProcedure;

import java.util.*;

/**
 * Algorithm of Alldifferent with AC
 * <p>
 * Uses Regin algorithm 这是目前最为公平的比较
 * Runs in O(m.n) worst case time for the initial propagation
 * but has a good average behavior in practice
 * <p/>
 * Keeps track of previous matching for further calls
 * <p/>
 *
 * @author Jean-Guillaume Fages
 */
public class AlgoAllDiffAC_Zhang20Old {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************
    // 约束的个数
    static public int num = 0;
    // 约束的编号
    private int id;

    private int n, n2;
    private DirectedGraph digraph;
    private int[] matching;
    private int[] nodeSCC;
    private BitSet free;
    private StrongConnectivityNewFinder SCCfinder;
    // for augmenting matching (BFS)
    private int[] father;
    private BitSet in;
    private TIntIntHashMap map;
    private int[] fifo;
    private IntVar[] vars;
    private ICause aCause;

    // for early detection
    protected IIntDeltaMonitor[] monitors;
    private UnaryIntProcedure<Integer> onValRem;
    private Stack<IntTuple2> DE;

//    private BitSet[] bitDoms;
//
//    private int[] olb;
//    private int[] oub;
//    private int[] iniDomSize;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public AlgoAllDiffAC_Zhang20Old(IntVar[] variables, ICause cause) {
        id = num++;

        this.vars = variables;
        aCause = cause;
        n = vars.length;
        // 存储匹配
        matching = new int[n];
        for (int i = 0; i < n; i++) {
            matching[i] = -1;
        }
        map = new TIntIntHashMap();
        IntVar v;
        int ub;
        int idx = n;
        // 统计所有变量论域中不同值的个数
        for (int i = 0; i < n; i++) {
            v = vars[i];
            ub = v.getUB();
            for (int j = v.getLB(); j <= ub; j = v.nextValue(j)) {
                if (!map.containsKey(j)) {
                    map.put(j, idx);
                    idx++;
                }
            }
        }
        n2 = idx;
        // 使用队列实现非递归广度优先搜索
        fifo = new int[n2];
        // 用Bitset邻接矩阵的有向图
        digraph = new DirectedGraph(n2 + 1, SetType.BITSET, false);
        // free应该区分匹配点和非匹配点（true表示非匹配点，false表示匹配点）
        free = new BitSet(n2);
        // 用于回溯增广路径
        father = new int[n2];
        // 标记进入fifo队列中的点（true表示进入过，false表示没有进入过）
        in = new BitSet(n2);

        //for early detection
        // 存的是变量索引及原值
        DE = new Stack<IntTuple2>();
        SCCfinder = new StrongConnectivityNewFinder(digraph);

        // for delta
        monitors = new IIntDeltaMonitor[vars.length];
        for (int i = 0; i < vars.length; i++) {
            monitors[i] = vars[i].monitorDelta(cause);
        }
        onValRem = makeProcedure();

        //for early detection
        // 存的是变量索引及原值
        DE = new Stack<IntTuple2>();
    }

    protected UnaryIntProcedure<Integer> makeProcedure() {
        return new UnaryIntProcedure<Integer>() {
            int var;

            @Override
            public UnaryIntProcedure set(Integer o) {
                var = o;
                return this;
            }

            @Override
            public void execute(int i) throws ContradictionException {
//                currTable.addToMask((supports[var][i - off]));
                DE.push(new IntTuple2(var, map.get(i)));
//                IntVar v = vars[var];
//                System.out.println(vars[var].getName() + "," + var + ", " + i + " = " + v.contains(i) + ", size = " + v.getDomainSize());
            }
        };
    }

    //***********************************************************************************
    // PROPAGATION
    //***********************************************************************************

    public boolean propagate() throws ContradictionException {
//        System.out.println("----------------" + id + " propagate----------------");
        DE.clear();

        long startTime = System.nanoTime();
        // 统计delta
        for (int i = 0; i < n; ++i) {
            monitors[i].freeze();
            monitors[i].forEachRemVal(onValRem.set(i));
        }

//        for (int i = 0; i < n; ++i) {
//            v = vars[i];
//            for (int j = bitDoms[i].nextSetBit(0); j >= 0 && j < iniDomSize[i]; j = bitDoms[i].nextSetBit(j + 1)) {
//
//                if (!v.contains(j)) {
//                    System.out.println("delete: " + v.getName() + ", " + j);
//                }
//            }
//        }

        System.out.println("|DE| = " + DE.size());
//        Iterator iterator = DE.iterator();
//        while (iterator.hasNext()) {
//            System.out.println(iterator.next());
//        }
        System.out.println(DE);
        System.out.println("--------------------------------");

        findMaximumMatching();
        Measurer.matchingTime += System.nanoTime() - startTime;

        startTime = System.nanoTime();
        boolean filter = filter();

        for (int i = 0; i < vars.length; i++) {
            monitors[i].unfreeze();
        }

//        for (int i = 0; i < vars.length; i++) {
//            bitDoms[i].clear();
//            v = vars[i];
//            ub = v.getUB();
//            for (k = v.getLB(); k <= ub; k = v.nextValue(k)) {
//                bitDoms[i].set(k);
////                System.out.println("have: " + v.getName() + ", " + k);
//            }
////            System.out.println(v.getDomainSize() + ", " + bitDoms[i].cardinality());
//        }

        Measurer.filterTime += System.nanoTime() - startTime;
        return filter;
    }

    //***********************************************************************************
    // Initialization
    //***********************************************************************************

    private void findMaximumMatching() throws ContradictionException {
        // 每次都重新建图
        for (int i = 0; i < n2; i++) {
            digraph.getSuccOf(i).clear();
            digraph.getPredOf(i).clear();
        }
        free.set(0, n2);
        int k, ub;
        IntVar v;
        for (int i = 0; i < n; i++) {
            v = vars[i];
            ub = v.getUB();
            int mate = matching[i];
            for (k = v.getLB(); k <= ub; k = v.nextValue(k)) {
                int j = map.get(k);
                // 利用之前已经找到的匹配
                if (mate == j) {
                    assert free.get(i) && free.get(j);
                    digraph.addArc(j, i);
                    free.clear(i);
                    free.clear(j);
                } else {
                    digraph.addArc(i, j);
                }
            }
        }
        // 尝试为每个变量都寻找一个匹配，即最大匹配的个数要与变量个数相等，否则回溯
        // 利用匈牙利算法寻找最大匹配
        for (int i = free.nextSetBit(0); i >= 0 && i < n; i = free.nextSetBit(i + 1)) {
            tryToMatch(i);
        }
        // 匹配边是由值指向变量，非匹配边是由变量指向值
        for (int i = 0; i < n; i++) {
            matching[i] = digraph.getPredOf(i).isEmpty() ? -1 : digraph.getPredOf(i).iterator().next();
        }
    }

    private void tryToMatch(int i) throws ContradictionException {
        int mate = augmentPath_BFS(i);
        if (mate != -1) {// 值mate是一个自由点
            free.clear(mate);
            free.clear(i);
            int tmp = mate;
            // 沿着father回溯即是增广路径
            while (tmp != i) {
                // 翻转边的方向
                digraph.removeArc(father[tmp], tmp);
                digraph.addArc(tmp, father[tmp]);
                // 回溯
                tmp = father[tmp];
            }
        } else {//应该是匹配失败，即最大匹配个数与变量个数不相等，需要回溯
            vars[0].instantiateTo(vars[0].getLB() - 1, aCause);
//            System.out.println("+instantiate : " + vars[0].getName() + ", " + (vars[0].getLB() - 1));
        }
    }

    // 广度优先搜索寻找增广路
    private int augmentPath_BFS(int root) {
        // root是一个自由点（变量）。
        // 如果与root相连的值中有自由点，就返回第一个自由点；
        // 如果没有，尝试为匹配变量找一个新的自由点，过程中通过father标记增广路径。
        in.clear();
        int indexFirst = 0, indexLast = 0;
        fifo[indexLast++] = root;
        int x;
        ISetIterator succs;
        while (indexFirst != indexLast) {
            x = fifo[indexFirst++];
            // 如果x是一个变量，那么它的后继就是非匹配的值；
            // 如果x是一个值，那么它的后继只有一个，是与它匹配的变量。
            succs = digraph.getSuccOf(x).iterator();
            while (succs.hasNext()) {
                int y = succs.nextInt();
                if (!in.get(y)) {
                    father[y] = x;
                    fifo[indexLast++] = y;
                    in.set(y);
                    if (free.get(y)) { //自由点（值）
                        return y;
                    }
                }
            }
        }
        return -1;
    }

    //***********************************************************************************
    // PRUNING
    //***********************************************************************************

    private boolean buildSCC() {
        if (n2 > n * 2) {// 添加额外的点t
            digraph.removeNode(n2);
            digraph.addNode(n2);
            for (int i = n; i < n2; i++) {
                if (free.get(i)) {
                    digraph.addArc(i, n2);
                } else {
                    digraph.addArc(n2, i);
                }
            }
        }
        if (SCCfinder.findAllSCCWithEarlyDetection(DE)) {
            return true;
        }
//        SCCfinder.findAllSCC();
        nodeSCC = SCCfinder.getNodesSCC();
//        System.out.println(Arrays.toString(nodeSCC));
        digraph.removeNode(n2);

        return false;
    }

    private boolean filter() throws ContradictionException {
        boolean filter = false;
        if (buildSCC()) {
            return true;
        }
        int j, ub;
        IntVar v;
        for (int i = 0; i < n; i++) {
            v = vars[i];
            ub = v.getUB();
            for (int k = v.getLB(); k <= ub; k = v.nextValue(k)) {
                j = map.get(k);
                if (nodeSCC[i] != nodeSCC[j]) {
                    if (matching[i] == j) {
                        filter |= v.instantiateTo(k, aCause);
                        System.out.println("instantiate  : " + v.getName() + ", " + k);
//                        bitDoms[i].clear();
//                        bitDoms[i].set(k);
                    } else {
                        filter |= v.removeValue(k, aCause);
                        System.out.println("second delete: " + v.getName() + ", " + k);
//                        bitDoms[i].clear(k);
//                        digraph.removeArc(i, j);
                    }
                }
            }
        }
        return filter;
    }

//    private void addCycles(int a, int b) {
//
//    }

//    private boolean inCycles(IntTuple2 t) {
//
//        for (IntTuple2 t : deletedEdges) {
//            if(dfs)
//        }
//
//        return false;
//    }
}
