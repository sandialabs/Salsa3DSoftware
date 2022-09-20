/**
 * Copyright 2009 Sandia Corporation. Under the terms of Contract
 * DE-AC04-94AL85000 with Sandia Corporation, the U.S. Government
 * retains certain rights in this software.
 * 
 * BSD Open Source License.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *    * Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the name of Sandia National Laboratories nor the names of its
 *      contributors may be used to endorse or promote products derived from
 *      this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package gov.sandia.gmp.util.numerical.intrinsicsort;

/**
 * Intrinsic sorting algorithm that sorts one intrinsic array in-place
 * while simultaneously sorting an additional intrinsic or generic array
 * based on the primary intrinsic array swaps. The sorter uses the modified
 * QuickSort algorithm taken from the Java Arrays class and modified to sort
 * two arrays simultaneously.
 * 
 * <p> By calling sort(int[] x, int[] index) where index is initialized
 * to index[i] = i, i=0 to x.length-1 then index will contain the
 * permutation of the sort on x which can be used to sort many other
 * arrays using the sorted permutation defined for x. This class is
 * generally useful if more than one array must be sorted based on a
 * single arrays sorting permutation.
 * 
 * @author jrhipp
 *
 */
public class IntrinsicSort
{
  /**
   * Sorts the input integer array and the accompanying integer array based
   * on the integer array sort.
   * 
   * @param x The array to be sorted.
   * @param v The accompanying integer vector sorted based on x.
   */
  public static void sort(int[] x, int[] v)
  {
    sort(x, v, 0, x.length);
  }

  /**
   * Sorts the input integer array and the accompanying long array based
   * on the integer array sort.
   * 
   * @param x The array to be sorted.
   * @param v The accompanying long vector sorted based on x.
   */
  public static void sort(int[] x, long[] v)
  {
    sort(x, v, 0, x.length);
  }

  /**
   * Sorts the input integer array and the accompanying float array based
   * on the integer array sort.
   * 
   * @param x The array to be sorted.
   * @param v The accompanying float vector sorted based on x.
   */
  public static void sort(int[] x, float[] v)
  {
    sort(x, v, 0, x.length);
  }

  /**
   * Sorts the input integer array and the accompanying double array based
   * on the integer array sort.
   * 
   * @param x The array to be sorted.
   * @param v The accompanying double vector sorted based on x.
   */
  public static void sort(int[] x, double[] v)
  {
    sort(x, v, 0, x.length);
  }

  /**
   * Sorts the input integer array and the accompanying generic array based
   * on the integer array sort.
   * 
   * @param x The array to be sorted.
   * @param v The accompanying generic vector sorted based on x.
   */
  public static <T> void sort(int[] x, T[] v)
  {
    sort(x, v, 0, x.length);
  }

  /**
   * Sorts the input long array and the accompanying integer array based
   * on the long array sort.
   * 
   * @param x The array to be sorted.
   * @param v The accompanying integer vector sorted based on x.
   */
  public static void sort(long[] x, int[] v)
  {
    sort(x, v, 0, x.length);
  }

  /**
   * Sorts the input long array and the accompanying long array based
   * on the long array sort.
   * 
   * @param x The array to be sorted.
   * @param v The accompanying long vector sorted based on x.
   */
  public static void sort(long[] x, long[] v)
  {
    sort(x, v, 0, x.length);
  }

  /**
   * Sorts the input long array and the accompanying float array based
   * on the long array sort.
   * 
   * @param x The array to be sorted.
   * @param v The accompanying float vector sorted based on x.
   */
  public static void sort(long[] x, float[] v)
  {
    sort(x, v, 0, x.length);
  }

  /**
   * Sorts the input long array and the accompanying double array based
   * on the long array sort.
   * 
   * @param x The array to be sorted.
   * @param v The accompanying double vector sorted based on x.
   */
  public static void sort(long[] x, double[] v)
  {
    sort(x, v, 0, x.length);
  }

  /**
   * Sorts the input long array and the accompanying generic array based
   * on the long array sort.
   * 
   * @param x The array to be sorted.
   * @param v The accompanying generic vector sorted based on x.
   */
  public static <T> void sort(long[] x, T[] v)
  {
    sort(x, v, 0, x.length);
  }

  /**
   * Sorts the input float array and the accompanying integer array based
   * on the float array sort.
   * 
   * @param x The array to be sorted.
   * @param v The accompanying integer vector sorted based on x.
   */
  public static void sort(float[] x, int[] v)
  {
    sort(x, v, 0, x.length);
  }

  /**
   * Sorts the input float array and the accompanying long array based
   * on the float array sort.
   * 
   * @param x The array to be sorted.
   * @param v The accompanying long vector sorted based on x.
   */
  public static void sort(float[] x, long[] v)
  {
    sort(x, v, 0, x.length);
  }

  /**
   * Sorts the input float array and the accompanying float array based
   * on the float array sort.
   * 
   * @param x The array to be sorted.
   * @param v The accompanying float vector sorted based on x.
   */
  public static void sort(float[] x, float[] v)
  {
    sort(x, v, 0, x.length);
  }

  /**
   * Sorts the input float array and the accompanying double array based
   * on the float array sort.
   * 
   * @param x The array to be sorted.
   * @param v The accompanying double vector sorted based on x.
   */
  public static void sort(float[] x, double[] v)
  {
    sort(x, v, 0, x.length);
  }

  /**
   * Sorts the input float array and the accompanying generic array based
   * on the float array sort.
   * 
   * @param x The array to be sorted.
   * @param v The accompanying generic vector sorted based on x.
   */
  public static <T> void sort(float[] x, T[] v)
  {
    sort(x, v, 0, x.length);
  }

  /**
   * Sorts the input double array and the accompanying integer array based
   * on the double array sort.
   * 
   * @param x The array to be sorted.
   * @param v The accompanying integer vector sorted based on x.
   */
  public static void sort(double[] x, int[] v)
  {
    sort(x, v, 0, x.length);
  }

  /**
   * Sorts the input double array and the accompanying long array based
   * on the double array sort.
   * 
   * @param x The array to be sorted.
   * @param v The accompanying long vector sorted based on x.
   */
  public static void sort(double[] x, long[] v)
  {
    sort(x, v, 0, x.length);
  }

  /**
   * Sorts the input double array and the accompanying float array based
   * on the double array sort.
   * 
   * @param x The array to be sorted.
   * @param v The accompanying float vector sorted based on x.
   */
  public static void sort(double[] x, float[] v)
  {
    sort(x, v, 0, x.length);
  }

  /**
   * Sorts the input double array and the accompanying double array based
   * on the double array sort.
   * 
   * @param x The array to be sorted.
   * @param v The accompanying double vector sorted based on x.
   */
  public static void sort(double[] x, double[] v)
  {
    sort(x, v, 0, x.length);
  }

  /**
   * Sorts the input double array and the accompanying generic array based
   * on the double array sort.
   * 
   * @param x The array to be sorted.
   * @param v The accompanying generic vector sorted based on x.
   */
  public static <T> void sort(double[] x, T[] v)
  {
    sort(x, v, 0, x.length);
  }

  /**
   * Used to flip an index array to obtain ascending order.
   * 
   * @param a The integer index array whose order is to be inverted.
   */
  public static void reverseOrder(int[] a)
  {
    reverseOrder(a, 0, a.length);
  }

  /**
   * Used to flip an int array to obtain ascending order.
   * 
   * @param a   The integer array whose order is to be inverted.
   * @param off The index of the first element to be reversed
   * @param len The number of elements to be reversed
   */
  public static void reverseOrder(int[] a, int off, int len)
  {
    // only flip if the array length is larger than 1

    if (len > 1)
    {
      // set i to the first index and j to the last and loop while i<j

      int i = off;
      int j = len + off - 1;
      while (i < j)
      {
        // sway a[i] with a[j] and increment i and decrement j

        int tmp = a[i];
        a[i++]  = a[j];
        a[j--]  = tmp;
      }
    }
  }

