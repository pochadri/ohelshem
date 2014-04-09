/*
 * ******************************************************************************
 *   Copyright (c) 2013-2014 Nicolas POMEPUY.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *  *****************************************************************************
 */
package fr.nicolaspomepuy.discreetapprate;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

public class ExceptionHandler implements Thread.UncaughtExceptionHandler {

    private final SharedPreferences settings;
    private final SharedPreferences.Editor editor;
    private Thread.UncaughtExceptionHandler defaultExceptionHandler;

	// Constructor.
	@SuppressLint("CommitPrefEdits")
	public ExceptionHandler(Thread.UncaughtExceptionHandler uncaughtExceptionHandler, Context context)
	{
        settings = context.getSharedPreferences(AppRate.PREFS_NAME, 0);
        editor = settings.edit();
		defaultExceptionHandler = uncaughtExceptionHandler;
	}

	public void uncaughtException(Thread thread, Throwable throwable) {

		editor.putLong(AppRate.KEY_LAST_CRASH, System.currentTimeMillis()).commit();

		// Call the original handler.
		defaultExceptionHandler.uncaughtException(thread, throwable);
	}
}