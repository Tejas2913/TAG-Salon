package com.tejgokabhi.salonbooking.preference

import android.content.Context
import com.google.gson.Gson
import com.tejgokabhi.salonbooking.model.UserModel
import com.tejgokabhi.salonbooking.utils.Constants

object SharedPref {



    private fun convertUserModelToJson(userModel: UserModel): String {
        val gson = Gson()
        return gson.toJson(userModel)
    }

    fun saveUserData(context: Context, userModel: UserModel) {
        val sharedPreferences = context.getSharedPreferences(Constants.USER_REF, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val jsonString = convertUserModelToJson(userModel)
        editor.putString("user_model", jsonString)
        editor.apply()
    }



    fun getUserData(context: Context): UserModel?{
        val sharedPreferences = context.getSharedPreferences(Constants.USER_REF, Context.MODE_PRIVATE)
        val jsonString = sharedPreferences.getString("user_model", null)

        if (jsonString != null) {
            val gson = Gson()
            return gson.fromJson(jsonString, UserModel::class.java)
        }

        return null
    }


    fun clearData(context: Context){
        val sp = context.getSharedPreferences("user", Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.clear()
        editor.apply()
    }
}