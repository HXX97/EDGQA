package cn.edu.nju.ws.edgqa.handler;

import cn.edu.nju.ws.edgqa.domain.beans.Link;
import cn.edu.nju.ws.edgqa.utils.linking.LinkingTool;

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
}
