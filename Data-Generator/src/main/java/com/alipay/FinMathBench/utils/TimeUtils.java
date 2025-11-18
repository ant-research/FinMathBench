/*
 * Ant Group
 * Copyright (c) 2004-2025 All Rights Reserved.
 */
package com.alipay.FinMathBench.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;

/**
 * @author luojing.wp
 * @version TimeUtils.java, v 0.1 2025年11月10日 下午5:32 luojing.wp
 */
@Slf4j
public class TimeUtils {
    public static final String              SYS_CONF_PRINT_RATIO           = "log.print.ratio";
    public static final String              FMT_DATE_YYYYMMDD_HHMMSS       = "yyyy-MM-dd HH:mm:ss";
    public static final String              FMT_DATE_YYYYMMDD_HHMM         = "yyyy-MM-dd HH:mm";
    public static final String              FMT_DATE_YYYYMMDD_HH           = "yyyy-MM-dd HH";
    public static final String              FMT_DATE_YYYYMMDD              = "yyyy-MM-dd";
    public static final String              FMT_DATE_YYYYMMDDHHMMSS        = "yyyyMMddHHmmss";
    public static final String              FMT_DATE_YYYYMMDDHHMMSSMS      = "yyyyMMddHHmmssSSS";
    public static final String              FMT_DATE_YYYYMMDDHHMMSSDOTMS   = "yyyyMMddHHmmss.SSS";
    public static final String              FMT_DATE_YYYYMMDDHHMMSSCOMMAMS = "yyyy-MM-dd HH:mm:ss,SSS";
    public static final String              FMT_DATE_YYYYMMDDHHMMSSNS      = "yyyyMMddHHmmssSSSSSS";
    public static final String              FMT_DATE_YYYYMMDDHHMMSS_E      = "yyyyMMdd HH:mm:ss";
    public static final String              FMT_DATE_YMD                   = "yyyyMMdd";
    public static final String              FMT_DATE_YMD_COLON             = "yyyy:MM:dd";
    public static final String              FMT_DATE_YYYYDOTMMDOTDD        = "yyyy.MM.dd";
    public static final String              FMT_DATE_YYYYMM                = "yyyyMM";
    public static final String              FMT_DATE_MMDOTDD               = "MM-dd";
    public static final String              FMT_DATE_MMSLASHDD             = "MM/dd";
    public static final String              FMT_DATE_HHMM                  = "HH:MM";
    public static final String              FMT_DATE_UTC                   = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String              TIMESTAMP_S                    = "TIMESTAMP_S";
    public static final String              TIMESTAMP_MS                   = "TIMESTAMP_MS";
    public static final String              DAY                            = "DAY";
    public static final String              HMS                            = "HHmmss";
    public static       Map<String, String> dateFormatToIndex              = new HashMap();
    public static       Map<String, String> indexToDateFormat              = new HashMap();

    public TimeUtils() {
    }

    public static Date defaultTime() {
        return getDateByStr("1999-01-01", "yyyy-MM-dd");
    }

    public static String now() {
        return toString(new Date(), "yyyy-MM-dd HH:mm:ss");
    }

    public static Date getDateByStr(String date, String pattern) {
        if (StringUtils.isEmpty(date)) {
            String ratioStr = System.getProperty("log.print.ratio", "0.01");

            Double ratio;
            try {
                ratio = Double.parseDouble(ratioStr);
            } catch (NumberFormatException var8) {
                ratio = 0.01;
            }

            log.error("TimeUtils.getDateByStr date is null!", (Throwable) null, ratio);
        }

        try {
            return (new SimpleDateFormat(pattern)).parse(date);
        } catch (ParseException var7) {
            String ratioStr = System.getProperty("log.print.ratio", "0.01");

            Double ratio;
            try {
                ratio = Double.parseDouble(ratioStr);
            } catch (NumberFormatException var6) {
                ratio = 0.01;
            }

            log.error("TimeUtils.getDateByStr has an error, pattern:" + pattern, var7, ratio);
            return null;
        }
    }

    public static String toString(Date date, String pattern) {
        return (new SimpleDateFormat(pattern)).format(date == null ? new Date() : date);
    }

    public static String formatDate(Date target, String format) {
        if (target == null) {
            return null;
        } else {
            try {
                return (new SimpleDateFormat(format)).format(target);
            } catch (Exception var3) {
                return null;
            }
        }
    }

    public static Date getDateWithSeconds(Date date, int delta) {
        return (new DateTime(date)).plusSeconds(delta).toDate();
    }

    public static Date getDateWithMinutes(Date date, int delta) {
        return (new DateTime(date)).plusMinutes(delta).toDate();
    }

    public static String fixTimeZero(int num) {
        return num >= 1 && num <= 9 ? "0" + num : "" + num;
    }

    public static Date getDateWithHours(Date date, int delta) {
        return (new DateTime(date)).plusHours(delta).toDate();
    }

    public static Date getDateWithDays(Date date, int delta) {
        return (new DateTime(date)).plusDays(delta).toDate();
    }

    public static Date getDateWithMonths(Date date, int delta) {
        return (new DateTime(date)).plusMonths(delta).toDate();
    }

    public static Date getLastDayOfMonth(Date date, int delta) {
        return delta == 0 ? (new DateTime(date)).dayOfMonth().withMaximumValue().toDate() : (new DateTime(date)).plusMonths(delta)
                .dayOfMonth().withMaximumValue().toDate();
    }

    public static Date getDateWithYears(Date date, int delta) {
        return (new DateTime(date)).plusYears(delta).toDate();
    }

