package cn.edu.nju.ws.edgqa.utils.similarity;

import cn.edu.nju.ws.edgqa.domain.edg.EDG;
import cn.edu.nju.ws.edgqa.utils.QAArgs;
import cn.edu.nju.ws.edgqa.utils.connect.HttpsClientUtil;
import cn.edu.nju.ws.edgqa.utils.enumerates.DatasetEnum;
import cn.edu.nju.ws.edgqa.utils.enumerates.KBEnum;
import cn.edu.nju.ws.edgqa.utils.kbutil.KBUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * neural relation detection: https://github.com/lzw429/EDG-relation-detection/treeNode/master/neural_relation_detection
 */
public class NeuralRelationDetectionStrategy implements SimilarityStrategy {

    //private static final String serverIP = "210.28.134.34";
    private static final String serverIP = "114.212.190.19";
    private static final int serverPort = 5678;
    private static final String serviceURL = "http://" + serverIP + ":" + serverPort + "/relation_detection";
    private static final int set_sparql_serverPort_LCQUAD = 5683;
    private static final int set_sparql_serverPort_QALD = 5684;
    private static HttpURLConnection conn = null;
    private static int set_serverPort = 5682;
    private static String set_serviceURL = "http://" + serverIP + ":" + set_serverPort + "/relation_detection";

    public static Map<String, Double> scoreSet_relation(@NotNull String question, @NotNull Set<String> labels) {

        if (EDG.getKB() == KBEnum.Freebase) {
            set_serverPort = 5680;
            set_serviceURL = "http://" + serverIP + ":" + set_serverPort + "/relation_detection";
        }

        String[] labelArr = new String[labels.size()];
        labels.toArray(labelArr);
        Double[] detection_res = null;
        Map<String, Double> resMap = new HashMap<>();
        try {
            JSONArray array = new JSONArray(labelArr);

            String input = "{\"question\": \"" + question + "\", \"labels\": " + array + "}";
            //System.out.println(input);
            String output = HttpsClientUtil.doPost(set_serviceURL, input);
            //System.out.println(output);
            Gson gson = new Gson();
            Map<String, Double[]> map = gson.fromJson(String.valueOf(output), new TypeToken<Map<String, Double[]>>() {
            }.getType());
            detection_res = map.get("detection_res");

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (detection_res != null) {
            for (int i = 0; i < detection_res.length; i++) {
                resMap.put(labelArr[i], detection_res[i]);
            }
        }

        return resMap;
    }

    public static Map<String, Double> scoreSet_sparql(@NotNull String question, @NotNull Set<String> labels) {

        String[] labelArr = new String[labels.size()];
        labels.toArray(labelArr);
        Double[] detection_res = null;
        Map<String, Double> resMap = new HashMap<>();
        int port = set_sparql_serverPort_LCQUAD;
        if (QAArgs.getDataset() == DatasetEnum.QALD_9) {
            port = set_sparql_serverPort_QALD;
        }
        try {
            JSONArray array = new JSONArray(labelArr);

            String input = "{\"question\": \"" + question + "\", \"labels\": " + array + "}";
            //System.out.println(input);
            String set_sparql_serviceURL = "http://" + serverIP + ":" + port + "/query_rerank";
            String output = HttpsClientUtil.doPost(set_sparql_serviceURL, input);
            Gson gson = new Gson();
            Map<String, Double[]> map = gson.fromJson(String.valueOf(output), new TypeToken<Map<String, Double[]>>() {
            }.getType());
            detection_res = map.get("detection_res");

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (detection_res != null) {
            for (int i = 0; i < detection_res.length; i++) {
                resMap.put(labelArr[i], detection_res[i]);
            }
        }

        return resMap;
    }

    public static void main(String[] args) {

        KBUtil.init(DatasetEnum.LC_QUAD);
        Map<String, Double> score = scoreSet_sparql("[BLK]  [DES] Name #entity1 [DES] is Ptolemy XIII Theos Philopator [DES]  [BLK]  [DES] a queen [DES] whose parent is Ptolemy XII Auletes [DES] whose parent is consort", new HashSet<>(Arrays.asList(" [TRP] ?e1 parent Ptolemy XII Auletes [TRP] ?e0 name ?e1")));
        Map<String, Double> score_1 = scoreSet_sparql("[BLK]  [DES] are the bands associated with #entity1 [BLK]  [DES] the artists of My Favorite Girl", new HashSet<>(Arrays.asList("\t [TRP] ?e1 Artist My Favorite Girl (Dave Hollister song) [TRP] ?e0 associated musical artist ?e1")));
        System.out.println(score);
        System.out.println(score_1);

    }

    public void connect() throws IOException {
        if (conn != null)
            return;
        URL url = new URL(serviceURL);
        conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("charset", "utf-8");
    }

    public void disConnect() throws IOException {
        conn.disconnect();
    }

    public double score(@NotNull String question, @NotNull String label) {
        Double score = 0.0;
        try {
            String input = "{\"question\": \"" + question + "\", \"label\": \"" + label + "\"}";
            String output = HttpsClientUtil.doPost(serviceURL, input);
            Gson gson = new Gson();
            Map<String, Double> map = gson.fromJson(String.valueOf(output), new TypeToken<Map<String, Double>>() {
            }.getType());
            score = map.get("detection_res");
//            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return score;
    }
}
