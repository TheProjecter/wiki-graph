package wikigraph;

import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.xml.sax.SAXException;
import wikigraph.model.Position;
import wikigraph.model.WordIndex;
import wikigraph.util.WikiGraphUtil;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.*;

public class WikiWordCountMap extends Mapper<Object,Text,Text,Text> {

    public static String BASE_WORD="anarchism";
    public static final String STOP_WORDS="a,able,about,across,after,all,almost,also,am,among,an,and,any,are,as,at,be,because,been,but,by,can,cannot,could,dear,did,do,does,either,else,ever,every,for,from,get,got,had,has,have,he,her,hers,him,his,how,however,i,if,in,into,is,it,its,just,least,let,like,likely,may,me,might,most,must,my,neither,no,nor,not,of,off,often,on,only,or,other,our,own,rather,said,say,says,she,should,since,so,some,than,that,the,their,them,then,there,these,they,this,tis,to,too,twas,us,wants,was,we,were,what,when,where,which,while,who,whom,why,will,with,would,yet,you,your," +
            "http,html,en,em,contributions,user,talk,diff,utc,en.wikipedia.org,nowiki,contribs,span,id,page,style,newbr,edit,links,sup,nbsp,uploaded,overlap";
    public static final Set<String> stopWordsSet;

    static{
        String[] stopwordsStringArray=STOP_WORDS.split(",");
        stopWordsSet=new HashSet<String>(Arrays.asList(stopwordsStringArray));
    }
    private Text word = new Text();

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        super.setup(context);
        BASE_WORD=context.getConfiguration().get("baseword");
    }

    @Override
    protected void map(Object key, Text value, Context context) throws IOException, InterruptedException {
        if(value.toString().indexOf(BASE_WORD)==-1)
            return;

        List<WordIndex> wordPositionList = new ArrayList<WordIndex>();
        List<Position> baseWordPositionList = new ArrayList<Position>();

        String cleanedUpText = getCleanedUpText(value);
        String[] allWords=cleanedUpText.split("[\\s]+");
        int sentenceCounter=1;
        int wordCounter=0;

        for (String eachWord : allWords) {
            if(eachWord.endsWith(".") || eachWord.equals(".")){
                sentenceCounter++;
            }
            if (isStopWord(eachWord)) continue;
            wordCounter++;
            wordPositionList.add(new WordIndex(new Position(sentenceCounter,wordCounter),eachWord));
            if(BASE_WORD.equals(eachWord)){
                baseWordPositionList.add(new Position(sentenceCounter,wordCounter));
            }
        }

        doSecondSweep(wordPositionList, baseWordPositionList, context);
   }

    private void doSecondSweep(List<WordIndex> wordPositionList, List<Position> baseWordPositionList,Context context) throws IOException,InterruptedException{

        for (WordIndex wordIndex : wordPositionList) {
            int minDistance=Integer.MAX_VALUE;
            for (Position position : baseWordPositionList) {
                int newMin=Math.abs(wordIndex.position.wordNumber-position.wordNumber);
                minDistance=Math.min(minDistance,newMin);
            }
            Text word = new Text();
            word.set(wordIndex.word);
            Text output = new Text();
            output.set(minDistance+":"+1);
            context.write(word,output);
        }
    }

    private boolean isStopWord(String eachWord) {
        if((eachWord.length()==1)){
            return true;
        }
        if(stopWordsSet.contains(eachWord))
            return true;
        if(eachWord.matches("\\d+"))
            return true;
        return false;
    }

    private String getCleanedUpText(Text value) throws IOException {
        String pageText= null;
        String cleanedUpText="";
        try {
            pageText = value.toString().toLowerCase();
            String pageMarkup= WikiGraphUtil.wikiPageXmlToMarkup(pageText);
            cleanedUpText=WikiGraphUtil.wikiMarkupToText(pageMarkup);
        } catch (Exception e) {
            throw new IOException(e);
        }
        return cleanedUpText;
    }


}
