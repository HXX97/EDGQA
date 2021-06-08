package cn.edu.nju.ws.edgqa.eval;

import cn.edu.nju.ws.edgqa.domain.edg.EDG;
import cn.edu.nju.ws.edgqa.domain.edg.Edge;
import cn.edu.nju.ws.edgqa.domain.edg.Node;

public class EDGEvaluation {
    public static double MaxTokenMatch(String[] s1, String[] s2) {
        String[] t1, t2;
        if (s1.length >= s2.length) {
            t1 = s1;
            t2 = s2;
        } else {
            t1 = s2;
            t2 = s1;
        }
        int[] m = new int[t1.length], maxm = new int[t1.length];
        for (int i = 0; i < t1.length; i++) {
            m[i] = i;
            maxm[i] = i;
        }
        return MaxTokenMatch_fun(m, 0, t1, t2, 0, maxm);
    }

    public static double MaxTokenMatch_fun(int[] m, int n, String[] t1, String[] t2, double max, int[] maxm) {
        if (n == t1.length - 1) {
            double result = 0;
            for (int i = 0; i < t1.length; i++) {
                if (m[i] < t2.length) {
                    if (t1[i].equals(t2[m[i]])) {
                        result++;
                    }
                }
            }
            result /= t1.length;
            if (result > max) {
                for (int i = 0; i < t1.length; i++) {
                    maxm[i] = m[i];
                }
                return result;
            }
            return max;
        }
        for (int i = n; i < t1.length; i++) {
            int temp = m[i];
            m[i] = m[n];
            m[n] = temp;
            max = MaxTokenMatch_fun(m, n + 1, t1, t2, max, maxm);
            temp = m[i];
            m[i] = m[n];
            m[n] = temp;
        }
        return max;
    }


    public static String ApproxMatch1(EDG edg1, EDG edg2) { // structure matching, output boolean value
        if (edg1.getNumNode() != edg2.getNumNode()) {
            return "nodenum not matched";
        }
        int n = edg1.getNumNode();
        int i1 = 0, i2 = 0;
        while (i1 < n && edg1.getNodes()[i1].getNodeType() != 1) {
            i1++;
        }
        while (i2 < n && edg2.getNodes()[i2].getNodeType() != 1) {
            i2++;
        }
        if (i1 == n) {
            return "no Root node";
        }
        if (i2 == n) {
            return "golden answer has Root node";
        }
        int[] d1 = new int[n];
        int[] d2 = new int[n];
        d1[i1] = 1;
        d2[i2] = 1;
        return ApproxMatch1_fun(i1, i2, d1, d2, edg1, edg2);
    }

    public static double ApproxMatch2(EDG edg1, EDG edg2) {
        if (!ApproxMatch1(edg1, edg2).equals("match")) {
            return 0;
        }
        int i1 = 0, i2 = 0;
        while (i1 < edg1.getNumNode() && edg1.getNodes()[i1].getNodeType() != 1) {
            i1++;
        }
        while (i2 < edg1.getNumNode() && edg2.getNodes()[i2].getNodeType() != 1) {
            i2++;
        }
        int[] m = new int[edg1.getNumNode()];
        for (int i = 0; i < edg1.getNumNode(); i++) {
            m[i] = i;
        }
        int[] maxm = new int[edg1.getNumNode()];
        for (int i = 0; i < edg1.getNumNode(); i++) {
            maxm[i] = i;
        }
        return ApproxMatch2_fun(m, 0, edg1, edg2, maxm, ApproxMatch2_EstimateDegree(maxm, edg1, edg2));
    }

    public static double ApproxMatch3(EDG edg1, EDG edg2) { // only measure the description node
        int desNum1 = 0, desNum2 = 0;
        Node[] des1 = new Node[edg1.getNumNode()];
        Node[] des2 = new Node[edg2.getNumNode()];
        for (int i = 0; i < edg1.getNumNode(); i++) {
            if (edg1.getNodes()[i].getNodeType() == 3 || edg1.getNodes()[i].getNodeType() == 4) {//verb/non-verb des
                des1[desNum1] = edg1.getNodes()[i];
                desNum1++;
            }
        }
        for (int i = 0; i < edg2.getNumNode(); i++) {
            if (edg2.getNodes()[i].getNodeType() == 3 || edg2.getNodes()[i].getNodeType() == 4) {//verb/non-verb des
                des2[desNum2] = edg2.getNodes()[i];
                desNum2++;
            }
        }
        int[] m = new int[Math.max(desNum1, desNum2)];
        for (int i = 0; i < Math.max(desNum1, desNum2); i++) {
            m[i] = i;
        }
        int[] maxm = new int[Math.max(desNum1, desNum2)];
        for (int i = 0; i < Math.max(desNum1, desNum2); i++) {
            maxm[i] = i;
        }
        if (desNum1 >= desNum2) {
            return ApproxMatch3_fun(m, 0, des1, des2, desNum1, desNum2, maxm, ApproxMatch3_EstimateDegree(maxm, des1, des2, desNum1, desNum2));
        } else {
            return ApproxMatch3_fun(m, 0, des2, des1, desNum2, desNum1, m, ApproxMatch3_EstimateDegree(maxm, des2, des1, desNum2, desNum1));
        }
    }

