package main.bindroid.sdattendance.utills;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import main.bindroid.sdattendance.beans.SDEmployee;

/**
 * Created by nitishshrivastava on 9/4/15.
 */
public class CommonUtils {

	public static boolean isConnectingToInternet(Context mContext) {
		ConnectivityManager connectivity = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null)
				for (int i = 0; i < info.length; i++)
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}

		}
		return false;
	}

	public static void hideKeypad(Context context, View view) {
		if (context != null && view != null) {
			InputMethodManager imm = (InputMethodManager) context
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}

	public static void setLoggedInUser(Context context, SDEmployee employee) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				"SDAttendance", Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		editor.putString("LoggedInUser", changeEmployeeToJson(employee)
				.toString());
		editor.commit();
	}

	public static SDEmployee getLoggedInUser(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				"SDAttendance", Context.MODE_PRIVATE);
		if (sharedPreferences.getString("LoggedInUser", null) != null) {
			try {
				return changeJsonToEmployee(new JSONObject(
						sharedPreferences.getString("LoggedInUser", null)));
			} catch (JSONException e) {

			}
		}
		return null;
	}

	private static JSONObject changeEmployeeToJson(SDEmployee employee) {
		JSONObject json = new JSONObject();
		try {
			json.put("empName", employee.getEmpName());
			json.put("empCode", employee.getEmpCode());
			json.put("empDept", employee.getEmpDepartment());
			json.put("empFloor", employee.getEmpFloor());
			json.put("empSeat", employee.getEmpSeat());
			json.put("empTeam", employee.getEmpTeam());
			json.put("empUnit", employee.getEmpUnit());
		} catch (JSONException e) {

		}
		return json;
	}

	private static SDEmployee changeJsonToEmployee(JSONObject json) {
		if (json != null && json.length() > 0) {
			SDEmployee emp = new SDEmployee();
			try {
				emp.setEmpCode(json.getString("empCode"));
				emp.setEmpName(json.getString("empName"));
				emp.setEmpFloor(json.getString("empFloor"));
				emp.setEmpDepartment(json.getString("empDept"));
				emp.setEmpSeat(json.getString("empSeat"));
				emp.setEmpTeam(json.getString("empTeam"));
				emp.setEmpUnit(json.getString("empUnit"));
			} catch (JSONException e) {

			}
			return emp;
		} else {
			return null;
		}
	}

	public static String getDate(Long milisecond) {
		if (milisecond == null || milisecond > 0)

			return "";
		Date date = new Date(milisecond);
		String stringMonth = (String) android.text.format.DateFormat.format(
				"MMM", date);
		String year = (String) android.text.format.DateFormat.format("yyyy",
				date);
		String day = (String) android.text.format.DateFormat.format("dd", date);
		return day + " " + stringMonth + " " + year;
	}

	public static String getTime(Long mili) {
		if (mili == null || mili > 0)
			return "";
		Date date = new Date(mili);
		// System.out.println("Date: " + date);

		DateFormat df = new SimpleDateFormat("h:mm a");
		String hour = df.format(date);
		return hour;
	}
}