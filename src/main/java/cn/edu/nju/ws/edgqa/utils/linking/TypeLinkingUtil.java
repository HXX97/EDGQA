package cn.edu.nju.ws.edgqa.utils.linking;

import cn.edu.nju.ws.edgqa.utils.connect.HttpsClientUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class TypeLinkingUtil {
    private static final String serverIP = "114.212.190.19";
    private static final int serverPort = 6789;
    private static final String serviceURL = "http://" + serverIP + ":" + serverPort + "/class_linking";

    /**
     * Class linking
     *
     * @param NVP a NVP string
     * @return linking result for this NVP
     */
    public static List<String> getTypeLinkingList(@NotNull String NVP) {
        List<String> classLinkingResult;

        String input = "{\"NVP\": \"" + NVP + "\"}";
        String output = HttpsClientUtil.doPost(serviceURL, input);
        Gson gson = new Gson();

        Map<String, List<String>> map = gson.fromJson(String.valueOf(output),
                new TypeToken<Map<String, List<String>>>() {
                }.getType());
        classLinkingResult = map.get("classLinking");
        return classLinkingResult;
    }
}
