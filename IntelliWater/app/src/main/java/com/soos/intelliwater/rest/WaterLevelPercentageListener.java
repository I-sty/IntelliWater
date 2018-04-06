package com.soos.intelliwater.rest;

import org.json.JSONArray;

public interface WaterLevelPercentageListener extends GenericErrorListener {
    void onResult(JSONArray jsonArray);
}
