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
import java.util.HashSet;

import gov.sandia.gmp.util.filebuffer.FileInputBuffer;
import gov.sandia.gmp.util.filebuffer.FileOutputBuffer;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.numerical.vector.VectorGeo;

/**
 * Defines 2D lateral spatial regions on the surface of the Earth that are
 * assembled from base Polygon2D objects using Constructive Solid Geometry (CSG)
 * techniques. At the base level a PolygonCSG is simply a Polygon2D. However,
 * Unlike Polygons, PolygonCSG objects can use set theory UNION and INTERSECT
 * operators (logical "OR" and "AND") to combine to existing PolygonCSG objects
 * into a new one. These can be combined hierarchically to form ever more
 * complex regions. These are very useful when mapping various properties to
 * specific Earth regions (e.g. Density or Poisson Ratio parametric functions
 * defined differently for different regions of the Earth).
 * 
 * Each PolygonCSG object has a name and either a polygon and a "not" flag, or
 * a pair of other regions, with "not" flags, and an operator defining a CSG
 * UNION or INTERSECT operation. The former is simply a wrapper around a Polygon2D
 * object that can be used to make more complex PolygonCSG objects. The
 * PolygonCSG object has a constructor that takes a single String argument that
 * contains a property definition for constructing the basic (Polygon2D like)
 * PolygonCSG. Three different forms are recognized.
 * 
 *   Empty or NULL:
 *     Produces a "GLOBAL" polygon that is true for all points on the Earth.
 *   
 *   Spherical Cap:
 *     Produces a base definition spherical cap given the center (lat, lon)
 *     in degrees and the radius in degrees.
 *     
 *     format = name lat lon radius npts
 *     
 *   Polygon2D:
 *     Produces a base definition arbitrary polygon given 3 or more lat/lon
 *     point pairs in degrees.
 *     
 *     format = name lat0 lon0 lat1 lon1 lat2 lon2 ...
 *
 * Constructors that form Higher level PolygonCSG objects include
 * 
 *     CSGPolygon(String name, PolygonCSG a, PolygonCSG b, int op);
 *     CSGPolygon(String name, boolean na, PolygonCSG a,
 *                             boolean nb, PolygonCSG b, int op);
 *
 * A static factory method getPolygonCSG(String defn, HashMap<String, PolygonCSG>)
 * is also defined that can return a basic or combinatorial region given an
 * input map of existing PolgyonCSG objects from which other PolygonCSG objects
 * can be constructed. The form of the CSG definition is
 * 
 *   CSG Polygon2D:
 *     produces a high level CSG polygon from the string definition.
 *     
 *     format = name PolygonCSG0_name operation PolygonCSG1_name
 * 
 * The definition string (defn) can be of the above form or either of the
 * base definition forms given above.
 * 
 * @author jrhipp
 *
 */
public class PolygonCSG
{
	/**
	 * Static global PolygonCSG that returns true for all points regardless of
	 * lateral position.
	 */
	private static  PolygonCSG    glblRegion = new PolygonCSG();

	/**
	 * The PolygonCSG name (e.g. "NTS").
	 */
	private String                     rName = "";

	/**
	 * The polygon defining this region if it is a base definition. For
	 * combinatorial definitions this value is null.
	 */
	private Polygon2D                    pgA   = null;

	/**
	 * The first Region in a combinatorial definition (A op B).
	 */
	private PolygonCSG                   A   = null;

	/**
	 * The not operator for PolgyonCSG A or Polygon2D pgA.
	 */
	private boolean                   notA   = false;

	/**
	 * The second region in a combinatorial definition (A op B).
	 */
	private PolygonCSG                   B   = null;

	/**
	 * The not operator for PolygonCSG B.
	 */
	private boolean                   notB   = false;

	/**
	 * The operator for in a combinatorial definition (A op B) where the op
	 * can be 0 "UNION" or "INTERSECT".
	 */
	private CSGOperation              opAB   = CSGOperation.UNION;

	/**
	 * Used to describe the construction of the basic string for output.
	 */
	private String[] aToString = null;

	/**
	 * Returns the static global region.
	 * 
	 * @return The static global region.
	 */
	public static PolygonCSG getGlobalRegion()
	{
		return glblRegion;
	}

	/**
	 * Default constructor. Builds a static "GLOBAL" PolgyonCSG.
	 */
	public PolygonCSG()
	{
		pgA   = new PolygonGlobal(true);
		rName = "GLOBAL";
		aToString = new String [1];
		aToString[0] = rName;		
	}

