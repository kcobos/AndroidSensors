package es.kcobos.PuntoSorpresa;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    final Context context = this;

    private final int CONFIG_CODE = 144;

    private SensorManager mSensor;
    private TextView texto;
    private int sensor;
    private LocationManager lm;
    private Location location;
    private GeomagneticField campo;

    private MediaPlayer reproSonido;
    private ProgressBar mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        sensor = Sensor.TYPE_MAGNETIC_FIELD;
        campo = new GeomagneticField((float) location.getLatitude(), (float) location.getLongitude(),
                (float) location.getAltitude(), location.getTime());

        texto = (TextView) findViewById(R.id.text);
        mSensor = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor.registerListener(this, mSensor.getDefaultSensor(sensor),SensorManager.SENSOR_DELAY_GAME);

        reproSonido = MediaPlayer.create(MainActivity.this, R.raw.sonido);
        mProgress = (ProgressBar) findViewById(R.id.progressBar);
        mProgress.setScaleY(10f);

    }

    protected void onResume() {
        super.onResume();
        mSensor.registerListener(this, mSensor.getDefaultSensor(sensor), SensorManager.SENSOR_DELAY_GAME);
    }

    protected void onPause() {
        super.onPause();
        mSensor.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == sensor)
        {
            float v0 = event.values[0];
            float v1 = event.values[1];
            float v2 = event.values[2];

            float v0_campo = campo.getX();
            float v1_campo = campo.getY();
            float v2_campo = campo.getX();

            double mag = Math.sqrt(Math.pow(v0, 2) + Math.pow(v1, 2) + Math.pow(v2, 2));
            double mag_campo = Math.sqrt(Math.pow(v0_campo, 2)+ Math.pow(v1_campo, 2) + Math.pow(v2_campo, 2));

            if ((mag*1000 < mag_campo*0.5) || (mag*1000 > mag_campo*1.5))
            {
                texto.setTextSize(18);
                texto.setText(String.format("%.0f", mag) + " µT");
                if (mag > 75) {
                    reproSonido.start();
                }

            }
            else
            {
                texto.setTextSize(12);
                texto.setText("Buscando metal...");
            }
            int p = (int)mag;
            if (p>mProgress.getMax())
                p=mProgress.getMax();
            mProgress.setProgress(p);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @SuppressLint("InlinedApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){

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
