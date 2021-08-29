package cn.edu.nju.ws.edgqa.utils.linking;

import cn.edu.nju.ws.edgqa.domain.beans.Link;
import cn.edu.nju.ws.edgqa.utils.FileUtil;
import org.json.JSONArray;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class EntityLinkingThread implements Callable<Map<String, List<Link>>> {
    public static final int LINKING_DEXTER = 0;
    public static final int LINKING_EARL = 1;
    public static final int LINKING_FALCON = 2;
    private final String nodeStr;
    private Thread t;
    private int linkingTool = -1;

    public EntityLinkingThread(int linkingTool, String nodeStr, Map<String, List<Link>> resultMap) {
        this.linkingTool = linkingTool;
        this.nodeStr = nodeStr;
    }

    @Override
    public Map<String, List<Link>> call() throws Exception {
        Map<String, List<Link>> res = null;
        if (linkingTool == LINKING_DEXTER) {
            res = LinkingTool.getDexterLinking(nodeStr);
        } else if (linkingTool == LINKING_EARL) {
            res = LinkingTool.getEARLLinking(nodeStr);
        } else if (linkingTool == LINKING_FALCON) {
            res = LinkingTool.getFalconLinking(nodeStr);
        }
        return res;
    }


    public static void main(String[] args) {
        JSONArray data = new JSONArray(FileUtil.readFileAsString("src/main/resources/datasets/lcquad-test.json"));
        for (int idx = 0; idx < data.length(); idx++) {
            String question = data.getJSONObject(idx).getString("corrected_question");
            Map<String, List<Link>> dexterLink = LinkingTool.getDexterLinking(question);
            Map<String, List<Link>> earlLink = LinkingTool.getEARLLinking(question);

            System.out.println(question);
            System.out.println(dexterLink);
            System.out.println(earlLink);
            System.out.println();
        }
    }
}
