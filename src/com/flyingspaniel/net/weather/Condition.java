package com.flyingspaniel.net.weather;



/**
 * Class representing weather conditions at a particular instant in time
 * 
 * @author Morgan Conrad
 * @since Copyright(c) 2013  Morgan Conrad
 *
 * @see <a href="http://www.gnu.org/copyleft/lesser.html">This software is released under the LGPL</a>
 *
 */
public class Condition extends TimeInterval {
   
   public final NDFD ndfd;
   public final String condition;
   
   public Condition(TimeInterval timeInterval, NDFD ndfd, String condition) {
      super(timeInterval);
      this.ndfd = ndfd;
      this.condition = condition;
   }
   
}
