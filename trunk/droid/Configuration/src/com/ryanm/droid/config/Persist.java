
package com.ryanm.droid.config;

import java.util.Arrays;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Methods for storage of configurations
 * 
 * @author ryanm
 */
public class Persist
{
	private Persist()
	{
	}

	private static SharedPreferences getPrefs( Activity act, String owner )
	{
		return act.getSharedPreferences( owner + ".config", Context.MODE_WORLD_WRITEABLE );
	}

	/**
	 * Saves the configuration of an object tree
	 * 
	 * @param owner
	 * @param savename
	 *           The name of the saved data
	 * @param roots
	 *           The objects whose configuration to save
	 */
	public static void save( Activity owner, String savename, Object... roots )
	{
		JSONObject json = Extract.extract( roots );
		save( owner, owner.getClass().getName(), savename, json );
	}

	/**
	 * Loads and applies a saved configuration
	 * 
	 * @param owner
	 * @param savename
	 *           The name of the saved data
	 * @param roots
	 *           The objects to apply the configuration to
	 */
	public static void load( Activity owner, String savename, Object... roots )
	{
		JSONObject json = load( owner, owner.getClass().getName(), savename );

		if( json != null )
		{
			Apply.apply( json, roots );
		}
	}

	/**
	 * Deletes a saved configuration
	 * 
	 * @param owner
	 * @param savename
	 *           The name of the save to delete
	 */
	public static void deleteSave( Activity owner, String savename )
	{
		deleteSave( owner, owner.getClass().getName(), savename );
	}

	/**
	 * Lists saved configurations
	 * 
	 * @param owner
	 * @return A sorted list of save names
	 */
	public static String[] listSaves( Activity owner )
	{
		return listSaves( owner, owner.getClass().getName() );
	}

	static void save( Activity act, String owner, String savename, JSONObject json )
	{
		SharedPreferences.Editor prefs = getPrefs( act, owner ).edit();
		prefs.putString( savename, json.toString() );
		prefs.commit();
	}

	static JSONObject load( Activity act, String owner, String savename )
	{
		SharedPreferences prefs = getPrefs( act, owner );
		try
		{
			return new JSONObject( prefs.getString( savename, null ) );
		}
		catch( JSONException e )
		{
			Log.e( Configuration.LOG_TAG, "Problem parsing save. I'll delete it", e );
			deleteSave( act, owner, savename );
			return null;
		}
	}

	static void deleteSave( Activity act, String owner, String savename )
	{
		SharedPreferences.Editor prefs = getPrefs( act, owner ).edit();
		prefs.remove( savename );
		prefs.commit();
	}

	static String[] listSaves( Activity act, String owner )
	{
		SharedPreferences prefs = getPrefs( act, owner );
		Set<String> saves = prefs.getAll().keySet();
		String[] array = saves.toArray( new String[ saves.size() ] );
		Arrays.sort( array );

		return array;
	}
}
