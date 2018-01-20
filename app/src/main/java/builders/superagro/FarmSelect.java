package builders.superagro;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

;

public class FarmSelect extends FragmentActivity implements OnMapReadyCallback
{
    String BASE = "https://maps.googleapis.com/maps/api/geocode/json?language=en&latlng=";//27.362816,2080.141343

    String weatherBase= "http://api.worldweatheronline.com/premium/v1/weather.ashx?key=de7b946f483d4dd8b91111715162511&mca=yes&fx=yes&cc=no&format=json&q=";
    private GoogleMap mMap;
    Button continueButton;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farm_select);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        continueButton = (Button) findViewById(R.id.continueButton);
   /*     Intent i = new Intent(FarmSelect.this,CropPrediction.class);
        i.putExtra("AREA",areainSqMeters/1000);
        startActivity(i);*/
        continueButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                callActvity();
            }
        });
    }

    String district = "Sitapur";
    String state = "Uttar Pradesh";
    private void callActvity()
    {
        OkHttpClient okHttpClient = new OkHttpClient();

        Request request = new Request.Builder()
                .url(BASE + districtLatLn.latitude + "," + districtLatLn.longitude)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback()
                                              {
                                                  @Override
                                                  public void onFailure(Call call, IOException e)
                                                  {
                                                      e.printStackTrace();
                                                  }

                                                  @Override
                                                  public void onResponse(Call call, final Response response) throws IOException
                                                  {
                                                      if (!response.isSuccessful())
                                                      {
                                                          throw new IOException("Unexpected code " + response);
                                                      } else
                                                      {
                                                          // do something wih the result
                                                          String body = response.body().string();
                                                          Log.d(TAG, body);
                                                          try
                                                          {
                                                              JSONArray arr =  (new JSONObject(body)).getJSONArray("results")
                                                                      .getJSONObject(0)
                                                                      .getJSONArray("address_components");
                                                              int len = arr.length();
                                                              for(int i=0;i<len;i++)
                                                              {
                                                                  if(arr.getJSONObject(i).getJSONArray("types").getString(0).equals("administrative_area_level_2"))
                                                                  {
                                                                      district = arr.getJSONObject(i).getString("long_name");
                                                                  }
                                                                  else
                                                                      if(arr.getJSONObject(i).getJSONArray("types").getString(0).equals("administrative_area_level_1"))
                                                                      {
                                                                          state = arr.getJSONObject(i).getString("long_name");
                                                                      }
                                                              }


                                                              Log.d(TAG,"District = "+ district);
                                                              Log.d(TAG,"State = "+ state);
                                                              getWeather(districtLatLn);
                                                          } catch (Exception e)
                                                          {
                                                              district = "Sitapur";
                                                              state = "Uttar Pradesh";
                                                              e.printStackTrace();
                                                          }

                                                      }
                                                  }
                                              }
        );

    }

    Double temp = 24.2,precipitation = 58.15;
    private void getWeather(LatLng districtLatLn)
    {
        OkHttpClient okHttpClient = new OkHttpClient();

        Request request = new Request.Builder()
                .url(weatherBase + districtLatLn.latitude + "," + districtLatLn.longitude)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback()
                                              {
                                                  @Override
                                                  public void onFailure(Call call, IOException e)
                                                  {
                                                      e.printStackTrace();
                                                      render("");
                                                  }

                                                  @Override
                                                  public void onResponse(Call call, final Response response) throws IOException
                                                  {
                                                      if (!response.isSuccessful())
                                                      {
                                                          render("");
                                                          throw new IOException("Unexpected code " + response);
                                                      } else
                                                      {
                                                          String body = response.body().string();
                                                          Log.d(TAG,body);
                                                          // do something wih the result
                                                          render(body);

                                                      }
                                                  }
                                                  void render(String body)
                                                  {
                                                      try
                                                      {


                                                          JSONObject jsonObject = new JSONObject(body);
                                                            /*  "ClimateAverages":[
                                                              {
                                                                  "month":[
                                                                  {
                                                                      "index":"a",
                                                                          "name":"January",
                                                                          "avgMinTemp":"15.9",*/
                                                          JSONArray months = jsonObject.getJSONObject("data").getJSONArray("ClimateAverages").getJSONObject(0).getJSONArray("month");
                                                          Double tempSum = 0.0;
                                                          JSONObject m ;
                                                          int n = months.length();
                                                          for(int i = 5;i<n;i++)
                                                          {
                                                              m = months.getJSONObject(i);
                                                              tempSum+=((m.getDouble("avgMinTemp")+m.getDouble("absMaxTemp"))/2);
                                                          }
                                                          temp = tempSum/6;
                                                          Log.d(TAG,"Temperature = "+temp);
                                                      } catch (JSONException e)
                                                      {
                                                          temp = 24.2;
                                                          e.printStackTrace();
                                                      }

                                                      runOnUiThread(new Runnable()
                                                      {
                                                          @Override
                                                          public void run()
                                                          {
                                                              Toast.makeText(FarmSelect.this,district+","+state,Toast.LENGTH_LONG).show();

                                                          }
                                                      });
                                                      Intent i = new Intent(FarmSelect.this, CropPrediction.class);
                                                      i.putExtra("AREA", areainSqMeters / 1000000+"");
                                                      i.putExtra("DISTRICT", district);
                                                      i.putExtra("STATE", state);
                                                      i.putExtra("TEMP", temp);
                                                      i.putExtra("PRECIPITATION", precipitation);
                                                      startActivity(i);
                                                  }
                                              }
        );
    }

    final String TAG = "FARMSELECT";

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    double areainSqMeters = 500000;
    LatLng districtLatLn;
    ArrayList<LatLng> polygonArr = new ArrayList<>();
    Polygon polygon;

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        polygonArr.clear();
        // Add a marker in Sydney and move the camera
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener()
        {
            @Override
            public void onMapClick(LatLng latLng)
            {
                polygonArr.add(latLng);
                polygon.remove();
                PolygonOptions rectOptions = new PolygonOptions().addAll(polygonArr);
                rectOptions.fillColor(Color.parseColor("#33FF0000"));
                rectOptions.strokeColor(Color.RED);
                districtLatLn = latLng;
                mMap.addMarker(new MarkerOptions().position(latLng).title("Ramu's Khet").visible(true));
                areainSqMeters = SphericalUtil.computeArea(polygonArr);

// Get back the mutable Polygon
                polygon = mMap.addPolygon(rectOptions);
            }
        });

        // Add a marker in Sydney and move the camera
        LatLng billsFarm = new LatLng(27.549036, 80.693232);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(billsFarm, 15.2f));
        CameraPosition oldPos = mMap.getCameraPosition();

        CameraPosition pos = CameraPosition.builder(oldPos).bearing(-75).build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(pos));
        // Instantiates a new Polygon object and adds points to define a rectangle
        ArrayList<LatLng> defFarm = new ArrayList<>();
        defFarm.add(new LatLng(27.548878, 80.690394));
        defFarm.add(new LatLng(27.551128, 80.691637));
        defFarm.add(new LatLng(27.549259, 80.696193));
        defFarm.add(new LatLng(27.546970, 80.694445));
        districtLatLn = new LatLng(27.546970, 80.694445);
        PolygonOptions rectOptions = new PolygonOptions()
                .addAll(defFarm);
        areainSqMeters = SphericalUtil.computeArea(defFarm);
        Log.d(TAG, "ARea in SqKM = " + (areainSqMeters / 1000000));
        rectOptions.fillColor(Color.parseColor("#33FF0000"));
        rectOptions.strokeColor(Color.RED);

        mMap.addMarker(new MarkerOptions().position(billsFarm).title("Ramu's Khet").visible(true));


// Get back the mutable Polygon
        polygon = mMap.addPolygon(rectOptions);
    }


}