    public static double ApproxMatch3_fun(int[] m, int n, Node[] des1, Node[] des2, int desnum1, int desnum2, int[] maxm, double max) {
        if (n == desnum1 - 1) {
            double t = ApproxMatch3_EstimateDegree(m, des1, des2, desnum1, desnum2);
            if (t > max) {
                for (int i = 0; i < desnum1; i++) {
                    maxm[i] = m[i];
                }
                return t;
            } else {
                return max;
            }
        }
        for (int i = n; i < desnum1; i++) {
            int temp = m[n];
            m[n] = m[i];
            m[i] = temp;
            max = ApproxMatch3_fun(m, n + 1, des1, des2, desnum1, desnum2, maxm, max);
            temp = m[n];
            m[n] = m[i];
            m[i] = temp;

        }
        return max;
    }

    public static double ApproxMatch3_EstimateDegree(int[] m, Node[] des1, Node[] des2, int desnum1, int desnum2) {
        double sum = 0;
        for (int i = 0; i < desnum1; i++) {
            if (m[i] < desnum2) {
                double temp = ((double) (Math.min(des1[i].getEnd(), des2[m[i]].getEnd()) - Math.max(des1[i].getStart(), des2[m[i]].getStart())))
                        / ((double) (Math.max(des1[i].getEnd(), des2[m[i]].getEnd()) - Math.min(des1[i].getStart(), des2[m[i]].getStart())));
                //System.out.println(i+" "+Math.max(0,temp));
                sum += Math.max(0, temp);
            }
        }
        return sum / (double) desnum1;
    }

    public static double ApproxMatch4(EDG edg1, EDG edg2) {
        int entityNum1 = 0, entityNum2 = 0;
        int[] entity1 = new int[edg1.getNumNode()];
        int[] entity2 = new int[edg2.getNumNode()];
        for (int i = 0; i < edg1.getNumNode(); i++) {
            if (edg1.getNodes()[i].getNodeType() == 2) {//
                entity1[entityNum1] = i;
                entityNum1++;
            }
        }
        for (int i = 0; i < edg2.getNumNode(); i++) {
            if (edg2.getNodes()[i].getNodeType() == 2) { // verb / non-verb des
                entity2[entityNum2] = i;
                entityNum2++;
            }
        }
        int[] m = new int[Math.max(entityNum1, entityNum2)];
        for (int i = 0; i < Math.max(entityNum1, entityNum2); i++) {
            m[i] = i;
        }
        int[] maxm = new int[Math.max(entityNum1, entityNum2)];
        for (int i = 0; i < Math.max(entityNum1, entityNum2); i++) {
            maxm[i] = i;
        }
        if (entityNum1 >= entityNum2) {
            return ApproxMatch4_fun1(m, 0, entity1, entity2, entityNum1, entityNum2, maxm, edg1, edg2, ApproxMatch4_EstimateDegree(maxm, entity1, entity2, entityNum1, entityNum2, edg1, edg2));
        } else {
            return ApproxMatch4_fun1(m, 0, entity2, entity1, entityNum2, entityNum1, maxm, edg2, edg1, ApproxMatch4_EstimateDegree(maxm, entity2, entity1, entityNum2, entityNum1, edg2, edg1));
        }
    }

    public static double ApproxMatch4_fun1(int[] m, int n, int[] entity1, int[] entity2, int entitynum1, int entitynum2, int[] maxm, EDG edg1, EDG edg2, double max) {
        if (n == entitynum1 - 1) {
            double t = ApproxMatch4_EstimateDegree(m, entity1, entity2, entitynum1, entitynum2, edg1, edg2);
            if (t > max) {
                return t;
            } else {
                return max;
            }
        }
        for (int i = n; i < entitynum1; i++) {
            int temp = m[n];
            m[n] = m[i];
            m[i] = temp;
            max = ApproxMatch4_fun1(m, n + 1, entity1, entity2, entitynum1, entitynum2, maxm, edg1, edg2, max);
            temp = m[n];
            m[n] = m[i];
            m[i] = temp;

        }
        return max;
    }

    public static double ApproxMatch4_EstimateDegree(int[] m, int[] entity1, int[] entity2, int entitynum1, int entitynum2, EDG edg1, EDG edg2) {
        double result = 0;
        //int totaldesnum1 = 0,totaldesnum2 = 0;
        for (int i = 0; i < entitynum1; i++) {
            if (m[i] < entitynum2) {
                int desnum1 = 0, desnum2 = 0;
                Node[] des1 = new Node[edg1.getNumNode()];
                Node[] des2 = new Node[edg2.getNumNode()];
                for (int j = 0; j < edg1.getNumNode(); j++) {
                    if (edg1.getEdges()[entity1[i]][j].edgeType > 0) {//i->j
                        des1[desnum1] = edg1.getNodes()[j];
                        desnum1++;
                    }
                }
                for (int j = 0; j < edg2.getNumNode(); j++) {
                    if (edg2.getEdges()[entity2[m[i]]][j].edgeType > 0) {//m[i]->j
                        des2[desnum2] = edg2.getNodes()[j];
                        desnum2++;
                    }
                }
                //totaldesnum1+=desnum1;
                //totaldesnum2+=desnum2;
                int[] mm = new int[Math.max(desnum1, desnum2)];
                for (int j = 0; j < Math.max(desnum1, desnum2); j++) {
                    mm[j] = j;
                }
                int[] mmaxm = new int[Math.max(desnum1, desnum2)];
                for (int j = 0; j < Math.max(desnum1, desnum2); j++) {
                    mmaxm[j] = j;
                }
                if (desnum1 >= desnum2) {
                    result += ApproxMatch3_fun(mm, 0, des1, des2, desnum1, desnum2, mmaxm, ApproxMatch3_EstimateDegree(mmaxm, des1, des2, desnum1, desnum2));
                } else {
                    result += ApproxMatch3_fun(mm, 0, des2, des1, desnum2, desnum1, mmaxm, ApproxMatch3_EstimateDegree(mmaxm, des2, des1, desnum2, desnum1));
                }

            }
        }
        return result / (double) entitynum1;
    }

