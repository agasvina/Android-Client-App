package com.harry.storq.ui;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.harry.storq.R;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

public class GeocodeTest extends Activity {
	
	protected static final String TAG = "JSONDATA";
	double Latitude = 52.232314;
	double Longitude = 0.150037;
	TextView test;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_geocode_test);
		
		
		
		test = (TextView) findViewById(R.id.textView1);
		 getForecast( Latitude,  Longitude);
		 
		
		
	}

	
	  private void getForecast(double latitude, double longitude) {
	      
			String forecastUrl = "http://maps.googleapis.com/maps/api/geocode/json?latlng="+latitude+ ","+ longitude +"&sensor=true&types=(cities)";

	        if (isNetworkAvailable()) {
	          //  toggleRefresh();

	            OkHttpClient client = new OkHttpClient();
	            Request request = new Request.Builder()
	                    .url(forecastUrl)
	                    .build();

	            Call call = client.newCall(request);
	            call.enqueue(new Callback() {
	                @Override
	                public void onFailure(Request request, IOException e) {
	                    runOnUiThread(new Runnable() {
	                        @Override
	                        public void run() {
	                          //  toggleRefresh();
	                        }
	                    });
	                  //  alertUserAboutError();
	                }

	                @Override
	                public void onResponse(Response response) throws IOException {
	                    runOnUiThread(new Runnable() {
	                        @Override
	                        public void run() {
	                      //      toggleRefresh();
	                        }
	                    });

	                    try {
	                        String jsonData = response.body().string();
	                        Log.v("JSONADD", jsonData);
	                        
	                        JSONObject forecast = new JSONObject(jsonData);
	                        JSONArray array = forecast.getJSONArray("results");
	                        String alamat = array.getJSONObject(0).getString("formatted_address");
	                        JSONArray x = array.getJSONObject(0).getJSONArray("address_components");
	                        int size = x.length();
	                        String country = "";
	                        int i = 0;
	                        while(i  < size) {
	                       JSONArray xarr = x.getJSONObject(i).getJSONArray("types");//(name)
	                       String s = xarr.getString(0);
	                       if (s.equalsIgnoreCase("country") || s.equalsIgnoreCase("postal_town")) {
		                        country += x.getJSONObject(i).getString("short_name") + " ";	   
	                       }
	                       i++;
	                        }
	                       
	                        Log.v("JSONADD", alamat);
	                        Log.v("JSONADD", country);


	                    
	                        
	                        
	                        //test.setText(jsonData);
	                        if (response.isSuccessful()) {
	                           // mCurrentWeather = getCurrentDetails(jsonData);
	                            runOnUiThread(new Runnable() {
	                                @Override
	                                public void run() {
	                             //       updateDisplay();
	                                }
	                            });
	                        } else {
	                          //  alertUserAboutError();
	                        }
	                    }
	                    catch (IOException e) {
	                        Log.e("IOEXCLOC", "Exception caught: ", e);
	                    } catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	                }
	            });
	        }
	        else {
	            Toast.makeText(this, "AAARGH",
	                    Toast.LENGTH_LONG).show();
	        }
	    }


	
	

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }

        return isAvailable;
    }
    
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.geocode_test, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
