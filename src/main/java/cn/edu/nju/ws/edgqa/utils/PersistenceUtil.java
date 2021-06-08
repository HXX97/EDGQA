package cn.edu.nju.ws.edgqa.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class PersistenceUtil {
    public static void writeLcQuadQuestions() throws IOException {
        BufferedWriter quesWriter = new BufferedWriter(new FileWriter(new File("output/lcquad_question_all.txt")));
        JSONArray sparqlArray = new JSONArray(FileUtil.readFileAsString("src/main/resources/datasets/lcquad-all.json"));
        for (int quesIdx = 0; quesIdx < sparqlArray.length(); ++quesIdx) { // for each question

            JSONObject jsonObject = sparqlArray.getJSONObject(quesIdx);
            String question = jsonObject.getString("corrected_question");  // the original question
            quesWriter.write(question + "\r\n");
            quesWriter.flush();
        }
    }
}
