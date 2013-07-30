package com.flyingspaniel.net.weather;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Represents a time-series of {@link NDFD} measurements, which are linked to a {@link TimeLayout}  
 * 
 * @author Morgan Conrad
 * @since Copyright(c) 2013  Morgan Conrad
 *
 * @see <a href="http://www.gnu.org/copyleft/lesser.html">This software is released under the LGPL</a>
 *
 */
public class NDFDSeries {
   
   public final NDFD ndfd;

   protected final TimeLayout timeLayout;
   protected final String units;
   
   protected final List<String> values; 
   
  /**
   * Constructor.
   * @param ndfd
   * @param timeLayout
   * @param units    units of the measurement, such as Fahrenheit, inches, knots
   * @param values   a "safe" copy will be made.
   */
   public NDFDSeries(NDFD ndfd, TimeLayout timeLayout, String units, List<String> values) {
      this.ndfd = ndfd;
      this.timeLayout = timeLayout;
      this.units = units != null ? units : "?";
      
      if (values == null)
         values = Collections.emptyList();
      
      this.values = Collections.unmodifiableList(values);
   }
   
   
   NDFDSeries(NDFD ndfd) {
      this(ndfd, null, "?", null);
   }
   
   
   /**
    * The associated TimeLayout
    */
   public TimeLayout getTimeLayout() {
      return timeLayout;
   }
   
   /**
    * The units of the measurement, such as Fahrenheit, inches, knots
    * @return "?" if none were specified
    */
   public String getUnits() {
      return units;
   }
   
   /**
    * Size of the series
    */
   public int size() {
      return values.size();
   }
   

   /**
    * Get a specific value
    * @param idx
    */
   public String getValue(int idx) {
      return values.get(idx);
   }
   
   /**
    * Get the (unmodifiable) list of the values
    */
   public List<String> getValues() {
      return values;
   }
   
   
   /**
    * Returns the value that is closest in time to desiredTime, but within maxHourDiff
    * 
    * @param desiredTime
    * @param maxHourDiff
    * @return  a String, or null if no value is within maxHourDiff
    */
   public String getValueClosestTo(Date desiredTime, double maxHourDiff) {
      int idxInTimeLayout = timeLayout.findClosestTimeIndex(desiredTime, maxHourDiff);
      if (idxInTimeLayout < 0)
         return null;
      return values.get(idxInTimeLayout);
   }
   
   
   /**
    * Returns the value as a float (if legal)
    * 
    * @param idx
    * @return float, may be Float.NaN
    * 
    * @throws IllegalStateException  if this series is not numeric
    */
   public float getFloatValue(int idx) {
      if (ndfd.isNumeric())
         return Weather.parseFloat(values.get(idx));
      else
         throw new IllegalStateException();
   }
}
