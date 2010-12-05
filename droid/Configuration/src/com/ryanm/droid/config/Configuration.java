
package com.ryanm.droid.config;

import java.util.LinkedList;
import java.util.Queue;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.ryanm.droid.config.annote.Variable;

/**
 * Handy utility for manipulating object trees that have been marked
 * up with {@link Variable} annotations
 * 
 * @author ryanm
 */
public class Configuration
{
	/**
	 * Logging tag
	 */
	public static final String LOG_TAG = "Configuration";

	/**
	 * The activity request flag used in
	 * {@link Activity#startActivityForResult(Intent, int)} when
	 * launching the configuration activity (value = 266344)
	 */
	public static final int ACTIVITY_REQUEST_FLAG = 266344;

	private static Object[] configTargets = null;

	private static Queue<ConfigResult> deferredResults =
			new LinkedList<Configuration.ConfigResult>();

	private Configuration()
	{
	}

	/**
	 * Starts a configuration activity
	 * 
	 * @param returnTo
	 *           The activity to return to when we are done configuring
	 * @param roots
	 *           The (annotated) objects to configure. These will be
	 *           saved to use the next time
	 *           {@link #onActivityResult(int, int, Intent)} is called
	 */
	public static void configure( Activity returnTo, Object... roots )
	{
		Log.i( LOG_TAG, "Launching configuration activity" );
		configTargets = roots;
		// launch config
		Intent i = new Intent( returnTo, ConfigActivity.class );
		i.putExtra( "conf", Extract.extract( roots ).toString() );
		i.putExtra( "returnto", returnTo.getClass().getName() );
		returnTo.startActivityForResult( i, ACTIVITY_REQUEST_FLAG );
	}

	/**
	 * Call this from your activity in {@link Activity}
	 * .onActivityResult() to apply a configuration
	 * 
	 * @param requestCode
	 *           from {@link Activity}.onActivityResult()
	 * @param resultCode
	 *           from {@link Activity}.onActivityResult()
	 * @param data
	 *           from {@link Activity}.onActivityResult()
	 */
	public static void onActivityResult( int requestCode, int resultCode, Intent data )
	{
		if( requestCode == Configuration.ACTIVITY_REQUEST_FLAG
				&& resultCode == Activity.RESULT_OK )
		{
			Log.i( LOG_TAG, "Applying configuration" );
			try
			{
				Apply.apply( new JSONObject( data.getStringExtra( "conf" ) ), configTargets );
			}
			catch( JSONException e )
			{
				Log.e( LOG_TAG,
						"Problem parsing json data : " + data.getStringExtra( "conf" ), e );
			}
		}
	}

	/**
	 * Call this from your {@link Activity}.onActivityResult() instead
	 * of {@link #onActivityResult(int, int, Intent)} to save a
	 * configuration for later application. This is useful if your
	 * configuration changes need to be done in a particular thread
	 * like, for example, OpenGL stuff
	 * 
	 * @param requestCode
	 *           from {@link Activity}.onActivityResult()
	 * @param resultCode
	 *           from {@link Activity}.onActivityResult()
	 * @param data
	 *           from {@link Activity}.onActivityResult()
	 */
	public static void deferActivityResult( int requestCode, int resultCode, Intent data )
	{
		if( requestCode == Configuration.ACTIVITY_REQUEST_FLAG
				&& resultCode == Activity.RESULT_OK )
		{
			synchronized( deferredResults )
			{
				Log.i( LOG_TAG, "Deferring configuration" );
				try
				{
					JSONObject json = new JSONObject( data.getStringExtra( "conf" ) );
					deferredResults.add( new ConfigResult( configTargets, json ) );
				}
				catch( JSONException e )
				{
					Log.e( LOG_TAG,
							"Problem parsing json data : " + data.getStringExtra( "conf" ), e );
				}
			}
		}
	}

	/**
	 * Call this to apply configurations previously deferred in
	 * {@link #deferActivityResult(int, int, Intent)}
	 */
	public static void applyDeferredConfigurations()
	{
		synchronized( deferredResults )
		{
			while( !deferredResults.isEmpty() )
			{
				Log.i( LOG_TAG, "Applying deferred configuration" );

				ConfigResult cr = deferredResults.poll();
				Apply.apply( cr.config, cr.roots );
			}
		}
	}

	/**
	 * For deferring configuration application
	 * 
	 * @author ryanm
	 */
	private static class ConfigResult
	{
		private final Object[] roots;

		private final JSONObject config;

		private ConfigResult( Object[] roots, JSONObject config )
		{
			this.roots = roots;
			this.config = config;
		}
	}
}
