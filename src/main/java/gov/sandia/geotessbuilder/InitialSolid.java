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
package gov.sandia.geotessbuilder;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import gov.sandia.geotess.GeoTessException;
import gov.sandia.geotess.GeoTessGrid;
import gov.sandia.gmp.util.numerical.platonicsolid.PlatonicSolid;
import gov.sandia.gmp.util.numerical.vector.VectorGeo;
import gov.sandia.gmp.util.numerical.vector.VectorUnit;

public class InitialSolid {

	/**
	 * A n by 3 array of doubles where n is the number of verteces in the polyhedron
	 * and 3 is the number of components in the unit vector.
	 */
	private final double[][] vertices;

	/**
	 * The connectivity for each face of the polyhedron. This is an nFaces by
	 * nVerticesPerFace array of integers where the integers are the index of the
	 * vertex in attribute vertices.
	 */
	private final int[][] faces;

	/**
	 * Constructor
	 *
	 * @param platonicSolid
	 */
	public InitialSolid(PlatonicSolid platonicSolid) {
		this.vertices = new double[platonicSolid.getVertices().length][];
		for (int i = 0; i < vertices.length; ++i)
			this.vertices[i] = platonicSolid.getVertex(i).clone();
		this.faces = platonicSolid.getFaces();
	}

	/**
	 * 
	 * @param grid
	 * @param tessId
	 * @throws GeoTessException
	 */
	public InitialSolid(GeoTessGrid grid, int tessId) throws GeoTessException {
		this.vertices = new double[grid.getVertices(tessId, 0).size()][];
		this.faces = new int[grid.getNTriangles(tessId, 0)][];

		for (int i = 0; i < vertices.length; ++i)
			this.vertices[i] = grid.getVertex(i).clone();

		for (int i = 0; i < grid.getNTriangles(tessId, 0); ++i)
			faces[i] = grid.getTriangleVertexIndexes(i);
	}

	/**
	 * Constructor
	 * 
	 * @param vertices double[][]
	 * @param faces    int[][]
	 */
	public InitialSolid(double[][] vertices, int[][] faces) {
		this.vertices = vertices;
		this.faces = faces;
	}

	/**
	 * Rotate all the vertices of this solid such that vertex(0) resides at location
	 * specified by lat, lon.
	 * 
	 * @param lat       latitude
	 * @param lon       longitude
	 * @param inDegrees if true, lat,lon are in degrees, otherwise radians.
	 */
	public void rotateAngles(double lat, double lon, boolean inDegrees) {
	    rotateAngles(VectorUnit.getEulerRotationAngles(inDegrees 
		    ? VectorGeo.getVectorDegrees(lat, lon)
			    : VectorGeo.getVector(lat, lon)));
	}

	/**
	 * Rotate all the vertices of this solid by the specified euler rotation angles.
	 * 
	 * @param eulerRotationAngles 3-element array with rotation angles in radians.
	 */
	public void rotateAngles(double[] eulerRotationAngles) {
		rotateMatrix(VectorGeo.getEulerMatrix(eulerRotationAngles));
	}

	/**
	 * Rotate all the vertices of this solid by the specified euler rotation matrix.
	 * 
	 * @param eulerMatrix a 3x3 matrix stored in a 1D 9-element array
	 */
	public void rotateMatrix(double[] eulerMatrix) {
		for (double[] vertex : vertices)
			VectorGeo.eulerRotation(vertex, eulerMatrix, vertex);
	}

	/**
	 * Retrieve the number of verteces that define the polyhedron: 4 for
	 * tetrahedron, 8 for cube, 6 for octahedron, 12 for icosahedron and 20 for
	 * dodecahedron.
	 * 
	 * @return int
	 */
	public int getNVertices() {
		return vertices.length;
	}

	/**
	 * Retrieve the number of faces that define the polyhedron: 4 for tetrahedron, 6
	 * for cube, 8 for octahedron, 20 for icosahedron and 12 for dodecahedron.
	 * 
	 * @return int
	 */
	public int getNFaces() {
		return faces.length;
	}

