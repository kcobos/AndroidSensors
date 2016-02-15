package es.kcobos.PuntoGestosFoto;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
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

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    final Context context = this;

    private final int CONFIG_CODE = 144;

    private float lastX, lastY, lastZ;

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private float deltaX = 0;
    private float deltaY = 0;
    private float deltaZ = 0;

    private float vibrateThreshold = 0;

    private TextView tv;
    private ImageView img;
    private MediaPlayer reproSonido;
    private int movimiento = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // success! we have an accelerometer

            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            vibrateThreshold = accelerometer.getMaximumRange() / 4;
        } else {
            finish();
        }
        img = (ImageView) findViewById(R.id.imageView);
        tv = (TextView) findViewById(R.id.textView);
        reproSonido = MediaPlayer.create(MainActivity.this, R.raw.moneda);
    }

    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // get the change of the x,y,z values of the accelerometer
        deltaX = Math.abs(lastX - event.values[0]);
        deltaY = Math.abs(lastY - event.values[1]);
        deltaZ = Math.abs(lastZ - event.values[2]);

        // if the change is below 2, it is just plain noise
        if (deltaX < 2)
            deltaX = 0;
        if (deltaY < 2)
            deltaY = 0;

        // set the last know values of x,y,z
        lastX = event.values[0];
        lastY = event.values[1];
        lastZ = event.values[2];

        switch (movimiento){
            case 0:
                if (deltaY > vibrateThreshold && deltaX < vibrateThreshold && deltaZ < vibrateThreshold) {
                    movimiento = 1;
                    tv.setText(R.string.txt2);
                    img.setImageResource(R.drawable.flecha2);
                    //reproSonido.start();
                }
                break;
            case 1:
                if (deltaY < vibrateThreshold && deltaX > vibrateThreshold && deltaZ < vibrateThreshold) {
                    movimiento = 0;
                    tv.setText(R.string.txt);
                    img.setImageResource(R.drawable.flecha);
                    reproSonido.start();
                }
                break;
        }

    }

    // if the change in the accelerometer value is big enough, then vibrate!
    // our threshold is MaxValue/2
    public void sound() {
        if (deltaY > vibrateThreshold && deltaX < vibrateThreshold && deltaZ < vibrateThreshold) {
            MediaPlayer reproSonido = MediaPlayer.create(MainActivity.this, R.raw.moneda);
            reproSonido.start();
        }
    }
    @SuppressLint("InlinedApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case CONFIG_CODE:
                if (resultCode == RESULT_OK) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Idioma cambiado", Toast.LENGTH_SHORT);
                    toast.show();
                }

        }
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
