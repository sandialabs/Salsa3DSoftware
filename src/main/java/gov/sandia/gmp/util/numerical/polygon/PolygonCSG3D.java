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
package gov.sandia.gmp.util.numerical.polygon;

import static gov.sandia.gmp.util.globals.Globals.NL;

import java.util.HashMap;

import gov.sandia.gmp.util.filebuffer.FileInputBuffer;
import gov.sandia.gmp.util.filebuffer.FileOutputBuffer;
import gov.sandia.gmp.util.globals.Globals;

/**
 * Defines 3D lateral and vertical point PolygonCSG3D objects within the Earth.
 * The basic PolygonCSG3D can be defined as a single PolygonCSG and a
 * DepthLayerInterval, or as the union or intersection of two other different
 * PolygonCSG3D definitions. Each PolygonCSG3D object has a name and either a
 * PolygonCSG/DepthLayerInterval, or a pair of other PolygonCSG3D objects, with
 * "not" flags, and an operator defining "UNION" or "INTERSECT".
 * 
 * 3D Combinatorial polygon (PolygonCSG3D) regions can be built from other
 * 3D combinatorial Polygons allowing for unlimited and sophisticated volumes
 * 
 * @author jrhipp
 *
 */
public class PolygonCSG3D
{
	/**
	 * The static global PolygonCSG3D object. All points are contained (true) with
	 * this object definition.
	 */
	private static  PolygonCSG3D glblRegion = new PolygonCSG3D();

	/**
	 * The PolygonCSG3D name.
	 */
	private String       rName = "";

	/**
	 * The PolygonCSG defining the PolygonCSG3D lateral extent if it is a
	 * base definition. For combinatorial definitions this value is null.
	 */
	private PolygonCSG surfPolygnCSG = null;

	/**
	 * The DepthLayerInterval defining the PolygonCSG3D vertical extent if it is a
	 * base definition. For combinatorial definitions this value is null.
	 */
	private DepthLayerInterval dpthLayrIntrvl = null;

	/**
	 * The first PolygonCSG3D in a combinatorial definition (A op B).
	 */
	private PolygonCSG3D  A   = null;

	/**
	 * The not operator for PolygonCSG3D A or a PolygonCSG and dpthLayrRegion.
	 */
	private boolean     notA   = false;

	/**
	 * The second PolygonCSG3D in a combinatorial definition (A op B).
	 */
	private PolygonCSG3D  B   = null;

	/**
	 * The not operator for PolygonCSG3D B.
	 */
	private boolean     notB   = false;

	/**
	 * The operator for in a combinatorial definition (A op B) where the op
	 * can be "UNION" or "INTERSECT".
	 */
	private CSGOperation         opAB   = CSGOperation.UNION;

	/**
	 * Used to describe the construction of the basic string for output.
	 */
	private String   aToString = "";

	/**
	 * Returns the static global PolygonCSG3D.
	 * 
	 * @return The static global PolygonCSG3D.
	 */
	public static PolygonCSG3D getGlobalRegion()
	{
		return glblRegion;
	}

	/**
	 * Default constructor. Makes a "GLOBAL" PolygonCSG3D.
	 */
	public PolygonCSG3D()
	{
		rName = "GLOBAL";
		surfPolygnCSG = new PolygonCSG();
		dpthLayrIntrvl = new DepthLayerInterval();
		aToString  = rName;
	}

	/**
	 * Standard constructor that reads this PolygonCSG3D from an input file
	 * buffer.
	 * 
	 * @param fib The input file buffer from which the PolygonCSG3D is read.
	 * @throws Exception
	 */
	public PolygonCSG3D(FileInputBuffer fib)
			   throws Exception
	{
		read(fib);
	}