    public static String ApproxMatch1_fun(int i1, int i2, int[] d1, int[] d2, EDG edg1, EDG edg2) {
        int edgen1 = 0, edgen2 = 0;
        for (int j = 0; j < edg1.getNumNode(); j++) {
            if (edg1.getEdges()[i1][j].edgeType > 0) {
                edgen1++;
            }
            if (edg2.getEdges()[i2][j].edgeType > 0) {
                edgen2++;
            }
        }
        if (edgen1 != edgen2) {
            return "Node" + i1 + " the number of edges is not matched";
        }
        for (int j1 = 0; j1 < edg1.getNumNode(); j1++) {
            if (edg1.getEdges()[i1][j1].edgeType > 0 && d1[j1] == 0) {
                int ok1 = 0;
                int j2;
                for (j2 = 0; j2 < edg2.getNumNode(); j2++) {
                    if (edg1.getNodes()[j1].getNodeType() != edg2.getNodes()[j2].getNodeType()) {
                        continue;
                    } // the same node type
                    if (edg2.getEdges()[i2][j2].edgeType == edg1.getEdges()[i1][j1].edgeType && d2[j2] == 0) {
                        // the same edge type
                        d1[j1] = 1;
                        d2[j2] = 1; // assume j1 is matched with j2
                        String t = ApproxMatch1_fun(j1, j2, d1, d2, edg1, edg2);
                        if (!t.equals("match")) {
                            //System.out.println(t);
                            d1[i1] = 0;
                            d2[i2] = 0;
                        } else {
                            ok1 = 1;
                            break;
                        }
                    }
                }
                if (ok1 == 0) { // no match with j1
                    return "Node" + i1 + " no matching node";
                }
            }
        }
        return "match";
    }

    public static String ExactMatch_fun(int i1, int i2, int[] d1, int[] d2, EDG edg1, EDG edg2) {
        Node node1 = edg1.getNodes()[i1];
        Node node2 = edg2.getNodes()[i2];
        switch (node1.getNodeType()) {
            case 1: {
                if (node1.getQueryType() != node2.getQueryType()) {
                    return "Node" + i1 + "_QuestionType not matched";
                }
                if (node1.getTrigger() == null || node2.getTrigger() == null) {
                    if (node1.getTrigger() != null || node2.getTrigger() != null) {
                        return "Node" + i1 + "_Trigger not matched";
                    }
                } else if (!node1.getTrigger().toLowerCase().equals(node2.getTrigger().toLowerCase())) {
                    System.out.println(node1.getTrigger());
                    System.out.println(node2.getTrigger());
                    return "Node" + i1 + "_Trigger not matched";
                }
                break;
            }
            case 2: {
                break;
            }
            case 3: {
                if (node1.getStart() != node2.getStart()) {
                    return "Node" + i1 + "_Start not matched";
                }
                if (node1.getEnd() != node2.getEnd()) {
                    return "Node" + i1 + "_End not matched";
                }
                break;
            }
            case 4: {
                if (node1.getStart() != node2.getStart()) {
                    return "Node" + i1 + "_Start not matched";
                }
                if (node1.getEnd() != node2.getEnd()) {
                    return "Node" + i1 + "_End not matched";
                }
                break;
            }

        }
        int edgen1 = 0, edgen2 = 0;
        for (int j = 0; j < edg1.getNumNode(); j++) {
            if (edg1.getEdges()[i1][j].edgeType > 0) {
                edgen1++;
            }
            if (edg2.getEdges()[i2][j].edgeType > 0) {
                edgen2++;
            }
        }
        if (edgen1 != edgen2) {
            return "Node" + i1 + " the number of edges is not matched";
        }
        for (int j1 = 0; j1 < edg1.getNumNode(); j1++) {
            if (edg1.getEdges()[i1][j1].edgeType > 0 && d1[j1] == 0) {
                int ok1 = 0;
                int j2;
                for (j2 = 0; j2 < edg2.getNumNode(); j2++) {
                    if (edg1.getNodes()[j1].getNodeType() != edg2.getNodes()[j2].getNodeType()) {
                        continue;
                    } // the same node type

                    if (edg2.getEdges()[i2][j2].edgeType == edg1.getEdges()[i1][j1].edgeType && d2[j2] == 0) {
                        // the same edge type
                        int ok2 = 1;
                        switch (edg1.getEdges()[i1][j1].edgeType) { // the detail information on edge
                            case 1: {
                                break;
                            }
                            case 2: {
                                if (edg1.getEdges()[i1][j1].getStart() != edg2.getEdges()[i2][j2].getStart()) {
                                    ok2 = 0;
                                }
                                if (edg1.getEdges()[i1][j1].getEnd() != edg2.getEdges()[i2][j2].getEnd()) {
                                    ok2 = 0;
                                }
                                break;
                            }
                            case 3: {

                                if (edg1.getEdges()[i1][j1].info != null || edg2.getEdges()[i2][j2].info != null) {
                                    if (edg1.getEdges()[i1][j1].info == null || edg2.getEdges()[i2][j2].info == null) {
                                        //ok2=0;
                                    } else if (!edg1.getEdges()[i1][j1].info.equals(edg2.getEdges()[i2][j2].info)) {
                                        //ok2=0;
                                    }
                                }
                                break;
                            }
                        }
                        if (ok2 == 0) { //j1 is not matched with j2
                            continue;
                        }
                        // assume j1 is matched with j2
                        d1[j1] = 1;
                        d2[j2] = 1;
                        String t = ExactMatch_fun(j1, j2, d1, d2, edg1, edg2);
                        if (!t.equals("match")) {
                            d1[i1] = 0;
                            d2[i2] = 0;
                        } else {
                            ok1 = 1;
                            break;
                        }
                    }
                }
                if (ok1 == 0) { // j1 no matching node
                    return "Node" + i1 + " no matching node";
                }
            }
        }
        return "match";
    }

