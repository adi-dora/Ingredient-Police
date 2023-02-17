package com.example.snapchatcopy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleListAdapter {
    private String[] textMain, textSub;

    public SimpleListAdapter(String[] text1, String[] text2){
        textMain = text1;
        textSub = text2;

    }

    public List<Map<String, String>> convertToListItems() {
        final List<Map<String, String>> listItem =
                new ArrayList<Map<String, String>>(textMain.length);

        for (int i = 0; i < textMain.length; i++) {
            final Map<String, String> listItemMap = new HashMap<String, String>();
            listItemMap.put("text1", textMain[i]);
            listItemMap.put("text2", textSub[i]);
            listItem.add(Collections.unmodifiableMap(listItemMap));
        }

        return Collections.unmodifiableList(listItem);
    }
}