	/**
	 * Standard constructor for a combinatorial definition given the PolygonCSG3D
	 * name, and two PolygonCSG3Ds and their respective "not" flags that are
	 * combinatorially combined using a "UNION" or "INTERSECT" operator.
	 * 
	 * @param name The new PolygonCSG3D name.
	 * @param na   The not flag. Inverts the result of A if true
	 * @param a    PolygonCSG3D A
	 * @param nb   The not flag. Inverts the result of B if true
	 * @param op   Combinatorial operator. Containment returns A op B.
	 */
	public PolygonCSG3D(String name, boolean na, PolygonCSG3D a,
			                boolean nb, PolygonCSG3D b, CSGOperation op)
	{
		// set the parameters

		rName = name;
		A     = a;
		B     = b;
		notA  = na;
		notB  = nb;
		opAB  = op;

		// build the string description

		aToString  = rName + " (";
		aToString += notA ? "!" + A.getName() : A.getName();
		if (opAB == CSGOperation.UNION)
			aToString  += " UNION ";
		else // (opAB == CSGOperation.INTERSECT)
			aToString  += " INTERSECT ";
		aToString  += notB ? "!" + B.getName() : B.getName();
		aToString  += ")";
	}

	/**
	 * Standard constructor for a combinatorial definition given the PolygonCSG3D
	 * name, and two PolygonCSG3D that are combinatorially combined using a
	 * "UNION" or "INTERSECT" operator.
	 * 
	 * @param name The new PolygonCSG3D name.
	 * @param a    PolygonCSG3D A
	 * @param b    PolygonCSG3D B
	 * @param op   Combinatorial operator. Containment returns A op B.
	 */
	public PolygonCSG3D(String name, PolygonCSG3D a, PolygonCSG3D b,
			                CSGOperation op)
	{
		// set the parameters

		rName = name;
		A     = a;
		B     = b;
		opAB  = op;

		// build the string description

		aToString  = rName + " (";
		aToString += notA ? "!" + A.getName() : A.getName();
		if (opAB == CSGOperation.UNION)
			aToString  += " UNION ";
		else // (opAB == CSGOperation.INTERSECT)
			aToString  += " INTERSECT ";
		aToString  += notB ? "!" + B.getName() : B.getName();
		aToString  += ")";
	}

	/**
	 * Creates a new basic PolygonCSG3D given the input PolygonCSG and
	 * DepthLayerInterval.
	 *  
	 * @param name     The PolygonCSG3D name.
	 * @param surfregn The laterally defining PolygonCSG.
	 * @param dli      The depth defining DepthLayerInterval.
	 */
	public PolygonCSG3D(String name, PolygonCSG surfregn, DepthLayerInterval dli)
	{
		rName = name;
		surfPolygnCSG = surfregn;
		dpthLayrIntrvl = dli;
		aToString = rName + " (SurfaceRegion: " + surfPolygnCSG.getName() +
				        ", Depth/Layers: " + dpthLayrIntrvl.toString() + ")";
	}

	/**
	 * Creates a new basic PolygonCSG3D given the input PolygonCSG and
	 * DepthLayerInterval.
	 *  
	 * @param name     The PolygonCSG3D name.
	 * @param na       The new PolygonCSG3D "not" flag.
	 * @param surfregn The laterally defining PolygonCSG.
	 * @param dlregn      The depth defining DepthLayerInterval.
	 */
	public PolygonCSG3D(String name, boolean na, PolygonCSG surfregn,
			                DepthLayerInterval dlregn)
	{
		rName = name;
		surfPolygnCSG = surfregn;
		dpthLayrIntrvl = dlregn;
		notA  = na;
		aToString = rName + " (SurfaceRegion: " + surfPolygnCSG.getName() +
				        ", Depth/Layers: " + dpthLayrIntrvl.toString() + ")";
	}