    public static double ApproxMatch2_EstimateDegree(int[] m, EDG edg1, EDG edg2) {
        double[] cn = new double[edg1.getNumNode()];
        double[] ce = new double[edg1.getNumNode() * 2];
        int edgenum = -1;
        double result1 = 0, result2 = 0;
        for (int i = 0; i < edg1.getNumNode(); i++) {
            Node node1 = edg1.getNodes()[i];
            Node node2 = edg2.getNodes()[m[i]];
            cn[i] = 1;  // measure the degree of information matching in the node itself
            if (node1.getNodeType() == 1) {
                if (node1.getQueryType() != node2.getQueryType()) {
                    cn[i] -= 0.5;
                    //System.out.println(node1.getQuestionType());
                }
                if (node1.getTrigger() == null || node2.getTrigger() == null) {
                    if (node1.getTrigger() != null || node2.getTrigger() != null) {
                        cn[i] -= 0.5;
                    }
                } else if (!node1.getTrigger().toLowerCase().equals(node2.getTrigger().toLowerCase())) {
                    cn[i] -= 0.5;
                }
            } else if (node1.getNodeType() == 3 || node1.getNodeType() == 4) {
                if (node1.getStart() != node2.getStart() || node1.getEnd() != node2.getEnd()) {
                    double t1 = 0, t2 = 0;
                    for (int k = Math.min(node1.getStart(), node2.getStart()); k < Math.max(node1.getEnd(), node2.getEnd()); k++) {
                        if (k >= node1.getStart() && k < node1.getEnd() && k >= node2.getStart() && k < node2.getEnd()) {
                            t1++;
                        }
                        t2++;
                    }
                    cn[i] = t1 / t2;
                }
            }
            result1 += cn[i];
            for (int j = 0; j < edg1.getNumNode(); j++) {
                Edge edge1 = edg1.getEdges()[i][j];
                Edge edge2 = edg2.getEdges()[m[i]][m[j]];
                if (edge1.edgeType > 0) {
                    edgenum++;
                    ce[edgenum] = 1;
                    if (edge1.edgeType == 2) {
                        if (edge1.getStart() != edge2.getStart() || edge1.getEnd() != edge2.getEnd()) {
                            double t1 = 0, t2 = 0;
                            for (int k = Math.min(edge1.getStart(), edge2.getStart()); k < Math.max(edge1.getEnd(), edge2.getEnd()); k++) {
                                if (k >= edge1.getStart() && k < edge1.getEnd() && k >= edge2.getStart() && k < edge2.getEnd()) {
                                    t1++;
                                }
                                t2++;
                            }
                            ce[edgenum] = t1 / t2;
                        }
                    } else if (edge1.edgeType == 3) {
                    }
                    result2 += ce[edgenum];
                }
            }
        }
        double result = 0.5 * result1 / (double) edg1.getNumNode() + 0.5 * result2 / (double) (edgenum + 1);
        return result;

    }

    public static boolean IsApproxMatch1(int[] m, EDG edg1, EDG edg2) {
        if (edg1.getNumNode() != edg2.getNumNode()) {
            return false;
        }
        for (int i = 0; i < edg1.getNumNode(); i++) {
            Node node1 = edg1.getNodes()[i];
            Node node2 = edg2.getNodes()[m[i]];
            if (node1.getNodeType() != node2.getNodeType()) {
                return false;
            }
            for (int j = 0; j < edg1.getNumNode(); j++) {
                if (edg1.getEdges()[i][j] != edg2.getEdges()[m[i]][m[j]]) {
                    return false;
                }
            }
        }
        return true;
    }

    public static double ApproxMatch2_fun(int[] m, int n, EDG edg1, EDG edg2, int[] maxm, double max) {
        if (n == edg1.getNumNode() - 1) {
            if (IsApproxMatch1(m, edg1, edg2)) {
                double t = ApproxMatch2_EstimateDegree(m, edg1, edg2);
                if (t > max) {
                    return t;
                } else {
                    return max;
                }
            } else {
                return max;
            }
        }
        for (int i = n; i < edg1.getNumNode(); i++) {
            int temp = m[n];
            m[n] = m[i];
            m[i] = temp;
            max = ApproxMatch2_fun(m, n + 1, edg1, edg2, maxm, max);
            temp = m[n];
            m[n] = m[i];
            m[i] = temp;
        }
        return max;
    }

