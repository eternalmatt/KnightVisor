
package com.visor.knight;

import android.app.Activity;
import android.content.Intent;

/* Ideally, I'd like something a little more graceful
 * than this implementation, but this actually works
 * pretty well.
 * 
 * Problem is: Ethereal Dialpad needs my activity to be 
 * declared with his intent-filter. No problem there, I 
 * CAN put that with KnightVisorActivity, but I would also
 * like a nice description in the Ethereal Dialpad listing.
 * Ethereal Dialpad grabs the intent-filter's Activity's
 * label and so does the Home Launcher to use as a title.
 * Since I want a different home-launcher-title than 
 * ethereal-dialpad-description, I created a different 
 * listing in the manifest for the intent-filter.
 * 
 * Simple enough, but the manifest officially doesn't
 * recognize an Activity being listed twice. Hence, I have
 * a dummy here to foward to the main.
 */

public class ForwardingDummy extends Activity {

    @Override
    protected void onStart() {
        super.onStart();
        startActivity(new Intent(this, KnightVisorActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        onBackPressed();
    }

}
