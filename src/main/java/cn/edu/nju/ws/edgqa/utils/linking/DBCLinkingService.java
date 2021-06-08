package cn.edu.nju.ws.edgqa.utils.linking;

import cn.edu.nju.ws.edgqa.domain.beans.Link;
import cn.edu.nju.ws.edgqa.utils.SimilarityUtil;
import cn.edu.nju.ws.edgqa.utils.enumerates.LinkEnum;
import com.google.gson.Gson;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class DBCLinkingService extends Thread {

    private static List<DbpediaCategory> dbcList;
    private ServerSocket serverSocket;

    public DBCLinkingService(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(10000);
    }

    public static List<DbpediaCategory> getDbcList() {
        if (dbcList == null)
            dbcList = readDBCListFromFile("cache/dbpedia1604-category.data");
        return dbcList;
    }

    /**
     * Get the dbpedia category linkings
     *
     * @param mention category mention
     * @return the linking map
     */
    public static Map<String, List<Link>> getDBCLinking(String mention) {
        Map<String, List<Link>> res = new HashMap<>();
        res.put(mention, new CopyOnWriteArrayList<>());
        AtomicInteger count = new AtomicInteger(0);

        getDbcList().parallelStream().forEach(dbc -> {
            double score = SimilarityUtil.getScore(dbc.getLabel(), mention);
            if (score >= 0.7) {
                res.get(mention).add(new Link(mention, dbc.getUri(), LinkEnum.TYPE, score));
            }
        });

        for (String key : res.keySet()) {
            res.get(key).sort(Collections.reverseOrder());
        }
        return res;
    }

    public static void writeDBCListToFile() {
        try {
            String cacheFilepath = "cache/dbpedia1604-category.data";
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(cacheFilepath));
            BufferedReader br = new BufferedReader(new FileReader("dataset/dbpedia1604Category.csv"));

            List<DbpediaCategory> dbcList = new ArrayList<>();
            String line = null;
            line = br.readLine(); // skip the first line of CSV file
            int count = 0;
            while ((line = br.readLine()) != null) {
                String[] lineSplit = line.split(",");
                dbcList.add(new DbpediaCategory(lineSplit[0], lineSplit[1], lineSplit[2]));
                if (count % 10000 == 0) {
                    System.out.println("[INFO] Line " + count);
                }
                count++;
            }
            oos.writeObject(dbcList);
            oos.flush();
            oos.close();
            System.out.println("[INFO] the DBC list has been saved in " + cacheFilepath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<DbpediaCategory> readDBCListFromFile(String filename) {
        List<DbpediaCategory> res = null;
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename));
            res = (List<DbpediaCategory>) ois.readObject();
            ois.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return res;
    }

    public static void main(String[] args) {
        int port = 5680;
        try {
            Thread t = new DBCLinkingService(port);
            t.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the dbpedia category linkings
     *
     * @param mention category mention
     * @return the json string for object Map(String, List(Link))
     */
    public String getDBCLinkingJson(String mention) {
        Gson gson = new Gson();
        return gson.toJson(getDBCLinking(mention));
    }

    public void run() {
        dbcList = DBCLinkingService.readDBCListFromFile("cache/dbpedia1604-category.data");
        System.out.println("[INFO] the dbpedia category linking service has started");
        while (true) {
            try {
                System.out.println("[INFO] Waiting for remote connections, port: " + serverSocket.getLocalPort() + "...");
                Socket server = serverSocket.accept();
                System.out.println("[INFO] Remote server address: " + server.getRemoteSocketAddress());
                DataInputStream in = new DataInputStream(server.getInputStream());

                String resJson = null;
                String inStr = in.readUTF();
                if (inStr.startsWith("mention:")) {
                    resJson = getDBCLinkingJson(inStr.substring(8)); // get the json string as linking result
                }
                DataOutputStream out = new DataOutputStream(server.getOutputStream());
                if (resJson == null)
                    resJson = "null";
                out.writeUTF(resJson);
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
