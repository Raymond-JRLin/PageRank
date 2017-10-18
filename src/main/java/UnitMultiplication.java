import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.chain.ChainMapper;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UnitMultiplication {

    public static class TransitionMapper extends Mapper<Object, Text, Text, Text> {

        @Override
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

            //input format: fromPage\t toPage1,toPage2,toPage3
            //target: build transition matrix unit -> fromPage\t toPage=probability

            // read value
            String line = value.toString().trim();
            // split it to fromPage and toPage
            String[] val = line.split("\t");
            // dead ends
            if (val.length < 2) {
                return;
                // but industry, we cannot return directly, instead, we can choose
                // 1: throw exception, but actually it's not exception - it's not bad input, so this method is not good enouth
                // 2: write into logger for our debug, i.e.
                // logger.debug("found dead ends" + val[0]);
            }
            // get from page
            String fromPage = val[0];
            // it's output key
            String outputKey = fromPage;
            // get to pages
            String[] toPages = val[1].split(",");
            // total number of to pages
            double total = toPages.length; // it should be double type, otherwise it would be incorrect in next division operation
            // we assume the probability is equal to jump to another page
            double prob = 1 / total;
            // loop to write output value
            for (String toPage : toPages) {
                context.write(new Text(outputKey), new Text(toPage + "=" + prob));
            }

        }
    }

    public static class PRMapper extends Mapper<Object, Text, Text, Text> {

        @Override
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

            //input format: Page\t PageRank
            //target: write to reducer

            // read input
            String line = value.toString().trim();
            // split
            String[] pr = line.split("\t");
            // output
            context.write(new Text(pr[0]), new Text(pr[1]));
        }
    }

    public static class MultiplicationReducer extends Reducer<Text, Text, Text, Text> {


        @Override
        public void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {

            //input key = fromPage value=<toPage=probability..., pageRank>
            //target: get the unit multiplication

            // input
            // key: fromId
            // value: {toPage1=prob, toPage2=prob, ..., pageRank}
            // key: 1, value: {2=1/3, 5=1/3, 8=1/3, 1}
            // we say it's a list for easier understanding, but actually it's Iterable

            // get the pagerank
            double pr = 0;
            // use a list to store toPage=prob
            List<String> transitions = new ArrayList<String>();
            for (Text val : values) {
                String str = val.toString().trim();
                if (str.contains("=")) {
                    transitions.add(str);
                } else {
                    pr = Double.parseDouble(str);
                }
            }
            for (String transCell : transitions) {
                String[] toProb = transCell.split("=");
                String toPage = toProb[0];
                String outputKey = toPage;
                
                double prob = Double.parseDouble(toProb[1]);
                double subPr = prob * pr;
                String outputValue = String.valueOf(subPr);
                // output: remember subPr belongs to toPage
                // key: toPage
                // value: prob * pr = subPr
                context.write(new Text(outputKey), new Text(outputValue));
            }

        }
    }

    public static void main(String[] args) throws Exception {

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf);
        job.setJarByClass(UnitMultiplication.class);

        //how chain two mapper classes?

        job.setReducerClass(MultiplicationReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        // MultipleInputs is used to tell mapper to where to read which file, assign different mapping relation
        MultipleInputs.addInputPath(job, new Path(args[0]), TextInputFormat.class, TransitionMapper.class);
        MultipleInputs.addInputPath(job, new Path(args[1]), TextInputFormat.class, PRMapper.class);

        FileOutputFormat.setOutputPath(job, new Path(args[2]));
        job.waitForCompletion(true);
    }

}
