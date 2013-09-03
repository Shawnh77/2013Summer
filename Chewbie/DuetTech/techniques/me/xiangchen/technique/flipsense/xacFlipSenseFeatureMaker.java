package me.xiangchen.technique.flipsense;

import me.xiangchen.app.duettech.DuetTech;
import me.xiangchen.app.duettech.DuetTechExtension;
import me.xiangchen.lib.xacAccelerometer;
import me.xiangchen.network.xacUDPTask;

public class xacFlipSenseFeatureMaker {
	
	public final static int FLIPTIMEOUT = 750; // ms
	public final static int UNKNOWN = -1;
	public final static int NOFLIP = 0;
	public final static int FLIP = 1;
	
	static final int MAXNUMROW = 1024;
	static final int NUMSOURCES = 2;
	
	static String[] featureNames = null;
	static double[][] featureTablePhone = null;
	static double[][] featureTableWatch = null;
	static int pntrEntryPhone = 0;
	static int pntrEntryWatch = 0;
	static int numFeatures = 0;
	static String tag = "FeatureMaker";
	static int was = -1;
	static int recognizedAs = -1;
	
	static xacAccelerometer accelWatch;
	static xacAccelerometer accelPhone;
	static xacAccelerometer[] accels;
	
	final static int NUMROWSFLIPGESTURE = DuetTech.PHONEACCELFPSGAME
			* FLIPTIMEOUT / 1000;
	
	/**
	 * create a table of features, including the first row (the names of the attributes)
	 */
	public static void createFeatureTable()
	{
		numFeatures = xacAccelerometer.NUMACCELAXES;
		featureTablePhone = new double[MAXNUMROW][numFeatures + 1];
		featureTableWatch = new double[MAXNUMROW][numFeatures + 1];
		pntrEntryPhone = 0;
		pntrEntryWatch = 0;
		
		accelWatch = new xacAccelerometer();
		accelPhone = new xacAccelerometer();
		accels = new xacAccelerometer[]{accelWatch, accelPhone};
	}
	
	/**
	 * add a row in the feature table
	 * @param features
	 * @param val
	 */
	public static void addPhoneFeatureEntry()
	{
		if(pntrEntryPhone >= MAXNUMROW) {
			pntrEntryPhone = 0;
		}
		
		int idxFeat = 0;
//		for (int i = 0; i < NUMSOURCES; i++) {
		featureTablePhone[pntrEntryPhone][idxFeat++] = accelPhone.getX();
		featureTablePhone[pntrEntryPhone][idxFeat++] = accelPhone.getY();
		featureTablePhone[pntrEntryPhone][idxFeat++] = accelPhone.getZ();
//		}

//		featureTable[pntrEntry][numFeatures] = label;
				
//		Log.d(tag, "The " + (pntrEntry+1) + "th entry added");
		pntrEntryPhone++;
	}
	
	public static void addWatchFeatureEntry()
	{
		if(pntrEntryWatch >= MAXNUMROW) {
			pntrEntryWatch = 0;
		}
		
		int idxFeat = 0;
//		for (int i = 0; i < NUMSOURCES; i++) {
		featureTableWatch[pntrEntryWatch][idxFeat++] = accelWatch.getX();
		featureTableWatch[pntrEntryWatch][idxFeat++] = accelWatch.getY();
		featureTableWatch[pntrEntryWatch][idxFeat++] = accelWatch.getZ();
//		}

//		featureTable[pntrEntry][numFeatures] = label;
				
//		Log.d(tag, "The " + (pntrEntry+1) + "th entry added");
		pntrEntryWatch++;
	}
	
	public static void setLabels(int lb, int ras) {
		was = lb;
		recognizedAs = ras;
	}
	
	public static void updateWatchAccel(float[] values) {
		if(accelWatch == null) return;
		accelWatch.update(values[0], values[1], values[2]);
	}
	
	public static void updatePhoneAccel(float[] values) {
		if(accelPhone == null) return;
		accelPhone.update(values[0], values[1], values[2]);
	}
	
