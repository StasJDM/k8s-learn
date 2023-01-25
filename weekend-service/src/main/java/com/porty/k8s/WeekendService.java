package com.porty.k8s;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.net.URI;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;

import static spark.Spark.*;

public class WeekendService {

  private static Logger logger = LoggerFactory.getLogger(WeekendService.class);

  private static AtomicBoolean ready = new AtomicBoolean(false);

  public static void main(String[] args) {
    port(5678);

    Gson gson = new Gson();

    get("/weekend/:country/:city", (req, res) -> {
      ZoneId timeZoneId = ZoneId.of(req.params("country") + "/" + req.params("city"));
      logger.info("Запрошен статус выходного дня для зоны {}",
          timeZoneId);

      TimeServiceResponse timeServiceResponse = gson.fromJson(
          new InputStreamReader(
              URI.create("http://time-service:8080/nanotime").toURL().openStream()),
          TimeServiceResponse.class);

      Instant millisTime = Instant.ofEpochMilli(
          Long.parseLong(timeServiceResponse.getNanoTime()) / 1000000);
      DayOfWeek dayOfWeek = millisTime.atZone(timeZoneId).getDayOfWeek();
      boolean isWeekend = EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
          .contains(dayOfWeek);

      return new TimeZoneReply(isWeekend, dayOfWeek.name());
    }, gson::toJson);

    get("/ready", (req, res) -> {
      if (ready.get()) {
        logger.info("Запрос проверки готовности, сервер готов.");
        return "Готов!";
      } else {
        logger.warn("Запрос проверки готовности, сервер не готов.");
        throw new IllegalStateException("Не готов!");
      }
    });

    new Thread(() -> {
      awaitInitialization();
      ready.set(true);
    }).start();
  }

  static class TimeZoneReply {

    private boolean weekend;
    private String day;

    TimeZoneReply(boolean weekend, String day) {
      this.weekend = weekend;
      this.day = day;
    }

    public boolean isWeekend() {
      return weekend;
    }

    public String getDay() {
      return day;
    }
  }

  static class TimeServiceResponse {

    private String time;
    private String nanoTime;

    public String getTime() {
      return time;
    }

    public void setTime(String time) {
      this.time = time;
    }

    public String getNanoTime() {
      return nanoTime;
    }

    public void setNanoTime(String nanoTime) {
      this.nanoTime = nanoTime;
    }
  }
}
