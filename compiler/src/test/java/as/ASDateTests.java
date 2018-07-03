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

import org.junit.Test;

/**
 * Feature tests for AS Date objects
 */
public class ASDateTests extends ASFeatureTestsBase
{

    @Test
    public void ASDateTests_date()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
            "var date : Date = new Date('Sat Jun 30 23:59:59 GMT-0800 2018');",
            "date.date += 1;",
            "assertEqual('date.date', date.date, 1);",
        };
        String source = getAS(imports, declarations, testCode, new String[0]);
        compileAndRun(source);
    }

    @Test
    public void ASDateTests_dateUTC()
    {
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
        compileAndRun(source);
    }

    @Test
    public void ASDateTests_dayUTC()
    {
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
        compileAndRun(source);
    }

    @Test
    public void ASDateTests_fullYear()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
            "var date : Date = new Date('Mon Dec 31 23:59:59 GMT-0800 2018');",
            "date.fullYear += 1;",
            "assertEqual('date.fullYear', date.fullYear, 2019);",
        };
        String source = getAS(imports, declarations, testCode, new String[0]);
        compileAndRun(source);
    }
    
    @Test
    public void ASDateTests_fullYearUTC()
    {
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
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
            "var date : Date = new Date('Sat Jun 30 23:59:59 GMT-0800 2018');",
            "date.hours += 1;",
            "assertEqual('date.hours', date.hours, 0);",
        };
        String source = getAS(imports, declarations, testCode, new String[0]);
        compileAndRun(source);
    }

    @Test
    public void ASDateTests_hoursUTC()
    {
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
            "assertEqual('date.minutesUTC', date.minutesUTC, 1);",
        };
        String source = getAS(imports, declarations, testCode, new String[0]);
        compileAndRun(source);
    }

    @Test
    public void ASDateTests_month()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
            "var date : Date = new Date('Sat Jun 30 23:59:59 GMT-0800 2018');",
            "date.month += 1;",
            "assertEqual('date.month', date.month, 6);",
        };
        String source = getAS(imports, declarations, testCode, new String[0]);
        compileAndRun(source);
    }

    @Test
    public void ASDateTests_monthUTC()
    {
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

    @Test
    public void ASDateTests_timezoneOffset_get()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
            "var date : Date = new Date('Sat Jun 30 23:59:59 GMT-0800 2018');",
            "assertEqual('date.timezoneOffset', date.timezoneOffset, -480);",
        };
        String source = getAS(imports, declarations, testCode, new String[0]);
        compileAndRun(source);
    }
    
    @Test
    public void ASDateTests_timezoneOffset_set()
    {
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
