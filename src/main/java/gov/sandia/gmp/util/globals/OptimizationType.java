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
package gov.sandia.gmp.util.globals;

/**
 * Execution can be optimized either for speed or memory. If optimization is
 * set to SPEED, then the following optimization strategies will be
 * implemented:
 * <ul>
 * <li>for each edge of a triangle the unit vector normal to the plane of
 * the great circle containing the edge will be computed during input of the
 * grid from file and stored in memory. With this information, the walking
 * triangle algorithm can use dot products instead of scalar triple products
 * when determining if a point resides inside a triangle. While much more
 * computationally efficient, it requires a lot of memory to store all those
 * unit vectors.
 * <li>when performing natural neighbor interpolation, lazy evaluation will
 * be used to store the circumcenters of triangles that are computed during
 * interpolation.
 * <li>when interpolating along radial profiles, every profile will record
 * the index of the radius that is discovered. That index will be the
 * starting point for the binary search the next time binary search is
 * implemented. Each GeoTessPosition object will store 2d array of shorts,
 * short[nVertices][nlayers] to record this information. Might be ~1MB per
 * GeoTessPosition object (they could share references to the same short[][]
 * as long as they don't break concurrency.
 * </ul>
 */
public enum OptimizationType
{
	/**
	 * Model is optimized for speed.
	 */
	SPEED,

	/**
	 * Model is optimized for memory.
	 */
	MEMORY
};
