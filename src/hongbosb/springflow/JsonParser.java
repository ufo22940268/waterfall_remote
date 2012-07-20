package hongbosb.springflow;

import org.json.*;
import java.util.List;
import java.util.ArrayList;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

public class JsonParser {
    private JSONTokener mTokener ;

    public JsonParser() {
        mTokener = new JSONTokener(loadContent());
    }

    public String[] getImageUrls() {
        List<String> list = null;
        try {
            JSONObject jo = new JSONObject(mTokener);
            JSONArray jArray = jo.getJSONArray("pics");
            list = new ArrayList<String>();
            for (int i = 0; i < jArray.length(); i ++) {
                list.add(jArray.getString(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String[] strs = new String[list.size()];
        return list.toArray(strs);
    }

    public String loadContent() {
        try {
            URL url = new URL(Config.SERVER_IP);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            String result = "";
            String line = reader.readLine();
            while (line != null) {
                result += line + "\n";
                line = reader.readLine();
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
