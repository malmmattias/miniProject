package Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Observer {
    private final Map<String, ArrayList<String>> map = new HashMap<>();

    public void subscribe(String username, String newData) {
        ArrayList<String> data = map.get(username);
        if (data == null) {
            data = new ArrayList<>();
        }
        data.add(newData);
        map.put(username, data);
    }

    public ArrayList<String> notify(String data) {

        ArrayList<String> matchingUsernames = new ArrayList<>();

        for (Map.Entry<String, ArrayList<String>> entry : map.entrySet()) {
            String username = entry.getKey();
            ArrayList<String> userInterestsList = entry.getValue();

            if (userInterestsList.contains(data)) {
                matchingUsernames.add(username);
            }
        }

        return matchingUsernames;
    }
}
