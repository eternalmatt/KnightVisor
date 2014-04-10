package com.visor.knight.extra;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

import com.visor.knight.R;

public class EtherealDialpadMarketAlert extends AlertDialog.Builder {

    public EtherealDialpadMarketAlert(final Context context) {
        super(context);
        setTitle(R.string.alertTitle);
        setMessage(R.string.alertMessage);
        setPositiveButton(R.string.alertPositiveButton,
            new DialogInterface.OnClickListener() {
               @Override public void onClick(DialogInterface dialog, int which) {
                    Uri url = Uri.parse(context.getString(R.string.etherealDialpadURL));
                    Intent intent = new Intent(Intent.ACTION_VIEW, url);
                    context.startActivity(intent);
                }
            });
        setNegativeButton(R.string.alertNegativeButton, null);
    }
    
    public static void show(final Context context) {
        new EtherealDialpadMarketAlert(context).show();
    }

}