  /**
   * Sorts the specified sub-array of integers into ascending order.
   * This function was taken from the Java Arrays class and modified to
   * sort the input value array (v) in place based on swapping assignments
   * occurring on the sort-able array x.
   */
  public static void sort(int x[], int v[], int off, int len)
  {
    // Insertion sort on smallest arrays

    if (len < 7)
    {
      for (int i = off; i < len + off; i++)
      {
        for (int j = i; j > off && (x[j-1] > x[j]); j--) swap(x, v, j, j-1);
      }
      return;
    }

    // Choose a partition element, v ... for small arrays pick middle element

    int m = off + (len >> 1);
    if (len > 7)
    {
      int l = off;
      int n = off + len - 1;
      if (len > 40)
      {
        // Big arrays, pseudo-median of 9

        int s = len/8;
        l = med3(x, l,     l+s, l+2*s);
        m = med3(x, m-s,   m,   m+s);
        n = med3(x, n-2*s, n-s, n);
      }
      m = med3(x, l, m, n); // Mid-size, med of 3
    }
    int vv = x[m];

    // Establish Invariant: v* (<v)* (>v)* v*

    int a = off, b = a, c = off + len - 1, d = c;
    while(true)
    {
      while ((b <= c) && (x[b] <= vv))
      {
        if (x[b] == vv) swap(x, v, a++, b);
        b++;
      }
      while ((c >= b) && (x[c] >= vv))
      {
        if (x[c] == vv) swap(x, v, c, d--);
        c--;
      }
      if (b > c) break;
      swap(x, v, b++, c--);
    }

    // Swap partition elements back to middle

    int s, n = off + len;
    s = Math.min(a-off, b-a  );
    vecswap(x, v, off, b-s, s);
    s = Math.min(d-c,   n-d-1);
    vecswap(x, v, b,   n-s, s);

    // Recursively sort non-partition-elements

    if ((s = b-a) > 1) sort(x, v, off, s);
    if ((s = d-c) > 1) sort(x, v, n-s, s);
  }

  /**
   * Sorts the specified sub-array of integers into ascending order.
   * This function was taken from the Java Arrays class and modified to
   * sort the input value arrays (v1 and v2) in place based on swapping
   * assignments occurring on the sort-able array x.
   */
  public static void sort(int x[], int v1[], double v2[], int off, int len)
  {
    // Insertion sort on smallest arrays

    if (len < 7)
    {
      for (int i = off; i < len + off; i++)
      {
        for (int j = i; j > off && (x[j-1] > x[j]); j--) swap(x, v1, v2, j, j-1);
      }
      return;
    }

    // Choose a partition element, vv ... for small arrays pick middle element

    int m = off + (len >> 1);
    if (len > 7)
    {
      int l = off;
      int n = off + len - 1;
      if (len > 40)
      {
        // Big arrays, pseudo-median of 9

        int s = len/8;
        l = med3(x, l,     l+s, l+2*s);
        m = med3(x, m-s,   m,   m+s);
        n = med3(x, n-2*s, n-s, n);
      }
      m = med3(x, l, m, n); // Mid-size, med of 3
    }
    int vv = x[m];

    // Establish Invariant: vv* (<vv)* (>vv)* vv*

    int a = off, b = a, c = off + len - 1, d = c;
    while(true)
    {
      while ((b <= c) && (x[b] <= vv))
      {
        if (x[b] == vv) swap(x, v1, v2, a++, b);
        b++;
      }
      while ((c >= b) && (x[c] >= vv))
      {
        if (x[c] == vv) swap(x, v1, v2, c, d--);
        c--;
      }
      if (b > c) break;
      swap(x, v1, v2, b++, c--);
    }

    // Swap partition elements back to middle

    int s, n = off + len;
    s = Math.min(a-off, b-a  );
    vecswap(x, v1, v2, off, b-s, s);
    s = Math.min(d-c,   n-d-1);
    vecswap(x, v1, v2, b,   n-s, s);

    // Recursively sort non-partition-elements

    if ((s = b-a) > 1) sort(x, v1, v2, off, s);
    if ((s = d-c) > 1) sort(x, v1, v2, n-s, s);
  }

  /**
   * Sorts the specified sub-array of integers into ascending order.
   * This function was taken from the Java Arrays class and modified to
   * sort the input value array (v) in place based on swapping assignments
   * occurring on the sort-able array x.
   */
  public static <T> void sort(int x[], long v[], int off, int len)
  {
    // Insertion sort on smallest arrays

    if (len < 7)
    {
      for (int i = off; i < len + off; i++)
      {
        for (int j = i; j > off && (x[j-1] > x[j]); j--) swap(x, v, j, j-1);
      }
      return;
    }

    // Choose a partition element, v ... for small arrays pick middle element

    int m = off + (len >> 1);
    if (len > 7)
    {
      int l = off;
      int n = off + len - 1;
      if (len > 40)
      {
        // Big arrays, pseudo-median of 9

        int s = len/8;
        l = med3(x, l,     l+s, l+2*s);
        m = med3(x, m-s,   m,   m+s);
        n = med3(x, n-2*s, n-s, n);
      }
      m = med3(x, l, m, n); // Mid-size, med of 3
    }
    int vv = x[m];

    // Establish Invariant: v* (<v)* (>v)* v*

    int a = off, b = a, c = off + len - 1, d = c;
    while(true)
    {
      while ((b <= c) && (x[b] <= vv))
      {
        if (x[b] == vv) swap(x, v, a++, b);
        b++;
      }
      while ((c >= b) && (x[c] >= vv))
      {
        if (x[c] == vv) swap(x, v, c, d--);
        c--;
      }
      if (b > c) break;
      swap(x, v, b++, c--);
    }

    // Swap partition elements back to middle

    int s, n = off + len;
    s = Math.min(a-off, b-a  );
    vecswap(x, v, off, b-s, s);
    s = Math.min(d-c,   n-d-1);
    vecswap(x, v, b,   n-s, s);

    // Recursively sort non-partition-elements

    if ((s = b-a) > 1) sort(x, v, off, s);
    if ((s = d-c) > 1) sort(x, v, n-s, s);
  }

  /**
   * Sorts the specified sub-array of integers into ascending order.
   * This function was taken from the Java Arrays class and modified to
   * sort the input value array (v) in place based on swapping assignments
   * occurring on the sort-able array x.
   */
  public static <T> void sort(int x[], float v[], int off, int len)
  {
    // Insertion sort on smallest arrays

    if (len < 7)
    {
      for (int i = off; i < len + off; i++)
      {
        for (int j = i; j > off && (x[j-1] > x[j]); j--) swap(x, v, j, j-1);
      }
      return;
    }

    // Choose a partition element, v ... for small arrays pick middle element

    int m = off + (len >> 1);
    if (len > 7)
    {
      int l = off;
      int n = off + len - 1;
      if (len > 40)
      {
        // Big arrays, pseudo-median of 9

        int s = len/8;
        l = med3(x, l,     l+s, l+2*s);
        m = med3(x, m-s,   m,   m+s);
        n = med3(x, n-2*s, n-s, n);
      }
      m = med3(x, l, m, n); // Mid-size, med of 3
    }
    int vv = x[m];

    // Establish Invariant: v* (<v)* (>v)* v*

    int a = off, b = a, c = off + len - 1, d = c;
    while(true)
    {
      while ((b <= c) && (x[b] <= vv))
      {
        if (x[b] == vv) swap(x, v, a++, b);
        b++;
      }
      while ((c >= b) && (x[c] >= vv))
      {
        if (x[c] == vv) swap(x, v, c, d--);
        c--;
      }
      if (b > c) break;
      swap(x, v, b++, c--);
    }

    // Swap partition elements back to middle

    int s, n = off + len;
    s = Math.min(a-off, b-a  );
    vecswap(x, v, off, b-s, s);
    s = Math.min(d-c,   n-d-1);
    vecswap(x, v, b,   n-s, s);

    // Recursively sort non-partition-elements

    if ((s = b-a) > 1) sort(x, v, off, s);
    if ((s = d-c) > 1) sort(x, v, n-s, s);
  }