	/**
	 * Standard constructor. Assembles a new base definition PolygonCSG3D from
	 * the input PolygonCSG map and a property data string. The format is defined as
	 *     
	 *     format = name SurfaceRegion.name [Depths: depth0 depth1] [Layers: id0 id1 ...]
	 *   
	 * @param data The input data string described above
	 * @throws Exception
	 */
	public PolygonCSG3D(HashMap<String, PolygonCSG> surfaceRegns, String data)
			   throws Exception
	{
		String[] tokens = Globals.getTokens(data, "\t, ");
		if ((tokens == null) || (tokens.length == 0))
		{
			// make a global region

			rName = "GLOBAL";
			surfPolygnCSG = new PolygonCSG();
			dpthLayrIntrvl = new DepthLayerInterval();
			aToString  = rName;
		}
		else if (tokens.length > 2)
		{
			// regionContainment = name surfaceRegionName [Depths: d0 d1] [Layers: l0 l1 ...]
      // set name and surface region. Throw error if surface region is not contained in
			// the input map

			rName = tokens[0];
			surfPolygnCSG = surfaceRegns.get(tokens[1]);
			if (surfPolygnCSG == null)
			{
				throw new Exception("Error: Unknown SurfaceRegion Name: \"" +
			                        tokens[1] + "\" ...");
			}

			// add "Depths:" definition if defined

			String sinfo = "";
			dpthLayrIntrvl = new DepthLayerInterval();
			for (int i = 2; i < tokens.length; ++i)
			{
				if (tokens[i].toUpperCase().equals("DEPTHS:"))
				{
					// throw error if d0 and d1 limits are not defined

					if (tokens.length < i + 3)
					{
						throw new Exception("Error: \"Depths:\" requires two limits " +
					                        "(d0 and d1) ... Definition = \"" +
								                  data + "\" ...");
					}
					
					// add depth definition

					dpthLayrIntrvl.setDepthRange(Double.valueOf(tokens[i+1]),
							                         Double.valueOf(tokens[i+2]));
					sinfo = "Depths: " + tokens[i+1] + " " + tokens[i+2] + " "; 
					break;
				}
			}

			// add "Layers:" definition if defined

			for (int i = 2; i < tokens.length; ++i)
			{
				if (tokens[i].toUpperCase().equals("LAYERS:"))
				{
					// throw error if there is not at least one layer

					if (tokens.length < i + 2)
					{
						throw new Exception("Error: \"Layers:\" requires at least one " +
					                        "or more entries ... Definition = \"" +
								                  data + "\" ...");
					}

					// loop over all layer entries and add to DepthLayerRegion

					sinfo = "Layers: "; 
					for (int j = i+1; j < tokens.length; ++j)
					{
						// stop if next token is "Depths:" ... otherwise add layer id

						if (tokens[j].toUpperCase().equals("DEPTHS:")) break;
						dpthLayrIntrvl.addLayer(Integer.valueOf(tokens[j]));
						sinfo += tokens[j] + " ";
					}
					break;
				}
			}

			// set to string and exit

			aToString  = rName + " (SurfaceRegion: " + surfPolygnCSG.getName() +
					         sinfo + ")";
		}
	}

	/**
	 * Tests the input point for containment. Returns true if contained and
	 * "not" is false.
	 * 
	 * @param pnt The point to be tested for containment.
	 * @return True if the PolygonCSG3D containment operation is true.
	 * @throws Exception
	 */
	public boolean contains(double[] pnt, double depth) throws Exception
	{
		if (surfPolygnCSG == null)
		{
			// Combinatorial Region ... Get A and B containments and return their
			// combinatorial result with the operator opAB

			boolean cntA = notA ? !A.contains(pnt, depth) : A.contains(pnt, depth);
			boolean cntB = notB ? !B.contains(pnt, depth) : B.contains(pnt, depth);
			return (opAB == CSGOperation.UNION) ? cntA || cntB : cntA && cntB;
		}
		else
		{
			// Basic SurfaceRegion ... Return the polygons containment result.

			boolean cnt = dpthLayrIntrvl.isPointContained(depth) && surfPolygnCSG.contains(pnt);
			return notA ? !cnt : cnt;
		}
	}

	/**
	 * Tests the input point for containment. Returns true if contained and
	 * "not" is false.
	 * 
	 * @param pnt The point to be tested for containment.
	 * @return True if the PolygonCSG3D containment operation is true.
	 * @throws Exception
	 */
	public boolean contains(double[] pnt, int layer) throws Exception
	{
		if (surfPolygnCSG == null)
		{
			// Combinatorial Region ... Get A and B containments and return their
			// combinatorial result with the operator opAB

			boolean cntA = notA ? !A.contains(pnt, layer) : A.contains(pnt, layer);
			boolean cntB = notB ? !B.contains(pnt, layer) : B.contains(pnt, layer);
			return (opAB == CSGOperation.UNION) ? cntA || cntB : cntA && cntB;
		}
		else
		{
			// Basic SurfaceRegion ... Return the polygons containment result.

			boolean cnt = dpthLayrIntrvl.isPointContained(layer) && surfPolygnCSG.contains(pnt);
			return notA ? !cnt : cnt;
		}
	}

