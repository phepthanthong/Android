package com.example.trunghieu.snow;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.PersistableBundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;



public class MainActivity extends ActionBarActivity {

    public static final String TAG_OUVERTURE = "ouverte";
    public static final String TAG_TEMPS = "temps";
    public static final String TAG_TEMPSMATIN = "temperatureMatin";
    public static final String TAG_TEMPMIDI = "temperatureMidi";
    public static final String TAG_VENT = "vent";
    public static final String TAG_NEIGE = "neige";

    TextView _temps;
    TextView _tempsMatin;
    TextView _tempsMidi;
    TextView _vent;
    TextView _neige;
    EditText _text;
    Button btn;
    Button btnVibrate;
    Button btnLocate;
    Button btnSelect;
    ImageView _img;


    String nomStation;
    String leTemps;

    ProgressDialog progress;

    Vibrator vibrator;
    boolean estVibre;

    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    String resultatJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _temps = (TextView)findViewById(R.id.img_desc);
        _tempsMatin = (TextView)findViewById(R.id.tempsMatin_deg);
        _tempsMidi = (TextView)findViewById(R.id.tempsMidi_deg);
        _vent = (TextView)findViewById(R.id.vent_dis);
        _neige = (TextView)findViewById(R.id.niveau_dis);
        _img = (ImageView)findViewById(R.id.imgWeather);
        btnVibrate = (Button)findViewById(R.id.btnVibration);
        btnLocate = (Button)findViewById(R.id.btnLocation);
        btnSelect = (Button)findViewById(R.id.btnSelectionner);
        btn = (Button) findViewById(R.id.btnSearch);
        _text = (EditText)findViewById(R.id.inputField);
        preferences = getSharedPreferences("MaPreference",MODE_PRIVATE);
        resultatJson = "";

