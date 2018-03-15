package uk.ac.napier.mobileappsdevcw1;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;


//=============================
//Author: Mark Pereira 40286471
//Last Modified: 15/03/18 10:35am
//=============================
//Image browser activity. At current stage, this is static.
public class ImageSelect extends AppCompatActivity {


    //Image browser list view Images
    String[] imageList = {"Eiffel","Colosseum","Prescott Park"};
    Integer[] imageIdList = {R.drawable.eiffel,R.drawable.colloseum,R.drawable.prescott_park};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_select);

        ArrayAdapter adapter = new ArrayAdapter<String>(this,R.layout.image_layout_text_view,imageList);

        //Create list view as image browser
        final ListView listView = (ListView) findViewById(R.id.imageList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selection = (String) (listView.getItemAtPosition(i));
                Toast.makeText(getApplicationContext(),selection,Toast.LENGTH_SHORT).show();

                Intent toCanvasPage = new Intent(ImageSelect.this, CanvasPage.class);
                toCanvasPage.putExtra("key",imageIdList[i]);
                startActivity(toCanvasPage);
            }



        });


    }
}