    public static double ApproxMatch5_fun1(int pre1, int pre2, EDG edg1, EDG edg2, int[] M, int x) {
        int oknum = 0;
        for (int i = 0; i < edg1.getNumNode(); i++) {
            if (M[i] < edg2.getNumNode()) {
                oknum++;
            }
        }
        if (oknum == edg2.getNumNode()) {
            return ApproxMatch5_EstimateDegree(M, edg1, edg2, x);
        }
        int num1 = 0, num2 = 0;
        int[] node1 = new int[edg1.getNumNode()];
        int[] node2 = new int[edg2.getNumNode()];
        for (int i = 0; i < edg1.getNumNode(); i++) {
            if (edg1.getNodes()[pre1].getNodeType() == 2) {
                if (edg1.getEdges()[pre1][i].edgeType == 3 || edg1.getEdges()[pre1][i].edgeType == 4) {
                    node1[num1] = i;
                    num1++;
                }
            } else {
                if (edg1.getEdges()[pre1][i].edgeType == 2 || edg1.getEdges()[pre1][i].edgeType == 1) {
                    node1[num1] = i;
                    num1++;
                }
            }
        }
        for (int i = 0; i < edg2.getNumNode(); i++) {
            if (edg2.getNodes()[pre2].getNodeType() == 2) {
                if (edg2.getEdges()[pre2][i].edgeType == 3 || edg2.getEdges()[pre2][i].edgeType == 4) {
                    node2[num2] = i;
                    num2++;
                }
            } else {
                if (edg2.getEdges()[pre2][i].edgeType == 2 || edg2.getEdges()[pre2][i].edgeType == 1) {
                    node2[num2] = i;
                    num2++;
                }
            }
        }
        int[] m = new int[Math.max(num1, num2)];
        int[] maxm = new int[Math.max(num1, num2)];
        for (int i = 0; i < Math.max(num1, num2); i++) {
            m[i] = i;
            maxm[i] = i;
        }
        int flag = 1;
        if (num1 >= num2) {
            for (int i = 0; i < num2; i++) {
                M[node1[i]] = node2[i];
            }
            return ApproxMatch5_fun2(m, 0, node1, node2, num1, num2, maxm, edg1, edg2, 0, M, flag, x);
        } else {
            flag = 0;
            for (int i = 0; i < num2; i++) {
                M[node1[m[i]]] = node2[i];
            }
            return ApproxMatch5_fun2(m, 0, node2, node1, num2, num1, maxm, edg2, edg1, 0, M, flag, x);
        }
    }

    public static double ApproxMatch5_fun2(int[] m, int n, int[] node1, int[] node2, int num1, int num2, int[] maxm, EDG edg1, EDG edg2, double max, int[] M, int flag, int x) {
        if (n == num1 - 1) {
            for (int i = 0; i < num1; i++) {
                if (m[i] < num2) {
                    double t;
                    if (flag == 1) {
                        t = ApproxMatch5_fun1(node1[i], node2[m[i]], edg1, edg2, M, x);
                    } else {
                        t = ApproxMatch5_fun1(node2[i], node1[m[i]], edg2, edg1, M, x);
                    }
                    if (t > max) {
                        return t;
                    }
                }

            }
            return max;
        }
        for (int i = n; i < num1; i++) {
            int temp = m[n];
            m[n] = m[i];
            m[i] = temp;
            if (flag == 1) {
                M[node1[i]] = node2[m[i]];
                M[node1[n]] = node2[m[n]];
            } else {
                M[node2[i]] = node1[m[i]];
                M[node2[n]] = node1[m[n]];
            }
            max = ApproxMatch5_fun2(m, n + 1, node1, node2, num1, num2, maxm, edg1, edg2, max, M, flag, x);
            temp = m[n];
            m[n] = m[i];
            m[i] = temp;
            if (flag == 1) {
                M[node1[i]] = node2[m[i]];
                M[node1[n]] = node2[m[n]];
            } else {
                M[node2[i]] = node1[m[i]];
                M[node2[n]] = node1[m[n]];
            }
        }
        return max;
    }

    public static double ApproxMatch5_EstimateDegree(int[] m, EDG edg1, EDG edg2, int x) {
        double cn = 0, ce = 0;
        int edgenum = 0;
        for (int i = 0; i < edg1.getNumNode(); i++) {
            if (m[i] < edg2.getNumNode()) {
                {
                    cn += EstimateNodeSimilarity(edg1.getNodes()[i], edg2.getNodes()[m[i]], x);
                }
            }
            for (int j = 0; j < edg1.getNumNode(); j++) {
                if (edg1.getEdges()[i][j].edgeType > 0) {
                    edgenum++;
                    if (m[i] < edg2.getNumNode() && m[j] < edg2.getNumNode() && edg2.getEdges()[m[i]][m[j]].edgeType > 0) {
                        ce += EstimateEdgeSimilarity(edg1.getEdges()[i][j], edg2.getEdges()[m[i]][m[j]], x);
                    }
                }
            }
        }
        return cn / 2 / (double) edg1.getNumNode() + ce / 2 / (double) edgenum;
    }

