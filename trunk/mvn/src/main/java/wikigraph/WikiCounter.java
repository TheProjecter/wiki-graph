package wikigraph;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.hackreduce.mappers.XMLRecordReader;

public class WikiCounter extends Configured implements Tool {


    public Class<? extends Mapper> getMapper(){
        return WikiWordCountMap.class;
    };

    private Class<? extends Reducer> getReducer() {
        return WikiWordCountReducer.class;
    }

    private void configureJob(Job job) {
         XMLRecordReader.setRecordTags(job, "<page>", "</page>");
         job.setInputFormatClass(WikiInputFormat.class);
        //job.setInputFormatClass(TextInputFormat.class);
    }


    public int run(String[] args) throws Exception {
        Configuration conf = getConf();

        if (args.length != 3) {
            System.err.println("Usage: " + getClass().getName() + " <input> <output> <keyword>");
            System.exit(2);
        }

        // Creating the MapReduce job (configuration) object
        Job job = new Job(conf);
        job.getConfiguration().set("baseword",args[2]);

        job.setJarByClass(getClass());
        job.setJobName(getClass().getName());

        // Tell the job which Mapper and Reducer to use (classes defined above)
        job.setMapperClass(getMapper());
        job.setReducerClass(getReducer());

        configureJob(job);


        // This is what the Mapper will be outputting to the Reducer
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);

        // This is what the Reducer will be outputting
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(LongWritable.class);

        // Setting the input folder of the job
        FileInputFormat.addInputPath(job, new Path(args[0]));

        // Preparing the output folder by first deleting it if it exists
        Path output = new Path(args[1]);
        FileSystem.get(conf).delete(output, true);
        FileOutputFormat.setOutputPath(job, output);

        return job.waitForCompletion(true) ? 0 : 1;
    }

    public static void main(String[] args) throws Exception {

        int result = ToolRunner.run(new Configuration(), new WikiCounter(), args);
        System.exit(result);
    }

}