  /**
   * Sorts the specified sub-array of integers into ascending order.
   * This function was taken from the Java Arrays class and modified to
   * sort the input value array (v) in place based on swapping assignments
   * occurring on the sort-able array x.
   */
  public static void sort(int x[], double v[], int off, int len)
  {
    // Insertion sort on smallest arrays

    if (len < 7)
    {
      for (int i = off; i < len + off; i++)
      {
        for (int j = i; j > off && (x[j-1] > x[j]); j--) swap(x, v, j, j-1);
      }
      return;
    }

    // Choose a partition element, v ... for small arrays pick middle element

    int m = off + (len >> 1);
    if (len > 7)
    {
      int l = off;
      int n = off + len - 1;
      if (len > 40)
      {
        // Big arrays, pseudo-median of 9

        int s = len/8;
        l = med3(x, l,     l+s, l+2*s);
        m = med3(x, m-s,   m,   m+s);
        n = med3(x, n-2*s, n-s, n);
      }
      m = med3(x, l, m, n); // Mid-size, med of 3
    }
    int vv = x[m];

    // Establish Invariant: v* (<v)* (>v)* v*

    int a = off, b = a, c = off + len - 1, d = c;
    while(true)
    {
      while ((b <= c) && (x[b] <= vv))
      {
        if (x[b] == vv) swap(x, v, a++, b);
        b++;
      }
      while ((c >= b) && (x[c] >= vv))
      {
        if (x[c] == vv) swap(x, v, c, d--);
        c--;
      }
      if (b > c) break;
      swap(x, v, b++, c--);
    }

    // Swap partition elements back to middle

    int s, n = off + len;
    s = Math.min(a-off, b-a  );
    vecswap(x, v, off, b-s, s);
    s = Math.min(d-c,   n-d-1);
    vecswap(x, v, b,   n-s, s);

    // Recursively sort non-partition-elements

    if ((s = b-a) > 1) sort(x, v, off, s);
    if ((s = d-c) > 1) sort(x, v, n-s, s);
  }

  /**
   * Sorts the specified sub-array of integers into ascending order.
   * This function was taken from the Java Arrays class and modified to
   * sort the input value array (v) in place based on swapping assignments
   * occurring on the sort-able array x.
   */
  public static <T> void sort(int x[], T v[], int off, int len)
  {
    // Insertion sort on smallest arrays

    if (len < 7)
    {
      for (int i = off; i < len + off; i++)
      {
        for (int j = i; j > off && (x[j-1] > x[j]); j--) swap(x, v, j, j-1);
      }
      return;
    }

    // Choose a partition element, v ... for small arrays pick middle element

    int m = off + (len >> 1);
    if (len > 7)
    {
      int l = off;
      int n = off + len - 1;
      if (len > 40)
      {
        // Big arrays, pseudo-median of 9

        int s = len/8;
        l = med3(x, l,     l+s, l+2*s);
        m = med3(x, m-s,   m,   m+s);
        n = med3(x, n-2*s, n-s, n);
      }
      m = med3(x, l, m, n); // Mid-size, med of 3
    }
    int vv = x[m];

    // Establish Invariant: v* (<v)* (>v)* v*

    int a = off, b = a, c = off + len - 1, d = c;
    while(true)
    {
      while ((b <= c) && (x[b] <= vv))
      {
        if (x[b] == vv) swap(x, v, a++, b);
        b++;
      }
      while ((c >= b) && (x[c] >= vv))
      {
        if (x[c] == vv) swap(x, v, c, d--);
        c--;
      }
      if (b > c) break;
      swap(x, v, b++, c--);
    }

    // Swap partition elements back to middle

    int s, n = off + len;
    s = Math.min(a-off, b-a  );
    vecswap(x, v, off, b-s, s);
    s = Math.min(d-c,   n-d-1);
    vecswap(x, v, b,   n-s, s);

    // Recursively sort non-partition-elements

    if ((s = b-a) > 1) sort(x, v, off, s);
    if ((s = d-c) > 1) sort(x, v, n-s, s);
  }

  /**
   * Sorts the specified sub-array of longs into ascending order.
   * This function was taken from the Java Arrays class and modified to
   * sort the input value array (v) in place based on swapping assignments
   * occurring on the sort-able array x.
   */
  public static void sort(long x[], int v[], int off, int len)
  {
    // Insertion sort on smallest arrays

    if (len < 7)
    {
      for (int i = off; i < len + off; i++)
      {
        for (int j = i; j > off && (x[j-1] > x[j]); j--) swap(x, v, j, j-1);
      }
      return;
    }

    // Choose a partition element, v ... for small arrays pick middle element

    int m = off + (len >> 1);
    if (len > 7)
    {
      int l = off;
      int n = off + len - 1;
      if (len > 40)
      {
        // Big arrays, pseudo-median of 9

        int s = len/8;
        l = med3(x, l,     l+s, l+2*s);
        m = med3(x, m-s,   m,   m+s);
        n = med3(x, n-2*s, n-s, n);
      }
      m = med3(x, l, m, n); // Mid-size, med of 3
    }
    long vv = x[m];

    // Establish Invariant: v* (<v)* (>v)* v*

    int a = off, b = a, c = off + len - 1, d = c;
    while(true)
    {
      while ((b <= c) && (x[b] <= vv))
      {
        if (x[b] == vv) swap(x, v, a++, b);
        b++;
      }
      while ((c >= b) && (x[c] >= vv))
      {
        if (x[c] == vv) swap(x, v, c, d--);
        c--;
      }
      if (b > c) break;
      swap(x, v, b++, c--);
    }

    // Swap partition elements back to middle

    int s, n = off + len;
    s = Math.min(a-off, b-a  );
    vecswap(x, v, off, b-s, s);
    s = Math.min(d-c,   n-d-1);
    vecswap(x, v, b,   n-s, s);

    // Recursively sort non-partition-elements

    if ((s = b-a) > 1) sort(x, v, off, s);
    if ((s = d-c) > 1) sort(x, v, n-s, s);
  }

  /**
   * Sorts the specified sub-array of longs into ascending order.
   * This function was taken from the Java Arrays class and modified to
   * sort the input value array (v) in place based on swapping assignments
   * occurring on the sort-able array x.
   */
  public static void sort(long x[], long v[], int off, int len)
  {
    // Insertion sort on smallest arrays

    if (len < 7)
    {
      for (int i = off; i < len + off; i++)
      {
        for (int j = i; j > off && (x[j-1] > x[j]); j--) swap(x, v, j, j-1);
      }
      return;
    }

    // Choose a partition element, v ... for small arrays pick middle element

    int m = off + (len >> 1);
    if (len > 7)
    {
      int l = off;
      int n = off + len - 1;
      if (len > 40)
      {
        // Big arrays, pseudo-median of 9

        int s = len/8;
        l = med3(x, l,     l+s, l+2*s);
        m = med3(x, m-s,   m,   m+s);
        n = med3(x, n-2*s, n-s, n);
      }
      m = med3(x, l, m, n); // Mid-size, med of 3
    }
    long vv = x[m];

    // Establish Invariant: v* (<v)* (>v)* v*

    int a = off, b = a, c = off + len - 1, d = c;
    while(true)
    {
      while ((b <= c) && (x[b] <= vv))
      {
        if (x[b] == vv) swap(x, v, a++, b);
        b++;
      }
      while ((c >= b) && (x[c] >= vv))
      {
        if (x[c] == vv) swap(x, v, c, d--);
        c--;
      }
      if (b > c) break;
      swap(x, v, b++, c--);
    }

    // Swap partition elements back to middle

    int s, n = off + len;
    s = Math.min(a-off, b-a  );
    vecswap(x, v, off, b-s, s);
    s = Math.min(d-c,   n-d-1);
    vecswap(x, v, b,   n-s, s);

    // Recursively sort non-partition-elements

    if ((s = b-a) > 1) sort(x, v, off, s);
    if ((s = d-c) > 1) sort(x, v, n-s, s);
  }