	public static boolean sendOffData() {
		int lockedPntrEntryPhone = pntrEntryPhone;
		int lockedPntrEntryWatch = pntrEntryWatch;
		int numToSendPhone = NUMROWSFLIPGESTURE;
		int numToSendWatch = numToSendPhone * DuetTechExtension.WATCHACCELFPS / DuetTech.PHONEACCELFPSGAME;
		
		if(was < 0 || numToSendPhone > lockedPntrEntryPhone || numToSendWatch > lockedPntrEntryWatch) 
			return false;
		
		String strFeatureRow = "";
		
		// 1. the phone's
		for(int i=lockedPntrEntryPhone-numToSendPhone; i<lockedPntrEntryPhone; i++) {
			for(int j=0; j<numFeatures; j++) {
				strFeatureRow += String.format("%.2f", featureTablePhone[i][j]) + ",";
			}
		}
		
		// 2. the watch's
		for(int i=lockedPntrEntryWatch-numToSendWatch; i<lockedPntrEntryWatch; i++) {
			for(int j=0; j<numFeatures; j++) {
				strFeatureRow += String.format("%.2f", featureTableWatch[i][j]) + ",";
			}
		}
		
		String[] classLabels = {"NoFlip", "Flip"};
		strFeatureRow += classLabels[was] + "," + classLabels[recognizedAs] + "\0";
		
		new xacUDPTask().execute(strFeatureRow);
		
		return true;
	}
	
	public static Object[] getFlattenedData(int numToSend) {
		int lockedPntrEntryPhone = pntrEntryPhone;
		int lockedPntrEntryWatch = pntrEntryWatch;
		int numToSendPhone = numToSend;
		int numToSendWatch = numToSendPhone * DuetTechExtension.WATCHACCELFPS / DuetTech.PHONEACCELFPSGAME;
		
		if(numToSendPhone > lockedPntrEntryPhone || numToSendWatch > lockedPntrEntryWatch) 
			return null;
		
		Object[] flattened = new Object[(numToSendPhone + numToSendWatch) * numFeatures];
		int idxFeature = 0;
		
		// 1. the phone's
		for(int i=lockedPntrEntryPhone-numToSendPhone; i<lockedPntrEntryPhone; i++) {
			for(int j=0; j<numFeatures; j++) {
				flattened[idxFeature++] = featureTablePhone[i][j];
			}
		}
		
		// 2. the watch's
		for(int i=lockedPntrEntryWatch-numToSendWatch; i<lockedPntrEntryWatch; i++) {
			for(int j=0; j<numFeatures; j++) {
				flattened[idxFeature++] = featureTableWatch[i][j];
			}
		}
		
		return flattened;
	}
	
	public static boolean isDataReady() {
		int lockedPntrEntryPhone = pntrEntryPhone;
		int lockedPntrEntryWatch = pntrEntryWatch;
		int numToSendPhone = NUMROWSFLIPGESTURE;
		int numToSendWatch = numToSendPhone * DuetTechExtension.WATCHACCELFPS / DuetTech.PHONEACCELFPSGAME;
		
		if(numToSendPhone > lockedPntrEntryPhone || numToSendWatch > lockedPntrEntryWatch) 
			return false;
		
		return true;
	}
	
	public static void clearData() {
		featureTablePhone = new double[MAXNUMROW][numFeatures + 1];
		featureTableWatch = new double[MAXNUMROW][numFeatures + 1];
		pntrEntryPhone = 0;
		pntrEntryWatch = 0;
	}
	
	public static int calculateFlipGesture() {
		int label = xacFlipSenseFeatureMaker.NOFLIP;

		Object[] features = xacFlipSenseFeatureMaker
				.getFlattenedData(NUMROWSFLIPGESTURE);

		int idxClass = -1;
		try {
			idxClass = (int) FlipSenseClassifier.classify(features);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		switch (idxClass) {
		case 0:
			label = xacFlipSenseFeatureMaker.FLIP;
			// Log.d(LOGTAG, "pad");
			break;
		case 1:
			label = xacFlipSenseFeatureMaker.NOFLIP;
			// Log.d(LOGTAG, "side");
			break;

		}

		return label;
	}

}