	/**
	 * Reads this PolygonCSG3D object from the input file buffer.
	 * 
	 * @param fib     The input file buffer from which the PolygonCSG3D
	 *                description is read.
	 * @throws Exception
	 */
	public void read(FileInputBuffer fib) throws Exception
	{
		// read name and string descriptors

		rName = fib.readString();
		aToString = fib.readString();

		// read type (base or combinatorial) and check type

		int typ = fib.readInt();
		if (typ == 0)
		{
			// read base definition ... not flag, PolygonCSG, and depth/layer interval
			
			notA  = fib.readBoolean();
			surfPolygnCSG = new PolygonCSG(fib);
			dpthLayrIntrvl = new DepthLayerInterval(fib);
		}
		else
		{
			// read combinatorial definition ... first PolygonCSG3D A

			notA = fib.readBoolean();
			A    = new PolygonCSG3D(fib);
			
			// now PolygonCSG3D B

			notB = fib.readBoolean();
			B    = new PolygonCSG3D(fib);
			
			// and finally operation (UNION or INTERSECT)

			opAB = CSGOperation.values()[fib.readInt()];
		}
	}

	/**
	 * Writes this PolygonCSG3D object to the output file buffer.
	 * 
	 * @param fob The output file buffer into which this PolygonCSG3D is written.
	 * @throws Exception
	 */
	public void write(FileOutputBuffer fob) throws Exception
	{
		// write PolygonCSG3D name and string descriptors

		fob.writeString(rName);
		fob.writeString(aToString);
		if (isBaseDefinition())
		{
			// write base definition (0)

			fob.writeInt(0);

			// write PolygonCSG and depth/layer region

			fob.writeBoolean(notA);
			surfPolygnCSG.write(fob);
			dpthLayrIntrvl.write(fob);
		}
		else
		{
			// write combinatorial identifier (1)

			fob.writeInt(1);
			
			// write PolygonCSG3D A and not flag, PolygonCSG3D B and not flag, and
			// finally the combinatorial operation

			fob.writeBoolean(notA);
			A.write(fob);
			fob.writeBoolean(notB);
			B.write(fob);
			fob.writeInt(opAB.ordinal());
		}
	}

	/**
	 * Returns the PolygonCSG3D operation as a string.
	 */
	@Override
	public String toString()
	{
		return toString("");
	}

	/**
	 * Returns the PolygonCSG3D combinatorial operation as a string. The input hdr
	 * is pre-pended to the line.
	 * 
	 * @param hdr The header pre-pended to the output string.
	 * @return The PolygonCSG3D combinatorial operation as a string.
	 */
	public String toString(String hdr)
	{
		return hdr + aToString + NL;
	}

	/**
	 * Returns true if this PolygonCSG3D is a base definition
	 * (i.e. surfPolygnCSG != null).
	 * 
	 * @return True if this PolygonCSG3D is a base definition
	 * (i.e. surfPolygnCSG != null).
	 */
	public boolean isBaseDefinition()
	{
		return (surfPolygnCSG != null) ? true : false;
	}

	/**
	 * Returns PolygonCSG3D A of a combinatorial region definition. If this is a
	 * base PolygonCSG3D null is returned.
	 * 
	 * @return PolygonCSG3D A of a combinatorial region definition.
	 */
	public PolygonCSG3D getRegionA()
	{
		return A;
	}

	/**
	 * Returns PolygonCSG3D B of a combinatorial region definition. If this is a
	 * base PolygonCSG3D null is returned.
	 * 
	 * @return PolygonCSG3D B of a combinatorial region definition.
	 */
	public PolygonCSG3D getRegionB()
	{
		return B;
	}

	/**
	 * Returns the PolygonCSG3D name.
	 * 
	 * @return The PolygonCSG3D name.
	 */
	public String getName()
	{
		return rName;
	}
}
