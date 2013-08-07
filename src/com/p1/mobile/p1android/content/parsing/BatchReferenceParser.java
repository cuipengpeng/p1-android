package com.p1.mobile.p1android.content.parsing;

import java.util.Iterator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.p1.mobile.p1android.content.BatchReferences;

public class BatchReferenceParser {
    public static final String TAG = BatchReferenceParser.class.getSimpleName();

    public static final String SHARES = "shares";
    public static final String PICTURES = "pictures";
    public static final String TAGS = "tags";
    public static final String ID = "id";
    public static final String BATCH_ID = "batch_id";

    /**
     * 
     * @param batchReferenceJson
     *            the json element "batch_references"
     * @return
     */
    public static BatchReferences parseBatchReferences(
            JsonElement batchReferenceJson) {
        BatchReferences references = new BatchReferences();
        JsonObject json = batchReferenceJson.getAsJsonObject();
        JsonArray jsonArr = json.getAsJsonArray(PICTURES);
        Iterator<JsonElement> iterator = jsonArr.iterator();
        while (iterator.hasNext()) {
            JsonObject idChange = iterator.next().getAsJsonObject();
            references.getPictureIdChanges().put(
                    idChange.getAsJsonPrimitive(BATCH_ID).getAsString(),
                    idChange.getAsJsonPrimitive(ID).getAsString());
        }
        jsonArr = json.getAsJsonArray(SHARES);
        iterator = jsonArr.iterator();
        while (iterator.hasNext()) {
            JsonObject idChange = iterator.next().getAsJsonObject();
            references.getShareIdChanges().put(
                    idChange.getAsJsonPrimitive(BATCH_ID).getAsString(),
                    idChange.getAsJsonPrimitive(ID).getAsString());
        }
        jsonArr = json.getAsJsonArray(TAGS);
        iterator = jsonArr.iterator();
        while (iterator.hasNext()) {
            JsonObject idChange = iterator.next().getAsJsonObject();
            references.getTagIdChanges().put(
                    idChange.getAsJsonPrimitive(BATCH_ID).getAsString(),
                    idChange.getAsJsonPrimitive(ID).getAsString());
        }

        return references;
    }
}
