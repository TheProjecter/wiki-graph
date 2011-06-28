package wikigraph;

import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class WikiWordCountReducer extends Reducer<Text,Text,Text,LongWritable>{

    private LongWritable result = new LongWritable();

    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
        try {
            long sum=0;
            for(Text value:values){
                //System.out.println("for key " + key + " value obtained as : "+value);
                String[] outputValues=value.toString().split(":");
                int distance=Integer.parseInt(outputValues[0]);
                int count=Integer.parseInt(outputValues[1]);
                sum=sum+calculateWeightedResult2(distance,count);
            }
            result.set(sum);
            context.write(key,result);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private long calculateWeightedResult(int distance, int count) {
        int weight=0;
        if(distance<=1){
            weight=10;
        }else if(distance<=2){
            weight=8;
        }else if(distance<=4){
            weight=6;
        }else if(distance<=8){
            weight=4;
        }else if(distance<=16){
            weight=2;
        }else if(distance<=32){
            weight=1;
        }

        return weight*count;
    }
    private long calculateWeightedResult2(int distance, int count) {
        int weight=0;
        if(distance<=10){
            weight=10;
        }else if(distance<=20){
            weight=8;
        }else if(distance<=30){
            weight=6;
        }else if(distance<=40){
            weight=4;
        }else if(distance<=50){
            weight=2;
        }else if(distance<=60){
            weight=1;
        }

        return weight*count;
    }
}
