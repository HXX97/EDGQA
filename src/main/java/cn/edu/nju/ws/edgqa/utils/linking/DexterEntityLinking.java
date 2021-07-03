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

    public static final String dexterIP = "114.212.190.19";
    public static final String dexterSpotUrl = "http://" + dexterIP + ":8080/dexter-webapp/api/rest/spot?&wn=false&debug=false&format=text&text=";
    public static final String dexterAnnotateUrl = "http://" + dexterIP + ":8080/dexter-webapp/api/rest/annotate?&n=50&wn=false&debug=false&format=text&min-conf=0.5&text=";

    /**
     * return entity and its candidateID detected by dexter2
     *
     * @param question natural language question
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

    public static void main(String[] args){
        String question = "Which architect of Marine Corps Air Station Kaneohe Bay was also tenant of New Sanno hotel";
        System.out.println(getCandidateEntityIDs(question));
    }
}
