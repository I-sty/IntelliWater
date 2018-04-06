package com.soos.intelliwater;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.soos.intelliwater.rest.GraphDataListener;
import com.soos.intelliwater.rest.RESTClient;
import com.soos.intelliwater.rest.ServerConfigListener;
import com.soos.intelliwater.rest.WaterLevelPercentageListener;
import com.soos.intelliwater.socket.SocketService;
import com.warkiz.widget.IndicatorSeekBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceholderFragment extends Fragment {

  public static final int TOTAL_SECTION = 3;

  public static final String JSON_KEY_PORT = "port";

  /**
   * The fragment argument representing the section number for this
   * fragment.
   */
  private static final String ARG_SECTION_NUMBER = "section_number";

  private static final int SECTION_AUTO = 1;

  private static final int SECTION_MANUAL = 2;

  private static final int SECTION_GRAPH = 3;

  private static final String TAG = PlaceholderFragment.class.getName();

  private static final int DELAY_MILLIS_UPDATE_BANKS_LEVEL = 2 * 1000;

  private static final String JSON_KEY_TYPE = "type";

  private static final String JSON_KEY_PERCENTAGE = "percentage";

  private static final String JSON_KEY_VOLUME = "volume";

  private static final String JSON_KEY_HEIGHT = "height";

  private static final String JSON_KEY_TIMESTAMP = "timestamp";

  private static final String MYSQL_TIMESTAMP = "yyyy-MM-dd hh:mm:ss";

  private static final String JSON_KEY_WARNING = "warning";

  @Nullable
  private static SocketService socketService;

  private static RESTClient restClient;

  private final CompoundButton.OnCheckedChangeListener pumpStatusChanged =
      (buttonView, isChecked) -> {
        if (socketService != null) {
          socketService.setPumpStatus(isChecked);
        }
      };

  private final GraphDataListener graphDataListener = new GraphDataListener() {

    @Override
    public void onResult(JSONArray jsonArray) {
      try {
        for (int i = 0; i < jsonArray.length(); ++i) {
          JSONObject jsonObject = jsonArray.getJSONObject(i);
          int type = jsonObject.getInt(JSON_KEY_TYPE);
          double percentage = jsonObject.getDouble(JSON_KEY_PERCENTAGE);
          double volume = jsonObject.getInt(JSON_KEY_VOLUME);
          double height = jsonObject.getDouble(JSON_KEY_HEIGHT);
          Date timestamp = new SimpleDateFormat(MYSQL_TIMESTAMP, Locale.getDefault())
              .parse(jsonObject.getString(JSON_KEY_TIMESTAMP));
          //entries.add(new Entry())
        }
      } catch (JSONException | ParseException e) {
        Log.e(TAG, "[graphDataListener - onResult] " + e);
      }
    }

    @Override
    public void onError(String message) {
      Log.e(TAG, "[graphDataListener - onError] " + message);
    }
  };

  private NumberProgressBar fragmentAutoBank1LevelPercentage;

  private IndicatorSeekBar.OnSeekBarChangeListener demandWaterLevelChanged =
      new IndicatorSeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(IndicatorSeekBar seekBar, int progress, float progressFloat,
            boolean fromUserTouch) {
        }

        @Override
        public void onSectionChanged(IndicatorSeekBar seekBar, int thumbPosOnTick,
            String textBelowTick, boolean fromUserTouch) {

        }

        @Override
        public void onStartTrackingTouch(IndicatorSeekBar seekBar, int thumbPosOnTick) {

        }

        @Override
        public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
          if (socketService != null) {
            socketService.setSollLevel(seekBar.getProgress());
          }
        }
      };

  @Nullable
  private Handler mHandler;

  private Switch pumpStatusSwitch;

  private final CompoundButton.OnCheckedChangeListener advancedModeChanged =
      new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
          toggleItems(isChecked);
          if (socketService != null) {
            socketService.setAdvancedMode(isChecked);
          }
        }
      };

  private TextView fragmentManualTankLevel;

  private TextView fragmentManualSourceLevel;

  private LineChart lineChart;

  private List<Entry> entries = new ArrayList<>();

  private NumberProgressBar fragmentAutoSourceLevelPercentage;

  private final WaterLevelPercentageListener waterLevelListener =
      new WaterLevelPercentageListener() {

        @Override
        public void onResult(JSONArray jsonArray) {
          try {
            for (int i = 0; i < jsonArray.length(); ++i) {
              JSONObject jsonObject = jsonArray.getJSONObject(i);
              int type = jsonObject.getInt(JSON_KEY_TYPE);
              double percentage = jsonObject.getDouble(JSON_KEY_PERCENTAGE);
              switch (type) {
                case 1: {
                  if (getActivity() == null) {
                    return;
                  }
                  getActivity().runOnUiThread(
                      () -> fragmentAutoBank1LevelPercentage.setProgress((int) percentage));
                  break;
                }
                case 2: {
                  if (getActivity() == null) {
                    return;
                  }
                  getActivity().runOnUiThread(
                      () -> fragmentAutoSourceLevelPercentage.setProgress((int) percentage));
                  break;
                }
                default: {
                  Log.w(TAG, "[onResult] Invalid type");
                }
              }
            }
          } catch (JSONException e) {
            Log.e(TAG, "[onResult]" + e);
          }
        }

        @Override
        public void onError(String message) {

        }
      };

  private final Runnable updateWaterLevel = new Runnable() {

    @Override
    public void run() {
      restClient.getWaterInfo(waterLevelListener);
      if (mHandler != null) {
        mHandler.postDelayed(updateWaterLevel, DELAY_MILLIS_UPDATE_BANKS_LEVEL);
      }
    }
  };

  private TextView lastWarningMessageTextView;

  private final ServerConfigListener lastWarningMessage = new ServerConfigListener() {

    @Override
    public void onConfig(JSONObject config) {
      try {
        String timestamp = config.getString(JSON_KEY_WARNING);
        if (getActivity() != null) {
          getActivity().runOnUiThread(() -> lastWarningMessageTextView.setText(timestamp));
        }
      } catch (JSONException e) {
        Log.e(TAG, "[lastWarningMessage - onConfig] " + e);
      }
    }

    @Override
    public void onError(String message) {
      Log.e(TAG, "[lastWarningMessage - onError] " + message);
    }
  };

  private TextView waterSourceStatusTextView;

  private WaterLevelPercentageListener manualFragmentWaterInfo =
      new WaterLevelPercentageListener() {

        @Override
        public void onResult(JSONArray jsonArray) {
          try {
            for (int i = 0; i < jsonArray.length(); ++i) {
              JSONObject jsonObject = jsonArray.getJSONObject(i);
              int type = jsonObject.getInt(JSON_KEY_TYPE);
              double percentage = jsonObject.getDouble(JSON_KEY_PERCENTAGE);
              double volume = jsonObject.getInt(JSON_KEY_VOLUME);
              double height = jsonObject.getDouble(JSON_KEY_HEIGHT);
              String result = (int) percentage + "%" + "\n" +
                  String.format(Locale.getDefault(), "%1$,.1f l\n %2$,.1f cm", volume, height);
              switch (type) {
                case 1: {
                  if (getActivity() == null) {
                    return;
                  }
                  getActivity().runOnUiThread(() -> fragmentManualTankLevel.setText(result));
                  break;
                }
                case 2: {
                  if (getActivity() == null) {
                    return;
                  }
                  getActivity().runOnUiThread(() -> {
                    fragmentManualSourceLevel.setText(result);
                    if (percentage < 10) {
                      waterSourceStatusTextView.setText(R.string.label_too_low);
                    } else {
                      waterSourceStatusTextView.setText(R.string.label_ok);
                    }
                  });
                  break;
                }
                default: {
                  Log.w(TAG, "[onResult] Invalid type");
                }
              }
            }
          } catch (JSONException e) {
            Log.e(TAG, "[manualFragmentWaterInfo - onResult] " + e);
          }
        }

        @Override
        public void onError(String message) {
          Log.e(TAG, "[manualFragmentWaterInfo - onError] " + message);
        }
      };

  private final Runnable updateWaterInfo = new Runnable() {

    @Override
    public void run() {
      restClient.getWaterInfo(manualFragmentWaterInfo);
      restClient.getLastWarningMessage(lastWarningMessage);
      if (mHandler != null) {
        mHandler.postDelayed(updateWaterInfo, DELAY_MILLIS_UPDATE_BANKS_LEVEL);
      }
    }
  };

  public static void setSocketService(@Nullable SocketService socketService) {
    PlaceholderFragment.socketService = socketService;
  }

  /**
   * Returns a new instance of this fragment for the given section
   * number.
   */
  public static PlaceholderFragment newInstance(int sectionNumber) {
    PlaceholderFragment fragment = new PlaceholderFragment();
    Bundle args = new Bundle();
    args.putInt(ARG_SECTION_NUMBER, sectionNumber);
    fragment.setArguments(args);
    restClient = RESTClient.getInstance();
    return fragment;
  }

  private void toggleItems(boolean isChecked) {
    if (!isChecked) {
      if (socketService != null) {
        pumpStatusSwitch.setChecked(false);
      }
    }
    pumpStatusSwitch.setEnabled(isChecked);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    stopAutoRepeatingTask();
    stopManualRepeatingTask();
  }

  private void startAutoRepeatingTask() {
    if (mHandler == null) {
      mHandler = new Handler();
    }
    updateWaterLevel.run();
  }

  private void stopAutoRepeatingTask() {
    if (mHandler != null) {
      mHandler.removeCallbacks(updateWaterLevel);
    }
  }

  private void startManualRepeatingTask() {
    if (mHandler == null) {
      mHandler = new Handler();
    }
    updateWaterInfo.run();
  }

  private void stopManualRepeatingTask() {
    if (mHandler != null) {
      mHandler.removeCallbacks(updateWaterInfo);
    }
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    Bundle args = getArguments();
    if (args == null) {
      return null;
    }

    int sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);
    switch (sectionNumber) {
      case SECTION_AUTO: {
        stopManualRepeatingTask();
        View rootView = inflater.inflate(R.layout.fragment_auto, container, false);
        IndicatorSeekBar fragmentAutoDemandWaterLevel =
            rootView.findViewById(R.id.fragment_auto_demand_water_level);
        fragmentAutoBank1LevelPercentage =
            rootView.findViewById(R.id.fragment_auto_bank1_level_percentage);
        fragmentAutoDemandWaterLevel.setOnSeekChangeListener(demandWaterLevelChanged);
        fragmentAutoSourceLevelPercentage =
            rootView.findViewById(R.id.fragment_auto_source_level_percentage);
        startAutoRepeatingTask();
        return rootView;
      }
      case SECTION_MANUAL: {
        stopAutoRepeatingTask();
        View rootView = inflater.inflate(R.layout.fragment_manual, container, false);
        Switch advancedModeSwitch =
            rootView.findViewById(R.id.fragment_manual_advanced_mode_switch);
        advancedModeSwitch.setOnCheckedChangeListener(advancedModeChanged);
        pumpStatusSwitch = rootView.findViewById(R.id.fragment_manual_pump_status_switch);
        pumpStatusSwitch.setOnCheckedChangeListener(pumpStatusChanged);
        fragmentManualTankLevel = rootView.findViewById(R.id.fragment_manual_tank_level);
        fragmentManualSourceLevel = rootView.findViewById(R.id.fragment_manual_source_level);
        lastWarningMessageTextView = rootView.findViewById(R.id.textView13);
        waterSourceStatusTextView = rootView.findViewById(R.id.textView7);
        startManualRepeatingTask();
        return rootView;
      }
      case SECTION_GRAPH: {
        stopManualRepeatingTask();
        stopAutoRepeatingTask();
        View rootView = inflater.inflate(R.layout.fragment_graph, container, false);
        lineChart = rootView.findViewById(R.id.chart);
        restClient.getGraphData(graphDataListener, 20);
        return rootView;
      }
      default: {
        Log.w(TAG, "[onCreateView] Invalid section number: " + sectionNumber);
        return null;
      }
    }
  }
}