  /**
   * Sorts the specified sub-array of longs into ascending order.
   * This function was taken from the Java Arrays class and modified to
   * sort the input value array (v) in place based on swapping assignments
   * occurring on the sort-able array x.
   */
  public static void sort(long x[], float v[], int off, int len)
  {
    // Insertion sort on smallest arrays

    if (len < 7)
    {
      for (int i = off; i < len + off; i++)
      {
        for (int j = i; j > off && (x[j-1] > x[j]); j--) swap(x, v, j, j-1);
      }
      return;
    }

    // Choose a partition element, v ... for small arrays pick middle element

    int m = off + (len >> 1);
    if (len > 7)
    {
      int l = off;
      int n = off + len - 1;
      if (len > 40)
      {
        // Big arrays, pseudo-median of 9

        int s = len/8;
        l = med3(x, l,     l+s, l+2*s);
        m = med3(x, m-s,   m,   m+s);
        n = med3(x, n-2*s, n-s, n);
      }
      m = med3(x, l, m, n); // Mid-size, med of 3
    }
    long vv = x[m];

    // Establish Invariant: v* (<v)* (>v)* v*

    int a = off, b = a, c = off + len - 1, d = c;
    while(true)
    {
      while ((b <= c) && (x[b] <= vv))
      {
        if (x[b] == vv) swap(x, v, a++, b);
        b++;
      }
      while ((c >= b) && (x[c] >= vv))
      {
        if (x[c] == vv) swap(x, v, c, d--);
        c--;
      }
      if (b > c) break;
      swap(x, v, b++, c--);
    }

    // Swap partition elements back to middle

    int s, n = off + len;
    s = Math.min(a-off, b-a  );
    vecswap(x, v, off, b-s, s);
    s = Math.min(d-c,   n-d-1);
    vecswap(x, v, b,   n-s, s);

    // Recursively sort non-partition-elements

    if ((s = b-a) > 1) sort(x, v, off, s);
    if ((s = d-c) > 1) sort(x, v, n-s, s);
  }

  /**
   * Sorts the specified sub-array of longs into ascending order.
   * This function was taken from the Java Arrays class and modified to
   * sort the input value array (v) in place based on swapping assignments
   * occurring on the sort-able array x.
   */
  public static void sort(long x[], double v[], int off, int len)
  {
    // Insertion sort on smallest arrays

    if (len < 7)
    {
      for (int i = off; i < len + off; i++)
      {
        for (int j = i; j > off && (x[j-1] > x[j]); j--) swap(x, v, j, j-1);
      }
      return;
    }

    // Choose a partition element, v ... for small arrays pick middle element

    int m = off + (len >> 1);
    if (len > 7)
    {
      int l = off;
      int n = off + len - 1;
      if (len > 40)
      {
        // Big arrays, pseudo-median of 9

        int s = len/8;
        l = med3(x, l,     l+s, l+2*s);
        m = med3(x, m-s,   m,   m+s);
        n = med3(x, n-2*s, n-s, n);
      }
      m = med3(x, l, m, n); // Mid-size, med of 3
    }
    long vv = x[m];

    // Establish Invariant: v* (<v)* (>v)* v*

    int a = off, b = a, c = off + len - 1, d = c;
    while(true)
    {
      while ((b <= c) && (x[b] <= vv))
      {
        if (x[b] == vv) swap(x, v, a++, b);
        b++;
      }
      while ((c >= b) && (x[c] >= vv))
      {
        if (x[c] == vv) swap(x, v, c, d--);
        c--;
      }
      if (b > c) break;
      swap(x, v, b++, c--);
    }

    // Swap partition elements back to middle

    int s, n = off + len;
    s = Math.min(a-off, b-a  );
    vecswap(x, v, off, b-s, s);
    s = Math.min(d-c,   n-d-1);
    vecswap(x, v, b,   n-s, s);

    // Recursively sort non-partition-elements

    if ((s = b-a) > 1) sort(x, v, off, s);
    if ((s = d-c) > 1) sort(x, v, n-s, s);
  }

  /**
   * Sorts the specified sub-array of longs into ascending order.
   * This function was taken from the Java Arrays class and modified to
   * sort the input value array (v) in place based on swapping assignments
   * occurring on the sort-able array x.
   */
  public static <T> void sort(long x[], T v[], int off, int len)
  {
    // Insertion sort on smallest arrays

    if (len < 7)
    {
      for (int i = off; i < len + off; i++)
      {
        for (int j = i; j > off && (x[j-1] > x[j]); j--) swap(x, v, j, j-1);
      }
      return;
    }

    // Choose a partition element, v ... for small arrays pick middle element

    int m = off + (len >> 1);
    if (len > 7)
    {
      int l = off;
      int n = off + len - 1;
      if (len > 40)
      {
        // Big arrays, pseudo-median of 9

        int s = len/8;
        l = med3(x, l,     l+s, l+2*s);
        m = med3(x, m-s,   m,   m+s);
        n = med3(x, n-2*s, n-s, n);
      }
      m = med3(x, l, m, n); // Mid-size, med of 3
    }
    long vv = x[m];

    // Establish Invariant: v* (<v)* (>v)* v*

    int a = off, b = a, c = off + len - 1, d = c;
    while(true)
    {
      while ((b <= c) && (x[b] <= vv))
      {
        if (x[b] == vv) swap(x, v, a++, b);
        b++;
      }
      while ((c >= b) && (x[c] >= vv))
      {
        if (x[c] == vv) swap(x, v, c, d--);
        c--;
      }
      if (b > c) break;
      swap(x, v, b++, c--);
    }

    // Swap partition elements back to middle

    int s, n = off + len;
    s = Math.min(a-off, b-a  );
    vecswap(x, v, off, b-s, s);
    s = Math.min(d-c,   n-d-1);
    vecswap(x, v, b,   n-s, s);

    // Recursively sort non-partition-elements

    if ((s = b-a) > 1) sort(x, v, off, s);
    if ((s = d-c) > 1) sort(x, v, n-s, s);
  }

  /**
   * Sorts the specified sub-array of floats into ascending order.
   * This function was taken from the Java Arrays class and modified to
   * sort the input value array (v) in place based on swapping assignments
   * occurring on the sort-able array x.
   */
  public static void sort(float x[], int v[], int off, int len)
  {
    // Insertion sort on smallest arrays

    if (len < 7)
    {
      for (int i = off; i < len + off; i++)
      {
        for (int j = i; j > off && (x[j-1] > x[j]); j--) swap(x, v, j, j-1);
      }
      return;
    }

    // Choose a partition element, v ... for small arrays pick middle element

    int m = off + (len >> 1);
    if (len > 7)
    {
      int l = off;
      int n = off + len - 1;
      if (len > 40)
      {
        // Big arrays, pseudo-median of 9

        int s = len/8;
        l = med3(x, l,     l+s, l+2*s);
        m = med3(x, m-s,   m,   m+s);
        n = med3(x, n-2*s, n-s, n);
      }
      m = med3(x, l, m, n); // Mid-size, med of 3
    }
    float vv = x[m];

    // Establish Invariant: v* (<v)* (>v)* v*

    int a = off, b = a, c = off + len - 1, d = c;
    while(true)
    {
      while ((b <= c) && (x[b] <= vv))
      {
        if (x[b] == vv) swap(x, v, a++, b);
        b++;
      }
      while ((c >= b) && (x[c] >= vv))
      {
        if (x[c] == vv) swap(x, v, c, d--);
        c--;
      }
      if (b > c) break;
      swap(x, v, b++, c--);
    }

    // Swap partition elements back to middle

    int s, n = off + len;
    s = Math.min(a-off, b-a  );
    vecswap(x, v, off, b-s, s);
    s = Math.min(d-c,   n-d-1);
    vecswap(x, v, b,   n-s, s);

    // Recursively sort non-partition-elements

    if ((s = b-a) > 1) sort(x, v, off, s);
    if ((s = d-c) > 1) sort(x, v, n-s, s);
  }

