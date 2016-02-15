package es.kcobos.brujulavoz;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    final Context context = this;

    // Brujula
    private ImageView compass;
    private ImageView arrow;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;
    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];
    private float mCurrentDegree = 0;
    private float azimuthInDegress;
    private float direccion;
    private float destinoG;
    private long startTime = 0;

    // Voz
    Button bntEmp;
    ListView listView;
    private int numberRecoResults = 2;
    private String languageModel = RecognizerIntent.LANGUAGE_MODEL_FREE_FORM;
    private static int ASR_CODE = 123;
    private String idioma;

    private int CONFIG_CODE = 144;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Brujula
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        compass = (ImageView) findViewById(R.id.imgCompass);
        arrow = (ImageView) findViewById(R.id.imgDireccion);

        direccion = 0;

        //Voz
        idioma = "es-ES";
        bntEmp = (Button) findViewById(R.id.btnEmpezar);
        bntEmp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                arrow.setVisibility(View.INVISIBLE);
                //Speech recognition does not currently work on simulated devices,
                //it the user is attempting to run the app in a simulated device
                //they will get a Toast
                if ("generic".equals(Build.BRAND.toLowerCase())) {
                    Toast toast = Toast.makeText(getApplicationContext(), "ASR is not supported on virtual devices", Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    listen();                //Set up the recognizer with the parameters and start listening
                }
            }
        });

        listView = (ListView) findViewById(R.id.listaReconocidos);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String texto = listView.getItemAtPosition(position).toString().toUpperCase();
                String[] palabras = texto.split(" ");
                if (palabras.length > 3) {
                    Toast toast = Toast.makeText(getApplicationContext(), "No has introducido bien dirección", Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    switch (palabras[0]) {
                        case "NORTE":
                        case "NORTH":
                            destinoG = 0;
                            break;
                        case "ESTE":
                        case "EAST":
                            destinoG = 90;
                            break;
                        case "SUR":
                        case "SOUTH":
                            destinoG = 180;
                            break;
                        case "OESTE":
                        case "WEST":
                            destinoG = 270;
                            break;
                    }
                    int signo = 1;
                    if (palabras.length == 3) {
                        switch (palabras[1]) {
                            case "MENOS":
                            case "LESS":
                                signo = -1;
                                break;
                        }
                        destinoG += signo * Double.parseDouble(palabras[2]);
                    } else if (palabras.length == 2)
                        destinoG += signo * Double.parseDouble(palabras[1]);
                }
                direccion = (destinoG - azimuthInDegress) % 360;

                RotateAnimation ra1;
                if (direccion > 180) {
                    ra1 = new RotateAnimation(
                            180,
                            direccion,
                            Animation.RELATIVE_TO_SELF, 0.5f,
                            Animation.RELATIVE_TO_SELF,
                            0.5f);
                } else {
                    ra1 = new RotateAnimation(
                            2,
                            direccion,
                            Animation.RELATIVE_TO_SELF, 0.5f,
                            Animation.RELATIVE_TO_SELF,
                            0.5f);
                }

                ra1.setDuration(250);

                ra1.setFillAfter(true);

                arrow.startAnimation(ra1);

                arrow.setVisibility(View.VISIBLE);
                listView.setVisibility(View.INVISIBLE);
            }
        });
    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);
        arrow.setVisibility(View.INVISIBLE);
        direccion = 180;
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this, mAccelerometer);
        mSensorManager.unregisterListener(this, mMagnetometer);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == mAccelerometer) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            mLastAccelerometerSet = true;
        } else if (event.sensor == mMagnetometer) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(mR, mOrientation);
            float azimuthInRadians = mOrientation[0];
            azimuthInDegress = (float) (Math.toDegrees(azimuthInRadians) + 360) % 360;
            ArrayList<String> aal = new ArrayList<String>();
            /*aal.add("mcur = "+mCurrentDegree);
            aal.add("azi = "+azimuthInDegress);
            setListView(aal);*/
            RotateAnimation ra = new RotateAnimation(
                    mCurrentDegree,
                    -azimuthInDegress,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f);

            ra.setDuration(250);

            ra.setFillAfter(true);

            compass.startAnimation(ra);

            if(arrow.getVisibility() == View.VISIBLE){
                RotateAnimation ra1 = new RotateAnimation(
                        direccion,
                        (destinoG - azimuthInDegress)%360,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF,
                        0.5f);

                ra1.setDuration(250);

                ra1.setFillAfter(true);

                arrow.startAnimation(ra1);
                direccion = destinoG - azimuthInDegress;
            }

            mCurrentDegree = -azimuthInDegress;

        }

        /*Toast toast = Toast.makeText(getApplicationContext(), "dirección = "+direccion, Toast.LENGTH_SHORT);
        toast.show();*/

        if (arrow.getVisibility() == View.VISIBLE && (direccion+1)%360 < 5 && (direccion-1)%360 > -5){
            /*Toast toast = Toast.makeText(getApplicationContext(), "dif tiempo"+(System.currentTimeMillis() - startTime), Toast.LENGTH_SHORT);
            toast.show();*/
            if ((System.currentTimeMillis() - startTime) > 500 && (System.currentTimeMillis() - startTime) < 1000) {
                arrow.clearAnimation();
                arrow.setVisibility(View.INVISIBLE);
                direccion = 180;
                startTime = 0;

                final Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.orientado);
                dialog.setTitle("Bien!!");
                dialog.show();
            } else if (System.currentTimeMillis() - startTime > 1500) {
                startTime = System.currentTimeMillis();
                /*toast = Toast.makeText(getApplicationContext(), "reiniciando tiempo", Toast.LENGTH_SHORT);
                toast.show();*/
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub
    }

    /**
     * Initializes the speech recognizer and starts listening to the user input
     */
    private void listen() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        // Specify language model
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, languageModel);
        // Specify how many results to receive
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, numberRecoResults);
        // Especificar el idioma para reconocimiento
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, idioma);
        // Start listening
        startActivityForResult(intent, ASR_CODE);
    }

    @SuppressLint("InlinedApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ASR_CODE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    //Retrieves the N-best list and the confidences from the ASR result
                    ArrayList<String> nBestList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    float[] nBestConfidences = null;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)  //Checks the API level because the confidence scores are supported only from API level 14
                        nBestConfidences = data.getFloatArrayExtra(RecognizerIntent.EXTRA_CONFIDENCE_SCORES);

                    //Creates a collection of strings, each one with a recognition result and its confidence
                    //following the structure "Phrase matched (conf: 0.5)"
                    ArrayList<String> nBestView = new ArrayList<String>();

                    for (int i = 0; i < nBestList.size(); i++) {
                        if (nBestConfidences != null) {
                            if (nBestConfidences[i] >= 0)
                                nBestView.add(nBestList.get(i));
                            else
                                nBestView.add(nBestList.get(i) + " (no confidence value available)");
                        } else
                            nBestView.add(nBestList.get(i) + " (no confidence value available)");
                    }

                    //Includes the collection in the ListView of the GUI
                    setListView(nBestView);
                }
            }
        } else if (requestCode == CONFIG_CODE){
            if (resultCode == RESULT_OK) {
                Toast toast = Toast.makeText(getApplicationContext(), "Idioma cambiado", Toast.LENGTH_SHORT);
                toast.show();
                idioma = data.getStringExtra("idioma");
            }
        }
    }

    /**
     * Includes the recognition results in the list view
     *
     * @param nBestView list of matches
     */
    private void setListView(ArrayList<String> nBestView) {

        // Instantiates the array adapter to populate the listView
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, nBestView);
        listView.setAdapter(adapter);
        listView.setVisibility(View.VISIBLE);
    }

    // MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    // Capturar acciones en el menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.creditos:
                muestraDialogo(R.id.creditos);
                break;
            case R.id.configuracion:
                Intent getNameScreenIntent = new Intent(this, ConfiguracionActivity.class);
                getNameScreenIntent.putExtra("callingActivity", "MainActivity");
                startActivityForResult(getNameScreenIntent, CONFIG_CODE);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    // Mostrar cuadros de dialogo
    private void muestraDialogo(int dia){
        final Dialog dialog = new Dialog(context);

        switch (dia) {
            case R.id.creditos:
                dialog.setContentView(R.layout.creditos);
                dialog.setTitle("Créditos");
                TextView tv = (TextView) dialog.findViewById(R.id.TVcreditos);
                tv.setText(Html.fromHtml(getResources().getString(R.string.creditosTxt)));
                dialog.show();
                break;
        }
    }

}
