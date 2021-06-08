package cn.edu.nju.ws.edgqa.utils.linking;

import cn.edu.nju.ws.edgqa.domain.beans.Link;
import cn.edu.nju.ws.edgqa.utils.FileUtil;
import cn.edu.nju.ws.edgqa.utils.QAArgs;
import cn.edu.nju.ws.edgqa.utils.SimilarityUtil;
import cn.edu.nju.ws.edgqa.utils.enumerates.DatasetEnum;
import cn.edu.nju.ws.edgqa.utils.enumerates.LinkEnum;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GoldenLinkingUtil {

    private static JSONArray ansArray = null;

    private static JSONArray getAnsArray() {
        if (ansArray == null) {
            if (QAArgs.getDataset() == DatasetEnum.LC_QUAD) {
                ansArray = new JSONArray(FileUtil.readFileAsString("src/main/resources/datasets/lcquad-answers.json"));
            }
        }
        return ansArray;
    }

    private static JSONObject getAnsArrayAt(int serialNumber) {
        if (serialNumber < 0) return null;
        if (QAArgs.getDataset() == DatasetEnum.LC_QUAD && serialNumber > 4999) return null;
        if (QAArgs.getDataset() == DatasetEnum.QALD_9) return null;

        for (int i = 0; i < getAnsArray().length(); i++) {
            JSONObject jsonObject = ansArray.getJSONObject(i);
            if (QAArgs.getDataset() == DatasetEnum.LC_QUAD) {
                if (jsonObject.getInt("SerialNumber") - 1 == serialNumber) {
                    // note that the serialNumber in ansArray file equals the real serialNumber + 1 here
                    return jsonObject;
                }
            }
        }
        return null;
    }

    public static Set<String> removeAngleBrackets(Set<String> stringSet) {
        Set<String> res = new HashSet<>();
        for (String str : stringSet) {
            int begin = 0;
            int end = str.length();
            if (str.startsWith("<"))
                begin += 1;
            if (str.endsWith(">"))
                end -= 1;
            res.add(str.substring(begin, end));
        }
        return res;
    }

    /**
     * Get all the dbr from the golden sparql
     *
     * @param goldenSparql the golden sparql query
     * @return a set of golden entity linking results
     */
    public static Set<String> getGoldenEntityLinkBySparql(String goldenSparql) {
        Set<String> res = new HashSet<>();
        if (QAArgs.getDataset() == DatasetEnum.LC_QUAD) {
            String pattern = "<http://dbpedia.org/resource/(.*?)>";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(goldenSparql);
            while (m.find()) {
                res.add(m.group());
            }
        }
        return removeAngleBrackets(res);
    }

    /**
     * Get all the dbp and lowercase dbo from the golden sparql
     *
     * @param goldenSparql the golden sparql query
     * @return a set of golden relation linking results
     */
    public static Set<String> getGoldenRelationLinkBySparql(String goldenSparql) {
        Set<String> res = new HashSet<>();
        if (QAArgs.getDataset() == DatasetEnum.LC_QUAD) {
            String pattern = "<http://dbpedia.org/property/(.*?)>";
            Pattern propertyPattern = Pattern.compile(pattern);
            Matcher propertyMatcher = propertyPattern.matcher(goldenSparql);
            while (propertyMatcher.find()) {
                res.add(propertyMatcher.group());
            }

            pattern = "<http://dbpedia.org/ontology/(.*?)>";
            Pattern ontologyPattern = Pattern.compile(pattern);
            Matcher ontologyMatcher = ontologyPattern.matcher(goldenSparql);
            while (ontologyMatcher.find()) {
                res.add(ontologyMatcher.group());
            }
        }
        res.removeIf(link -> link.startsWith("<http://dbpedia.org/ontology/") && Character.isUpperCase(link.charAt(29)));
        return removeAngleBrackets(res);
    }

    /**
     * Get all the uppercase dbo from the golden sparql
     *
     * @param goldenSparql the golden sparql query
     * @return a set of golden type linking results
     */
    public static Set<String> getGoldenTypeLinkBySparql(String goldenSparql) {
        Set<String> res = new HashSet<>();
        if (QAArgs.getDataset() == DatasetEnum.LC_QUAD) {
            String pattern = "<http://dbpedia.org/ontology/(.*?)>";
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(goldenSparql);
            while (m.find()) {
                res.add(m.group());
            }
        }
        res.removeIf(link -> link.startsWith("<http://dbpedia.org/ontology/") && Character.isLowerCase(link.charAt(29)));
        return removeAngleBrackets(res);
    }

    public static List<Link> getGoldenEntityLinkBySerialNumber(int serialNumber) {
        List<Link> res = new ArrayList<>();
        JSONObject quesJSONObject = getAnsArrayAt(serialNumber);
        if (quesJSONObject == null) return res;
        JSONArray entityMapping = quesJSONObject.getJSONArray(getEntityMappingKey());
        for (int j = 0; j < entityMapping.length(); j++) {
            JSONObject mapping = entityMapping.getJSONObject(j);
            String matchedBy = null;
            if (mapping.has("matchedBy")) mapping.getString("matchedBy");
            String seq = null;
            if (mapping.has("seq")) mapping.getString("seq");
            res.add(new Link(mapping.getString("label"), mapping.getString("uri"), LinkEnum.ENTITY,
                    matchedBy, seq));
        }
        return res;
    }

    public static List<String> getGoldenEntityLinkURIBySerialNumber(int serialNumber) {
        return getGoldenEntityLinkBySerialNumber(serialNumber).stream().map(Link::getUri).distinct().collect(Collectors.toList());
    }

    public static String getEntityMappingKey() {
        if (QAArgs.getDataset() == DatasetEnum.LC_QUAD)
            return "entity mapping";
        return null;
    }

    public static List<Link> getGoldenRelationLinkBySerialNumber(int serialNumber) {
        List<Link> res = new ArrayList<>();
        JSONObject quesJSONObject = getAnsArrayAt(serialNumber);
        if (quesJSONObject == null) return res;
        JSONArray relationMapping = quesJSONObject.getJSONArray(getPredicateMappingKey());
        getGoldenRelationLinkForQues(res, relationMapping);
        return res;
    }

    public static List<String> getGoldenRelationLinkURIBySerialNumber(int serialNumber) {
        return getGoldenRelationLinkBySerialNumber(serialNumber).stream().map(Link::getUri).distinct().collect(Collectors.toList());
    }

    public static String getPredicateMappingKey() {
        if (QAArgs.getDataset() == DatasetEnum.LC_QUAD)
            return "predicate mapping";
        return null;
    }

    private static void getGoldenRelationLinkForQues(List<Link> res, JSONArray relationMapping) {
        for (int j = 0; j < relationMapping.length(); j++) {
            JSONObject mapping = relationMapping.getJSONObject(j);
            String uri = mapping.getString("uri");
            if (uri.startsWith("http://dbpedia.org/ontology/") && Character.isUpperCase(uri.charAt(28)))
                continue;
            String mappedBy = null;
            if (mapping.has("mappedBy")) mappedBy = mapping.getString("mappedBy");
            String seq = null;
            if (mapping.has("seq")) seq = mapping.getString("seq");
            res.add(new Link(mapping.getString("label"), uri, LinkEnum.RELATION,
                    mappedBy, seq));
        }
    }

    private static void getGoldenTypeLinkForQues(List<Link> quesLinkList, JSONArray typeMapping) {
        for (int j = 0; j < typeMapping.length(); j++) {
            JSONObject mapping = typeMapping.getJSONObject(j);
            String uri = mapping.getString("uri");
            if (!uri.startsWith("http://dbpedia.org/ontology/") || Character.isLowerCase(uri.charAt(28)))
                continue;
            quesLinkList.add(new Link(mapping.getString("label"), uri, LinkEnum.TYPE,
                    mapping.getString("mappedBy"), mapping.getString("seq")));
        }
    }

    public static List<Link> getGoldenTypeLinkBySerialNumber(int serialNumber) {
        List<Link> res = new ArrayList<>();
        JSONObject ansArray = getAnsArrayAt(serialNumber);
        if (ansArray != null) {
            JSONArray relationMapping = ansArray.getJSONArray(getTypeMappingKey());
            getGoldenTypeLinkForQues(res, relationMapping);
        }

        return res;
    }

    public static List<String> getGoldenTypeLinkURIBySerialNumber(int serialNumber) {
        return getGoldenTypeLinkBySerialNumber(serialNumber).stream().map(Link::getUri).distinct().collect(Collectors.toList());
    }

    public static String getTypeMappingKey() {
        if (QAArgs.getDataset() == DatasetEnum.LC_QUAD)
            return "predicate mapping";
        return null;
    }

    /**
     * Get the most similar golden relation link for this node string
     *
     * @param nodeStr       node string from EDG node
     * @param goldenLinking the list of golden relation linking
     * @return the most similar golden relation link
     */
    @Nullable
    public static Link getPotentialGoldenRelationLink(@NotNull String nodeStr, List<Link> goldenLinking) {
        if (goldenLinking == null || goldenLinking.isEmpty())
            return null;
        double maxScore = -1;
        Link res = null;
        for (Link link : goldenLinking) {
            if (link.getType() == LinkEnum.RELATION) {

                double curScore = Double.max(SimilarityUtil.getScore(nodeStr, link.getMention()),
                        SimilarityUtil.getScoreIgnoreCase(nodeStr, link.getMention()));

                if (nodeStr.contains(link.getMention())) {
                    curScore = 1.0;
                }
                if (curScore > maxScore) {
                    maxScore = curScore;
                    res = link;
                }
            }
        }
        if (maxScore > 0)
            return res;
        return null;
    }


    /**
     * Get the most similar golden Entity link for this node string
     *
     * @param nodeStr       node string from EDG node
     * @param goldenLinking the list of golden linking
     * @return the most similar golden entity link
     */
    @Nullable
    public static Link getPotentialGoldenEntityLink(@NotNull String nodeStr, List<Link> goldenLinking) {
        if (goldenLinking == null || goldenLinking.isEmpty())
            return null;
        double maxScore = -1;
        Link res = null;
        for (Link link : goldenLinking) {
            if (link.getType() == LinkEnum.ENTITY) {
                double curScore = Double.max(SimilarityUtil.getScore(nodeStr, link.getMention()),
                        SimilarityUtil.getScoreIgnoreCase(nodeStr, link.getMention()));
                if (curScore > maxScore) {
                    maxScore = curScore;
                    res = link;
                }
            }
        }
        if (res != null) {//A golden linking is only used once
            goldenLinking.remove(res);
        }
        if (maxScore > 0)
            return res;
        return null;
    }


    /**
     * Get the entity/relation/type golden linking list by serial number of a question.
     * Designed for dataset QALD-7
     *
     * @param serialNumber a serial number of a question
     * @return the entity/relation/type golden linking list
     */
    public static List<Link> getGoldenLinkBySerialNumber(int serialNumber) {
        List<Link> res = new ArrayList<>();
        JSONObject quesJSONObject = getAnsArrayAt(serialNumber);
        assert quesJSONObject != null;
        JSONArray mappingArray = quesJSONObject.getJSONArray("mapping");
        for (int j = 0; j < mappingArray.length(); j++) {
            JSONObject mappingObj = mappingArray.getJSONObject(j);
            res.add(new Link(mappingObj.getString("label"), mappingObj.getString("uri"), LinkEnum.UNKNOWN));
        }
        return res;
    }
}
