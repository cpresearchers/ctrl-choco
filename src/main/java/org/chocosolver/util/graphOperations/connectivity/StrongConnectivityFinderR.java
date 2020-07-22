package org.chocosolver.util.graphOperations.connectivity;

import org.chocosolver.util.objects.IntTuple2;
import org.chocosolver.util.objects.graphs.DirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Stack;

public class StrongConnectivityFinderR {
    // input
    private DirectedGraph graph;
    private BitSet unvisited;
    private int n;

    //栈
    private int[] stack;
    private BitSet inStack;
    int stackIdx = 0;

    // 标记SCC
    private int nbSCC;
    private int[] nodeSCC;

    //
    private int maxDFS = 1;
    private int[] DFSNum;
    private int[] lowLink;
    private boolean hasSCCSplit = false;
    private Stack<IntTuple2> DE;
    private boolean unconnected = false;

    private ArrayList<IntTuple2> cycles;
    IntTuple2 tt;

    private Iterator<Integer>[] iters;
//    private int index = 0;
//    private BitSet visited;


    public StrongConnectivityFinderR(DirectedGraph graph) {
        this.graph = graph;
        this.n = graph.getNbMaxNodes();

        stack = new int[n];
        inStack = new BitSet(n);

        nodeSCC = new int[n];
        nbSCC = 0;

        DFSNum = new int[n];
        lowLink = new int[n];

        unvisited = new BitSet(n);
        cycles = new ArrayList<>();
        iters = new Iterator[n];
//        p = new int[n];
//        inf = new int[n];
//        nodeOfDfsNum = new int[n];
//        dfsNumOfNode = new int[n];
//        restriction = new BitSet(n);
//        sccFirstNode = new int[n];
//        nextNode = new int[n];
//        nodeSCC = new int[n];
//        nbSCC = 0;
//        //noinspection unchecked
//        iterator = new Iterator[n];
    }

    public void findAllSCC() {
        ISet nodes = graph.getNodes();
        for (int i = 0; i < n; i++) {
            unvisited.set(i, nodes.contains(i));
        }
        findAllSCCOf(unvisited);
    }

    private void findAllSCCOf(BitSet restriction) {
        // initialization
        clearStack();
        maxDFS = 0;
        nbSCC = 0;

        for (int i = 0; i < n; i++) {
            lowLink[i] = n + 2;
            nodeSCC[i] = -1;
            DFSNum[i] = -1;
        }

        findSingletons(restriction);
        int v = restriction.nextSetBit(0);
        while (v >= 0) {
            strongConnectR(v);
            v = restriction.nextSetBit(v);
        }
    }

    private void strongConnectR(int curnode) {
        pushStack(curnode);
        DFSNum[curnode] = maxDFS;
        lowLink[curnode] = maxDFS;
        maxDFS++;
        unvisited.clear(curnode);

        Iterator<Integer> iterator = graph.getSuccOf(curnode).iterator();
        while (iterator.hasNext()) {
            int newnode = iterator.next();
            if (!unvisited.get(newnode)) {
                if (inStack.get(newnode)) {
                    lowLink[curnode] = Math.min(lowLink[curnode], DFSNum[newnode]);
                }
            } else {
                strongConnectR(newnode);
                lowLink[curnode] = Math.min(lowLink[curnode], lowLink[newnode]);
            }
        }

        if (lowLink[curnode] == DFSNum[curnode]) {
            if (lowLink[curnode] > 0 || inStack.cardinality() > 0) {
                hasSCCSplit = true;
            }
            if (hasSCCSplit) {
                int stacknode = -1;

                while (stacknode != curnode) {
                    stacknode = popStack();
                    nodeSCC[stacknode] = nbSCC;
                }
                nbSCC++;
            }
        }
    }

    private void findSingletons(BitSet restriction) {
        ISet nodes = graph.getNodes();
        for (int i = restriction.nextSetBit(0); i >= 0; i = restriction.nextSetBit(i + 1)) {
            if (nodes.contains(i) && graph.getPredOf(i).size() * graph.getSuccOf(i).size() == 0) {
                nodeSCC[i] = nbSCC;
                restriction.clear(i);
            }
        }
    }

    public boolean findAllSCC_ED(Stack<IntTuple2> deleteEdge) {
        DE = deleteEdge;
        ISet nodes = graph.getNodes();
        for (int i = 0; i < n; i++) {
            unvisited.set(i, nodes.contains(i));
        }
        return findAllSCCOf_ED(unvisited);
    }

    private boolean findAllSCCOf_ED(BitSet restriction) {
        // initialization
        clearStack();
        maxDFS = 0;
        nbSCC = 0;
        unconnected = false;
        cycles.clear();

        for (int i = 0; i < n; i++) {
            lowLink[i] = n + 2;
            nodeSCC[i] = -1;
            DFSNum[i] = -1;
        }

        findSingletons(restriction);
        int v = restriction.nextSetBit(0);
        while (v >= 0) {
            if (strongConnect_EDR(v)) {
                return true;
            }
            v = restriction.nextSetBit(v);
        }
        return false;
    }

