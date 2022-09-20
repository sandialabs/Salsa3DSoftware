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
package gov.sandia.gmp.util.statistics;

import java.util.ArrayList;

import gov.sandia.gmp.util.containers.arraylist.ArrayListByte;
import gov.sandia.gmp.util.containers.arraylist.ArrayListDouble;
import gov.sandia.gmp.util.containers.arraylist.ArrayListFloat;
import gov.sandia.gmp.util.containers.arraylist.ArrayListInt;
import gov.sandia.gmp.util.containers.arraylist.ArrayListLong;
import gov.sandia.gmp.util.containers.arraylist.ArrayListShort;

public class DoubleGetTypes
{
  public static DoubleGetTypes dgt = new DoubleGetTypes();

  public DoubleGetTypes()
  {
    // default
  }

  public static DoubleGet newDoubleGet(ArrayList<Double> ain)
  {
    return dgt.new DoubleGetArrayListDoubleT(ain);
  }

  public static DoubleGet newDoubleGet(ArrayList<Double> ain, int beg, int end)
  {
    return dgt.new DoubleGetArrayListDoubleT(ain, beg, end);
  }

  public static DoubleGet newDoubleGet(ArrayListDouble ain)
  {
    return dgt.new DoubleGetArrayListDouble(ain);
  }

  public static DoubleGet newDoubleGet(ArrayListDouble ain, int beg, int end)
  {
    return dgt.new DoubleGetArrayListDouble(ain, beg, end);
  }

  public static DoubleGet newDoubleGet(ArrayListFloat ain)
  {
    return dgt.new DoubleGetArrayListFloat(ain);
  }

  public static DoubleGet newDoubleGet(ArrayListFloat ain, int beg, int end)
  {
    return dgt.new DoubleGetArrayListFloat(ain, beg, end);
  }

  public static DoubleGet newDoubleGet(ArrayListLong ain)
  {
    return dgt.new DoubleGetArrayListLong(ain);
  }

  public static DoubleGet newDoubleGet(ArrayListLong ain, int beg, int end)
  {
    return dgt.new DoubleGetArrayListLong(ain, beg, end);
  }

  public static DoubleGet newDoubleGet(ArrayListInt ain)
  {
    return dgt.new DoubleGetArrayListInt(ain);
  }

  public static DoubleGet newDoubleGet(ArrayListInt ain, int beg, int end)
  {
    return dgt.new DoubleGetArrayListInt(ain, beg, end);
  }

  public static DoubleGet newDoubleGet(ArrayListShort ain)
  {
    return dgt.new DoubleGetArrayListShort(ain);
  }

  public static DoubleGet newDoubleGet(ArrayListShort ain, int beg, int end)
  {
    return dgt.new DoubleGetArrayListShort(ain, beg, end);
  }

  public static DoubleGet newDoubleGet(ArrayListByte ain)
  {
    return dgt.new DoubleGetArrayListByte(ain);
  }

  public static DoubleGet newDoubleGet(ArrayListByte ain, int beg, int end)
  {
    return dgt.new DoubleGetArrayListByte(ain, beg, end);
  }

  public static DoubleGet newDoubleGet(double[] ain)
  {
    return dgt.new DoubleGetArrayDouble(ain);
  }

  public static DoubleGet newDoubleGet(double[] ain, int beg, int end)
  {
    return dgt.new DoubleGetArrayDouble(ain, beg, end);
  }

  public static DoubleGet newDoubleGet(float[] ain)
  {
    return dgt.new DoubleGetArrayFloat(ain);
  }

  public static DoubleGet newDoubleGet(float[] ain, int beg, int end)
  {
    return dgt.new DoubleGetArrayFloat(ain, beg, end);
  }

  public static DoubleGet newDoubleGet(long[] ain)
  {
    return dgt.new DoubleGetArrayLong(ain);
  }

  public static DoubleGet newDoubleGet(long[] ain, int beg, int end)
  {
    return dgt.new DoubleGetArrayLong(ain, beg, end);
  }

  public static DoubleGet newDoubleGet(int[] ain)
  {
    return dgt.new DoubleGetArrayInt(ain);
  }

  public static DoubleGet newDoubleGet(int[] ain, int beg, int end)
  {
    return dgt.new DoubleGetArrayInt(ain, beg, end);
  }

  public static DoubleGet newDoubleGet(short[] ain)
  {
    return dgt.new DoubleGetArrayShort(ain);
  }

  public static DoubleGet newDoubleGet(short[] ain, int beg, int end)
  {
    return dgt.new DoubleGetArrayShort(ain, beg, end);
  }

  public static DoubleGet newDoubleGet(byte[] ain)
  {
    return dgt.new DoubleGetArrayByte(ain);
  }

  public static DoubleGet newDoubleGet(byte[] ain, int beg, int end)
  {
    return dgt.new DoubleGetArrayByte(ain, beg, end);
  }

  
  
  
  public abstract class DoubleGetArray implements DoubleGet
  {
    protected int begIndex;
    protected int endIndex;

    protected DoubleGetArray(int beg, int end)
    {
      begIndex = beg;
      endIndex = end;
    }

    @Override
    public int size()
    {
      return endIndex - begIndex + 1;
    }    
  }

  public class DoubleGetArrayListDoubleT extends DoubleGetArray
  {
    private ArrayList<Double> aLst = null;

    public DoubleGetArrayListDoubleT(ArrayList<Double> ain)
    {
      super(0, ain.size()-1);
      aLst = ain;
    }

    public DoubleGetArrayListDoubleT(ArrayList<Double> ain, int beg, int end)
    {
      super(beg, end);
      aLst = ain;
    }

    @Override
    public double get(int i)
    {
      return aLst.get(i+begIndex);
    }
  }

