package com.soos.intelliwater.rest;

import org.json.JSONArray;

public interface GraphDataListener extends GenericErrorListener{
  void onResult(JSONArray jsonArray);
}