        estVibre = true;
        btn.setEnabled(false);
        nomStation = _text.getText().toString();
        _text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkFieldsForEmpty();
            }

            @Override
            public void afterTextChanged(Editable s) {
                btn.setOnClickListener(new View.OnClickListener() {
                    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
                    public void onClick(View v) {
                        editor = preferences.edit();
                        String nameStation = nomStation;
                        StringBuilder s = new StringBuilder();
                        s.append("station");
                        editor.putString(s.toString(), nameStation).apply();

                        String url = "http://snowlabri.appspot.com/snow?station" + nomStation;
                        GetWeatherTask request = new GetWeatherTask();
                        request.execute(url);
                        hideSoftKeyboard(MainActivity.this);
                    }
                });
            }
        });
        /**
         * Ajouter l'événément click sur le bouton
         */
        btnVibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                estVibre = !estVibre;
                if (estVibre) {
                    btnVibrate.setText("Desactiver");
                } else {
                    btnVibrate.setText("Activer");
                }
            }
        });

        btnLocate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation();
            }
        });

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectionner();
            }
        });
    }

    /**
     * Méthode servant de sauvegarder les données de la page
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("TEMPS", (String) _temps.getText());
        outState.putString("NEIGE", (String) _neige.getText());
        outState.putString("VENT", (String) _vent.getText());
        outState.putString("TEMPSMATIN", (String) _tempsMatin.getText());
        outState.putString("TEMPSMIDI", (String) _tempsMidi.getText());
        outState.putBoolean("VIBRATION", estVibre);
        outState.putString("JSON",resultatJson);
    }

    /**
     * Méthode servant de restaurer les données lors du changement de l'orientation
     * @param savedInstanceState
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            super.onRestoreInstanceState(savedInstanceState);
            //setContentView();

            _neige.setText(savedInstanceState.getString("NEIGE"));
            _vent.setText(savedInstanceState.getString("VENT"));
            _tempsMatin.setText(savedInstanceState.getString("TEMPSMATIN"));
            _tempsMidi.setText(savedInstanceState.getString("TEMPSMIDI"));
            resultatJson = savedInstanceState.getString("JSON");

            // Charger la vibration
            estVibre = savedInstanceState.getBoolean("VIBRATION");
            if (!estVibre)
                btnVibrate.setText("Activer");
            else {
                btnVibrate.setText("Deactiver");
            }

            // Charger l'image et sa description
            _temps.setText(savedInstanceState.getString("TEMPS"));
            leTemps = (String)_temps.getText();
            switch (leTemps) {
                case "beau": {
                    _temps.setText(leTemps);
                    _img.setImageResource(R.drawable.sunny_256);
                    break;
                }
                case "neige": {
                    _temps.setText(leTemps);
                    _img.setImageResource(R.drawable.snowy_256);
                    break;
                }
                case "couvert": {
                    _temps.setText(leTemps);
                    _img.setImageResource(R.drawable.cloudy_256);
                    break;
                }
                default: {
                    break;
                }
            }
        }
    }

    public void checkFieldsForEmpty(){
        String input = _text.getText().toString();
        if (input.length() == 0)
            btn.setEnabled(false);
        else
            btn.setEnabled(true);
    }

    public void getLocation(){

        Uri format = Uri.parse("geo:0,0?q=(Bordeaux)");
        startActivity(new Intent(Intent.ACTION_VIEW, format));
    }

    public void selectionner(){
        Intent intent = new Intent(MainActivity.this, StationListActivity.class);
        MainActivity.this.startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            _text.setText(data.getStringExtra("selected"));
        }
    }

    class GetWeatherTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = new ProgressDialog(MainActivity.this);
            progress.setTitle("In progress");
            progress.setMessage("Loading");
            progress.show();
        }

        @Override
        protected String doInBackground(String... uri) {
            // Methode 1
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response;
            String responseString = null;
            try {
                response = httpclient.execute(new HttpGet(uri[0]));
                StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    responseString = out.toString();
                    out.close();
                } else {
                    //Closes the connection.
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
            }catch (Exception e) {
                e.printStackTrace();
                //Toast.makeText(getApplicationContext(), "Mauvaise requete", Toast.LENGTH_SHORT).show();
            }
            Log.i("HTTP", responseString);
            return responseString;

            // Methode 2
            /*HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet(uri[0]);

            String result = null;
            try {
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity entity = response.getEntity();

                if (entity != null) {
                    InputStream inputstream = entity.getContent();
                    BufferedReader bufferedreader =
                            new BufferedReader(new InputStreamReader(inputstream));
                    StringBuilder stringbuilder = new StringBuilder();

                    String currentline = null;
                    while ((currentline = bufferedreader.readLine()) != null) {
                        stringbuilder.append(currentline + "\n");
                    }
                    result = stringbuilder.toString();
                    Log.v("HTTP REQUEST", result);
                    inputstream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;*/
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //Do anything with response..
            progress.dismiss();
            updateWidgets(result);
            if (estVibre){
                vibrator  = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(600);
            }
        }
    }

    /**
     * Cacher le clavier lorsqu'on clique en dehors du champs de texte
     * @param activity
     */
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    public void updateWidgets(String response){
        try {
            JSONObject jObject = new JSONObject(response);

            String ouverte = jObject.getString(TAG_OUVERTURE);
            String temps = jObject.getString(TAG_TEMPS);
            String tempsMatin = jObject.getString(TAG_TEMPSMATIN);
            String tempsMidi = jObject.getString(TAG_TEMPMIDI);
            String vent = jObject.getString(TAG_VENT);
            String neige = jObject.getString(TAG_NEIGE);

            // Mettre  a jour l'image et sa description
            _temps = (TextView)findViewById(R.id.img_desc);
            _img = (ImageView)findViewById(R.id.imgWeather);
            switch (temps) {
                case "beau": {
                    _temps.setText(temps);
                    _img.setImageResource(R.drawable.sunny_256);
                    break;
                }
                case "neige": {
                    _temps.setText(temps);
                    _img.setImageResource(R.drawable.snowy_256);
                    break;
                }
                case "couvert": {
                    _temps.setText(temps);
                    _img.setImageResource(R.drawable.cloudy_256);
                    break;
                }
                default:
            }

            // Mettre a jour les autres informations
            //_tempsMatin = (TextView)findViewById(R.id.tempsMatin_deg);
           // _tempsMidi = (TextView)findViewById(R.id.tempsMidi_deg);

           // _neige = (TextView)findViewById(R.id.niveau_dis);
           // _vent = (TextView)findViewById(R.id.vent_dis);

            _tempsMatin.setText(tempsMatin);
            _tempsMidi.setText(tempsMidi);

            _neige.setText(neige);
            _vent.setText(vent);

        } catch (JSONException e) {
            e.printStackTrace();
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
}
