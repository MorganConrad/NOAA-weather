package com.flyingspaniel.ranges;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.flyingspaniel.ranges.Ranges.Comparison.*;


/**
 * A stateless implementation of Ranges.Predicate 
 * Various static finals referring to "standard" implementations 
 * Static utility methods dealing with Ranges.Compable or Ranges.Comparator
 * 
 * @author Morgan Conrad
 * @since Copyright(c) 2013 Morgan Conrad
 * 
 * @see <a href="http://www.gnu.org/copyleft/lesser.html">This software is released under the LGPL</a>
 * 
 */
public class RangesPredicates implements Ranges.Predicate {

   /**
    * Contains in the broad/human-logical sense, i.e. the first range "contains or equals" the second
    */
   public static final RangesPredicates Contains = new RangesPredicates(EQ, CONTAINS);

   /**
    * ContainedBy in the broad/human-logical sense, i.e. the first range is "containedBy or equals" the second
    */
   public static final RangesPredicates ContainedBy = new RangesPredicates(Ranges.Comparison.EQ, Ranges.Comparison.CONTAINED_BY);

   /**
    * Overlaps in the broad/human-logical sense, i.e. contains, containedBy, overlaps in any form, or equals 
    * Unlike all the others, this uses a special implementation for a bit of extra speed.
    */
   public static final RangesPredicates Overlaps = new RangesPredicates(0) {

      @Override
      public boolean accept(int input) {
         return (input & 3) != 0;
      }

   };

   
   /**
    * Accepts only when the two Ranges are Equal
    */
   public static final RangesPredicates Equals = new RangesPredicates(EQ);
   
   /**
    * Accepts no pair of Ranges.
    */
   public static final RangesPredicates None = new RangesPredicates(new int[0]);
   
   // Strict predicates - relation be exactly as specified, not the broader, more logical sense.
   public static final RangesPredicates StrictlyContains = new RangesPredicates(Ranges.Comparison.CONTAINS);
   public static final RangesPredicates StrictlyContainedBy = new RangesPredicates(Ranges.Comparison.CONTAINED_BY);
   public static final RangesPredicates StrictlyOverlaps = new RangesPredicates(Ranges.Comparison.OVERLAPS_LT, Ranges.Comparison.OVERLAPS_GT);
   public static final RangesPredicates StrictlyLT = new RangesPredicates(Ranges.Comparison.STRICTLY_LT);
   public static final RangesPredicates StrictlyGT = new RangesPredicates(Ranges.Comparison.STRICTLY_GT);
   
   
   /*
    * Finally, the implementation
    * keep acceptable values as bits.  Shifted 8 so they are all positive.  And we have room for one more value than +-4.
    */
   final int bitsShifted8;

   
   /**
    * Constructor
    * @param accept  one or more ints from the ComparableRange constants
    */
   public RangesPredicates(int... accept) {
      int tempRangePredicate2 = 0;
      for (int a : accept)
         tempRangePredicate2 |= 1 << (a + 8);

      bitsShifted8 = tempRangePredicate2;
   }

   
   @Override
   public boolean accept(int input) {
      int shift8 = 1 << (input + 8);
      return (bitsShifted8 & shift8) != 0;
   }
   
   
   /*
    * Static utilities for selecting ranges from Collections
    */
   
   
   /**
    * Selects all items from comparableRanges such that predicate.accept(exemplar, item) is true.
    * If your predicate has an inverse pInv this is method equivalent to 
    *     listAcceptsExemplar(comparableRanges, exemplar, pInv)
    *     
    * Use this version when your Collection is of Ranges.Comparable (i.e. they know how to compare themselves)
    *     
    * @param exemplar            the range you want to match
    * @param comparableRanges    a Collection of possible matches (may be empty)
    * @param predicate           how to match
    * @return ArrayList<T>       non-null, may be empty
    */
   public static <T extends Ranges.Comparable<T>> List<T> exemplarAcceptsList(
         Ranges.Comparable<T> exemplar,
         Collection<T> comparableRanges, 
         Ranges.Predicate predicate) {
      
      List<T> list = new ArrayList<T>();
      for (T item : comparableRanges) {
         int compare = exemplar.compareRange(item);
         if (predicate.accept(compare))
            list.add(item);
      }

      return list;
   }

   
   
