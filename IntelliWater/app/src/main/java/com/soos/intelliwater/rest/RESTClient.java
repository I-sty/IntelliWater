package com.soos.intelliwater.rest;


import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class RESTClient {

  private static final String NO_RESPONSE = "No response";

  private static final String PROTOCOL = "http";

  private static final String CUSTOM_FOLDER = "/intelliwater";

  private static final String TAG = RESTClient.class.getName();

  private static final MediaType MEDIA_TYPE_JSON =
      MediaType.parse("application/json; charset=utf-8");

  private static final String JSON_KEY_PERIOD = "period";

  private static String SERVER_ADDRESS;

  private static RESTClient selfInstance;

  private final OkHttpClient okHttpClient;

  private RESTClient() {
    okHttpClient = new OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build();
  }

  public static RESTClient getInstance() {
    if (selfInstance == null) {
      selfInstance = new RESTClient();
    }
    return selfInstance;
  }

  /**
   * Generate a full path
   *
   * @param path
   *     The relative path
   *
   * @return The full path to the server
   */
  private static String getServerAddress(String path) {
    String url = PROTOCOL + "://" + SERVER_ADDRESS + CUSTOM_FOLDER + path;
    Log.i(TAG, "[getServerAddress] Generated url: " + url);
    return url;
  }

  public void getWaterInfo(WaterLevelPercentageListener listener) {
    String url = getServerAddress("/water/info");
    Request request = new Request.Builder().url(url).build();
    okHttpClient.newCall(request).enqueue(new Callback() {

      @Override
      public void onFailure(Call call, IOException e) {
        listener.onError(e.getMessage());
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        if (response.isSuccessful()) {
          ResponseBody body = response.body();
          if (body == null) {
            listener.onError(NO_RESPONSE);
            return;
          }
          try {
            listener.onResult(new JSONArray(body.string()));
          } catch (JSONException e) {
            Log.e(TAG, "[getWaterInfo - onResponse] " + e);
            listener.onError(NO_RESPONSE);
          }
        } else {
          listener.onError(response.message());
        }
        response.close();
      }
    });
  }

  public void getSocketPort(ServerConfigListener listener) {
    String url = getServerAddress("/config");
    Request request = new Request.Builder().url(url).build();
    okHttpClient.newCall(request).enqueue(new Callback() {

      @Override
      public void onFailure(Call call, IOException e) {
        listener.onError(e.getMessage());
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        if (response.isSuccessful()) {
          ResponseBody body = response.body();
          if (body == null) {
            listener.onError(NO_RESPONSE);
            return;
          }
          try {
            listener.onConfig(new JSONObject(body.string().trim()));
          } catch (JSONException e) {
            Log.e(TAG, "[getSocketPort - onResponse] " + e);
            listener.onError(NO_RESPONSE);
          }
        } else {
          listener.onError(response.message());
        }
        response.close();
      }
    });
  }

  public void setServerAddress(String serverAddress) {
    SERVER_ADDRESS = serverAddress;
  }

  public void getGraphData(GraphDataListener listener, int period) {
    String url = getServerAddress("/graph");
    JSONObject json = new JSONObject();
    try {
      json.put(JSON_KEY_PERIOD, period);
    } catch (JSONException e) {
      Log.e(TAG, "[getGraphData] cannot create json: " + e);
    }
    Request request =
        new Request.Builder().url(url).post(RequestBody.create(MEDIA_TYPE_JSON, json.toString()))
            .build();
    okHttpClient.newCall(request).enqueue(new Callback() {

      @Override
      public void onFailure(Call call, IOException e) {
        listener.onError(e.getMessage());
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        if (response.isSuccessful()) {
          ResponseBody body = response.body();
          if (body == null) {
            listener.onError(NO_RESPONSE);
            return;
          }
          try {
            listener.onResult(new JSONArray(body.string()));
          } catch (JSONException e) {
            Log.e(TAG, "[getGraphData - onResponse] " + e);
            listener.onError(e.getMessage());
          }
        } else {
          listener.onError(NO_RESPONSE);
        }
      }
    });
  }

  public void getLastWarningMessage(ServerConfigListener listener) {
    String url = getServerAddress("/water/warning");
    Request request = new Request.Builder().url(url).build();
    okHttpClient.newCall(request).enqueue(new Callback() {

      @Override
      public void onFailure(Call call, IOException e) {
        listener.onError(e.getMessage());
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        if (response.isSuccessful()) {
          ResponseBody body = response.body();
          if (body == null) {
            listener.onError(NO_RESPONSE);
            return;
          }
          try {
            listener.onConfig(new JSONObject(body.string().trim()));
          } catch (JSONException e) {
            Log.e(TAG, "[getSocketPort - onResponse] " + e);
            listener.onError(NO_RESPONSE);
          }
        } else {
          listener.onError(response.message());
        }
        response.close();
      }
    });
  }
}
