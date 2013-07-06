package com.flyingspaniel.net.weather;

import java.text.DateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import com.flyingspaniel.ranges.Ranges;

/**
 * Class representing a weather forecast for a single day
 * 
 * @author Morgan Conrad
 * @since Copyright(c) 2012  Morgan Conrad
 *
 * @see <a href="http://www.gnu.org/copyleft/lesser.html">This software is released under the LGPL</a>
 */
public class Forecast extends TimeInterval {

   static final DateFormat DATE_FORMAT = DateFormat.getDateInstance(DateFormat.FULL);
   
   /*
    * The following are considered "standard enough" to include in all Forecasts
    */
   
   protected final Date date;
   protected final String iconInterpretation;
   protected final String iconPath;
   protected float lowF = Float.NaN;
   protected float highF = Float.NaN;
   
   protected Map<Date, Condition> conditionMap = new TreeMap<Date, Condition>();
   
//   protected float tempF = Float.NaN;
   
   /**
    * Any additional info, e.g. humidity, to be stored here
    * Alternatively, make a subclass...
    */
   private Map<String,Object> moreInfo = null;
   
   
//   /**
//    * Constructor   Date is set to now
//    * 
//    * @param lowF
//    * @param highF
//    * @param condition
//    * @param conditionIcon
//    */
//   public Forecast(int lowF, int highF, String condition, String conditionIconPath) {
//      this(new Date());
//      
//      this.lowF = lowF;
//      this.highF = highF;
//      this.condition = condition;
//      this.conditionIconPath = conditionIconPath;
//   }
//   
   /**
    * 
    */
//   public Forecast(Date date) {
//      super(new Date());
//      this.date = new Date(date.getTime());
//   }
   
   /**
    * Constructor
    * @param date  gets copied
    */
   public Forecast(Date date, float low, float high, boolean inCelsius, String iconInterpretation, String iconPath) {
	   super(date);
      this.date = new Date(date.getTime());
      this.lowF = inCelsius ? Weather.toFahrenheit(low) : low;
      this.highF = inCelsius ? Weather.toFahrenheit(high) : high;
      this.iconInterpretation = iconInterpretation;
      this.iconPath = iconPath;
   }
   
   /**
    * Returns a nice String representation
    * @param inCelsius
    * @return String
    */
   public String toString(boolean inCelsius) {
      StringBuilder sb = new StringBuilder();
      sb.append(DATE_FORMAT.format(date));
      sb.append(" : " + iconInterpretation);
      addIfPresent(sb, " High=", getHigh(inCelsius));
      addIfPresent(sb, " Low=", getLow(inCelsius));
      
      if (moreInfo != null) {
         for (Map.Entry<String,Object> me : getMoreInfo().entrySet()) {
            sb.append(" " + me.getKey() + "=" + me.getValue());
         }
      }
      
      return sb.toString();
   }
   
   
   
   @Override
   public String toString() {
      return toString(false);
   }
   
   /**
    * Date of the Forecast
    * @return never null
    */
   public Date getDate() {
      return date;
   }

//   /**
//    * Sets Date for the forecast
//    * @param date   will get copied.  If null, use current time
//    */
//   public void setDate(Date date) {
//      this.date = date != null ? new Date(date.getTime()) : new Date();
//   }

   /**
    * Get the general condition (e.g. "Showers")
    * @return
    */
   public String getInterpretation() {
      return iconInterpretation;
   }
   
   
   /**
    * Gets a path to an icon representing the condition, 
    * e.g. http://www.weather.gov/images/fcicons/hi_shwrs.jpg
    * 
    * @return may be null
    */
   public String getIconPath() {
      return iconPath;
   }

   
   /**
    * Get the predicted low temperature
    * @param inCelsius
    * @return degrees (C or F)
    */
   public float getLow(boolean inCelsius) {
      return inCelsius ? Weather.toCelsius(lowF) : lowF;
   }
   
   /**
    * Sets predicted low temperature
    * @param low
    * @param inCelsius
    */
   public void setLow(float low, boolean inCelsius) {
      this.lowF = inCelsius ? Weather.toFahrenheit(low) : low;
   }

 
   /**
    * Get the predicted high temperature
    * @param inCelsius
    * @return degrees (C or F)
    */
   public float getHigh(boolean inCelsius) {
      return inCelsius ? Weather.toCelsius(highF) : highF;
   }


   /**
    * Sets predicted high temperature
    * @param high
    * @param inCelsius
    */
   public void setHigh(float high, boolean inCelsius) {
      this.highF = inCelsius ? Weather.toFahrenheit(high) : high;
   }

//   /**
//    * Get current temperature
//    * @param inCelsius
//    * @return degrees (C or F)
//    */
//   public float getTemp(boolean inCelsius) {
//      return inCelsius ? toCelsius(tempF) : tempF;
//   }
//
//   /**
//    * Set current temperature
//    * @param temp
//    * @param inCelsius temp is celsius
//    */
//   public void setTempF(float temp, boolean inCelsius) {
//      this.tempF = inCelsius ? toFahrenheit(temp) : temp;
//   }
//   
   
   /**
    * Return the Map of moreInfo, creating it if necessary
    * @return
    */
   public synchronized Map<String, Object> getMoreInfo() {
      if (moreInfo == null)
         moreInfo = new TreeMap<String, Object>();
      
      return moreInfo;
   }
   
   
   public Object putMoreInfo(String key, Object value) {
      return getMoreInfo().put(key, value);
   }
      
   
   protected void addIfPresent(StringBuilder sb, String name, float value) {
      if (!Float.isNaN(value)) {
         sb.append(name);
         sb.append(value);
      }
   }
   
   protected void addIfPresent(StringBuilder sb, String name, String value) {
      if ((value != null) && (value.length() > 0)) {
         sb.append(name);
         sb.append(value);
      }
   }

}
