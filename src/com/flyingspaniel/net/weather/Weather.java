package com.flyingspaniel.net.weather;


import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Utilities and inner interfaces / classes relating to weather
 * 
 * @author Morgan Conrad
 * @since Copyright(c) 2013  Morgan Conrad
 *
 * @see <a href="http://www.gnu.org/copyleft/lesser.html">This software is released under the LGPL</a>
 *
 */
public class Weather {

   /**
    * Converts Fahrenheit to Celsius
    * @param fahrenheit
    * @return degrees C
    */
   public static float toCelsius(float fahrenheit) {
      return (fahrenheit - 32.0f)/1.8f;
   }
   
   /**
    * Converts Celsius to Fahrenheit
    * @param celsius
    * @return degrees F
    */
   public static float toFahrenheit(float celsius) {
      return (celsius * 1.8f) + 32.0f;
   }
 
   
   
   /**
    * Parses a String to a float, return Float.NaN for various error conditions
    * 
    * @param s
    * @return float
    */
   public static float parseFloat(String s) {
      if ((s == null) || s.length() == 0)
         return Float.NaN;
      try {
         return Float.parseFloat(s);
      }
      catch (NumberFormatException nfe) {
         return Float.NaN;
      }
   }
   
   /**
    * Interface for classes that predict the weather (usually by asking somebody online)
    * Classes should implement Callable and return themselves
    *
    * @param <LOCATION>  how the location is defined.  Typically a String or a Double[]
    */
   public interface Interface<LOCATION> extends Callable<Interface<LOCATION>> {
      
      /**
       * Sets the start and end date for which we are interested
       * @param startDate
       * @param endDate
       */
      public void setDates(Date startDate, Date endDate);
      
      
      /**
       * Sets the start date for which we are interested
       * endDate will be calculated at days later
       * @param startDate
       * @param days
       */
      public void setDates(Date startDate, long days);
      
      
      
      /**
       * Sets the location for the forecast.
       * @param l
       */
      public void setLocation(LOCATION l);
      
      /**
       * Get the Current Conditions
       * @return Forecast
       */
      public List<Condition> getConditions();
      
      /**
       * Get all forecasts
       * @return may also contain the current conditions
       */
      public List<Forecast> getForecasts();
      
      /**
       * Catch-all for any extra information that does not fit nicely in a Forecast
       * @return non-null Map (may be empty)
       */
      public Map<String, String> getMoreInfo();
      
      
   }
   
   
   /**
    * Our API will throw IOExceptions cause they are somewhat "expected"
    * This Runtime Exception wraps less unexpected Exceptions (e.g. XPathExceptions)
    * 
    */
   @SuppressWarnings("serial")
   public static class Exception extends RuntimeException {
      public Exception(String message, Throwable cause) {
         super(message, cause);
      }
      
      public Exception(Throwable cause) {
         super(cause);
      }
      
      public Exception(String message) {
         super(message);
      }
   }
   
   
   
//   public static class Condition {
//      
//      public final String description;
//      public final String details;
//      
//      public Condition(String description, String details) {
//         this.description = description;
//         this.details = (details != null) ? details : "";
//      }
//      
//      
//      @Override
//      public String toString() {
//         return description + "  " + details;
//      }
//      
//      
//      public static class Dated extends Condition {
//         public final Date when;
//         
//         public Dated(Date when, String description, String details) {
//            super(description, details);
//            this.when = new Date(when.getTime());
//         }
//         
//         @Override
//         public String toString() {
//            return when + "  "  + super.toString();
//         }
//      }
//   }
//   
}
