
public class Driver {

    public static void main(String[] args) throws Exception {

        UnitMultiplication multiplication = new UnitMultiplication();
        UnitSum sum = new UnitSum();

        //args0: dir of transition.txt
        //args1: dir of PageRank.txt
        //args2: dir of unitMultiplication result
        //args3: times of convergence
        String transitionMatrix = args[0]; // not change
        String prMatrix = args[1]; // change: read pr(n - 1) to output pr(n): pr(0).txt, pr(1).txt, ...
        String subPr = args[2]; // change: pr(n) to sum up
        int count = Integer.parseInt(args[3]);
        // every time input is dynamically changed, so every time we should change the input and output file name to read them and output them dynamically
        for(int i = 0; i < count; i++) {
            String[] multiplicationArgs = {transitionMatrix, prMatrix + i, subPr + i};
            multiplication.main(multiplicationArgs);
            String[] sumArgs = {subPr + i, prMatrix + (i + 1)};
            sum.main(sumArgs);
        }
    }
}