  /**
   * Sorts the specified sub-array of floats into ascending order.
   * This function was taken from the Java Arrays class and modified to
   * sort the input value array (v) in place based on swapping assignments
   * occurring on the sort-able array x.
   */
  public static void sort(float x[], long v[], int off, int len)
  {
    // Insertion sort on smallest arrays

    if (len < 7)
    {
      for (int i = off; i < len + off; i++)
      {
        for (int j = i; j > off && (x[j-1] > x[j]); j--) swap(x, v, j, j-1);
      }
      return;
    }

    // Choose a partition element, v ... for small arrays pick middle element

    int m = off + (len >> 1);
    if (len > 7)
    {
      int l = off;
      int n = off + len - 1;
      if (len > 40)
      {
        // Big arrays, pseudo-median of 9

        int s = len/8;
        l = med3(x, l,     l+s, l+2*s);
        m = med3(x, m-s,   m,   m+s);
        n = med3(x, n-2*s, n-s, n);
      }
      m = med3(x, l, m, n); // Mid-size, med of 3
    }
    float vv = x[m];

    // Establish Invariant: v* (<v)* (>v)* v*

    int a = off, b = a, c = off + len - 1, d = c;
    while(true)
    {
      while ((b <= c) && (x[b] <= vv))
      {
        if (x[b] == vv) swap(x, v, a++, b);
        b++;
      }
      while ((c >= b) && (x[c] >= vv))
      {
        if (x[c] == vv) swap(x, v, c, d--);
        c--;
      }
      if (b > c) break;
      swap(x, v, b++, c--);
    }

    // Swap partition elements back to middle

    int s, n = off + len;
    s = Math.min(a-off, b-a  );
    vecswap(x, v, off, b-s, s);
    s = Math.min(d-c,   n-d-1);
    vecswap(x, v, b,   n-s, s);

    // Recursively sort non-partition-elements

    if ((s = b-a) > 1) sort(x, v, off, s);
    if ((s = d-c) > 1) sort(x, v, n-s, s);
  }

  /**
   * Sorts the specified sub-array of floats into ascending order.
   * This function was taken from the Java Arrays class and modified to
   * sort the input value array (v) in place based on swapping assignments
   * occurring on the sort-able array x.
   */
  public static void sort(float x[], float v[], int off, int len)
  {
    // Insertion sort on smallest arrays

    if (len < 7)
    {
      for (int i = off; i < len + off; i++)
      {
        for (int j = i; j > off && (x[j-1] > x[j]); j--) swap(x, v, j, j-1);
      }
      return;
    }

    // Choose a partition element, v ... for small arrays pick middle element

    int m = off + (len >> 1);
    if (len > 7)
    {
      int l = off;
      int n = off + len - 1;
      if (len > 40)
      {
        // Big arrays, pseudo-median of 9

        int s = len/8;
        l = med3(x, l,     l+s, l+2*s);
        m = med3(x, m-s,   m,   m+s);
        n = med3(x, n-2*s, n-s, n);
      }
      m = med3(x, l, m, n); // Mid-size, med of 3
    }
    float vv = x[m];

    // Establish Invariant: v* (<v)* (>v)* v*

    int a = off, b = a, c = off + len - 1, d = c;
    while(true)
    {
      while ((b <= c) && (x[b] <= vv))
      {
        if (x[b] == vv) swap(x, v, a++, b);
        b++;
      }
      while ((c >= b) && (x[c] >= vv))
      {
        if (x[c] == vv) swap(x, v, c, d--);
        c--;
      }
      if (b > c) break;
      swap(x, v, b++, c--);
    }

    // Swap partition elements back to middle

    int s, n = off + len;
    s = Math.min(a-off, b-a  );
    vecswap(x, v, off, b-s, s);
    s = Math.min(d-c,   n-d-1);
    vecswap(x, v, b,   n-s, s);

    // Recursively sort non-partition-elements

    if ((s = b-a) > 1) sort(x, v, off, s);
    if ((s = d-c) > 1) sort(x, v, n-s, s);
  }

  /**
   * Sorts the specified sub-array of floats into ascending order.
   * This function was taken from the Java Arrays class and modified to
   * sort the input value array (v) in place based on swapping assignments
   * occurring on the sort-able array x.
   */
  public static void sort(float x[], double v[], int off, int len)
  {
    // Insertion sort on smallest arrays

    if (len < 7)
    {
      for (int i = off; i < len + off; i++)
      {
        for (int j = i; j > off && (x[j-1] > x[j]); j--) swap(x, v, j, j-1);
      }
      return;
    }

    // Choose a partition element, v ... for small arrays pick middle element

    int m = off + (len >> 1);
    if (len > 7)
    {
      int l = off;
      int n = off + len - 1;
      if (len > 40)
      {
        // Big arrays, pseudo-median of 9

        int s = len/8;
        l = med3(x, l,     l+s, l+2*s);
        m = med3(x, m-s,   m,   m+s);
        n = med3(x, n-2*s, n-s, n);
      }
      m = med3(x, l, m, n); // Mid-size, med of 3
    }
    float vv = x[m];

    // Establish Invariant: v* (<v)* (>v)* v*

    int a = off, b = a, c = off + len - 1, d = c;
    while(true)
    {
      while ((b <= c) && (x[b] <= vv))
      {
        if (x[b] == vv) swap(x, v, a++, b);
        b++;
      }
      while ((c >= b) && (x[c] >= vv))
      {
        if (x[c] == vv) swap(x, v, c, d--);
        c--;
      }
      if (b > c) break;
      swap(x, v, b++, c--);
    }

    // Swap partition elements back to middle

    int s, n = off + len;
    s = Math.min(a-off, b-a  );
    vecswap(x, v, off, b-s, s);
    s = Math.min(d-c,   n-d-1);
    vecswap(x, v, b,   n-s, s);

    // Recursively sort non-partition-elements

    if ((s = b-a) > 1) sort(x, v, off, s);
    if ((s = d-c) > 1) sort(x, v, n-s, s);
  }

  /**
   * Sorts the specified sub-array of floats into ascending order.
   * This function was taken from the Java Arrays class and modified to
   * sort the input value array (v) in place based on swapping assignments
   * occurring on the sort-able array x.
   */
  public static <T> void sort(float x[], T v[], int off, int len)
  {
    // Insertion sort on smallest arrays

    if (len < 7)
    {
      for (int i = off; i < len + off; i++)
      {
        for (int j = i; j > off && (x[j-1] > x[j]); j--) swap(x, v, j, j-1);
      }
      return;
    }

    // Choose a partition element, v ... for small arrays pick middle element

    int m = off + (len >> 1);
    if (len > 7)
    {
      int l = off;
      int n = off + len - 1;
      if (len > 40)
      {
        // Big arrays, pseudo-median of 9

        int s = len/8;
        l = med3(x, l,     l+s, l+2*s);
        m = med3(x, m-s,   m,   m+s);
        n = med3(x, n-2*s, n-s, n);
      }
      m = med3(x, l, m, n); // Mid-size, med of 3
    }
    float vv = x[m];

    // Establish Invariant: v* (<v)* (>v)* v*

    int a = off, b = a, c = off + len - 1, d = c;
    while(true)
    {
      while ((b <= c) && (x[b] <= vv))
      {
        if (x[b] == vv) swap(x, v, a++, b);
        b++;
      }
      while ((c >= b) && (x[c] >= vv))
      {
        if (x[c] == vv) swap(x, v, c, d--);
        c--;
      }
      if (b > c) break;
      swap(x, v, b++, c--);
    }

    // Swap partition elements back to middle

    int s, n = off + len;
    s = Math.min(a-off, b-a  );
    vecswap(x, v, off, b-s, s);
    s = Math.min(d-c,   n-d-1);
    vecswap(x, v, b,   n-s, s);

    // Recursively sort non-partition-elements

    if ((s = b-a) > 1) sort(x, v, off, s);
    if ((s = d-c) > 1) sort(x, v, n-s, s);
  }

