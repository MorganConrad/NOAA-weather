package com.flyingspaniel.net.weather;

import static com.flyingspaniel.ranges.Ranges.Comparison.CONTAINED_BY;
import static com.flyingspaniel.ranges.Ranges.Comparison.CONTAINS;
import static com.flyingspaniel.ranges.Ranges.Comparison.EQ;
import static com.flyingspaniel.ranges.Ranges.Comparison.OVERLAPS_GT;
import static com.flyingspaniel.ranges.Ranges.Comparison.OVERLAPS_LT;
import static com.flyingspaniel.ranges.Ranges.Comparison.STRICTLY_GT;
import static com.flyingspaniel.ranges.Ranges.Comparison.STRICTLY_LT;

import java.util.Date;
import java.util.List;

import com.flyingspaniel.ranges.Ranges;


/**
 * Represents an immutable interval of time (using longs for ms since Jan 1 1970)
 * Most methods have two forms, one taking a long (and ending in "MS"), the other a non-null Date
 * 
 * 
 * @author Morgan Conrad
 * @since Copyright(c) 2013  Morgan Conrad
 *
 * @see <a href="http://www.gnu.org/copyleft/lesser.html">This software is released under the LGPL</a>
 *
 */
public class TimeInterval implements Ranges.Comparable<TimeInterval> {

   public static final long HOURL = 1000L*60*60;
   public static final float HOURF = 1000.0f*60*60;
   
   // internally we use longs instead of Dates to save on object creation and ensure immutablilty
   public final long startMS;
   public final long stopMS;
   
   
   /**
    * Main constructor
    * @param startMS
    * @param stopMS
    */
   public TimeInterval(long startMS, long stopMS) {
      this.startMS = startMS;
      this.stopMS = stopMS;
   }
   
   /**
    * Main constructor using dates,
    * @param start non-null
    * @param stop  non-null
    */
   public TimeInterval(Date start, Date stop) {
      this(start.getTime(), stop.getTime());
   }
   
   
   /**
    * Constructor for an instant in time, stop == start
    * @param startAndStopMS
    */
   public TimeInterval(long startAndStopMS) {
      this(startAndStopMS, startAndStopMS);
   }
   
   
   /**
    * Constructor for an instant in time, stop == start
    * @param startAndStop
    */
   public TimeInterval(Date startAndStop) {
      this(startAndStop.getTime(), startAndStop.getTime());
   }
   
   /**
    * "Copy constructor"
    * @param copyMe
    */
   public TimeInterval(TimeInterval copyMe) {
      this(copyMe.startMS, copyMe.stopMS);
   }
   
   
   /**
    * Whether this starts after ms 
    * @param ms since 1970
    */
   public boolean after(long ms) {
      return startMS > ms;
   }
   
   /**
    * Whether this starts after the given date
    * @param date  non-null
    */
   public boolean after(Date date) {
      return after(date.getTime());
   }
   
   
   /**
    * Whether this stops before ms
    * @param ms since 1970
    */
   public boolean before(long ms) {
      return stopMS < ms;
   }

   /**
    * Whether this stops before the given Date
    * @param date  non-null
    */
   public boolean before(Date date) {
      return before(date.getTime());
   }

   /**
    * Whether ms is within our range
    * @param ms since 1970
    */
   public boolean contains(long ms) {
      return (ms >= startMS) && (ms <= stopMS);
   }
   
   /**
    * Whether date is within our range
    * @param date  non-null
    */
   public boolean contains(Date date) {
     return contains(date.getTime());
   }
   
 
   