    private boolean strongConnect_EDR(int curnode) {
        pushStack(curnode);
        DFSNum[curnode] = maxDFS;
        lowLink[curnode] = maxDFS;
        maxDFS++;
        unvisited.clear(curnode);

        Iterator<Integer> iterator = graph.getSuccOf(curnode).iterator();
        while (iterator.hasNext()) {
            int newnode = iterator.next();
            if (!unvisited.get(newnode)) {
                if (inStack.get(newnode)) {
                    lowLink[curnode] = Math.min(lowLink[curnode], DFSNum[newnode]);
                    if (!unconnected) {
                        addCycles(lowLink[newnode], maxDFS - 1);
                        while (!DE.empty() && inCycles(DE.peek())) {
                            DE.pop();
                        }
                    }
                }
            } else {
                if (strongConnect_EDR(newnode)) {
                    return true;
                }
                lowLink[curnode] = Math.min(lowLink[curnode], lowLink[newnode]);
            }
        }

        if (lowLink[curnode] == DFSNum[curnode]) {
            if (lowLink[curnode] > 0 || inStack.cardinality() > 0) {
                hasSCCSplit = true;
            }
            if (hasSCCSplit) {
                int stacknode = -1;

                while (stacknode != curnode) {
                    stacknode = popStack();
                    nodeSCC[stacknode] = nbSCC;
                }
                nbSCC++;

                unconnected = true;
            }
        }

        if (!unconnected && DE.empty()) {
//            System.out.println("xixi");
            return true;
        }

        return false;
    }


//    private void strongConnect(int curnode) {
//        pushStack(curnode);
//        DFSNum[curnode] = maxDFS;
//        lowLink[curnode] = maxDFS;
//        maxDFS++;
//        unvisited.clear(curnode);
//        int curLevel = 0;
//
//        while (stackIdx > 0) {
//            iters[curLevel] = graph.getSuccOf(curnode).iterator();
//            while (iters[curLevel].hasNext()) {
//                int newnode = iters[curLevel].next();
//                if (!unvisited.get(newnode)) {
//                    if (inStack.get(newnode)) {
//                        lowLink[curnode] = Math.min(lowLink[curnode], DFSNum[newnode]);
//                    }
//                } else {
////                    if (strongConnect_EDR(newnode)) {
////                        return true;
////                    }
//                    pushStack(newnode);
//                    DFSNum[newnode] = maxDFS;
//                    lowLink[newnode] = maxDFS;
//                    maxDFS++;
//                    unvisited.clear(newnode);
//                    curLevel++;
//                    iters[] = graph.getSuccOf(newnode).iterator();
////                    lowLink[newnode] = Math.min(lowLink[curnode], lowLink[newnode]);
//                }
//
////                if (unvisited.get(newnode)) {
////                    if (!inStack.get(newnode)) {
////                        pushStack(newnode);
////                        DFSNum[newnode] = maxDFS;
////                        lowLink[newnode] = maxDFS;
////                        maxDFS++;
////                        unvisited.clear(newnode);
////                    }
//////                    else {
//////                        lowLink[curnode] = Math.min(lowLink[curnode], DFSNum[newnode]);
//////                    }
////                }
////                else {
////                    lowLink[curnode] = Math.min(lowLink[curnode], DFSNum[newnode]);
////                }
//            }
//        }
//    }

    private void pushStack(int v) {
        stack[stackIdx++] = v;
        inStack.set(v);
    }

    private void clearStack() {
        inStack.clear();
        stackIdx = 0;
    }

    private int popStack() {
        int x = stack[--stackIdx];
        inStack.clear(x);
        return x;
    }

    public int[] getNodesSCC() {
        return nodeSCC;
    }

//    boolean inStack()

    private void addCycles(int a, int b) {
//        Iterator<IntTuple2> iter = cycles.iterator();
//        IntTuple2 t;
//        while (iter.hasNext()) {
//            t = iter.next();
//            if (t.overlap(a, b)) {
//                t.a = Math.min(t.a, a);
//                t.b = Math.max(t.b, b);
//                return;
//            }
//        }

        IntTuple2 t;
        for (int i = 0, len = cycles.size(); i < len; ++i) {
            t = cycles.get(i);
            if (t.overlap(a, b)) {
                t.a = Math.min(t.a, a);
                t.b = Math.max(t.b, b);
                return;
            }
        }
        cycles.add(new IntTuple2(a, b));
//        System.out.println("cycles: " + cycles);
    }

    private boolean inCycles(IntTuple2 t) {
//        for (IntTuple2 tt : cycles) {
////            System.out.println("inCycles: (" + t.a + ", " + t.b + ") , = (" + dfsNumOfNode[t.a] + ", " + dfsNumOfNode[t.b] + ") =" +(tt.cover(dfsNumOfNode[t.a]) && tt.cover(dfsNumOfNode[t.b])));
////            if (tt.cover(dfsNumOfNode[t.a], dfsNumOfNode[t.b])) {
////                return true;
////            }
//            if (tt.cover(DFSNum[t.a]) && tt.cover(DFSNum[t.b])) {
//                return true;
//            }
//        }
        IntTuple2 tt;
        for (int i = 0, len = cycles.size(); i < len; ++i) {
            tt = cycles.get(i);
            if (tt.cover(DFSNum[t.a]) && tt.cover(DFSNum[t.b])) {
                return true;
            }
        }
        return false;
    }
}
