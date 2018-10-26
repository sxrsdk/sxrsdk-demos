package com.samsungxr.sample.sxrjavascript;

import android.util.Log;

/**
 * Utility class for scripts.
 * 
 * public members in this class can be accessed in scripts
 * after adding as a global variable.
 *
 * <pre>
 * SXRScriptManager sm = getSXRContext().getScriptManager();
 *
 * sm.addVariable("utils", new ScriptUtils());
 * </pre>
 */
public class ScriptUtils {
	private static final String TAG = ScriptUtils.class.getSimpleName();

	public void log(String msg) {
		Log.i(TAG, msg);
	}
}