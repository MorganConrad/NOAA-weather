package com.flyingspaniel.ranges;


/**
 * Class (actually, a bunch of inner classes) for comparing a pair of ranges.
 * 
 * @author Morgan Conrad
 * @since Copyright(c) 2013  Morgan Conrad 
 *
 * @see <a href="http://www.gnu.org/copyleft/lesser.html">This software is released under the LGPL</a>
 *
 */
public class Ranges {

   /**
    * Similar to @link java.util.Comparable.  
    * Implement this interface if the range itself can do the comparison.
    * The result returned should be one of the Ranges.Comparison constants below
    *
    * @param <T> the type of ranges that this range may be compared to (usually it's own class)
    */
   public interface Comparable<T> {
      public int compareRange(T other);
   }
   
   
   /**
    * Similar to @link java.util.Comparator.  
    * Implement this interface if a class separate from the range does the comparison.
    * The result returned should be one of constants below
    *
    * @param <R>  the type of ranges that may be compared by this comparator
    */
   public interface Comparator<R> extends java.util.Comparator<R> {
      // already defined in java.util.Comparator as  int compare(R r1, R r2);
   }
   
   
   /**
    * The values to be returned by Ranges.Comparable or Ranges.Comparator
    * Arguably these should be enums, but they are kept as ints
    * for compatability with java.util.Comparable and java.util.Comparator
    * Organized into an inner class for distinctness
    */

   public class Comparison {
      /**
       * Range A is complete less than (or equal to) Range B max(A) <= min(B)
       */
      public static final int STRICTLY_LT = -4;
   
      /**
       * Range A is generally less than Range B but there is some overlap
       * (min(A) < min(B)) && (max(A) < max(B)) && (max(A) > min(B))
       */
      public static final int OVERLAPS_LT = -2;
   
      /**
       * Range A is "inside" B (min(A) >= min(B)) && (max(A) <= max(B)) but they
       * are not both equal
       */
      public static final int CONTAINED_BY = -1;
   
      /**
       * As for the "range" part, A.equals(B) (min(A) == min(B)) && (max(A) ==
       * max(B))
       */
      public static final int EQ = 0;
   
      /**
       * B is "inside" A (min(A) <= min(B)) && (max(A) >= max(B)) but they are
       * not both equal
       */
      public static final int CONTAINS = 1;
   
      /**
       * Range A is generally greater than Range B but there is some overlap
       * (min(A) > min(B)) && (max(A) > max(B)) && (min(A) < max(B))
       */
      public static final int OVERLAPS_GT = 2;
   
      /**
       * Range A is completely greater than (or equal to) Range B min(A) >=
       * max(B)
       */
      public static final int STRICTLY_GT = 4;
   
   }
   
   
   
   /**
    * Interface for accepting the comparison result, which should be a Ranges.Comparison constant
    *
    * @see <a href="http://docs.guava-libraries.googlecode.com/git/javadoc/com/google/common/base/Predicate.html">Guava Predicate</a>
    *
    */
   public interface Predicate {
      public boolean accept(int input);
   }
   
}
