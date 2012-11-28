package com.maptime.maptime;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

public class DismissListener implements OnClickListener{

	public void onClick(DialogInterface dialog, int which) {
		dialog.dismiss();
		
	}

}