  /**
   * Sorts the specified sub-array of doubles into ascending order.
   * This function was taken from the Java Arrays class and modified to
   * sort the input value array (v) in place based on swapping assignments
   * occurring on the sort-able array x.
   */
  public static void sort(double x[], int v[], int off, int len)
  {
    // Insertion sort on smallest arrays

    if (len < 7)
    {
      for (int i = off; i < len + off; i++)
      {
        for (int j = i; j > off && (x[j-1] > x[j]); j--) swap(x, v, j, j-1);
      }
      return;
    }

    // Choose a partition element, v ... for small arrays pick middle element

    int m = off + (len >> 1);
    if (len > 7)
    {
      int l = off;
      int n = off + len - 1;
      if (len > 40)
      {
        // Big arrays, pseudo-median of 9

        int s = len/8;
        l = med3(x, l,     l+s, l+2*s);
        m = med3(x, m-s,   m,   m+s);
        n = med3(x, n-2*s, n-s, n);
      }
      m = med3(x, l, m, n); // Mid-size, med of 3
    }
    double vv = x[m];

    // Establish Invariant: v* (<v)* (>v)* v*

    int a = off, b = a, c = off + len - 1, d = c;
    while(true)
    {
      while ((b <= c) && (x[b] <= vv))
      {
        if (x[b] == vv) swap(x, v, a++, b);
        b++;
      }
      while ((c >= b) && (x[c] >= vv))
      {
        if (x[c] == vv) swap(x, v, c, d--);
        c--;
      }
      if (b > c) break;
      swap(x, v, b++, c--);
    }

    // Swap partition elements back to middle

    int s, n = off + len;
    s = Math.min(a-off, b-a  );
    vecswap(x, v, off, b-s, s);
    s = Math.min(d-c,   n-d-1);
    vecswap(x, v, b,   n-s, s);

    // Recursively sort non-partition-elements

    if ((s = b-a) > 1) sort(x, v, off, s);
    if ((s = d-c) > 1) sort(x, v, n-s, s);
  }

  /**
   * Sorts the specified sub-array of doubles into ascending order.
   * This function was taken from the Java Arrays class and modified to
   * sort the input value array (v) in place based on swapping assignments
   * occurring on the sort-able array x.
   */
  public static void sort(double x[], long v[], int off, int len)
  {
    // Insertion sort on smallest arrays

    if (len < 7)
    {
      for (int i = off; i < len + off; i++)
      {
        for (int j = i; j > off && (x[j-1] > x[j]); j--) swap(x, v, j, j-1);
      }
      return;
    }

    // Choose a partition element, v ... for small arrays pick middle element

    int m = off + (len >> 1);
    if (len > 7)
    {
      int l = off;
      int n = off + len - 1;
      if (len > 40)
      {
        // Big arrays, pseudo-median of 9

        int s = len/8;
        l = med3(x, l,     l+s, l+2*s);
        m = med3(x, m-s,   m,   m+s);
        n = med3(x, n-2*s, n-s, n);
      }
      m = med3(x, l, m, n); // Mid-size, med of 3
    }
    double vv = x[m];

    // Establish Invariant: v* (<v)* (>v)* v*

    int a = off, b = a, c = off + len - 1, d = c;
    while(true)
    {
      while ((b <= c) && (x[b] <= vv))
      {
        if (x[b] == vv) swap(x, v, a++, b);
        b++;
      }
      while ((c >= b) && (x[c] >= vv))
      {
        if (x[c] == vv) swap(x, v, c, d--);
        c--;
      }
      if (b > c) break;
      swap(x, v, b++, c--);
    }

    // Swap partition elements back to middle

    int s, n = off + len;
    s = Math.min(a-off, b-a  );
    vecswap(x, v, off, b-s, s);
    s = Math.min(d-c,   n-d-1);
    vecswap(x, v, b,   n-s, s);

    // Recursively sort non-partition-elements

    if ((s = b-a) > 1) sort(x, v, off, s);
    if ((s = d-c) > 1) sort(x, v, n-s, s);
  }

  /**
   * Sorts the specified sub-array of doubles into ascending order.
   * This function was taken from the Java Arrays class and modified to
   * sort the input value array (v) in place based on swapping assignments
   * occurring on the sort-able array x.
   */
  public static void sort(double x[], float v[], int off, int len)
  {
    // Insertion sort on smallest arrays

    if (len < 7)
    {
      for (int i = off; i < len + off; i++)
      {
        for (int j = i; j > off && (x[j-1] > x[j]); j--) swap(x, v, j, j-1);
      }
      return;
    }

    // Choose a partition element, v ... for small arrays pick middle element

    int m = off + (len >> 1);
    if (len > 7)
    {
      int l = off;
      int n = off + len - 1;
      if (len > 40)
      {
        // Big arrays, pseudo-median of 9

        int s = len/8;
        l = med3(x, l,     l+s, l+2*s);
        m = med3(x, m-s,   m,   m+s);
        n = med3(x, n-2*s, n-s, n);
      }
      m = med3(x, l, m, n); // Mid-size, med of 3
    }
    double vv = x[m];

    // Establish Invariant: v* (<v)* (>v)* v*

    int a = off, b = a, c = off + len - 1, d = c;
    while(true)
    {
      while ((b <= c) && (x[b] <= vv))
      {
        if (x[b] == vv) swap(x, v, a++, b);
        b++;
      }
      while ((c >= b) && (x[c] >= vv))
      {
        if (x[c] == vv) swap(x, v, c, d--);
        c--;
      }
      if (b > c) break;
      swap(x, v, b++, c--);
    }

    // Swap partition elements back to middle

    int s, n = off + len;
    s = Math.min(a-off, b-a  );
    vecswap(x, v, off, b-s, s);
    s = Math.min(d-c,   n-d-1);
    vecswap(x, v, b,   n-s, s);

    // Recursively sort non-partition-elements

    if ((s = b-a) > 1) sort(x, v, off, s);
    if ((s = d-c) > 1) sort(x, v, n-s, s);
  }

  /**
   * Sorts the specified sub-array of doubles into ascending order.
   * This function was taken from the Java Arrays class and modified to
   * sort the input value array (v) in place based on swapping assignments
   * occurring on the sort-able array x.
   */
  public static void sort(double x[], double v[], int off, int len)
  {
    // Insertion sort on smallest arrays

    if (len < 7)
    {
      for (int i = off; i < len + off; i++)
      {
        for (int j = i; j > off && (x[j-1] > x[j]); j--) swap(x, v, j, j-1);
      }
      return;
    }

    // Choose a partition element, v ... for small arrays pick middle element

    int m = off + (len >> 1);
    if (len > 7)
    {
      int l = off;
      int n = off + len - 1;
      if (len > 40)
      {
        // Big arrays, pseudo-median of 9

        int s = len/8;
        l = med3(x, l,     l+s, l+2*s);
        m = med3(x, m-s,   m,   m+s);
        n = med3(x, n-2*s, n-s, n);
      }
      m = med3(x, l, m, n); // Mid-size, med of 3
    }
    double vv = x[m];

    // Establish Invariant: v* (<v)* (>v)* v*

    int a = off, b = a, c = off + len - 1, d = c;
    while(true)
    {
      while ((b <= c) && (x[b] <= vv))
      {
        if (x[b] == vv) swap(x, v, a++, b);
        b++;
      }
      while ((c >= b) && (x[c] >= vv))
      {
        if (x[c] == vv) swap(x, v, c, d--);
        c--;
      }
      if (b > c) break;
      swap(x, v, b++, c--);
    }

    // Swap partition elements back to middle

    int s, n = off + len;
    s = Math.min(a-off, b-a  );
    vecswap(x, v, off, b-s, s);
    s = Math.min(d-c,   n-d-1);
    vecswap(x, v, b,   n-s, s);

    // Recursively sort non-partition-elements

    if ((s = b-a) > 1) sort(x, v, off, s);
    if ((s = d-c) > 1) sort(x, v, n-s, s);
  }

