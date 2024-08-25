package com.fittracker.utilits

import android.content.Context
import android.content.SharedPreferences
import com.fittracker.utilits.ConstantsSquats.sharedPrefFile

object FormFixSharedPreferences {
    fun getSharedPreferences(context:Context):SharedPreferences{
      return context.getSharedPreferences(sharedPrefFile,Context.MODE_PRIVATE)
    }

    fun saveSharedPreferencesValue(context:Context,key:String,value:String){
        var editor = getSharedPreferences(context).edit()
        editor.putString(key,value)
        editor.commit()
    }
    fun saveSharedPreferencesValue(context:Context,key:String,value:Int){
        var editor = getSharedPreferences(context).edit()
        editor.putInt(key,value)
        editor.commit()
    }

    fun saveSharedPreferencesValue(context:Context,key:String,value:Boolean){
        var editor = getSharedPreferences(context).edit()
        editor.putBoolean(key,value)
        editor.commit()
    }

    fun getSharedPrefStringValue(context:Context,key:String):String?{
        val sharedPref = getSharedPreferences(context)
        return sharedPref.getString(key,"")
    }
    fun getSharedPrefBooleanValue(context:Context,key:String):Boolean{
        val sharedPref = getSharedPreferences(context)
        return sharedPref.getBoolean(key,false)
    }
    fun getSharedPrefIntValue(context:Context,key:String):Int?{
        val sharedPref = getSharedPreferences(context)
        return sharedPref.getInt(key,-1)
    }



}