	/**
	 * Standard constructor. Assembles a new base definition PolygonCSG from
	 * the input data string. Valid entries include:
	 * 
	 *   Empty or NULL:
	 *     Produces a "GLOBAL" polygon that is true for all points on the Earth.
	 *   
	 *   Spherical Cap:
	 *     Produces a base definition spherical cap given the center (lat, lon)
	 *     in degrees and the radius in degrees.
	 *     
	 *     format = name lat lon radius
	 *     
	 *   Polygon2D:
	 *     Produces a base definition arbitrary polygon given 3 or more lat/lon
	 *     point pairs in degrees.
	 *     
	 *     format = name lat0 lon0 lat1 lon1 lat2 lon2 ...
	 *   
	 * @param data The input data string described above
	 * @throws Exception
	 */
	public PolygonCSG(String data) throws Exception
	{
		String s;
	
		// get the region definition parameters and see if there are 4 (a spherical
		// cap or a combinatorial region

		String[] tokens = Globals.getTokens(data, "\t, ");
		if ((tokens == null) || (tokens.length == 0))
		{
			pgA   = new PolygonGlobal(true);
			rName = "GLOBAL";
			aToString = new String [1];
			aToString[0] = rName;
		}
		else if (tokens.length == 4)
		{
			// else make a spherical cap region ... get the position and radius of
			// the cap

			double[] scc = VectorGeo.getVectorDegrees(Double.valueOf(tokens[1]),
					                                      Double.valueOf(tokens[2]));
			double   scr = Math.toRadians(Double.valueOf(tokens[3]));
			
			// create the containment polygon and the region

			pgA   = new PolygonSmallCircles(scc, true, scr);
			rName = tokens[0];
			aToString = new String [1];
			aToString[0] = rName + " (Spherical Cap: Center (deg) = " + tokens[1] +
	                   ", " + tokens[2] + "; Radius (deg) = " + tokens[3] + ")";
		}
		else if (tokens.length >= 7)
		{
			// possibly a polygonal region ... make sure the proper number of lon /
			// lat pairs are defined

			if ((tokens.length-1) % 2 != 0)
			{
				s = "Error: PolygonCSG definition must have 3 or more point pairs" +
			      " (Lat, Lon in degrees) ... found: " +
						((double) (tokens.length-1)/2) + " (\"" + data + "\")";
				throw new Exception(s);
			}

			// now read all point pairs into an array of point unit vectors 

			int n = (tokens.length-1) / 2;
			double[][] pts = new double [n][];
			rName = tokens[0];
			aToString = new String [n];
			aToString[0] = rName + " (Polygon2D: Pts (deg) = ";
			int nc = aToString[0].length();
			for (int i = 0, j = 1; i < n; ++i, j+=2)
			{
				pts[i] = VectorGeo.getVectorDegrees(Double.valueOf(tokens[j]),
                                            Double.valueOf(tokens[j+1]));
				if (i > 0) aToString[i] = Globals.repeat(" ",  nc);
				aToString[i] += tokens[j] + ", " + tokens[j+1];
			}
			aToString[n-1] += ")";

			// create the containment polygon and the region

			pgA   = new PolygonPoints(pts);
		}
		else
		{
			// unknown region definition ... throw an error

			throw new Exception("Error: Unknown PolygonCSG Definition: \"" +
			                      data + "\"");
		}
	}

	/**
	 * Standard constructor for a combinatorial definition given the PolygonCSG
	 * name, and two PolygonCSGs that are combined using a "UNION" or "INTERSECT"
	 * operator.
	 * 
	 * @param name The new PolygonCSG name.
	 * @param a    PolygonCSG A
	 * @param b    PolygonCSG B
	 * @param op   Combinatorial operator. Containment returns A op B.
	 */
	public PolygonCSG(String name, PolygonCSG a, PolygonCSG b, CSGOperation op)
	{
		// set the parameters

		rName = name;
		A     = a;
		B     = b;
		opAB  = op;

		// build the string description

		aToString = new String [1];
		aToString[0]  = rName + " (";
		aToString[0] += notA ? "!" + A.getName() : A.getName();
		if (opAB == CSGOperation.UNION)
			aToString[0]  += " UNION ";
		else // (opAB == CSGOperation.INTERSECT)
			aToString[0]  += " INTERSECT ";
		aToString[0]  += notB ? "!" + B.getName() : B.getName();
		aToString[0]  += ")";
	}

