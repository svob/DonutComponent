package cz.fsvoboda.donut;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;

import cz.fsvoboda.donut.donut.Donut;
import cz.fsvoboda.donut.donut.DonutPage;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Donut donut = (Donut) findViewById(R.id.donut);
        LayoutInflater inflater = LayoutInflater.from(this);
        donut.addPage(new DonutPage(inflater.inflate(R.layout.page_layout, null), 700, 350));
        DonutPage d = new DonutPage(inflater.inflate(R.layout.page_layout2, null), 800, 400);
        d.setColor(Color.parseColor("#FFFF00CC"));
        donut.addPage(d);
        donut.addPage(new DonutPage(inflater.inflate(R.layout.page_layout, null), 700, 650));
    }
}
