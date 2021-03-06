package androidapp.batru.dubaothoitiet;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import adapter.DailyAdapter;
import model.DailyItem;
import model.SingletonClass;
import model.VolleySingleton;

public class MainActivity extends AppCompatActivity {

    private static final String VolleyTAG = "MYTAG";
    private static final String PREF_NAME = "MyPref";

    private static final String mainUrl = "http://api.openweathermap.org/data/2.5/weather?q=";
    private static final String dailyUrl = "http://api.openweathermap.org/data/2.5/forecast/daily?q=";
    private static String soNgayLink = "&cnt=7";
    private static final String apiLink = "&appid=50c4705f25a93cb530a067bd324c4c8d";
    private String cityName = "Ha%20Noi";

    private TextView tvCity, tvCurrentMain, tvHumidity, tvCurrentDeg;
    private TextView tvWindSpeed, tvSunrise, tvSunset, tvLastUpdated;
    private ImageView imgIcon;

    private ListView listView;
    private ArrayList<DailyItem> ds;
    private DailyAdapter adapter;

    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        anhXa();
    }

    private void anhXa() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                menuItemClicked(item);
                return true;
            }
        });

        tvCity = (TextView) findViewById(R.id.cityText);
        tvCurrentMain = (TextView) findViewById(R.id.tvCurrentMain);
        tvHumidity = (TextView) findViewById(R.id.tvHumidity);
        tvCurrentDeg = (TextView) findViewById(R.id.tvCurrentDeg);
        tvWindSpeed = (TextView) findViewById(R.id.tvWindSpeed);
        tvSunrise = (TextView) findViewById(R.id.tvSunrise);
        tvSunset = (TextView) findViewById(R.id.tvSunset);
        tvLastUpdated = (TextView) findViewById(R.id.tvLastUpdated);
        imgIcon = (ImageView) findViewById(R.id.currentIconImage);

        listView = (ListView) findViewById(R.id.listview);
        ds = new ArrayList<>();
        adapter = new DailyAdapter(this, R.layout.item_listview, ds);
        listView.setAdapter(adapter);
    }

    private void menuItemClicked(MenuItem item) {
        drawer.closeDrawers();
        if (item.getItemId() == R.id.menu_detail) {
            startActivity(new Intent(MainActivity.this, DevelopedActivity.class));
        } else {
            cityName = item.getTitle().toString();
            hienThiDuLieu();
        }
        drawer.closeDrawers();
    }

    private void hienThiDuLieu() {
        String urlLink = mainUrl + cityName + apiLink;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                urlLink, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                ganDuLieu(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("BANGGG", error.getMessage());
            }
        });
        jsonObjectRequest.setTag(VolleyTAG);
        VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);

        String dailyLink = dailyUrl + cityName + soNgayLink + apiLink;
        JsonObjectRequest dailyRequest = new JsonObjectRequest(Request.Method.GET, dailyLink, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        ganDuLieuChoListView(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("BANGGG", error.getMessage());
            }
        });
        dailyRequest.setTag(VolleyTAG);
        VolleySingleton.getInstance(this).addToRequestQueue(dailyRequest);
    }

    private void ganDuLieuChoListView(JSONObject response) {
        ds.clear();
        try {
            JSONArray listArr = response.getJSONArray("list");
            for (int i = 0; i < listArr.length(); i++) {
                JSONObject obj = listArr.getJSONObject(i);
                int dt = obj.getInt("dt");
                float speed = (float) obj.getDouble("speed");
                int humidity = obj.getInt("humidity");

                JSONObject tempObj = obj.getJSONObject("temp");
                int minDeg = (int) (tempObj.getDouble("min") - 272.15f);
                int maxDeg = (int) (tempObj.getDouble("max") - 272.15f);

                JSONArray weatherArray = obj.getJSONArray("weather");
                JSONObject weatherObj = weatherArray.getJSONObject(0);
                String description = weatherObj.getString("main") + " - "
                        + weatherObj.getString("description");

                String icon = weatherObj.getString("icon");
                icon = SingletonClass.getInstance().ChangeStringIcon(icon);
                int iconId = SingletonClass.getInstance().getImageId(this, icon);

                String dayOfWeek = "";
                if (i == 0) {
                    dayOfWeek = "Today";
                } else {
                    dayOfWeek = SingletonClass.getInstance().ConvertUnixToTime(dt, "EEE");
                }
                DailyItem item = new DailyItem(dayOfWeek, dt, iconId, humidity, speed, minDeg, maxDeg, description);
                ds.add(item);
            }
            adapter.notifyDataSetChanged();
        } catch (JSONException e) {
            Log.d("BANGGG", e.getMessage());
        }
    }

    private void ganDuLieu(JSONObject respone) {
        if (respone == null)
            return;
        try {
            JSONObject windObj = respone.getJSONObject("wind");
            float speed = (float) windObj.getDouble("speed");
            tvWindSpeed.setText(speed + " m/s");

            JSONArray weatherArray = respone.getJSONArray("weather");
            JSONObject weatherObj = weatherArray.getJSONObject(0);
            String currentMain = weatherObj.getString("main");
            tvCurrentMain.setText(currentMain);

            String iconString = weatherObj.getString("icon");
            String iconRaw = SingletonClass.getInstance().ChangeStringIcon(iconString);
            int iconId = SingletonClass.getInstance().getImageId(this, iconRaw);
            imgIcon.setImageResource(iconId);

            String city = respone.getString("name");
            JSONObject sysObj = respone.getJSONObject("sys");
            String country = sysObj.getString("country");
            tvCity.setText(city + ", " + country);

            String timeLastUpdated = SingletonClass.getInstance().ConvertUnixToTime(respone.getInt("dt"), "h:mm a");
            String timeSunrise = SingletonClass.getInstance().ConvertUnixToTime(sysObj.getInt("sunrise"), "h:mm a");
            String timeSunset = SingletonClass.getInstance().ConvertUnixToTime(sysObj.getInt("sunset"), "h:mm a");
            tvSunrise.setText(timeSunrise);
            tvSunset.setText(timeSunset);
            tvLastUpdated.setText(timeLastUpdated);

            JSONObject mainObj = respone.getJSONObject("main");
            String humidity = mainObj.getString("humidity");
            tvHumidity.setText(humidity + "%");
            int deg = (int) (mainObj.getDouble("temp") - 272.15f);
            tvCurrentDeg.setText(deg + "°C");

        } catch (JSONException e) {
            Log.d("ERROR", e.getMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_setting:
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setTitle("Change City");
                final EditText edtCityInput = new EditText(MainActivity.this);
                edtCityInput.setInputType(InputType.TYPE_CLASS_TEXT);
                edtCityInput.setHint("NewYork");
                dialog.setView(edtCityInput);
                dialog.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cityName = SingletonClass.getInstance().ChuanHoaChuoi(edtCityInput.getText().toString());
                        hienThiDuLieu();
                    }
                });
                dialog.show();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //region Luu sharedPreferences
    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences pref = getSharedPreferences(PREF_NAME, 0);
        if (pref.contains("cityName")) {
            cityName = pref.getString("cityName", "Ha%20noi");
        } else {
            cityName = "Ha%20noi";
        }
        hienThiDuLieu();
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences cityPref = getSharedPreferences(PREF_NAME, 0);
        SharedPreferences.Editor editor = cityPref.edit();
        editor.putString("cityName", cityName);
        editor.apply();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //endregion
}
