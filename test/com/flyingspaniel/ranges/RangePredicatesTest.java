package com.flyingspaniel.ranges;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import static com.flyingspaniel.ranges.Ranges.Comparison.*;

public class RangePredicatesTest extends TestCase {

	 
	   public void test1() {
	      ArrayList<TestRange> list = new ArrayList<TestRange>();
	      for (int i=2; i<=10; i+=2)
	         for (int j=2; j<=10; j+=2)
	            list.add(new TestRange(i,j));
	      
	      Collections.shuffle(list);
	      Collections.sort(list);
	      
	      int size = list.size();
	      
	      for (int i=0; i<size; i++)
	         for (int j=0; j<size; j++) {
	        	 TestRange trI = list.get(i);
	        	 TestRange trJ = list.get(j);
	        	 
	            int cmp = trI.compareTo(trJ);
	            if (i>j)
	               assertTrue(trI + ">=" + trJ + cmp, cmp >= 0);
	            else if (i==j)
	               assertTrue(trI + "==" + trJ + cmp, cmp == 0);
	            else
	               assertTrue(trI + "<=" + trJ + cmp, cmp <= 0);
	         }
	      
	      
	      TestRange tr = new TestRange(3,7);
	      TestRange tr2 = new TestRange(6,8);
	      TestRange tr3 = new TestRange(4,4);
	      assertEquals(-2, tr.compareTo(tr2));
	      assertEquals(2, tr2.compareTo(tr));
	      
	      // test closures
	      assertTrue(doCompare(RangesPredicates.Equals, tr,tr));
	      assertFalse(doCompare(RangesPredicates.Equals, tr,tr2));
	      assertTrue(doCompare(RangesPredicates.Overlaps, tr,tr2));
	      assertTrue(doCompare(RangesPredicates.StrictlyOverlaps, tr,tr2));
	      assertTrue(doCompare(RangesPredicates.Contains, tr,tr3));
	      assertTrue(doCompare(RangesPredicates.ContainedBy, tr3,tr));
	      
	      assertTrue(doCompare(RangesPredicates.Contains, tr,tr));
	      assertFalse(doCompare(RangesPredicates.StrictlyContains, tr,tr));
	      assertTrue(doCompare(RangesPredicates.StrictlyLT, tr3,tr2));
	      assertTrue(doCompare(RangesPredicates.StrictlyGT, tr2,tr3));
	      
	      List<TestRange> contained = RangesPredicates.exemplarAcceptsList(tr,  list,  RangesPredicates.Contains);
	      assertEquals("[ 4..4 ,  4..6 ,  4..6 ,  6..6 ]", contained.toString());
	      contained = RangesPredicates.exemplarAcceptsList(tr,  list,  MY_COMPARATOR, RangesPredicates.Contains);
         assertEquals("[ 4..4 ,  4..6 ,  4..6 ,  6..6 ]", contained.toString());
         contained = RangesPredicates.exemplarAcceptsList(tr,  list,  null, RangesPredicates.Contains);
         assertEquals("[ 4..4 ,  4..6 ,  4..6 ,  6..6 ]", contained.toString());
	      List<TestRange> containing = RangesPredicates.Contains.listAcceptsExemplar(list, tr);
	      assertEquals("[ 2..8 ,  2..8 ,  2..10 ,  2..10 ]", containing.toString());
	      containing = RangesPredicates.listAcceptsExemplar(list, tr, MY_COMPARATOR, RangesPredicates.Contains);
         assertEquals("[ 2..8 ,  2..8 ,  2..10 ,  2..10 ]", containing.toString());
         containing = RangesPredicates.listAcceptsExemplar(list, tr, null, RangesPredicates.Contains);
         assertEquals("[ 2..8 ,  2..8 ,  2..10 ,  2..10 ]", containing.toString());
	      List<TestRange> overlapped = RangesPredicates.Overlaps.exemplarAcceptsList(tr,  list);
	      assertEquals(20, overlapped.size());
	      int[] indices = RangesPredicates.exemplarAcceptsIndices(tr,  list,  RangesPredicates.Contains);
	      assertEquals("[3, 4, 5, 8]", Arrays.toString(indices));
	      
	   }
	   
	   
	  
	   boolean doCompare(RangesPredicates predicate, TestRange tr1, TestRange tr2 ) {
	      int compare = tr1.compareRange(tr2);
	      return predicate.accept(compare);
	   }
	   
	   
	   class TestRange implements Ranges.Comparable<TestRange>, Comparable<TestRange> {
	      
	      final int start;
	      final int stop;
	      
	      TestRange(int start, int stop) {
	    	  if (stop < start) {
	    		 int x = stop;
	    		 stop = start;
	    		 start = x;
	    	  }
	         this.start = start;
	         this.stop = stop;
	      }

	      @Override
	      public int compareRange(TestRange o) {
	    	  
	    	  // test equals first to avoid trick boundary cases
	    	 if ((start == o.start) && (stop == o.stop))
	    		 return EQ;
	    	 
	         if (stop <= o.start)
	            return STRICTLY_LT;
	         if (start >= o.stop)
	            return STRICTLY_GT;
	         
	         int startCmp = compareInt(start, o.start);
	         int stopCmp = compareInt(stop, o.stop);
	         switch(startCmp) {
	            case -1: return stopCmp >= 0 ? CONTAINS : OVERLAPS_LT;
	            case  1: return stopCmp > 0 ? OVERLAPS_GT : CONTAINED_BY;
	            case  0: switch(stopCmp) {
	               case -1: return CONTAINED_BY;
	               case  0: return EQ;
	               case  1: return CONTAINS;
	               default: throw new RuntimeException();
	            }
	            default: throw new RuntimeException();
	         }
	         
	      }
	      
	      @Override
			public int compareTo(TestRange arg0) {
				return compareRange(arg0);
			}


	      int compareInt(int i1, int i2) {
	         return (i1 == i2) ? 0 : (i1 < i2 ? -1 : 1);
	      }
	      
	      
	      @Override
	      public String toString() {
	    	 return " " + start + ".." + stop + " "; 
	      }
	      
	      
	      @Override
		public boolean equals(Object o) {
			if (this == o)
				return true;

			if (o instanceof TestRange) {
				TestRange cast = (TestRange) o;
				return start == cast.start && stop == cast.stop;
			}

			return false;
		}

		@Override
		public int hashCode() {
			return (start << 16) ^ stop;
		}

     

	      
	   }
	   
	   
	   static final Ranges.Comparator<TestRange> MY_COMPARATOR = new Ranges.Comparator<TestRange>() {

         @Override
         public int compare(TestRange o1, TestRange o2) {
            return o1.compareRange(o2);
         }
	      
	   };
}