    public static double EstimateNodeSimilarity(Node node1, Node node2, int x) {
        double result = 1;
        if (node1.getNodeType() != node2.getNodeType()) {
            return 0;
        }
        if (x == 0) {
            return result;
        }
        if (node1.getNodeType() == 1) {//root
            if (x == 1) {
                if (node1.getQueryType() != node2.getQueryType()) {
                    result -= 0.5;
                }
                if (node1.getTrigger() == null || node2.getTrigger() == null) {
                    if (node1.getTrigger() != null || node2.getTrigger() != null) {
                        result -= 0.5;
                    }
                } else if (!node1.getTrigger().toLowerCase().equals(node2.getTrigger().toLowerCase())) {
                    String[] s1 = node1.getTrigger().toLowerCase().split(" ");
                    String[] s2 = node2.getTrigger().toLowerCase().split(" ");
                    result -= 0.5 * (1 - MaxTokenMatch(s1, s2));
                }
            }
        } else if (node1.getNodeType() == 2) {//entity
        } else {//des
            result = Math.max(0, (Math.min(node1.getEnd(), node2.getEnd()) - Math.max(node1.getStart(), node2.getStart())) / (Math.max(node1.getEnd(), node2.getEnd()) - Math.min(node1.getStart(), node2.getStart())));
        }
        return result;
    }

    public static double EstimateEdgeSimilarity(Edge edge1, Edge edge2, int x) {
        double result = 1;
        if (edge1.edgeType != edge2.edgeType) {
            return 0;
        }
        if (x == 0) {
            return result;
        }
        if (edge1.edgeType == 1) {
        } else if (edge1.edgeType == 2) {
            result = Math.max(0, (Math.min(edge1.getEnd(), edge2.getEnd()) - Math.max(edge1.getStart(), edge2.getStart())) / (Math.max(edge1.getEnd(), edge2.getEnd()) - Math.min(edge1.getStart(), edge2.getStart())));
        } else if (edge1.edgeType == 3) {
            if (x == 1) {

            }
        }
        return result;
    }

    public static double[] ApproxMatch(EDG edg1, EDG edg2) {
        double[] result = new double[3];
        EDG g1, g2;//g1.nodenum>=g2.nodenum
        if (edg1.getNumNode() >= edg2.getNumNode()) {
            g1 = edg1;
            g2 = edg2;
        } else {
            g1 = edg2;
            g2 = edg1;
        }
        int root1 = 0, root2 = 0;
        while (root1 < g1.getNumNode()) {
            if (g1.getNodes()[root1].getNodeType() == 1) {
                break;
            }
            root1++;
        }
        while (root2 < g2.getNumNode()) {
            if (g2.getNodes()[root2].getNodeType() == 1) {
                break;
            }
            root2++;
        }
        result[0] = EstimateNodeSimilarity(g1.getNodes()[root1], g2.getNodes()[root2], 1);

        int[] refer1i = new int[g1.getNumNode()], refer1j = new int[g1.getNumNode()], refer2i = new int[g1.getNumNode()], refer2j = new int[g1.getNumNode()];
        int refernum1 = 0, refernum2 = 0;
        for (int i = 0; i < g1.getNumNode(); i++) {
            for (int j = 0; j < g1.getNumNode(); j++) {
                if (g1.getEdges()[i][j].edgeType == 2) {
                    refer1i[refernum1] = i;
                    refer1j[refernum1] = j;
                    refernum1++;
                }
            }
        }
        for (int i = 0; i < g2.getNumNode(); i++) {
            for (int j = 0; j < g2.getNumNode(); j++) {
                if (g2.getEdges()[i][j].edgeType == 2) {
                    refer2i[refernum2] = i;
                    refer2j[refernum2] = j;
                    refernum2++;
                }
            }
        }
        int[] mr = new int[g1.getNumNode()], maxmr = new int[g1.getNumNode()];
        for (int i = 0; i < g1.getNumNode(); i++) {
            mr[i] = i;
            maxmr[i] = i;
        }
        int flag = 1;
        if (refernum1 >= refernum2) {
            result[1] = EstimateReferEdgesSimilarity(mr, 0, refer1i, refer1j, refer2i, refer2j, refernum1, refernum2, g1, g2, 0, maxmr);
            //for(int i=0;i<refernum1;i++){System.out.println(refer1j[i]+" "+refer2j[maxmr[i]]);}
            flag = 0;
        } else {
            result[1] = EstimateReferEdgesSimilarity(mr, 0, refer2i, refer2j, refer1i, refer1j, refernum2, refernum1, g2, g1, 0, maxmr);
        }


        result[2] = 0;
        int[] M = new int[g1.getNumNode()];
        for (int i = 0; i < g1.getNumNode(); i++) {
            M[i] = g2.getNumNode();
        }
        int[] entity1 = new int[g1.getNumNode()], entity2 = new int[g1.getNumNode()];
        int entitynum1 = 0, entitynum2 = 0;
        if (flag == 0) {
            for (int i = 0; i < refernum1; i++) {
                if (maxmr[i] < refernum2) {
                    M[refer1i[i]] = refer2i[maxmr[i]];
                    M[refer1j[i]] = refer2j[maxmr[i]];
                }
            }
            for (int i = 0; i < refernum1; i++) {
                if (maxmr[i] < refernum2) {
                    result[2] += EstimateEntityBlockSimilarity(refer1j[i], refer2j[maxmr[i]], M, g1, g2);
                }
            }

            for (int i = 0; i < g1.getNumNode(); i++) {
                if (M[i] >= g2.getNumNode() && g1.getNodes()[i].getNodeType() == 2) {
                    entity1[entitynum1] = i;
                    entitynum1++;
                }
            }
            for (int i = 0; i < g2.getNumNode(); i++) {
                if (g2.getNodes()[i].getNodeType() == 2) {
                    int ok = 1;
                    for (int j = 0; j < g1.getNumNode(); j++) {
                        if (M[j] == i) {
                            ok = 0;
                            break;
                        }
                    }
                    if (ok == 1) {
                        entity2[entitynum2] = i;
                        entitynum2++;
                    }
                }
            }
            int[] m = new int[entitynum1], maxm = new int[entitynum1];
            for (int i = 0; i < entitynum1; i++) {
                m[i] = i;
                maxm[i] = i;
            }
            result[2] += ApproxMatch_fun2(m, 0, entity1, entity2, entitynum1, entitynum2, 0, maxm, M, g1, g2);
            result[2] /= (double) (refernum1 + 1);
        } else {
            for (int i = 0; i < refernum2; i++) {
                M[refer2i[i]] = refer1i[maxmr[i]];
                M[refer2j[i]] = refer1j[maxmr[i]];
            }
            for (int i = 0; i < refernum2; i++) {
                if (maxmr[i] < refernum1) {
                    result[2] += EstimateEntityBlockSimilarity(refer2j[i], refer1j[maxmr[i]], M, g2, g1);
                }
            }

            for (int i = 0; i < g1.getNumNode(); i++) {
                if (M[i] >= g2.getNumNode() && g1.getNodes()[i].getNodeType() == 2) { // entity not matched
                    entity1[entitynum1] = i;
                    entitynum1++;
                }
            }
            for (int i = 0; i < g2.getNumNode(); i++) {
                if (g2.getNodes()[i].getNodeType() == 2) {
                    int ok = 1;
                    for (int j = 0; j < g1.getNumNode(); j++) {
                        if (M[j] == i) {
                            ok = 0;
                            break;
                        }
                    }
                    if (ok == 1) {
                        entity2[entitynum2] = i;
                        entitynum2++;
                    }
                }
            }
            int[] m = new int[entitynum2], maxm = new int[entitynum2];
            for (int i = 0; i < entitynum2; i++) {
                m[i] = i;
                maxm[i] = i;
            }

            result[2] += ApproxMatch_fun2(m, 0, entity2, entity1, entitynum2, entitynum1, 0, maxm, M, g2, g1);
            result[2] /= (double) (refernum2 + 1);
        }
        if (Math.max(refernum1, refernum2) == 0) {
            result[1] = 1;
        }
        return result;
    }

