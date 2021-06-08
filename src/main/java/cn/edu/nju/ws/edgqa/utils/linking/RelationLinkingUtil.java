package cn.edu.nju.ws.edgqa.utils.linking;

import cn.edu.nju.ws.edgqa.utils.connect.HttpUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

public class RelationLinkingUtil {

    // falcon link to wikidata including EL/RL
    private static final String falconLongURL = "https://labs.tib.eu/falcon/falcon2/api?mode=long";
    //Wikidata executes the URL of the sparql query
    private static final String wikidataSparqlURL = "https://query.wikidata.org/sparql?format=json&query=";

    // Local proxy IP
    private static final String proxyIP = "127.0.0.1";
    // Local proxy port
    private static final String proxyPort = "7890";


    /**
     * Set up HTTP proxy
     */
    private static void setHttpProxy() {
        // HTTP proxy, can only proxy HTTP requests
        System.setProperty("http.proxyHost", proxyIP);
        System.setProperty("http.proxyPort", proxyPort);

        // HTTPS proxy, can only proxy HTTPS requests
        System.setProperty("https.proxyHost", proxyIP);
        System.setProperty("https.proxyPort", proxyPort);

        //System.setProperty("socksProxyHost", proxyIP);
        //System.setProperty("socksProxyPort", proxyPort);
    }

    /**
     * Unset HTTP proxy
     */
    private static void unsetHttpProxy() {
        System.getProperties().remove("http.roxyHost");
        System.getProperties().remove("https.roxyHost");
        System.getProperties().remove("http.ProxyPort");
        System.getProperties().remove("https.ProxyPort");
        System.getProperties().remove("socksProxyHost");
        System.getProperties().remove("socksProxyPort");
    }


    /**
     * Tool Falcon 2.0
     * Enter a phrase that is suspected to be a relationship, and return its corresponding entity link/relational link
     *
     * @param utterance utterance
     * @param k         top k
     * @return top k link List
     */
    public static List<String> getFalconRelLinking(String utterance, int k) {
        List<String> result = new LinkedList<>();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("text", utterance);
        try {
            setHttpProxy();
            String url = falconLongURL + "&k=" + k;
            String response = HttpUtil.sendPost(url, jsonObject.toString());
            if (response != null) {
                JSONObject o1 = new JSONObject(response);
                JSONArray wikidataRels = o1.getJSONArray("relations_wikidata");
                for (int i = 0; i < wikidataRels.length(); i++) {
                    result.add(wikidataRels.getJSONArray(i).getString(0));
                }
            }
            unsetHttpProxy();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }


    /**
     * Enter a relational word and return its possible alias on wikidata
     *
     * @param utterance Enter relative words, such as author/occupation
     * @return Possible aliases
     */
    public static List<String> getAlias(String utterance) {

        List<String> alias = new LinkedList<>();
        //Take the possible linking of top3
        List<String> pIds = getFalconRelLinking(utterance, 3);
        for (String pid : pIds) {
            String[] strArr = pid.replaceAll(">", "").split("/");
            String id = strArr[strArr.length - 1];//Get PID such as P106
            String sparqlQuery = "SELECT ?altLabel\n" +
                    "{\n" +
                    " VALUES (?wd) {(wd:" + id + ")}\n" +
                    " ?wd skos:altLabel ?altLabel .\n" +
                    " FILTER (lang(?altLabel) = \"en\")\n" +
                    "}";
            String s = queryWikidataSparql(sparqlQuery);
            JSONObject o = new JSONObject(s);
            JSONObject results = o.getJSONObject("results");
            if (results.keySet().contains("bindings")) { //Judging whether there is a result
                JSONArray bindings = results.getJSONArray("bindings");
                for (int i = 0; i < bindings.length(); i++) {
                    alias.add(bindings.getJSONObject(i).getJSONObject("altLabel").getString("value"));
                    //System.out.println(bindings.getJSONObject(i).getJSONObject("altLabel").getString("value"));
                }
            }
        }
        return alias;
    }


    /**
     * Enter sparqlQuery to query String, and return the query result in json format
     *
     * @param sparqlQuery sparql query String
     * @return json format query result
     */
    public static String queryWikidataSparql(String sparqlQuery) {
        String res = null;
        try {
            String url = wikidataSparqlURL + URLEncoder.encode(sparqlQuery, "UTF-8");
            setHttpProxy();
            res = HttpUtil.loadJson(url);
            unsetHttpProxy();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return res;
    }


    public static void main(String[] args) {

        //System.out.println(getFalconRelLinking("occupation", 5));
        System.out.println(getAlias("occupation"));
        System.out.println(getAlias("author"));
    }

}
