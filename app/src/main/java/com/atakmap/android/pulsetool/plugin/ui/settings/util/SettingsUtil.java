/*
 * Copyright (c) 2017 Garmin International. All Rights Reserved.
 * <p></p>
 * This software is the confidential and proprietary information of
 * Garmin International.
 * You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement
 * you entered into with Garmin International.
 * <p></p>
 * Garmin International MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. Garmin International SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 * <p></p>
 * Created by johnsongar on 2/2/2017.
 */
package com.atakmap.android.pulsetool.plugin.ui.settings.util;

import android.util.ArrayMap;
import android.view.View;
import com.garmin.health.settings.Goal;
import com.garmin.health.settings.GoalOption;
import com.garmin.health.settings.Settings;

import java.util.*;

public final class SettingsUtil
{
    public static boolean isVisible(View v)
    {
        return v != null && v.getVisibility() == View.VISIBLE;
    }

    public static List<Goal> getGoals(Settings settings)
    {
        List<Goal> goals = new ArrayList<>(settings.userSettings().goals());

        Set<String> goalTypes = new HashSet<>();

        for (Goal goal : goals)
        {
            goalTypes.add(goal.goalType());
        }

        //Any goals not already set should default
        for (String goalType : settings.deviceSettings().schema().supportedGoals())
        {
            if(!goalTypes.contains(goalType))
            {
                goals.add(new Goal(goalType, 0, GoalOption.AUTO));
            }
        }
        return goals;
    }

    public static Map<String, Float> generateGoalsMap(Settings settings)
    {
        Map<String, Float> goalsMap = new ArrayMap<>();

        for (Goal goal : SettingsUtil.getGoals(settings))
        {
            goalsMap.put(goal.goalType(), (float)(!goal.goalOption().equals(GoalOption.AUTO) ? goal.value() : 0));
        }

        return goalsMap;
    }

    public static List<Goal> parseGoalsMap(Map<String, Float> goalsMap)
    {
        List<Goal> goals = new ArrayList<>();

        for(String type : goalsMap.keySet())
        {
            int value = Math.round(goalsMap.get(type));
            //Zero value corresponds to Auto
            goals.add(new Goal(type, value, value != 0 ? GoalOption.USER_DEFINED : GoalOption.AUTO));
        }
        return goals;
    }

    public static String formatNumber(double d)
    {
        if(d == (long) d)
            return String.format("%d",(long)d);
        else
            return String.format("%.1f",d);
    }
}
