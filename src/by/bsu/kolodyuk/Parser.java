package by.bsu.kolodyuk;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    private final static Pattern NUM_VERTEX = Pattern.compile("/\\*\\|I\\|\\*/(\\d+)");
    private final static Pattern NUM_EDGE = Pattern.compile("/\\*\\|U\\|\\*/(\\d+)");
    private final static Pattern EDGE = Pattern.compile("\\{(\\d),(\\d)\\}");
    private final static Pattern NUM_THREAD = Pattern.compile("/\\*\\|K\\|\\*/(\\d+)");
    private final static Pattern EDGE_PER_THREAD = Pattern.compile("/\\*widetilde\\{U\\}\\^(\\d+)\\*/");
    private final static Pattern EDGE_AND_VALUE = Pattern.compile("/\\*\\{(\\d+),(\\d+)\\}\\*/(\\d+)");
    private final static Pattern VERTEX_LIMIT = Pattern.compile("/\\*widetilde\\{U\\}\\_0\\^(\\d+)\\*/");
    private final static Pattern NUM_EQUATION = Pattern.compile("/\\*q\\*/(\\d+)");
    private final static Pattern COEFFICIENT = Pattern.compile("/\\*lambda\\_(\\d{2})\\^(\\d{2})\\*/(\\d+)");
    private final static Pattern VERTEX_LIMIT_VALUE = Pattern.compile("/\\*z_(\\d{2})\\^0\\*/(\\d+)");
    private final static Pattern VERTEX_VALUE = Pattern.compile("/\\*a\\_(\\d+)\\^(\\d+)\\*/(-?\\d+)");
    private final static Pattern EQUATION_VALUE = Pattern.compile("/\\*alpha\\_(\\d+)\\*/(\\d+)");

    public static void main(String... args) throws IOException {

        int[][][] edges = null;
        int[][][] vertexEquation = null;
        int[][] vertex = null;
        int[][][] equations = null;
        int[] resEquations = null;
        ArrayList<String> edgeList = new ArrayList<String>();
        int numV = 0, numE = 0, numK = 0, numEq = 0;
        int currentThread = -1, currentThread2 = -1;

        // Parse Input
        Scanner scanner = new Scanner(new FileReader("input.txt"));
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            Matcher m = NUM_VERTEX.matcher(line);
            if (m.find()) {
                numV = Integer.valueOf(m.group(1));
                continue;
            }
            m = NUM_EDGE.matcher(line);
            if (m.find()) {
                numE = Integer.valueOf(m.group(1));
            }
            m = NUM_THREAD.matcher(line);
            if (m.find()) {
                numK = Integer.valueOf(m.group(1));
                edges = new int[numV][numV][numK];
                vertexEquation = new int[numV][numV][numK + 1];
                vertex = new int[numV][numK];
                continue;
            }
            m = EDGE_PER_THREAD.matcher(line);
            if (m.find()) {
                currentThread = Integer.valueOf(m.group(1)) - 1;
                continue;
            }
            m = EDGE_AND_VALUE.matcher(line);
            if (m.find()) {
                if (Integer.valueOf(m.group(3)) != 0) {
                    if (currentThread2 != -1) {
                        vertexEquation[Integer.valueOf(m.group(1)) - 1][Integer.valueOf(m.group(2)) - 1][currentThread2] =
                                Integer.valueOf(m.group(3));
                    }
                    edges[Integer.valueOf(m.group(1)) - 1][Integer.valueOf(m.group(2)) - 1][currentThread] = Integer.valueOf(m.group(3));
                }
                continue;
            }
            m = VERTEX_LIMIT.matcher(line);
            if (m.find()) {
                currentThread2 = Integer.valueOf(m.group(1)) - 1;
                continue;
            }
            m = NUM_EQUATION.matcher(line);
            if (m.find()) {
                numEq = Integer.valueOf(m.group(1));
                equations = new int[numE][numK][numEq];
                resEquations = new int[numEq];
                continue;
            }
            m = COEFFICIENT.matcher(line);
            if (m.find()) {
                int tmp = edgeList.indexOf(m.group(1));
                int k = Integer.valueOf(m.group(2)) / 10;
                equations[tmp][k - 1][(Integer.valueOf(m.group(2)) % 10) - 1] = Integer.valueOf(m.group(3));
                continue;
            }
            m = VERTEX_LIMIT_VALUE.matcher(line);
            if (m.find()) {
                int tmp = Integer.valueOf(m.group(1));
                vertexEquation[(tmp / 10) - 1][(tmp % 10) - 1][numK] = Integer.valueOf(m.group(2));
                continue;
            }
            m = VERTEX_VALUE.matcher(line);
            if (m.find()) {
                vertex[Integer.valueOf(m.group(1)) - 1][Integer.valueOf(m.group(2)) - 1] = Integer.valueOf(m.group(3));
                continue;
            }
            m = EDGE.matcher(line);
            if (m.find()) {
                edgeList.add(m.group(1) + m.group(2));
                continue;
            }
            m = EQUATION_VALUE.matcher(line);
            if (m.find()) {
                resEquations[Integer.valueOf(m.group(1)) - 1] = Integer.valueOf(m.group(2));
            }
        }

        // Write Output
        BufferedWriter bw = new BufferedWriter(new FileWriter("output.tex"));
        bw.write("\\documentclass{article}\n" +
                "\\usepackage{amssymb,amsmath}\n" +
                "\\usepackage[english,russian]{babel}\n" +
                "\\usepackage{graphicx}\n" +
                "\\usepackage{multirow}\n" +
                "\\begin{document}\n" +
                "\\begin{center}\n");
        for (int k = 0; k < numK; k++) {
            for (int i = 0; i < numV; i++) {
                StringBuilder str = new StringBuilder();
                for (int j = 0; j < numV; j++) {
                    if (edges[i][j][k] == 1) {
                        if (str.length() == 0) {
                            str.append("x_{" + (i + 1) + "," + (j + 1) + "}^{" + (k + 1) + "} ");
                        } else {
                            str.append("+ x_{" + (i + 1) + "," + (j + 1) + "}^{" + (k + 1) + "} ");
                        }
                    }
                }
                for (int j = 0; j < numV; j++) {
                    if (edges[j][i][k] == 1) {
                        str.append("- x_{" + (j + 1) + "," + (i + 1) + "}^{" + (k + 1) + "} ");
                    }
                }
                str.append("= " + vertex[i][k]);
                bw.write("$" + str + "$\\" + "\\" + "\n");
            }
            bw.write("\\bigskip\n");
        }

        for (int k = 0; k < numEq; k++) {
            StringBuilder str = new StringBuilder();
            for (int i = 0; i < numE; i++) {
                for (int j = 0; j < numK; j++) {
                    if (equations[i][j][k] != 0) {
                        if (str.length() == 0) {
                            str.append((equations[i][j][k] == 1 ? "" : equations[i][j][k]) + "x_{" + edgeList.get(i).charAt(0) + "," + edgeList.get(i).charAt(1) + "}^{" + (j + 1) + "} ");
                        } else {
                            str.append("+" + (equations[i][j][k] == 1 ? "" : equations[i][j][k]) + "x_{" + edgeList.get(i).charAt(0) + "," + edgeList.get(i).charAt(1) + "}^{" + (j + 1) + "} ");
                        }
                    }
                }
            }
            str.append("= " + resEquations[k]);
            bw.write("$" + str + "$\\\\\n");
            bw.write("\\bigskip\n");
        }

        for (int i = 0; i < numV; i++) {
            for (int j = 0; j < numV; j++) {
                if (vertexEquation[i][j][numK] != 0) {
                    StringBuilder str = new StringBuilder();
                    for (int k = 0; k < numK; k++) {
                        if (vertexEquation[i][j][k] != 0) {
                            if (str.length() == 0) {
                                str.append("x_{" + (i + 1) + "," + (j + 1) + "}^{" + (k + 1) + "} ");
                            } else {
                                str.append("+ x_{" + (i + 1) + "," + (j + 1) + "}^{" + (k + 1) + "} ");
                            }
                        }
                    }
                    str.append("= " + vertexEquation[i][j][numK]);
                    bw.write("$" + str + "$\\\\\n");
                }
            }
        }
        bw.write("\\bigskip\n");
        bw.write("\\begin{tabular}{|c|c||c|c|c||c|c|c|}\n");
        bw.write("\\hline\n");
        bw.write("$(i, j)$ & $k$ & $U^{k}$ & $U_{1}^{k}$ & $U_{0}$ & $K(i,j)$ & $K_{1}(i,j)$ & $K_{0}(i,j)$ \\\\\n");
        bw.write("\\hline\n");
        for (int i = 0; i < numE; i++) {
            bw.write("\\multirow{" + numK + "}{*}\n");
            String tmpEdge = "(" + edgeList.get(i).charAt(0) + ", " + edgeList.get(i).charAt(1) + ")";
            int num1 = Integer.parseInt(String.valueOf(edgeList.get(i).charAt(0)));
            int num2 = Integer.parseInt(String.valueOf(edgeList.get(i).charAt(1)));
            String listForUk[] = new String[4];
            String stringForK = "";
            String stringForK0 = "";
            bw.write("{$" + tmpEdge + "$}");
            for (int j = 0; j < numK; j++) {
                if (edges[num1 - 1][num2 - 1][j] == 1) {
                    if (j == 0) {
                        listForUk[j] = "& $1$ & + &  & ";
                    } else {
                        listForUk[j] = "& $" + (j + 1) + "$ & + &  &  &  &  & ";
                    }
                    stringForK += (j + 1) + ",";
                } else {
                    if (j == 0) {
                        listForUk[j] = "& $1$ & &  & ";
                    } else {
                        listForUk[j] = "& $" + (j + 1) + "$ & &  &  &  &  & ";
                    }
                }
                if (vertexEquation[num1 - 1][num2 - 1][j] == 1) {
                    stringForK0 += (j + 1) + ",";
                }
            }
            bw.write(listForUk[0]);
            if (stringForK0 != "") {
                bw.write("\\multirow{" + numK + "}{*}{+} & ");
            } else
                bw.write("\\multirow{" + numK + "}{*}{} & ");

            stringForK = stringForK.substring(0, stringForK.length() - 1);
            bw.write("\\multirow{" + numK + "}{*}{\\{" + stringForK + "\\}} & ");

            bw.write("\\multirow{" + numK + "}{*}{$\\varnothing$} & ");

            if (stringForK0 != "") {
                stringForK0 = stringForK0.substring(0, stringForK0.length() - 1);
                bw.write("\\multirow{" + numK + "}{*}{\\{" + stringForK0 + "\\}} \\\\\n");
            } else
                bw.write("\\multirow{" + numK + "}{*}{} \\\\\n");

            for (int j = 1; j < numK; j++) {
                bw.write("\\cline{2-4}\n");
                bw.write(listForUk[j] + " \\\\\n");
            }
            bw.write("\\cline{1-8}\n");
        }

        bw.write("\\end{tabular}\n");
        bw.write("\\\\\n");
        bw.write("\\bigskip\n");

        bw.write("\\includegraphics[width=1\\linewidth]{graph}\\\\\n");
        bw.write("\\bigskip\n");
        bw.write("\\end{center}\n" +
                "\\end{document}\n");
        bw.close();
    }
}