	/**
	 * Retrieve the number of verteces that define each face of the polyhedron: 3
	 * for tetrahedron, 4 for cube, 3 for octahedron, 3 for icosahedron and 5 for
	 * dodecahedron.
	 * 
	 * @return int
	 */
	public int getNVerticesPerFace() {
		return faces[0].length;
	}

	/**
	 * Retrieve and array of unit vectors, one for each vertex of the polyhedron.
	 * 
	 * @return double[][]
	 */
	public double[][] getVertices() {
		return vertices;
	}

	/**
	 * Retrieve the unit vector for the i'th vertex.
	 * 
	 * @param i int
	 * @return double[]
	 */
	public double[] getVertex(int i) {
		return vertices[i];
	}

	/**
	 * Get the unit vector of the vertex that is the j'th vertex of the i'th face of
	 * the polyhedron. i ranges from 0 to nFaces and j ranges from 0 to
	 * nVertecesPerFace.
	 * 
	 * @param i int
	 * @param j int
	 * @return double[]
	 */
	public double[] getVertex(int i, int j) {
		return vertices[faces[i][j]];
	}

	/**
	 * Retrieve the connectivity map for all the faces of the polyhedron.
	 * 
	 * @return int[][] nFaces by nVerticesPerFace array of integers where the
	 *         integers are the index of the vertex.
	 */
	public int[][] getFaces() {
		return faces;
	}

	/**
	 * Retrieve the connectivity of the i'th face fo the polyhedron.
	 * 
	 * @param i int the index of the face for which the connectivity is desired.
	 * @return int[] the indeces of the verteces that comprise the i'th face. The
	 *         length will be 3 for tetrahedron, 4 for cube, 3 for octahedron, 3 for
	 *         icosahedron and 5 for dodecahedron.
	 */
	public int[] getFace(int i) {
		return faces[i];
	}

	/**
	 * Retrieve the length of the first edge of the first face of the solid, in
	 * radians.
	 * 
	 * @param nSubdivisions number of times actual edgelength should be divided in
	 *                      half.
	 * @return the length of the first edge of the first face of the solid, in
	 *         radians.
	 */
	public double getEdgeLength(int nSubdivisions) {
		double[] u = getVertex(0, 0);
		double[] v = getVertex(0, 1);
		double length = Math.acos(u[0] * v[0] + u[1] * v[1] + u[2] * v[2]);
		for (int i = 1; i < nSubdivisions; ++i)
			length *= 0.5;
		return length;
	}

	/**
	 * Retrieve the length of the first edge of the first face of the solid, in
	 * degrees.
	 * 
	 * @param nSubdivisions number of times actual edgelength should be divided in
	 *                      half.
	 * @return the length of the first edge of the first face of the solid, in
	 *         degrees.
	 */
	public double getEdgeLengthDegrees(int nSubdivisions) {
		return Math.toDegrees(getEdgeLength(nSubdivisions));
	}

	public void vtkGrid(File file) throws IOException {
		DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));

		output.writeBytes(String.format("# vtk DataFile Version 2.0%n"));
		output.writeBytes(String.format("InitialSolid%n"));
		output.writeBytes(String.format("BINARY%n"));

		output.writeBytes(String.format("DATASET UNSTRUCTURED_GRID%n"));

		output.writeBytes(String.format("POINTS %d double%n", vertices.length));

		// iterate over all the grid vertices and write out their position
		double[] vertex;
		double radius = 1;
		for (int i = 0; i < vertices.length; ++i) {
			vertex = vertices[i];
			// radius = model.getProfile(i, layerId).getRadiusTop();
			output.writeDouble(vertex[0] * radius);
			output.writeDouble(vertex[1] * radius);
			output.writeDouble(vertex[2] * radius);
		}

		// write out node connectivity
		int nTriangles = faces.length;
		output.writeBytes(String.format("CELLS %d %d%n", nTriangles, nTriangles * 4));

		for (int t = 0; t < faces.length; ++t) {
			output.writeInt(3);
			output.writeInt(faces[t][0]);
			output.writeInt(faces[t][1]);
			output.writeInt(faces[t][2]);
		}

		output.writeBytes(String.format("CELL_TYPES %d%n", nTriangles));
		for (int t = 0; t < nTriangles; ++t)
			output.writeInt(5); // vtk_triangle

		output.close();
	}

}
