package th.ac.tu.siit.its333.lab7exercise1;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;


public class MainActivity extends ActionBarActivity {
    long oldtime = System.currentTimeMillis();
    int oid = R.id.btBangkok;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        WeatherTask w = new WeatherTask();
        w.execute("http://ict.siit.tu.ac.th/~cholwich/bangkok.json", "Bangkok Weather");
    }

    public void buttonClicked(View v) {

        long currenttime;
        long timediff;
        int id = v.getId();

        WeatherTask w = new WeatherTask();
        double timediffToMin;
        switch (id) {
            case R.id.btBangkok:
                currenttime = System.currentTimeMillis();
                timediff = currenttime - oldtime;
                timediffToMin = TimeUnit.MILLISECONDS.toMinutes(timediff);
                if(timediffToMin > 1 || id != oid) {
                    w.execute("http://ict.siit.tu.ac.th/~cholwich/bangkok.json", "Bangkok Weather");
                    oldtime = System.currentTimeMillis();
                    oid=id;
                }
                break;
            case R.id.btNon:
                currenttime = System.currentTimeMillis();
                timediff = currenttime - oldtime;
                timediffToMin = TimeUnit.MILLISECONDS.toMinutes(timediff);
                if(timediffToMin > 1 || id !=oid) {
                    w.execute("http://ict.siit.tu.ac.th/~cholwich/nonthaburi.json", "Nonthaburi Weather");
                    oldtime = System.currentTimeMillis();
                    oid = id;
                }

                break;
            case R.id.btPathum:
                currenttime = System.currentTimeMillis();
                timediff = currenttime - oldtime;
                timediffToMin = TimeUnit.MILLISECONDS.toMinutes(timediff);
                if(timediffToMin > 1 || id != oid) {
                    w.execute("http://ict.siit.tu.ac.th/~cholwich/pathumthani.json", "PathumThani Weather");
                    oldtime = System.currentTimeMillis();
                    oid = id;
                }

                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class WeatherTask extends AsyncTask<String, Void, Boolean> {
        String errorMsg = "";
        ProgressDialog pDialog;
        String title;

        double windSpeed;
        String whether;
        Double temperature;
        Double tempmin;
        Double tempmax;
        int humidity;


        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Loading weather data ...");
            pDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            BufferedReader reader;
            StringBuilder buffer = new StringBuilder();
            String line;
            try {
                title = params[1];
                URL u = new URL(params[0]);
                HttpURLConnection h = (HttpURLConnection)u.openConnection();
                h.setRequestMethod("GET");
                h.setDoInput(true);
                h.connect();

                int response = h.getResponseCode();
                if (response == 200) {
                    reader = new BufferedReader(new InputStreamReader(h.getInputStream()));
                    while((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }
                    //Start parsing JSON
                    JSONObject jWeather = new JSONObject(buffer.toString());
                    JSONArray jwheth = jWeather.getJSONArray("weather");
                    whether = jwheth.getJSONObject(0).getString("main");

                    JSONObject jWind = jWeather.getJSONObject("wind");
                    windSpeed = jWind.getDouble("speed");

                    JSONObject jtemp = jWeather.getJSONObject("main");
                    temperature = jtemp.getDouble("temp")-273.15;
                    tempmin = jtemp.getDouble("temp_min")- 273.15;
                    tempmax = jtemp.getDouble("temp_max")- 273.15;
                    humidity = jtemp.getInt("humidity");


                    errorMsg = "";
                    return true;
                }
                else {
                    errorMsg = "HTTP Error";
                }
            } catch (MalformedURLException e) {
                Log.e("WeatherTask", "URL Error");
                errorMsg = "URL Error";
            } catch (IOException e) {
                Log.e("WeatherTask", "I/O Error");
                errorMsg = "I/O Error";
            } catch (JSONException e) {
                Log.e("WeatherTask", "JSON Error");
                errorMsg = "JSON Error";
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            TextView tvTitle, tvWeather, tvWind, tvTemp, tvHumid;
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }

            tvTitle = (TextView)findViewById(R.id.tvTitle);
            tvWeather = (TextView)findViewById(R.id.tvWeather);
            tvWind = (TextView)findViewById(R.id.tvWind);
            tvTemp = (TextView)findViewById(R.id.tvTemp);
            tvHumid = (TextView)findViewById(R.id.tvHumid);

            if (result) {
                tvWeather.setText(whether);
                tvTitle.setText(title);
                tvWind.setText(String.format("%.1f", windSpeed));
                tvTemp.setText(String.format("%.1f", temperature)+"(max = "+String.format("%.1f", tempmin)+", min = "+String.format("%.1f", tempmax)+")");
                tvHumid.setText(String.format("%d%%", humidity));
            }
            else {
                tvTitle.setText(errorMsg);
                tvWeather.setText("");
                tvWind.setText("");
            }
        }
    }
}
