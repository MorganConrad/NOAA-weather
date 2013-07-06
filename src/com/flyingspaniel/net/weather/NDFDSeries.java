package com.flyingspaniel.net.weather;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;


/**
 * Represents a time-series of NDFD measurements, which are linked to a TimeLayout  
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
    * Parse an XML node (usually obtained via ndfd.xPathToNode)
    * @param noaa
    * @param node   non-null
    * @throws XPathExpressionException
    */
//   public static NDFDSeries parseNode(NDFD ndfd, NOAAWeather noaa, Node mainNode) throws XPathExpressionException {      
//      String timeLayoutName = UsesXPath.getAttribute(mainNode, NOAAWeather.TIME_LAYOUT);
//      String units = UsesXPath.getAttribute(mainNode, "units");
//      
//      NodeList nodeList = noaa.getNodeListFromXPath(mainNode, ndfd.listTag);
//      List<String> values = ndfd.parseValues(nodeList);
//      NDFDSeries ndfdSeries = new NDFDSeries(ndfd, noaa.timeLayoutMap.get(timeLayoutName), units, values);
//     
//      return ndfdSeries;
//   }
//   


   /*
    * Basic accessors
    */
   
   public TimeLayout getTimeLayout() {
      return timeLayout;
   }
   
   public String getUnits() {
      return units;
   }
   
   public int size() {
      return values.size();
   }
   

   public String getValue(int idx) {
      return values.get(idx);
   }
   
   
   public List<String> getValues() {
      return values;
   }
   
   
   /**
    * Returns the value that is closest in time to desiredTime, but within maxHourDiff
    * 
    * @param desiredTime
    * @param maxHourDiff
    * @return  a String, or null if no value is within maxHourrDiff
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