  public class DoubleGetArrayListDouble extends DoubleGetArray
  {
    private ArrayListDouble aLst = null;

    public DoubleGetArrayListDouble(ArrayListDouble ain)
    {
      super(0, ain.size()-1);
      aLst = ain;
    }

    public DoubleGetArrayListDouble(ArrayListDouble ain, int beg, int end)
    {
      super(beg, end);
      aLst = ain;
    }

    @Override
    public double get(int i)
    {
      return aLst.get(i+begIndex);
    }
  }
  
  public class DoubleGetArrayListFloat extends DoubleGetArray
  {
    private ArrayListFloat aLst = null;

    public DoubleGetArrayListFloat(ArrayListFloat ain)
    {
      super(0, ain.size()-1);
      aLst = ain;
    }

    public DoubleGetArrayListFloat(ArrayListFloat ain, int beg, int end)
    {
      super(beg, end);
      aLst = ain;
    }

    @Override
    public double get(int i)
    {
      return aLst.get(i+begIndex);
    }
  }
  
  public class DoubleGetArrayListLong extends DoubleGetArray
  {
    private ArrayListLong aLst = null;

    public DoubleGetArrayListLong(ArrayListLong ain)
    {
      super(0, ain.size()-1);
      aLst = ain;
    }

    public DoubleGetArrayListLong(ArrayListLong ain, int beg, int end)
    {
      super(beg, end);
      aLst = ain;
    }

    @Override
    public double get(int i)
    {
      return aLst.get(i+begIndex);
    }
  }
  
  public class DoubleGetArrayListInt extends DoubleGetArray
  {
    private ArrayListInt aLst = null;

    public DoubleGetArrayListInt(ArrayListInt ain)
    {
      super(0, ain.size()-1);
      aLst = ain;
    }

    public DoubleGetArrayListInt(ArrayListInt ain, int beg, int end)
    {
      super(beg, end);
      aLst = ain;
    }

    @Override
    public double get(int i)
    {
      return aLst.get(i+begIndex);
    }
  }
  
  public class DoubleGetArrayListShort extends DoubleGetArray
  {
    private ArrayListShort aLst = null;

    public DoubleGetArrayListShort(ArrayListShort ain)
    {
      super(0, ain.size()-1);
      aLst = ain;
    }

    public DoubleGetArrayListShort(ArrayListShort ain, int beg, int end)
    {
      super(beg, end);
      aLst = ain;
    }

    @Override
    public double get(int i)
    {
      return aLst.get(i+begIndex);
    }
  }
  
  public class DoubleGetArrayListByte extends DoubleGetArray
  {
    private ArrayListByte aLst = null;

    public DoubleGetArrayListByte(ArrayListByte ain)
    {
      super(0, ain.size()-1);
      aLst = ain;
    }

    public DoubleGetArrayListByte(ArrayListByte ain, int beg, int end)
    {
      super(beg, end);
      aLst = ain;
    }

    @Override
    public double get(int i)
    {
      return aLst.get(i+begIndex);
    }
  }

  public class DoubleGetArrayDouble extends DoubleGetArray
  {
    private double[] aLst = null;

    public DoubleGetArrayDouble(double[] ain)
    {
      super(0, ain.length-1);
      aLst = ain;
    }

    public DoubleGetArrayDouble(double[] ain, int beg, int end)
    {
      super(beg, end);
      aLst = ain;
    }

    @Override
    public double get(int i)
    {
      return aLst[i+begIndex];
    }
  }

  public class DoubleGetArrayFloat extends DoubleGetArray
  {
    private float[] aLst = null;

    public DoubleGetArrayFloat(float[] ain)
    {
      super(0, ain.length-1);
      aLst = ain;
    }

    public DoubleGetArrayFloat(float[] ain, int beg, int end)
    {
      super(beg, end);
      aLst = ain;
    }

    @Override
    public double get(int i)
    {
      return aLst[i+begIndex];
    }
  }

  public class DoubleGetArrayLong extends DoubleGetArray
  {
    private long[] aLst = null;

    public DoubleGetArrayLong(long[] ain)
    {
      super(0, ain.length-1);
      aLst = ain;
    }

    public DoubleGetArrayLong(long[] ain, int beg, int end)
    {
      super(beg, end);
      aLst = ain;
    }

    @Override
    public double get(int i)
    {
      return aLst[i+begIndex];
    }
  }

  public class DoubleGetArrayInt extends DoubleGetArray
  {
    private int[] aLst = null;

    public DoubleGetArrayInt(int[] ain)
    {
      super(0, ain.length-1);
      aLst = ain;
    }

    public DoubleGetArrayInt(int[] ain, int beg, int end)
    {
      super(beg, end);
      aLst = ain;
    }

    @Override
    public double get(int i)
    {
      return aLst[i+begIndex];
    }
  }

  public class DoubleGetArrayShort extends DoubleGetArray
  {
    private short[] aLst = null;

    public DoubleGetArrayShort(short[] ain)
    {
      super(0, ain.length-1);
      aLst = ain;
    }

    public DoubleGetArrayShort(short[] ain, int beg, int end)
    {
      super(beg, end);
      aLst = ain;
    }

    @Override
    public double get(int i)
    {
      return aLst[i+begIndex];
    }
  }

  public class DoubleGetArrayByte extends DoubleGetArray
  {
    private byte[] aLst = null;

    public DoubleGetArrayByte(byte[] ain)
    {
      super(0, ain.length-1);
      aLst = ain;
    }

    public DoubleGetArrayByte(byte[] ain, int beg, int end)
    {
      super(beg, end);
      aLst = ain;
    }

    @Override
    public double get(int i)
    {
      return aLst[i+begIndex];
    }
  }
}