  /**
   * Sorts the specified sub-array of doubles into ascending order.
   * This function was taken from the Java Arrays class and modified to
   * sort the input value array (v) in place based on swapping assignments
   * occurring on the sort-able array x.
   */
  public static <T> void sort(double x[], T v[], int off, int len)
  {
    // Insertion sort on smallest arrays

    if (len < 7)
    {
      for (int i = off; i < len + off; i++)
      {
        for (int j = i; j > off && (x[j-1] > x[j]); j--) swap(x, v, j, j-1);
      }
      return;
    }

    // Choose a partition element, v ... for small arrays pick middle element

    int m = off + (len >> 1);
    if (len > 7)
    {
      int l = off;
      int n = off + len - 1;
      if (len > 40)
      {
        // Big arrays, pseudo-median of 9

        int s = len/8;
        l = med3(x, l,     l+s, l+2*s);
        m = med3(x, m-s,   m,   m+s);
        n = med3(x, n-2*s, n-s, n);
      }
      m = med3(x, l, m, n); // Mid-size, med of 3
    }
    double vv = x[m];

    // Establish Invariant: v* (<v)* (>v)* v*

    int a = off, b = a, c = off + len - 1, d = c;
    while(true)
    {
      while ((b <= c) && (x[b] <= vv))
      {
        if (x[b] == vv) swap(x, v, a++, b);
        b++;
      }
      while ((c >= b) && (x[c] >= vv))
      {
        if (x[c] == vv) swap(x, v, c, d--);
        c--;
      }
      if (b > c) break;
      swap(x, v, b++, c--);
    }

    // Swap partition elements back to middle

    int s, n = off + len;
    s = Math.min(a-off, b-a  );
    vecswap(x, v, off, b-s, s);
    s = Math.min(d-c,   n-d-1);
    vecswap(x, v, b,   n-s, s);

    // Recursively sort non-partition-elements

    if ((s = b-a) > 1) sort(x, v, off, s);
    if ((s = d-c) > 1) sort(x, v, n-s, s);
  }

  /**
   * Swaps x[a] with x[b] and v[a] with v[b].
   */
  private static void swap(int x[], int v[], int a, int b)
  {
    int t  = x[a];
    x[a]   = x[b];
    x[b]   = t;

    int tv = v[a];
    v[a]   = v[b];
    v[b]   = tv;
  }

  /**
   * Swaps x[a] with x[b] and v[a] with v[b].
   */
  private static void swap(int x[], int v1[], double v2[], int a, int b)
  {
    int t  = x[a];
    x[a]   = x[b];
    x[b]   = t;

    int tv1 = v1[a];
    v1[a]   = v1[b];
    v1[b]   = tv1;

    double tv2 = v2[a];
    v2[a]   = v2[b];
    v2[b]   = tv2;
  }

  /**
   * Swaps x[a] with x[b] and v[a] with v[b].
   */
  private static void swap(int x[], long v[], int a, int b)
  {
    int t  = x[a];
    x[a]   = x[b];
    x[b]   = t;

    long tv = v[a];
    v[a]    = v[b];
    v[b]    = tv;
  }

  /**
   * Swaps x[a] with x[b] and v[a] with v[b].
   */
  private static void swap(int x[], float v[], int a, int b)
  {
    int t  = x[a];
    x[a]   = x[b];
    x[b]   = t;

    float tv = v[a];
    v[a]     = v[b];
    v[b]     = tv;
  }

  /**
   * Swaps x[a] with x[b] and v[a] with v[b].
   */
  private static void swap(int x[], double v[], int a, int b)
  {
    int t  = x[a];
    x[a]   = x[b];
    x[b]   = t;

    double tv = v[a];
    v[a]      = v[b];
    v[b]      = tv;
  }

  /**
   * Swaps x[a] with x[b] and v[a] with v[b].
   */
  private static <T> void swap(int x[], T v[], int a, int b)
  {
    int t  = x[a];
    x[a]   = x[b];
    x[b]   = t;

    T tv   = v[a];
    v[a]   = v[b];
    v[b]   = tv;
  }

  /**
   * Swaps x[a] with x[b] and v[a] with v[b].
   */
  private static void swap(long x[], int v[], int a, int b)
  {
    long t  = x[a];
    x[a]    = x[b];
    x[b]    = t;

    int tv  = v[a];
    v[a]    = v[b];
    v[b]    = tv;
  }

  /**
   * Swaps x[a] with x[b] and v[a] with v[b].
   */
  private static void swap(long x[], long v[], int a, int b)
  {
    long t  = x[a];
    x[a]    = x[b];
    x[b]    = t;

    long tv = v[a];
    v[a]    = v[b];
    v[b]    = tv;
  }

  /**
   * Swaps x[a] with x[b] and v[a] with v[b].
   */
  private static void swap(long x[], float v[], int a, int b)
  {
    long t  = x[a];
    x[a]    = x[b];
    x[b]    = t;

    float tv = v[a];
    v[a]     = v[b];
    v[b]     = tv;
  }

  /**
   * Swaps x[a] with x[b] and v[a] with v[b].
   */
  private static void swap(long x[], double v[], int a, int b)
  {
    long t  = x[a];
    x[a]    = x[b];
    x[b]    = t;

    double tv = v[a];
    v[a]      = v[b];
    v[b]      = tv;
  }

  /**
   * Swaps x[a] with x[b] and v[a] with v[b].
   */
  private static <T> void swap(long x[], T v[], int a, int b)
  {
    long t  = x[a];
    x[a]    = x[b];
    x[b]    = t;

    T tv    = v[a];
    v[a]    = v[b];
    v[b]    = tv;
  }

  /**
   * Swaps x[a] with x[b] and v[a] with v[b].
   */
  private static void swap(float x[], int v[], int a, int b)
  {
    float t  = x[a];
    x[a]     = x[b];
    x[b]     = t;

    int tv   = v[a];
    v[a]     = v[b];
    v[b]     = tv;
  }

  /**
   * Swaps x[a] with x[b] and v[a] with v[b].
   */
  private static void swap(float x[], long v[], int a, int b)
  {
    float t  = x[a];
    x[a]     = x[b];
    x[b]     = t;

    long tv  = v[a];
    v[a]     = v[b];
    v[b]     = tv;
  }

  /**
   * Swaps x[a] with x[b] and v[a] with v[b].
   */
  private static void swap(float x[], float v[], int a, int b)
  {
    float t  = x[a];
    x[a]     = x[b];
    x[b]     = t;

    float tv = v[a];
    v[a]     = v[b];
    v[b]     = tv;
  }

  /**
   * Swaps x[a] with x[b] and v[a] with v[b].
   */
  private static void swap(float x[], double v[], int a, int b)
  {
    float t  = x[a];
    x[a]     = x[b];
    x[b]     = t;

    double tv = v[a];
    v[a]      = v[b];
    v[b]      = tv;
  }

  /**
   * Swaps x[a] with x[b] and v[a] with v[b].
   */
  private static <T> void swap(float x[], T v[], int a, int b)
  {
    float t  = x[a];
    x[a]     = x[b];
    x[b]     = t;

    T tv     = v[a];
    v[a]     = v[b];
    v[b]     = tv;
  }

  /**
   * Swaps x[a] with x[b] and v[a] with v[b].
   */
  private static void swap(double x[], int v[], int a, int b)
  {
    double t  = x[a];
    x[a]      = x[b];
    x[b]      = t;

    int tv    = v[a];
    v[a]      = v[b];
    v[b]      = tv;
  }

  /**
   * Swaps x[a] with x[b] and v[a] with v[b].
   */
  private static void swap(double x[], long v[], int a, int b)
  {
    double t  = x[a];
    x[a]      = x[b];
    x[b]      = t;

    long tv   = v[a];
    v[a]      = v[b];
    v[b]      = tv;
  }

  /**
   * Swaps x[a] with x[b] and v[a] with v[b].
   */
  private static void swap(double x[], float v[], int a, int b)
  {
    double t  = x[a];
    x[a]      = x[b];
    x[b]      = t;

    float tv  = v[a];
    v[a]      = v[b];
    v[b]      = tv;
  }

