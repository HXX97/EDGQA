package cn.edu.nju.ws.edgqa.utils.linking;

import cn.edu.nju.ws.edgqa.utils.connect.HttpUtil;
import cn.edu.nju.ws.edgqa.utils.connect.HttpsClientUtil;
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

    public static final String dexterLocalUrl = "http://" + dexterIP + ":8080/dexter-webapp/api/rest/spot";


    /**
     * return entity and its candidateID detected by dexter2
     *
     * @param question natural language question
     * @return {mention:wikiID} map
     */
    public static HashMap<String, List<Integer>> getCandidateEntityIDs(String question) {

        HashMap<String, List<Integer>> result = new HashMap<>();

        String url = null;

        JSONObject inputObj = new JSONObject();
        inputObj.put("text", question);
        inputObj.put("wn", "false");
        inputObj.put("debug", "false");
        inputObj.put("format", "text");

        url = dexterLocalUrl;
        String jsonString = HttpsClientUtil.doPostWithParams(url, inputObj);

        if(jsonString!=null&&!jsonString.isEmpty()) {
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


        JSONObject inputObj = new JSONObject();
        inputObj.put("text", utterance);
        inputObj.put("wn", "false");
        inputObj.put("debug", "false");
        inputObj.put("format", "text");

        String jsonString = HttpsClientUtil.doPostWithParams(dexterLocalUrl, inputObj);
        if (jsonString != null && !jsonString.isEmpty()) {
            JSONObject jsonObject = new JSONObject(jsonString);

            JSONArray spots = jsonObject.getJSONArray("spots");
            for (int i = 0; i < spots.length(); i++) {
                JSONObject spot = spots.getJSONObject(i);
                String mention = spot.getString("mention");
                //System.out.println(mention);
                if ((double) mention.length() / utterance.length() >= e) {
                    /*The proportion of the total length is greater than the threshold e*/
                    return true;
                }
            }
        }


        return false;
    }

    public static void main(String[] args) {
        String question = "Which architect of Marine Corps Air Station Kaneohe Bay was also tenant of New Sanno hotel";
        System.out.println(getCandidateEntityIDs(question));
        //System.out.println(getCandidateEntityIDs_new(question));

        System.out.println(isDexterEntity("Barack Obama", 0.8));
    }
}