   /**
    * Selects all items from comparableRanges such that predicate.accept(exemplar, item) is true.
    * If your predicate has an inverse pInv this is method equivalent to 
    *     listAcceptsExemplar(comparableRanges, exemplar, comparator, pInv)
    *     
    * Use this version when your Collection is not of Ranges.Comparable, and you need a separate Ranges.Comparator
    * 
    * @param exemplar            the range you want to match
    * @param ranges              a Collection of possible matches (may be empty)
    * @param comparator          the external comparator.  If null, will attempt to compare using "natural ordering"
    *                                                      assuming the Ts ARE Ranges.Comparable - requires some unsafe casting
    * @param predicate           how to match
    * @return ArrayList<T>       non-null, may be empty
    */
   public static <T> List<T> exemplarAcceptsList(
         T exemplar,
         Collection<T> ranges,
         Ranges.Comparator<T> comparator,
         Ranges.Predicate predicate) {
    
      if (comparator == null)
         if (exemplar instanceof Ranges.Comparable)  // attempt "natural" ordering if comparator is null
            return exemplarAcceptsList( (Ranges.Comparable) exemplar, (Collection)ranges, predicate);
         else
            throw new IllegalArgumentException("You must provide a Ranges.Comparator if the examplar is not a Ranges.Comparable");
      
      List<T> list = new ArrayList<T>();
      for (T item : ranges) {
         int compare = comparator.compare(exemplar, item);
         if (predicate.accept(compare))
            list.add(item);
      }

      return list;
   }
   
   
   /**
    * Selects all items from comparableRanges such that predicate.accept(item, exemplar) is true.
    * If your predicate has an inverse pInv this is method equivalent to 
    *     exemplarAcceptsList(exemplar, comparableRanges, pInv)
    *     
    * Use this version when your Collection is of Ranges.Comparable (i.e. they know how to compare themselves)
    *     
    * @param comparableRanges    a Collection of possible matches (may be empty) 
    * @param exemplar            the range you want to match
    * @param predicate           how to match
    * @return ArrayList<T>       non-null, may be empty
    */
   public static <T extends Ranges.Comparable<T>> List<T> listAcceptsExemplar(
         Collection<T> comparableRanges, 
         T exemplar, 
         Ranges.Predicate predicate) {
      
      List<T> list = new ArrayList<T>();
      for (T item : comparableRanges) {
         int compare = item.compareRange(exemplar);
         if (predicate.accept(compare))
            list.add(item);
      }

      return list;
   }
   
   
   /**
    * Selects all items from comparableRanges such that predicate.accept(exemplar, item) is true.
    * If your predicate has an inverse pInv this is method equivalent to 
    *     listAcceptsExemplar(comparableRanges, exemplar, comparator, pInv)
    *     
    * Use this version when your Collection is not of Ranges.Comparable, and you need a separate Ranges.Comparator
    * 
    * @param ranges              a Collection of possible matches (may be empty)
    * @param exemplar            the range you want to match
    * @param comparator          the external comparator.  If null, will attempt to compare using "natural ordering"
    *                                                      assuming the Ts ARE Ranges.Comparable - requires some unsafe casting
    * @param predicate           how to match
    * @return ArrayList<T>       non-null, may be empty
    */
   public static <T> List<T> listAcceptsExemplar(
         Collection<T> ranges,
         T exemplar,
         Ranges.Comparator<T> comparator,
         Ranges.Predicate predicate) {
      
      if (comparator == null)
         if (exemplar instanceof Ranges.Comparable)  // attempt "natural" ordering if comparator is null
            return listAcceptsExemplar( (Collection)ranges, (Ranges.Comparable) exemplar, predicate);
         else
            throw new IllegalArgumentException("You must provide a Ranges.Comparator if the examplar is not a Ranges.Comparable");
  
      List<T> list = new ArrayList<T>();
      for (T item : ranges) {
         int compare = comparator.compare(item, exemplar);
         if (predicate.accept(compare))
            list.add(item);
      }

      return list;
   }
   
   
   /**
    * Selects the indices of items from comparableRanges such that predicate.accept(exemplar, item) is true
    * 
    * @param comparableRanges    a Collection of possible matches (may be empty)
    * @param exemplar            the range you want to match
    * @param predicate           how to match
    * @return int[]              non-null but may be 0 length
    */
   public static <T extends Ranges.Comparable<T>> int[] exemplarAcceptsIndices(T exemplar, List<T> comparableRanges, Ranges.Predicate predicate) {
      int[] array1 = new int[comparableRanges.size()];
      int idx = 0;

      for (int i = 0; i < comparableRanges.size(); i++) {
         T item = comparableRanges.get(i);
         int compare = exemplar.compareRange(item);
         if (predicate.accept(compare))
            array1[idx++] = i;
      }

      // some versions of Android don't have Arrays.copy

      int[] result = new int[idx];
      for (int i = 0; i < idx; i++)
         result[i] = array1[i];

      return result;
   }
   
   
   
   /*
    * Non-static utilities (they call the static versions)
    */
   
   /**
    * Selects all items from comparableRanges such that this.accept(exemplar, item) is true.
    * 
    * @param exemplar          the range you want to match
    * @param comparableRanges  a Collection of possible matches (may be empty)
    * 
    * @return ArrayList<T>     non-null, may be empty
    */
   public <T extends Ranges.Comparable<T>> List<T> exemplarAcceptsList(T exemplar, Collection<T> comparableRanges) {
      return exemplarAcceptsList(exemplar, comparableRanges, this);
   }

   /**
    * Selects all items from comparableRanges such that this.accept(item, exemplar) is true.
    * 
    * @param comparableRanges   the range you want to match
    * @param exemplar           the range you want to match
    * 
    * @return ArrayList<T>      non-null, may be empty
    */
   public <T extends Ranges.Comparable<T>> List<T> listAcceptsExemplar(Collection<T> comparableRanges, T exemplar) {
      return listAcceptsExemplar(comparableRanges, exemplar, this);
   }

   

}
