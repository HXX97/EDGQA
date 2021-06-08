package cn.edu.nju.ws.edgqa.utils.connect;


import org.json.JSONArray;
import org.json.JSONObject;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class HttpUtil {

    final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    public static void main(String[] args) throws UnsupportedEncodingException {
        // SOCKS proxy, supporting HTTP and HTTPS requests
        // note that if SOCKS proxy is set, do not set HTTP/HTTPS proxy
        System.setProperty("socksProxyHost", "127.0.0.1");
        System.setProperty("socksProxyPort", "7891");

        String sparqlQuery = "SELECT ?altLabel\n" +
                "{\n" +
                " VALUES (?wd) {(wd:P106)}\n" +
                " ?wd skos:altLabel ?altLabel .\n" +
                " FILTER (lang(?altLabel) = \"en\")\n" +
                "}";

        String url = "https://query.wikidata.org/sparql?format=json&query=";
        url += URLEncoder.encode(sparqlQuery, "UTF-8");
        String s = loadJson(url);
        JSONObject o = new JSONObject(s);
        for (String key : o.keySet()) {
            System.out.println(key + ": " + o.get(key));
        }
        JSONObject results = o.getJSONObject("results");
        if (results.keySet().contains("bindings")) {
            JSONArray bindings = results.getJSONArray("bindings");
            for (int i = 0; i < bindings.length(); i++) {
                System.out.println(bindings.getJSONObject(i).getJSONObject("altLabel").getString("value"));
            }
        }

        /*String url = "https://earldemo.sda.tech/earl/api/processQuery";
        try {
            String res =sendPost(url, " {\"nlquery\":\"Where is the residence of the prime minister of Spain\", \"pagerankflag\": false}");
            JSONObject json = new JSONObject(res);
            //System.out.println("ertypes: "+json.get("ertypes"));
            Iterator<String> keys = json.keys();
            while(keys.hasNext()){
                String key = keys.next();
                System.out.println(key+": " + json.get(key));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }*/
        System.getProperties().remove("socksProxyHost");
        System.getProperties().remove("socksProxyPort");
    }

    /**
     *    * @use urlconnection
     *    * @param url
     *    * @param Params JSON data
     *    * @return
     *    * @throws IOException
     *    
     */
    public static String sendPost(String url, String Params) throws IOException {

        OutputStreamWriter out = null;
        BufferedReader reader = null;
        String response = "";
        try {
            trustAllHosts();
            URL httpUrl = null; //HTTP URL class Use this class to create a connection
            //URLConnection connection = httpUrl.openConnection();
            //Create URL
            httpUrl = new URL(url);
            //establish connection
            URLConnection conn = httpUrl.openConnection();
            if (conn instanceof HttpsURLConnection) {
                ((HttpsURLConnection) conn).setRequestMethod("POST");
                conn.setRequestProperty("accept", "application/json");
                conn.setRequestProperty("connection", "keep-alive");
                conn.setUseCaches(false);//Don't cache
                ((HttpsURLConnection) conn).setInstanceFollowRedirects(true); //allow redirect
                conn.setDoInput(true); //receive input
                conn.setDoOutput(true); //no output
                ((HttpsURLConnection) conn).setHostnameVerifier(DO_NOT_VERIFY);
            } else {
                ((HttpURLConnection) conn).setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("connection", "keep-alive");
                conn.setUseCaches(false);//Don't cache
                ((HttpURLConnection) conn).setInstanceFollowRedirects(true);
                conn.setDoOutput(true);
                conn.setDoInput(true);

            }
            conn.connect();
            //POST requests
            out = new OutputStreamWriter(conn.getOutputStream(), StandardCharsets.UTF_8);
            out.write(Params);
            out.flush();
            //read response
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String lines;
            while ((lines = reader.readLine()) != null) {
                lines = new String(lines.getBytes(), StandardCharsets.UTF_8);
                response += lines;
            }
            reader.close();
            ((HttpURLConnection) conn).disconnect();

            //System.out.println(response);


        } catch (Exception e) {
            System.out.println("seen POST requests error!" + e);
            e.printStackTrace();
        }
        //Use finally block to close output stream and input stream
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return response;
    }

    public static String sendGet(String url) {

        BufferedReader reader = null;
        String response = "";

        try {
            //trustAllHosts();
            URL httpUrl = new URL(url);
            URLConnection connection = httpUrl.openConnection();
            if (connection instanceof HttpsURLConnection) {
                HttpsURLConnection conn = (HttpsURLConnection) connection;
                conn.setRequestMethod("GET");
                conn.setRequestProperty("accept", "application/json");
                conn.setRequestProperty("connection", "keep-alive");
                conn.setUseCaches(false);//don't cache
                conn.setInstanceFollowRedirects(true); //allow redirect
                conn.setDoInput(true); //receive input
                conn.setDoOutput(false); //no output
                conn.setHostnameVerifier(DO_NOT_VERIFY);
                conn.connect();
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String lines;
                while ((lines = reader.readLine()) != null) {
                    lines = new String(lines.getBytes(), StandardCharsets.UTF_8);
                    response += lines;
                }
                reader.close();
                // disconnected
                conn.disconnect();
            } else {
                HttpURLConnection conn = (HttpURLConnection) connection;
                conn.setRequestMethod("GET");
                conn.setRequestProperty("accept", "application/json");
                conn.setRequestProperty("connection", "keep-alive");
                conn.setUseCaches(false);//don't cache
                conn.setInstanceFollowRedirects(true); //allow redirect
                conn.setDoInput(true); //receive input
                conn.setDoOutput(false); //no output
                conn.connect();
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String lines;
                while ((lines = reader.readLine()) != null) {
                    lines = new String(lines.getBytes(), "utf-8");
                    response += lines;
                }
                reader.close();
                // disconnected
                conn.disconnect();

            }

        } catch (IOException e) {
            System.out.println("send GET requests error!" + e);
            e.printStackTrace();
        }//Use finally block to close output stream and input stream
        finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return response;

    }

    /**
     * Access a url in GET mode and return data in json format
     *
     * @param url website
     * @return Json format data returned by the website
     */
    public static String loadJson(String url) {
        StringBuilder json = new StringBuilder();
        try {
            URL urlObject = new URL(url);
            URLConnection uc = urlObject.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream(), "utf-8"));
            String inputLine = null;
            while ((inputLine = in.readLine()) != null) {
                json.append(inputLine);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    private static void trustAllHosts() {
        final String TAG = "trustAllHosts";
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[]{};
            }

            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                //Log.i(TAG, "checkClientTrusted");
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                //Log.i(TAG, "checkServerTrusted");
            }
        }};
        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

