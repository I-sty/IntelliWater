package com.soos.intelliwater.rest;

import org.json.JSONObject;

public interface ServerConfigListener extends GenericErrorListener{
    void onConfig(JSONObject config);
}
