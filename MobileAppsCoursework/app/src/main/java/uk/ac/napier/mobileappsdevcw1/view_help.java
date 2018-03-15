package uk.ac.napier.mobileappsdevcw1;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;


//View help Activity.
public class view_help extends AppCompatActivity {


    private int mShortAnimationDuration;
    private ImageView mImageView;
    private int viewingImage = 0;

    Integer[] imageIdList = {R.drawable.tutorial_img1,R.drawable.tutorial_img2};

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_help);

        setTitle("Help");

        mImageView = (ImageView)findViewById(R.id.helpImageView);
        mImageView.setVisibility(View.VISIBLE);


        mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_mediumAnimTime);

        mImageView.setImageResource(imageIdList[0]);
    }

    //Below code adapted from:
    //https://developer.android.com/training/animation/reveal-or-hide-view.html#Crossfade
    private void crossfadeIn() {

        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        mImageView.setAlpha(0f);
        mImageView.setVisibility(View.VISIBLE);

        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
        mImageView.animate()
                .alpha(1f)
                .setDuration(mShortAnimationDuration)
                .setListener(null);


    }

    public void crossfadeOut()
    {
        // Animate the loading view to 0% opacity. After the animation ends,
        // set its visibility to GONE as an optimization step (it won't
        // participate in layout passes, etc.)
        mImageView.animate()
                .alpha(0f)
                .setDuration(mShortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mImageView.setVisibility(View.GONE);
                    }
                });
    }

    //When user taps the image, performs a fade animation and displays the new image
    public void imageEvent(View view)
    {
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if(viewingImage==1){viewingImage=0;}else{viewingImage++;}
                        crossfadeOut();
                        mImageView.setImageResource(imageIdList[viewingImage]);
                        crossfadeIn();

                        break;
                    default:
                }
                return true;
            }
        });

    }
}
