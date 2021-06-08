package cn.edu.nju.ws.edgqa.utils.linking;

import cn.edu.nju.ws.edgqa.utils.connect.HttpUtil;
import cn.edu.nju.ws.edgqa.utils.enumerates.KBEnum;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;


public class DexterEntityLinking {

    //public static final String dexterIP = "127.0.0.1";
    public static final String dexterIP = "114.212.190.19";
    //public static final String dexterIP = "114.212.86.218";
    public static final String dexterSpotUrl = "http://" + dexterIP + ":8080/dexter-webapp/api/rest/spot?&wn=false&debug=false&format=text&text=";
    public static final String dexterAnnotateUrl = "http://" + dexterIP + ":8080/dexter-webapp/api/rest/annotate?&n=50&wn=false&debug=false&format=text&min-conf=0.5&text=";
    //private static final String dexterPath = "D:\\Programs\\dexter2\\dexter-2.1.0.jar";
    private static final String dexterPath = "D:\\Programs\\dexter2\\dexter-2.1.0.jar";
    private static final String env = "D:\\Programs\\dexter2";
    private static boolean isStart = false;

    /**
     * Load json from Dexter server
     * Note that the spot mention will be lowercase, you can make it a substring of original sentence manually
     *
     * @param url the url of Dexter service
     * @return json string
     */
    public static String loadJson(String url) {
        StringBuilder json = new StringBuilder();
        try {
            URL urlObject = new URL(url);
            URLConnection uc = urlObject.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream(), StandardCharsets.UTF_8));
            String line = null;
            while ((line = in.readLine()) != null) {
                json.append(line);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    /**
     * Return the Entity and corresponding ID recognized by dexter2
     *
     * @param question
     * @return
     */
    public static HashMap<String, Integer> getEntityID(String question) {
        //String question = "What is the total population of Melbourne, Florida?";
        HashMap<String, Integer> map = new HashMap<>();
        String questionURL = null;
        try {
            questionURL = URLEncoder.encode(question, "UTF-8");
            String url = dexterAnnotateUrl + questionURL;
            String jsonData = loadJson(url);
            JSONObject object = new JSONObject(jsonData);
            JSONArray jsonArray = object.getJSONArray("spots");
            //System.out.println(jsonArray);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject spot = jsonArray.getJSONObject(i);
                spot.put("mention", question.substring(spot.getInt("start"), spot.getInt("end"))); // keep the original case
                String mention = spot.getString("mention");
                int mentionLen = mention.split(" ").length;
                map.put(mention, spot.getInt("entity"));
                //System.out.println("[" + mentionLen + "]" + question + ": " + mention + " score: " + spot.get("score") + " id: " + spot.get("entity"));
            }


        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return map;

    }

    /**
     * return entity and its candidateID detected by dexter2
     *
     * @param question
     * @return {mention:wikiID} map
     */
    public static HashMap<String, List<Integer>> getCandidateEntityIDs(String question) {

        HashMap<String, List<Integer>> result = new HashMap<>();

        String url = null;
        try {
            url = dexterSpotUrl + URLEncoder.encode(question, "UTF-8");
            //url = dexterAnnotateUrl+URLEncoder.encode(question,"UTF-8");
            String jsonString = HttpUtil.loadJson(url);

            JSONObject jsonObject = new JSONObject(jsonString);
            //System.out.println(jsonObject.toString(4));
            JSONArray spots = jsonObject.getJSONArray("spots");
            // System.out.println(spots);
            for (int i = 0; i < spots.length(); i++) {
                List<Integer> res = new LinkedList<>();
                // the substring of question has the original case
                spots.getJSONObject(i).put("mention",
                        question.substring(spots.getJSONObject(i).getInt("start"), spots.getJSONObject(i).getInt("end")));
                String mention = spots.getJSONObject(i).getString("mention");
                double linkProbability = spots.getJSONObject(i).getDouble("linkProbability");
                if (linkProbability < 0.5) {
                    continue;
                }
                JSONArray candidates = spots.getJSONObject(i).getJSONArray("candidates");
                for (int j = 0; j < Math.min(candidates.length(), 5); j++) {//Only take top5
                    res.add(candidates.getJSONObject(j).getInt("entity"));
                }
                result.put(mention, res);
            }
            //System.out.println(jsonObject);
            //System.out.println(loadJson(url));

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * judge if a mention is dexter entity
     *
     * @param utterance mention
     * @param e         threshold of confidence
     * @return if confidence grater than threshold e, return true，else return false
     */
    public static boolean isDexterEntity(String utterance, double e) {
        try {
            String url = dexterAnnotateUrl + URLEncoder.encode(utterance, "UTF-8");
            String jsonString = HttpUtil.loadJson(url);
            //System.out.println(jsonString);
            JSONObject jsonObject = new JSONObject(jsonString);

            JSONArray spots = jsonObject.getJSONArray("spots");
            for (int i = 0; i < spots.length(); i++) {
                JSONObject spot = spots.getJSONObject(i);
                String mention = spot.getString("mention");
                //System.out.println(mention);
                if ((double) mention.length() / utterance.length() >= e) { //The proportion of the total length is greater than the threshold e
                    return true;
                }
            }

        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        return false;
    }

    public static void main(String[] args) throws IOException {

        //System.out.println(recognizeLongE("Who has official residences at Colts Neck Township and Beverly Hills?",new LinkedHashMap<>()));
        System.out.println(LinkingTool.recognizeLongEntity("how many nations can I find dishes made of Shallot", new LinkedHashMap<>(), KBEnum.DBpedia));
        //System.out.println(recognizeLongELCQUAD("What is the incumbent of the Al Gore presidential campaign, 2000 and also the president of the Ann Lewis",new LinkedHashMap<>()));
        //System.out.println(getEntityID("Barack Obama and Barack Michelle"));
        //System.out.println(getCandidateEntityIDs("Which architect of Marine Corps Air Station Kaneohe Bay was also tenant of New Sanno hotel '/"));
        //System.out.println(getEntityID("Barack Obama and Barack Michelle"));

    }
}