   /**
    * How many hours apart is ms from our range.
    * @param ms
    * @return  0.0f means we contain ms, else a positive float
    */
   public float hoursApart(long ms) {
      if (contains(ms))
         return 0.0f;
      long ad1 = Math.abs(ms-startMS);
      long ad2 = Math.abs(ms-stopMS);
      
      return Math.min(ad1, ad2) / HOURF;     
   }
   
   
   /**
    * How many hours apart is date from our range.
    * @param date non-null
    * @return  0.0f means we contain ms, else a positive float
    */
   public float hoursApart(Date date) {
      return hoursApart(date.getTime());
   }
   
   
   /**
    * How many hours apart (minimum) is the other TimeInterval
    * @param other
    * @return 0.0 means we contain ms, else a positive float
    */
   public float hoursApart(TimeInterval other) {
      float f1 = hoursApart(other.startMS);
      float f2 = hoursApart(other.stopMS);
      return Math.min(f1,  f2);
   }
   
   
   /**
    * Whether this interval has 0 duration (startMS == stopMS)
    */
   public boolean isInstant() {
      return startMS == stopMS;
   }

   
   
   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj instanceof TimeInterval) {
         TimeInterval o = (TimeInterval) obj;
         return startMS == o.startMS && stopMS == o.stopMS;
      }

      return false;
   }

   @Override
   public int hashCode() {
      return (int) startMS;
   }

   
   @Override
   public int compareRange(TimeInterval o) {
	   
	   // test equals first to avoid trick boundary cases
  	 if ((startMS == o.startMS) && (stopMS == o.stopMS))
  		 return EQ;
  	 
       if (stopMS <= o.startMS)
          return STRICTLY_LT;
       if (startMS >= o.stopMS)
          return STRICTLY_GT;
       
       int startCmp = compareLong(startMS, o.startMS);
       int stopCmp = compareLong(stopMS, o.stopMS);
       switch(startCmp) {
          case -1: return stopCmp >= 0 ? CONTAINS : OVERLAPS_LT;
          case  1: return stopCmp > 0 ? OVERLAPS_GT : CONTAINED_BY;
          case  0: switch(stopCmp) {
             case -1: return CONTAINED_BY;
             case  0: return EQ;
             case  1: return CONTAINS;
          }
       }
       
       // should not get here
       throw new RuntimeException();
   }
   
 
   
   /**
    * Finds the index of the closest TimeInterval 
    * @param intervals    non-null
    * @param desiredTime
    * @param maxHourDiff
    * @return  -1 if none are within maxHourDiff
    */
   public static <T  extends TimeInterval> int findClosestTime(List<T> intervals, Date desiredTime, double maxHourDiff) {
      int lastInterval = intervals.size()-1;
      if (lastInterval < 0)
         return -1;
      
      long desiredMS = desiredTime.getTime();
           
      int idx = -1;
      float hoursDiff;
      
      // test 1st and last cases first...
      TimeInterval ti0 = intervals.get(0);
      if (ti0.after(desiredMS)) {
         hoursDiff = hourDiff(desiredMS, ti0.startMS);
         return (hoursDiff < maxHourDiff) ? 0 : -1;
      }
      else {
         TimeInterval tilast = intervals.get(lastInterval);
         if (tilast.before(desiredMS)) {
            hoursDiff = hourDiff(tilast.stopMS, desiredMS);
            return (hoursDiff < maxHourDiff) ? lastInterval : -1;
         }
      }
            
      idx = 1;  // we know 0 is not correct, so start at 1
      while (desiredMS > intervals.get(idx).stopMS)
         idx++;
      
      // at this point, intervals(idx).stopMS is greater than the desiredTime
      TimeInterval maybeMatch1 = intervals.get(idx);
      if (maybeMatch1.contains(desiredMS))
         return idx;
      
      TimeInterval maybeMatch2 = intervals.get(idx-1);
      
      long d1 = maybeMatch1.startMS - desiredMS;
      long d2 = desiredMS - maybeMatch2.stopMS;
      hoursDiff = msToHours(Math.min(d1, d2));
      
      return (hoursDiff <= maxHourDiff) ? idx : -1;
   }
   
   
   /**
    * Handy Comparator for longs
    * @param l1
    * @param l2
    * @return 0 if l1==l2, -1 if l1<l2,  1 if l1>l2
    */
   public static int compareLong(long l1, long l2) {
      return (l1 == l2) ? 0 : (l1 < l2 ? -1 : 1);
   }
   
   /**
    * Difference in hours (signed) between ms2 and ms1
    * @param ms1
    * @param ms2
    */
   public static float hourDiff(long ms1, long ms2) {
      return (ms2 - ms1) / HOURF;
   }
   
   /**
    * Converts milliseconds to hours
    * @param ms
    */
   public static float msToHours(long ms) {
      return ms / HOURF;
   }
   

  
}
