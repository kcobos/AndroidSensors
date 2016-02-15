package es.kcobos.brujulavoz;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;


public class ConfiguracionActivity extends AppCompatActivity {

    final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.configuracion);
    }

    public void btnEspagnolClick(View v){
        Intent goingBack = new Intent();
        goingBack.putExtra("idioma", "es-ES");
        setResult(RESULT_OK, goingBack);
        finish();
    }

    public void btnInglesClick(View v){
        Intent goingBack = new Intent();
        goingBack.putExtra("idioma", "en-GB");
        setResult(RESULT_OK, goingBack);
        finish();
    }
}
