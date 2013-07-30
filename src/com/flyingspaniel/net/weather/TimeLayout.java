package com.flyingspaniel.net.weather;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.flyingspaniel.xml.UsesXPath;


/**
 * Represents a single TimeLayout from the NOAA weather REST API
 * @author Morgan Conrad
 * @since Copyright(c) 2013  Morgan Conrad 
 *
 * @see <a href="http://www.gnu.org/copyleft/lesser.html">This software is released under the LGPL</a><p>
 * @see <a href="http://graphical.weather.gov/xml/rest.php">National Digital Forecast Database (NDFD) REST Web Service</a><p>
 *
 * An example of the XML for a single TimeLayout follows:
 *<pre>
   <time-layout time-coordinate="local" summarization="none">
      <layout-key>k-p24h-n7-1</layout-key>
      <start-valid-time>2012-08-20T08:00:00-07:00</start-valid-time>
      <end-valid-time>2012-08-20T20:00:00-07:00</end-valid-time>  (optional)
      ... more starts and ends
   </time-layout>
 */
public class TimeLayout {

   static final UsesXPath xPath = new UsesXPath();
      
   // the name of this time layout, e.g. "k-p24h-n7-1"
   public final String layoutKey;
   
   // the intervals
   final ArrayList<TimeInterval> intervals = new ArrayList<TimeInterval>();
   
   // true if end-valid-time tags exist.
   protected boolean isIntervals = false;

   
   /**
    * Constructor - generally, one would use parseXML instead
    * 
    * @param layoutKey  non-null
    */
   public TimeLayout(String layoutKey) {
      if (layoutKey == null)
         throw new IllegalArgumentException();
      
      this.layoutKey = layoutKey;
   }
   
   
   /**
    * Utility to find the index (within this.intervals) closest to desiredTime
    * @param desiredTime
    * @param maxHourDiff
    * @return  -1 if none found
    */
   public int findClosestTimeIndex(Date desiredTime, double maxHourDiff) {
      return TimeInterval.findClosestTime(intervals, desiredTime, maxHourDiff);
   }
   
   
   /**
    * Parses an XML Node representing the TimeLayout
    * @param  timeLayout  XML Node
    * @return TimeLayout  never null
    * @throws XPathExpressionException
    * @throws ParseException
    */
   public static TimeLayout parseXML(Node timeLayout) throws XPathExpressionException, ParseException {
      String key = xPath.getStringFromXPath(timeLayout, "layout-key");
      NodeList allStartTimes = xPath.getNodeListFromXPath(timeLayout, "start-valid-time" );
      NodeList allEndTimes = xPath.getNodeListFromXPath(timeLayout, "end-valid-time" );
      int allEndTimesLen = allEndTimes != null ? allEndTimes.getLength() : 0;
      
      TimeLayout result = new TimeLayout(key);
      result.isIntervals = allEndTimesLen > 0;

      for (int n=0; n<allStartTimes.getLength(); n++) {
         String dateS = allStartTimes.item(n).getTextContent();
         Date start = NOAAWeather.parseRFC3339(dateS);
         Date stop = (n < allEndTimesLen) ? NOAAWeather.parseRFC3339(allEndTimes.item(n).getTextContent()) : start;
         result.intervals.add(new TimeInterval(start, stop));           
      }
      
      return result;
   }
   


   /*
    * Use the unique (within a forecast) layoutKey
    */
   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj instanceof TimeLayout) {
         TimeLayout o = (TimeLayout) obj;
         return this.layoutKey.equals(o.layoutKey);
      }

      return false;
   }

   /*
    * Use the unique (within a forecast) layoutKey
    */
   @Override
   public int hashCode() {
      return layoutKey.hashCode();
   }
}