    public static Date floor(Date date) {
        DateTime dateTime = new DateTime(date);
        return dateTime.withTime(0, 0, 0, 0).toDate();
    }

    public static Date floorHour(Date date) {
        DateTime dateTime = new DateTime(date);
        int hour = dateTime.getHourOfDay();
        return dateTime.withTime(hour, 0, 0, 0).toDate();
    }

    public static Date getDateFloor(Date date, int delta) {
        DateTime dateTime = new DateTime(date);
        return dateTime.plusDays(delta).withTime(0, 0, 0, 0).toDate();
    }

    public static Date ceiling(Date date) {
        DateTime dateTime = new DateTime(date);
        return dateTime.withTime(23, 59, 59, 999).toDate();
    }

    public static Date getDateCeiling(Date date, int delta) {
        DateTime dateTime = new DateTime(date);
        return dateTime.plusDays(delta).withTime(23, 59, 59, 0).toDate();
    }

    public static int getHoursOfDay(Date date) {
        DateTime dateTime = new DateTime(date);
        return dateTime.getHourOfDay();
    }

    public static int hoursBetween(Date date1, Date date2) {
        DateTime dateTime1 = new DateTime(date1);
        DateTime dateTime2 = new DateTime(date2);
        return Math.abs(Hours.hoursBetween(dateTime1, dateTime2).getHours());
    }

    public static int daysBetween(Date date1, Date date2) {
        DateTime dateTime1 = new DateTime(date1);
        DateTime dateTime2 = new DateTime(date2);
        return Math.abs(Days.daysBetween(dateTime1, dateTime2).getDays());
    }

    public static int dayForWeek(Date date) {
        try {
            return (new DateTime(date)).getDayOfWeek();
        } catch (Exception var2) {
            return 0;
        }
    }

    public static int getFiscalYear(Date time) {
        DateTime dateTime = new DateTime(time);
        int year = dateTime.getYear();
        int month = dateTime.getMonthOfYear();
        if (month >= 4) {
            ++year;
        }

        return year;
    }

    public static Date getFiscalYearStart(Date time) {
        DateTime dateTime = new DateTime(time);
        int year = dateTime.getYear();
        int month = dateTime.getMonthOfYear();
        if (month >= 4) {
            ++year;
        }

        dateTime = new DateTime(year - 1, 4, 1, 0, 0, 0);
        return dateTime.toDate();
    }

    public static Date getMonthStart(Date time) {
        DateTime dateTime = new DateTime(time);
        int year = dateTime.getYear();
        int month = dateTime.getMonthOfYear();
        dateTime = new DateTime(year, month, 1, 0, 0, 0);
        return dateTime.toDate();
    }

    public static String getWeek(Date time) {
        DateTime dateTime = new DateTime(time);
        int year = dateTime.getYear();
        int week = dateTime.getWeekOfWeekyear();
        return year + "-W" + week;
    }

    public static Date getWeekStart(Date time) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(time);
        int dayWeek = cal.get(7);
        if (1 == dayWeek) {
            cal.add(5, -1);
        }

        cal.setFirstDayOfWeek(2);
        int day = cal.get(7);
        cal.add(5, cal.getFirstDayOfWeek() - day);
        return floor(cal.getTime());
    }

    public static String getCurrDate(String pattern) {
        return new SimpleDateFormat(pattern).format(System.currentTimeMillis());
    }

    static {
        dateFormatToIndex.put("yyyy-MM-dd HH:mm:ss", "1");
        dateFormatToIndex.put("yyyy-MM-dd", "2");
        dateFormatToIndex.put("yyyyMMddHHmmss", "3");
        dateFormatToIndex.put("yyyyMMdd", "4");
        dateFormatToIndex.put("yyyy:MM:dd", "5");
        dateFormatToIndex.put("TIMESTAMP_S", "6");
        dateFormatToIndex.put("TIMESTAMP_MS", "7");
        dateFormatToIndex.put("DAY", "8");
        dateFormatToIndex.put("yyyyMMdd HH:mm:ss", "9");
        dateFormatToIndex.put("yyyy.MM.dd", "10");
        dateFormatToIndex.put("HHmmss", "11");
        dateFormatToIndex.put("yyyy-MM-dd'T'HH:mm:ss", "12");
        dateFormatToIndex.put("yyyyMMddHHmmssSSS", "13");
        dateFormatToIndex.put("yyyyMMddHHmmssSSSSSS", "14");
        dateFormatToIndex.put("yyyyMMddHHmmss.SSS", "15");
        indexToDateFormat.put("1", "yyyy-MM-dd HH:mm:ss");
        indexToDateFormat.put("2", "yyyy-MM-dd");
        indexToDateFormat.put("3", "yyyyMMddHHmmss");
        indexToDateFormat.put("4", "yyyyMMdd");
        indexToDateFormat.put("5", "yyyy:MM:dd");
        indexToDateFormat.put("6", "TIMESTAMP_S");
        indexToDateFormat.put("7", "TIMESTAMP_MS");
        indexToDateFormat.put("8", "DAY");
        indexToDateFormat.put("9", "yyyyMMdd HH:mm:ss");
        indexToDateFormat.put("10", "yyyy.MM.dd");
        indexToDateFormat.put("11", "HHmmss");
        indexToDateFormat.put("12", "yyyy-MM-dd'T'HH:mm:ss");
        indexToDateFormat.put("13", "yyyyMMddHHmmssSSS");
        indexToDateFormat.put("14", "yyyyMMddHHmmssSSSSSS");
        indexToDateFormat.put("15", "yyyyMMddHHmmss.SSS");
    }
}