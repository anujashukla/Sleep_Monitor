package com.example.anuja.sleep_monitor;

public class StillTimePeriod {
	String mStartTimeStr;
	String mEndTimeStr;
	long mStillPeriod;
	long mStartTime;
	long mEndTime;
	String mStillPeriodStr;

	public StillTimePeriod(String startTimeStr, String endTimeStr, long stillPeriod, long startTime, long endTime) {
		mStartTimeStr = startTimeStr;
		mEndTimeStr = endTimeStr;
		mStillPeriod = stillPeriod;
		mStartTime = startTime;
		mEndTime = endTime;
	}

	public String getStartTimeStr() {
		return mStartTimeStr;
	}

	public String getEndTimeStr() {
		return mEndTimeStr;
	}

	public long getStillPeriod() {
		return mStillPeriod;
	}

	public long getStartTime() {
		return mStartTime;
	}

	public long getEndTime() {
		return mEndTime;
	}

	public String getStillPeriodStr() {
		long seconds=(mStillPeriod/1000)%60;
        long minutes=(mStillPeriod/(1000*60))%60;
        long hours=(mStillPeriod/(1000*60*60))%24;
        return hours+":"+minutes+":"+seconds;
	}

}