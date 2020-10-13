/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.fromtoday;


import android.content.Context;
import android.location.Location;
import android.preference.PreferenceManager;

import java.text.DateFormat;
import java.util.Date;

class Utils {

    static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting_location_updates";

    /**
     * 위치 업데이트를 요청할 경우 true를 반환하고, 그렇지 않을 경우 false를 반환한다.
     *
     * @param context The {@link Context}.
     */
    static boolean requestingLocationUpdates(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false);
    }

    /**
     *SharedPreference에 위치 업데이트 상태 저장
     * * @param 요청LocationUpdates 위치 업데이트 상태.
     */
    static void setRequestingLocationUpdates(Context context, boolean requestingLocationUpdates) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates)
                .apply();
    }

    /**
     * Returns the {@code location} object as a human readable string.
     * @param location  The {@link Location}.
     */
//    static String getLocationText(Location location) {
////        return location == null ? "Unknown location" :
////                "(" + location.getSpeed() + ", " + location.getTime() + ")";//값 찍어주기
////    }
////
////    static String getLocationTextSpeed(Location location) {
////        return location == null ? "Unknown location" :
////                "(" + location.getSpeed() + ")";//값 찍어주기
////    }
////
////    static String getLocationTextDistance(Location location) {
////
////        return location == null ? "Unknown location" :
////                "(" + location.getSpeed() + ")";//값 찍어주기
////    }

    static String getLocationTitle(Context context) {
        return context.getString(R.string.location_updated,
                DateFormat.getDateTimeInstance().format(new Date()));
    }


}