	/**
	 * Standard constructor for a combinatorial definition given the PolgonCSG
	 * name, and two PolygonCSGs and their respective "not" flags that are
	 * combined using a "UNION" or "INTERSECT" operator.
	 * 
	 * @param name The new PolygonCSG name.
	 * @param na   The not flag. Inverts the result of A if true
	 * @param a    PolygonCSG A
	 * @param nb   The not flag. Inverts the result of B if true
	 * @param op   Combinatorial operator. Containment returns A op B.
	 */
	public PolygonCSG(String name, boolean na, PolygonCSG a,
			                 boolean nb, PolygonCSG b, CSGOperation op)
	{
		// set the parameters

		rName = name;
		A     = a;
		B     = b;
		notA  = na;
		notB  = nb;
		opAB  = op;

		// build the string description

		aToString = new String [1];
		aToString[0]  = rName + " (";
		aToString[0] += notA ? "!" + A.getName() : A.getName();
		if (opAB == CSGOperation.UNION)
			aToString[0]  += " UNION ";
		else // (opAB == CSGOperation.INTERSECT)
			aToString[0]  += " INTERSECT ";
		aToString[0]  += notB ? "!" + B.getName() : B.getName();
		aToString[0]  += ")";
	}

	/**
	 * Standard constructor that reads this PolygonCSG from an input file
	 * buffer.
	 * 
	 * @param fib The input file buffer from which the PolygonCSG is read.
	 * @throws Exception
	 */
	public PolygonCSG(FileInputBuffer fib)
			   throws Exception
	{
		read(fib);
	}

	/**
	 * Tests the input point for containment. Returns true if contained and
	 * "not" is false.
	 * 
	 * @param pnt The point to be tested for containment.
	 * @return True if the PolygonCSG containment operation is true.
	 * @throws Exception
	 */
	public boolean contains(double[] pnt) throws Exception
	{
		if (pgA == null)
		{
			// Combinatorial Region ... Get A and B containments and return their
			// combinatorial result with the operator opAB

			boolean cntA = notA ? !A.contains(pnt) : A.contains(pnt);
			boolean cntB = notB ? !B.contains(pnt) : B.contains(pnt);
			return (opAB == CSGOperation.UNION) ? cntA || cntB : cntA && cntB;
		}
		else
			// Basic PolgongCSG ... Return the polygons containment result.

			return notA ? !polygonContains(pnt) : polygonContains(pnt);
	}

	/**
	 * Private polygon containment function to remove the defined exception and
	 * replace it with an Exception.
	 * 
	 * @param pnt The point to be tested for containment.
	 * @return True if the point is contained by the polygon (pgA).
	 * @throws Exception
	 */
	private boolean polygonContains(double[] pnt) throws Exception
	{
		try
		{
			return pgA.contains(pnt);
		}
		catch (Exception ex)
		{
			throw new Exception(ex.getMessage());
		}
	}

	/**
	 * Reads this PolygonCSG object from the input file buffer.
	 * 
	 * @param fib     The input file buffer from which the PolygonCSG
	 *                description is read.
	 * @throws Exception
	 */
	public void read(FileInputBuffer fib) throws Exception
	{
		// read name and string descriptors

		rName = fib.readString();
		int n = fib.readInt();
		aToString = new String [n];
		for (int i = 0; i < aToString.length; ++i)
			aToString[i] = fib.readString();

		// read type (base or combinatorial) and check type

		int typ = fib.readInt();
		if (typ == 0)
		{
			// read base definition ... first not flag and number of polygon points

			notA  = fib.readBoolean();
			n = fib.readInt();

			// create point array and loop over all entries

			double[][] pts = new double [n][];
			for (int i = 0; i < n; ++i)
			{
				// get lat and lon and create point

				double lat = fib.readDouble();
				double lon = fib.readDouble();
				pts[i] = VectorGeo.getVectorDegrees(lat, lon);
			}

			// create the containment polygon

			pgA   = new PolygonPoints(pts);
		}
		else
		{
			// read combinatorial definition ... first region A

			notA = fib.readBoolean();
			A    = new PolygonCSG(fib);
			
			// now region B

			notB = fib.readBoolean();
			B    = new PolygonCSG(fib);
			
			// and finally operation (UNION or INTERSECT)

			opAB = CSGOperation.values()[fib.readInt()];
		}
	}

