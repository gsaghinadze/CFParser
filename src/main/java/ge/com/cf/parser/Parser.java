package ge.com.cf.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javafx.util.Pair;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
 
public class Parser {
 
    public static HashSet<String> students = new HashSet<>(
            Arrays.asList(
               "akhor16",
               "AndriaToria",
               "Brainrequired",
               "bgoga16",
               "dbejanishvili",
               "cero",  
               "gabadzeluka",
               "giorobota",
               "gogakoreli",
               "ditotet",
               "GrigalashviliT",
               "gvancacucxashvili",
               //"InDaBag",
               "Jonah28",
               "jsandro",
               "keta_tsimakuridze",
               "kvrivishvil1",
               "lashamez",
               "LukaTchumburidze",
               "LashaBukhnikashvili",
               "LTabidze",
               "lbero15",
               "matcharadze",
               "mixo",
               "mtser15",
               "n999th",
               "nikalosaberidze",
               "plyr",
               "Rezga",
               "Saba_24",
               "sano23",
               "Sheng-O",
               "SnapOutOfIt",
               "Senshi",
               "MT13",
               "tamuna_314",
               "TheScienceGuy",
               "Valer123",
               "waska.chaduneli",
               "Wikk",
               "Zoroo",
               "Schikeria"
            ));
    public static String[] contests = {
    "101726",
    "100253",
    "101778",
    "100187",
    "101755",
    "101649"
    };
 
    public static void main(String[] args) throws Exception {
        HashMap<String, HashMap<String, Integer>> res = new HashMap<>();
        HashMap<String, HashMap<String, Integer>> grades = new HashMap<>();
 
        for (String curContest : contests) {
            TimeUnit.SECONDS.sleep((long) 1);
            String contestURL = "http://codeforces.com/api/contest.standings?contestId=" + curContest + "&from=1&count=10000&showUnofficial=true";
            try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
                HttpGet httpget = new HttpGet(contestURL);
 
                System.out.print("Executing request " + httpget.getRequestLine());
 
                ResponseHandler<String> responseHandler = (final HttpResponse response) -> {
                    int status = response.getStatusLine().getStatusCode();
                    System.out.println("   status    = " + status);
                    if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();
                        return entity != null ? EntityUtils.toString(entity) : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                };
 
                ObjectMapper objectMapper = new ObjectMapper();
                String responseBody = httpclient.execute(httpget, responseHandler);
 
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                JsonNode result = jsonNode.get("result").get("rows");
 
                for (int i = 0; i < result.size(); i++) {
                    JsonNode handles = result.get(i).get("party").get("members").get(0);
                    String userName;
                    if (handles != null) {
                        userName = handles.get("handle").asText();
                        if (students.contains(userName)) {
                            for (int j = 0; j < result.get(i).get("problemResults").size(); j++) {
                                int score = result.get(i).get("problemResults").get(j).get("points").asInt();
                                if (score == 1) {
 
                                    if (!res.containsKey(userName)) {
                                        HashMap<String, Integer> map = new HashMap<>();
                                        res.put(userName, map);
                                    }
 
                                    if (!res.get(userName).containsKey(curContest)) {
                                        res.get(userName).put(curContest, 0);
                                    }
 
                                    Integer contestRes = res.get(userName).get(curContest);
                                    contestRes |= (1 << j);
                                    res.get(userName).put(curContest, contestRes);
 
                                    if (!grades.containsKey(userName)) {
                                        HashMap<String, Integer> map1 = new HashMap<>();
                                        grades.put(userName, map1);
                                    }
 
                                    if (!grades.get(userName).containsKey(curContest)) {
                                        grades.get(userName).put(curContest, 0);
                                    }
                                }
                            }
                        }
                    }
                }
 
            }
        }
 
        for (String cont : contests) {
            int maxGrade = 0;
            for (String curUser : res.keySet()) {
                HashMap<String, Integer> map = res.get(curUser);
                if (map.containsKey(cont)) {
                    maxGrade = Math.max(maxGrade, Integer.bitCount(map.get(cont)));
                }
            }
 
            for (String us : res.keySet()) {
                HashMap<String, Integer> map = res.get(us);
                HashMap<String, Integer> curGrades = grades.get(us);
 
                if (map.containsKey(cont)) {
 
                    int solved = Integer.bitCount(map.get(cont));
                    int grade = 0;
                    if (solved > 0 && solved * 100 >= maxGrade * 80) {
                        grade = 5;
                    } else if (solved > 0 && solved * 100 >= maxGrade * 65) {
                        grade = 4;
                    } else if (solved > 0 && solved * 100 >= maxGrade * 50) {
                        grade = 3;
                    } else if (solved > 0 && solved * 100 >= maxGrade * 35) {
                        grade = 2;
                    }
                    else if (solved > 0) {
                        grade = 1;
                    }
 
                    curGrades.put(cont, grade);
                }
 
                grades.put(us, curGrades);
            }
 
        }
 
        System.out.println(grades);
        System.out.println("------------------------------------------------------");
        HashMap<String, Integer> finalResult = new HashMap<>();
 
        grades.keySet().stream().forEach((usr) -> {
            List<Integer> list = new ArrayList<>(grades.get(usr).values());
            Collections.sort(list, (Integer a, Integer b) -> b.compareTo(a));
 
            int finalRes = 0;
            for (int i = 0; i < list.size() && i < 8; i++) {
                finalRes += list.get(i);
            }
 
            finalResult.put(usr, finalRes);
        });
 
        List<Pair<String, Integer>> finalRes = new ArrayList<>();
 
        finalResult.entrySet().stream().forEach((r) -> {
            finalRes.add(new Pair(r.getKey(), r.getValue()));
        });
 
        Collections.sort(finalRes, (Pair a, Pair b) -> ((Integer) b.getValue()).compareTo((Integer) a.getValue()));
 
        finalRes.stream().forEach((r) -> { System.out.println(r.getKey() + " " + r.getValue()); });
    }
}