    public static double ApproxMatch_fun2(int[] m, int n, int[] entity1, int[] entity2, int entitynum1, int entitynum2, double max, int[] maxm, int[] M, EDG g1, EDG g2) {
        if (n == entitynum1 - 1) {
            double t = 0;
            for (int i = 0; i < entitynum1; i++) {
                if (m[i] < entitynum2) {
                    t += EstimateEntityBlockSimilarity(entity1[i], entity2[m[i]], M, g1, g2);
                }
            }
            if (t >= max) {
                for (int i = 0; i < entitynum1; i++) {
                    maxm[i] = m[i];
                }
                return t;
            }
            return max;
        }

        for (int i = 0; i < entitynum1; i++) {
            int temp = m[i];
            m[i] = m[n];
            m[n] = temp;
            max = ApproxMatch_fun2(m, n + 1, entity1, entity2, entitynum1, entitynum2, max, maxm, M, g1, g2);
            temp = m[i];
            m[i] = m[n];
            m[n] = temp;
        }
        return max;
    }

    public static double EstimateEntityBlockSimilarity(int indexofentity1, int indexofentity2, int[] M, EDG g1, EDG g2) {
        int[] des1 = new int[g1.getNumNode()], des2 = new int[g2.getNumNode()];
        int desnum1 = 0, desnum2 = 0;
        for (int i = 0; i < g1.getNumNode(); i++) {
            if (g1.getEdges()[indexofentity1][i].edgeType == 3 || g1.getEdges()[indexofentity1][i].edgeType == 4) {
                des1[desnum1] = i;
                desnum1++;
            }
        }
        for (int i = 0; i < g2.getNumNode(); i++) {
            if (g2.getEdges()[indexofentity2][i].edgeType == 3 || g2.getEdges()[indexofentity2][i].edgeType == 4) {
                des2[desnum2] = i;
                desnum2++;
            }
        }
        int[] m = new int[Math.max(desnum1, desnum2)], maxm = new int[Math.max(desnum1, desnum2)];
        for (int i = 0; i < Math.max(desnum1, desnum2); i++) {
            m[i] = i;
            maxm[i] = i;
        }
        double result;
        if (desnum1 >= desnum2) {
            result = EstimateEntityBlockSimilarity_fun(m, 0, des1, des2, desnum1, desnum2, g1, g2, M, 0, maxm, 0);
            for (int i = 0; i < desnum1; i++) {
                if (maxm[i] < desnum2) {
                    M[des1[i]] = des2[maxm[i]];
                }
            }
        } else {
            result = EstimateEntityBlockSimilarity_fun(m, 0, des2, des1, desnum2, desnum1, g2, g1, M, 0, maxm, 1);
            for (int i = 0; i < desnum2; i++) {
                if (maxm[i] < desnum1) {
                    M[des1[maxm[i]]] = des2[i];
                }
            }
        }
        return result;
    }

