package com.soos.intelliwater;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.soos.intelliwater.rest.RESTClient;
import com.soos.intelliwater.rest.ServerConfigListener;
import com.soos.intelliwater.socket.SocketService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements ServerConfigListener {

  private static final String TAG = MainActivity.class.getName();

  @BindView(R.id.progressBar)
  ContentLoadingProgressBar loadingProgressBar;

  @BindView(R.id.activity_main_retry_button)
  AppCompatButton retryButton;

  @BindView(R.id.activity_main_layout)
  ConstraintLayout mainLayout;

  @BindView(R.id.activity_main_status_text)
  TextView statusTextView;

  @BindView(R.id.activity_main_textInputEditText)
  TextInputEditText textInputEditText;

  private RESTClient restClient;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);
    restClient = RESTClient.getInstance();
    textInputEditText.setRawInputType(Configuration.KEYBOARD_12KEY);
  }

  public void onTryAgainClick(View view) {
    textInputEditText.setEnabled(false);
    String serverAddress = textInputEditText.getText().toString();
    if(serverAddress.isEmpty() || !validate(serverAddress)){
      Snackbar.make(mainLayout, R.string.label_enter_valid_server_address, Snackbar.LENGTH_LONG).show();
      return;
    }
    restClient.setServerAddress(serverAddress);
    SocketService.setServerIp(serverAddress);
    restClient.getSocketPort(this);
    retryButton.setVisibility(View.INVISIBLE);
    loadingProgressBar.show();
    statusTextView.setText(R.string.label_connecting);
  }

  private static final Pattern PATTERN_IPV4 = Pattern.compile(
      "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

  public static boolean validate(@NonNull final String ip) {
    return PATTERN_IPV4.matcher(ip).matches();
  }

  @Override
  public void onError(String message) {
    Log.e(TAG, "[onError] " + getString(R.string.label_no_server_config) + ": " + message);
    showError(message);
  }

  private void showError(String message) {
    runOnUiThread(() -> {
      textInputEditText.setEnabled(true);
      retryButton.setVisibility(View.VISIBLE);
      Snackbar snackbar =
          Snackbar.make(mainLayout, R.string.label_no_server_config, Snackbar.LENGTH_LONG);
      snackbar.setAction(R.string.label_try_again, this::onTryAgainClick);
      loadingProgressBar.hide();
      snackbar.show();
      statusTextView.setText(message);
    });
  }

  @Override
  public void onConfig(JSONObject config) {
    try {
      int port = config.getInt(PlaceholderFragment.JSON_KEY_PORT);
      SocketService.setServerPort(port);
      Intent configIntent = new Intent(MainActivity.this, ConfigActivity.class);
      startActivity(configIntent);
      Log.i(TAG, "[onConfig] Config found. Redirect to main application");
    } catch (JSONException e) {
      Log.e(TAG, "[onConfig] " + e);
      showError(e.getMessage());
    }
  }
}
