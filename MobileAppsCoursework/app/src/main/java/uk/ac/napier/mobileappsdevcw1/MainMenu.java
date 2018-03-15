package uk.ac.napier.mobileappsdevcw1;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toolbar;


//=============================
//Author: Mark Pereira 40286471
//Last Modified: 15/03/18 10:52am
//=============================
public class MainMenu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        setTitle("Main Menu");


        Button btOpenScrollActivity = (Button) findViewById(R.id.main_menu_BT_openBrowser);
        {
            btOpenScrollActivity.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {

                    Intent toBrowser = new Intent(MainMenu.this, ImageSelect.class);
                    startActivity(toBrowser);
                }
            });
        }

        Button btOpenHelp = (Button) findViewById(R.id.main_menu_BT_openHelp);
        {
            btOpenHelp.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {

                    Intent toHelp = new Intent(MainMenu.this, view_help.class);
                    startActivity(toHelp);
                }
            });
        }

        Button btOpenAbout = (Button) findViewById(R.id.main_menu_BT_openAbout);
        {
            btOpenAbout.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {

                    Intent toAbout = new Intent(MainMenu.this, AboutActivity.class);
                    startActivity(toAbout);
                }
            });
        }


    }
}
