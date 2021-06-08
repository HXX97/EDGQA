package cn.edu.nju.ws.edgqa.utils.linking;

import cn.edu.nju.ws.edgqa.domain.beans.Link;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Map;

public class DBCLinkingUtil {

    private static String host = "localhost";
    private static int port = 5680;

    public static Map<String, List<Link>> getDBCLinking(String mention) {
        Map<String, List<Link>> res = null;
        try {
            Socket client = new Socket(host, port);
            OutputStream outToServer = client.getOutputStream();
            DataOutputStream out = new DataOutputStream(outToServer);

            out.writeUTF("mention:" + mention);
            InputStream inFromServer = client.getInputStream();
            DataInputStream in = new DataInputStream(inFromServer);
            String resStr = in.readUTF();
            if (resStr.equals("null"))
                return null;
            Gson gson = new Gson();
            res = gson.fromJson(resStr, new TypeToken<Map<String, List<Link>>>() {
            }.getType());
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }
}
