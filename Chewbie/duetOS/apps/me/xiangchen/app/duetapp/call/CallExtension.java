package me.xiangchen.app.duetapp.call;

import java.util.Hashtable;

import me.xiangchen.app.duetapp.AppExtension;
import me.xiangchen.technique.posturesense.xacPostureSenseFeatureMaker;
import me.xiangchen.technique.sharesense.xacShareSenseFeatureMaker;

import com.sonyericsson.extras.liveware.aef.control.Control;

public class CallExtension extends AppExtension {

	int posture;
	final String[] appExtensions = {"Contact", "Email", "Calendar"};
	int idxAppExts = 0;
	Hashtable<String, Integer> htIdxItems;
	
	public CallExtension() {
		
		CallManager.setWatch(this);
		htIdxItems = new Hashtable<String, Integer>();
		for(String appExt : appExtensions) {
			htIdxItems.put(appExt, 0);
		}
	}
	
	@Override
	public void doResume() {
		showText("Call");
	}
	
	@Override
	public void doAccelerometer(float[] values) {
		xacPostureSenseFeatureMaker.updateWatchAccel(values);
		xacPostureSenseFeatureMaker.addWatchFeatureEntry();
	}
	
	@Override
	public void doSwipe(int direction) {
		posture = xacPostureSenseFeatureMaker.calculatePosture();
		if(posture != xacPostureSenseFeatureMaker.WATCH) {
			return;
		}
		
		switch (direction) {
		case Control.Intents.SWIPE_DIRECTION_UP:
			buzz(100);
			CallManager.playLastVoiceMail();
			break;
		case Control.Intents.SWIPE_DIRECTION_DOWN:
			buzz(100);
			CallManager.playNextVoiceMail();
			break;
		}
	}
	
	public void showAppExtension() {
		String strAppItem = appExtensions[idxAppExts] + " #" + (htIdxItems.get(appExtensions[idxAppExts]) + 1);
		showText(strAppItem);
	}
	
	public void nextAppExtension() {
		idxAppExts = (idxAppExts + 1) % appExtensions.length;
		showAppExtension();
	}
	
	public void lastAppExtension() {
		idxAppExts = (idxAppExts + appExtensions.length - 1) % appExtensions.length;
		showAppExtension();
	}
	
	public void nextItem() {
		Integer idxItem = htIdxItems.get(appExtensions[idxAppExts]);
		idxItem++;
		htIdxItems.put(appExtensions[idxAppExts], idxItem);
		showAppExtension();
	}
	
	public void lastItem() {
		Integer idxItem = htIdxItems.get(appExtensions[idxAppExts]);
		idxItem--;
		idxItem = Math.max(0, idxItem);
		htIdxItems.put(appExtensions[idxAppExts], idxItem);
		showAppExtension();
	}
}
