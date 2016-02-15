package es.kcobos.PuntoGestosFoto;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements GestureOverlayView.OnGesturePerformedListener {

    final Context context = this;

    // Componentes gesture library para el reconocimiento de gestos
    private GestureOverlayView gesture;
    private GestureLibrary gLibrary;

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private Uri fileUri;
    private final String dir = Environment.getDataDirectory() + "/" + Environment.DIRECTORY_DCIM;// + "/Patron/";

    private boolean crear;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gesture = (GestureOverlayView)findViewById(R.id.gestureOverlayView1);
        gesture.addOnGesturePerformedListener(this);
        gLibrary = GestureLibraries.fromRawResource(this, R.raw.gestures);
        gLibrary.load();

        crear = true;
        findViewById(R.id.textView2).setVisibility(View.VISIBLE);
        gesture.setGestureColor(Color.parseColor("#FFFF0000"));

    }

    protected void onResume() {
        super.onResume();
    }

    protected void onPause() {
        super.onPause();
    }

    /**
     * Reconocimiento gestual
     */
    @Override
    public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture1) {
        if (crear){
            gLibrary.removeEntry("t");
            gLibrary.addGesture("t", gesture1);
            gLibrary.save();
            Toast.makeText(this, "Gesto guardado", Toast.LENGTH_SHORT).show();
            crear = false;
            findViewById(R.id.textView2).setVisibility(View.INVISIBLE);
            gesture.setGestureColor(Color.parseColor("#2551ff"));
        } else {
            ArrayList<Prediction> predictions = gLibrary.recognize(gesture1);

            if (predictions.size() > 0) {
                Prediction prediction = predictions.get(0);

                if (prediction.score > 5) {

                    if (predictions.get(0).name.contains("t")) {
                        // create Intent to take a picture and return control to the calling application
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                        String file = dir + getCode() + ".jpg";
                        File mi_foto = new File( file );
                        fileUri = Uri.fromFile(mi_foto); // create a file to save the image
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

                        // start the image capture Intent
                        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);

                    } else { // No se ha renocido un gesto
                        Toast.makeText(this, "Gesto no reconocido.", Toast.LENGTH_SHORT).show();
                    }
                } else { // No supera el 10% de fiabilidad
                    Toast.makeText(this, "Gesto no reconocido.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * Metodo privado que genera un codigo único para la fotografía segun la fecha y hora del sistema
     * @return photoCode
     **/
    @SuppressLint("SimpleDateFormat")
    private String getCode(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_hhmmss");
        String date = dateFormat.format(new Date() );
        String photoCode = "img_" + date;
        return photoCode;
    }


    @SuppressLint("InlinedApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    // Image captured and saved to fileUri specified in the Intent
                    Toast.makeText(this, "Image saved to:\n" +
                            data.getData(), Toast.LENGTH_LONG).show();
                } else if (resultCode == RESULT_CANCELED) {
                    // User cancelled the image capture
                } else {
                    // Image capture failed, advise user
                }
                break;
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
                crear = true;
                findViewById(R.id.textView2).setVisibility(View.VISIBLE);
                gesture.setGestureColor(Color.parseColor("#FFFF0000"));
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