	/**
	 * Writes this PolygonCSG object to the output file buffer.
	 * 
	 * @param fob The output file buffer into which this PolygonCSG is written.
	 * @throws Exception
	 */
	public void write(FileOutputBuffer fob) throws Exception
	{
		// write PolygonCSG name and string descriptors

		fob.writeString(rName);
		fob.writeInt(aToString.length);
		for (int i = 0; i < aToString.length; ++i)
			fob.writeString(aToString[i]);
		if (isBaseDefinition())
		{
			// write base definition identifier (0), not flag, and number of polygon
			// points ... loop over all points
			
			double[][]  points = pgA.getPoints(false)[0];

			fob.writeInt(0);
			fob.writeBoolean(notA);
			fob.writeInt(pgA.size());
			for (double[] pnt : points)
			{
				// get next polygon point lat and lon and output both
				double lat = VectorGeo.getLatDegrees(pnt);
				double lon = VectorGeo.getLonDegrees(pnt);
				fob.writeDouble(lat);
				fob.writeDouble(lon);
			}
		}
		else
		{
			// write combinatorial identifier (1)

			fob.writeInt(1);
			
			// write PolygonCSG A and not flag, PolygonCSG B and not flag, and
			// finally the combinatorial operation

			fob.writeBoolean(notA);
			A.write(fob);
			fob.writeBoolean(notB);
			B.write(fob);
			fob.writeInt(opAB.ordinal());
		}
	}

	/**
	 * Returns the PolygonCSG operation as a string.
	 */
	@Override
	public String toString()
	{
		return toString("");
	}

	/**
	 * Returns the PolygonCSG combinatorial operation as a string. The input
	 * hdr is pre-pended to the line.
	 * 
	 * @param hdr The header pre-pended to the output string.
	 * @return The PolygonCSG combinatorial operation as a string.
	 */
	public String toString(String hdr)
	{
		String s = "";
		for (int i = 0; i < aToString.length; ++i)
			s += hdr + aToString[i] + NL;

		// return the string

		return s;
	}

	/**
	 * Returns true if this PolygonCSG is a base definition (i.e. pgA != null).
	 * 
	 * @return True if this PolygonCSG is a base definition (i.e. pgA != null).
	 */
	public boolean isBaseDefinition()
	{
		return (pgA != null) ? true : false;
	}

	/**
	 * Returns PolygonCSG A of a combinatorial region definition. If this is a
	 * base PolygonCSG null is returned.
	 * 
	 * @return PolygonCSG A of a combinatorial region definition.
	 */
	public PolygonCSG getRegionA()
	{
		return A;
	}

	/**
	 * Returns PolygonCSG B of a combinatorial region definition. If this is a
	 * base PolygonCSG null is returned.
	 * 
	 * @return PolygonCSG B of a combinatorial region definition.
	 */
	public PolygonCSG getRegionB()
	{
		return B;
	}

	/**
	 * Returns the PolygonCSG name.
	 * 
	 * @return The PolygonCSG name.
	 */
	public String getName()
	{
		return rName;
	}