  /**
   * Swaps x[a] with x[b] and v[a] with v[b].
   */
  private static void swap(double x[], double v[], int a, int b)
  {
    double t  = x[a];
    x[a]      = x[b];
    x[b]      = t;

    double tv = v[a];
    v[a]      = v[b];
    v[b]      = tv;
  }

  /**
   * Swaps x[a] with x[b] and v[a] with v[b].
   */
  private static <T> void swap(double x[], T v[], int a, int b)
  {
    double t  = x[a];
    x[a]      = x[b];
    x[b]      = t;

    T tv      = v[a];
    v[a]      = v[b];
    v[b]      = tv;
  }

  /**
   * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)] and
   * v[a .. (a+n-1)] with v[b .. (b+n-1)].
   */
  private static void vecswap(int x[], int v[], int a, int b, int n)
  {
    for (int i = 0; i < n; i++, a++, b++) swap(x, v, a, b);
  }

  /**
   * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)], 
   * v1[a .. (a+n-1)] with v1[b .. (b+n-1)], and
   * v2[a .. (a+n-1)] with v2[b .. (b+n-1)].
   */
  private static void vecswap(int x[], int v1[], double v2[], int a, int b, int n)
  {
    for (int i = 0; i < n; i++, a++, b++) swap(x, v1, v2, a, b);
  }

  /**
   * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)] and
   * v[a .. (a+n-1)] with v[b .. (b+n-1)].
   */
  private static void vecswap(int x[], long v[], int a, int b, int n)
  {
    for (int i = 0; i < n; i++, a++, b++) swap(x, v, a, b);
  }

  /**
   * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)] and
   * v[a .. (a+n-1)] with v[b .. (b+n-1)].
   */
  private static void vecswap(int x[], float v[], int a, int b, int n)
  {
    for (int i = 0; i < n; i++, a++, b++) swap(x, v, a, b);
  }

  /**
   * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)] and
   * v[a .. (a+n-1)] with v[b .. (b+n-1)].
   */
  private static void vecswap(int x[], double v[], int a, int b, int n)
  {
    for (int i = 0; i < n; i++, a++, b++) swap(x, v, a, b);
  }

  /**
   * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)] and
   * v[a .. (a+n-1)] with v[b .. (b+n-1)].
   */
  private static <T> void vecswap(int x[], T v[], int a, int b, int n)
  {
    for (int i = 0; i < n; i++, a++, b++) swap(x, v, a, b);
  }

  /**
   * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)] and
   * v[a .. (a+n-1)] with v[b .. (b+n-1)].
   */
  private static void vecswap(long x[], int v[], int a, int b, int n)
  {
    for (int i = 0; i < n; i++, a++, b++) swap(x, v, a, b);
  }

  /**
   * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)] and
   * v[a .. (a+n-1)] with v[b .. (b+n-1)].
   */
  private static void vecswap(long x[], long v[], int a, int b, int n)
  {
    for (int i = 0; i < n; i++, a++, b++) swap(x, v, a, b);
  }

  /**
   * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)] and
   * v[a .. (a+n-1)] with v[b .. (b+n-1)].
   */
  private static void vecswap(long x[], float v[], int a, int b, int n)
  {
    for (int i = 0; i < n; i++, a++, b++) swap(x, v, a, b);
  }

  /**
   * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)] and
   * v[a .. (a+n-1)] with v[b .. (b+n-1)].
   */
  private static void vecswap(long x[], double v[], int a, int b, int n)
  {
    for (int i = 0; i < n; i++, a++, b++) swap(x, v, a, b);
  }

  /**
   * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)] and
   * v[a .. (a+n-1)] with v[b .. (b+n-1)].
   */
  private static <T> void vecswap(long x[], T v[], int a, int b, int n)
  {
    for (int i = 0; i < n; i++, a++, b++) swap(x, v, a, b);
  }

  /**
   * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)] and
   * v[a .. (a+n-1)] with v[b .. (b+n-1)].
   */
  private static void vecswap(float x[], int v[], int a, int b, int n)
  {
    for (int i = 0; i < n; i++, a++, b++) swap(x, v, a, b);
  }

  /**
   * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)] and
   * v[a .. (a+n-1)] with v[b .. (b+n-1)].
   */
  private static void vecswap(float x[], long v[], int a, int b, int n)
  {
    for (int i = 0; i < n; i++, a++, b++) swap(x, v, a, b);
  }

  /**
   * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)] and
   * v[a .. (a+n-1)] with v[b .. (b+n-1)].
   */
  private static void vecswap(float x[], float v[], int a, int b, int n)
  {
    for (int i = 0; i < n; i++, a++, b++) swap(x, v, a, b);
  }

  /**
   * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)] and
   * v[a .. (a+n-1)] with v[b .. (b+n-1)].
   */
  private static void vecswap(float x[], double v[], int a, int b, int n)
  {
    for (int i = 0; i < n; i++, a++, b++) swap(x, v, a, b);
  }

  /**
   * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)] and
   * v[a .. (a+n-1)] with v[b .. (b+n-1)].
   */
  private static <T> void vecswap(float x[], T v[], int a, int b, int n)
  {
    for (int i = 0; i < n; i++, a++, b++) swap(x, v, a, b);
  }

  /**
   * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)] and
   * v[a .. (a+n-1)] with v[b .. (b+n-1)].
   */
  private static void vecswap(double x[], int v[], int a, int b, int n)
  {
    for (int i = 0; i < n; i++, a++, b++) swap(x, v, a, b);
  }

  /**
   * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)] and
   * v[a .. (a+n-1)] with v[b .. (b+n-1)].
   */
  private static void vecswap(double x[], long v[], int a, int b, int n)
  {
    for (int i = 0; i < n; i++, a++, b++) swap(x, v, a, b);
  }

  /**
   * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)] and
   * v[a .. (a+n-1)] with v[b .. (b+n-1)].
   */
  private static void vecswap(double x[], float v[], int a, int b, int n)
  {
    for (int i = 0; i < n; i++, a++, b++) swap(x, v, a, b);
  }

  /**
   * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)] and
   * v[a .. (a+n-1)] with v[b .. (b+n-1)].
   */
  private static void vecswap(double x[], double v[], int a, int b, int n)
  {
    for (int i = 0; i < n; i++, a++, b++) swap(x, v, a, b);
  }

  /**
   * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)] and
   * v[a .. (a+n-1)] with v[b .. (b+n-1)].
   */
  private static <T> void vecswap(double x[], T v[], int a, int b, int n)
  {
    for (int i = 0; i < n; i++, a++, b++) swap(x, v, a, b);
  }

  /**
   * Returns the index of the median of the three indexed integers.
   */
  private static int med3(int x[], int a, int b, int c)
  {
    return (x[a] < x[b] ?
           (x[b] < x[c] ? b : x[a] < x[c] ? c : a) :
           (x[b] > x[c] ? b : x[a] > x[c] ? c : a));
  }

  /**
   * Returns the index of the median of the three indexed integers.
   */
  private static int med3(long x[], int a, int b, int c)
  {
    return (x[a] < x[b] ?
           (x[b] < x[c] ? b : x[a] < x[c] ? c : a) :
           (x[b] > x[c] ? b : x[a] > x[c] ? c : a));
  }

  /**
   * Returns the index of the median of the three indexed integers.
   */
  private static int med3(float x[], int a, int b, int c)
  {
    return (x[a] < x[b] ?
           (x[b] < x[c] ? b : x[a] < x[c] ? c : a) :
           (x[b] > x[c] ? b : x[a] > x[c] ? c : a));
  }

  /**
   * Returns the index of the median of the three indexed integers.
   */
  private static int med3(double x[], int a, int b, int c)
  {
    return (x[a] < x[b] ?
           (x[b] < x[c] ? b : x[a] < x[c] ? c : a) :
           (x[b] > x[c] ? b : x[a] > x[c] ? c : a));
  }
}
