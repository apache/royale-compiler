/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package as;

import org.junit.Ignore;
import org.junit.Test;

import java.util.Date;
import java.util.TimeZone;

/**
 * Feature tests for AS Date objects
 */
public class ASDateTests extends ASFeatureTestsBase
{

    private String setTimeZone(String s)
    {
        TimeZone tz = TimeZone.getDefault();
        System.out.println("tzoffset is " + new Integer(tz.getOffset(new Date().getTime()) / 3600000).toString());
        String offsetString = new Integer(tz.getOffset(new Date().getTime()) / 3600000).toString();
        if (offsetString.length() == 2)
            offsetString = offsetString.substring(0,1) + 0 + offsetString.substring(1, 2);
        if(offsetString.charAt(0) != '-')
            offsetString = "+" + offsetString;
        offsetString = "GMT" + offsetString + "00";
        if (!hasFlashPlayerGlobal)
            offsetString = "GMT-0800";
        System.out.println("GMT is " + offsetString);
        return s.replace("TZ", offsetString);
    }
    
    private String setTimeZoneOffsetMinutes(String s)
    {
        TimeZone tz = TimeZone.getDefault();
        System.out.println("tzoffset is " + new Integer(tz.getOffset(new Date().getTime()) / 3600000).toString());
        String offsetString = new Integer(tz.getOffset(new Date().getTime()) / -60000).toString();
        if (!hasFlashPlayerGlobal)
            offsetString = "-480";
        System.out.println("offset in minutes is " + offsetString);
        return s.replace("TZ", offsetString);
    }
    
