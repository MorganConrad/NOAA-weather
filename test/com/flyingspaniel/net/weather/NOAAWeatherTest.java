package com.flyingspaniel.net.weather;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;


import junit.framework.TestCase;

public class NOAAWeatherTest extends TestCase {

//   public void testFoo() throws ParseException {
//      TimeZone tz = TimeZone.getTimeZone("GMT-05:00");
//      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SZ");  
//      Date fff = sdf.parse("2012-12-23T07:00:00-05:00");
//      Date d1 =  NOAAWeather.parseRFC3339("2012-12-23T07:00:00-05:00");
//      Date d2 = NOAAWeather.parseRFC3339("2012-12-23T07:00:00-04:00");
//      
//      System.out.println(fff + "    " + d1 + "   " + d2);
//   }
   
   
   
   public void testFile() throws IOException {
      NOAAWeather weather = new NOAAWeather();
      weather.document = weather.loadDocFromFile(new File("testdata/noaaweather.xml"));
      weather.call();
      
      List<Forecast> forecasts =  weather.getForecasts();
      assertEquals("Tuesday, August 21, 2012 : Foggy High=143.6 Low=125.6", forecasts.get(1).toString());
      
      Date aug202012 = new Date("August 20, 2012");
      
      Forecast currentConditions = weather.getConditionAtTime(aug202012);
      assertEquals("Monday, August 20, 2012 : Foggy High=143.6 Low=125.6", currentConditions.toString());
      
      weather.setPreferredHour(11);  // same as old preferred hour, should be no change
      assertEquals("Monday, August 20, 2012 : Foggy High=143.6 Low=125.6", weather.getConditionAtTime(aug202012).toString());
   }
   
   public void testReal() throws IOException {
      NOAAWeather weather = new NOAAWeather();
      weather.setDates(null, null);
      weather.setLocation(39.096, -94.595);  // Kansas City may have interesting weather
      weather.setLocation(45.88, -93.29);  // Minnesota
      weather.addNDFDParameters("wgust", "wwa");
      weather.call();
      Date now = new Date();
      Forecast currentConditions = weather.getConditionAtTime(now);
      //assertTrue(currentConditions.toString().startsWith(now.toString()));
      assertNotNull(currentConditions);
      assertTrue(weather.getMoreInfo().get(NOAAWeather.MoreInfoKeys.OUT_URL.name()).startsWith("http://forecast.weather.gov"));
      
      
      boolean b = true & false;
      
   }
   
   
   public void testInterpret() {
      NOAAWeather weather = new NOAAWeather();
      String s = weather.interpretIconName("hi_ntsra20.jpg");
      assertEquals("Thunderstorms in Vicinity (20%)", s);
      s = weather.interpretIconName("fzra100");
      assertEquals("Freezing Rain (100%)", s);
      s = weather.interpretIconName("nhi_fg40");  // try a weirdo combination
      assertEquals("Foggy (40%) (high)", s);
      s = weather.interpretIconName("n");         // try a failure
      assertEquals("Cannot interpret n", s);
   }
   
   
  
}