	/**
	 * Returns a PolygonCSG object given its string parameter definition as
	 * input. The string definition has the following valid forms (all lat, lon,
	 * radius entries are in degrees)
	 * 
	 *   SphericalCap
	 *     regn = name lat lon radius
	 *   
	 *   Polygon2D
	 *     regn = name lat0 lon0 lat1 lon1 lat2 lon2 ...
	 *   
	 *   Combinatorial
	 *     regn = name [!]nameA OP [!]nameB
	 * 
	 * Each PolygonCSG begins with a name followed by its defining parameters.
	 * A spherical cap has the lon and lat (in degrees) of its center followed by
	 * the radius of the cap (in deg). The polygon has 3 or more lat/lon point
	 * pairs (in degrees) defining the polygon. Finally, the combinatorial region
	 * uses two previously  defined regions by name (nameA and nameB) and combines
	 * them into a new region using optional not operators ("!") and a single
	 * logical operator that can be intersection ("&") or union ("|"). Although
	 * rarely used, complex combinatorial regions can be constructed in this
	 * fashion.
	 * 
	 * @param regn    The PolygonCSG string parameter definition.
	 * @param regnMap A map of existing PolygonCSGs. Used if this is a
	 *                combinatorial PolygonCSG definition (depends on pre-
	 *                existing regions to make this new region). The new PolygonCSG
	 *                is added to this map on exit.
	 * @return The new PolygonCSG object.
	 * @throws Exception
	 */
	public static PolygonCSG getRegion(String regn,
			                               HashMap<String, PolygonCSG> regnMap)
			   throws Exception
	{
		PolygonCSG newRegion = null;

		// get the region definition parameters and see if combinatorial region
		// is requested

		String[] tokens = Globals.getTokens(regn, "\t, ");
		if ((tokens.length == 4) &&  
				(tokens[2].equals(CSGOperation.UNION.opSymbol()) ||
				 tokens[2].equals(CSGOperation.INTERSECT.opSymbol())))
		{
			// make a combinatorial region ... first get region a

			String rap = tokens[1];
			boolean rapnot = false;
			if (rap.substring(0, 1).equals("!"))
			{
				rapnot = true;
				rap = rap.substring(1);
			}
			PolygonCSG ra = regnMap.get(rap);
			if (ra == null)
				throw new Exception("Error: Unknown region: \"" + tokens[1] + "\"");

			// now get region b

			String rbp = tokens[3];
			boolean rbpnot = false;
			if (rbp.substring(0, 1).equals("!"))
			{
				rbpnot = true;
				rbp = rbp.substring(1);
			}
			PolygonCSG rb = regnMap.get(rbp);
			if (rb == null)
				throw new Exception("Error: Unknown region: \"" + tokens[2] + "\"");

			// now get the operation type and create the new region

			CSGOperation opType = PolygonCSG.getOpType(tokens[2]);
			newRegion = new PolygonCSG(tokens[0], rapnot, ra, rbpnot, rb, opType);
		}
		else
			// return a basic polygon region
			newRegion = new PolygonCSG(regn);

		// add to map and return

		regnMap.put(newRegion.getName(), newRegion);
		return newRegion;
	}

	/**
	 * Returns a comma separated string containing all PolygonCSG names of the
	 * PolygonCSG objects contained in the input set.
	 * 
	 * @param hdr     A header pre-pended to the output text. 
	 * @param regnSet The set of PolygonCSG objects from which names are
	 *                extracted.
	 * @return Returns a comma separated string containing all PolygonCSGs
	 *         names of the PolygonCSG objects contained in the input set.
	 */
	public static String getContainmentRegionNames(String hdr,
			                                           HashSet<PolygonCSG> regnSet)
	{
	  String s = "";

		s += hdr + "Containment Region(s): ";
		int i = 0;
		for (PolygonCSG rgn: regnSet)
		{
			s += rgn.getName();
			if (i < regnSet.size() - 1) s += ", ";
		}
		s += NL;

		return s;
	}

	/**
	 * Return a set of PolygonCSG objects that match the names defined in the
	 * input string array (regns).
	 * 
	 * @param regns   The input PolygonCSG names from which the PolygonCSG
	 *                object set is defined.
	 * @param regnSet The PolygonCSG object set that is returned to the caller.
	 * @throws Exception
	 */
	public static void getRegionSet(String[] regns, HashSet<PolygonCSG> regnSet,
			                            HashMap<String, PolygonCSG> regnMap)
	       throws Exception
	{
		// see if regions input array (regns) is not defined or empty ... if so
		// use global region
		
		if ((regns == null) || (regns.length == 0))
			regnSet.add(regnMap.get("GLOBAL"));
		else
		{
			// loop over each region entry

			for (int i = 0; i < regns.length; ++i)
			{
				// retrieve entry from map and add it to the set ... if not defined
				// throw an error

				PolygonCSG regn = regnMap.get(regns[i].toUpperCase());
				if (regn == null)
					throw new Exception("Error: Unknown Region Name: \"" + regns[i] + "\"");

				// add region to set and continue
				
        regnSet.add(regn);
			}
		}
	}

	/**
	 * Static function that returns 0 if opName is "|" and 1 if opName is "&".
	 * 
	 * @param opName The operation name.
	 * @return 0 or 1.
	 */
	public static CSGOperation getOpType(String opName)
	{
		if (opName.equals(CSGOperation.UNION.opSymbol()))
			return CSGOperation.UNION;
		else if (opName.equals(CSGOperation.INTERSECT.opSymbol()))
			return CSGOperation.INTERSECT;
		else
			return null;
	}
}
