package es.kcobos.puntogpsqr;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    final Context context = this;
    private Activity activity = this;

    private static final int QR_SCANER_INTENT =32;
    private Button scanBtn;
    private TextView contentTxt;

    private LocationManager manager;
    private static final int MAP_INTENT=33;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        checkgps();

        scanBtn = (Button)findViewById(R.id.scan_button);
        contentTxt = (TextView)findViewById(R.id.scan_content);

        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkgps()) {
                    Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                    intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
                    startActivityForResult(intent, QR_SCANER_INTENT);
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), "Debe de habilitar el GPS", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
    }

    private boolean checkgps(){
        boolean b = true;

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            final String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;
            String message = "Enable either GPS or any other location"
                    + " service to find current location.  Click OK to go to"
                    + " location services settings to let you do so.";

            builder.setMessage(message)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int id) {
                                    activity.startActivity(new Intent(action));
                                    d.dismiss();
                                }
                            })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int id) {
                                    d.cancel();
                                }
                            });
            builder.create().show();
            b = false;
        }
        return b;
    }

    protected void onResume() {
        super.onResume();

    }

    protected void onPause() {
        super.onPause();

    }



    @SuppressLint("InlinedApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case QR_SCANER_INTENT:
                if (resultCode == Activity.RESULT_OK) {
                    String contents = data.getStringExtra("SCAN_RESULT");
                    String[] sp = contents.split("_");

                    contentTxt.setText("Latitud = "+sp[1] + "\nLongitud = "+sp[3]);

                    // Create a Uri from an intent string. Use the result to create an Intent.
                    Uri gmmIntentUri = Uri.parse("google.navigation:q="+sp[1]+","+sp[3]+"&mode=w");
                    // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    // Make the Intent explicit by setting the Google Maps package
                    mapIntent.setPackage("com.google.android.apps.maps");

                    // Attempt to start an activity that can handle the Intent
                    startActivityForResult(mapIntent, MAP_INTENT);

                } else if (resultCode == Activity.RESULT_CANCELED) {
                    Toast toast = Toast.makeText(getApplicationContext(), "No se ha escaneado ningún QR", Toast.LENGTH_SHORT);
                    toast.show();
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