    public static double EstimateEntityBlockSimilarity_fun(int[] m, int n, int[] des1, int[] des2, int desnum1, int desnum2, EDG g1, EDG g2, int[] M, double max, int[] maxm, int flag) {
        if (n == desnum1 - 1) {
            double t = 0;
            for (int i = 0; i < desnum1; i++) {
                if (m[i] < desnum2) {
                    if (flag == 0) {
                        if (M[des1[i]] < g2.getNumNode() && M[des1[i]] != des2[m[i]]) {
                            return max;
                        }
                    } else {
                        if (M[des2[m[i]]] < g1.getNumNode() && M[des2[m[i]]] != des1[i]) {
                            return max;
                        }
                    }
                    t += EstimateNodeSimilarity(g1.getNodes()[des1[i]], g2.getNodes()[des2[m[i]]], 1);
                }
            }
            t /= (double) desnum1;
            if (t > max) {
                for (int i = 0; i < desnum1; i++) {
                    maxm[i] = m[i];
                }
                return t;
            }
            return max;
        }
        for (int i = 0; i < desnum1; i++) {
            int temp = m[i];
            m[i] = m[n];
            m[n] = temp;
            max = EstimateEntityBlockSimilarity_fun(m, n + 1, des1, des2, desnum1, desnum2, g1, g2, M, max, maxm, flag);
            temp = m[i];
            m[i] = m[n];
            m[n] = temp;
        }
        return max;
    }

    public static double EstimateReferEdgesSimilarity(int[] mr, int n, int[] refer1i, int[] refer1j, int[] refer2i, int[] refer2j, int refernum1, int refernum2, EDG g1, EDG g2, double max, int[] maxmr) {
        if (n == refernum1 - 1) {
            double result = 0;
            for (int i = 0; i < refernum1; i++) {
                if (mr[i] < refernum2) {
                    result += EstimateEdgeSimilarity(g1.getEdges()[refer1i[i]][refer1j[i]], g2.getEdges()[refer2i[mr[i]]][refer2j[mr[i]]], 1);
                }
            }
            result /= refernum1;
            if (result > max) {
                for (int i = 0; i < refernum1; i++) {
                    maxmr[i] = mr[i];
                }
                return result;
            }
            return max;
        }
        for (int i = 0; i < refernum1; i++) {
            int temp = mr[i];
            mr[i] = mr[n];
            mr[n] = temp;
            max = EstimateReferEdgesSimilarity(mr, n + 1, refer1i, refer1j, refer2i, refer2j, refernum1, refernum2, g1, g2, max, maxmr);
            temp = mr[i];
            mr[i] = mr[n];
            mr[n] = temp;
        }
        return max;
    }

    public static double ApproxMatchDesStartEnd(EDG g1, EDG g2) {
        int[] des1 = new int[g1.getNumNode()], des2 = new int[g2.getNumNode()];
        int desnum1 = 0, desnum2 = 0;
        for (int i = 0; i < g1.getNumNode(); i++) {
            if (g1.getNodes()[i].getNodeType() == 3 || g1.getNodes()[i].getNodeType() == 4) {
                des1[desnum1] = i;
                desnum1++;
            }
        }
        for (int i = 0; i < g2.getNumNode(); i++) {
            if (g2.getNodes()[i].getNodeType() == 3 || g2.getNodes()[i].getNodeType() == 4) {
                des2[desnum2] = i;
                desnum2++;
            }
        }
        int[] m = new int[Math.max(desnum1, desnum2)], maxm = new int[Math.max(desnum1, desnum2)];
        for (int i = 0; i < Math.max(desnum1, desnum2); i++) {
            m[i] = i;
            maxm[i] = i;
        }
        if (desnum1 >= desnum2) {
            return ApproxMatchDesStartEnd_fun(m, 0, des1, des2, desnum1, desnum2, g1, g2, 0, maxm);
        } else {
            return ApproxMatchDesStartEnd_fun(m, 0, des2, des1, desnum2, desnum1, g2, g1, 0, maxm);
        }
    }

    public static double ApproxMatchDesStartEnd_fun(int[] m, int n, int[] des1, int[] des2, int desnum1, int desnum2, EDG g1, EDG g2, double max, int[] maxm) {
        if (n == desnum1 - 1) {
            double t = 0;
            for (int i = 0; i < desnum1; i++) {
                if (m[i] < desnum2) {
                    t += EstimateNodeSimilarity(g1.getNodes()[des1[i]], g2.getNodes()[des2[m[i]]], 1);
                }
            }
            t /= desnum1;
            if (t >= max) {
                for (int i = 0; i < desnum1; i++) {
                    maxm[i] = m[i];
                }
                return t;
            }
            return max;
        }
        for (int i = 0; i < desnum1; i++) {
            int temp = m[i];
            m[i] = m[n];
            m[n] = temp;
            max = ApproxMatchDesStartEnd_fun(m, n + 1, des1, des2, desnum1, desnum2, g1, g2, max, maxm);
            temp = m[i];
            m[i] = m[n];
            m[n] = temp;
        }
        return max;
    }
}