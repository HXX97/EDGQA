package cn.edu.nju.ws.edgqa.utils;

import cn.edu.nju.ws.edgqa.domain.beans.TreeNode;
import cn.edu.nju.ws.edgqa.domain.edg.EDG;
import cn.edu.nju.ws.edgqa.utils.linking.DexterEntityLinking;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.time.TimeAnnotations;
import edu.stanford.nlp.time.TimeAnnotator;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class NLPUtil {

    private static final HashSet<String> entityTypes = new HashSet<>(Arrays.asList("PERSON", "LOCATION", "ORGANIZATION", "MISC"));

    /**
     * Get all the n_gram in the question, desc order by length
     *
     * @param question the natural language question
     * @return
     */
    public static Vector<String> getNgram(String question) {

        //deal word by word
        String[] tokens = question.toLowerCase().replaceAll("([\u4e00-\u9fa5])", " $1 ")
                .replaceAll("[ ]{2,}", " ").split(" "); // remove the redundant space and split it by space

        Vector<String> ngram = new Vector<String>();
        for (int i = 0; i < tokens.length; i++) {
            for (int j = 0; j <= i; j++) {
                if (i - j <= 8) {  // the maximum token length is set to 8 here
                    StringBuilder sb = new StringBuilder();
                    for (int k = j; k <= i; k++) {
                        if (k == i)
                            sb.append(tokens[k]);
                        else
                            sb.append(tokens[k]);
                    }
                    if (!ngram.contains(sb.toString()))
                        ngram.add(sb.toString());
                }
            }
        }
        //sort result by the length of element
        ngram.sort((left, right) -> (right.length() - left.length()));
        return ngram;
    }

    /**
     * return the lemmazation of a word
     *
     * @param word raw word
     * @return lemmazatio of a word
     */
    public static String getLemma(String word) {

        // do not lemmatize for 'born'
        if (word.toLowerCase().equals("born")) {
            return word;
        }

        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        Annotation doc = new Annotation(word);
        pipeline.annotate(doc);
        return doc.get(CoreAnnotations.TokensAnnotation.class).get(0).lemma();
    }

    /**
     * return the lemmazation of a sentence
     *
     * @param sent raw sentence
     * @return lemmazation of the sentence
     */
    public static String getLemmaSent(String sent) {
        ArrayList<String> tokens = getTokens(sent);
        StringBuilder sb = new StringBuilder();
        for (String token : tokens) {
            sb.append(getLemma(token)).append(" ");
        }
        return sb.toString().trim();
    }

    public static String tagNumNTime(String sentence) {

        StringBuilder sb = new StringBuilder(sentence);
        HashSet<String> timeNNumber = new HashSet<>(Arrays.asList("MONEY", "NUMBER", "ORDINAL", "PERCENT", "DATE", "TIME", "DURATION", "SET"));
        //ArrayList<Character> sent = new ArrayList<Character>(Arrays.asList(sentence.toCharArray()));
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        CoreDocument document = new CoreDocument(sentence);
        pipeline.annotate(document);
        List<Integer> indexes = new ArrayList<>();
        for (CoreEntityMention mention : document.entityMentions()) {
            if (timeNNumber.contains(mention.entityType().trim())) {
                int start = mention.charOffsets().first;
                int end = mention.charOffsets().second;
                indexes.add(start);
                indexes.add(end);
                /*sb.insert(start,'<');
                //sb.insert(end,(" "+mention.entityType()).toCharArray());
                sb.insert(end,'>');*/
            }
        }
        for (int i = 0; i < indexes.size(); i++) {
            if ((i & 1) == 0) { // i is even
                sb.insert(indexes.get(i) + i, '<');
            } else {
                sb.insert(indexes.get(i) + i, '>');
            }
        }

        //System.out.println(document.entityMentions());
        //System.out.println(document.quotes());

        //return document.entityMentions();

        return sb.toString();
    }

    public static List<String> coreNLPEntityMentions(String sentence) {
        List<String> res = new LinkedList<>();

        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
        props.setProperty("ner.applyFineGrained", "false");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        CoreDocument document = new CoreDocument(sentence);
        pipeline.annotate(document);
        List<Integer> indexes = new ArrayList<>();
        for (CoreEntityMention mention : document.entityMentions()) {
            if (entityTypes.contains(mention.entityType())) {
                res.add(mention.text());
            }
            /*System.out.println("Entity: " + mention.entity());
            System.out.println("Mention Type: " + mention.entityType());
            System.out.println("Text: " + mention.text());
            System.out.println("Tokens: " + mention.tokens());*/
        }

        return res;
    }

    public static boolean judgeIfEntity(String phase) {
        System.out.println("[DEBUG] judgeIfEntity:" + phase);

        if (phase == null || phase.equals("")) {  // prevent it's an empty string
            return false;
        }

        if (phase.matches("<e\\d>")) {
            return true;
        }

        double e = 0.7; // if len(mention) / len(phase) is greater than 0.7, it is identified as an entity

        // A dexter2 server is needed, comment it if the server is not available
        if (DexterEntityLinking.isDexterEntity(phase, e)) {
            return true;
        }

        // need set proxy in EntityLinking script，not recommend in large scale(band ip warning)
        /*if(EntityLinking.isDBpediaEntity(phase,e)){
            return true;
        }*/

        for (String mention : coreNLPEntityMentions(phase)) {
            if ((double) mention.length() / phase.length() >= e) {
                return true;
            }
        }

        //judge if it is instance after concatenate
        /*String[] tokens = phase.split(" ");
        StringBuilder sb = new StringBuilder("http://dbpedia.org/resource/");
        for (String token : tokens) {
            sb.append(token).append("_");
        }
        sb.deleteCharAt(sb.length() - 1);
        System.out.println(sb);

        if (KBUtil.isAnInstance(sb.toString())) {
            return true;
        }*/

        return false;
    }

    public static boolean judgeIfCompare(String phase) {
        HashSet<String> comparePOS = new HashSet<>(Arrays.asList("JJR", "JJS", "RBR", "RBS"));
        List<String> poses = getPOS(phase);
        for (String pos : poses) {
            if (comparePOS.contains(pos)) {
                return true;
            }
        }
        return false;
    }

    public static boolean judgeIfTime(String phase) {
        Properties props = new Properties();
        AnnotationPipeline pipeline = new AnnotationPipeline();
        pipeline.addAnnotator(new TokenizerAnnotator(false));
        pipeline.addAnnotator(new WordsToSentencesAnnotator(false));
        pipeline.addAnnotator(new POSTaggerAnnotator(false));
        pipeline.addAnnotator(new TimeAnnotator("sutime", props));

        Annotation annotation = new Annotation(phase);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String today = sdf.format(new Date());
        annotation.set(CoreAnnotations.DocDateAnnotation.class, today);
        pipeline.annotate(annotation);
        //System.out.println(annotation.get(CoreAnnotations.TextAnnotation.class));
        List<CoreMap> timexAnnsAll = annotation.get(TimeAnnotations.TimexAnnotations.class);
        List<CoreLabel> all_tokens = annotation.get(CoreAnnotations.TokensAnnotation.class);
        int length = all_tokens.size();

        if (timexAnnsAll.size() != 1) {
            return false;
        } else {
            CoreMap timexAnn = timexAnnsAll.get(0);
            List<CoreLabel> tokens = timexAnn.get(CoreAnnotations.TokensAnnotation.class);
            int start = tokens.get(0).index();
            int end = tokens.get(tokens.size() - 1).index();
            if (start == 1 && end == length) {
                return true;
            }
        }
        return false;
    }

    /**
     * input a sentence, return entity in it(use Stanford CoreNLP NER)
     *
     * @param sentence
     * @return entity in sentence
     */
    public static List<String> getEntities(String sentence) {
        //HashSet<String> notEntity = new HashSet<>(Arrays.asList("MISC", "MONEY", "NUMBER", "ORDINAL", "PERCENT", "DATE", "TIME", "DURATION", "SET"));
        HashSet<String> entitySet = new HashSet<>(Arrays.asList("PERSON", "LOCATION", "ORGANIZATION"));
        List<String> entities = new LinkedList<>();
        Properties properties = new Properties();
        properties.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
        properties.setProperty("ner.applyFineGrained", "false");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(properties);
        CoreDocument document = new CoreDocument(sentence);
        pipeline.annotate(document);

        if (document.entityMentions() != null) {
            //System.out.println(document.entityMentions());
            document.entityMentions().forEach(o -> {
                if (entitySet.contains(o.entityType())) {
                    entities.add(o.text());
                }
            });
        }

        return entities;
    }

    public static List<String> getTimeEntities(String sentence) {
        Properties properties = new Properties();
        properties.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(properties);
        Annotation document = new Annotation(sentence);
        pipeline.annotate(document);
        HashSet<String> timeEntity = new HashSet<>(Arrays.asList("DATE", "TIME", "DURATION", "SET"));
        List<CoreMap> sents = document.get(CoreAnnotations.SentencesAnnotation.class);
        List<String> entities = new LinkedList<>();
        for (CoreMap sent : sents) {
            String currentType = null;
            String entity = null;
            for (CoreLabel token : sent.get(CoreAnnotations.TokensAnnotation.class)) {
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                String ner = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                if (!ner.equals("O") && (timeEntity.contains(ner))) {
                    if (currentType == null) {
                        currentType = ner;
                        entity = word;
                    } else if (currentType.equals(ner)) {
                        entity += " " + word;
                    } else {
                        if (entity != null) {
                            entities.add(entity.trim());
                        }
                        currentType = ner;
                        entity = word;
                    }
                } else {
                    if (entity != null) {
                        entities.add(entity.trim());
                        entity = null;
                        currentType = null;
                    }
                }
            }
            if (entity != null) {
                entities.add(entity.trim());
            }
        }
        return entities;
    }

    public static List<EDG> generateSimpleEDGFromTXT(String inputFilePath) {

        File inputFile = new File(inputFilePath);
        List<EDG> EDGS = new LinkedList<>();
        try {
            Scanner scanner = new Scanner(inputFile);
            String line = null;
            while (scanner.hasNextLine()) {

                line = scanner.nextLine();
                System.out.println("Processing: " + line);
                try {
                    EDGS.add(new EDG(line));
                } catch (RuntimeException e) {
                    System.out.println("Processing Error:" + line + "\n");
                }
            }

            scanner.close();

        } catch (FileNotFoundException e) {

            e.printStackTrace();
        }
        return EDGS;

    }

    public static ArrayList<String> getTokens(String sentence) {

        //null str check
        if (sentence == null || sentence.trim().equals("")) {
            return new ArrayList<>();
        }

        Properties props = new Properties();
        props.setProperty("annotators", "tokenize");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        Annotation annotation = new Annotation(sentence);
        pipeline.annotate(annotation);


        ArrayList<String> result = new ArrayList<>();
        List<CoreLabel> coreLabels = annotation.get(CoreAnnotations.TokensAnnotation.class);

        for (CoreLabel token : coreLabels) {
            result.add(token.toString());
        }

        return result;
    }

    //return token number of a sentence
    public static int getTokenNum(String sentence) {
        return getTokens(sentence).size();
    }

    public static void addPosTag(String inputFilePath, String outputFilePath) {
        File inputFile = new File(inputFilePath);
        File outputFile = new File(outputFilePath);
        try {
            if (!inputFile.exists()) {
                System.out.println("Input File '" + inputFilePath + "' does not exist! Please check it.");
            } else {

                Properties props = new Properties();
                props.setProperty("annotators", "tokenize");
                StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
                Scanner scanner = new Scanner(inputFile);
                if (!outputFile.exists()) {
                    outputFile.createNewFile();
                }
                FileWriter fileWriter = new FileWriter(outputFile);

                String line = null;
                while (scanner.hasNextLine()) {
                    line = scanner.nextLine();
                    //create a parser, input text
                    Annotation document = new Annotation(line);
                    //parse
                    pipeline.annotate(document);

                    List<CoreLabel> tokens = document.get(CoreAnnotations.TokensAnnotation.class);
                    int count = 0;
                    for (CoreLabel token : tokens) {

                        String word = token.toString();
                        fileWriter.write(word + "[" + count + "] ");
                        System.out.print(word + "[" + count + "] ");
                        count++;
                    }
                    System.out.println();
                    fileWriter.write("\n");
                }
                scanner.close();


            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static String getSyntaxTree(String sentence) {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, parse");
        //props.setProperty("parse.model", "edu/stanford/nlp/models/srparser/englishSR.ser.gz");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        Annotation document = new Annotation(sentence);
        pipeline.annotate(document);
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        StringBuilder sb = new StringBuilder();
        for (CoreMap sent : sentences) {
            sb.append(sent.get(TreeCoreAnnotations.TreeAnnotation.class).toString());
        }
        return sb.toString();
    }

    public static String getPosNum(String sentence) {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        Annotation document = new Annotation(sentence);
        pipeline.annotate(document);
        List<CoreLabel> tokens = document.get(CoreAnnotations.TokensAnnotation.class);
        StringBuilder sb = new StringBuilder();

        int count = 0;
        for (CoreLabel token : tokens) {

            String word = token.toString();
            //fileWriter.write(word + "[" + count + "] ");
            //System.out.print(word + "[" + count + "] ");
            sb.append(word).append("[").append(count).append("] ");
            count++;
        }
        return sb.toString();

    }

    public static List<String> getPOS(String sentence) {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        Annotation annotation = new Annotation(sentence);
        pipeline.annotate(annotation);
        LinkedList<String> poses = new LinkedList<>();
        for (CoreLabel token : annotation.get(CoreAnnotations.TokensAnnotation.class)) {
            //poses.add(token.get(CoreAnnotations.PartOfSpeechAnnotation.class))
            poses.add(token.tag());
        }
        return poses;
    }

    //Judge whether a phrase is a verb phrase, return true if yes, false if not
    public static boolean judgeIfVP(String span) {
        HashSet<String> relationTags = new HashSet<>(
                Arrays.asList("VB", "VBD", "VBG", "VBN", "VBP", "VBZ", "VP", "S", "SQ", "ADJP", "ADVP"));
        String syntaxTree = NLPUtil.getSyntaxTree(span);
        TreeNode treeNode = TreeNode.createTree(syntaxTree);

        if (treeNode.getFirstChild().getData().trim().equals("FRAG")) {//If it is FRAG, go down one level
            treeNode = treeNode.getFirstChild();
        }

        if (!treeNode.children.isEmpty()) {
            TreeNode treeNode1 = treeNode.getFirstChild();
            return relationTags.contains(treeNode1.data.trim());
        }
        return false;

    }

    public static String removeRedundantHeaderAndTailer(String str) {
        String originalStr = str;
        int len = str.length();
        try {
            if (str.endsWith(" of")) {
                if (str.startsWith("be ")) {
                    str = str.substring(3, len - 3);
                } else if (str.startsWith("the ")) {
                    str = str.substring(4, len - 3);
                }
            } else if (str.endsWith(" in")) {
                if (str.startsWith("whose ") || str.startsWith("which ")) {
                    str = str.substring(6, len - 3);
                }
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            return originalStr;
        }
        return str;
    }

    public static void main(String[] args) throws IOException {

        /*        String all = "truest, lastest, smaller, samllest, deepest, Greatest, Best, greater, shortest, shorter, younger, lower, lowest, highest, clostest, kennedy, Nearest, his/her, fewest, Higher,larger, longest, tallest, least, youngest, soonest, More, Truer, Funniest, ernest, older, broader, coolest, Briefer, latest, taller, sparsest, fastest, more, bigger, smalllest, thier, fonder, widest, oldest, oliver, strongest, fitter, richest, southwest, cheaper, Richest, Most, fewer, nearest, lightest, Sweetest, faster, Bluest, biggest, earlier, Noblest, newest, Worst, less, worse, most, longer, eldest, worst, forrest, higher, largest, smallest, best, elder, hitler, Modest, hottest, lester, wealthiest, sweetest, tamer, greatest, Closer, warmest, heaviest, Cheaper, Earliest, earliest, tyler, better, wiser, richer, easier, Biggest, closest";
        HashSet<String> notCompare = new HashSet<>(
                Arrays.asList(
                        "his/her",
                        "<",
                        ">",
                        "truest",
                        "southwest",
                        "kennedy",
                        "truer",
                        "ernest",
                        "smalllest",
                        "thier",
                        "fonder",
                        "oliver",
                        "sweetest",
                        "noblest",
                        "hitler",
                        "modest",
                        "lester",
                        "tamer",
                        "tyler",
                        "wiser",
                        "briefer",
                        "funniest",
                        "worse",
                        "clostest",
                        "worst",
                        "forrest",
                        "bluest",
                        "easier",
                        "strongest",
                        "fitter",
                        "soonest"
                )
        );*/

        String question = "How many different teams have the players debuted in Houston Astros played for?";
        String syntaxTree = NLPUtil.getSyntaxTree(question);
        System.out.println(TransferParentheses(syntaxTree));

    }

    public static String TransferParentheses(String s) {
        return s.replace("(", "[").replace(")", "]");
    }
}