    @Test
    public void ASDateTests_date()
    {
        System.out.println("ASDateTests_date");
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
            setTimeZone("var date : Date = new Date('Sat Jun 30 23:59:59 2018');"),
            "date.date += 1;",
            "assertEqual('date.date', date.date, 1);",
        };
        String source = getAS(imports, declarations, testCode, new String[0]);
        compileAndRun(source);
    }

    @Test
    public void ASDateTests_dateUTC()
    {
        System.out.println("ASDateTests_dateUTC");
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
            "var date : Date = new Date('Sat Jun 30 23:59:59 GMT-0800 2018');",
            "date.dateUTC += 1;",
            "assertEqual('date.dateUTC', date.dateUTC, 2);",
        };
        String source = getAS(imports, declarations, testCode, new String[0]);
        compileAndRun(source);
    }

    @Test
    public void ASDateTests_day()
    {
        System.out.println("ASDateTests_day");
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
            "var date : Date = new Date('Sat Jun 30 23:59:59 GMT-0800 2018');",
            "date.day += 1;",
            "assertEqual('date.day', date.day, 0);",
        };
        String source = getAS(imports, declarations, testCode, new String[0]);
        compileAndExpectErrors(source, false, false, false, null, "Property day is read-only.\n");
    }

    @Test
    public void ASDateTests_dayUTC()
    {
        System.out.println("ASDateTests_dayUTC");
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
            "var date : Date = new Date('Sat Jun 30 23:59:59 GMT-0800 2018');",
            "date.dayUTC += 1;",
            "assertEqual('date.dayUTC', date.dayUTC, 1);",
        };
        String source = getAS(imports, declarations, testCode, new String[0]);
        compileAndExpectErrors(source, false, false, false, null, "Property dayUTC is read-only.\n");
    }

    @Test
    public void ASDateTests_fullYear()
    {
        System.out.println("ASDateTests_fullYear");
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
            setTimeZone("var date : Date = new Date('Mon Dec 31 23:59:59 TZ 2018');"),
            "date.fullYear += 1;",
            "assertEqual('date.fullYear', date.fullYear, 2019);",
        };
        String source = getAS(imports, declarations, testCode, new String[0]);
        compileAndRun(source);
    }
    
    @Test
    public void ASDateTests_fullYearUTC()
    {
        System.out.println("ASDateTests_fullYearUTC");
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
            "var date : Date = new Date('Mon Dec 31 23:59:59 GMT-0800 2018');",
            "date.fullYearUTC += 1;",
            "assertEqual('date.fullYearUTC', date.fullYearUTC, 2020);",
        };
        String source = getAS(imports, declarations, testCode, new String[0]);
        compileAndRun(source);
    }

    @Test
    public void ASDateTests_hours()
    {
        System.out.println("ASDateTests_hours");
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
            setTimeZone("var date : Date = new Date('Sat Jun 30 23:59:59 2018');"),
            "date.hours += 1;",
            "assertEqual('date.hours', date.hours, 0);",
        };
        String source = getAS(imports, declarations, testCode, new String[0]);
        compileAndRun(source);
    }

    @Test
    public void ASDateTests_hoursUTC()
    {
        System.out.println("ASDateTests_hoursUTC");
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
            "var date : Date = new Date('Sat Jun 30 23:59:59 GMT-0800 2018');",
            "date.hoursUTC += 1;",
            "assertEqual('date.hoursUTC', date.hoursUTC, 8);",
        };
        String source = getAS(imports, declarations, testCode, new String[0]);
        compileAndRun(source);
    }

    @Test
    public void ASDateTests_milliseconds()
    {
        System.out.println("ASDateTests_milliseconds");
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
            "var date : Date = new Date('Sat Jun 30 23:59:59 GMT-0800 2018');",
            "date.milliseconds -= 1;",
            "assertEqual('date.milliseconds', date.milliseconds, 999);",
        };
        String source = getAS(imports, declarations, testCode, new String[0]);
        compileAndRun(source);
    }

    @Test
    public void ASDateTests_millisecondsUTC()
    {
        System.out.println("ASDateTests_millisecondsUTC");
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
            "var date : Date = new Date('Sat Jun 30 23:59:59 GMT-0800 2018');",
            "date.millisecondsUTC -= 1;",
            "assertEqual('date.millisecondsUTC', date.millisecondsUTC, 999);",
        };
        String source = getAS(imports, declarations, testCode, new String[0]);
        compileAndRun(source);
    }

    @Test
    public void ASDateTests_minutes()
    {
        System.out.println("ASDateTests_minutes");
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
            "var date : Date = new Date('Sat Jun 30 23:59:59 GMT-0800 2018');",
            "date.minutes += 1;",
            "assertEqual('date.minutes', date.minutes, 0);",
        };
        String source = getAS(imports, declarations, testCode, new String[0]);
        compileAndRun(source);
    }

    @Test
    public void ASDateTests_minutesUTC()
    {
        System.out.println("ASDateTests_minutesUTC");
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
            "var date : Date = new Date('Sat Jun 30 23:59:59 GMT-0800 2018');",
            "date.minutesUTC += 1;",
            "assertEqual('date.minutesUTC', date.minutesUTC, 0);",
        };
        String source = getAS(imports, declarations, testCode, new String[0]);
        compileAndRun(source);
    }

    @Test
    public void ASDateTests_month()
    {
        System.out.println("ASDateTests_month");
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
            setTimeZone("var date : Date = new Date('Sat Jun 30 23:59:59 2018');"),
            "date.month += 1;",
            "assertEqual('date.month', date.month, 6);",
        };
        String source = getAS(imports, declarations, testCode, new String[0]);
        compileAndRun(source);
    }

    @Test
    public void ASDateTests_monthUTC()
    {
        System.out.println("ASDateTests_monthUTC");
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
            "var date : Date = new Date('Sat Jun 30 23:59:59 GMT-0800 2018');",
            "date.monthUTC += 1;",
            "assertEqual('date.monthUTC', date.monthUTC, 7);",
        };
        String source = getAS(imports, declarations, testCode, new String[0]);
        compileAndRun(source);
    }

    @Test
    public void ASDateTests_seconds()
    {
        System.out.println("ASDateTests_seconds");
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
            "var date : Date = new Date('Sat Jun 30 23:59:59 GMT-0800 2018');",
            "date.seconds += 1;",
            "assertEqual('date.seconds', date.seconds, 0);",
        };
        String source = getAS(imports, declarations, testCode, new String[0]);
        compileAndRun(source);
    }

    @Test
    public void ASDateTests_secondsUTC()
    {
        System.out.println("ASDateTests_secondsUTC");
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
            "var date : Date = new Date('Sat Jun 30 23:59:59 GMT-0800 2018');",
            "date.secondsUTC += 1;",
            "assertEqual('date.secondsUTC', date.secondsUTC, 0);",
        };
        String source = getAS(imports, declarations, testCode, new String[0]);
        compileAndRun(source);
    }

    @Test
    public void ASDateTests_time()
    {
        System.out.println("ASDateTests_time");
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
            "var date : Date = new Date('Sat Jun 30 23:59:59 GMT-0800 2018');",
            "date.time += 1;",
            "assertEqual('date.time', date.time, 1530431999001);",
        };
        String source = getAS(imports, declarations, testCode, new String[0]);
        compileAndRun(source);
    }

    @Ignore
    @Test
    public void ASDateTests_timezoneOffset_get()
    {
        System.out.println("ASDateTests_timezoneOffset_get");
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
            "var date : Date = new Date('Sat Jun 30 23:59:59 GMT-0800 2018');",
            setTimeZoneOffsetMinutes("assertEqual('date.timezoneOffset', date.timezoneOffset, TZ);"),
        };
        String source = getAS(imports, declarations, testCode, new String[0]);
        compileAndRun(source);
    }
    
    @Test
    public void ASDateTests_timezoneOffset_set()
    {
        System.out.println("ASDateTests_timezoneOffset_set");
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
            "var date : Date = new Date('Sat Jun 30 23:59:59 GMT-0800 2018');",
            "date.timezoneOffset += 480;",
            "assertEqual('date.timezoneOffset', date.timezoneOffset, 0);",
        };
        String source = getAS(imports, declarations, testCode, new String[0]);
        compileAndExpectErrors(source, false, false, false, null, "Property timezoneOffset is read-only.\n");
    }
}
