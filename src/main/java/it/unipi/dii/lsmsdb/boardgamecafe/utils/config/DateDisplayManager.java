package it.unipi.dii.lsmsdb.boardgamecafe.utils.config;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;


public class DateDisplayManager {

    public static String convertDateToReadable(Date timestampDate) {
        System.out.println("[DEBUG] received: " + timestampDate);

        Instant now = Instant.now();
        Instant targetTime = timestampDate.toInstant();

        Duration duration = Duration.between(now, targetTime);

        long hours = Math.abs(duration.toHours());
        if (hours < 24) {
            return "less than " + hours + " hours ago";
        } else {
            long days = Math.abs(duration.toDays());
            if (days < 365) {
                return days + " days ago";
            } else {
                return "more than a year ago";
            }
        }
    }
}
