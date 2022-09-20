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
package gov.sandia.gmp.baseobjects.globals;

import gov.sandia.gmp.baseobjects.interfaces.impl.Prediction;
import gov.sandia.gmp.util.globals.Globals;

/**
 * Enum of seismic phases, assembled by Richard Stead at LANL. Started by
 * executing the following SQL statement against the GLOSSARY table:
 * <p>
 * select name, definition from glossary where column_name='phase' and
 * schema_name='USxxxNDC P2B2' order by glossid, lineno
 * <p>
 * Parsed the result and made the following modifications:
 * <ul>
 * <li>replaced '*' with '_star_'
 * <li>replaced '-' with '_minus_'
 * <li>replaced '+' with '_plus_'
 * <li>replaced "'" with "_prime_"
 * <li>if name starts with digit, prepended '_'
 * </ul>
 * <p>
 * Renamed filenames for phases that have names that depend on case sensitivity
 * for uniqueness:<br>
 * bigN_bigP -> NP <br>
 * bigP_bigP -> PP <br>
 * bigP_bigS -> PS <br>
 * bigS_bigP -> SP <br>
 * bigS_bigS -> SS <br>
 * littlen_bigP -> nP <br>
 * littlep_bigP -> pP <br>
 * littlep_bigS -> pS <br>
 * littles_bigP -> sP <br>
 * littles_bigS -> sS
 * 
 * @author sballar
 * 
 */
public enum SeismicPhase {
	/**
	 * At short distances, either an upgoing P wave from a source in the upper crust
	 * or a P wave bottoming in the upper crust. At larger distances also arrivals
	 * caused by multiple P-wave reverberations inside the whole crust with a group
	 * velocity around 5.8 km/s.
	 */
	Pg("PSLOWNESS", "BOTTOM, LOWER_CRUST_TOP",
			"At short distances, either an upgoing P wave from a source in the"
					+ "<BR>upper crust or a P wave bottoming in the upper crust. At larger"
					+ "<BR>distances also arrivals caused by multiple P-wave reverberations inside"
					+ "<BR>the whole crust with a group velocity around 5.8 km/s."),

	/**
	 * (alternate: Pb) Either an upgoing P wave from a source in the lower crust
	 */
	P_star_(null, "(alternate: Pb) Either an upgoing P wave from a source in the lower crust"),

	/**
	 * (alternate: P*) Either an upgoing P wave from a source in the lower crust or
	 * a P wave bottoming in the lower crust
	 */
	Pb(GeoAttributes.PSLOWNESS,
			"(alternate: P*) Either an upgoing P wave from a source in the lower crust"
					+ "<BR>or a P wave bottoming in the lower crust"),

	/**
	 * Any P wave bottoming in the uppermost mantle or an upgoing P wave from a
	 * source in the uppermost mantle
	 */
	Pn("PSLOWNESS", "BOTTOM, M410",
			"Any P wave bottoming in the uppermost mantle or an upgoing P wave"
					+ "<BR>from a source in the uppermost mantle"),

	/**
	 * Pn free surface reflection
	 */
	PnPn(GeoAttributes.PSLOWNESS, "Pn free surface reflection"),

	/**
	 * Pg free surface reflection
	 */
	PgPg(GeoAttributes.PSLOWNESS, "Pg free surface reflection"),

	/**
	 * P reflection from the outer side of the Moho
	 */
	PmP("PSLOWNESS", "TOP_SIDE_REFLECTION, MOHO", "P reflection from the outer side of the Moho"),

	/**
	 * PmP free surface reflection, (PmPPmP)
	 */
	PmP2(GeoAttributes.PSLOWNESS, "PmP free surface reflection, (PmPPmP)"),

	/**
	 * PmP multiple free surface reflection, (2x)
	 */
	PmP3(GeoAttributes.PSLOWNESS, "PmP multiple free surface reflection, (2x)"),

	/**
	 * PmP multiple free surface reflection, (3x)
	 */
	PmP4(GeoAttributes.PSLOWNESS, "PmP multiple free surface reflection, (3x)"),

	/**
	 * PmP multiple free surface reflection, (4x)
	 */
	PmP5(GeoAttributes.PSLOWNESS, "PmP multiple free surface reflection, (4x)"),

	/**
	 * PmP multiple free surface reflection, (5x)
	 */
	PmP6(GeoAttributes.PSLOWNESS, "PmP multiple free surface reflection, (5x)"),

	/**
	 * PmP multiple free surface reflection, (6x)
	 */
	PmP7(GeoAttributes.PSLOWNESS, "PmP multiple free surface reflection, (6x)"),

	/**
	 * P to S reflection from the outer side of the Moho
	 */
	PmS(null, "P to S reflection from the outer side of the Moho"),

	/**
	 * At short distances, either an upgoing S wave from a source in the upper crust
	 * or an S wave bottoming in the upper crust. At larger distances also arrivals
	 * caused by superposition of multiple S-wave reverberations and SV to P and/or
	 * P to SV conversions inside the whole crust.
	 */
	Sg("SSLOWNESS", "BOTTOM, LOWER_CRUST_TOP",
			"At short distances, either an upgoing S wave from a source in the"
					+ "<BR>upper crust or an S wave bottoming in the upper crust. At larger"
					+ "<BR>distances also arrivals caused by superposition of multiple S-wave"
					+ "<BR>reverberations and SV to P and/or P to SV conversions inside the whole crust."),

	/**
	 * (alternate: Sb) Either an upgoing S wave from a source in the lower crust
	 */
	S_star_(GeoAttributes.SSLOWNESS, "(alternate: Sb) Either an upgoing S wave from a source in the lower crust"),

	/**
	 * (alternate: S*) Either an upgoing S wave from a source in the lower crust or
	 * an S wave bottoming in the lower crust
	 */
	Sb(GeoAttributes.SSLOWNESS,
			"(alternate: S*) Either an upgoing S wave from a source in the lower crust"
					+ "<BR>or an S wave bottoming in the lower crust"),

	/**
	 * Any S wave bottoming in the uppermost mantle or an upgoing S wave from a
	 * source in the uppermost mantle
	 */
	Sn("SSLOWNESS", "BOTTOM, M410",
			"Any S wave bottoming in the uppermost mantle or an upgoing S wave"
					+ "<BR>from a source in the uppermost mantle"),

	/**
	 * Sn free surface reflection
	 */
	SnSn(GeoAttributes.SSLOWNESS, "Sn free surface reflection"),

	/**
	 * Sg free surface reflection
	 */
	SgSg(GeoAttributes.SSLOWNESS, "Sg free surface reflection"),

	/**
	 * S reflection from the outer side of the Moho
	 */
	SmS(GeoAttributes.SSLOWNESS, "S reflection from the outer side of the Moho"),

	/**
	 * SmS free surface reflection, (SmSSmS)
	 */
	SmS2(GeoAttributes.SSLOWNESS, "SmS free surface reflection, (SmSSmS)"),

	/**
	 * SmS multiple free surface reflection, (2x)
	 */
	SmS3(GeoAttributes.SSLOWNESS, "SmS multiple free surface reflection, (2x)"),

	/**
	 * SmS multiple free surface reflection, (3x)
	 */
	SmS4(GeoAttributes.SSLOWNESS, "SmS multiple free surface reflection, (3x)"),

	/**
	 * SmS multiple free surface reflection, (4x)
	 */
	SmS5(GeoAttributes.SSLOWNESS, "SmS multiple free surface reflection, (4x)"),

	/**
	 * SmS multiple free surface reflection, (5x)
	 */
	SmS6(GeoAttributes.SSLOWNESS, "SmS multiple free surface reflection, (5x)"),

	/**
	 * SmS multiple free surface reflection, (6x)
	 */
	SmS7(GeoAttributes.SSLOWNESS, "SmS multiple free surface reflection, (6x)"),

	/**
	 * S to P reflection from the outer side of the Moho
	 */
	SmP(null, "S to P reflection from the outer side of the Moho"),

	/**
	 * A wave group observed at larger regional distances and caused by
	 * superposition of multiple S-wave reverberations and SV to P and/or P to SV
	 * conversions inside the whole crust. The maximum energy travels with a group
	 * velocity around 3.5 km/s
	 */
	Lg("SSLOWNESS", "BOTTOM, LOWER_CRUST_TOP",
			"A wave group observed at larger regional distances and caused by"
					+ "<BR>superposition of multiple S-wave reverberations and SV to P and/or P to"
					+ "<BR>SV conversions inside the whole crust. The maximum energy travels with a"
					+ "<BR>group velocity around 3.5 km/s"),

	/**
	 * Short period crustal Rayleigh wave
	 */
	Rg(null, "Short period crustal Rayleigh wave"),

	/**
	 * A longitudinal wave, bottoming below the uppermost mantle; also an upgoing
	 * longitudinal wave from a source below the uppermost mantle
	 */
	P("PSLOWNESS", "BOTTOM, M660",
			"A longitudinal wave, bottoming below the uppermost mantle; also an"
					+ "<BR>upgoing longitudinal wave from a source below the uppermost mantle"),

	/**
	 * The first arriving compressional wave. Defined from 0 to 180 degrees
	 * epicentral distance. This is typically Pg at local distances and Pn at
	 * regional distances. At teleseismic distances, this is usually P then Pdiff
	 * then PKIKP.
	 * <p>
	 * Added by sballar 2/6/2011
	 */
	Pfirst(GeoAttributes.PSLOWNESS, "The first arriving compressional wave.  Defined from 0 to 180 degrees"
			+ "<BR>epicentral distance.  This is typically Pg at local distances and Pn "
			+ "<BR>at regional distances.  At teleseismic distances, this" + "<BR>is usually P then Pdiff then PKIKP."),

	/**
	 * A compressional wave that bottoms in the mantle. Defined from 0 to 180
	 * degrees epicentral distance. At local distances, this is PmP (reflection off
	 * the MOHO). At regional to near teleseismic distances this is a refracted
	 * compressional wave that bottoms in the mantle. At far teleseismic, this is
	 * Pdiff (a compressional wave that is diffracted along the CMB).
	 * <p>
	 * Added by sballar 2/6/2011
	 */
	Pmantle("PSLOWNESS", "BOTTOM, M410",
			"A compressional wave that bottoms in the mantle.  Defined from 0 to 180"
					+ "<BR>degrees epicentral distance.  At local distances, this is PmP (reflection "
					+ "<BR>off the MOHO).  At regional to near teleseismic distances this is a refracted "
					+ "<BR>compressional wave that bottoms in the mantle.  At far teleseismic, this is "
					+ "<BR>Pdiff (a compressional wave that is diffracted along the CMB)."),

	/**
	 * Free surface reflection of P wave leaving a source downwards
	 */
	PP("PSLOWNESS", "BOTTOM, M660, BOTTOM_SIDE_REFLECTION, FREE_SURFACE, BOTTOM, M660",
			"Free surface reflection of P wave leaving a source downwards", "bigP_bigP"),

	/**
	 * P, leaving a source downwards, reflected as an S at the free surface. At
	 * shorter distances the first leg is represented by a crustal P wave.
	 */
	PS("PSLOWNESS, FREE_SURFACE, SSLOWNESS", "BOTTOM, M660, BOTTOM_SIDE_REFLECTION, FREE_SURFACE, BOTTOM, M660",
			"P, leaving a source downwards, reflected as an S at the free surface."
					+ "<BR>At shorter distances the first leg is represented by a crustal P wave.",
			"bigP_bigS"),

	/**
	 * analogous to PP, multiple free surface reflection of P (2x)
	 */
	PPP("PSLOWNESS",
			"BOTTOM, M660, BOTTOM_SIDE_REFLECTION, FREE_SURFACE, BOTTOM, M660, BOTTOM_SIDE_REFLECTION, FREE_SURFACE, BOTTOM, M660",
			"analogous to PP, multiple free surface reflection of P (2x)"),

	/**
	 * PP to S converted reflection at the free surface; travel time matches that of
	 * PSP
	 */
	PPS("PSLOWNESS, FREE_SURFACE, PSLOWNESS, FREE_SURFACE, SSLOWNESS",
			"BOTTOM, M660, BOTTOM_SIDE_REFLECTION, FREE_SURFACE, BOTTOM, M660, BOTTOM_SIDE_REFLECTION, FREE_SURFACE, BOTTOM, M660",
			"PP to S converted reflection at the free surface; travel time" + "<BR>matches that of PSP"),

	/**
	 * PS reflected at the free surface
	 */
	PSS("PSLOWNESS, FREE_SURFACE, SSLOWNESS",
			"BOTTOM, M660, BOTTOM_SIDE_REFLECTION, FREE_SURFACE, BOTTOM, M660, BOTTOM_SIDE_REFLECTION, FREE_SURFACE, BOTTOM, M660",
			"PS reflected at the free surface"),

	/**
	 * P reflection from the core-mantle boundary (CMB)
	 */
	PcP("PSLOWNESS", "TOP_SIDE_REFLECTION, CMB", "P reflection from the core-mantle boundary (CMB)"),

	/**
	 * P to S converted reflection from the CMB
	 */
	PcS("PSLOWNESS, CMB, SSLOWNESS", "TOP_SIDE_REFLECTION, CMB", "P to S converted reflection from the CMB"),

	/**
	 * PcP free surface reflection, (PcPPcP)
	 */
	PcP2(GeoAttributes.PSLOWNESS, "PcP free surface reflection, (PcPPcP)"),

	/**
	 * PcP multiple free surface reflection, (2x)
	 */
	PcP3(GeoAttributes.PSLOWNESS, "PcP multiple free surface reflection, (2x)"),

	/**
	 * PcP multiple free surface reflection, (3x)
	 */
	PcP4(GeoAttributes.PSLOWNESS, "PcP multiple free surface reflection, (3x)"),

	/**
	 * PcP multiple free surface reflection, (4x)
	 */
	PcP5(GeoAttributes.PSLOWNESS, "PcP multiple free surface reflection, (4x)"),

	/**
	 * PcP multiple free surface reflection, (5x)
	 */
	PcP6(GeoAttributes.PSLOWNESS, "PcP multiple free surface reflection, (5x)"),

	/**
	 * PcP multiple free surface reflection, (6x)
	 */
	PcP7(GeoAttributes.PSLOWNESS, "PcP multiple free surface reflection, (6x)"),

	/**
	 * (alternate: P660P) P reflection from outer side of a discontinuity at depth
	 * 660.
	 */
	P660_plus_P(null, "(alternate: P660P) P reflection from outer side of a discontinuity at depth 660."),

	/**
	 * (alternate: P410P) P reflection from outer side of a discontinuity at depth
	 * 410.
	 */
	P410_plus_P(null, "(alternate: P410P) P reflection from outer side of a discontinuity at depth 410."),

	/**
	 * (alternate: P210P) P reflection from outer side of a discontinuity at depth
	 * 210.
	 */
	P210_plus_P(null, "(alternate: P210P) P reflection from outer side of a discontinuity at depth 210."),

	/**
	 * P reflection from inner side of discontinuity at depth 660. P660-P is a P
	 * reflection from below the 660 km discontinuity, which means it is precursory
	 * to PP.
	 */
	P660_minus_P(null,
			"P reflection from inner side of discontinuity at depth 660."
					+ "<BR>P660-P is a P reflection from below the 660 km discontinuity,"
					+ "<BR>which means it is precursory to PP."),

	/**
	 * P reflection from inner side of discontinuity at depth 410. P410-P is a P
	 * reflection from below the 410 km discontinuity, which means it is precursory
	 * to PP.
	 */
	P410_minus_P(null,
			"P reflection from inner side of discontinuity at depth 410."
					+ "<BR>P410-P is a P reflection from below the 410 km discontinuity,"
					+ "<BR>which means it is precursory to PP."),

	/**
	 * P reflection from inner side of discontinuity at depth 210. P210-P is a P
	 * reflection from below the 210 km discontinuity, which means it is precursory
	 * to PP.
	 */
	P210_minus_P(null,
			"P reflection from inner side of discontinuity at depth 210."
					+ "<BR>P210-P is a P reflection from below the 210 km discontinuity,"
					+ "<BR>which means it is precursory to PP."),

	/**
	 * (alternate: P660S) P to S converted reflection from outer side of
	 * discontinuity at depth 660
	 */
	P660_plus_S(null,
			"(alternate: P660S) P to S converted reflection from outer side of" + "<BR>discontinuity at depth 660"),

	/**
	 * (alternate: P410S) P to S converted reflection from outer side of
	 * discontinuity at depth 410
	 */
	P410_plus_S(null,
			"(alternate: P410S) P to S converted reflection from outer side of" + "<BR>discontinuity at depth 410"),

	/**
	 * (alternate: P210S) P to S converted reflection from outer side of
	 * discontinuity at depth 210
	 */
	P210_plus_S(null,
			"(alternate: P210S) P to S converted reflection from outer side of" + "<BR>discontinuity at depth 210"),

	/**
	 * P to S converted reflection from inner side of discontinuity at depth 660
	 */
	P660_minus_S(null, "P to S converted reflection from inner side of discontinuity at depth 660"),

	/**
	 * P to S converted reflection from inner side of discontinuity at depth 410
	 */
	P410_minus_S(null, "P to S converted reflection from inner side of discontinuity at depth 410"),

	/**
	 * P to S converted reflection from inner side of discontinuity at depth 210
	 */
	P210_minus_S(null, "P to S converted reflection from inner side of discontinuity at depth 210"),

	/**
	 * P (leaving a source downwards) to ScS reflection at the free surface
	 */
	PScS(null, "P (leaving a source downwards) to ScS reflection at the free surface"),

	/**
	 * (old: Pdiff) P diffracted along the CMB in the mantle
	 */
	Pdif("PSLOWNESS", "BOTTOM, M660", "(old: Pdiff) P diffracted along the CMB in the mantle"),

	/**
	 * P diffracted along the CMB in the mantle
	 */
	Pdiff("PSLOWNESS", "BOTTOM, M660", "P diffracted along the CMB in the mantle"),

	/**
	 * A shear wave, bottoming below the uppermost mantle; also an upgoing shear
	 * wave from a source below the uppermost mantle
	 */
	S("SSLOWNESS", "BOTTOM, M660",
			"A shear wave, bottoming below the uppermost mantle; also an upgoing"
					+ "<BR>shear wave from a source below the uppermost mantle"),

	/**
	 * The first arriving shear wave. Defined from 0 to 180 degrees epicentral
	 * distance. This is typically Lg at local distances and Sn at regional
	 * distances. At teleseismic distances, this is usually S then Sdiff then a
	 * shear wave core phase.
	 * <p>
	 * Added by sballar 2/6/2011
	 */
	Sfirst(GeoAttributes.SSLOWNESS,
			"The first arriving shear wave.  Defined from 0 to 180 degrees epicentral"
					+ "<BR>distance.  This is typically Lg at local "
					+ "<BR>distances and Sn at regional distances.  At teleseismic distances, this"
					+ "<BR>is usually S then Sdiff then a shear wave core phase."),

	/**
	 * A shear wave that bottoms in the mantle. Defined from 0 to 180 degrees
	 * epicentral distance. At local distances, this is SmS (reflection off the
	 * MOHO). At regional to near teleseismic distances this is a refracted shear
	 * wave that bottoms in the mantle. At far teleseismic, this is Sdiff (a shear
	 * wave that is diffracted along the CMB).
	 * <p>
	 * Added by sballar 2/6/2011
	 */
	Smantle("SSLOWNESS", "BOTTOM, M410",
			"A shear wave that bottoms in the mantle.  Defined from 0 to 180"
					+ "<BR>degrees epicentral distance.  At local distances, this"
					+ "<BR>is SmS (reflection off the MOHO).  At regional to near teleseismic distances"
					+ "<BR>this is a refracted shear wave that bottoms in the mantle.  At"
					+ "<BR>far teleseismic, this is Sdiff (a shear wave that is diffracted " + "<BR>along the CMB)."),

	/**
	 * Free surface reflection of an S wave leaving a source downwards
	 */
	SS("SSLOWNESS", "BOTTOM, M660, BOTTOM_SIDE_REFLECTION, FREE_SURFACE, BOTTOM, M660",
			"Free surface reflection of an S wave leaving a source downwards", "bigS_bigS"),

	/**
	 * S, leaving source downwards, reflected as P at the free surface. At shorter
	 * distances the second leg is represented by a crustal P wave.
	 */
	SP("SSLOWNESS, FREE_SURFACE, PSLOWNESS", "BOTTOM, M660, BOTTOM_SIDE_REFLECTION, FREE_SURFACE, BOTTOM, M660",
			"S, leaving source downwards, reflected as P at the free surface. At"
					+ "<BR>shorter distances the second leg is represented by a crustal P wave.",
			"bigS_bigP"),

	/**
	 * analogous to SS, multiple free surface reflection of S (2x)
	 */
	SSS("SSLOWNESS",
			"BOTTOM, M660, BOTTOM_SIDE_REFLECTION, FREE_SURFACE, BOTTOM, M660, BOTTOM_SIDE_REFLECTION, FREE_SURFACE, BOTTOM, M660",
			"analogous to SS, multiple free surface reflection of S (2x)"),

	/**
	 * SS to P converted reflection at the free surface; travel time matches that of
	 * SPS
	 */
	SSP("SSLOWNESS, FREE_SURFACE, SSLOWNESS, FREE_SURFACE, PSLOWNESS",
			"BOTTOM, M660, BOTTOM_SIDE_REFLECTION, FREE_SURFACE, BOTTOM, M660, BOTTOM_SIDE_REFLECTION, FREE_SURFACE, BOTTOM, M660",
			"SS to P converted reflection at the free surface; travel time" + "<BR>matches that of SPS"),

	/**
	 * SP reflected at the free surface
	 */
	SPP("SSLOWNESS, FREE_SURFACE, PSLOWNESS",
			"BOTTOM, M660, BOTTOM_SIDE_REFLECTION, FREE_SURFACE, BOTTOM, M660, BOTTOM_SIDE_REFLECTION, FREE_SURFACE, BOTTOM, M660",
			"SP reflected at the free surface"),

	/**
	 * S reflection from the CMB
	 */
	ScS("SSLOWNESS", "TOP_SIDE_REFLECTION, CMB", "S reflection from the CMB"),

	/**
	 * S to P converted reflection from the CMB
	 */
	ScP("SSLOWNESS, CMB, PSLOWNESS", "TOP_SIDE_REFLECTION, CMB", "S to P converted reflection from the CMB"),

	/**
	 * ScS free surface reflection, (ScSScS)
	 */
	ScS2(GeoAttributes.SSLOWNESS, "ScS free surface reflection, (ScSScS)"),

	/**
	 * ScS multiple free surface reflection, (2x)
	 */
	ScS3(GeoAttributes.SSLOWNESS, "ScS multiple free surface reflection, (2x)"),

	/**
	 * ScS multiple free surface reflection, (3x)
	 */
	ScS4(GeoAttributes.SSLOWNESS, "ScS multiple free surface reflection, (3x)"),

	/**
	 * ScS multiple free surface reflection, (4x)
	 */
	ScS5(GeoAttributes.SSLOWNESS, "ScS multiple free surface reflection, (4x)"),

	/**
	 * ScS multiple free surface reflection, (5x)
	 */
	ScS6(GeoAttributes.SSLOWNESS, "ScS multiple free surface reflection, (5x)"),

	/**
	 * ScS multiple free surface reflection, (6x)
	 */
	ScS7(GeoAttributes.SSLOWNESS, "ScS multiple free surface reflection, (6x)"),

	/**
	 * (alternate: S660S) S reflection from outer side of a discontinuity at depth
	 * 660.
	 */
	S660_plus_S(null, "(alternate: S660S) S reflection from outer side of a discontinuity at depth 660."),

	/**
	 * (alternate: S410S) S reflection from outer side of a discontinuity at depth
	 * 410.
	 */
	S410_plus_S(null, "(alternate: S410S) S reflection from outer side of a discontinuity at depth 410."),

	/**
	 * (alternate: S210S) S reflection from outer side of a discontinuity at depth
	 * 210.
	 */
	S210_plus_S(null, "(alternate: S210S) S reflection from outer side of a discontinuity at depth 210."),

	/**
	 * S reflection from inner side of discontinuity at depth 660. S660-S is a S
	 * reflection from below the 660 km discontinuity, which means it is precursory
	 * to SS.
	 */
	S660_minus_S(GeoAttributes.SSLOWNESS,
			"S reflection from inner side of discontinuity at depth 660."
					+ "<BR>S660-S is a S reflection from below the 660 km discontinuity,"
					+ "<BR>which means it is precursory to SS."),

	/**
	 * S reflection from inner side of discontinuity at depth 410. S410-S is a S
	 * reflection from below the 410 km discontinuity, which means it is precursory
	 * to SS.
	 */
	S410_minus_S(GeoAttributes.SSLOWNESS,
			"S reflection from inner side of discontinuity at depth 410."
					+ "<BR>S410-S is a S reflection from below the 410 km discontinuity,"
					+ "<BR>which means it is precursory to SS."),

	/**
	 * S reflection from inner side of discontinuity at depth 210. S210-S is a S
	 * reflection from below the 210 km discontinuity, which means it is precursory
	 * to SS.
	 */
	S210_minus_S(GeoAttributes.SSLOWNESS,
			"S reflection from inner side of discontinuity at depth 210."
					+ "<BR>S210-S is a S reflection from below the 210 km discontinuity,"
					+ "<BR>which means it is precursory to SS."),

	/**
	 * (alternate: S660P) S to P converted reflection from outer side of
	 * discontinuity at depth 660
	 */
	S660_plus_P(null,
			"(alternate: S660P) S to P converted reflection from outer side of" + "<BR>discontinuity at depth 660"),

	/**
	 * (alternate: S410P) S to P converted reflection from outer side of
	 * discontinuity at depth 410
	 */
	S410_plus_P(null,
			"(alternate: S410P) S to P converted reflection from outer side of" + "<BR>discontinuity at depth 410"),

	/**
	 * (alternate: S210P) S to P converted reflection from outer side of
	 * discontinuity at depth 210
	 */
	S210_plus_P(null,
			"(alternate: S210P) S to P converted reflection from outer side of" + "<BR>discontinuity at depth 210"),

	/**
	 * S to P converted reflection from inner side of discontinuity at depth 660
	 */
	S660_minus_P(null, "S to P converted reflection from inner side of discontinuity at depth 660"),

	/**
	 * S to P converted reflection from inner side of discontinuity at depth 410
	 */
	S410_minus_P(null, "S to P converted reflection from inner side of discontinuity at depth 410"),

	/**
	 * S to P converted reflection from inner side of discontinuity at depth 210
	 */
	S210_minus_P(null, "S to P converted reflection from inner side of discontinuity at depth 210"),

	/**
	 * ScS to P reflection at the free surface
	 */
	ScSP(null, "ScS to P reflection at the free surface"),

	/**
	 * (old: Sdiff) S diffracted along the CMB in the mantle
	 */
	Sdif("SSLOWNESS", "BOTTOM, M660", "(old: Sdiff) S diffracted along the CMB in the mantle"),

	/**
	 * S diffracted along the CMB in the mantle
	 */
	Sdiff("SSLOWNESS", "BOTTOM, M660", "S diffracted along the CMB in the mantle"),

	/**
	 * (alternate: P') unspecified P wave bottoming in the core
	 */
	PKP("PSLOWNESS", "BOTTOM, CMB", "(alternate: P') unspecified P wave bottoming in the core"),

	/**
	 * (old: PKP2) P wave bottoming in the upper outer core; ab indicates the
	 * retrograde branch of the PKP caustic
	 */
	PKPab("PSLOWNESS", "BOTTOM, CMB",
			"(old: PKP2) P wave bottoming in the upper outer core; ab indicates"
					+ "<BR>the retrograde branch of the PKP caustic"),

	/**
	 * (old: PKP1) P wave bottoming in the lower outer core; bc indicates the
	 * prograde branch of the PKP caustic
	 */
	PKPbc("PSLOWNESS", "BOTTOM, CMB",
			"(old: PKP1) P wave bottoming in the lower outer core; bc indicates"
					+ "<BR>the prograde branch of the PKP caustic"),

	/**
	 * (alternate: PKPdf) P wave bottoming in the inner core
	 */
	PKIKP("PSLOWNESS", "BOTTOM, ICB", "(alternate: PKPdf) P wave bottoming in the inner core", "PKPdf"),

	/**
	 * (alternate: PKIKP) P wave bottoming in the inner core
	 */
	PKPdf("PSLOWNESS", "BOTTOM, ICB", "(alternate: PKIKP) P wave bottoming in the inner core"),

	/**
	 * (old: PKhKP) a precursor to PKPdf due to scattering near or at the CMB
	 */
	PKPpre(GeoAttributes.PSLOWNESS, "(old: PKhKP) a precursor to PKPdf due to scattering near or at the CMB"),

	/**
	 * P wave diffracted at the inner core boundary (ICB) in the outer core
	 */
	PKPdif(GeoAttributes.PSLOWNESS, "P wave diffracted at the inner core boundary (ICB) in the outer core"),

	/**
	 * Unspecified P wave bottoming in the core and converting to S at the CMB
	 */
	PKS("PSLOWNESS, CMB, PSLOWNESS, CMB, SSLOWNESS", "BOTTOM, ICB",
			"(Treated as PKSdf) Unspecified P wave bottoming in the core and converting to S at the CMB"),

	/**
	 * PKS bottoming in the upper outer core
	 */
	PKSab("PSLOWNESS, CMB, PSLOWNESS, CMB, SSLOWNESS", "BOTTOM, CMB", "PKS bottoming in the upper outer core"),

	/**
	 * PKS bottoming in the lower outer core
	 */
	PKSbc("PSLOWNESS, CMB, PSLOWNESS, CMB, SSLOWNESS", "BOTTOM, CMB", "PKS bottoming in the lower outer core"),

	/**
	 * PKS bottoming in the inner core
	 */
	PKSdf("PSLOWNESS, CMB, PSLOWNESS, CMB, SSLOWNESS", "BOTTOM, ICB", "PKS bottoming in the inner core"),

	/**
	 * (alternates: PKPPKP, PKP2, P'2) Free surface reflection of PKP
	 */
	P_prime_P_prime_(GeoAttributes.PSLOWNESS, "(alternates: PKPPKP, PKP2, P'2) Free surface reflection of PKP"),

	/**
	 * (alternate: PKP3) PKP reflected at the free surface 2 times. P'3 is P'P'P'
	 */
	P_prime_3(GeoAttributes.PSLOWNESS, "(alternate: PKP3) PKP reflected at the free surface 2 times. P'3 is P'P'P'"),

	/**
	 * (alternate: PKP4) PKP reflected at the free surface 3 times.
	 */
	P_prime_4(GeoAttributes.PSLOWNESS, "(alternate: PKP4) PKP reflected at the free surface 3 times."),

	/**
	 * (alternate: PKP5) PKP reflected at the free surface 4 times.
	 */
	P_prime_5(GeoAttributes.PSLOWNESS, "(alternate: PKP5) PKP reflected at the free surface 4 times."),

	/**
	 * (alternate: PKP6) PKP reflected at the free surface 5 times.
	 */
	P_prime_6(GeoAttributes.PSLOWNESS, "(alternate: PKP6) PKP reflected at the free surface 5 times."),

	/**
	 * (alternate: PKP7) PKP reflected at the free surface 6 times.
	 */
	P_prime_7(GeoAttributes.PSLOWNESS, "(alternate: PKP7) PKP reflected at the free surface 6 times."),

	/**
	 * (alternates: P'P', PKP2, P'2) Free surface reflection of PKP
	 */
	PKPPKP(GeoAttributes.PSLOWNESS, "(alternates: P'P', PKP2, P'2) Free surface reflection of PKP"),

	/**
	 * (alternate: P'3) PKP reflected at the free surface 2 times. PKP3 is PKPPKPPKP
	 */
	PKP3(GeoAttributes.PSLOWNESS, "(alternate: P'3) PKP reflected at the free surface 2 times. PKP3 is PKPPKPPKP"),

	/**
	 * (alternate: P'4) PKP reflected at the free surface 3 times.
	 */
	PKP4(GeoAttributes.PSLOWNESS, "(alternate: P'4) PKP reflected at the free surface 3 times."),

	/**
	 * (alternate: P'5) PKP reflected at the free surface 4 times.
	 */
	PKP5(GeoAttributes.PSLOWNESS, "(alternate: P'5) PKP reflected at the free surface 4 times."),

	/**
	 * (alternate: P'6) PKP reflected at the free surface 5 times.
	 */
	PKP6(GeoAttributes.PSLOWNESS, "(alternate: P'6) PKP reflected at the free surface 5 times."),

	/**
	 * (alternate: P'7) PKP reflected at the free surface 6 times.
	 */
	PKP7(GeoAttributes.PSLOWNESS, "(alternate: P'7) PKP reflected at the free surface 6 times."),

	/**
	 * PKP reflected from inner side of a discontinuity at depth 660 outside the
	 * core, which means it is precursory to P'P'
	 */
	P_prime_660_minus_P_prime_(null,
			"PKP reflected from inner side of a discontinuity at depth 660"
					+ "<BR>outside the core, which means it is precursory to P'P'"),

	/**
	 * PKP reflected from inner side of a discontinuity at depth 410 outside the
	 * core, which means it is precursory to P'P'
	 */
	P_prime_410_minus_P_prime_(null,
			"PKP reflected from inner side of a discontinuity at depth 410"
					+ "<BR>outside the core, which means it is precursory to P'P'"),

	/**
	 * PKP reflected from inner side of a discontinuity at depth 210 outside the
	 * core, which means it is precursory to P'P'
	 */
	P_prime_210_minus_P_prime_(null,
			"PKP reflected from inner side of a discontinuity at depth 210"
					+ "<BR>outside the core, which means it is precursory to P'P'"),

	/**
	 * (alternate: PKPSKS) PKP to SKS converted reflection at the free surface
	 */
	P_prime_S_prime_(null, "(alternate: PKPSKS) PKP to SKS converted reflection at the free surface"),

	/**
	 * (alternate: P'S') PKP to SKS converted reflection at the free surface
	 */
	PKPSKS(null, "(alternate: P'S') PKP to SKS converted reflection at the free surface"),

	/**
	 * (alternate: PKPPKS) PKP to PKS reflection at the free surface
	 */
	P_prime_PKS(null, "(alternate: PKPPKS) PKP to PKS reflection at the free surface"),

	/**
	 * (alternate: P'PKS) PKP to PKS reflection at the free surface
	 */
	PKPPKS(null, "(alternate: P'PKS) PKP to PKS reflection at the free surface"),

	/**
	 * (alternate: PKPSKP) PKP to SKP converted reflection at the free surface
	 */
	P_prime_SKP(null, "(alternate: PKPSKP) PKP to SKP converted reflection at the free surface"),

	/**
	 * (alternate: P'SKP) PKP to SKP converted reflection at the free surface
	 */
	PKPSKP(null, "(alternate: P'SKP) PKP to SKP converted reflection at the free surface"),

	/**
	 * (alternate: PS') P (leaving a source downwards) to SKS reflection at the free
	 * surface
	 */
	PSKS(null, "(alternate: PS') P (leaving a source downwards) to SKS reflection at the" + "<BR>free surface"),

	/**
	 * (alternate: PSKS) P (leaving a source downwards) to SKS reflection at the
	 * free surface
	 */
	PS_prime_(null, "(alternate: PSKS) P (leaving a source downwards) to SKS reflection at the" + "<BR>free surface"),

	/**
	 * Unspecified P wave reflected once from the inner side of the CMB
	 */
	PKKP(GeoAttributes.PSLOWNESS, "Unspecified P wave reflected once from the inner side of the CMB"),

	/**
	 * PKKP bottoming in the upper outer core
	 */
	PKKPab(GeoAttributes.PSLOWNESS, "PKKP bottoming in the upper outer core"),

	/**
	 * PKKP bottoming in the lower outer core
	 */
	PKKPbc("PSLOWNESS", "BOTTOM, CMB, BOTTOM_SIDE_REFLECTION, CMB, BOTTOM, CMB",
			"PKKP bottoming in the lower outer core"),

	/**
	 * PKKP bottoming in the inner core
	 */
	PKKPdf(GeoAttributes.PSLOWNESS, "PKKP bottoming in the inner core"),

	/**
	 * a precursor to PKKP due to scattering near the CMB
	 */
	PKKPpre(GeoAttributes.PSLOWNESS, "a precursor to PKKP due to scattering near the CMB"),

	/**
	 * P wave reflected from the inner core boundary (ICB)
	 */
	PKiKP(GeoAttributes.PSLOWNESS, "P wave reflected from the inner core boundary (ICB)"),

	/**
	 * P wave traversing the outer core as P and the inner core as S
	 */
	PKJKP(GeoAttributes.PSLOWNESS, "P wave traversing the outer core as P and the inner core as S"),

	/**
	 * (alternate: P2KS) P wave reflected once from inner side of the CMB and
	 * converted to S at the CMB
	 */
	PKKS(null,
			"(alternate: P2KS) P wave reflected once from inner side of the CMB and" + "<BR>converted to S at the CMB"),

	/**
	 * PKKS bottoming in the upper outer core
	 */
	PKKSab(null, "PKKS bottoming in the upper outer core"),

	/**
	 * PKKS bottoming in the lower outer core
	 */
	PKKSbc(null, "PKKS bottoming in the lower outer core"),

	/**
	 * PKKS bottoming in the inner core
	 */
	PKKSdf(null, "PKKS bottoming in the inner core"),

	/**
	 * (alternate: PcPP') PcP to PKP reflection at the free surface
	 */
	PcPPKP(GeoAttributes.PSLOWNESS, "(alternate: PcPP') PcP to PKP reflection at the free surface"),

	/**
	 * (alternate: PcPPKP) PcP to PKP reflection at the free surface
	 */
	PcPP_prime_(GeoAttributes.PSLOWNESS, "(alternate: PcPPKP) PcP to PKP reflection at the free surface"),

	/**
	 * (alternate: PcSP') PcS to PKP converted reflection at the free surface
	 */
	PcSPKP(null, "(alternate: PcSP') PcS to PKP converted reflection at the free surface"),

	/**
	 * (alternate: PcSPKP) PcS to PKP converted reflection at the free surface
	 */
	PcSP_prime_(null, "(alternate: PcSPKP) PcS to PKP converted reflection at the free surface"),

	/**
	 * (alternate: PcSSKS) PcS to SKS reflection at the free surface
	 */
	PcSS_prime_(null, "(alternate: PcSSKS) PcS to SKS reflection at the free surface"),

	/**
	 * (alternate: PcSS') PcS to SKS reflection at the free surface
	 */
	PcSSKS(null, "(alternate: PcSS') PcS to SKS reflection at the free surface"),

	/**
	 * PcP to SKP converted reflection at the free surface
	 */
	PcPSKP(null, "PcP to SKP converted reflection at the free surface"),

	/**
	 * PcS to SKP reflection at the free surface
	 */
	PcSSKP(null, "PcS to SKP reflection at the free surface"),

	/**
	 * P wave reflected 1 time from inner side of the CMB
	 */
	P2KP(GeoAttributes.PSLOWNESS, "P wave reflected 1 time from inner side of the CMB"),

	/**
	 * P wave reflected 1 time from the inner side of the ICB
	 */
	PK2IKP(GeoAttributes.PSLOWNESS, "P wave reflected 1 time from the inner side of the ICB"),

	/**
	 * P wave reflected 2 times from inner side of the CMB
	 */
	P3KP(GeoAttributes.PSLOWNESS, "P wave reflected 2 times from inner side of the CMB"),

	/**
	 * P wave reflected 2 times from the inner side of the ICB
	 */
	PK3IKP(GeoAttributes.PSLOWNESS, "P wave reflected 2 times from the inner side of the ICB"),

	/**
	 * P wave reflected 3 times from inner side of the CMB
	 */
	P4KP(GeoAttributes.PSLOWNESS, "P wave reflected 3 times from inner side of the CMB"),

	/**
	 * P wave reflected 3 times from the inner side of the ICB
	 */
	PK4IKP(GeoAttributes.PSLOWNESS, "P wave reflected 3 times from the inner side of the ICB"),

	/**
	 * P wave reflected 4 times from inner side of the CMB
	 */
	P5KP(GeoAttributes.PSLOWNESS, "P wave reflected 4 times from inner side of the CMB"),

	/**
	 * P wave reflected 4 times from the inner side of the ICB
	 */
	PK5IKP(GeoAttributes.PSLOWNESS, "P wave reflected 4 times from the inner side of the ICB"),

	/**
	 * P wave reflected 5 times from inner side of the CMB
	 */
	P6KP(GeoAttributes.PSLOWNESS, "P wave reflected 5 times from inner side of the CMB"),

	/**
	 * P wave reflected 5 times from the inner side of the ICB
	 */
	PK6IKP(GeoAttributes.PSLOWNESS, "P wave reflected 5 times from the inner side of the ICB"),

	/**
	 * P wave reflected 6 times from inner side of the CMB
	 */
	P7KP(GeoAttributes.PSLOWNESS, "P wave reflected 6 times from inner side of the CMB"),

	/**
	 * P wave reflected 6 times from the inner side of the ICB
	 */
	PK7IKP(GeoAttributes.PSLOWNESS, "P wave reflected 6 times from the inner side of the ICB"),

	/**
	 * (alternate: SKS) unspecified S wave traversing the core as P
	 */
	S_prime_(null, "(alternate: SKS) unspecified S wave traversing the core as P"),

	/**
	 * (alternate: S') unspecified S wave traversing the core as P
	 */
	SKS("SSLOWNESS, CMB, PSLOWNESS, CMB, SSLOWNESS", "BOTTOM, ICB",
			"(treated as SKSdf) unspecified S wave traversing the core as P"),

	/**
	 * SKS bottoming in the outer core
	 */
	SKSac("SSLOWNESS, CMB, PSLOWNESS, CMB, SSLOWNESS", "BOTTOM, CMB", "SKS bottoming in the outer core"),

	/**
	 * SKS bottoming in the outer core
	 */
	SKSbc("SSLOWNESS, CMB, PSLOWNESS, CMB, SSLOWNESS", "BOTTOM, CMB", "SKS bottoming in the outer core"),

	/**
	 * (alternate: SKSdf) SKS bottoming in the inner core
	 */
	SKIKS("SSLOWNESS, CMB, PSLOWNESS, CMB, SSLOWNESS", "BOTTOM, ICB",
			"(alternate: SKSdf) SKS bottoming in the inner core"),

	/**
	 * (alternate: SKIKS) SKS bottoming in the inner core
	 */
	SKSdf("SSLOWNESS, CMB, PSLOWNESS, CMB, SSLOWNESS", "BOTTOM, ICB",
			"(alternate: SKIKS) SKS bottoming in the inner core"),

	/**
	 * Unspecified S wave traversing the core and then the mantle as P
	 */
	SKP("SSLOWNESS, CMB, PSLOWNESS", "BOTTOM, ICB",
			"(treated as SKSdf) Unspecified S wave traversing the core and then the mantle as P"),

	/**
	 * SKP bottoming in the upper outer core
	 */
	SKPab("SSLOWNESS, CMB, PSLOWNESS", "BOTTOM, CMB", "SKP bottoming in the upper outer core"),

	/**
	 * SKP bottoming in the lower outer core
	 */
	SKPbc("SSLOWNESS, CMB, PSLOWNESS", "BOTTOM, CMB", "SKP bottoming in the lower outer core"),

	/**
	 * SKP bottoming in the inner core
	 */
	SKPdf("SSLOWNESS, CMB, PSLOWNESS", "BOTTOM, ICB", "SKP bottoming in the inner core"),

	/**
	 * (alternates: S'S', S'2, SKS2) Free surface reflection of SKS
	 */
	SKSSKS(GeoAttributes.SSLOWNESS, "(alternates: S'S', S'2, SKS2) Free surface reflection of SKS"),

	/**
	 * (alternates: SKSSKS, S'2, SKS2) Free surface reflection of SKS
	 */
	S_prime_S_prime_(null, "(alternates: SKSSKS, S'2, SKS2) Free surface reflection of SKS"),

	/**
	 * (alternate: SKS3) SKS reflected at the free surface 2 times. S'3 is S'S'S'
	 */
	S_prime_3(null, "(alternate: SKS3) SKS reflected at the free surface 2 times. S'3 is S'S'S'"),

	/**
	 * (alternate: SKS4) SKS reflected at the free surface 3 times.
	 */
	S_prime_4(null, "(alternate: SKS4) SKS reflected at the free surface 3 times."),

	/**
	 * (alternate: SKS5) SKS reflected at the free surface 4 times.
	 */
	S_prime_5(null, "(alternate: SKS5) SKS reflected at the free surface 4 times."),

	/**
	 * (alternate: SKS6) SKS reflected at the free surface 5 times.
	 */
	S_prime_6(null, "(alternate: SKS6) SKS reflected at the free surface 5 times."),

	/**
	 * (alternate: SKS7) SKS reflected at the free surface 6 times.
	 */
	S_prime_7(null, "(alternate: SKS7) SKS reflected at the free surface 6 times."),

	/**
	 * (alternate: S'3) SKS reflected at the free surface 2 times. SKS3 is SKSSKSSKS
	 */
	SKS3(GeoAttributes.SSLOWNESS, "(alternate: S'3) SKS reflected at the free surface 2 times. SKS3 is SKSSKSSKS"),

	/**
	 * (alternate: S'4) SKS reflected at the free surface 3 times.
	 */
	SKS4(GeoAttributes.SSLOWNESS, "(alternate: S'4) SKS reflected at the free surface 3 times."),

	/**
	 * (alternate: S'5) SKS reflected at the free surface 4 times.
	 */
	SKS5(GeoAttributes.SSLOWNESS, "(alternate: S'5) SKS reflected at the free surface 4 times."),

	/**
	 * (alternate: S'6) SKS reflected at the free surface 5 times.
	 */
	SKS6(GeoAttributes.SSLOWNESS, "(alternate: S'6) SKS reflected at the free surface 5 times."),

	/**
	 * (alternate: S'7) SKS reflected at the free surface 6 times.
	 */
	SKS7(GeoAttributes.SSLOWNESS, "(alternate: S'7) SKS reflected at the free surface 6 times."),

	/**
	 * SKS reflected from inner side of a discontinuity at depth 660 outside the
	 * core, which means it is precursory to S'S'
	 */
	S_prime_660_minus_S_prime_(null,
			"SKS reflected from inner side of a discontinuity at depth 660"
					+ "<BR>outside the core, which means it is precursory to S'S'"),

	/**
	 * SKS reflected from inner side of a discontinuity at depth 410 outside the
	 * core, which means it is precursory to S'S'
	 */
	S_prime_410_minus_S_prime_(null,
			"SKS reflected from inner side of a discontinuity at depth 410"
					+ "<BR>outside the core, which means it is precursory to S'S'"),

	/**
	 * SKS reflected from inner side of a discontinuity at depth 210 outside the
	 * core, which means it is precursory to S'S'
	 */
	S_prime_210_minus_S_prime_(null,
			"SKS reflected from inner side of a discontinuity at depth 210"
					+ "<BR>outside the core, which means it is precursory to S'S'"),

	/**
	 * (alternate: S'P) SKS to P reflection at the free surface
	 */
	SKSP(null, "(alternate: S'P) SKS to P reflection at the free surface"),

	/**
	 * (alternate: SKSP) SKS to P reflection at the free surface
	 */
	S_prime_P(null, "(alternate: SKSP) SKS to P reflection at the free surface"),

	/**
	 * Unspecified S wave reflected once from inner side of the CMB
	 */
	SKKS(GeoAttributes.SSLOWNESS, "Unspecified S wave reflected once from inner side of the CMB"),

	/**
	 * SKKS bottoming in the outer core
	 */
	SKKSac(GeoAttributes.SSLOWNESS, "SKKS bottoming in the outer core"),

	/**
	 * SKKS bottoming in the inner core
	 */
	SKKSdf(GeoAttributes.SSLOWNESS, "SKKS bottoming in the inner core"),

	/**
	 * S wave traversing the outer core as P and reflected from the ICB
	 */
	SKiKS(GeoAttributes.SSLOWNESS, "S wave traversing the outer core as P and reflected from the ICB"),

	/**
	 * S wave traversing the outer core as P and the inner core as S
	 */
	SKJKS(GeoAttributes.SSLOWNESS, "S wave traversing the outer core as P and the inner core as S"),

	/**
	 * (alternate: S2KP) S wave traversing the core as P with one reflection from
	 * the inner side of the CMB and then continuing as P in the mantle
	 */
	SKKP(null,
			"(alternate: S2KP) S wave traversing the core as P with one reflection from the"
					+ "<BR>inner side of the CMB and then continuing as P in the mantle"),

	/**
	 * SKKP bottoming in the upper outer core
	 */
	SKKPab(null, "SKKP bottoming in the upper outer core"),

	/**
	 * SKKP bottoming in the lower outer core
	 */
	SKKPbc(null, "SKKP bottoming in the lower outer core"),

	/**
	 * SKKP bottoming in the inner core
	 */
	SKKPdf(null, "SKKP bottoming in the inner core"),

	/**
	 * S wave reflected 1 time from inner side of the CMB
	 */
	S2KS(GeoAttributes.SSLOWNESS, "S wave reflected 1 time from inner side of the CMB"),

	/**
	 * S wave reflected 2 times from inner side of the CMB
	 */
	S3KS(GeoAttributes.SSLOWNESS, "S wave reflected 2 times from inner side of the CMB"),

	/**
	 * S wave reflected 3 times from inner side of the CMB
	 */
	S4KS(GeoAttributes.SSLOWNESS, "S wave reflected 3 times from inner side of the CMB"),

	/**
	 * S wave reflected 4 times from inner side of the CMB
	 */
	S5KS(GeoAttributes.SSLOWNESS, "S wave reflected 4 times from inner side of the CMB"),

	/**
	 * S wave reflected 5 times from inner side of the CMB
	 */
	S6KS(GeoAttributes.SSLOWNESS, "S wave reflected 5 times from inner side of the CMB"),

	/**
	 * S wave reflected 6 times from inner side of the CMB
	 */
	S7KS(GeoAttributes.SSLOWNESS, "S wave reflected 6 times from inner side of the CMB"),

	/**
	 * (alternate: S'P') SKS to PKP converted reflection at the free surface
	 */
	SKSPKP(null, "(alternate: S'P') SKS to PKP converted reflection at the free surface"),

	/**
	 * (alternate: SKSPKP) SKS to PKP converted reflection at the free surface
	 */
	S_prime_P_prime_(null, "(alternate: SKSPKP) SKS to PKP converted reflection at the free surface"),

	/**
	 * (alternate: S'SKP) SKS to SKP reflection at the free surface
	 */
	SKSSKP(null, "(alternate: S'SKP) SKS to SKP reflection at the free surface"),

	/**
	 * (alternate: SKSSKP) SKS to SKP reflection at the free surface
	 */
	S_prime_SKP(null, "(alternate: SKSSKP) SKS to SKP reflection at the free surface"),

	/**
	 * (alternate: S'PKS) SKS to PKS converted reflection at the free surface
	 */
	SKSPKS(null, "(alternate: S'PKS) SKS to PKS converted reflection at the free surface"),

	/**
	 * (alternate: SKSPKS) SKS to PKS converted reflection at the free surface
	 */
	S_prime_PKS(null, "(alternate: SKSPKS) SKS to PKS converted reflection at the free surface"),

	/**
	 * (alternate: ScSS') ScS to SKS reflection at the free surface
	 */
	ScSSKS(GeoAttributes.SSLOWNESS, "(alternate: ScSS') ScS to SKS reflection at the free surface"),

	/**
	 * (alternate: ScSSKS) ScS to SKS reflection at the free surface
	 */
	ScSS_prime_(null, "(alternate: ScSSKS) ScS to SKS reflection at the free surface"),

	/**
	 * (alternate: ScSPKP) ScS to PKP converted reflection at the free surface
	 */
	ScSP_prime_(null, "(alternate: ScSPKP) ScS to PKP converted reflection at the free surface"),

	/**
	 * (alternate: ScSP') ScS to PKP converted reflection at the free surface
	 */
	ScSPKP(null, "(alternate: ScSP') ScS to PKP converted reflection at the free surface"),

	/**
	 * (alternate: ScPPKP) ScP to PKP reflection at the free surface
	 */
	ScPP_prime_(null, "(alternate: ScPPKP) ScP to PKP reflection at the free surface"),

	/**
	 * (alternate: ScPP') ScP to PKP reflection at the free surface
	 */
	ScPPKP(null, "(alternate: ScPP') ScP to PKP reflection at the free surface"),

	/**
	 * ScS to SKP reflection at the free surface
	 */
	ScSSKP(null, "ScS to SKP reflection at the free surface"),

	/**
	 * ScP to SKP converted reflection at the free surface
	 */
	ScPSKP(null, "ScP to SKP converted reflection at the free surface"),

	/**
	 * P resulting from reflection of upgoing P at the free surface
	 */
	pP("PSLOWNESS", "BOTTOM_SIDE_REFLECTION, FREE_SURFACE, BOTTOM, M660",
			"P resulting from reflection of upgoing P at the free surface", "littlep_bigP"),

	/**
	 * P'3 resulting from reflection of upgoing P at the free surface
	 */
	pP_prime_3(GeoAttributes.PSLOWNESS, "P'3 resulting from reflection of upgoing P at the free surface"),

	/**
	 * P'4 resulting from reflection of upgoing P at the free surface
	 */
	pP_prime_4(GeoAttributes.PSLOWNESS, "P'4 resulting from reflection of upgoing P at the free surface"),

	/**
	 * P'5 resulting from reflection of upgoing P at the free surface
	 */
	pP_prime_5(GeoAttributes.PSLOWNESS, "P'5 resulting from reflection of upgoing P at the free surface"),

	/**
	 * P'6 resulting from reflection of upgoing P at the free surface
	 */
	pP_prime_6(GeoAttributes.PSLOWNESS, "P'6 resulting from reflection of upgoing P at the free surface"),

	/**
	 * P'7 resulting from reflection of upgoing P at the free surface
	 */
	pP_prime_7(GeoAttributes.PSLOWNESS, "P'7 resulting from reflection of upgoing P at the free surface"),

	/**
	 * P'P' resulting from reflection of upgoing P at the free surface
	 */
	pP_prime_P_prime_(GeoAttributes.PSLOWNESS, "P'P' resulting from reflection of upgoing P at the free surface"),

	/**
	 * P'PKS resulting from reflection of upgoing P at the free surface
	 */
	pP_prime_PKS(null, "P'PKS resulting from reflection of upgoing P at the free surface"),

	/**
	 * P'S' resulting from reflection of upgoing P at the free surface
	 */
	pP_prime_S_prime_(null, "P'S' resulting from reflection of upgoing P at the free surface"),

	/**
	 * P'SKP resulting from reflection of upgoing P at the free surface
	 */
	pP_prime_SKP(null, "P'SKP resulting from reflection of upgoing P at the free surface"),

	/**
	 * P* resulting from reflection of upgoing P at the free surface
	 */
	pP_star_(null, "P* resulting from reflection of upgoing P at the free surface"),

	/**
	 * P210+P resulting from reflection of upgoing P at the free surface
	 */
	pP210_plus_P(null, "P210+P resulting from reflection of upgoing P at the free surface"),

	/**
	 * P210+S resulting from reflection of upgoing P at the free surface
	 */
	pP210_plus_S(null, "P210+S resulting from reflection of upgoing P at the free surface"),

	/**
	 * P210-P resulting from reflection of upgoing P at the free surface
	 */
	pP210_minus_P(null, "P210-P resulting from reflection of upgoing P at the free surface"),

	/**
	 * P210-S resulting from reflection of upgoing P at the free surface
	 */
	pP210_minus_S(null, "P210-S resulting from reflection of upgoing P at the free surface"),

	/**
	 * P2KP resulting from reflection of upgoing P at the free surface
	 */
	pP2KP(GeoAttributes.PSLOWNESS, "P2KP resulting from reflection of upgoing P at the free surface"),

	/**
	 * P3KP resulting from reflection of upgoing P at the free surface
	 */
	pP3KP(GeoAttributes.PSLOWNESS, "P3KP resulting from reflection of upgoing P at the free surface"),

	/**
	 * P410+P resulting from reflection of upgoing P at the free surface
	 */
	pP410_plus_P(null, "P410+P resulting from reflection of upgoing P at the free surface"),

	/**
	 * P410+S resulting from reflection of upgoing P at the free surface
	 */
	pP410_plus_S(null, "P410+S resulting from reflection of upgoing P at the free surface"),

	/**
	 * P410-P resulting from reflection of upgoing P at the free surface
	 */
	pP410_minus_P(null, "P410-P resulting from reflection of upgoing P at the free surface"),

	/**
	 * P410-S resulting from reflection of upgoing P at the free surface
	 */
	pP410_minus_S(null, "P410-S resulting from reflection of upgoing P at the free surface"),

	/**
	 * P4KP resulting from reflection of upgoing P at the free surface
	 */
	pP4KP(GeoAttributes.PSLOWNESS, "P4KP resulting from reflection of upgoing P at the free surface"),

	/**
	 * P5KP resulting from reflection of upgoing P at the free surface
	 */
	pP5KP(GeoAttributes.PSLOWNESS, "P5KP resulting from reflection of upgoing P at the free surface"),

	/**
	 * P660+P resulting from reflection of upgoing P at the free surface
	 */
	pP660_plus_P(null, "P660+P resulting from reflection of upgoing P at the free surface"),

	/**
	 * P660+S resulting from reflection of upgoing P at the free surface
	 */
	pP660_plus_S(null, "P660+S resulting from reflection of upgoing P at the free surface"),

	/**
	 * P660-P resulting from reflection of upgoing P at the free surface
	 */
	pP660_minus_P(null, "P660-P resulting from reflection of upgoing P at the free surface"),

	/**
	 * P660-S resulting from reflection of upgoing P at the free surface
	 */
	pP660_minus_S(null, "P660-S resulting from reflection of upgoing P at the free surface"),

	/**
	 * P6KP resulting from reflection of upgoing P at the free surface
	 */
	pP6KP(GeoAttributes.PSLOWNESS, "P6KP resulting from reflection of upgoing P at the free surface"),

	/**
	 * P7KP resulting from reflection of upgoing P at the free surface
	 */
	pP7KP(GeoAttributes.PSLOWNESS, "P7KP resulting from reflection of upgoing P at the free surface"),

	/**
	 * PK2IKP resulting from reflection of upgoing P at the free surface
	 */
	pPK2IKP(GeoAttributes.PSLOWNESS, "PK2IKP resulting from reflection of upgoing P at the free surface"),

	/**
	 * PK3IKP resulting from reflection of upgoing P at the free surface
	 */
	pPK3IKP(GeoAttributes.PSLOWNESS, "PK3IKP resulting from reflection of upgoing P at the free surface"),

	/**
	 * PK4IKP resulting from reflection of upgoing P at the free surface
	 */
	pPK4IKP(GeoAttributes.PSLOWNESS, "PK4IKP resulting from reflection of upgoing P at the free surface"),

	/**
	 * PK5IKP resulting from reflection of upgoing P at the free surface
	 */
	pPK5IKP(GeoAttributes.PSLOWNESS, "PK5IKP resulting from reflection of upgoing P at the free surface"),

	/**
	 * PK6IKP resulting from reflection of upgoing P at the free surface
	 */
	pPK6IKP(GeoAttributes.PSLOWNESS, "PK6IKP resulting from reflection of upgoing P at the free surface"),

	/**
	 * PK7IKP resulting from reflection of upgoing P at the free surface
	 */
	pPK7IKP(GeoAttributes.PSLOWNESS, "PK7IKP resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKIKP resulting from reflection of upgoing P at the free surface
	 */
	pPKIKP(GeoAttributes.PSLOWNESS, "PKIKP resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKJKP resulting from reflection of upgoing P at the free surface
	 */
	pPKJKP(GeoAttributes.PSLOWNESS, "PKJKP resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKKP resulting from reflection of upgoing P at the free surface
	 */
	pPKKP(GeoAttributes.PSLOWNESS, "PKKP resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKKPab resulting from reflection of upgoing P at the free surface
	 */
	pPKKPab(GeoAttributes.PSLOWNESS, "PKKPab resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKKPbc resulting from reflection of upgoing P at the free surface
	 */
	pPKKPbc(GeoAttributes.PSLOWNESS, "PKKPbc resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKKPdf resulting from reflection of upgoing P at the free surface
	 */
	pPKKPdf(GeoAttributes.PSLOWNESS, "PKKPdf resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKKPpre resulting from reflection of upgoing P at the free surface
	 */
	pPKKPpre(GeoAttributes.PSLOWNESS, "PKKPpre resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKKS resulting from reflection of upgoing P at the free surface
	 */
	pPKKS(null, "PKKS resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKKSab resulting from reflection of upgoing P at the free surface
	 */
	pPKKSab(null, "PKKSab resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKKSbc resulting from reflection of upgoing P at the free surface
	 */
	pPKKSbc(null, "PKKSbc resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKKSdf resulting from reflection of upgoing P at the free surface
	 */
	pPKKSdf(null, "PKKSdf resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKP resulting from reflection of upgoing P at the free surface
	 */
	pPKP(GeoAttributes.PSLOWNESS, "PKP resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKP3 resulting from reflection of upgoing P at the free surface
	 */
	pPKP3(GeoAttributes.PSLOWNESS, "PKP3 resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKP4 resulting from reflection of upgoing P at the free surface
	 */
	pPKP4(GeoAttributes.PSLOWNESS, "PKP4 resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKP5 resulting from reflection of upgoing P at the free surface
	 */
	pPKP5(GeoAttributes.PSLOWNESS, "PKP5 resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKP6 resulting from reflection of upgoing P at the free surface
	 */
	pPKP6(GeoAttributes.PSLOWNESS, "PKP6 resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKP7 resulting from reflection of upgoing P at the free surface
	 */
	pPKP7(GeoAttributes.PSLOWNESS, "PKP7 resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKPPKP resulting from reflection of upgoing P at the free surface
	 */
	pPKPPKP(GeoAttributes.PSLOWNESS, "PKPPKP resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKPPKS resulting from reflection of upgoing P at the free surface
	 */
	pPKPPKS(null, "PKPPKS resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKPSKP resulting from reflection of upgoing P at the free surface
	 */
	pPKPSKP(null, "PKPSKP resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKPSKS resulting from reflection of upgoing P at the free surface
	 */
	pPKPSKS(null, "PKPSKS resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKPab resulting from reflection of upgoing P at the free surface
	 */
	pPKPab(GeoAttributes.PSLOWNESS, "PKPab resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKPbc resulting from reflection of upgoing P at the free surface
	 */
	pPKPbc(GeoAttributes.PSLOWNESS, "PKPbc resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKPdf resulting from reflection of upgoing P at the free surface
	 */
	pPKPdf(GeoAttributes.PSLOWNESS, "PKPdf resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKPdif resulting from reflection of upgoing P at the free surface
	 */
	pPKPdif(GeoAttributes.PSLOWNESS, "PKPdif resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKPpre resulting from reflection of upgoing P at the free surface
	 */
	pPKPpre(GeoAttributes.PSLOWNESS, "PKPpre resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKS resulting from reflection of upgoing P at the free surface
	 */
	pPKS(null, "PKS resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKSab resulting from reflection of upgoing P at the free surface
	 */
	pPKSab(null, "PKSab resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKSbc resulting from reflection of upgoing P at the free surface
	 */
	pPKSbc(null, "PKSbc resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKSdf resulting from reflection of upgoing P at the free surface
	 */
	pPKSdf(null, "PKSdf resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKiKP resulting from reflection of upgoing P at the free surface
	 */
	pPKiKP(GeoAttributes.PSLOWNESS, "PKiKP resulting from reflection of upgoing P at the free surface"),

	/**
	 * PP resulting from reflection of upgoing P at the free surface
	 */
	pPP(GeoAttributes.PSLOWNESS, "PP resulting from reflection of upgoing P at the free surface"),

	/**
	 * PPP resulting from reflection of upgoing P at the free surface
	 */
	pPPP(GeoAttributes.PSLOWNESS, "PPP resulting from reflection of upgoing P at the free surface"),

	/**
	 * PPS resulting from reflection of upgoing P at the free surface
	 */
	pPPS(null, "PPS resulting from reflection of upgoing P at the free surface"),

	/**
	 * PS resulting from reflection of upgoing P at the free surface
	 */
	pPS(null, "PS resulting from reflection of upgoing P at the free surface"),

	/**
	 * PS' resulting from reflection of upgoing P at the free surface
	 */
	pPS_prime_(null, "PS' resulting from reflection of upgoing P at the free surface"),

	/**
	 * PSKS resulting from reflection of upgoing P at the free surface
	 */
	pPSKS(null, "PSKS resulting from reflection of upgoing P at the free surface"),

	/**
	 * PSS resulting from reflection of upgoing P at the free surface
	 */
	pPSS(null, "PSS resulting from reflection of upgoing P at the free surface"),

	/**
	 * PScS resulting from reflection of upgoing P at the free surface
	 */
	pPScS(null, "PScS resulting from reflection of upgoing P at the free surface"),

	/**
	 * Pb resulting from reflection of upgoing P at the free surface
	 */
	pPb(GeoAttributes.PSLOWNESS, "Pb resulting from reflection of upgoing P at the free surface"),

	/**
	 * PcP resulting from reflection of upgoing P at the free surface
	 */
	pPcP(GeoAttributes.PSLOWNESS, "PcP resulting from reflection of upgoing P at the free surface"),

	/**
	 * PcP2 resulting from reflection of upgoing P at the free surface
	 */
	pPcP2(GeoAttributes.PSLOWNESS, "PcP2 resulting from reflection of upgoing P at the free surface"),

	/**
	 * PcP3 resulting from reflection of upgoing P at the free surface
	 */
	pPcP3(GeoAttributes.PSLOWNESS, "PcP3 resulting from reflection of upgoing P at the free surface"),

	/**
	 * PcP4 resulting from reflection of upgoing P at the free surface
	 */
	pPcP4(GeoAttributes.PSLOWNESS, "PcP4 resulting from reflection of upgoing P at the free surface"),

	/**
	 * PcP5 resulting from reflection of upgoing P at the free surface
	 */
	pPcP5(GeoAttributes.PSLOWNESS, "PcP5 resulting from reflection of upgoing P at the free surface"),

	/**
	 * PcP6 resulting from reflection of upgoing P at the free surface
	 */
	pPcP6(GeoAttributes.PSLOWNESS, "PcP6 resulting from reflection of upgoing P at the free surface"),

	/**
	 * PcP7 resulting from reflection of upgoing P at the free surface
	 */
	pPcP7(GeoAttributes.PSLOWNESS, "PcP7 resulting from reflection of upgoing P at the free surface"),

	/**
	 * PcPP' resulting from reflection of upgoing P at the free surface
	 */
	pPcPP_prime_(GeoAttributes.PSLOWNESS, "PcPP' resulting from reflection of upgoing P at the free surface"),

	/**
	 * PcPPKP resulting from reflection of upgoing P at the free surface
	 */
	pPcPPKP(GeoAttributes.PSLOWNESS, "PcPPKP resulting from reflection of upgoing P at the free surface"),

	/**
	 * PcPS resulting from reflection of upgoing P at the free surface
	 */
	pPcPS(null, "PcPS resulting from reflection of upgoing P at the free surface"),

	/**
	 * PcPSKP resulting from reflection of upgoing P at the free surface
	 */
	pPcPSKP(null, "PcPSKP resulting from reflection of upgoing P at the free surface"),

	/**
	 * PcS resulting from reflection of upgoing P at the free surface
	 */
	pPcS(null, "PcS resulting from reflection of upgoing P at the free surface"),

	/**
	 * PcSP' resulting from reflection of upgoing P at the free surface
	 */
	pPcSP_prime_(null, "PcSP' resulting from reflection of upgoing P at the free surface"),

	/**
	 * PcSPKP resulting from reflection of upgoing P at the free surface
	 */
	pPcSPKP(null, "PcSPKP resulting from reflection of upgoing P at the free surface"),

	/**
	 * PcSS' resulting from reflection of upgoing P at the free surface
	 */
	pPcSS_prime_(null, "PcSS' resulting from reflection of upgoing P at the free surface"),

	/**
	 * PcSSKP resulting from reflection of upgoing P at the free surface
	 */
	pPcSSKP(null, "PcSSKP resulting from reflection of upgoing P at the free surface"),

	/**
	 * PcSSKS resulting from reflection of upgoing P at the free surface
	 */
	pPcSSKS(null, "PcSSKS resulting from reflection of upgoing P at the free surface"),

	/**
	 * Pdif resulting from reflection of upgoing P at the free surface
	 */
	pPdif(GeoAttributes.PSLOWNESS, "Pdif resulting from reflection of upgoing P at the free surface"),

	/**
	 * Pg resulting from reflection of upgoing P at the free surface
	 */
	pPg(GeoAttributes.PSLOWNESS, "Pg resulting from reflection of upgoing P at the free surface"),

	/**
	 * PgPg resulting from reflection of upgoing P at the free surface
	 */
	pPgPg(GeoAttributes.PSLOWNESS, "PgPg resulting from reflection of upgoing P at the free surface"),

	/**
	 * PmP resulting from reflection of upgoing P at the free surface
	 */
	pPmP(GeoAttributes.PSLOWNESS, "PmP resulting from reflection of upgoing P at the free surface"),

	/**
	 * PmP2 resulting from reflection of upgoing P at the free surface
	 */
	pPmP2(GeoAttributes.PSLOWNESS, "PmP2 resulting from reflection of upgoing P at the free surface"),

	/**
	 * PmP3 resulting from reflection of upgoing P at the free surface
	 */
	pPmP3(GeoAttributes.PSLOWNESS, "PmP3 resulting from reflection of upgoing P at the free surface"),

	/**
	 * PmP4 resulting from reflection of upgoing P at the free surface
	 */
	pPmP4(GeoAttributes.PSLOWNESS, "PmP4 resulting from reflection of upgoing P at the free surface"),

	/**
	 * PmP5 resulting from reflection of upgoing P at the free surface
	 */
	pPmP5(GeoAttributes.PSLOWNESS, "PmP5 resulting from reflection of upgoing P at the free surface"),

	/**
	 * PmP6 resulting from reflection of upgoing P at the free surface
	 */
	pPmP6(GeoAttributes.PSLOWNESS, "PmP6 resulting from reflection of upgoing P at the free surface"),

	/**
	 * PmP7 resulting from reflection of upgoing P at the free surface
	 */
	pPmP7(GeoAttributes.PSLOWNESS, "PmP7 resulting from reflection of upgoing P at the free surface"),

	/**
	 * PmS resulting from reflection of upgoing P at the free surface
	 */
	pPmS(null, "PmS resulting from reflection of upgoing P at the free surface"),

	/**
	 * Pn resulting from reflection of upgoing P at the free surface
	 */
	pPn(GeoAttributes.PSLOWNESS, "Pn resulting from reflection of upgoing P at the free surface"),

	/**
	 * PnPn resulting from reflection of upgoing P at the free surface
	 */
	pPnPn(GeoAttributes.PSLOWNESS, "PnPn resulting from reflection of upgoing P at the free surface"),

	/**
	 * S resulting from converted reflection of upgoing P at the free surface
	 */
	pS("PSLOWNESS, FREE_SURFACE, SSLOWNESS", "BOTTOM_SIDE_REFLECTION, FREE_SURFACE, BOTTOM, M660",
			"S resulting from converted reflection of upgoing P at the free surface", "littlep_bigS"),

	/**
	 * S' resulting from converted reflection of upgoing P at the free surface
	 */
	pS_prime_(null, "S' resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * S'3 resulting from converted reflection of upgoing P at the free surface
	 */
	pS_prime_3(null, "S'3 resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * S'4 resulting from converted reflection of upgoing P at the free surface
	 */
	pS_prime_4(null, "S'4 resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * S'5 resulting from converted reflection of upgoing P at the free surface
	 */
	pS_prime_5(null, "S'5 resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * S'6 resulting from converted reflection of upgoing P at the free surface
	 */
	pS_prime_6(null, "S'6 resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * S'7 resulting from converted reflection of upgoing P at the free surface
	 */
	pS_prime_7(null, "S'7 resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * S'P resulting from converted reflection of upgoing P at the free surface
	 */
	pS_prime_P(null, "S'P resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * S'P' resulting from converted reflection of upgoing P at the free surface
	 */
	pS_prime_P_prime_(null, "S'P' resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * S'PKS resulting from converted reflection of upgoing P at the free surface
	 */
	pS_prime_PKS(null, "S'PKS resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * S'S' resulting from converted reflection of upgoing P at the free surface
	 */
	pS_prime_S_prime_(null, "S'S' resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * S'SKP resulting from converted reflection of upgoing P at the free surface
	 */
	pS_prime_SKP(null, "S'SKP resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * S* resulting from converted reflection of upgoing P at the free surface
	 */
	pS_star_(null, "S* resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * S210+P resulting from converted reflection of upgoing P at the free surface
	 */
	pS210_plus_P(null, "S210+P resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * S210+S resulting from converted reflection of upgoing P at the free surface
	 */
	pS210_plus_S(null, "S210+S resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * S210-P resulting from converted reflection of upgoing P at the free surface
	 */
	pS210_minus_P(null, "S210-P resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * S210-S resulting from converted reflection of upgoing P at the free surface
	 */
	pS210_minus_S(null, "S210-S resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * S2KS resulting from converted reflection of upgoing P at the free surface
	 */
	pS2KS(null, "S2KS resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * S3KS resulting from converted reflection of upgoing P at the free surface
	 */
	pS3KS(null, "S3KS resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * S410+P resulting from converted reflection of upgoing P at the free surface
	 */
	pS410_plus_P(null, "S410+P resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * S410+S resulting from converted reflection of upgoing P at the free surface
	 */
	pS410_plus_S(null, "S410+S resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * S410-P resulting from converted reflection of upgoing P at the free surface
	 */
	pS410_minus_P(null, "S410-P resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * S410-S resulting from converted reflection of upgoing P at the free surface
	 */
	pS410_minus_S(null, "S410-S resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * S4KS resulting from converted reflection of upgoing P at the free surface
	 */
	pS4KS(null, "S4KS resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * S5KS resulting from converted reflection of upgoing P at the free surface
	 */
	pS5KS(null, "S5KS resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * S660+P resulting from converted reflection of upgoing P at the free surface
	 */
	pS660_plus_P(null, "S660+P resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * S660+S resulting from converted reflection of upgoing P at the free surface
	 */
	pS660_plus_S(null, "S660+S resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * S660-P resulting from converted reflection of upgoing P at the free surface
	 */
	pS660_minus_P(null, "S660-P resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * S660-S resulting from converted reflection of upgoing P at the free surface
	 */
	pS660_minus_S(null, "S660-S resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * S6KS resulting from converted reflection of upgoing P at the free surface
	 */
	pS6KS(null, "S6KS resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * S7KS resulting from converted reflection of upgoing P at the free surface
	 */
	pS7KS(null, "S7KS resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKIKS resulting from converted reflection of upgoing P at the free surface
	 */
	pSKIKS(null, "SKIKS resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKJKS resulting from converted reflection of upgoing P at the free surface
	 */
	pSKJKS(null, "SKJKS resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKKP resulting from converted reflection of upgoing P at the free surface
	 */
	pSKKP(null, "SKKP resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKKPab resulting from converted reflection of upgoing P at the free surface
	 */
	pSKKPab(null, "SKKPab resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKKPbc resulting from converted reflection of upgoing P at the free surface
	 */
	pSKKPbc(null, "SKKPbc resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKKPdf resulting from converted reflection of upgoing P at the free surface
	 */
	pSKKPdf(null, "SKKPdf resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKKS resulting from converted reflection of upgoing P at the free surface
	 */
	pSKKS(null, "SKKS resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKKSac resulting from converted reflection of upgoing P at the free surface
	 */
	pSKKSac(null, "SKKSac resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKKSdf resulting from converted reflection of upgoing P at the free surface
	 */
	pSKKSdf(null, "SKKSdf resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKP resulting from converted reflection of upgoing P at the free surface
	 */
	pSKP(null, "SKP resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKPab resulting from converted reflection of upgoing P at the free surface
	 */
	pSKPab(null, "SKPab resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKPbc resulting from converted reflection of upgoing P at the free surface
	 */
	pSKPbc(null, "SKPbc resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKPdf resulting from converted reflection of upgoing P at the free surface
	 */
	pSKPdf(null, "SKPdf resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKS resulting from converted reflection of upgoing P at the free surface
	 */
	pSKS(null, "SKS resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKS3 resulting from converted reflection of upgoing P at the free surface
	 */
	pSKS3(null, "SKS3 resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKS4 resulting from converted reflection of upgoing P at the free surface
	 */
	pSKS4(null, "SKS4 resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKS5 resulting from converted reflection of upgoing P at the free surface
	 */
	pSKS5(null, "SKS5 resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKS6 resulting from converted reflection of upgoing P at the free surface
	 */
	pSKS6(null, "SKS6 resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKS7 resulting from converted reflection of upgoing P at the free surface
	 */
	pSKS7(null, "SKS7 resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKSP resulting from converted reflection of upgoing P at the free surface
	 */
	pSKSP(null, "SKSP resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKSPKP resulting from converted reflection of upgoing P at the free surface
	 */
	pSKSPKP(null, "SKSPKP resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKSPKS resulting from converted reflection of upgoing P at the free surface
	 */
	pSKSPKS(null, "SKSPKS resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKSSKP resulting from converted reflection of upgoing P at the free surface
	 */
	pSKSSKP(null, "SKSSKP resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKSSKS resulting from converted reflection of upgoing P at the free surface
	 */
	pSKSSKS(null, "SKSSKS resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKSac resulting from converted reflection of upgoing P at the free surface
	 */
	pSKSac(null, "SKSac resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKSdf resulting from converted reflection of upgoing P at the free surface
	 */
	pSKSdf(null, "SKSdf resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKiKS resulting from converted reflection of upgoing P at the free surface
	 */
	pSKiKS(null, "SKiKS resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SP resulting from converted reflection of upgoing P at the free surface
	 */
	pSP(null, "SP resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SPP resulting from converted reflection of upgoing P at the free surface
	 */
	pSPP(null, "SPP resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SS resulting from converted reflection of upgoing P at the free surface
	 */
	pSS(null, "SS resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SSP resulting from converted reflection of upgoing P at the free surface
	 */
	pSSP(null, "SSP resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SSS resulting from converted reflection of upgoing P at the free surface
	 */
	pSSS(null, "SSS resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * Sb resulting from converted reflection of upgoing P at the free surface
	 */
	pSb(null, "Sb resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * ScP resulting from converted reflection of upgoing P at the free surface
	 */
	pScP(null, "ScP resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * ScPP' resulting from converted reflection of upgoing P at the free surface
	 */
	pScPP_prime_(null, "ScPP' resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * ScPPKP resulting from converted reflection of upgoing P at the free surface
	 */
	pScPPKP(null, "ScPPKP resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * ScPSKP resulting from converted reflection of upgoing P at the free surface
	 */
	pScPSKP(null, "ScPSKP resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * ScS resulting from converted reflection of upgoing P at the free surface
	 */
	pScS(null, "ScS resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * ScS2 resulting from converted reflection of upgoing P at the free surface
	 */
	pScS2(null, "ScS2 resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * ScS3 resulting from converted reflection of upgoing P at the free surface
	 */
	pScS3(null, "ScS3 resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * ScS4 resulting from converted reflection of upgoing P at the free surface
	 */
	pScS4(null, "ScS4 resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * ScS5 resulting from converted reflection of upgoing P at the free surface
	 */
	pScS5(null, "ScS5 resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * ScS6 resulting from converted reflection of upgoing P at the free surface
	 */
	pScS6(null, "ScS6 resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * ScS7 resulting from converted reflection of upgoing P at the free surface
	 */
	pScS7(null, "ScS7 resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * ScSP resulting from converted reflection of upgoing P at the free surface
	 */
	pScSP(null, "ScSP resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * ScSP' resulting from converted reflection of upgoing P at the free surface
	 */
	pScSP_prime_(null, "ScSP' resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * ScSPKP resulting from converted reflection of upgoing P at the free surface
	 */
	pScSPKP(null, "ScSPKP resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * ScSS' resulting from converted reflection of upgoing P at the free surface
	 */
	pScSS_prime_(null, "ScSS' resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * ScSSKP resulting from converted reflection of upgoing P at the free surface
	 */
	pScSSKP(null, "ScSSKP resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * ScSSKS resulting from converted reflection of upgoing P at the free surface
	 */
	pScSSKS(null, "ScSSKS resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * Sdif, resulting from converted reflection of upgoing P at the free surface
	 */
	pSdif(null, "Sdif, resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * Sg resulting from converted reflection of upgoing P at the free surface
	 */
	pSg(null, "Sg resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SgSg resulting from converted reflection of upgoing P at the free surface
	 */
	pSgSg(null, "SgSg resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SmP resulting from converted reflection of upgoing P at the free surface
	 */
	pSmP(null, "SmP resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SmS resulting from converted reflection of upgoing P at the free surface
	 */
	pSmS(null, "SmS resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SmS2 resulting from converted reflection of upgoing P at the free surface
	 */
	pSmS2(null, "SmS2 resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SmS3 resulting from converted reflection of upgoing P at the free surface
	 */
	pSmS3(null, "SmS3 resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SmS4 resulting from converted reflection of upgoing P at the free surface
	 */
	pSmS4(null, "SmS4 resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SmS5 resulting from converted reflection of upgoing P at the free surface
	 */
	pSmS5(null, "SmS5 resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SmS6 resulting from converted reflection of upgoing P at the free surface
	 */
	pSmS6(null, "SmS6 resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SmS7 resulting from converted reflection of upgoing P at the free surface
	 */
	pSmS7(null, "SmS7 resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * Sn resulting from converted reflection of upgoing P at the free surface
	 */
	pSn(null, "Sn resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SnSn resulting from converted reflection of upgoing P at the free surface
	 */
	pSnSn(null, "SnSn resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * P resulting from converted reflection of upgoing S at the free surface
	 */
	sP("SSLOWNESS, FREE_SURFACE, PSLOWNESS", "BOTTOM_SIDE_REFLECTION, FREE_SURFACE, BOTTOM, M660",
			"P resulting from converted reflection of upgoing S at the free surface", "littles_bigP"),

	/**
	 * P'3 resulting from converted reflection of upgoing S at the free surface
	 */
	sP_prime_3(null, "P'3 resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P'4 resulting from converted reflection of upgoing S at the free surface
	 */
	sP_prime_4(null, "P'4 resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P'5 resulting from converted reflection of upgoing S at the free surface
	 */
	sP_prime_5(null, "P'5 resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P'6 resulting from converted reflection of upgoing S at the free surface
	 */
	sP_prime_6(null, "P'6 resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P'7 resulting from converted reflection of upgoing S at the free surface
	 */
	sP_prime_7(null, "P'7 resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P'P' resulting from converted reflection of upgoing S at the free surface
	 */
	sP_prime_P_prime_(null, "P'P' resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P'PKS resulting from converted reflection of upgoing S at the free surface
	 */
	sP_prime_PKS(null, "P'PKS resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P'S' resulting from converted reflection of upgoing S at the free surface
	 */
	sP_prime_S_prime_(null, "P'S' resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P'SKP resulting from converted reflection of upgoing S at the free surface
	 */
	sP_prime_SKP(null, "P'SKP resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P* resulting from converted reflection of upgoing S at the free surface
	 */
	sP_star_(null, "P* resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P210+P resulting from converted reflection of upgoing S at the free surface
	 */
	sP210_plus_P(null, "P210+P resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P210+S resulting from converted reflection of upgoing S at the free surface
	 */
	sP210_plus_S(null, "P210+S resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P210-P resulting from converted reflection of upgoing S at the free surface
	 */
	sP210_minus_P(null, "P210-P resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P210-S resulting from converted reflection of upgoing S at the free surface
	 */
	sP210_minus_S(null, "P210-S resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P2KP resulting from converted reflection of upgoing S at the free surface
	 */
	sP2KP(null, "P2KP resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P3KP resulting from converted reflection of upgoing S at the free surface
	 */
	sP3KP(null, "P3KP resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P410+P resulting from converted reflection of upgoing S at the free surface
	 */
	sP410_plus_P(null, "P410+P resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P410+S resulting from converted reflection of upgoing S at the free surface
	 */
	sP410_plus_S(null, "P410+S resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P410-P resulting from converted reflection of upgoing S at the free surface
	 */
	sP410_minus_P(null, "P410-P resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P410-S resulting from converted reflection of upgoing S at the free surface
	 */
	sP410_minus_S(null, "P410-S resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P4KP resulting from converted reflection of upgoing S at the free surface
	 */
	sP4KP(null, "P4KP resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P5KP resulting from converted reflection of upgoing S at the free surface
	 */
	sP5KP(null, "P5KP resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P660+P resulting from converted reflection of upgoing S at the free surface
	 */
	sP660_plus_P(null, "P660+P resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P660+S resulting from converted reflection of upgoing S at the free surface
	 */
	sP660_plus_S(null, "P660+S resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P660-P resulting from converted reflection of upgoing S at the free surface
	 */
	sP660_minus_P(null, "P660-P resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P660-S resulting from converted reflection of upgoing S at the free surface
	 */
	sP660_minus_S(null, "P660-S resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P6KP resulting from converted reflection of upgoing S at the free surface
	 */
	sP6KP(null, "P6KP resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P7KP resulting from converted reflection of upgoing S at the free surface
	 */
	sP7KP(null, "P7KP resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PK2IKP resulting from converted reflection of upgoing S at the free surface
	 */
	sPK2IKP(null, "PK2IKP resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PK3IKP resulting from converted reflection of upgoing S at the free surface
	 */
	sPK3IKP(null, "PK3IKP resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PK4IKP resulting from converted reflection of upgoing S at the free surface
	 */
	sPK4IKP(null, "PK4IKP resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PK5IKP resulting from converted reflection of upgoing S at the free surface
	 */
	sPK5IKP(null, "PK5IKP resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PK6IKP resulting from converted reflection of upgoing S at the free surface
	 */
	sPK6IKP(null, "PK6IKP resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PK7IKP resulting from converted reflection of upgoing S at the free surface
	 */
	sPK7IKP(null, "PK7IKP resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKIKP resulting from converted reflection of upgoing S at the free surface
	 */
	sPKIKP(null, "PKIKP resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKJKP resulting from converted reflection of upgoing S at the free surface
	 */
	sPKJKP(null, "PKJKP resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKKP resulting from converted reflection of upgoing S at the free surface
	 */
	sPKKP(null, "PKKP resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKKPab resulting from converted reflection of upgoing S at the free surface
	 */
	sPKKPab(null, "PKKPab resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKKPbc resulting from converted reflection of upgoing S at the free surface
	 */
	sPKKPbc(null, "PKKPbc resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKKPdf resulting from converted reflection of upgoing S at the free surface
	 */
	sPKKPdf(null, "PKKPdf resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKKPpre resulting from converted reflection of upgoing S at the free surface
	 */
	sPKKPpre(null, "PKKPpre resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKKS resulting from converted reflection of upgoing S at the free surface
	 */
	sPKKS(null, "PKKS resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKKSab resulting from converted reflection of upgoing S at the free surface
	 */
	sPKKSab(null, "PKKSab resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKKSbc resulting from converted reflection of upgoing S at the free surface
	 */
	sPKKSbc(null, "PKKSbc resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKKSdf resulting from converted reflection of upgoing S at the free surface
	 */
	sPKKSdf(null, "PKKSdf resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKP resulting from converted reflection of upgoing S at the free surface
	 */
	sPKP(null, "PKP resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKP3 resulting from converted reflection of upgoing S at the free surface
	 */
	sPKP3(null, "PKP3 resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKP4 resulting from converted reflection of upgoing S at the free surface
	 */
	sPKP4(null, "PKP4 resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKP5 resulting from converted reflection of upgoing S at the free surface
	 */
	sPKP5(null, "PKP5 resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKP6 resulting from converted reflection of upgoing S at the free surface
	 */
	sPKP6(null, "PKP6 resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKP7 resulting from converted reflection of upgoing S at the free surface
	 */
	sPKP7(null, "PKP7 resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKPPKP resulting from converted reflection of upgoing S at the free surface
	 */
	sPKPPKP(null, "PKPPKP resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKPPKS resulting from converted reflection of upgoing S at the free surface
	 */
	sPKPPKS(null, "PKPPKS resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKPSKP resulting from converted reflection of upgoing S at the free surface
	 */
	sPKPSKP(null, "PKPSKP resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKPSKS resulting from converted reflection of upgoing S at the free surface
	 */
	sPKPSKS(null, "PKPSKS resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKPab resulting from converted reflection of upgoing S at the free surface
	 */
	sPKPab(null, "PKPab resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKPbc resulting from converted reflection of upgoing S at the free surface
	 */
	sPKPbc(null, "PKPbc resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKPdf resulting from converted reflection of upgoing S at the free surface
	 */
	sPKPdf(null, "PKPdf resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKPdif resulting from converted reflection of upgoing S at the free surface
	 */
	sPKPdif(null, "PKPdif resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKPpre resulting from converted reflection of upgoing S at the free surface
	 */
	sPKPpre(null, "PKPpre resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKS resulting from converted reflection of upgoing S at the free surface
	 */
	sPKS(null, "PKS resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKSab resulting from converted reflection of upgoing S at the free surface
	 */
	sPKSab(null, "PKSab resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKSbc resulting from converted reflection of upgoing S at the free surface
	 */
	sPKSbc(null, "PKSbc resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKSdf resulting from converted reflection of upgoing S at the free surface
	 */
	sPKSdf(null, "PKSdf resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKiKP resulting from converted reflection of upgoing S at the free surface
	 */
	sPKiKP(null, "PKiKP resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PP resulting from converted reflection of upgoing S at the free surface
	 */
	sPP(null, "PP resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PPP resulting from converted reflection of upgoing S at the free surface
	 */
	sPPP(null, "PPP resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PPS resulting from converted reflection of upgoing S at the free surface
	 */
	sPPS(null, "PPS resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PS resulting from converted reflection of upgoing S at the free surface
	 */
	sPS(null, "PS resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PS' resulting from converted reflection of upgoing S at the free surface
	 */
	sPS_prime_(null, "PS' resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PSKS resulting from converted reflection of upgoing S at the free surface
	 */
	sPSKS(null, "PSKS resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PSS resulting from converted reflection of upgoing S at the free surface
	 */
	sPSS(null, "PSS resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PScS resulting from converted reflection of upgoing S at the free surface
	 */
	sPScS(null, "PScS resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * Pb resulting from converted reflection of upgoing S at the free surface
	 */
	sPb(null, "Pb resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PcP resulting from converted reflection of upgoing S at the free surface
	 */
	sPcP(null, "PcP resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PcP2 resulting from converted reflection of upgoing S at the free surface
	 */
	sPcP2(null, "PcP2 resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PcP3 resulting from converted reflection of upgoing S at the free surface
	 */
	sPcP3(null, "PcP3 resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PcP4 resulting from converted reflection of upgoing S at the free surface
	 */
	sPcP4(null, "PcP4 resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PcP5 resulting from converted reflection of upgoing S at the free surface
	 */
	sPcP5(null, "PcP5 resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PcP6 resulting from converted reflection of upgoing S at the free surface
	 */
	sPcP6(null, "PcP6 resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PcP7 resulting from converted reflection of upgoing S at the free surface
	 */
	sPcP7(null, "PcP7 resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PcPP' resulting from converted reflection of upgoing S at the free surface
	 */
	sPcPP_prime_(null, "PcPP' resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PcPPKP resulting from converted reflection of upgoing S at the free surface
	 */
	sPcPPKP(null, "PcPPKP resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PcPSKP resulting from converted reflection of upgoing S at the free surface
	 */
	sPcPSKP(null, "PcPSKP resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PcS resulting from converted reflection of upgoing S at the free surface
	 */
	sPcS(null, "PcS resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PcSP' resulting from converted reflection of upgoing S at the free surface
	 */
	sPcSP_prime_(null, "PcSP' resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PcSPKP resulting from converted reflection of upgoing S at the free surface
	 */
	sPcSPKP(null, "PcSPKP resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PcSS' resulting from converted reflection of upgoing S at the free surface
	 */
	sPcSS_prime_(null, "PcSS' resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PcSSKP resulting from converted reflection of upgoing S at the free surface
	 */
	sPcSSKP(null, "PcSSKP resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PcSSKS resulting from converted reflection of upgoing S at the free surface
	 */
	sPcSSKS(null, "PcSSKS resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * Pdif resulting from converted reflection of upgoing S at the free surface
	 */
	sPdif(null, "Pdif resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * Pg resulting from converted reflection of upgoing S at the free surface
	 */
	sPg(null, "Pg resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PgPg resulting from converted reflection of upgoing S at the free surface
	 */
	sPgPg(null, "PgPg resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PmP resulting from converted reflection of upgoing S at the free surface
	 */
	sPmP(null, "PmP resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PmP2 resulting from converted reflection of upgoing S at the free surface
	 */
	sPmP2(null, "PmP2 resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PmP3 resulting from converted reflection of upgoing S at the free surface
	 */
	sPmP3(null, "PmP3 resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PmP4 resulting from converted reflection of upgoing S at the free surface
	 */
	sPmP4(null, "PmP4 resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PmP5 resulting from converted reflection of upgoing S at the free surface
	 */
	sPmP5(null, "PmP5 resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PmP6 resulting from converted reflection of upgoing S at the free surface
	 */
	sPmP6(null, "PmP6 resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PmP7 resulting from converted reflection of upgoing S at the free surface
	 */
	sPmP7(null, "PmP7 resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PmS resulting from converted reflection of upgoing S at the free surface
	 */
	sPmS(null, "PmS resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * Pn resulting from converted reflection of upgoing S at the free surface
	 */
	sPn(null, "Pn resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PnPn resulting from converted reflection of upgoing S at the free surface
	 */
	sPnPn(null, "PnPn resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * S resulting from reflection of upgoing S at the free surface
	 */
	sS("SSLOWNESS, FREE_SURFACE, SSLOWNESS", "BOTTOM_SIDE_REFLECTION, FREE_SURFACE, BOTTOM, M660",
			"S resulting from reflection of upgoing S at the free surface", "littles_bigS"),

	/**
	 * S' resulting from reflection of upgoing S at the free surface
	 */
	sS_prime_(null, "S' resulting from reflection of upgoing S at the free surface"),

	/**
	 * S'3 resulting from reflection of upgoing S at the free surface
	 */
	sS_prime_3(null, "S'3 resulting from reflection of upgoing S at the free surface"),

	/**
	 * S'4 resulting from reflection of upgoing S at the free surface
	 */
	sS_prime_4(null, "S'4 resulting from reflection of upgoing S at the free surface"),

	/**
	 * S'5 resulting from reflection of upgoing S at the free surface
	 */
	sS_prime_5(null, "S'5 resulting from reflection of upgoing S at the free surface"),

	/**
	 * S'6 resulting from reflection of upgoing S at the free surface
	 */
	sS_prime_6(null, "S'6 resulting from reflection of upgoing S at the free surface"),

	/**
	 * S'7 resulting from reflection of upgoing S at the free surface
	 */
	sS_prime_7(null, "S'7 resulting from reflection of upgoing S at the free surface"),

	/**
	 * S'P resulting from reflection of upgoing S at the free surface
	 */
	sS_prime_P(null, "S'P resulting from reflection of upgoing S at the free surface"),

	/**
	 * S'P' resulting from reflection of upgoing S at the free surface
	 */
	sS_prime_P_prime_(null, "S'P' resulting from reflection of upgoing S at the free surface"),

	/**
	 * S'PKS resulting from reflection of upgoing S at the free surface
	 */
	sS_prime_PKS(null, "S'PKS resulting from reflection of upgoing S at the free surface"),

	/**
	 * S'S' resulting from reflection of upgoing S at the free surface
	 */
	sS_prime_S_prime_(null, "S'S' resulting from reflection of upgoing S at the free surface"),

	/**
	 * S'SKP resulting from reflection of upgoing S at the free surface
	 */
	sS_prime_SKP(null, "S'SKP resulting from reflection of upgoing S at the free surface"),

	/**
	 * S* resulting from reflection of upgoing S at the free surface
	 */
	sS_star_(GeoAttributes.SSLOWNESS, "S* resulting from reflection of upgoing S at the free surface"),

	/**
	 * S210+P resulting from reflection of upgoing S at the free surface
	 */
	sS210_plus_P(null, "S210+P resulting from reflection of upgoing S at the free surface"),

	/**
	 * S210+S resulting from reflection of upgoing S at the free surface
	 */
	sS210_plus_S(null, "S210+S resulting from reflection of upgoing S at the free surface"),

	/**
	 * S210-P resulting from reflection of upgoing S at the free surface
	 */
	sS210_minus_P(null, "S210-P resulting from reflection of upgoing S at the free surface"),

	/**
	 * S210-S resulting from reflection of upgoing S at the free surface
	 */
	sS210_minus_S(GeoAttributes.SSLOWNESS, "S210-S resulting from reflection of upgoing S at the free surface"),

	/**
	 * S2KS resulting from reflection of upgoing S at the free surface
	 */
	sS2KS(GeoAttributes.SSLOWNESS, "S2KS resulting from reflection of upgoing S at the free surface"),

	/**
	 * S3KS resulting from reflection of upgoing S at the free surface
	 */
	sS3KS(GeoAttributes.SSLOWNESS, "S3KS resulting from reflection of upgoing S at the free surface"),

	/**
	 * S410+P resulting from reflection of upgoing S at the free surface
	 */
	sS410_plus_P(null, "S410+P resulting from reflection of upgoing S at the free surface"),

	/**
	 * S410+S resulting from reflection of upgoing S at the free surface
	 */
	sS410_plus_S(null, "S410+S resulting from reflection of upgoing S at the free surface"),

	/**
	 * S410-P resulting from reflection of upgoing S at the free surface
	 */
	sS410_minus_P(null, "S410-P resulting from reflection of upgoing S at the free surface"),

	/**
	 * S410-S resulting from reflection of upgoing S at the free surface
	 */
	sS410_minus_S(GeoAttributes.SSLOWNESS, "S410-S resulting from reflection of upgoing S at the free surface"),

	/**
	 * S4KS resulting from reflection of upgoing S at the free surface
	 */
	sS4KS(GeoAttributes.SSLOWNESS, "S4KS resulting from reflection of upgoing S at the free surface"),

	/**
	 * S5KS resulting from reflection of upgoing S at the free surface
	 */
	sS5KS(GeoAttributes.SSLOWNESS, "S5KS resulting from reflection of upgoing S at the free surface"),

	/**
	 * S660+P resulting from reflection of upgoing S at the free surface
	 */
	sS660_plus_P(null, "S660+P resulting from reflection of upgoing S at the free surface"),

	/**
	 * S660+S resulting from reflection of upgoing S at the free surface
	 */
	sS660_plus_S(null, "S660+S resulting from reflection of upgoing S at the free surface"),

	/**
	 * S660-P resulting from reflection of upgoing S at the free surface
	 */
	sS660_minus_P(null, "S660-P resulting from reflection of upgoing S at the free surface"),

	/**
	 * S660-S resulting from reflection of upgoing S at the free surface
	 */
	sS660_minus_S(GeoAttributes.SSLOWNESS, "S660-S resulting from reflection of upgoing S at the free surface"),

	/**
	 * S6KS resulting from reflection of upgoing S at the free surface
	 */
	sS6KS(GeoAttributes.SSLOWNESS, "S6KS resulting from reflection of upgoing S at the free surface"),

	/**
	 * S7KS resulting from reflection of upgoing S at the free surface
	 */
	sS7KS(GeoAttributes.SSLOWNESS, "S7KS resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKIKS resulting from reflection of upgoing S at the free surface
	 */
	sSKIKS(GeoAttributes.SSLOWNESS, "SKIKS resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKJKS resulting from reflection of upgoing S at the free surface
	 */
	sSKJKS(GeoAttributes.SSLOWNESS, "SKJKS resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKKP resulting from reflection of upgoing S at the free surface
	 */
	sSKKP(null, "SKKP resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKKPab resulting from reflection of upgoing S at the free surface
	 */
	sSKKPab(null, "SKKPab resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKKPbc resulting from reflection of upgoing S at the free surface
	 */
	sSKKPbc(null, "SKKPbc resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKKPdf resulting from reflection of upgoing S at the free surface
	 */
	sSKKPdf(null, "SKKPdf resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKKS resulting from reflection of upgoing S at the free surface
	 */
	sSKKS(GeoAttributes.SSLOWNESS, "SKKS resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKKSac resulting from reflection of upgoing S at the free surface
	 */
	sSKKSac(GeoAttributes.SSLOWNESS, "SKKSac resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKKSdf resulting from reflection of upgoing S at the free surface
	 */
	sSKKSdf(GeoAttributes.SSLOWNESS, "SKKSdf resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKP resulting from reflection of upgoing S at the free surface
	 */
	sSKP(null, "SKP resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKPab resulting from reflection of upgoing S at the free surface
	 */
	sSKPab(null, "SKPab resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKPbc resulting from reflection of upgoing S at the free surface
	 */
	sSKPbc(null, "SKPbc resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKPdf resulting from reflection of upgoing S at the free surface
	 */
	sSKPdf(null, "SKPdf resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKS resulting from reflection of upgoing S at the free surface
	 */
	sSKS(GeoAttributes.SSLOWNESS, "SKS resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKS3 resulting from reflection of upgoing S at the free surface
	 */
	sSKS3(GeoAttributes.SSLOWNESS, "SKS3 resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKS4 resulting from reflection of upgoing S at the free surface
	 */
	sSKS4(GeoAttributes.SSLOWNESS, "SKS4 resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKS5 resulting from reflection of upgoing S at the free surface
	 */
	sSKS5(GeoAttributes.SSLOWNESS, "SKS5 resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKS6 resulting from reflection of upgoing S at the free surface
	 */
	sSKS6(GeoAttributes.SSLOWNESS, "SKS6 resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKS7 resulting from reflection of upgoing S at the free surface
	 */
	sSKS7(GeoAttributes.SSLOWNESS, "SKS7 resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKSP resulting from reflection of upgoing S at the free surface
	 */
	sSKSP(null, "SKSP resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKSPKP resulting from reflection of upgoing S at the free surface
	 */
	sSKSPKP(null, "SKSPKP resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKSPKS resulting from reflection of upgoing S at the free surface
	 */
	sSKSPKS(null, "SKSPKS resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKSSKP resulting from reflection of upgoing S at the free surface
	 */
	sSKSSKP(null, "SKSSKP resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKSSKS resulting from reflection of upgoing S at the free surface
	 */
	sSKSSKS(GeoAttributes.SSLOWNESS, "SKSSKS resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKSac resulting from reflection of upgoing S at the free surface
	 */
	sSKSac(GeoAttributes.SSLOWNESS, "SKSac resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKSdf resulting from reflection of upgoing S at the free surface
	 */
	sSKSdf(GeoAttributes.SSLOWNESS, "SKSdf resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKiKS resulting from reflection of upgoing S at the free surface
	 */
	sSKiKS(GeoAttributes.SSLOWNESS, "SKiKS resulting from reflection of upgoing S at the free surface"),

	/**
	 * SP resulting from reflection of upgoing S at the free surface
	 */
	sSP(null, "SP resulting from reflection of upgoing S at the free surface"),

	/**
	 * SPP resulting from reflection of upgoing S at the free surface
	 */
	sSPP(null, "SPP resulting from reflection of upgoing S at the free surface"),

	/**
	 * SS resulting from reflection of upgoing S at the free surface
	 */
	sSS(GeoAttributes.SSLOWNESS, "SS resulting from reflection of upgoing S at the free surface"),

	/**
	 * SSP resulting from reflection of upgoing S at the free surface
	 */
	sSSP(null, "SSP resulting from reflection of upgoing S at the free surface"),

	/**
	 * SSS resulting from reflection of upgoing S at the free surface
	 */
	sSSS(GeoAttributes.SSLOWNESS, "SSS resulting from reflection of upgoing S at the free surface"),

	/**
	 * Sb resulting from reflection of upgoing S at the free surface
	 */
	sSb(GeoAttributes.SSLOWNESS, "Sb resulting from reflection of upgoing S at the free surface"),

	/**
	 * ScP resulting from reflection of upgoing S at the free surface
	 */
	sScP(null, "ScP resulting from reflection of upgoing S at the free surface"),

	/**
	 * ScPP' resulting from reflection of upgoing S at the free surface
	 */
	sScPP_prime_(null, "ScPP' resulting from reflection of upgoing S at the free surface"),

	/**
	 * ScPPKP resulting from reflection of upgoing S at the free surface
	 */
	sScPPKP(null, "ScPPKP resulting from reflection of upgoing S at the free surface"),

	/**
	 * ScPSKP resulting from reflection of upgoing S at the free surface
	 */
	sScPSKP(null, "ScPSKP resulting from reflection of upgoing S at the free surface"),

	/**
	 * ScS resulting from reflection of upgoing S at the free surface
	 */
	sScS(GeoAttributes.SSLOWNESS, "ScS resulting from reflection of upgoing S at the free surface"),

	/**
	 * ScS2 resulting from reflection of upgoing S at the free surface
	 */
	sScS2(GeoAttributes.SSLOWNESS, "ScS2 resulting from reflection of upgoing S at the free surface"),

	/**
	 * ScS3 resulting from reflection of upgoing S at the free surface
	 */
	sScS3(GeoAttributes.SSLOWNESS, "ScS3 resulting from reflection of upgoing S at the free surface"),

	/**
	 * ScS4 resulting from reflection of upgoing S at the free surface
	 */
	sScS4(GeoAttributes.SSLOWNESS, "ScS4 resulting from reflection of upgoing S at the free surface"),

	/**
	 * ScS5 resulting from reflection of upgoing S at the free surface
	 */
	sScS5(GeoAttributes.SSLOWNESS, "ScS5 resulting from reflection of upgoing S at the free surface"),

	/**
	 * ScS6 resulting from reflection of upgoing S at the free surface
	 */
	sScS6(GeoAttributes.SSLOWNESS, "ScS6 resulting from reflection of upgoing S at the free surface"),

	/**
	 * ScS7 resulting from reflection of upgoing S at the free surface
	 */
	sScS7(GeoAttributes.SSLOWNESS, "ScS7 resulting from reflection of upgoing S at the free surface"),

	/**
	 * ScSP resulting from reflection of upgoing S at the free surface
	 */
	sScSP(null, "ScSP resulting from reflection of upgoing S at the free surface"),

	/**
	 * ScSP' resulting from reflection of upgoing S at the free surface
	 */
	sScSP_prime_(null, "ScSP' resulting from reflection of upgoing S at the free surface"),

	/**
	 * ScSPKP resulting from reflection of upgoing S at the free surface
	 */
	sScSPKP(null, "ScSPKP resulting from reflection of upgoing S at the free surface"),

	/**
	 * ScSS' resulting from reflection of upgoing S at the free surface
	 */
	sScSS_prime_(null, "ScSS' resulting from reflection of upgoing S at the free surface"),

	/**
	 * ScSSKP resulting from reflection of upgoing S at the free surface
	 */
	sScSSKP(null, "ScSSKP resulting from reflection of upgoing S at the free surface"),

	/**
	 * ScSSKS resulting from reflection of upgoing S at the free surface
	 */
	sScSSKS(GeoAttributes.SSLOWNESS, "ScSSKS resulting from reflection of upgoing S at the free surface"),

	/**
	 * Sdif, resulting from reflection of upgoing S at the free surface
	 */
	sSdif(GeoAttributes.SSLOWNESS, "Sdif, resulting from reflection of upgoing S at the free surface"),

	/**
	 * Sg resulting from reflection of upgoing S at the free surface
	 */
	sSg(GeoAttributes.SSLOWNESS, "Sg resulting from reflection of upgoing S at the free surface"),

	/**
	 * SgSg resulting from reflection of upgoing S at the free surface
	 */
	sSgSg(GeoAttributes.SSLOWNESS, "SgSg resulting from reflection of upgoing S at the free surface"),

	/**
	 * SmP resulting from reflection of upgoing S at the free surface
	 */
	sSmP(null, "SmP resulting from reflection of upgoing S at the free surface"),

	/**
	 * SmS resulting from reflection of upgoing S at the free surface
	 */
	sSmS(GeoAttributes.SSLOWNESS, "SmS resulting from reflection of upgoing S at the free surface"),

	/**
	 * SmS2 resulting from reflection of upgoing S at the free surface
	 */
	sSmS2(GeoAttributes.SSLOWNESS, "SmS2 resulting from reflection of upgoing S at the free surface"),

	/**
	 * SmS3 resulting from reflection of upgoing S at the free surface
	 */
	sSmS3(GeoAttributes.SSLOWNESS, "SmS3 resulting from reflection of upgoing S at the free surface"),

	/**
	 * SmS4 resulting from reflection of upgoing S at the free surface
	 */
	sSmS4(GeoAttributes.SSLOWNESS, "SmS4 resulting from reflection of upgoing S at the free surface"),

	/**
	 * SmS5 resulting from reflection of upgoing S at the free surface
	 */
	sSmS5(GeoAttributes.SSLOWNESS, "SmS5 resulting from reflection of upgoing S at the free surface"),

	/**
	 * SmS6 resulting from reflection of upgoing S at the free surface
	 */
	sSmS6(GeoAttributes.SSLOWNESS, "SmS6 resulting from reflection of upgoing S at the free surface"),

	/**
	 * SmS7 resulting from reflection of upgoing S at the free surface
	 */
	sSmS7(GeoAttributes.SSLOWNESS, "SmS7 resulting from reflection of upgoing S at the free surface"),

	/**
	 * Sn resulting from reflection of upgoing S at the free surface
	 */
	sSn(GeoAttributes.SSLOWNESS, "Sn resulting from reflection of upgoing S at the free surface"),

	/**
	 * SnSn resulting from reflection of upgoing S at the free surface
	 */
	sSnSn(GeoAttributes.SSLOWNESS, "SnSn resulting from reflection of upgoing S at the free surface"),

	/**
	 * P resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP(GeoAttributes.PSLOWNESS, "P resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P'3 resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP_prime_3(GeoAttributes.PSLOWNESS, "P'3 resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P'4 resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP_prime_4(GeoAttributes.PSLOWNESS, "P'4 resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P'5 resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP_prime_5(GeoAttributes.PSLOWNESS, "P'5 resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P'6 resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP_prime_6(GeoAttributes.PSLOWNESS, "P'6 resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P'7 resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP_prime_7(GeoAttributes.PSLOWNESS, "P'7 resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P'P' resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP_prime_P_prime_(GeoAttributes.PSLOWNESS,
			"P'P' resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P'PKS resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP_prime_PKS(null, "P'PKS resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P'S' resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP_prime_S_prime_(null, "P'S' resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P'SKP resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP_prime_SKP(null, "P'SKP resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P* resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP_star_(null, "P* resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P210+P resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP210_plus_P(null, "P210+P resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P210+S resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP210_plus_S(null, "P210+S resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P210-P resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP210_minus_P(null, "P210-P resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P210-S resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP210_minus_S(null, "P210-S resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P2KP resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP2KP(GeoAttributes.PSLOWNESS, "P2KP resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P3KP resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP3KP(GeoAttributes.PSLOWNESS, "P3KP resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P410+P resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP410_plus_P(null, "P410+P resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P410+S resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP410_plus_S(null, "P410+S resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P410-P resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP410_minus_P(null, "P410-P resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P410-S resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP410_minus_S(null, "P410-S resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P4KP resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP4KP(GeoAttributes.PSLOWNESS, "P4KP resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P5KP resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP5KP(GeoAttributes.PSLOWNESS, "P5KP resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P660+P resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP660_plus_P(null, "P660+P resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P660+S resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP660_plus_S(null, "P660+S resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P660-P resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP660_minus_P(null, "P660-P resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P660-S resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP660_minus_S(null, "P660-S resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P6KP resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP6KP(GeoAttributes.PSLOWNESS, "P6KP resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P7KP resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP7KP(GeoAttributes.PSLOWNESS, "P7KP resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PK2IKP resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPK2IKP(GeoAttributes.PSLOWNESS, "PK2IKP resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PK3IKP resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPK3IKP(GeoAttributes.PSLOWNESS, "PK3IKP resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PK4IKP resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPK4IKP(GeoAttributes.PSLOWNESS, "PK4IKP resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PK5IKP resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPK5IKP(GeoAttributes.PSLOWNESS, "PK5IKP resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PK6IKP resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPK6IKP(GeoAttributes.PSLOWNESS, "PK6IKP resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PK7IKP resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPK7IKP(GeoAttributes.PSLOWNESS, "PK7IKP resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKIKP resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKIKP(GeoAttributes.PSLOWNESS, "PKIKP resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKJKP resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKJKP(GeoAttributes.PSLOWNESS, "PKJKP resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKKP resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKKP(GeoAttributes.PSLOWNESS, "PKKP resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKKPab resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKKPab(GeoAttributes.PSLOWNESS, "PKKPab resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKKPbc resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKKPbc(GeoAttributes.PSLOWNESS, "PKKPbc resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKKPdf resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKKPdf(GeoAttributes.PSLOWNESS, "PKKPdf resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKKS resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKKS(null, "PKKS resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKKSab resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKKSab(null, "PKKSab resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKKSbc resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKKSbc(null, "PKKSbc resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKKSdf resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKKSdf(null, "PKKSdf resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKP resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKP(GeoAttributes.PSLOWNESS, "PKP resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKP3 resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKP3(GeoAttributes.PSLOWNESS, "PKP3 resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKP4 resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKP4(GeoAttributes.PSLOWNESS, "PKP4 resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKP5 resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKP5(GeoAttributes.PSLOWNESS, "PKP5 resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKP6 resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKP6(GeoAttributes.PSLOWNESS, "PKP6 resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKP7 resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKP7(GeoAttributes.PSLOWNESS, "PKP7 resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKPPKP resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKPPKP(GeoAttributes.PSLOWNESS, "PKPPKP resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKPPKS resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKPPKS(null, "PKPPKS resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKPSKP resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKPSKP(null, "PKPSKP resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKPSKS resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKPSKS(null, "PKPSKS resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKPab resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKPab(GeoAttributes.PSLOWNESS, "PKPab resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKPbc resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKPbc(GeoAttributes.PSLOWNESS, "PKPbc resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKPdf resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKPdf(GeoAttributes.PSLOWNESS, "PKPdf resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKPdif resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKPdif(GeoAttributes.PSLOWNESS, "PKPdif resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKPpre resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKPpre(GeoAttributes.PSLOWNESS, "PKPpre resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKS resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKS(null, "PKS resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKSab resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKSab(null, "PKSab resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKSbc resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKSbc(null, "PKSbc resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKSdf resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKSdf(null, "PKSdf resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKiKP resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKiKP(GeoAttributes.PSLOWNESS, "PKiKP resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PP resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPP(GeoAttributes.PSLOWNESS, "PP resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PPP resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPPP(GeoAttributes.PSLOWNESS, "PPP resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PPS resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPPS(null, "PPS resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PS resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPS(null, "PS resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PS' resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPS_prime_(null, "PS' resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PSKS resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPSKS(null, "PSKS resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PSS resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPSS(null, "PSS resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PScS resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPScS(null, "PScS resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * Pb resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPb(GeoAttributes.PSLOWNESS, "Pb resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PcP resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPcP(GeoAttributes.PSLOWNESS, "PcP resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PcP2 resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPcP2(GeoAttributes.PSLOWNESS, "PcP2 resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PcP3 resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPcP3(GeoAttributes.PSLOWNESS, "PcP3 resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PcP4 resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPcP4(GeoAttributes.PSLOWNESS, "PcP4 resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PcP5 resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPcP5(GeoAttributes.PSLOWNESS, "PcP5 resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PcP6 resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPcP6(GeoAttributes.PSLOWNESS, "PcP6 resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PcP7 resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPcP7(GeoAttributes.PSLOWNESS, "PcP7 resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PcPP' resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPcPP_prime_(GeoAttributes.PSLOWNESS, "PcPP' resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PcPPKP resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPcPPKP(GeoAttributes.PSLOWNESS, "PcPPKP resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PcPSKP resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPcPSKP(null, "PcPSKP resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PcS resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPcS(null, "PcS resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PcSP' resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPcSP_prime_(null, "PcSP' resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PcSPKP resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPcSPKP(null, "PcSPKP resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PcSS' resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPcSS_prime_(null, "PcSS' resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PcSSKP resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPcSSKP(null, "PcSSKP resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PcSSKS resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPcSSKS(null, "PcSSKS resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * Pdif resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPdif(GeoAttributes.PSLOWNESS, "Pdif resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * Pg resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPg(GeoAttributes.PSLOWNESS, "Pg resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PgPg resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPgPg(GeoAttributes.PSLOWNESS, "PgPg resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PmP resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPmP(GeoAttributes.PSLOWNESS, "PmP resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PmP2 resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPmP2(GeoAttributes.PSLOWNESS, "PmP2 resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PmP3 resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPmP3(GeoAttributes.PSLOWNESS, "PmP3 resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PmP4 resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPmP4(GeoAttributes.PSLOWNESS, "PmP4 resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PmP5 resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPmP5(GeoAttributes.PSLOWNESS, "PmP5 resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PmP6 resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPmP6(GeoAttributes.PSLOWNESS, "PmP6 resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PmP7 resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPmP7(GeoAttributes.PSLOWNESS, "PmP7 resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PmS resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPmS(null, "PmS resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * Pn resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPn(GeoAttributes.PSLOWNESS, "Pn resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PnPn resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPnPn(GeoAttributes.PSLOWNESS, "PnPn resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP(GeoAttributes.PSLOWNESS, "P resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P'3 resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP_prime_3(GeoAttributes.PSLOWNESS, "P'3 resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P'4 resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP_prime_4(GeoAttributes.PSLOWNESS, "P'4 resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P'5 resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP_prime_5(GeoAttributes.PSLOWNESS, "P'5 resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P'6 resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP_prime_6(GeoAttributes.PSLOWNESS, "P'6 resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P'7 resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP_prime_7(GeoAttributes.PSLOWNESS, "P'7 resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P'P' resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP_prime_P_prime_(GeoAttributes.PSLOWNESS,
			"P'P' resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P'PKS resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP_prime_PKS(null, "P'PKS resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P'S' resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP_prime_S_prime_(null, "P'S' resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P'SKP resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP_prime_SKP(null, "P'SKP resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P* resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP_star_(null, "P* resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P210+P resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP210_plus_P(null, "P210+P resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P210+S resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP210_plus_S(null, "P210+S resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P210-P resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP210_minus_P(null, "P210-P resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P210-S resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP210_minus_S(null, "P210-S resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P2KP resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP2KP(GeoAttributes.PSLOWNESS, "P2KP resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P3KP resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP3KP(GeoAttributes.PSLOWNESS, "P3KP resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P410+P resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP410_plus_P(null, "P410+P resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P410+S resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP410_plus_S(null, "P410+S resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P410-P resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP410_minus_P(null, "P410-P resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P410-S resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP410_minus_S(null, "P410-S resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P4KP resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP4KP(GeoAttributes.PSLOWNESS, "P4KP resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P5KP resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP5KP(GeoAttributes.PSLOWNESS, "P5KP resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P660+P resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP660_plus_P(null, "P660+P resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P660+S resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP660_plus_S(null, "P660+S resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P660-P resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP660_minus_P(null, "P660-P resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P660-S resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP660_minus_S(null, "P660-S resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P6KP resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP6KP(GeoAttributes.PSLOWNESS, "P6KP resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P7KP resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP7KP(GeoAttributes.PSLOWNESS, "P7KP resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PK2IKP resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPK2IKP(GeoAttributes.PSLOWNESS, "PK2IKP resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PK3IKP resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPK3IKP(GeoAttributes.PSLOWNESS, "PK3IKP resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PK4IKP resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPK4IKP(GeoAttributes.PSLOWNESS, "PK4IKP resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PK5IKP resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPK5IKP(GeoAttributes.PSLOWNESS, "PK5IKP resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PK6IKP resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPK6IKP(GeoAttributes.PSLOWNESS, "PK6IKP resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PK7IKP resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPK7IKP(GeoAttributes.PSLOWNESS, "PK7IKP resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKIKP resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKIKP(GeoAttributes.PSLOWNESS, "PKIKP resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKJKP resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKJKP(GeoAttributes.PSLOWNESS, "PKJKP resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKKP resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKKP(GeoAttributes.PSLOWNESS, "PKKP resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKKPab resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKKPab(GeoAttributes.PSLOWNESS, "PKKPab resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKKPbc resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKKPbc(GeoAttributes.PSLOWNESS, "PKKPbc resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKKPdf resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKKPdf(GeoAttributes.PSLOWNESS, "PKKPdf resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKKS resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKKS(null, "PKKS resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKKSab resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKKSab(null, "PKKSab resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKKSbc resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKKSbc(null, "PKKSbc resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKKSdf resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKKSdf(null, "PKKSdf resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKP resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKP(GeoAttributes.PSLOWNESS, "PKP resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKP3 resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKP3(GeoAttributes.PSLOWNESS, "PKP3 resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKP4 resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKP4(GeoAttributes.PSLOWNESS, "PKP4 resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKP5 resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKP5(GeoAttributes.PSLOWNESS, "PKP5 resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKP6 resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKP6(GeoAttributes.PSLOWNESS, "PKP6 resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKP7 resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKP7(GeoAttributes.PSLOWNESS, "PKP7 resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKPPKP resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKPPKP(GeoAttributes.PSLOWNESS, "PKPPKP resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKPPKS resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKPPKS(null, "PKPPKS resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKPSKP resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKPSKP(null, "PKPSKP resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKPSKS resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKPSKS(null, "PKPSKS resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKPab resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKPab(GeoAttributes.PSLOWNESS, "PKPab resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKPbc resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKPbc(GeoAttributes.PSLOWNESS, "PKPbc resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKPdf resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKPdf(GeoAttributes.PSLOWNESS, "PKPdf resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKPdif resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKPdif(GeoAttributes.PSLOWNESS, "PKPdif resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKPpre resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKPpre(GeoAttributes.PSLOWNESS, "PKPpre resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKS resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKS(null, "PKS resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKSab resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKSab(null, "PKSab resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKSbc resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKSbc(null, "PKSbc resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKSdf resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKSdf(null, "PKSdf resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKiKP resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKiKP(GeoAttributes.PSLOWNESS, "PKiKP resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PP resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPP(GeoAttributes.PSLOWNESS, "PP resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PPP resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPPP(GeoAttributes.PSLOWNESS, "PPP resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PPS resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPPS(null, "PPS resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PS resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPS(null, "PS resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PS' resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPS_prime_(null, "PS' resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PSKS resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPSKS(null, "PSKS resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PSS resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPSS(null, "PSS resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PScS resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPScS(null, "PScS resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PcP resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPcP(GeoAttributes.PSLOWNESS, "PcP resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PcP2 resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPcP2(GeoAttributes.PSLOWNESS, "PcP2 resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PcP3 resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPcP3(GeoAttributes.PSLOWNESS, "PcP3 resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PcP4 resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPcP4(GeoAttributes.PSLOWNESS, "PcP4 resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PcP5 resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPcP5(GeoAttributes.PSLOWNESS, "PcP5 resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PcP6 resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPcP6(GeoAttributes.PSLOWNESS, "PcP6 resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PcP7 resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPcP7(GeoAttributes.PSLOWNESS, "PcP7 resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PcPP' resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPcPP_prime_(GeoAttributes.PSLOWNESS,
			"PcPP' resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PcPPKP resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPcPPKP(GeoAttributes.PSLOWNESS, "PcPPKP resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PcPSKP resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPcPSKP(null, "PcPSKP resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PcS resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPcS(null, "PcS resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PcSP' resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPcSP_prime_(null, "PcSP' resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PcSPKP resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPcSPKP(null, "PcSPKP resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PcSS' resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPcSS_prime_(null, "PcSS' resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PcSSKP resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPcSSKP(null, "PcSSKP resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PcSSKS resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPcSSKS(null, "PcSSKS resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * Pdif resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPdif(GeoAttributes.PSLOWNESS, "Pdif resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * Unspecified long period surface wave
	 */
	L(null, "Unspecified long period surface wave"),

	/**
	 * Love wave
	 */
	LQ(null, "Love wave"),

	/**
	 * Rayleigh wave
	 */
	LR(null, "Rayleigh wave"),

	/**
	 * Mantle wave of Love type
	 */
	G(null, "Mantle wave of Love type"),

	/**
	 * Mantle wave of Rayleigh type
	 */
	R(null, "Mantle wave of Rayleigh type"),

	/**
	 * Mantle wave of Love type; traveling along the major arc of the great circle
	 */
	G2(null, "Mantle wave of Love type; traveling along the major arc of the great circle"),

	/**
	 * Mantle wave of Love type; traveling 1 circuit and the minor arc
	 */
	G3(null, "Mantle wave of Love type; traveling 1 circuit and the minor arc"),

	/**
	 * Mantle wave of Love type; traveling 1 circuit and the major arc
	 */
	G4(null, "Mantle wave of Love type; traveling 1 circuit and the major arc"),

	/**
	 * Mantle wave of Love type; traveling 2 circuits and the minor arc
	 */
	G5(null, "Mantle wave of Love type; traveling 2 circuits and the minor arc"),

	/**
	 * Mantle wave of Love type; traveling 2 circuits and the major arc
	 */
	G6(null, "Mantle wave of Love type; traveling 2 circuits and the major arc"),

	/**
	 * Mantle wave of Love type; traveling 3 circuits and the minor arc
	 */
	G7(null, "Mantle wave of Love type; traveling 3 circuits and the minor arc"),

	/**
	 * Mantle wave of Rayleigh type; traveling along the major arc of the great
	 * circle
	 */
	R2(null, "Mantle wave of Rayleigh type; traveling along the major arc of the great circle"),

	/**
	 * Mantle wave of Rayleigh type; traveling 1 circuit and the minor arc
	 */
	R3(null, "Mantle wave of Rayleigh type; traveling 1 circuit and the minor arc"),

	/**
	 * Mantle wave of Rayleigh type; traveling 1 circuit and the major arc
	 */
	R4(null, "Mantle wave of Rayleigh type; traveling 1 circuit and the major arc"),

	/**
	 * Mantle wave of Rayleigh type; traveling 2 circuits and the minor arc
	 */
	R5(null, "Mantle wave of Rayleigh type; traveling 2 circuits and the minor arc"),

	/**
	 * Mantle wave of Rayleigh type; traveling 2 circuits and the major arc
	 */
	R6(null, "Mantle wave of Rayleigh type; traveling 2 circuits and the major arc"),

	/**
	 * Mantle wave of Rayleigh type; traveling 3 circuits and the minor arc
	 */
	R7(null, "Mantle wave of Rayleigh type; traveling 3 circuits and the minor arc"),

	/**
	 * Fundamental leaking mode following P onsets generated by coupling of P energy
	 * into the waveguide formed by the crust and upper mantle
	 */
	PL(GeoAttributes.PSLOWNESS,
			"Fundamental leaking mode following P onsets generated by coupling of"
					+ "<BR>P energy into the waveguide formed by the crust and upper mantle"),

	/**
	 * S wave coupling into the PL waveguide
	 */
	SPL(null, "S wave coupling into the PL waveguide"),

	/**
	 * S wave reflected once from the free surface and coupling into the PL
	 * waveguide
	 */
	SSPL(null, "S wave reflected once from the free surface and coupling into the PL waveguide"),

	/**
	 * S wave reflected twice from the free surface and coupling into the PL
	 * waveguide
	 */
	SSSPL(null, "S wave reflected twice from the free surface and coupling into the PL waveguide"),

	/**
	 * A hydroacoustic wave from a source in the water, which couples in the ground
	 */
	H(null, "A hydroacoustic wave from a source in the water, which couples in the ground"),

	/**
	 * H phase converted to Pg at the receiver side
	 */
	HPg(GeoAttributes.PSLOWNESS, "H phase converted to Pg at the receiver side"),

	/**
	 * H phase converted to Sg at the receiver side
	 */
	HSg(GeoAttributes.SSLOWNESS, "H phase converted to Sg at the receiver side"),

	/**
	 * H phase converted to Rg at the receiver side
	 */
	HRg(null, "H phase converted to Rg at the receiver side"),

	/**
	 * Direct Infrasound wave
	 */
	I(null, "Direct Infrasound wave"),

	/**
	 * Infrasound tropospheric ducted wave with a turing height of <15-20 km
	 */
	Iw(null, "Infrasound tropospheric ducted wave with a turing height of <15-20 km"),

	/**
	 * Infrasound stratospheric ducted wave with a turning height of < 60 km
	 */
	Is(null, "Infrasound stratospheric ducted wave with a turning height of < 60 km"),

	/**
	 * Infrasound thermospheric ducted wave with a turning height of < 120 km
	 */
	It(null, "Infrasound thermospheric ducted wave with a turning height of < 120 km"),

	/**
	 * I phase converted to Pg at the receiver side
	 */
	IPg(GeoAttributes.PSLOWNESS, "I phase converted to Pg at the receiver side"),

	/**
	 * I phase converted to Sg at the receiver side
	 */
	ISg(GeoAttributes.SSLOWNESS, "I phase converted to Sg at the receiver side"),

	/**
	 * I phase converted to Rg at the receiver side
	 */
	IRg(null, "I phase converted to Rg at the receiver side"),

	/**
	 * A tertiary wave. This is an acoustic wave from a source in the solid earth,
	 * usually trapped in a low velocity oceanic water layer called the SOFAR
	 * channel (SOund Fixing And Ranging)
	 */
	T(null, "A tertiary wave. This is an acoustic wave from a source in the solid"
			+ "<BR>earth, usually trapped in a low velocity oceanic water layer called the"
			+ "<BR>SOFAR channel (SOund Fixing And Ranging)"),

	/**
	 * T phase converted to Pg at the receiver side
	 */
	TPg(GeoAttributes.PSLOWNESS, "T phase converted to Pg at the receiver side"),

	/**
	 * T phase converted to Sg at the receiver side
	 */
	TSg(GeoAttributes.SSLOWNESS, "T phase converted to Sg at the receiver side"),

	/**
	 * T phase converted to Rg at the receiver side
	 */
	TRg(null, "T phase converted to Rg at the receiver side"),

	/**
	 * Unspecified amplitude measurement
	 */
	A(null, "Unspecified amplitude measurement"),

	/**
	 * Amplitude measurement for local magnitude
	 */
	AML(null, "Amplitude measurement for local magnitude"),

	/**
	 * Amplitude measurement for body wave magnitude
	 */
	AMB(null, "Amplitude measurement for body wave magnitude"),

	/**
	 * Amplitude measurement for surface wave magnitude
	 */
	AMS(GeoAttributes.SSLOWNESS, "Amplitude measurement for surface wave magnitude"),

	/**
	 * Time of visible end of record for duration magnitude
	 */
	END(null, "Time of visible end of record for duration magnitude"),

	/**
	 * (old: i,e,NULL) unidentified arrival
	 */
	x(null, "(old: i,e,NULL) unidentified arrival"),

	/**
	 * (old: i,e,NULL) unidentified regional arrival
	 */
	rx(null, "(old: i,e,NULL) unidentified regional arrival"),

	/**
	 * (old: i,e,NULL) unidentified teleseismic arrival
	 */
	tx(null, "(old: i,e,NULL) unidentified teleseismic arrival"),

	/**
	 * P wave reflected 2 times from inner side of the CMB; bc indicates the
	 * prograde branch of the PKP caustic
	 */
	P3KPbc(GeoAttributes.PSLOWNESS,
			"P wave reflected 2 times from inner side of the CMB; bc indicates"
					+ "<BR>the prograde branch of the PKP caustic"),

	/**
	 * P wave reflected 3 times from inner side of the CMB; bc indicates the
	 * prograde branch of the PKP caustic
	 */
	P4KPbc(GeoAttributes.PSLOWNESS,
			"P wave reflected 3 times from inner side of the CMB; bc indicates"
					+ "<BR>the prograde branch of the PKP caustic"),

	/**
	 * (alternates: P'P', PKPPKP, P'2) Free surface reflection of PKP
	 */
	PKP2(GeoAttributes.PSLOWNESS, "(alternates: P'P', PKPPKP, P'2) Free surface reflection of PKP"),

	/**
	 * (alternates: P'P', PKPPKP, PKP2) Free surface reflection of PKP
	 */
	P_prime_2(GeoAttributes.PSLOWNESS, "(alternates: P'P', PKPPKP, PKP2) Free surface reflection of PKP"),

	/**
	 * (alternates: PKPPKPbc, PKP2bc, P'2bc) Free surface reflection of PKP; bc
	 * indicates the prograde branch of the PKP caustic
	 */
	P_prime_P_prime_bc(GeoAttributes.PSLOWNESS,
			"(alternates: PKPPKPbc, PKP2bc, P'2bc) Free surface reflection of PKP;"
					+ "<BR>bc indicates the prograde branch of the PKP caustic"),

	/**
	 * (alternates: P'P'bc, PKP2bc, P'2bc) Free surface reflection of PKP; bc
	 * indicates the prograde branch of the PKP caustic
	 */
	PKPPKPbc(GeoAttributes.PSLOWNESS,
			"(alternates: P'P'bc, PKP2bc, P'2bc) Free surface reflection of PKP;"
					+ "<BR>bc indicates the prograde branch of the PKP caustic"),

	/**
	 * (alternates: P'P'bc, PKPPKPbc, P'2bc) Free surface reflection of PKP; bc
	 * indicates the prograde branch of the PKP caustic
	 */
	PKP2bc(GeoAttributes.PSLOWNESS,
			"(alternates: P'P'bc, PKPPKPbc, P'2bc) Free surface reflection of PKP;"
					+ "<BR>bc indicates the prograde branch of the PKP caustic"),

	/**
	 * (alternates: P'P'bc, PKPPKPbc, PKP2bc) Free surface reflection of PKP; bc
	 * indicates the prograde branch of the PKP caustic
	 */
	P_prime_2bc(GeoAttributes.PSLOWNESS,
			"(alternates: P'P'bc, PKPPKPbc, PKP2bc) Free surface reflection of PKP;"
					+ "<BR>bc indicates the prograde branch of the PKP caustic"),

	/**
	 * (alternates: PKPPKPab, PKP2ab, P'2ab) Free surface reflection of PKP; ab
	 * indicates the retrograde branch of the PKP caustic
	 */
	P_prime_P_prime_ab(GeoAttributes.PSLOWNESS,
			"(alternates: PKPPKPab, PKP2ab, P'2ab) Free surface reflection of PKP;"
					+ "<BR>ab indicates the retrograde branch of the PKP caustic"),

	/**
	 * (alternates: P'P'ab, PKP2ab, P'2ab) Free surface reflection of PKP; ab
	 * indicates the retrograde branch of the PKP caustic
	 */
	PKPPKPab(GeoAttributes.PSLOWNESS,
			"(alternates: P'P'ab, PKP2ab, P'2ab) Free surface reflection of PKP;"
					+ "<BR>ab indicates the retrograde branch of the PKP caustic"),

	/**
	 * (alternates: P'P'ab, PKPPKPab, P'2ab) Free surface reflection of PKP; ab
	 * indicates the retrograde branch of the PKP caustic
	 */
	PKP2ab(GeoAttributes.PSLOWNESS,
			"(alternates: P'P'ab, PKPPKPab, P'2ab) Free surface reflection of PKP;"
					+ "<BR>ab indicates the retrograde branch of the PKP caustic"),

	/**
	 * (alternates: P'P'ab, PKPPKPab, PKP2ab) Free surface reflection of PKP; ab
	 * indicates the retrograde branch of the PKP caustic
	 */
	P_prime_2ab(GeoAttributes.PSLOWNESS,
			"(alternates: P'P'ab, PKPPKPab, PKP2ab) Free surface reflection of PKP;"
					+ "<BR>ab indicates the retrograde branch of the PKP caustic"),

	/**
	 * S wave traversing the outer core as P and reflected from the ICB and
	 * continuing from the core to the receiver as P
	 */
	SKiKP("SSLOWNESS, CMB, PSLOWNESS", "BOTTOM, ICB",
			"(Alternate SKPdf) S wave traversing the outer core as P and reflected from the ICB and"
					+ "<BR>continuing from the core to the receiver as P"),

	/**
	 * unidentified arrival of infrasound type
	 */
	Ix(null, "unidentified arrival of infrasound type"),

	/**
	 * SKS wave with a segment of mantle side Pdif at the source side of the
	 * raypath. Note that this is not a full Pdif path, but just the section along
	 * the CMB. See Kind R., G. Muller, "Computations of SV waves in realistic Earth
	 * models", J.Geophys 41, 149-172, 1975 and Garnero, E. J., D. V. Helmberger,
	 * "Seismic detection of a thin laterally varying boundary layer at the base of
	 * the mantle beneath the central-Pacific", Geophys. Res. Lett., 23(9), 977-980,
	 * 10.1029/95GL03603, 1996.
	 */
	SPdifKS(null,
			"SKS wave with a segment of mantle side Pdif at the source side of the"
					+ "<BR>raypath. Note that this is not a full Pdif path, but just the section along"
					+ "<BR>the CMB. See Kind R., G. Muller, \"Computations of SV waves in realistic"
					+ "<BR>Earth models\", J.Geophys 41, 149-172, 1975 and Garnero, E. J., D. V."
					+ "<BR>Helmberger, \"Seismic detection of a thin laterally varying boundary layer"
					+ "<BR>at the base of the mantle beneath the central-Pacific\", Geophys. Res."
					+ "<BR>Lett., 23(9), 977-980, 10.1029/95GL03603, 1996."),

	/**
	 * SKS wave with a segment of mantle side Pdif at the receiver side of the
	 * raypath. Note that this is not a full Pdif path, but just the section along
	 * the CMB. See Kind R., G. Muller, "Computations of SV waves in realistic Earth
	 * models", J.Geophys 41, 149-172, 1975 and Garnero, E. J., D. V. Helmberger,
	 * "Seismic detection of a thin laterally varying boundary layer at the base of
	 * the mantle beneath the central-Pacific", Geophys. Res. Lett., 23(9), 977-980,
	 * 10.1029/95GL03603, 1996.
	 */
	SKPdifS(null,
			"SKS wave with a segment of mantle side Pdif at the receiver side of the"
					+ "<BR>raypath. Note that this is not a full Pdif path, but just the section along"
					+ "<BR>the CMB. See Kind R., G. Muller, \"Computations of SV waves in realistic"
					+ "<BR>Earth models\", J.Geophys 41, 149-172, 1975 and Garnero, E. J., D. V."
					+ "<BR>Helmberger, \"Seismic detection of a thin laterally varying boundary layer"
					+ "<BR>at the base of the mantle beneath the central-Pacific\", Geophys. Res."
					+ "<BR>Lett., 23(9), 977-980, 10.1029/95GL03603, 1996."),

	/**
	 * noise pick at predicted time for P
	 */
	nP(GeoAttributes.PSLOWNESS, "noise pick at predicted time for P"),

	/**
	 * noise pick at predicted time for LR
	 */
	nLR(null, "noise pick at predicted time for LR"),

	/**
	 * noise pick, corresponding phase not specified
	 */
	nNL(null, "noise pick, corresponding phase not specified"),

	/**
	 * noise
	 */
	N(null, "noise"),

	/**
	 * infrasound: direct Lamb wave
	 */
	LW(null, "infrasound: direct Lamb wave"),

	/**
	 * infrasound: ionospheric path
	 */
	IONO(null, "infrasound: ionospheric path"),

	/**
	 * infrasound: stratospheric path
	 */
	STRATO(GeoAttributes.SSLOWNESS, "infrasound: stratospheric path"),

	/**
	 * infrasound: 50 km elevation path
	 */
	_50(null, "infrasound: 50 km elevation path"),

	/**
	 * infrasound: 100 km elevation path
	 */
	_100(null, "infrasound: 100 km elevation path"),

	/**
	 * P wave reflected twice from inner side of the CMB and converted to S at the
	 * CMB
	 */
	P3KS(null, "P wave reflected twice from inner side of the CMB and converted to S at the CMB"),

	/**
	 * P wave reflected 3x from inner side of the CMB and converted to S at the CMB
	 */
	P4KS(null, "P wave reflected 3x from inner side of the CMB and converted to S at the CMB"),

	/**
	 * P wave reflected 4x from inner side of the CMB and converted to S at the CMB
	 */
	P5KS(null, "P wave reflected 4x from inner side of the CMB and converted to S at the CMB"),

	/**
	 * P wave reflected 5x from inner side of the CMB and converted to S at the CMB
	 */
	P6KS(null, "P wave reflected 5x from inner side of the CMB and converted to S at the CMB"),

	/**
	 * P wave reflected 6x from inner side of the CMB and converted to S at the CMB
	 */
	P7KS(null, "P wave reflected 6x from inner side of the CMB and converted to S at the CMB"),

	/**
	 * PKKP reflected from the free surface once
	 */
	PKKP2(GeoAttributes.PSLOWNESS, "PKKP reflected from the free surface once"),

	/**
	 * PKKP reflected from the free surface 2 times
	 */
	PKKP3(GeoAttributes.PSLOWNESS, "PKKP reflected from the free surface 2 times"),

	/**
	 * PKKP reflected from the free surface 3 times
	 */
	PKKP4(GeoAttributes.PSLOWNESS, "PKKP reflected from the free surface 3 times"),

	/**
	 * PKKP reflected from the free surface 4 times
	 */
	PKKP5(GeoAttributes.PSLOWNESS, "PKKP reflected from the free surface 4 times"),

	/**
	 * PKKP reflected from the free surface 5 times
	 */
	PKKP6(GeoAttributes.PSLOWNESS, "PKKP reflected from the free surface 5 times"),

	/**
	 * PKKP reflected from the free surface 6 times
	 */
	PKKP7(GeoAttributes.PSLOWNESS, "PKKP reflected from the free surface 6 times"),

	/**
	 * S convert to P at the CMB and reflected 2x from the inner side of the CMB
	 */
	S3KP(null, "S convert to P at the CMB and reflected 2x from the inner side of the CMB"),

	/**
	 * S convert to P at the CMB and reflected 3x from the inner side of the CMB
	 */
	S4KP(null, "S convert to P at the CMB and reflected 3x from the inner side of the CMB"),

	/**
	 * S convert to P at the CMB and reflected 4x from the inner side of the CMB
	 */
	S5KP(null, "S convert to P at the CMB and reflected 4x from the inner side of the CMB"),

	/**
	 * S convert to P at the CMB and reflected 5x from the inner side of the CMB
	 */
	S6KP(null, "S convert to P at the CMB and reflected 5x from the inner side of the CMB"),

	/**
	 * S convert to P at the CMB and reflected 6x from the inner side of the CMB
	 */
	S7KP(null, "S convert to P at the CMB and reflected 6x from the inner side of the CMB"),

	/**
	 * PKP3 bottoming in the upper outer core
	 */
	PKP3ab(GeoAttributes.PSLOWNESS, "PKP3 bottoming in the upper outer core"),

	/**
	 * PKP4 bottoming in the upper outer core
	 */
	PKP4ab(GeoAttributes.PSLOWNESS, "PKP4 bottoming in the upper outer core"),

	/**
	 * PKP5 bottoming in the upper outer core
	 */
	PKP5ab(GeoAttributes.PSLOWNESS, "PKP5 bottoming in the upper outer core"),

	/**
	 * PKP6 bottoming in the upper outer core
	 */
	PKP6ab(GeoAttributes.PSLOWNESS, "PKP6 bottoming in the upper outer core"),

	/**
	 * PKP7 bottoming in the upper outer core
	 */
	PKP7ab(GeoAttributes.PSLOWNESS, "PKP7 bottoming in the upper outer core"),

	/**
	 * PKP3 bottoming in the lower outer core
	 */
	PKP3bc(GeoAttributes.PSLOWNESS, "PKP3 bottoming in the lower outer core"),

	/**
	 * PKP4 bottoming in the lower outer core
	 */
	PKP4bc(GeoAttributes.PSLOWNESS, "PKP4 bottoming in the lower outer core"),

	/**
	 * PKP5 bottoming in the lower outer core
	 */
	PKP5bc(GeoAttributes.PSLOWNESS, "PKP5 bottoming in the lower outer core"),

	/**
	 * PKP6 bottoming in the lower outer core
	 */
	PKP6bc(GeoAttributes.PSLOWNESS, "PKP6 bottoming in the lower outer core"),

	/**
	 * PKP7 bottoming in the lower outer core
	 */
	PKP7bc(GeoAttributes.PSLOWNESS, "PKP7 bottoming in the lower outer core"),

	/**
	 * PKP3 bottoming in the inner core
	 */
	PKP3df(GeoAttributes.PSLOWNESS, "PKP3 bottoming in the inner core"),

	/**
	 * PKP4 bottoming in the inner core
	 */
	PKP4df(GeoAttributes.PSLOWNESS, "PKP4 bottoming in the inner core"),

	/**
	 * PKP5 bottoming in the inner core
	 */
	PKP5df(GeoAttributes.PSLOWNESS, "PKP5 bottoming in the inner core"),

	/**
	 * PKP6 bottoming in the inner core
	 */
	PKP6df(GeoAttributes.PSLOWNESS, "PKP6 bottoming in the inner core"),

	/**
	 * PKP7 bottoming in the inner core
	 */
	PKP7df(GeoAttributes.PSLOWNESS, "PKP7 bottoming in the inner core"),

	/**
	 * P3KP bottoming in the upper outer core
	 */
	P3KPab(GeoAttributes.PSLOWNESS, "P3KP bottoming in the upper outer core"),

	/**
	 * P4KP bottoming in the upper outer core
	 */
	P4KPab(GeoAttributes.PSLOWNESS, "P4KP bottoming in the upper outer core"),

	/**
	 * P5KP bottoming in the upper outer core
	 */
	P5KPab(GeoAttributes.PSLOWNESS, "P5KP bottoming in the upper outer core"),

	/**
	 * P6KP bottoming in the upper outer core
	 */
	P6KPab(GeoAttributes.PSLOWNESS, "P6KP bottoming in the upper outer core"),

	/**
	 * P7KP bottoming in the upper outer core
	 */
	P7KPab(GeoAttributes.PSLOWNESS, "P7KP bottoming in the upper outer core"),

	/**
	 * P5KP bottoming in the lower outer core
	 */
	P5KPbc(GeoAttributes.PSLOWNESS, "P5KP bottoming in the lower outer core"),

	/**
	 * P6KP bottoming in the lower outer core
	 */
	P6KPbc(GeoAttributes.PSLOWNESS, "P6KP bottoming in the lower outer core"),

	/**
	 * P7KP bottoming in the lower outer core
	 */
	P7KPbc(GeoAttributes.PSLOWNESS, "P7KP bottoming in the lower outer core"),

	/**
	 * P3KP bottoming in the inner core
	 */
	P3KPdf(GeoAttributes.PSLOWNESS, "P3KP bottoming in the inner core"),

	/**
	 * P4KP bottoming in the inner core
	 */
	P4KPdf(GeoAttributes.PSLOWNESS, "P4KP bottoming in the inner core"),

	/**
	 * P5KP bottoming in the inner core
	 */
	P5KPdf(GeoAttributes.PSLOWNESS, "P5KP bottoming in the inner core"),

	/**
	 * P6KP bottoming in the inner core
	 */
	P6KPdf(GeoAttributes.PSLOWNESS, "P6KP bottoming in the inner core"),

	/**
	 * P7KP bottoming in the inner core
	 */
	P7KPdf(GeoAttributes.PSLOWNESS, "P7KP bottoming in the inner core"),

	/**
	 * P3KS resulting from reflection of upgoing P at the free surface
	 */
	pP3KS(null, "P3KS resulting from reflection of upgoing P at the free surface"),

	/**
	 * P4KS resulting from reflection of upgoing P at the free surface
	 */
	pP4KS(null, "P4KS resulting from reflection of upgoing P at the free surface"),

	/**
	 * P5KS resulting from reflection of upgoing P at the free surface
	 */
	pP5KS(null, "P5KS resulting from reflection of upgoing P at the free surface"),

	/**
	 * P6KS resulting from reflection of upgoing P at the free surface
	 */
	pP6KS(null, "P6KS resulting from reflection of upgoing P at the free surface"),

	/**
	 * P7KS resulting from reflection of upgoing P at the free surface
	 */
	pP7KS(null, "P7KS resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKKP2 resulting from reflection of upgoing P at the free surface
	 */
	pPKKP2(GeoAttributes.PSLOWNESS, "PKKP2 resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKKP3 resulting from reflection of upgoing P at the free surface
	 */
	pPKKP3(GeoAttributes.PSLOWNESS, "PKKP3 resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKKP4 resulting from reflection of upgoing P at the free surface
	 */
	pPKKP4(GeoAttributes.PSLOWNESS, "PKKP4 resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKKP5 resulting from reflection of upgoing P at the free surface
	 */
	pPKKP5(GeoAttributes.PSLOWNESS, "PKKP5 resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKKP6 resulting from reflection of upgoing P at the free surface
	 */
	pPKKP6(GeoAttributes.PSLOWNESS, "PKKP6 resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKKP7 resulting from reflection of upgoing P at the free surface
	 */
	pPKKP7(GeoAttributes.PSLOWNESS, "PKKP7 resulting from reflection of upgoing P at the free surface"),

	/**
	 * S3KP resulting from converted reflection of upgoing P at the free surface
	 */
	pS3KP(null, "S3KP resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * S4KP resulting from converted reflection of upgoing P at the free surface
	 */
	pS4KP(null, "S4KP resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * S5KP resulting from converted reflection of upgoing P at the free surface
	 */
	pS5KP(null, "S5KP resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * S6KP resulting from converted reflection of upgoing P at the free surface
	 */
	pS6KP(null, "S6KP resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * S7KP resulting from converted reflection of upgoing P at the free surface
	 */
	pS7KP(null, "S7KP resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * PKP2ab resulting from reflection of upgoing P at the free surface
	 */
	pPKP2ab(GeoAttributes.PSLOWNESS, "PKP2ab resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKP3ab resulting from reflection of upgoing P at the free surface
	 */
	pPKP3ab(GeoAttributes.PSLOWNESS, "PKP3ab resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKP4ab resulting from reflection of upgoing P at the free surface
	 */
	pPKP4ab(GeoAttributes.PSLOWNESS, "PKP4ab resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKP5ab resulting from reflection of upgoing P at the free surface
	 */
	pPKP5ab(GeoAttributes.PSLOWNESS, "PKP5ab resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKP6ab resulting from reflection of upgoing P at the free surface
	 */
	pPKP6ab(GeoAttributes.PSLOWNESS, "PKP6ab resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKP7ab resulting from reflection of upgoing P at the free surface
	 */
	pPKP7ab(GeoAttributes.PSLOWNESS, "PKP7ab resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKP2bc resulting from reflection of upgoing P at the free surface
	 */
	pPKP2bc(GeoAttributes.PSLOWNESS, "PKP2bc resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKP3bc resulting from reflection of upgoing P at the free surface
	 */
	pPKP3bc(GeoAttributes.PSLOWNESS, "PKP3bc resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKP4bc resulting from reflection of upgoing P at the free surface
	 */
	pPKP4bc(GeoAttributes.PSLOWNESS, "PKP4bc resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKP5bc resulting from reflection of upgoing P at the free surface
	 */
	pPKP5bc(GeoAttributes.PSLOWNESS, "PKP5bc resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKP6bc resulting from reflection of upgoing P at the free surface
	 */
	pPKP6bc(GeoAttributes.PSLOWNESS, "PKP6bc resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKP7bc resulting from reflection of upgoing P at the free surface
	 */
	pPKP7bc(GeoAttributes.PSLOWNESS, "PKP7bc resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKP2df resulting from reflection of upgoing P at the free surface
	 */
	pPKP2df(GeoAttributes.PSLOWNESS, "PKP2df resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKP3df resulting from reflection of upgoing P at the free surface
	 */
	pPKP3df(GeoAttributes.PSLOWNESS, "PKP3df resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKP4df resulting from reflection of upgoing P at the free surface
	 */
	pPKP4df(GeoAttributes.PSLOWNESS, "PKP4df resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKP5df resulting from reflection of upgoing P at the free surface
	 */
	pPKP5df(GeoAttributes.PSLOWNESS, "PKP5df resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKP6df resulting from reflection of upgoing P at the free surface
	 */
	pPKP6df(GeoAttributes.PSLOWNESS, "PKP6df resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKP7df resulting from reflection of upgoing P at the free surface
	 */
	pPKP7df(GeoAttributes.PSLOWNESS, "PKP7df resulting from reflection of upgoing P at the free surface"),

	/**
	 * P3KPab resulting from reflection of upgoing P at the free surface
	 */
	pP3KPab(GeoAttributes.PSLOWNESS, "P3KPab resulting from reflection of upgoing P at the free surface"),

	/**
	 * P4KPab resulting from reflection of upgoing P at the free surface
	 */
	pP4KPab(GeoAttributes.PSLOWNESS, "P4KPab resulting from reflection of upgoing P at the free surface"),

	/**
	 * P5KPab resulting from reflection of upgoing P at the free surface
	 */
	pP5KPab(GeoAttributes.PSLOWNESS, "P5KPab resulting from reflection of upgoing P at the free surface"),

	/**
	 * P6KPab resulting from reflection of upgoing P at the free surface
	 */
	pP6KPab(GeoAttributes.PSLOWNESS, "P6KPab resulting from reflection of upgoing P at the free surface"),

	/**
	 * P7KPab resulting from reflection of upgoing P at the free surface
	 */
	pP7KPab(GeoAttributes.PSLOWNESS, "P7KPab resulting from reflection of upgoing P at the free surface"),

	/**
	 * P3KPbc resulting from reflection of upgoing P at the free surface
	 */
	pP3KPbc(GeoAttributes.PSLOWNESS, "P3KPbc resulting from reflection of upgoing P at the free surface"),

	/**
	 * P4KPbc resulting from reflection of upgoing P at the free surface
	 */
	pP4KPbc(GeoAttributes.PSLOWNESS, "P4KPbc resulting from reflection of upgoing P at the free surface"),

	/**
	 * P5KPbc resulting from reflection of upgoing P at the free surface
	 */
	pP5KPbc(GeoAttributes.PSLOWNESS, "P5KPbc resulting from reflection of upgoing P at the free surface"),

	/**
	 * P6KPbc resulting from reflection of upgoing P at the free surface
	 */
	pP6KPbc(GeoAttributes.PSLOWNESS, "P6KPbc resulting from reflection of upgoing P at the free surface"),

	/**
	 * P7KPbc resulting from reflection of upgoing P at the free surface
	 */
	pP7KPbc(GeoAttributes.PSLOWNESS, "P7KPbc resulting from reflection of upgoing P at the free surface"),

	/**
	 * P3KPdf resulting from reflection of upgoing P at the free surface
	 */
	pP3KPdf(GeoAttributes.PSLOWNESS, "P3KPdf resulting from reflection of upgoing P at the free surface"),

	/**
	 * P4KPdf resulting from reflection of upgoing P at the free surface
	 */
	pP4KPdf(GeoAttributes.PSLOWNESS, "P4KPdf resulting from reflection of upgoing P at the free surface"),

	/**
	 * P5KPdf resulting from reflection of upgoing P at the free surface
	 */
	pP5KPdf(GeoAttributes.PSLOWNESS, "P5KPdf resulting from reflection of upgoing P at the free surface"),

	/**
	 * P6KPdf resulting from reflection of upgoing P at the free surface
	 */
	pP6KPdf(GeoAttributes.PSLOWNESS, "P6KPdf resulting from reflection of upgoing P at the free surface"),

	/**
	 * P7KPdf resulting from reflection of upgoing P at the free surface
	 */
	pP7KPdf(GeoAttributes.PSLOWNESS, "P7KPdf resulting from reflection of upgoing P at the free surface"),

	/**
	 * P3KS resulting from converted reflection of upgoing S at the free surface
	 */
	sP3KS(null, "P3KS resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P4KS resulting from converted reflection of upgoing S at the free surface
	 */
	sP4KS(null, "P4KS resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P5KS resulting from converted reflection of upgoing S at the free surface
	 */
	sP5KS(null, "P5KS resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P6KS resulting from converted reflection of upgoing S at the free surface
	 */
	sP6KS(null, "P6KS resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P7KS resulting from converted reflection of upgoing S at the free surface
	 */
	sP7KS(null, "P7KS resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKKP2 resulting from converted reflection of upgoing S at the free surface
	 */
	sPKKP2(null, "PKKP2 resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKKP3 resulting from converted reflection of upgoing S at the free surface
	 */
	sPKKP3(null, "PKKP3 resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKKP4 resulting from converted reflection of upgoing S at the free surface
	 */
	sPKKP4(null, "PKKP4 resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKKP5 resulting from converted reflection of upgoing S at the free surface
	 */
	sPKKP5(null, "PKKP5 resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKKP6 resulting from converted reflection of upgoing S at the free surface
	 */
	sPKKP6(null, "PKKP6 resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKKP7 resulting from converted reflection of upgoing S at the free surface
	 */
	sPKKP7(null, "PKKP7 resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * S3KP resulting from reflection of upgoing S at the free surface
	 */
	sS3KP(null, "S3KP resulting from reflection of upgoing S at the free surface"),

	/**
	 * S4KP resulting from reflection of upgoing S at the free surface
	 */
	sS4KP(null, "S4KP resulting from reflection of upgoing S at the free surface"),

	/**
	 * S5KP resulting from reflection of upgoing S at the free surface
	 */
	sS5KP(null, "S5KP resulting from reflection of upgoing S at the free surface"),

	/**
	 * S6KP resulting from reflection of upgoing S at the free surface
	 */
	sS6KP(null, "S6KP resulting from reflection of upgoing S at the free surface"),

	/**
	 * S7KP resulting from reflection of upgoing S at the free surface
	 */
	sS7KP(null, "S7KP resulting from reflection of upgoing S at the free surface"),

	/**
	 * PKP2ab resulting from converted reflection of upgoing S at the free surface
	 */
	sPKP2ab(null, "PKP2ab resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKP3ab resulting from converted reflection of upgoing S at the free surface
	 */
	sPKP3ab(null, "PKP3ab resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKP4ab resulting from converted reflection of upgoing S at the free surface
	 */
	sPKP4ab(null, "PKP4ab resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKP5ab resulting from converted reflection of upgoing S at the free surface
	 */
	sPKP5ab(null, "PKP5ab resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKP6ab resulting from converted reflection of upgoing S at the free surface
	 */
	sPKP6ab(null, "PKP6ab resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKP7ab resulting from converted reflection of upgoing S at the free surface
	 */
	sPKP7ab(null, "PKP7ab resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKP2bc resulting from converted reflection of upgoing S at the free surface
	 */
	sPKP2bc(null, "PKP2bc resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKP3bc resulting from converted reflection of upgoing S at the free surface
	 */
	sPKP3bc(null, "PKP3bc resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKP4bc resulting from converted reflection of upgoing S at the free surface
	 */
	sPKP4bc(null, "PKP4bc resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKP5bc resulting from converted reflection of upgoing S at the free surface
	 */
	sPKP5bc(null, "PKP5bc resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKP6bc resulting from converted reflection of upgoing S at the free surface
	 */
	sPKP6bc(null, "PKP6bc resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKP7bc resulting from converted reflection of upgoing S at the free surface
	 */
	sPKP7bc(null, "PKP7bc resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKP2df resulting from converted reflection of upgoing S at the free surface
	 */
	sPKP2df(null, "PKP2df resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKP3df resulting from converted reflection of upgoing S at the free surface
	 */
	sPKP3df(null, "PKP3df resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKP4df resulting from converted reflection of upgoing S at the free surface
	 */
	sPKP4df(null, "PKP4df resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKP5df resulting from converted reflection of upgoing S at the free surface
	 */
	sPKP5df(null, "PKP5df resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKP6df resulting from converted reflection of upgoing S at the free surface
	 */
	sPKP6df(null, "PKP6df resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKP7df resulting from converted reflection of upgoing S at the free surface
	 */
	sPKP7df(null, "PKP7df resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P3KPab resulting from converted reflection of upgoing S at the free surface
	 */
	sP3KPab(null, "P3KPab resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P4KPab resulting from converted reflection of upgoing S at the free surface
	 */
	sP4KPab(null, "P4KPab resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P5KPab resulting from converted reflection of upgoing S at the free surface
	 */
	sP5KPab(null, "P5KPab resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P6KPab resulting from converted reflection of upgoing S at the free surface
	 */
	sP6KPab(null, "P6KPab resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P7KPab resulting from converted reflection of upgoing S at the free surface
	 */
	sP7KPab(null, "P7KPab resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P3KPbc resulting from converted reflection of upgoing S at the free surface
	 */
	sP3KPbc(null, "P3KPbc resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P4KPbc resulting from converted reflection of upgoing S at the free surface
	 */
	sP4KPbc(null, "P4KPbc resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P5KPbc resulting from converted reflection of upgoing S at the free surface
	 */
	sP5KPbc(null, "P5KPbc resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P6KPbc resulting from converted reflection of upgoing S at the free surface
	 */
	sP6KPbc(null, "P6KPbc resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P7KPbc resulting from converted reflection of upgoing S at the free surface
	 */
	sP7KPbc(null, "P7KPbc resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P3KPdf resulting from converted reflection of upgoing S at the free surface
	 */
	sP3KPdf(null, "P3KPdf resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P4KPdf resulting from converted reflection of upgoing S at the free surface
	 */
	sP4KPdf(null, "P4KPdf resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P5KPdf resulting from converted reflection of upgoing S at the free surface
	 */
	sP5KPdf(null, "P5KPdf resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P6KPdf resulting from converted reflection of upgoing S at the free surface
	 */
	sP6KPdf(null, "P6KPdf resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P7KPdf resulting from converted reflection of upgoing S at the free surface
	 */
	sP7KPdf(null, "P7KPdf resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P3KS resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP3KS(null, "P3KS resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P4KS resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP4KS(null, "P4KS resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P5KS resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP5KS(null, "P5KS resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P6KS resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP6KS(null, "P6KS resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P7KS resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP7KS(null, "P7KS resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKKP2 resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKKP2(GeoAttributes.PSLOWNESS, "PKKP2 resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKKP3 resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKKP3(GeoAttributes.PSLOWNESS, "PKKP3 resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKKP4 resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKKP4(GeoAttributes.PSLOWNESS, "PKKP4 resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKKP5 resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKKP5(GeoAttributes.PSLOWNESS, "PKKP5 resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKKP6 resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKKP6(GeoAttributes.PSLOWNESS, "PKKP6 resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKKP7 resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKKP7(GeoAttributes.PSLOWNESS, "PKKP7 resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKP2ab resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKP2ab(GeoAttributes.PSLOWNESS, "PKP2ab resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKP3ab resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKP3ab(GeoAttributes.PSLOWNESS, "PKP3ab resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKP4ab resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKP4ab(GeoAttributes.PSLOWNESS, "PKP4ab resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKP5ab resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKP5ab(GeoAttributes.PSLOWNESS, "PKP5ab resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKP6ab resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKP6ab(GeoAttributes.PSLOWNESS, "PKP6ab resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKP7ab resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKP7ab(GeoAttributes.PSLOWNESS, "PKP7ab resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKP2bc resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKP2bc(GeoAttributes.PSLOWNESS, "PKP2bc resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKP3bc resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKP3bc(GeoAttributes.PSLOWNESS, "PKP3bc resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKP4bc resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKP4bc(GeoAttributes.PSLOWNESS, "PKP4bc resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKP5bc resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKP5bc(GeoAttributes.PSLOWNESS, "PKP5bc resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKP6bc resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKP6bc(GeoAttributes.PSLOWNESS, "PKP6bc resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKP7bc resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKP7bc(GeoAttributes.PSLOWNESS, "PKP7bc resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKP2df resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKP2df(GeoAttributes.PSLOWNESS, "PKP2df resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKP3df resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKP3df(GeoAttributes.PSLOWNESS, "PKP3df resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKP4df resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKP4df(GeoAttributes.PSLOWNESS, "PKP4df resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKP5df resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKP5df(GeoAttributes.PSLOWNESS, "PKP5df resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKP6df resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKP6df(GeoAttributes.PSLOWNESS, "PKP6df resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKP7df resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKP7df(GeoAttributes.PSLOWNESS, "PKP7df resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P3KPab resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP3KPab(GeoAttributes.PSLOWNESS, "P3KPab resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P4KPab resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP4KPab(GeoAttributes.PSLOWNESS, "P4KPab resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P5KPab resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP5KPab(GeoAttributes.PSLOWNESS, "P5KPab resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P6KPab resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP6KPab(GeoAttributes.PSLOWNESS, "P6KPab resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P7KPab resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP7KPab(GeoAttributes.PSLOWNESS, "P7KPab resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P3KPbc resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP3KPbc(GeoAttributes.PSLOWNESS, "P3KPbc resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P4KPbc resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP4KPbc(GeoAttributes.PSLOWNESS, "P4KPbc resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P5KPbc resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP5KPbc(GeoAttributes.PSLOWNESS, "P5KPbc resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P6KPbc resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP6KPbc(GeoAttributes.PSLOWNESS, "P6KPbc resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P7KPbc resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP7KPbc(GeoAttributes.PSLOWNESS, "P7KPbc resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P3KPdf resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP3KPdf(GeoAttributes.PSLOWNESS, "P3KPdf resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P4KPdf resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP4KPdf(GeoAttributes.PSLOWNESS, "P4KPdf resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P5KPdf resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP5KPdf(GeoAttributes.PSLOWNESS, "P5KPdf resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P6KPdf resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP6KPdf(GeoAttributes.PSLOWNESS, "P6KPdf resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P7KPdf resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP7KPdf(GeoAttributes.PSLOWNESS, "P7KPdf resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P3KS resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP3KS(null, "P3KS resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P4KS resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP4KS(null, "P4KS resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P5KS resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP5KS(null, "P5KS resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P6KS resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP6KS(null, "P6KS resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P7KS resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP7KS(null, "P7KS resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKKP2 resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKKP2(GeoAttributes.PSLOWNESS, "PKKP2 resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKKP3 resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKKP3(GeoAttributes.PSLOWNESS, "PKKP3 resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKKP4 resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKKP4(GeoAttributes.PSLOWNESS, "PKKP4 resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKKP5 resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKKP5(GeoAttributes.PSLOWNESS, "PKKP5 resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKKP6 resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKKP6(GeoAttributes.PSLOWNESS, "PKKP6 resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKKP7 resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKKP7(GeoAttributes.PSLOWNESS, "PKKP7 resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKP2ab resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKP2ab(GeoAttributes.PSLOWNESS, "PKP2ab resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKP3ab resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKP3ab(GeoAttributes.PSLOWNESS, "PKP3ab resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKP4ab resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKP4ab(GeoAttributes.PSLOWNESS, "PKP4ab resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKP5ab resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKP5ab(GeoAttributes.PSLOWNESS, "PKP5ab resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKP6ab resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKP6ab(GeoAttributes.PSLOWNESS, "PKP6ab resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKP7ab resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKP7ab(GeoAttributes.PSLOWNESS, "PKP7ab resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKP2bc resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKP2bc(GeoAttributes.PSLOWNESS, "PKP2bc resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKP3bc resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKP3bc(GeoAttributes.PSLOWNESS, "PKP3bc resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKP4bc resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKP4bc(GeoAttributes.PSLOWNESS, "PKP4bc resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKP5bc resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKP5bc(GeoAttributes.PSLOWNESS, "PKP5bc resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKP6bc resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKP6bc(GeoAttributes.PSLOWNESS, "PKP6bc resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKP7bc resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKP7bc(GeoAttributes.PSLOWNESS, "PKP7bc resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKP2df resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKP2df(GeoAttributes.PSLOWNESS, "PKP2df resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKP3df resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKP3df(GeoAttributes.PSLOWNESS, "PKP3df resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKP4df resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKP4df(GeoAttributes.PSLOWNESS, "PKP4df resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKP5df resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKP5df(GeoAttributes.PSLOWNESS, "PKP5df resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKP6df resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKP6df(GeoAttributes.PSLOWNESS, "PKP6df resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKP7df resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKP7df(GeoAttributes.PSLOWNESS, "PKP7df resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P3KPab resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP3KPab(GeoAttributes.PSLOWNESS, "P3KPab resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P4KPab resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP4KPab(GeoAttributes.PSLOWNESS, "P4KPab resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P5KPab resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP5KPab(GeoAttributes.PSLOWNESS, "P5KPab resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P6KPab resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP6KPab(GeoAttributes.PSLOWNESS, "P6KPab resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P7KPab resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP7KPab(GeoAttributes.PSLOWNESS, "P7KPab resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P3KPbc resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP3KPbc(GeoAttributes.PSLOWNESS, "P3KPbc resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P4KPbc resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP4KPbc(GeoAttributes.PSLOWNESS, "P4KPbc resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P5KPbc resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP5KPbc(GeoAttributes.PSLOWNESS, "P5KPbc resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P6KPbc resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP6KPbc(GeoAttributes.PSLOWNESS, "P6KPbc resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P7KPbc resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP7KPbc(GeoAttributes.PSLOWNESS, "P7KPbc resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P3KPdf resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP3KPdf(GeoAttributes.PSLOWNESS, "P3KPdf resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P4KPdf resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP4KPdf(GeoAttributes.PSLOWNESS, "P4KPdf resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P5KPdf resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP5KPdf(GeoAttributes.PSLOWNESS, "P5KPdf resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P6KPdf resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP6KPdf(GeoAttributes.PSLOWNESS, "P6KPdf resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P7KPdf resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP7KPdf(GeoAttributes.PSLOWNESS, "P7KPdf resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * SKiKP resulting from converted reflection of upgoing P at the free surface
	 */
	pSKiKP(null, "SKiKP resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKiKP resulting from reflection of upgoing S at the free surface
	 */
	sSKiKP(null, "SKiKP resulting from reflection of upgoing S at the free surface"),

	/**
	 * (alternate: PKP) unspecified P wave bottoming in the core
	 */
	P_prime_(GeoAttributes.PSLOWNESS, "(alternate: PKP) unspecified P wave bottoming in the core"),

	/**
	 * (alternate: PKKPbc) P wave reflected 1 time from inner side of the CMB; bc
	 * indicates the prograde branch of the PKP caustic
	 */
	P2KPbc(GeoAttributes.PSLOWNESS,
			"(alternate: PKKPbc) P wave reflected 1 time from inner side of the CMB;"
					+ "<BR>bc indicates the prograde branch of the PKP caustic"),

	/**
	 * (alternate: PKKPab) P2KP bottoming in the upper outer core
	 */
	P2KPab(GeoAttributes.PSLOWNESS, "(alternate: PKKPab) P2KP bottoming in the upper outer core"),

	/**
	 * (alternate: PKKPdf) P2KP bottoming in the inner core
	 */
	P2KPdf(GeoAttributes.PSLOWNESS, "(alternate: PKKPdf) P2KP bottoming in the inner core"),

	/**
	 * (alternates: PKPPKPdf, PKP2df, P'2df) Free surface reflection of PKP; df
	 * indicates the branch of PKP bottoming in the inner core
	 */
	P_prime_P_prime_df(GeoAttributes.PSLOWNESS,
			"(alternates: PKPPKPdf, PKP2df, P'2df) Free surface reflection of PKP;"
					+ "<BR>df indicates the branch of PKP bottoming in the inner core"),

	/**
	 * (alternates: P'P'df, PKP2df, P'2df) Free surface reflection of PKP; df
	 * indicates the branch of PKP bottoming in the inner core
	 */
	PKPPKPdf(GeoAttributes.PSLOWNESS,
			"(alternates: P'P'df, PKP2df, P'2df) Free surface reflection of PKP;"
					+ "<BR>df indicates the branch of PKP bottoming in the inner core"),

	/**
	 * (alternates: P'P'df, PKPPKPdf, P'2df) Free surface reflection of PKP; df
	 * indicates the branch of PKP bottoming in the inner core
	 */
	PKP2df(GeoAttributes.PSLOWNESS,
			"(alternates: P'P'df, PKPPKPdf, P'2df) Free surface reflection of PKP;"
					+ "<BR>df indicates the branch of PKP bottoming in the inner core"),

	/**
	 * (alternates: P'P'df, PKPPKPdf, PKP2df) Free surface reflection of PKP; df
	 * indicates the branch of PKP bottoming in the inner core
	 */
	P_prime_2df(GeoAttributes.PSLOWNESS,
			"(alternates: P'P'df, PKPPKPdf, PKP2df) Free surface reflection of PKP;"
					+ "<BR>df indicates the branch of PKP bottoming in the inner core"),

	/**
	 * (alternate: PKKS) P wave reflected once from inner side of the CMB and
	 * converted to S at the CMB
	 */
	P2KS(null,
			"(alternate: PKKS) P wave reflected once from inner side of the CMB and" + "<BR>converted to S at the CMB"),

	/**
	 * (alternate: SKKP) S wave traversing the core as P with one reflection from
	 * the inner side of the CMB and then continuing as P in the mantle
	 */
	S2KP(null,
			"(alternate: SKKP) S wave traversing the core as P with one reflection from the"
					+ "<BR>inner side of the CMB and then continuing as P in the mantle"),

	/**
	 * (alternates: SKSSKS, S'S', SKS2) Free surface reflection of SKS
	 */
	S_prime_2(null, "(alternates: SKSSKS, S'S', SKS2) Free surface reflection of SKS"),

	/**
	 * (alternates: SKSSKS, S'S', S'2) Free surface reflection of SKS
	 */
	SKS2(GeoAttributes.SSLOWNESS, "(alternates: SKSSKS, S'S', S'2) Free surface reflection of SKS"),

	/**
	 * PS to P converted reflection at the free surface; travel time matches that of
	 * PPS
	 */
	PSP("PSLOWNESS, FREE_SURFACE, SSLOWNESS, FREE_SURFACE, PSLOWNESS",
			"BOTTOM, M660, BOTTOM_SIDE_REFLECTION, FREE_SURFACE, BOTTOM, M660, BOTTOM_SIDE_REFLECTION, FREE_SURFACE, BOTTOM, M660",
			"PS to P converted reflection at the free surface; travel time" + "<BR>matches that of PPS"),

	/**
	 * SP to S converted reflection at the free surface; travel time matches that of
	 * SSP
	 */
	SPS("SSLOWNESS, FREE_SURFACE, PSLOWNESS, FREE_SURFACE, SSLOWNESS",
			"BOTTOM, M660, BOTTOM_SIDE_REFLECTION, FREE_SURFACE, BOTTOM, M660, BOTTOM_SIDE_REFLECTION, FREE_SURFACE, BOTTOM, M660",
			"SP to S converted reflection at the free surface; travel time" + "<BR>matches that of SSP"),

	/**
	 * P wave reflected from the ICB and continuing from the core to the receiver as
	 * S
	 */
	PKiKS("PSLOWNESS, CMB, PSLOWNESS, CMB, SSLOWNESS", "BOTTOM, ICB",
			"(Alternate PKSdf) P wave reflected from the ICB and"
					+ "<BR>continuing from the core to the receiver as S"),

	/**
	 * (alternate: PKSP') PKS to PKP converted reflection at the free surface
	 */
	PKSPKP(null, "(alternate: PKSP') PKS to PKP converted reflection at the free surface"),

	/**
	 * (alternate: SKPP') SKP to PKP reflection at the free surface
	 */
	SKPPKP(null, "(alternate: SKPP') SKP to PKP reflection at the free surface"),

	/**
	 * (alternate: SKPS') SKP to SKS converted reflection at the free surface
	 */
	SKPSKS(null, "(alternate: SKPS') SKP to SKS converted reflection at the free surface"),

	/**
	 * (alternate: PKSS') PKS to SKS reflection at the free surface
	 */
	PKSSKS(null, "(alternate: PKSS') PKS to SKS reflection at the free surface"),

	/**
	 * (alternate: PKSPKP) PKS to P' converted reflection at the free surface
	 */
	PKSP_prime_(null, "(alternate: PKSPKP) PKS to P' converted reflection at the free surface"),

	/**
	 * (alternate: SKPPKP) SKP to P' reflection at the free surface
	 */
	SKPP_prime_(null, "(alternate: SKPPKP) SKP to P' reflection at the free surface"),

	/**
	 * (alternate: SKPSKS) SKP to S' converted reflection at the free surface
	 */
	SKPS_prime_(null, "(alternate: SKPSKS) SKP to S' converted reflection at the free surface"),

	/**
	 * (alternate: PKSSKS) PKS to S' reflection at the free surface
	 */
	PKSS_prime_(null, "(alternate: PKSSKS) PKS to S' reflection at the free surface"),

	/**
	 * PKS to PKS converted reflection at the free surface
	 */
	PKSPKS(null, "PKS to PKS converted reflection at the free surface"),

	/**
	 * SKP to PKS reflection at the free surface
	 */
	SKPPKS(null, "SKP to PKS reflection at the free surface"),

	/**
	 * SKP to SKP converted reflection at the free surface
	 */
	SKPSKP(null, "SKP to SKP converted reflection at the free surface"),

	/**
	 * PKS to SKP reflection at the free surface
	 */
	PKSSKP(null, "PKS to SKP reflection at the free surface"),

	/**
	 * PcP to PKS reflection at the free surface
	 */
	PcPPKS(null, "PcP to PKS reflection at the free surface"),

	/**
	 * PcS to PKS converted reflection at the free surface
	 */
	PcSPKS(null, "PcS to PKS converted reflection at the free surface"),

	/**
	 * ScS to PKS converted reflection at the free surface
	 */
	ScSPKS(null, "ScS to PKS converted reflection at the free surface"),

	/**
	 * ScP to PKS reflection at the free surface
	 */
	ScPPKS(null, "ScP to PKS reflection at the free surface"),

	/**
	 * (alternate: PcPS') PcP to SKS converted reflection at the free surface
	 */
	PcPSKS(null, "(alternate: PcPS') PcP to SKS converted reflection at the free surface"),

	/**
	 * (alternate: ScPS') ScP to SKS converted reflection at the free surface
	 */
	ScPSKS(null, "(alternate: ScPS') ScP to SKS converted reflection at the free surface"),

	/**
	 * (alternate: PcPSKS) PcP to S' converted reflection at the free surface
	 */
	PcPS_prime_(null, "(alternate: PcPSKS) PcP to S' converted reflection at the free surface"),

	/**
	 * (alternate: ScPSKS) ScP to S' converted reflection at the free surface
	 */
	ScPS_prime_(null, "(alternate: ScPSKS) ScP to S' converted reflection at the free surface"),

	/**
	 * (alternate: P'PcP) PKP to PcP reflection at the free surface
	 */
	PKPPcP(GeoAttributes.PSLOWNESS, "(alternate: P'PcP) PKP to PcP reflection at the free surface"),

	/**
	 * PKS to PcP converted reflection at the free surface
	 */
	PKSPcP(null, "PKS to PcP converted reflection at the free surface"),

	/**
	 * PKS to ScS reflection at the free surface
	 */
	PKSScS(null, "PKS to ScS reflection at the free surface"),

	/**
	 * (alternate: P'ScP) PKP to ScP converted reflection at the free surface
	 */
	PKPScP(null, "(alternate: P'ScP) PKP to ScP converted reflection at the free surface"),

	/**
	 * PKS to ScP reflection at the free surface
	 */
	PKSScP(null, "PKS to ScP reflection at the free surface"),

	/**
	 * (alternate: S'ScS) SKS to ScS reflection at the free surface
	 */
	SKSScS(GeoAttributes.SSLOWNESS, "(alternate: S'ScS) SKS to ScS reflection at the free surface"),

	/**
	 * (alternate: S'PcP) SKS to PcP converted reflection at the free surface
	 */
	SKSPcP(null, "(alternate: S'PcP) SKS to PcP converted reflection at the free surface"),

	/**
	 * SKP to PcP reflection at the free surface
	 */
	SKPPcP(null, "SKP to PcP reflection at the free surface"),

	/**
	 * (alternate: S'ScP) SKS to ScP reflection at the free surface
	 */
	SKSScP(null, "(alternate: S'ScP) SKS to ScP reflection at the free surface"),

	/**
	 * SKP to ScP converted reflection at the free surface
	 */
	SKPScP(null, "SKP to ScP converted reflection at the free surface"),

	/**
	 * (alternate: P'PcS) PKP to PcS reflection at the free surface
	 */
	PKPPcS(null, "(alternate: P'PcS) PKP to PcS reflection at the free surface"),

	/**
	 * PKS to PcS converted reflection at the free surface
	 */
	PKSPcS(null, "PKS to PcS converted reflection at the free surface"),

	/**
	 * (alternate: S'PcS) SKS to PcS converted reflection at the free surface
	 */
	SKSPcS(null, "(alternate: S'PcS) SKS to PcS converted reflection at the free surface"),

	/**
	 * SKP to PcS reflection at the free surface
	 */
	SKPPcS(null, "SKP to PcS reflection at the free surface"),

	/**
	 * (alternate: P'ScS) PKP to ScS converted reflection at the free surface
	 */
	PKPScS(null, "(alternate: P'ScS) PKP to ScS converted reflection at the free surface"),

	/**
	 * SKP to ScS converted reflection at the free surface
	 */
	SKPScS(null, "SKP to ScS converted reflection at the free surface"),

	/**
	 * (alternate: PKPPcP) P' to PcP reflection at the free surface
	 */
	P_prime_PcP(GeoAttributes.PSLOWNESS, "(alternate: PKPPcP) P' to PcP reflection at the free surface"),

	/**
	 * (alternate: PKPScP) P' to ScP converted reflection at the free surface
	 */
	P_prime_ScP(null, "(alternate: PKPScP) P' to ScP converted reflection at the free surface"),

	/**
	 * (alternate: SKSScS) S' to ScS reflection at the free surface
	 */
	S_prime_ScS(null, "(alternate: SKSScS) S' to ScS reflection at the free surface"),

	/**
	 * (alternate: SKSPcP) S' to PcP converted reflection at the free surface
	 */
	S_prime_PcP(null, "(alternate: SKSPcP) S' to PcP converted reflection at the free surface"),

	/**
	 * (alternate: SKSScP) S' to ScP reflection at the free surface
	 */
	S_prime_ScP(null, "(alternate: SKSScP) S' to ScP reflection at the free surface"),

	/**
	 * (alternate: PKPPcS) P' to PcS reflection at the free surface
	 */
	P_prime_PcS(null, "(alternate: PKPPcS) P' to PcS reflection at the free surface"),

	/**
	 * (alternate: SKSPcS) S' to PcS converted reflection at the free surface
	 */
	S_prime_PcS(null, "(alternate: SKSPcS) S' to PcS converted reflection at the free surface"),

	/**
	 * (alternate: PKPScS) P' to ScS converted reflection at the free surface
	 */
	P_prime_ScS(null, "(alternate: PKPScS) P' to ScS converted reflection at the free surface"),

	/**
	 * analogous to PP, multiple free surface reflection of P (3x)
	 */
	PPPP(GeoAttributes.PSLOWNESS, "analogous to PP, multiple free surface reflection of P (3x)"),

	/**
	 * analogous to SS, multiple free surface reflection of S (3x)
	 */
	SSSS(GeoAttributes.SSLOWNESS, "analogous to SS, multiple free surface reflection of S (3x)"),

	/**
	 * PPP to S converted reflection at the free surface; travel time matches that
	 * of PPSP, PSPP
	 */
	PPPS(null, "PPP to S converted reflection at the free surface; travel time" + "<BR>matches that of PPSP, PSPP"),

	/**
	 * PSS to P converted reflection at the free surface; travel time matches that
	 * of PSPS, PPSS
	 */
	PSSP(null, "PSS to P converted reflection at the free surface; travel time" + "<BR>matches that of PSPS, PPSS"),

	/**
	 * PPS to P converted reflection at the free surface; travel time matches that
	 * of PSPP, PPPS
	 */
	PPSP(null, "PPS to P converted reflection at the free surface; travel time" + "<BR>matches that of PSPP, PPPS"),

	/**
	 * PSP to S converted reflection at the free surface; travel time matches that
	 * of PPSS, PSSP
	 */
	PSPS(null, "PSP to S converted reflection at the free surface; travel time" + "<BR>matches that of PPSS, PSSP"),

	/**
	 * SPP to S converted reflection at the free surface; travel time matches that
	 * of SPSP, SSPP
	 */
	SPPS(null, "SPP to S converted reflection at the free surface; travel time" + "<BR>matches that of SPSP, SSPP"),

	/**
	 * SSS to P converted reflection at the free surface; travel time matches that
	 * of SSPS, SPSS
	 */
	SSSP(null, "SSS to P converted reflection at the free surface; travel time" + "<BR>matches that of SSPS, SPSS"),

	/**
	 * SPS to P converted reflection at the free surface; travel time matches that
	 * of SSPP, SPPS
	 */
	SPSP(null, "SPS to P converted reflection at the free surface; travel time" + "<BR>matches that of SSPP, SPPS"),

	/**
	 * SSP to S converted reflection at the free surface; travel time matches that
	 * of SPSS, SSSP
	 */
	SSPS(null, "SSP to S converted reflection at the free surface; travel time" + "<BR>matches that of SPSS, SSSP"),

	/**
	 * PPS reflected at the free surface; travel time matches that of PSSP, PSPS
	 */
	PPSS(null, "PPS reflected at the free surface; travel time" + "<BR>matches that of PSSP, PSPS"),

	/**
	 * SSP reflected at the free surface; travel time matches that of SPPS, SPSP
	 */
	SSPP(null, "SSP reflected at the free surface; travel time" + "<BR>matches that of SPPS, SPSP"),

	/**
	 * PSP reflected at the free surface; travel time matches that of PPPS, PPSP
	 */
	PSPP(null, "PSP reflected at the free surface; travel time" + "<BR>matches that of PPPS, PPSP"),

	/**
	 * SPS reflected at the free surface; travel time matches that of SSSP, SSPS
	 */
	SPSS(null, "SPS reflected at the free surface; travel time" + "<BR>matches that of SSSP, SSPS"),

	/**
	 * PSS reflected at the free surface
	 */
	PSSS(null, "PSS reflected at the free surface"),

	/**
	 * SPP reflected at the free surface
	 */
	SPPP(null, "SPP reflected at the free surface"),

	/**
	 * Maximum amplitude of the unspecified long period surface wave group
	 */
	Lm(null, "Maximum amplitude of the unspecified long period surface wave group"),

	/**
	 * Maximum amplitude of the Love wave group
	 */
	Qm(null, "Maximum amplitude of the Love wave group"),

	/**
	 * Maximum amplitude of the Rayleigh wave group
	 */
	Rm(null, "Maximum amplitude of the Rayleigh wave group"),

	/**
	 * Maximum amplitude of the unspecified long period surface wave group, as
	 * measured on the horizontal
	 */
	LmH(null,
			"Maximum amplitude of the unspecified long period surface wave group,"
					+ "<BR>as measured on the horizontal"),

	/**
	 * Maximum amplitude of the unspecified long period surface wave group, as
	 * measured on the vertical
	 */
	LmV(null,
			"Maximum amplitude of the unspecified long period surface wave group," + "<BR>as measured on the vertical"),

	/**
	 * PKP2 resulting from reflection of upgoing P at the free surface
	 */
	pPKP2(GeoAttributes.PSLOWNESS, "PKP2 resulting from reflection of upgoing P at the free surface"),

	/**
	 * P'2 resulting from reflection of upgoing P at the free surface
	 */
	pP_prime_2(GeoAttributes.PSLOWNESS, "P'2 resulting from reflection of upgoing P at the free surface"),

	/**
	 * P'P'bc resulting from reflection of upgoing P at the free surface
	 */
	pP_prime_P_prime_bc(GeoAttributes.PSLOWNESS, "P'P'bc resulting from reflection of upgoing P at the free surface"),

	/**
	 * P'2bc resulting from reflection of upgoing P at the free surface
	 */
	pP_prime_2bc(GeoAttributes.PSLOWNESS, "P'2bc resulting from reflection of upgoing P at the free surface"),

	/**
	 * P'P'ab resulting from reflection of upgoing P at the free surface
	 */
	pP_prime_P_prime_ab(GeoAttributes.PSLOWNESS, "P'P'ab resulting from reflection of upgoing P at the free surface"),

	/**
	 * P'2ab resulting from reflection of upgoing P at the free surface
	 */
	pP_prime_2ab(GeoAttributes.PSLOWNESS, "P'2ab resulting from reflection of upgoing P at the free surface"),

	/**
	 * SPdifKS resulting from converted reflection of upgoing P at the free surface
	 */
	pSPdifKS(null, "SPdifKS resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKPdifS resulting from converted reflection of upgoing P at the free surface
	 */
	pSKPdifS(null, "SKPdifS resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * P' resulting from reflection of upgoing P at the free surface
	 */
	pP_prime_(GeoAttributes.PSLOWNESS, "P' resulting from reflection of upgoing P at the free surface"),

	/**
	 * P2KPbc resulting from reflection of upgoing P at the free surface
	 */
	pP2KPbc(GeoAttributes.PSLOWNESS, "P2KPbc resulting from reflection of upgoing P at the free surface"),

	/**
	 * P2KPab resulting from reflection of upgoing P at the free surface
	 */
	pP2KPab(GeoAttributes.PSLOWNESS, "P2KPab resulting from reflection of upgoing P at the free surface"),

	/**
	 * P2KPdf resulting from reflection of upgoing P at the free surface
	 */
	pP2KPdf(GeoAttributes.PSLOWNESS, "P2KPdf resulting from reflection of upgoing P at the free surface"),

	/**
	 * P'P'df resulting from reflection of upgoing P at the free surface
	 */
	pP_prime_P_prime_df(GeoAttributes.PSLOWNESS, "P'P'df resulting from reflection of upgoing P at the free surface"),

	/**
	 * P'2df resulting from reflection of upgoing P at the free surface
	 */
	pP_prime_2df(GeoAttributes.PSLOWNESS, "P'2df resulting from reflection of upgoing P at the free surface"),

	/**
	 * P2KS resulting from reflection of upgoing P at the free surface
	 */
	pP2KS(null, "P2KS resulting from reflection of upgoing P at the free surface"),

	/**
	 * S2KP resulting from converted reflection of upgoing P at the free surface
	 */
	pS2KP(null, "S2KP resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * S'2 resulting from converted reflection of upgoing P at the free surface
	 */
	pS_prime_2(null, "S'2 resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKS2 resulting from converted reflection of upgoing P at the free surface
	 */
	pSKS2(null, "SKS2 resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * PSP resulting from reflection of upgoing P at the free surface
	 */
	pPSP(null, "PSP resulting from reflection of upgoing P at the free surface"),

	/**
	 * SPS resulting from converted reflection of upgoing P at the free surface
	 */
	pSPS(null, "SPS resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * PKiKS resulting from reflection of upgoing P at the free surface
	 */
	pPKiKS(null, "PKiKS resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKSPKP resulting from reflection of upgoing P at the free surface
	 */
	pPKSPKP(null, "PKSPKP resulting from reflection of upgoing P at the free surface"),

	/**
	 * SKPPKP resulting from converted reflection of upgoing P at the free surface
	 */
	pSKPPKP(null, "SKPPKP resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKPSKS resulting from converted reflection of upgoing P at the free surface
	 */
	pSKPSKS(null, "SKPSKS resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * PKSSKS resulting from reflection of upgoing P at the free surface
	 */
	pPKSSKS(null, "PKSSKS resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKSP' resulting from reflection of upgoing P at the free surface
	 */
	pPKSP_prime_(null, "PKSP' resulting from reflection of upgoing P at the free surface"),

	/**
	 * SKPP' resulting from converted reflection of upgoing P at the free surface
	 */
	pSKPP_prime_(null, "SKPP' resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKPS' resulting from converted reflection of upgoing P at the free surface
	 */
	pSKPS_prime_(null, "SKPS' resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * PKSS' resulting from reflection of upgoing P at the free surface
	 */
	pPKSS_prime_(null, "PKSS' resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKSPKS resulting from reflection of upgoing P at the free surface
	 */
	pPKSPKS(null, "PKSPKS resulting from reflection of upgoing P at the free surface"),

	/**
	 * SKPPKS resulting from converted reflection of upgoing P at the free surface
	 */
	pSKPPKS(null, "SKPPKS resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKPSKP resulting from converted reflection of upgoing P at the free surface
	 */
	pSKPSKP(null, "SKPSKP resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * PKSSKP resulting from reflection of upgoing P at the free surface
	 */
	pPKSSKP(null, "PKSSKP resulting from reflection of upgoing P at the free surface"),

	/**
	 * PcPPKS resulting from reflection of upgoing P at the free surface
	 */
	pPcPPKS(null, "PcPPKS resulting from reflection of upgoing P at the free surface"),

	/**
	 * PcSPKS resulting from reflection of upgoing P at the free surface
	 */
	pPcSPKS(null, "PcSPKS resulting from reflection of upgoing P at the free surface"),

	/**
	 * ScSPKS resulting from converted reflection of upgoing P at the free surface
	 */
	pScSPKS(null, "ScSPKS resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * ScPPKS resulting from converted reflection of upgoing P at the free surface
	 */
	pScPPKS(null, "ScPPKS resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * PcPSKS resulting from reflection of upgoing P at the free surface
	 */
	pPcPSKS(null, "PcPSKS resulting from reflection of upgoing P at the free surface"),

	/**
	 * ScPSKS resulting from converted reflection of upgoing P at the free surface
	 */
	pScPSKS(null, "ScPSKS resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * PcPS' resulting from reflection of upgoing P at the free surface
	 */
	pPcPS_prime_(null, "PcPS' resulting from reflection of upgoing P at the free surface"),

	/**
	 * ScPS' resulting from converted reflection of upgoing P at the free surface
	 */
	pScPS_prime_(null, "ScPS' resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * PKPPcP resulting from reflection of upgoing P at the free surface
	 */
	pPKPPcP(GeoAttributes.PSLOWNESS, "PKPPcP resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKSPcP resulting from reflection of upgoing P at the free surface
	 */
	pPKSPcP(null, "PKSPcP resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKSScS resulting from reflection of upgoing P at the free surface
	 */
	pPKSScS(null, "PKSScS resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKPScP resulting from reflection of upgoing P at the free surface
	 */
	pPKPScP(null, "PKPScP resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKSScP resulting from reflection of upgoing P at the free surface
	 */
	pPKSScP(null, "PKSScP resulting from reflection of upgoing P at the free surface"),

	/**
	 * SKSScS resulting from converted reflection of upgoing P at the free surface
	 */
	pSKSScS(null, "SKSScS resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKSPcP resulting from converted reflection of upgoing P at the free surface
	 */
	pSKSPcP(null, "SKSPcP resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKPPcP resulting from converted reflection of upgoing P at the free surface
	 */
	pSKPPcP(null, "SKPPcP resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKSScP resulting from converted reflection of upgoing P at the free surface
	 */
	pSKSScP(null, "SKSScP resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKPScP resulting from converted reflection of upgoing P at the free surface
	 */
	pSKPScP(null, "SKPScP resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * PKPPcS resulting from reflection of upgoing P at the free surface
	 */
	pPKPPcS(null, "PKPPcS resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKSPcS resulting from reflection of upgoing P at the free surface
	 */
	pPKSPcS(null, "PKSPcS resulting from reflection of upgoing P at the free surface"),

	/**
	 * SKSPcS resulting from converted reflection of upgoing P at the free surface
	 */
	pSKSPcS(null, "SKSPcS resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKPPcS resulting from converted reflection of upgoing P at the free surface
	 */
	pSKPPcS(null, "SKPPcS resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * PKPScS resulting from reflection of upgoing P at the free surface
	 */
	pPKPScS(null, "PKPScS resulting from reflection of upgoing P at the free surface"),

	/**
	 * SKPScS resulting from converted reflection of upgoing P at the free surface
	 */
	pSKPScS(null, "SKPScS resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * P'PcP resulting from reflection of upgoing P at the free surface
	 */
	pP_prime_PcP(GeoAttributes.PSLOWNESS, "P'PcP resulting from reflection of upgoing P at the free surface"),

	/**
	 * P'ScP resulting from reflection of upgoing P at the free surface
	 */
	pP_prime_ScP(null, "P'ScP resulting from reflection of upgoing P at the free surface"),

	/**
	 * S'ScS resulting from converted reflection of upgoing P at the free surface
	 */
	pS_prime_ScS(null, "S'ScS resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * S'PcP resulting from converted reflection of upgoing P at the free surface
	 */
	pS_prime_PcP(null, "S'PcP resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * S'ScP resulting from converted reflection of upgoing P at the free surface
	 */
	pS_prime_ScP(null, "S'ScP resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * P'PcS resulting from reflection of upgoing P at the free surface
	 */
	pP_prime_PcS(null, "P'PcS resulting from reflection of upgoing P at the free surface"),

	/**
	 * S'PcS resulting from converted reflection of upgoing P at the free surface
	 */
	pS_prime_PcS(null, "S'PcS resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * P'ScS resulting from reflection of upgoing P at the free surface
	 */
	pP_prime_ScS(null, "P'ScS resulting from reflection of upgoing P at the free surface"),

	/**
	 * PPPP resulting from reflection of upgoing P at the free surface
	 */
	pPPPP(GeoAttributes.PSLOWNESS, "PPPP resulting from reflection of upgoing P at the free surface"),

	/**
	 * SSSS resulting from converted reflection of upgoing P at the free surface
	 */
	pSSSS(null, "SSSS resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * PPPS resulting from reflection of upgoing P at the free surface
	 */
	pPPPS(null, "PPPS resulting from reflection of upgoing P at the free surface"),

	/**
	 * PSSP resulting from reflection of upgoing P at the free surface
	 */
	pPSSP(null, "PSSP resulting from reflection of upgoing P at the free surface"),

	/**
	 * PPSP resulting from reflection of upgoing P at the free surface
	 */
	pPPSP(null, "PPSP resulting from reflection of upgoing P at the free surface"),

	/**
	 * PSPS resulting from reflection of upgoing P at the free surface
	 */
	pPSPS(null, "PSPS resulting from reflection of upgoing P at the free surface"),

	/**
	 * SPPS resulting from converted reflection of upgoing P at the free surface
	 */
	pSPPS(null, "SPPS resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SSSP resulting from converted reflection of upgoing P at the free surface
	 */
	pSSSP(null, "SSSP resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SPSP resulting from converted reflection of upgoing P at the free surface
	 */
	pSPSP(null, "SPSP resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SSPS resulting from converted reflection of upgoing P at the free surface
	 */
	pSSPS(null, "SSPS resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * PPSS resulting from reflection of upgoing P at the free surface
	 */
	pPPSS(null, "PPSS resulting from reflection of upgoing P at the free surface"),

	/**
	 * SSPP resulting from converted reflection of upgoing P at the free surface
	 */
	pSSPP(null, "SSPP resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * PSPP resulting from reflection of upgoing P at the free surface
	 */
	pPSPP(null, "PSPP resulting from reflection of upgoing P at the free surface"),

	/**
	 * SPSS resulting from converted reflection of upgoing P at the free surface
	 */
	pSPSS(null, "SPSS resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * PSSS resulting from reflection of upgoing P at the free surface
	 */
	pPSSS(null, "PSSS resulting from reflection of upgoing P at the free surface"),

	/**
	 * SPPP resulting from converted reflection of upgoing P at the free surface
	 */
	pSPPP(null, "SPPP resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * PKP2 resulting from converted reflection of upgoing S at the free surface
	 */
	sPKP2(null, "PKP2 resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P'2 resulting from converted reflection of upgoing S at the free surface
	 */
	sP_prime_2(null, "P'2 resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P'P'bc resulting from converted reflection of upgoing S at the free surface
	 */
	sP_prime_P_prime_bc(null, "P'P'bc resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P'2bc resulting from converted reflection of upgoing S at the free surface
	 */
	sP_prime_2bc(null, "P'2bc resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P'P'ab resulting from converted reflection of upgoing S at the free surface
	 */
	sP_prime_P_prime_ab(null, "P'P'ab resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P'2ab resulting from converted reflection of upgoing S at the free surface
	 */
	sP_prime_2ab(null, "P'2ab resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * SPdifKS resulting from reflection of upgoing S at the free surface
	 */
	sSPdifKS(null, "SPdifKS resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKPdifS resulting from reflection of upgoing S at the free surface
	 */
	sSKPdifS(null, "SKPdifS resulting from reflection of upgoing S at the free surface"),

	/**
	 * P' resulting from converted reflection of upgoing S at the free surface
	 */
	sP_prime_(null, "P' resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P2KPbc resulting from converted reflection of upgoing S at the free surface
	 */
	sP2KPbc(null, "P2KPbc resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P2KPab resulting from converted reflection of upgoing S at the free surface
	 */
	sP2KPab(null, "P2KPab resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P2KPdf resulting from converted reflection of upgoing S at the free surface
	 */
	sP2KPdf(null, "P2KPdf resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P'P'df resulting from converted reflection of upgoing S at the free surface
	 */
	sP_prime_P_prime_df(null, "P'P'df resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P'2df resulting from converted reflection of upgoing S at the free surface
	 */
	sP_prime_2df(null, "P'2df resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P2KS resulting from converted reflection of upgoing S at the free surface
	 */
	sP2KS(null, "P2KS resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * S2KP resulting from reflection of upgoing S at the free surface
	 */
	sS2KP(null, "S2KP resulting from reflection of upgoing S at the free surface"),

	/**
	 * S'2 resulting from reflection of upgoing S at the free surface
	 */
	sS_prime_2(null, "S'2 resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKS2 resulting from reflection of upgoing S at the free surface
	 */
	sSKS2(GeoAttributes.SSLOWNESS, "SKS2 resulting from reflection of upgoing S at the free surface"),

	/**
	 * PSP resulting from converted reflection of upgoing S at the free surface
	 */
	sPSP(null, "PSP resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * SPS resulting from reflection of upgoing S at the free surface
	 */
	sSPS(null, "SPS resulting from reflection of upgoing S at the free surface"),

	/**
	 * PKiKS resulting from converted reflection of upgoing S at the free surface
	 */
	sPKiKS(null, "PKiKS resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKSPKP resulting from converted reflection of upgoing S at the free surface
	 */
	sPKSPKP(null, "PKSPKP resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * SKPPKP resulting from reflection of upgoing S at the free surface
	 */
	sSKPPKP(null, "SKPPKP resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKPSKS resulting from reflection of upgoing S at the free surface
	 */
	sSKPSKS(null, "SKPSKS resulting from reflection of upgoing S at the free surface"),

	/**
	 * PKSSKS resulting from converted reflection of upgoing S at the free surface
	 */
	sPKSSKS(null, "PKSSKS resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKSP' resulting from converted reflection of upgoing S at the free surface
	 */
	sPKSP_prime_(null, "PKSP' resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * SKPP' resulting from reflection of upgoing S at the free surface
	 */
	sSKPP_prime_(null, "SKPP' resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKPS' resulting from reflection of upgoing S at the free surface
	 */
	sSKPS_prime_(null, "SKPS' resulting from reflection of upgoing S at the free surface"),

	/**
	 * PKSS' resulting from converted reflection of upgoing S at the free surface
	 */
	sPKSS_prime_(null, "PKSS' resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKSPKS resulting from converted reflection of upgoing S at the free surface
	 */
	sPKSPKS(null, "PKSPKS resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * SKPPKS resulting from reflection of upgoing S at the free surface
	 */
	sSKPPKS(null, "SKPPKS resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKPSKP resulting from reflection of upgoing S at the free surface
	 */
	sSKPSKP(null, "SKPSKP resulting from reflection of upgoing S at the free surface"),

	/**
	 * PKSSKP resulting from converted reflection of upgoing S at the free surface
	 */
	sPKSSKP(null, "PKSSKP resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PcPPKS resulting from converted reflection of upgoing S at the free surface
	 */
	sPcPPKS(null, "PcPPKS resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PcSPKS resulting from converted reflection of upgoing S at the free surface
	 */
	sPcSPKS(null, "PcSPKS resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * ScSPKS resulting from reflection of upgoing S at the free surface
	 */
	sScSPKS(null, "ScSPKS resulting from reflection of upgoing S at the free surface"),

	/**
	 * ScPPKS resulting from reflection of upgoing S at the free surface
	 */
	sScPPKS(null, "ScPPKS resulting from reflection of upgoing S at the free surface"),

	/**
	 * PcPSKS resulting from converted reflection of upgoing S at the free surface
	 */
	sPcPSKS(null, "PcPSKS resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * ScPSKS resulting from reflection of upgoing S at the free surface
	 */
	sScPSKS(null, "ScPSKS resulting from reflection of upgoing S at the free surface"),

	/**
	 * PcPS' resulting from converted reflection of upgoing S at the free surface
	 */
	sPcPS_prime_(null, "PcPS' resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * ScPS' resulting from reflection of upgoing S at the free surface
	 */
	sScPS_prime_(null, "ScPS' resulting from reflection of upgoing S at the free surface"),

	/**
	 * PKPPcP resulting from converted reflection of upgoing S at the free surface
	 */
	sPKPPcP(null, "PKPPcP resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKSPcP resulting from converted reflection of upgoing S at the free surface
	 */
	sPKSPcP(null, "PKSPcP resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKSScS resulting from converted reflection of upgoing S at the free surface
	 */
	sPKSScS(null, "PKSScS resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKPScP resulting from converted reflection of upgoing S at the free surface
	 */
	sPKPScP(null, "PKPScP resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKSScP resulting from converted reflection of upgoing S at the free surface
	 */
	sPKSScP(null, "PKSScP resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * SKSScS resulting from reflection of upgoing S at the free surface
	 */
	sSKSScS(GeoAttributes.SSLOWNESS, "SKSScS resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKSPcP resulting from reflection of upgoing S at the free surface
	 */
	sSKSPcP(null, "SKSPcP resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKPPcP resulting from reflection of upgoing S at the free surface
	 */
	sSKPPcP(null, "SKPPcP resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKSScP resulting from reflection of upgoing S at the free surface
	 */
	sSKSScP(null, "SKSScP resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKPScP resulting from reflection of upgoing S at the free surface
	 */
	sSKPScP(null, "SKPScP resulting from reflection of upgoing S at the free surface"),

	/**
	 * PKPPcS resulting from converted reflection of upgoing S at the free surface
	 */
	sPKPPcS(null, "PKPPcS resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKSPcS resulting from converted reflection of upgoing S at the free surface
	 */
	sPKSPcS(null, "PKSPcS resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * SKSPcS resulting from reflection of upgoing S at the free surface
	 */
	sSKSPcS(null, "SKSPcS resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKPPcS resulting from reflection of upgoing S at the free surface
	 */
	sSKPPcS(null, "SKPPcS resulting from reflection of upgoing S at the free surface"),

	/**
	 * PKPScS resulting from converted reflection of upgoing S at the free surface
	 */
	sPKPScS(null, "PKPScS resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * SKPScS resulting from reflection of upgoing S at the free surface
	 */
	sSKPScS(null, "SKPScS resulting from reflection of upgoing S at the free surface"),

	/**
	 * P'PcP resulting from converted reflection of upgoing S at the free surface
	 */
	sP_prime_PcP(null, "P'PcP resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P'ScP resulting from converted reflection of upgoing S at the free surface
	 */
	sP_prime_ScP(null, "P'ScP resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * S'ScS resulting from reflection of upgoing S at the free surface
	 */
	sS_prime_ScS(null, "S'ScS resulting from reflection of upgoing S at the free surface"),

	/**
	 * S'PcP resulting from reflection of upgoing S at the free surface
	 */
	sS_prime_PcP(null, "S'PcP resulting from reflection of upgoing S at the free surface"),

	/**
	 * S'ScP resulting from reflection of upgoing S at the free surface
	 */
	sS_prime_ScP(null, "S'ScP resulting from reflection of upgoing S at the free surface"),

	/**
	 * P'PcS resulting from converted reflection of upgoing S at the free surface
	 */
	sP_prime_PcS(null, "P'PcS resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * S'PcS resulting from reflection of upgoing S at the free surface
	 */
	sS_prime_PcS(null, "S'PcS resulting from reflection of upgoing S at the free surface"),

	/**
	 * P'ScS resulting from converted reflection of upgoing S at the free surface
	 */
	sP_prime_ScS(null, "P'ScS resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PPPP resulting from converted reflection of upgoing S at the free surface
	 */
	sPPPP(null, "PPPP resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * SSSS resulting from reflection of upgoing S at the free surface
	 */
	sSSSS(GeoAttributes.SSLOWNESS, "SSSS resulting from reflection of upgoing S at the free surface"),

	/**
	 * PPPS resulting from converted reflection of upgoing S at the free surface
	 */
	sPPPS(null, "PPPS resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PSSP resulting from converted reflection of upgoing S at the free surface
	 */
	sPSSP(null, "PSSP resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PPSP resulting from converted reflection of upgoing S at the free surface
	 */
	sPPSP(null, "PPSP resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PSPS resulting from converted reflection of upgoing S at the free surface
	 */
	sPSPS(null, "PSPS resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * SPPS resulting from reflection of upgoing S at the free surface
	 */
	sSPPS(null, "SPPS resulting from reflection of upgoing S at the free surface"),

	/**
	 * SSSP resulting from reflection of upgoing S at the free surface
	 */
	sSSSP(null, "SSSP resulting from reflection of upgoing S at the free surface"),

	/**
	 * SPSP resulting from reflection of upgoing S at the free surface
	 */
	sSPSP(null, "SPSP resulting from reflection of upgoing S at the free surface"),

	/**
	 * SSPS resulting from reflection of upgoing S at the free surface
	 */
	sSSPS(null, "SSPS resulting from reflection of upgoing S at the free surface"),

	/**
	 * PPSS resulting from converted reflection of upgoing S at the free surface
	 */
	sPPSS(null, "PPSS resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * SSPP resulting from reflection of upgoing S at the free surface
	 */
	sSSPP(null, "SSPP resulting from reflection of upgoing S at the free surface"),

	/**
	 * PSPP resulting from converted reflection of upgoing S at the free surface
	 */
	sPSPP(null, "PSPP resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * SPSS resulting from reflection of upgoing S at the free surface
	 */
	sSPSS(null, "SPSS resulting from reflection of upgoing S at the free surface"),

	/**
	 * PSSS resulting from converted reflection of upgoing S at the free surface
	 */
	sPSSS(null, "PSSS resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * SPPP resulting from reflection of upgoing S at the free surface
	 */
	sSPPP(null, "SPPP resulting from reflection of upgoing S at the free surface"),

	/**
	 * PKP2 resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKP2(GeoAttributes.PSLOWNESS, "PKP2 resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P'2 resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP_prime_2(GeoAttributes.PSLOWNESS, "P'2 resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P'P'bc resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP_prime_P_prime_bc(GeoAttributes.PSLOWNESS,
			"P'P'bc resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P'2bc resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP_prime_2bc(GeoAttributes.PSLOWNESS, "P'2bc resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P'P'ab resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP_prime_P_prime_ab(GeoAttributes.PSLOWNESS,
			"P'P'ab resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P'2ab resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP_prime_2ab(GeoAttributes.PSLOWNESS, "P'2ab resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P' resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP_prime_(GeoAttributes.PSLOWNESS, "P' resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P2KPbc resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP2KPbc(GeoAttributes.PSLOWNESS, "P2KPbc resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P2KPab resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP2KPab(GeoAttributes.PSLOWNESS, "P2KPab resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P2KPdf resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP2KPdf(GeoAttributes.PSLOWNESS, "P2KPdf resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P'P'df resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP_prime_P_prime_df(GeoAttributes.PSLOWNESS,
			"P'P'df resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P'2df resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP_prime_2df(GeoAttributes.PSLOWNESS, "P'2df resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P2KS resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP2KS(null, "P2KS resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PSP resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPSP(null, "PSP resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKiKS resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKiKS(null, "PKiKS resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKSPKP resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKSPKP(null, "PKSPKP resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKSSKS resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKSSKS(null, "PKSSKS resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKSP' resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKSP_prime_(null, "PKSP' resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKSS' resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKSS_prime_(null, "PKSS' resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKSPKS resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKSPKS(null, "PKSPKS resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKSSKP resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKSSKP(null, "PKSSKP resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PcPPKS resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPcPPKS(null, "PcPPKS resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PcSPKS resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPcSPKS(null, "PcSPKS resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PcPSKS resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPcPSKS(null, "PcPSKS resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PcPS' resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPcPS_prime_(null, "PcPS' resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKPPcP resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKPPcP(GeoAttributes.PSLOWNESS, "PKPPcP resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKSPcP resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKSPcP(null, "PKSPcP resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKSScS resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKSScS(null, "PKSScS resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKPScP resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKPScP(null, "PKPScP resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKSScP resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKSScP(null, "PKSScP resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKPPcS resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKPPcS(null, "PKPPcS resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKSPcS resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKSPcS(null, "PKSPcS resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKPScS resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKPScS(null, "PKPScS resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P'PcP resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP_prime_PcP(GeoAttributes.PSLOWNESS, "P'PcP resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P'ScP resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP_prime_ScP(null, "P'ScP resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P'PcS resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP_prime_PcS(null, "P'PcS resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P'ScS resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP_prime_ScS(null, "P'ScS resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PPPP resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPPPP(GeoAttributes.PSLOWNESS, "PPPP resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PPPS resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPPPS(null, "PPPS resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PSSP resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPSSP(null, "PSSP resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PPSP resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPPSP(null, "PPSP resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PSPS resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPSPS(null, "PSPS resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PPSS resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPPSS(null, "PPSS resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PSPP resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPSPP(null, "PSPP resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PSSS resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPSSS(null, "PSSS resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKP2 resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKP2(GeoAttributes.PSLOWNESS, "PKP2 resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P'2 resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP_prime_2(GeoAttributes.PSLOWNESS, "P'2 resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P' resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP_prime_(GeoAttributes.PSLOWNESS, "P' resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P'P'df resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP_prime_P_prime_df(GeoAttributes.PSLOWNESS,
			"P'P'df resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P'2df resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP_prime_2df(GeoAttributes.PSLOWNESS,
			"P'2df resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P2KS resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP2KS(null, "P2KS resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PSP resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPSP(null, "PSP resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKiKS resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKiKS(null, "PKiKS resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKSPKP resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKSPKP(null, "PKSPKP resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKSSKS resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKSSKS(null, "PKSSKS resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKSP' resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKSP_prime_(null, "PKSP' resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKSS' resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKSS_prime_(null, "PKSS' resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKSPKS resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKSPKS(null, "PKSPKS resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKSSKP resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKSSKP(null, "PKSSKP resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PcPPKS resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPcPPKS(null, "PcPPKS resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PcSPKS resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPcSPKS(null, "PcSPKS resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PcPSKS resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPcPSKS(null, "PcPSKS resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PcPS' resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPcPS_prime_(null, "PcPS' resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKPPcP resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKPPcP(GeoAttributes.PSLOWNESS, "PKPPcP resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKSPcP resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKSPcP(null, "PKSPcP resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKSScS resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKSScS(null, "PKSScS resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKPScP resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKPScP(null, "PKPScP resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKSScP resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKSScP(null, "PKSScP resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKPPcS resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKPPcS(null, "PKPPcS resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKSPcS resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKSPcS(null, "PKSPcS resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKPScS resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKPScS(null, "PKPScS resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P'PcP resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP_prime_PcP(GeoAttributes.PSLOWNESS,
			"P'PcP resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P'ScP resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP_prime_ScP(null, "P'ScP resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P'PcS resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP_prime_PcS(null, "P'PcS resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P'ScS resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP_prime_ScS(null, "P'ScS resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PPPP resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPPPP(GeoAttributes.PSLOWNESS, "PPPP resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PPPS resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPPPS(null, "PPPS resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PSSP resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPSSP(null, "PSSP resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PPSP resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPPSP(null, "PPSP resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PSPS resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPSPS(null, "PSPS resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PPSS resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPPSS(null, "PPSS resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PSPP resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPSPP(null, "PSPP resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PSSS resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPSSS(null, "PSSS resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * Maximum amplitude of the Rayleigh wave group, as measured on the horizontal
	 */
	RmH(null, "Maximum amplitude of the Rayleigh wave group," + "<BR>as measured on the horizontal"),

	/**
	 * Maximum amplitude of the Rayleigh wave group, as measured on the vertical
	 */
	RmV(null, "Maximum amplitude of the Rayleigh wave group," + "<BR>as measured on the vertical"),

	/**
	 * Amplitude measurement for Pg
	 */
	AMPg(GeoAttributes.PSLOWNESS, "Amplitude measurement for Pg"),

	/**
	 * Amplitude measurement for Pn
	 */
	AMPn(GeoAttributes.PSLOWNESS, "Amplitude measurement for Pn"),

	/**
	 * Amplitude measurement for Sg
	 */
	AMSg(GeoAttributes.SSLOWNESS, "Amplitude measurement for Sg"),

	/**
	 * Amplitude measurement for Sn
	 */
	AMSn(GeoAttributes.SSLOWNESS, "Amplitude measurement for Sn"),

	/**
	 * Sb free surface reflection (IASPEI extension)
	 */
	SbSb(GeoAttributes.SSLOWNESS, "Sb free surface reflection (IASPEI extension)"),

	/**
	 * Pb free surface reflection (IASPEI extension)
	 */
	PbPb(GeoAttributes.PSLOWNESS, "Pb free surface reflection (IASPEI extension)"),

	/**
	 * PcS to ScP reflection at the free surface (IASPEI extension)
	 */
	PcSScP(null, "PcS to ScP reflection at the free surface (IASPEI extension)"),

	/**
	 * PcSScP resulting from reflection of upgoing P at the free surface
	 */
	pPcSScP(null, "PcSScP resulting from reflection of upgoing P at the free surface"),

	/**
	 * PcSScP resulting from converted reflection of upgoing S at the free surface
	 */
	sPcSScP(null, "PcSScP resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PcSScP resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPcSScP(null, "PcSScP resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PcSScP resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPcSScP(null, "PcSScP resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKPPcP with K leg bottoming in the upper outer core (IASPEI extension)
	 */
	PKPPcPab(GeoAttributes.PSLOWNESS, "PKPPcP with K leg bottoming in the upper outer core (IASPEI extension)"),

	/**
	 * PKPPcS with K leg bottoming in the upper outer core (IASPEI extension)
	 */
	PKPPcSab(null, "PKPPcS with K leg bottoming in the upper outer core (IASPEI extension)"),

	/**
	 * PKPScP with K leg bottoming in the upper outer core (IASPEI extension)
	 */
	PKPScPab(null, "PKPScP with K leg bottoming in the upper outer core (IASPEI extension)"),

	/**
	 * PKPScS with K leg bottoming in the upper outer core (IASPEI extension)
	 */
	PKPScSab(null, "PKPScS with K leg bottoming in the upper outer core (IASPEI extension)"),

	/**
	 * PKSPcP with K leg bottoming in the upper outer core (IASPEI extension)
	 */
	PKSPcPab(null, "PKSPcP with K leg bottoming in the upper outer core (IASPEI extension)"),

	/**
	 * PKSPcS with K leg bottoming in the upper outer core (IASPEI extension)
	 */
	PKSPcSab(null, "PKSPcS with K leg bottoming in the upper outer core (IASPEI extension)"),

	/**
	 * PKSScP with K leg bottoming in the upper outer core (IASPEI extension)
	 */
	PKSScPab(null, "PKSScP with K leg bottoming in the upper outer core (IASPEI extension)"),

	/**
	 * PKSScS with K leg bottoming in the upper outer core (IASPEI extension)
	 */
	PKSScSab(null, "PKSScS with K leg bottoming in the upper outer core (IASPEI extension)"),

	/**
	 * PcPPKP with K leg bottoming in the upper outer core (IASPEI extension)
	 */
	PcPPKPab(GeoAttributes.PSLOWNESS, "PcPPKP with K leg bottoming in the upper outer core (IASPEI extension)"),

	/**
	 * PcPSKP with K leg bottoming in the upper outer core (IASPEI extension)
	 */
	PcPSKPab(null, "PcPSKP with K leg bottoming in the upper outer core (IASPEI extension)"),

	/**
	 * PcSPKP with K leg bottoming in the upper outer core (IASPEI extension)
	 */
	PcSPKPab(null, "PcSPKP with K leg bottoming in the upper outer core (IASPEI extension)"),

	/**
	 * PcSPKS with K leg bottoming in the upper outer core (IASPEI extension)
	 */
	PcSPKSab(null, "PcSPKS with K leg bottoming in the upper outer core (IASPEI extension)"),

	/**
	 * PcSSKP with K leg bottoming in the upper outer core (IASPEI extension)
	 */
	PcSSKPab(null, "PcSSKP with K leg bottoming in the upper outer core (IASPEI extension)"),

	/**
	 * SKPPcS with K leg bottoming in the upper outer core (IASPEI extension)
	 */
	SKPPcSab(null, "SKPPcS with K leg bottoming in the upper outer core (IASPEI extension)"),

	/**
	 * SKPScP with K leg bottoming in the upper outer core (IASPEI extension)
	 */
	SKPScPab(null, "SKPScP with K leg bottoming in the upper outer core (IASPEI extension)"),

	/**
	 * SKPScS with K leg bottoming in the upper outer core (IASPEI extension)
	 */
	SKPScSab(null, "SKPScS with K leg bottoming in the upper outer core (IASPEI extension)"),

	/**
	 * ScPPKP with K leg bottoming in the upper outer core (IASPEI extension)
	 */
	ScPPKPab(null, "ScPPKP with K leg bottoming in the upper outer core (IASPEI extension)"),

	/**
	 * ScPPKS with K leg bottoming in the upper outer core (IASPEI extension)
	 */
	ScPPKSab(null, "ScPPKS with K leg bottoming in the upper outer core (IASPEI extension)"),

	/**
	 * ScPSKP with K leg bottoming in the upper outer core (IASPEI extension)
	 */
	ScPSKPab(null, "ScPSKP with K leg bottoming in the upper outer core (IASPEI extension)"),

	/**
	 * ScSPKP with K leg bottoming in the upper outer core (IASPEI extension)
	 */
	ScSPKPab(null, "ScSPKP with K leg bottoming in the upper outer core (IASPEI extension)"),

	/**
	 * ScSPKS with K leg bottoming in the upper outer core (IASPEI extension)
	 */
	ScSPKSab(null, "ScSPKS with K leg bottoming in the upper outer core (IASPEI extension)"),

	/**
	 * ScSSKP with K leg bottoming in the upper outer core (IASPEI extension)
	 */
	ScSSKPab(null, "ScSSKP with K leg bottoming in the upper outer core (IASPEI extension)"),

	/**
	 * PKPPcPab resulting from reflection of upgoing P at the free surface
	 */
	pPKPPcPab(GeoAttributes.PSLOWNESS, "PKPPcPab resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKPPcSab resulting from reflection of upgoing P at the free surface
	 */
	pPKPPcSab(null, "PKPPcSab resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKPScPab resulting from reflection of upgoing P at the free surface
	 */
	pPKPScPab(null, "PKPScPab resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKPScSab resulting from reflection of upgoing P at the free surface
	 */
	pPKPScSab(null, "PKPScSab resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKSPcPab resulting from reflection of upgoing P at the free surface
	 */
	pPKSPcPab(null, "PKSPcPab resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKSPcSab resulting from reflection of upgoing P at the free surface
	 */
	pPKSPcSab(null, "PKSPcSab resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKSScPab resulting from reflection of upgoing P at the free surface
	 */
	pPKSScPab(null, "PKSScPab resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKSScSab resulting from reflection of upgoing P at the free surface
	 */
	pPKSScSab(null, "PKSScSab resulting from reflection of upgoing P at the free surface"),

	/**
	 * PcPPKPab resulting from reflection of upgoing P at the free surface
	 */
	pPcPPKPab(GeoAttributes.PSLOWNESS, "PcPPKPab resulting from reflection of upgoing P at the free surface"),

	/**
	 * PcPPKSab resulting from reflection of upgoing P at the free surface
	 */
	pPcPPKSab(null, "PcPPKSab resulting from reflection of upgoing P at the free surface"),

	/**
	 * PcPSKPab resulting from reflection of upgoing P at the free surface
	 */
	pPcPSKPab(null, "PcPSKPab resulting from reflection of upgoing P at the free surface"),

	/**
	 * PcSPKPab resulting from reflection of upgoing P at the free surface
	 */
	pPcSPKPab(null, "PcSPKPab resulting from reflection of upgoing P at the free surface"),

	/**
	 * PcSPKSab resulting from reflection of upgoing P at the free surface
	 */
	pPcSPKSab(null, "PcSPKSab resulting from reflection of upgoing P at the free surface"),

	/**
	 * PcSSKPab resulting from reflection of upgoing P at the free surface
	 */
	pPcSSKPab(null, "PcSSKPab resulting from reflection of upgoing P at the free surface"),

	/**
	 * SKPPcPab resulting from converted reflection of upgoing P at the free surface
	 */
	pSKPPcPab(null, "SKPPcPab resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKPPcSab resulting from converted reflection of upgoing P at the free surface
	 */
	pSKPPcSab(null, "SKPPcSab resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKPScPab resulting from converted reflection of upgoing P at the free surface
	 */
	pSKPScPab(null, "SKPScPab resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKPScSab resulting from converted reflection of upgoing P at the free surface
	 */
	pSKPScSab(null, "SKPScSab resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * ScPPKPab resulting from converted reflection of upgoing P at the free surface
	 */
	pScPPKPab(null, "ScPPKPab resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * ScPPKSab resulting from converted reflection of upgoing P at the free surface
	 */
	pScPPKSab(null, "ScPPKSab resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * ScPSKPab resulting from converted reflection of upgoing P at the free surface
	 */
	pScPSKPab(null, "ScPSKPab resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * ScSPKPab resulting from converted reflection of upgoing P at the free surface
	 */
	pScSPKPab(null, "ScSPKPab resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * ScSPKSab resulting from converted reflection of upgoing P at the free surface
	 */
	pScSPKSab(null, "ScSPKSab resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * ScSSKPab resulting from converted reflection of upgoing P at the free surface
	 */
	pScSSKPab(null, "ScSSKPab resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * PKPPcPab resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKPPcPab(GeoAttributes.PSLOWNESS,
			"PKPPcPab resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKPPcSab resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKPPcSab(null, "PKPPcSab resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKPScPab resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKPScPab(null, "PKPScPab resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKPScSab resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKPScSab(null, "PKPScSab resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKSPcPab resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKSPcPab(null, "PKSPcPab resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKSPcSab resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKSPcSab(null, "PKSPcSab resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKSScPab resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKSScPab(null, "PKSScPab resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKSScSab resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKSScSab(null, "PKSScSab resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PcPPKPab resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPcPPKPab(GeoAttributes.PSLOWNESS,
			"PcPPKPab resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PcPPKSab resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPcPPKSab(null, "PcPPKSab resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PcPSKPab resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPcPSKPab(null, "PcPSKPab resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PcSPKPab resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPcSPKPab(null, "PcSPKPab resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PcSPKSab resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPcSPKSab(null, "PcSPKSab resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PcSSKPab resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPcSSKPab(null, "PcSSKPab resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKPPcPab resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKPPcPab(GeoAttributes.PSLOWNESS, "PKPPcPab resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKPPcSab resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKPPcSab(null, "PKPPcSab resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKPScPab resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKPScPab(null, "PKPScPab resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKPScSab resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKPScSab(null, "PKPScSab resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKSPcPab resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKSPcPab(null, "PKSPcPab resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKSPcSab resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKSPcSab(null, "PKSPcSab resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKSScPab resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKSScPab(null, "PKSScPab resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKSScSab resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKSScSab(null, "PKSScSab resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PcPPKPab resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPcPPKPab(GeoAttributes.PSLOWNESS, "PcPPKPab resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PcPPKSab resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPcPPKSab(null, "PcPPKSab resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PcPSKPab resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPcPSKPab(null, "PcPSKPab resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PcSPKPab resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPcSPKPab(null, "PcSPKPab resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PcSPKSab resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPcSPKSab(null, "PcSPKSab resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PcSSKPab resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPcSSKPab(null, "PcSSKPab resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKPPcPab resulting from converted reflection of upgoing S at the free surface
	 */
	sPKPPcPab(null, "PKPPcPab resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKPPcSab resulting from converted reflection of upgoing S at the free surface
	 */
	sPKPPcSab(null, "PKPPcSab resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKPScPab resulting from converted reflection of upgoing S at the free surface
	 */
	sPKPScPab(null, "PKPScPab resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKPScSab resulting from converted reflection of upgoing S at the free surface
	 */
	sPKPScSab(null, "PKPScSab resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKSPcPab resulting from converted reflection of upgoing S at the free surface
	 */
	sPKSPcPab(null, "PKSPcPab resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKSPcSab resulting from converted reflection of upgoing S at the free surface
	 */
	sPKSPcSab(null, "PKSPcSab resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKSScPab resulting from converted reflection of upgoing S at the free surface
	 */
	sPKSScPab(null, "PKSScPab resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKSScSab resulting from converted reflection of upgoing S at the free surface
	 */
	sPKSScSab(null, "PKSScSab resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PcPPKPab resulting from converted reflection of upgoing S at the free surface
	 */
	sPcPPKPab(null, "PcPPKPab resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PcPPKSab resulting from converted reflection of upgoing S at the free surface
	 */
	sPcPPKSab(null, "PcPPKSab resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PcPSKPab resulting from converted reflection of upgoing S at the free surface
	 */
	sPcPSKPab(null, "PcPSKPab resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PcSPKPab resulting from converted reflection of upgoing S at the free surface
	 */
	sPcSPKPab(null, "PcSPKPab resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PcSPKSab resulting from converted reflection of upgoing S at the free surface
	 */
	sPcSPKSab(null, "PcSPKSab resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PcSSKPab resulting from converted reflection of upgoing S at the free surface
	 */
	sPcSSKPab(null, "PcSSKPab resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * SKPPcPab resulting from reflection of upgoing S at the free surface
	 */
	sSKPPcPab(null, "SKPPcPab resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKPPcSab resulting from reflection of upgoing S at the free surface
	 */
	sSKPPcSab(null, "SKPPcSab resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKPScPab resulting from reflection of upgoing S at the free surface
	 */
	sSKPScPab(null, "SKPScPab resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKPScSab resulting from reflection of upgoing S at the free surface
	 */
	sSKPScSab(null, "SKPScSab resulting from reflection of upgoing S at the free surface"),

	/**
	 * ScPPKPab resulting from reflection of upgoing S at the free surface
	 */
	sScPPKPab(null, "ScPPKPab resulting from reflection of upgoing S at the free surface"),

	/**
	 * ScPPKSab resulting from reflection of upgoing S at the free surface
	 */
	sScPPKSab(null, "ScPPKSab resulting from reflection of upgoing S at the free surface"),

	/**
	 * ScPSKPab resulting from reflection of upgoing S at the free surface
	 */
	sScPSKPab(null, "ScPSKPab resulting from reflection of upgoing S at the free surface"),

	/**
	 * ScSPKPab resulting from reflection of upgoing S at the free surface
	 */
	sScSPKPab(null, "ScSPKPab resulting from reflection of upgoing S at the free surface"),

	/**
	 * ScSPKSab resulting from reflection of upgoing S at the free surface
	 */
	sScSPKSab(null, "ScSPKSab resulting from reflection of upgoing S at the free surface"),

	/**
	 * ScSSKPab resulting from reflection of upgoing S at the free surface
	 */
	sScSSKPab(null, "ScSSKPab resulting from reflection of upgoing S at the free surface"),

	/**
	 * PcPPKS with K leg bottoming in the upper outer core (IASPEI extension)
	 */
	PcPPKSab(null, "PcPPKS with K leg bottoming in the upper outer core (IASPEI extension)"),

	/**
	 * SKPPcP with K leg bottoming in the upper outer core (IASPEI extension)
	 */
	SKPPcPab(null, "SKPPcP with K leg bottoming in the upper outer core (IASPEI extension)"),

	/**
	 * PKPPcP with K leg bottoming in the lower outer core (IASPEI extension)
	 */
	PKPPcPbc(GeoAttributes.PSLOWNESS, "PKPPcP with K leg bottoming in the lower outer core (IASPEI extension)"),

	/**
	 * PKPPcS with K leg bottoming in the lower outer core (IASPEI extension)
	 */
	PKPPcSbc(null, "PKPPcS with K leg bottoming in the lower outer core (IASPEI extension)"),

	/**
	 * PKPScP with K leg bottoming in the lower outer core (IASPEI extension)
	 */
	PKPScPbc(null, "PKPScP with K leg bottoming in the lower outer core (IASPEI extension)"),

	/**
	 * PKPScS with K leg bottoming in the lower outer core (IASPEI extension)
	 */
	PKPScSbc(null, "PKPScS with K leg bottoming in the lower outer core (IASPEI extension)"),

	/**
	 * PKSPcP with K leg bottoming in the lower outer core (IASPEI extension)
	 */
	PKSPcPbc(null, "PKSPcP with K leg bottoming in the lower outer core (IASPEI extension)"),

	/**
	 * PKSPcS with K leg bottoming in the lower outer core (IASPEI extension)
	 */
	PKSPcSbc(null, "PKSPcS with K leg bottoming in the lower outer core (IASPEI extension)"),

	/**
	 * PKSScP with K leg bottoming in the lower outer core (IASPEI extension)
	 */
	PKSScPbc(null, "PKSScP with K leg bottoming in the lower outer core (IASPEI extension)"),

	/**
	 * PKSScS with K leg bottoming in the lower outer core (IASPEI extension)
	 */
	PKSScSbc(null, "PKSScS with K leg bottoming in the lower outer core (IASPEI extension)"),

	/**
	 * PcPPKP with K leg bottoming in the lower outer core (IASPEI extension)
	 */
	PcPPKPbc(GeoAttributes.PSLOWNESS, "PcPPKP with K leg bottoming in the lower outer core (IASPEI extension)"),

	/**
	 * PcPSKP with K leg bottoming in the lower outer core (IASPEI extension)
	 */
	PcPSKPbc(null, "PcPSKP with K leg bottoming in the lower outer core (IASPEI extension)"),

	/**
	 * PcSPKP with K leg bottoming in the lower outer core (IASPEI extension)
	 */
	PcSPKPbc(null, "PcSPKP with K leg bottoming in the lower outer core (IASPEI extension)"),

	/**
	 * PcSPKS with K leg bottoming in the lower outer core (IASPEI extension)
	 */
	PcSPKSbc(null, "PcSPKS with K leg bottoming in the lower outer core (IASPEI extension)"),

	/**
	 * PcSSKP with K leg bottoming in the lower outer core (IASPEI extension)
	 */
	PcSSKPbc(null, "PcSSKP with K leg bottoming in the lower outer core (IASPEI extension)"),

	/**
	 * SKPPcS with K leg bottoming in the lower outer core (IASPEI extension)
	 */
	SKPPcSbc(null, "SKPPcS with K leg bottoming in the lower outer core (IASPEI extension)"),

	/**
	 * SKPScP with K leg bottoming in the lower outer core (IASPEI extension)
	 */
	SKPScPbc(null, "SKPScP with K leg bottoming in the lower outer core (IASPEI extension)"),

	/**
	 * SKPScS with K leg bottoming in the lower outer core (IASPEI extension)
	 */
	SKPScSbc(null, "SKPScS with K leg bottoming in the lower outer core (IASPEI extension)"),

	/**
	 * ScPPKP with K leg bottoming in the lower outer core (IASPEI extension)
	 */
	ScPPKPbc(null, "ScPPKP with K leg bottoming in the lower outer core (IASPEI extension)"),

	/**
	 * ScPPKS with K leg bottoming in the lower outer core (IASPEI extension)
	 */
	ScPPKSbc(null, "ScPPKS with K leg bottoming in the lower outer core (IASPEI extension)"),

	/**
	 * ScPSKP with K leg bottoming in the lower outer core (IASPEI extension)
	 */
	ScPSKPbc(null, "ScPSKP with K leg bottoming in the lower outer core (IASPEI extension)"),

	/**
	 * ScSPKP with K leg bottoming in the lower outer core (IASPEI extension)
	 */
	ScSPKPbc(null, "ScSPKP with K leg bottoming in the lower outer core (IASPEI extension)"),

	/**
	 * ScSPKS with K leg bottoming in the lower outer core (IASPEI extension)
	 */
	ScSPKSbc(null, "ScSPKS with K leg bottoming in the lower outer core (IASPEI extension)"),

	/**
	 * ScSSKP with K leg bottoming in the lower outer core (IASPEI extension)
	 */
	ScSSKPbc(null, "ScSSKP with K leg bottoming in the lower outer core (IASPEI extension)"),

	/**
	 * PKPPcPbc resulting from reflection of upgoing P at the free surface
	 */
	pPKPPcPbc(GeoAttributes.PSLOWNESS, "PKPPcPbc resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKPPcSbc resulting from reflection of upgoing P at the free surface
	 */
	pPKPPcSbc(null, "PKPPcSbc resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKPScPbc resulting from reflection of upgoing P at the free surface
	 */
	pPKPScPbc(null, "PKPScPbc resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKPScSbc resulting from reflection of upgoing P at the free surface
	 */
	pPKPScSbc(null, "PKPScSbc resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKSPcPbc resulting from reflection of upgoing P at the free surface
	 */
	pPKSPcPbc(null, "PKSPcPbc resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKSPcSbc resulting from reflection of upgoing P at the free surface
	 */
	pPKSPcSbc(null, "PKSPcSbc resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKSScPbc resulting from reflection of upgoing P at the free surface
	 */
	pPKSScPbc(null, "PKSScPbc resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKSScSbc resulting from reflection of upgoing P at the free surface
	 */
	pPKSScSbc(null, "PKSScSbc resulting from reflection of upgoing P at the free surface"),

	/**
	 * PcPPKPbc resulting from reflection of upgoing P at the free surface
	 */
	pPcPPKPbc(GeoAttributes.PSLOWNESS, "PcPPKPbc resulting from reflection of upgoing P at the free surface"),

	/**
	 * PcPPKSbc resulting from reflection of upgoing P at the free surface
	 */
	pPcPPKSbc(null, "PcPPKSbc resulting from reflection of upgoing P at the free surface"),

	/**
	 * PcPSKPbc resulting from reflection of upgoing P at the free surface
	 */
	pPcPSKPbc(null, "PcPSKPbc resulting from reflection of upgoing P at the free surface"),

	/**
	 * PcSPKPbc resulting from reflection of upgoing P at the free surface
	 */
	pPcSPKPbc(null, "PcSPKPbc resulting from reflection of upgoing P at the free surface"),

	/**
	 * PcSPKSbc resulting from reflection of upgoing P at the free surface
	 */
	pPcSPKSbc(null, "PcSPKSbc resulting from reflection of upgoing P at the free surface"),

	/**
	 * PcSSKPbc resulting from reflection of upgoing P at the free surface
	 */
	pPcSSKPbc(null, "PcSSKPbc resulting from reflection of upgoing P at the free surface"),

	/**
	 * SKPPcPbc resulting from converted reflection of upgoing P at the free surface
	 */
	pSKPPcPbc(null, "SKPPcPbc resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKPPcSbc resulting from converted reflection of upgoing P at the free surface
	 */
	pSKPPcSbc(null, "SKPPcSbc resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKPScPbc resulting from converted reflection of upgoing P at the free surface
	 */
	pSKPScPbc(null, "SKPScPbc resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKPScSbc resulting from converted reflection of upgoing P at the free surface
	 */
	pSKPScSbc(null, "SKPScSbc resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * ScPPKPbc resulting from converted reflection of upgoing P at the free surface
	 */
	pScPPKPbc(null, "ScPPKPbc resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * ScPPKSbc resulting from converted reflection of upgoing P at the free surface
	 */
	pScPPKSbc(null, "ScPPKSbc resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * ScPSKPbc resulting from converted reflection of upgoing P at the free surface
	 */
	pScPSKPbc(null, "ScPSKPbc resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * ScSPKPbc resulting from converted reflection of upgoing P at the free surface
	 */
	pScSPKPbc(null, "ScSPKPbc resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * ScSPKSbc resulting from converted reflection of upgoing P at the free surface
	 */
	pScSPKSbc(null, "ScSPKSbc resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * ScSSKPbc resulting from converted reflection of upgoing P at the free surface
	 */
	pScSSKPbc(null, "ScSSKPbc resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * PKPPcPbc resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKPPcPbc(GeoAttributes.PSLOWNESS,
			"PKPPcPbc resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKPPcSbc resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKPPcSbc(null, "PKPPcSbc resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKPScPbc resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKPScPbc(null, "PKPScPbc resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKPScSbc resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKPScSbc(null, "PKPScSbc resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKSPcPbc resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKSPcPbc(null, "PKSPcPbc resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKSPcSbc resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKSPcSbc(null, "PKSPcSbc resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKSScPbc resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKSScPbc(null, "PKSScPbc resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKSScSbc resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKSScSbc(null, "PKSScSbc resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PcPPKPbc resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPcPPKPbc(GeoAttributes.PSLOWNESS,
			"PcPPKPbc resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PcPPKSbc resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPcPPKSbc(null, "PcPPKSbc resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PcPSKPbc resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPcPSKPbc(null, "PcPSKPbc resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PcSPKPbc resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPcSPKPbc(null, "PcSPKPbc resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PcSPKSbc resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPcSPKSbc(null, "PcSPKSbc resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PcSSKPbc resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPcSSKPbc(null, "PcSSKPbc resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKPPcPbc resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKPPcPbc(GeoAttributes.PSLOWNESS, "PKPPcPbc resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKPPcSbc resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKPPcSbc(null, "PKPPcSbc resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKPScPbc resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKPScPbc(null, "PKPScPbc resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKPScSbc resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKPScSbc(null, "PKPScSbc resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKSPcPbc resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKSPcPbc(null, "PKSPcPbc resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKSPcSbc resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKSPcSbc(null, "PKSPcSbc resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKSScPbc resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKSScPbc(null, "PKSScPbc resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKSScSbc resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKSScSbc(null, "PKSScSbc resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PcPPKPbc resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPcPPKPbc(GeoAttributes.PSLOWNESS, "PcPPKPbc resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PcPPKSbc resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPcPPKSbc(null, "PcPPKSbc resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PcPSKPbc resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPcPSKPbc(null, "PcPSKPbc resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PcSPKPbc resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPcSPKPbc(null, "PcSPKPbc resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PcSPKSbc resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPcSPKSbc(null, "PcSPKSbc resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PcSSKPbc resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPcSSKPbc(null, "PcSSKPbc resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKPPcPbc resulting from converted reflection of upgoing S at the free surface
	 */
	sPKPPcPbc(null, "PKPPcPbc resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKPPcSbc resulting from converted reflection of upgoing S at the free surface
	 */
	sPKPPcSbc(null, "PKPPcSbc resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKPScPbc resulting from converted reflection of upgoing S at the free surface
	 */
	sPKPScPbc(null, "PKPScPbc resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKPScSbc resulting from converted reflection of upgoing S at the free surface
	 */
	sPKPScSbc(null, "PKPScSbc resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKSPcPbc resulting from converted reflection of upgoing S at the free surface
	 */
	sPKSPcPbc(null, "PKSPcPbc resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKSPcSbc resulting from converted reflection of upgoing S at the free surface
	 */
	sPKSPcSbc(null, "PKSPcSbc resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKSScPbc resulting from converted reflection of upgoing S at the free surface
	 */
	sPKSScPbc(null, "PKSScPbc resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKSScSbc resulting from converted reflection of upgoing S at the free surface
	 */
	sPKSScSbc(null, "PKSScSbc resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PcPPKPbc resulting from converted reflection of upgoing S at the free surface
	 */
	sPcPPKPbc(null, "PcPPKPbc resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PcPPKSbc resulting from converted reflection of upgoing S at the free surface
	 */
	sPcPPKSbc(null, "PcPPKSbc resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PcPSKPbc resulting from converted reflection of upgoing S at the free surface
	 */
	sPcPSKPbc(null, "PcPSKPbc resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PcSPKPbc resulting from converted reflection of upgoing S at the free surface
	 */
	sPcSPKPbc(null, "PcSPKPbc resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PcSPKSbc resulting from converted reflection of upgoing S at the free surface
	 */
	sPcSPKSbc(null, "PcSPKSbc resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PcSSKPbc resulting from converted reflection of upgoing S at the free surface
	 */
	sPcSSKPbc(null, "PcSSKPbc resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * SKPPcPbc resulting from reflection of upgoing S at the free surface
	 */
	sSKPPcPbc(null, "SKPPcPbc resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKPPcSbc resulting from reflection of upgoing S at the free surface
	 */
	sSKPPcSbc(null, "SKPPcSbc resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKPScPbc resulting from reflection of upgoing S at the free surface
	 */
	sSKPScPbc(null, "SKPScPbc resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKPScSbc resulting from reflection of upgoing S at the free surface
	 */
	sSKPScSbc(null, "SKPScSbc resulting from reflection of upgoing S at the free surface"),

	/**
	 * ScPPKPbc resulting from reflection of upgoing S at the free surface
	 */
	sScPPKPbc(null, "ScPPKPbc resulting from reflection of upgoing S at the free surface"),

	/**
	 * ScPPKSbc resulting from reflection of upgoing S at the free surface
	 */
	sScPPKSbc(null, "ScPPKSbc resulting from reflection of upgoing S at the free surface"),

	/**
	 * ScPSKPbc resulting from reflection of upgoing S at the free surface
	 */
	sScPSKPbc(null, "ScPSKPbc resulting from reflection of upgoing S at the free surface"),

	/**
	 * ScSPKPbc resulting from reflection of upgoing S at the free surface
	 */
	sScSPKPbc(null, "ScSPKPbc resulting from reflection of upgoing S at the free surface"),

	/**
	 * ScSPKSbc resulting from reflection of upgoing S at the free surface
	 */
	sScSPKSbc(null, "ScSPKSbc resulting from reflection of upgoing S at the free surface"),

	/**
	 * ScSSKPbc resulting from reflection of upgoing S at the free surface
	 */
	sScSSKPbc(null, "ScSSKPbc resulting from reflection of upgoing S at the free surface"),

	/**
	 * PcPPKS with K leg bottoming in the lower outer core (IASPEI extension)
	 */
	PcPPKSbc(null, "PcPPKS with K leg bottoming in the lower outer core (IASPEI extension)"),

	/**
	 * SKPPcP with K leg bottoming in the lower outer core (IASPEI extension)
	 */
	SKPPcPbc(null, "SKPPcP with K leg bottoming in the lower outer core (IASPEI extension)"),

	/**
	 * PKPPcP with K leg bottoming in the inner core (IASPEI extension)
	 */
	PKPPcPdf(GeoAttributes.PSLOWNESS, "PKPPcP with K leg bottoming in the inner core (IASPEI extension)"),

	/**
	 * PKPPcS with K leg bottoming in the inner core (IASPEI extension)
	 */
	PKPPcSdf(null, "PKPPcS with K leg bottoming in the inner core (IASPEI extension)"),

	/**
	 * PKPScP with K leg bottoming in the inner core (IASPEI extension)
	 */
	PKPScPdf(null, "PKPScP with K leg bottoming in the inner core (IASPEI extension)"),

	/**
	 * PKPScS with K leg bottoming in the inner core (IASPEI extension)
	 */
	PKPScSdf(null, "PKPScS with K leg bottoming in the inner core (IASPEI extension)"),

	/**
	 * PKSPcP with K leg bottoming in the inner core (IASPEI extension)
	 */
	PKSPcPdf(null, "PKSPcP with K leg bottoming in the inner core (IASPEI extension)"),

	/**
	 * PKSPcS with K leg bottoming in the inner core (IASPEI extension)
	 */
	PKSPcSdf(null, "PKSPcS with K leg bottoming in the inner core (IASPEI extension)"),

	/**
	 * PKSScP with K leg bottoming in the inner core (IASPEI extension)
	 */
	PKSScPdf(null, "PKSScP with K leg bottoming in the inner core (IASPEI extension)"),

	/**
	 * PKSScS with K leg bottoming in the inner core (IASPEI extension)
	 */
	PKSScSdf(null, "PKSScS with K leg bottoming in the inner core (IASPEI extension)"),

	/**
	 * PcPPKP with K leg bottoming in the inner core (IASPEI extension)
	 */
	PcPPKPdf(GeoAttributes.PSLOWNESS, "PcPPKP with K leg bottoming in the inner core (IASPEI extension)"),

	/**
	 * PcPSKP with K leg bottoming in the inner core (IASPEI extension)
	 */
	PcPSKPdf(null, "PcPSKP with K leg bottoming in the inner core (IASPEI extension)"),

	/**
	 * PcSPKP with K leg bottoming in the inner core (IASPEI extension)
	 */
	PcSPKPdf(null, "PcSPKP with K leg bottoming in the inner core (IASPEI extension)"),

	/**
	 * PcSPKS with K leg bottoming in the inner core (IASPEI extension)
	 */
	PcSPKSdf(null, "PcSPKS with K leg bottoming in the inner core (IASPEI extension)"),

	/**
	 * PcSSKP with K leg bottoming in the inner core (IASPEI extension)
	 */
	PcSSKPdf(null, "PcSSKP with K leg bottoming in the inner core (IASPEI extension)"),

	/**
	 * SKPPcS with K leg bottoming in the inner core (IASPEI extension)
	 */
	SKPPcSdf(null, "SKPPcS with K leg bottoming in the inner core (IASPEI extension)"),

	/**
	 * SKPScP with K leg bottoming in the inner core (IASPEI extension)
	 */
	SKPScPdf(null, "SKPScP with K leg bottoming in the inner core (IASPEI extension)"),

	/**
	 * SKPScS with K leg bottoming in the inner core (IASPEI extension)
	 */
	SKPScSdf(null, "SKPScS with K leg bottoming in the inner core (IASPEI extension)"),

	/**
	 * ScPPKP with K leg bottoming in the inner core (IASPEI extension)
	 */
	ScPPKPdf(null, "ScPPKP with K leg bottoming in the inner core (IASPEI extension)"),

	/**
	 * ScPPKS with K leg bottoming in the inner core (IASPEI extension)
	 */
	ScPPKSdf(null, "ScPPKS with K leg bottoming in the inner core (IASPEI extension)"),

	/**
	 * ScPSKP with K leg bottoming in the inner core (IASPEI extension)
	 */
	ScPSKPdf(null, "ScPSKP with K leg bottoming in the inner core (IASPEI extension)"),

	/**
	 * ScSPKP with K leg bottoming in the inner core (IASPEI extension)
	 */
	ScSPKPdf(null, "ScSPKP with K leg bottoming in the inner core (IASPEI extension)"),

	/**
	 * ScSPKS with K leg bottoming in the inner core (IASPEI extension)
	 */
	ScSPKSdf(null, "ScSPKS with K leg bottoming in the inner core (IASPEI extension)"),

	/**
	 * ScSSKP with K leg bottoming in the inner core (IASPEI extension)
	 */
	ScSSKPdf(null, "ScSSKP with K leg bottoming in the inner core (IASPEI extension)"),

	/**
	 * PKPPcPdf resulting from reflection of upgoing P at the free surface
	 */
	pPKPPcPdf(GeoAttributes.PSLOWNESS, "PKPPcPdf resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKPPcSdf resulting from reflection of upgoing P at the free surface
	 */
	pPKPPcSdf(null, "PKPPcSdf resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKPScPdf resulting from reflection of upgoing P at the free surface
	 */
	pPKPScPdf(null, "PKPScPdf resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKPScSdf resulting from reflection of upgoing P at the free surface
	 */
	pPKPScSdf(null, "PKPScSdf resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKSPcPdf resulting from reflection of upgoing P at the free surface
	 */
	pPKSPcPdf(null, "PKSPcPdf resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKSPcSdf resulting from reflection of upgoing P at the free surface
	 */
	pPKSPcSdf(null, "PKSPcSdf resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKSScPdf resulting from reflection of upgoing P at the free surface
	 */
	pPKSScPdf(null, "PKSScPdf resulting from reflection of upgoing P at the free surface"),

	/**
	 * PKSScSdf resulting from reflection of upgoing P at the free surface
	 */
	pPKSScSdf(null, "PKSScSdf resulting from reflection of upgoing P at the free surface"),

	/**
	 * PcPPKPdf resulting from reflection of upgoing P at the free surface
	 */
	pPcPPKPdf(GeoAttributes.PSLOWNESS, "PcPPKPdf resulting from reflection of upgoing P at the free surface"),

	/**
	 * PcPPKSdf resulting from reflection of upgoing P at the free surface
	 */
	pPcPPKSdf(null, "PcPPKSdf resulting from reflection of upgoing P at the free surface"),

	/**
	 * PcPSKPdf resulting from reflection of upgoing P at the free surface
	 */
	pPcPSKPdf(null, "PcPSKPdf resulting from reflection of upgoing P at the free surface"),

	/**
	 * PcSPKPdf resulting from reflection of upgoing P at the free surface
	 */
	pPcSPKPdf(null, "PcSPKPdf resulting from reflection of upgoing P at the free surface"),

	/**
	 * PcSPKSdf resulting from reflection of upgoing P at the free surface
	 */
	pPcSPKSdf(null, "PcSPKSdf resulting from reflection of upgoing P at the free surface"),

	/**
	 * PcSSKPdf resulting from reflection of upgoing P at the free surface
	 */
	pPcSSKPdf(null, "PcSSKPdf resulting from reflection of upgoing P at the free surface"),

	/**
	 * SKPPcPdf resulting from converted reflection of upgoing P at the free surface
	 */
	pSKPPcPdf(null, "SKPPcPdf resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKPPcSdf resulting from converted reflection of upgoing P at the free surface
	 */
	pSKPPcSdf(null, "SKPPcSdf resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKPScPdf resulting from converted reflection of upgoing P at the free surface
	 */
	pSKPScPdf(null, "SKPScPdf resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKPScSdf resulting from converted reflection of upgoing P at the free surface
	 */
	pSKPScSdf(null, "SKPScSdf resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * ScPPKPdf resulting from converted reflection of upgoing P at the free surface
	 */
	pScPPKPdf(null, "ScPPKPdf resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * ScPPKSdf resulting from converted reflection of upgoing P at the free surface
	 */
	pScPPKSdf(null, "ScPPKSdf resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * ScPSKPdf resulting from converted reflection of upgoing P at the free surface
	 */
	pScPSKPdf(null, "ScPSKPdf resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * ScSPKPdf resulting from converted reflection of upgoing P at the free surface
	 */
	pScSPKPdf(null, "ScSPKPdf resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * ScSPKSdf resulting from converted reflection of upgoing P at the free surface
	 */
	pScSPKSdf(null, "ScSPKSdf resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * ScSSKPdf resulting from converted reflection of upgoing P at the free surface
	 */
	pScSSKPdf(null, "ScSSKPdf resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * PKPPcPdf resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKPPcPdf(GeoAttributes.PSLOWNESS,
			"PKPPcPdf resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKPPcSdf resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKPPcSdf(null, "PKPPcSdf resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKPScPdf resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKPScPdf(null, "PKPScPdf resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKPScSdf resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKPScSdf(null, "PKPScSdf resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKSPcPdf resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKSPcPdf(null, "PKSPcPdf resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKSPcSdf resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKSPcSdf(null, "PKSPcSdf resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKSScPdf resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKSScPdf(null, "PKSScPdf resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKSScSdf resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPKSScSdf(null, "PKSScSdf resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PcPPKPdf resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPcPPKPdf(GeoAttributes.PSLOWNESS,
			"PcPPKPdf resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PcPPKSdf resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPcPPKSdf(null, "PcPPKSdf resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PcPSKPdf resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPcPSKPdf(null, "PcPSKPdf resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PcSPKPdf resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPcSPKPdf(null, "PcSPKPdf resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PcSPKSdf resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPcSPKSdf(null, "PcSPKSdf resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PcSSKPdf resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPcSSKPdf(null, "PcSSKPdf resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PKPPcPdf resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKPPcPdf(GeoAttributes.PSLOWNESS, "PKPPcPdf resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKPPcSdf resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKPPcSdf(null, "PKPPcSdf resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKPScPdf resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKPScPdf(null, "PKPScPdf resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKPScSdf resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKPScSdf(null, "PKPScSdf resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKSPcPdf resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKSPcPdf(null, "PKSPcPdf resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKSPcSdf resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKSPcSdf(null, "PKSPcSdf resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKSScPdf resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKSScPdf(null, "PKSScPdf resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKSScSdf resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPKSScSdf(null, "PKSScSdf resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PcPPKPdf resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPcPPKPdf(GeoAttributes.PSLOWNESS, "PcPPKPdf resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PcPPKSdf resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPcPPKSdf(null, "PcPPKSdf resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PcPSKPdf resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPcPSKPdf(null, "PcPSKPdf resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PcSPKPdf resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPcSPKPdf(null, "PcSPKPdf resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PcSPKSdf resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPcSPKSdf(null, "PcSPKSdf resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PcSSKPdf resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPcSSKPdf(null, "PcSSKPdf resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKPPcPdf resulting from converted reflection of upgoing S at the free surface
	 */
	sPKPPcPdf(null, "PKPPcPdf resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKPPcSdf resulting from converted reflection of upgoing S at the free surface
	 */
	sPKPPcSdf(null, "PKPPcSdf resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKPScPdf resulting from converted reflection of upgoing S at the free surface
	 */
	sPKPScPdf(null, "PKPScPdf resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKPScSdf resulting from converted reflection of upgoing S at the free surface
	 */
	sPKPScSdf(null, "PKPScSdf resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKSPcPdf resulting from converted reflection of upgoing S at the free surface
	 */
	sPKSPcPdf(null, "PKSPcPdf resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKSPcSdf resulting from converted reflection of upgoing S at the free surface
	 */
	sPKSPcSdf(null, "PKSPcSdf resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKSScPdf resulting from converted reflection of upgoing S at the free surface
	 */
	sPKSScPdf(null, "PKSScPdf resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKSScSdf resulting from converted reflection of upgoing S at the free surface
	 */
	sPKSScSdf(null, "PKSScSdf resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PcPPKPdf resulting from converted reflection of upgoing S at the free surface
	 */
	sPcPPKPdf(null, "PcPPKPdf resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PcPPKSdf resulting from converted reflection of upgoing S at the free surface
	 */
	sPcPPKSdf(null, "PcPPKSdf resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PcPSKPdf resulting from converted reflection of upgoing S at the free surface
	 */
	sPcPSKPdf(null, "PcPSKPdf resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PcSPKPdf resulting from converted reflection of upgoing S at the free surface
	 */
	sPcSPKPdf(null, "PcSPKPdf resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PcSPKSdf resulting from converted reflection of upgoing S at the free surface
	 */
	sPcSPKSdf(null, "PcSPKSdf resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PcSSKPdf resulting from converted reflection of upgoing S at the free surface
	 */
	sPcSSKPdf(null, "PcSSKPdf resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * SKPPcPdf resulting from reflection of upgoing S at the free surface
	 */
	sSKPPcPdf(null, "SKPPcPdf resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKPPcSdf resulting from reflection of upgoing S at the free surface
	 */
	sSKPPcSdf(null, "SKPPcSdf resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKPScPdf resulting from reflection of upgoing S at the free surface
	 */
	sSKPScPdf(null, "SKPScPdf resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKPScSdf resulting from reflection of upgoing S at the free surface
	 */
	sSKPScSdf(null, "SKPScSdf resulting from reflection of upgoing S at the free surface"),

	/**
	 * ScPPKPdf resulting from reflection of upgoing S at the free surface
	 */
	sScPPKPdf(null, "ScPPKPdf resulting from reflection of upgoing S at the free surface"),

	/**
	 * ScPPKSdf resulting from reflection of upgoing S at the free surface
	 */
	sScPPKSdf(null, "ScPPKSdf resulting from reflection of upgoing S at the free surface"),

	/**
	 * ScPSKPdf resulting from reflection of upgoing S at the free surface
	 */
	sScPSKPdf(null, "ScPSKPdf resulting from reflection of upgoing S at the free surface"),

	/**
	 * ScSPKPdf resulting from reflection of upgoing S at the free surface
	 */
	sScSPKPdf(null, "ScSPKPdf resulting from reflection of upgoing S at the free surface"),

	/**
	 * ScSPKSdf resulting from reflection of upgoing S at the free surface
	 */
	sScSPKSdf(null, "ScSPKSdf resulting from reflection of upgoing S at the free surface"),

	/**
	 * ScSSKPdf resulting from reflection of upgoing S at the free surface
	 */
	sScSSKPdf(null, "ScSSKPdf resulting from reflection of upgoing S at the free surface"),

	/**
	 * PcPPKS with K leg bottoming in the inner core (IASPEI extension)
	 */
	PcPPKSdf(null, "PcPPKS with K leg bottoming in the inner core (IASPEI extension)"),

	/**
	 * SKPPcP with K leg bottoming in the inner core (IASPEI extension)
	 */
	SKPPcPdf(null, "SKPPcP with K leg bottoming in the inner core (IASPEI extension)"),

	/**
	 * P2KS with K leg bottoming in the upper outer core (IASPEI extension)
	 */
	P2KSab(null, "P2KS with K leg bottoming in the upper outer core (IASPEI extension)"),

	/**
	 * S2KP with K leg bottoming in the upper outer core (IASPEI extension)
	 */
	S2KPab(null, "S2KP with K leg bottoming in the upper outer core (IASPEI extension)"),

	/**
	 * P2KSab resulting from reflection of upgoing P at the free surface
	 */
	pP2KSab(null, "P2KSab resulting from reflection of upgoing P at the free surface"),

	/**
	 * S2KPab resulting from converted reflection of upgoing P at the free surface
	 */
	pS2KPab(null, "S2KPab resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * P2KPab resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP2KPab(GeoAttributes.PSLOWNESS, "P2KPab resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P2KSab resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP2KSab(null, "P2KSab resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P2KSab resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP2KSab(null, "P2KSab resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P2KSab resulting from converted reflection of upgoing S at the free surface
	 */
	sP2KSab(null, "P2KSab resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * S2KPab resulting from reflection of upgoing S at the free surface
	 */
	sS2KPab(null, "S2KPab resulting from reflection of upgoing S at the free surface"),

	/**
	 * P2KS with K leg bottoming in the lower outer core (IASPEI extension)
	 */
	P2KSbc(null, "P2KS with K leg bottoming in the lower outer core (IASPEI extension)"),

	/**
	 * S2KP with K leg bottoming in the lower outer core (IASPEI extension)
	 */
	S2KPbc(null, "S2KP with K leg bottoming in the lower outer core (IASPEI extension)"),

	/**
	 * P2KSbc resulting from reflection of upgoing P at the free surface
	 */
	pP2KSbc(null, "P2KSbc resulting from reflection of upgoing P at the free surface"),

	/**
	 * S2KPbc resulting from converted reflection of upgoing P at the free surface
	 */
	pS2KPbc(null, "S2KPbc resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * P2KPbc resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP2KPbc(GeoAttributes.PSLOWNESS, "P2KPbc resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P2KSbc resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP2KSbc(null, "P2KSbc resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P2KSbc resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP2KSbc(null, "P2KSbc resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P2KSbc resulting from converted reflection of upgoing S at the free surface
	 */
	sP2KSbc(null, "P2KSbc resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * S2KPbc resulting from reflection of upgoing S at the free surface
	 */
	sS2KPbc(null, "S2KPbc resulting from reflection of upgoing S at the free surface"),

	/**
	 * P2KS with K leg bottoming in the inner core (IASPEI extension)
	 */
	P2KSdf(null, "P2KS with K leg bottoming in the inner core (IASPEI extension)"),

	/**
	 * S2KP with K leg bottoming in the inner core (IASPEI extension)
	 */
	S2KPdf(null, "S2KP with K leg bottoming in the inner core (IASPEI extension)"),

	/**
	 * P2KSdf resulting from reflection of upgoing P at the free surface
	 */
	pP2KSdf(null, "P2KSdf resulting from reflection of upgoing P at the free surface"),

	/**
	 * S2KPdf resulting from converted reflection of upgoing P at the free surface
	 */
	pS2KPdf(null, "S2KPdf resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * P2KPdf resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP2KPdf(GeoAttributes.PSLOWNESS, "P2KPdf resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P2KSdf resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP2KSdf(null, "P2KSdf resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P2KSdf resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP2KSdf(null, "P2KSdf resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P2KSdf resulting from converted reflection of upgoing S at the free surface
	 */
	sP2KSdf(null, "P2KSdf resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * S2KPdf resulting from reflection of upgoing S at the free surface
	 */
	sS2KPdf(null, "S2KPdf resulting from reflection of upgoing S at the free surface"),

	/**
	 * P3KS with K leg bottoming in the upper outer core (IASPEI extension)
	 */
	P3KSab(null, "P3KS with K leg bottoming in the upper outer core (IASPEI extension)"),

	/**
	 * S3KP with K leg bottoming in the upper outer core (IASPEI extension)
	 */
	S3KPab(null, "S3KP with K leg bottoming in the upper outer core (IASPEI extension)"),

	/**
	 * P3KSab resulting from reflection of upgoing P at the free surface
	 */
	pP3KSab(null, "P3KSab resulting from reflection of upgoing P at the free surface"),

	/**
	 * S3KPab resulting from converted reflection of upgoing P at the free surface
	 */
	pS3KPab(null, "S3KPab resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * P3KSab resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP3KSab(null, "P3KSab resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P3KSab resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP3KSab(null, "P3KSab resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P3KSab resulting from converted reflection of upgoing S at the free surface
	 */
	sP3KSab(null, "P3KSab resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * S3KPab resulting from reflection of upgoing S at the free surface
	 */
	sS3KPab(null, "S3KPab resulting from reflection of upgoing S at the free surface"),

	/**
	 * P3KS with K leg bottoming in the lower outer core (IASPEI extension)
	 */
	P3KSbc(null, "P3KS with K leg bottoming in the lower outer core (IASPEI extension)"),

	/**
	 * S3KP with K leg bottoming in the lower outer core (IASPEI extension)
	 */
	S3KPbc(null, "S3KP with K leg bottoming in the lower outer core (IASPEI extension)"),

	/**
	 * P3KSbc resulting from reflection of upgoing P at the free surface
	 */
	pP3KSbc(null, "P3KSbc resulting from reflection of upgoing P at the free surface"),

	/**
	 * S3KPbc resulting from converted reflection of upgoing P at the free surface
	 */
	pS3KPbc(null, "S3KPbc resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * P3KSbc resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP3KSbc(null, "P3KSbc resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P3KSbc resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP3KSbc(null, "P3KSbc resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P3KSbc resulting from converted reflection of upgoing S at the free surface
	 */
	sP3KSbc(null, "P3KSbc resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * S3KPbc resulting from reflection of upgoing S at the free surface
	 */
	sS3KPbc(null, "S3KPbc resulting from reflection of upgoing S at the free surface"),

	/**
	 * P3KS with K leg bottoming in the inner core (IASPEI extension)
	 */
	P3KSdf(null, "P3KS with K leg bottoming in the inner core (IASPEI extension)"),

	/**
	 * S3KP with K leg bottoming in the inner core (IASPEI extension)
	 */
	S3KPdf(null, "S3KP with K leg bottoming in the inner core (IASPEI extension)"),

	/**
	 * P3KSdf resulting from reflection of upgoing P at the free surface
	 */
	pP3KSdf(null, "P3KSdf resulting from reflection of upgoing P at the free surface"),

	/**
	 * S3KPdf resulting from converted reflection of upgoing P at the free surface
	 */
	pS3KPdf(null, "S3KPdf resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * P3KSdf resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP3KSdf(null, "P3KSdf resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P3KSdf resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP3KSdf(null, "P3KSdf resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P3KSdf resulting from converted reflection of upgoing S at the free surface
	 */
	sP3KSdf(null, "P3KSdf resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * S3KPdf resulting from reflection of upgoing S at the free surface
	 */
	sS3KPdf(null, "S3KPdf resulting from reflection of upgoing S at the free surface"),

	/**
	 * P4KS with K leg bottoming in the upper outer core (IASPEI extension)
	 */
	P4KSab(null, "P4KS with K leg bottoming in the upper outer core (IASPEI extension)"),

	/**
	 * S4KP with K leg bottoming in the upper outer core (IASPEI extension)
	 */
	S4KPab(null, "S4KP with K leg bottoming in the upper outer core (IASPEI extension)"),

	/**
	 * P4KSab resulting from reflection of upgoing P at the free surface
	 */
	pP4KSab(null, "P4KSab resulting from reflection of upgoing P at the free surface"),

	/**
	 * S4KPab resulting from converted reflection of upgoing P at the free surface
	 */
	pS4KPab(null, "S4KPab resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * P4KSab resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP4KSab(null, "P4KSab resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P4KSab resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP4KSab(null, "P4KSab resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P4KSab resulting from converted reflection of upgoing S at the free surface
	 */
	sP4KSab(null, "P4KSab resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * S4KPab resulting from reflection of upgoing S at the free surface
	 */
	sS4KPab(null, "S4KPab resulting from reflection of upgoing S at the free surface"),

	/**
	 * P4KS with K leg bottoming in the lower outer core (IASPEI extension)
	 */
	P4KSbc(null, "P4KS with K leg bottoming in the lower outer core (IASPEI extension)"),

	/**
	 * S4KP with K leg bottoming in the lower outer core (IASPEI extension)
	 */
	S4KPbc(null, "S4KP with K leg bottoming in the lower outer core (IASPEI extension)"),

	/**
	 * P4KSbc resulting from reflection of upgoing P at the free surface
	 */
	pP4KSbc(null, "P4KSbc resulting from reflection of upgoing P at the free surface"),

	/**
	 * S4KPbc resulting from converted reflection of upgoing P at the free surface
	 */
	pS4KPbc(null, "S4KPbc resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * P4KSbc resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP4KSbc(null, "P4KSbc resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P4KSbc resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP4KSbc(null, "P4KSbc resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P4KSbc resulting from converted reflection of upgoing S at the free surface
	 */
	sP4KSbc(null, "P4KSbc resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * S4KPbc resulting from reflection of upgoing S at the free surface
	 */
	sS4KPbc(null, "S4KPbc resulting from reflection of upgoing S at the free surface"),

	/**
	 * P4KS with K leg bottoming in the inner core (IASPEI extension)
	 */
	P4KSdf(null, "P4KS with K leg bottoming in the inner core (IASPEI extension)"),

	/**
	 * S4KP with K leg bottoming in the inner core (IASPEI extension)
	 */
	S4KPdf(null, "S4KP with K leg bottoming in the inner core (IASPEI extension)"),

	/**
	 * P4KSdf resulting from reflection of upgoing P at the free surface
	 */
	pP4KSdf(null, "P4KSdf resulting from reflection of upgoing P at the free surface"),

	/**
	 * S4KPdf resulting from converted reflection of upgoing P at the free surface
	 */
	pS4KPdf(null, "S4KPdf resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * P4KSdf resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP4KSdf(null, "P4KSdf resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P4KSdf resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP4KSdf(null, "P4KSdf resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P4KSdf resulting from converted reflection of upgoing S at the free surface
	 */
	sP4KSdf(null, "P4KSdf resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * S4KPdf resulting from reflection of upgoing S at the free surface
	 */
	sS4KPdf(null, "S4KPdf resulting from reflection of upgoing S at the free surface"),

	/**
	 * (alternate: P660+P) P reflection from outer side of a discontinuity at depth
	 * 660
	 */
	P660P(GeoAttributes.PSLOWNESS, "(alternate: P660+P) P reflection from outer side of a discontinuity at depth 660"),

	/**
	 * (alternate: P410+P) P reflection from outer side of a discontinuity at depth
	 * 410
	 */
	P410P(GeoAttributes.PSLOWNESS, "(alternate: P410+P) P reflection from outer side of a discontinuity at depth 410"),

	/**
	 * (alternate: P210+P) P reflection from outer side of a discontinuity at depth
	 * 210
	 */
	P210P(GeoAttributes.PSLOWNESS, "(alternate: P210+P) P reflection from outer side of a discontinuity at depth 210"),

	/**
	 * (alternate: P660+S) P to S converted reflection from outer side of
	 * discontinuity at depth 660
	 */
	P660S(null,
			"(alternate: P660+S) P to S converted reflection from outer side of" + "<BR>discontinuity at depth 660"),

	/**
	 * (alternate: P410+S) P to S converted reflection from outer side of
	 * discontinuity at depth 410
	 */
	P410S(null,
			"(alternate: P410+S) P to S converted reflection from outer side of" + "<BR>discontinuity at depth 410"),

	/**
	 * (alternate: P210+S) P to S converted reflection from outer side of
	 * discontinuity at depth 210
	 */
	P210S(null,
			"(alternate: P210+S) P to S converted reflection from outer side of" + "<BR>discontinuity at depth 210"),

	/**
	 * (alternate: S660+S) S reflection from outer side of a discontinuity at depth
	 * 660
	 */
	S660S(GeoAttributes.SSLOWNESS, "(alternate: S660+S) S reflection from outer side of a discontinuity at depth 660"),

	/**
	 * (alternate: S410+S) S reflection from outer side of a discontinuity at depth
	 * 410
	 */
	S410S(GeoAttributes.SSLOWNESS, "(alternate: S410+S) S reflection from outer side of a discontinuity at depth 410"),

	/**
	 * (alternate: S210+S) S reflection from outer side of a discontinuity at depth
	 * 210
	 */
	S210S(GeoAttributes.SSLOWNESS, "(alternate: S210+S) S reflection from outer side of a discontinuity at depth 210"),

	/**
	 * (alternate: S660+P) S to P converted reflection from outer side of
	 * discontinuity at depth 660
	 */
	S660P(null,
			"(alternate: S660+P) S to P converted reflection from outer side of" + "<BR>discontinuity at depth 660"),

	/**
	 * (alternate: S410+P) S to P converted reflection from outer side of
	 * discontinuity at depth 410
	 */
	S410P(null,
			"(alternate: S410+P) S to P converted reflection from outer side of" + "<BR>discontinuity at depth 410"),

	/**
	 * (alternate: S210+P) S to P converted reflection from outer side of
	 * discontinuity at depth 210
	 */
	S210P(null,
			"(alternate: S210+P) S to P converted reflection from outer side of" + "<BR>discontinuity at depth 210"),

	/**
	 * P210+P resulting from reflection of upgoing P at the free surface
	 */
	pP210P(GeoAttributes.PSLOWNESS, "P210+P resulting from reflection of upgoing P at the free surface"),

	/**
	 * P210+S resulting from reflection of upgoing P at the free surface
	 */
	pP210S(null, "P210+S resulting from reflection of upgoing P at the free surface"),

	/**
	 * P410+P resulting from reflection of upgoing P at the free surface
	 */
	pP410P(GeoAttributes.PSLOWNESS, "P410+P resulting from reflection of upgoing P at the free surface"),

	/**
	 * P410+S resulting from reflection of upgoing P at the free surface
	 */
	pP410S(null, "P410+S resulting from reflection of upgoing P at the free surface"),

	/**
	 * P660+P resulting from reflection of upgoing P at the free surface
	 */
	pP660P(GeoAttributes.PSLOWNESS, "P660+P resulting from reflection of upgoing P at the free surface"),

	/**
	 * P660+S resulting from reflection of upgoing P at the free surface
	 */
	pP660S(null, "P660+S resulting from reflection of upgoing P at the free surface"),

	/**
	 * S210+P resulting from converted reflection of upgoing P at the free surface
	 */
	pS210P(null, "S210+P resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * S210+S resulting from converted reflection of upgoing P at the free surface
	 */
	pS210S(null, "S210+S resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * S410+P resulting from converted reflection of upgoing P at the free surface
	 */
	pS410P(null, "S410+P resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * S410+S resulting from converted reflection of upgoing P at the free surface
	 */
	pS410S(null, "S410+S resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * S660+P resulting from converted reflection of upgoing P at the free surface
	 */
	pS660P(null, "S660+P resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * S660+S resulting from converted reflection of upgoing P at the free surface
	 */
	pS660S(null, "S660+S resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * P210+P resulting from converted reflection of upgoing S at the free surface
	 */
	sP210P(null, "P210+P resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P210+S resulting from converted reflection of upgoing S at the free surface
	 */
	sP210S(null, "P210+S resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P410+P resulting from converted reflection of upgoing S at the free surface
	 */
	sP410P(null, "P410+P resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P410+S resulting from converted reflection of upgoing S at the free surface
	 */
	sP410S(null, "P410+S resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P660+P resulting from converted reflection of upgoing S at the free surface
	 */
	sP660P(null, "P660+P resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P660+S resulting from converted reflection of upgoing S at the free surface
	 */
	sP660S(null, "P660+S resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * S210+P resulting from reflection of upgoing S at the free surface
	 */
	sS210P(null, "S210+P resulting from reflection of upgoing S at the free surface"),

	/**
	 * S210+S resulting from reflection of upgoing S at the free surface
	 */
	sS210S(GeoAttributes.SSLOWNESS, "S210+S resulting from reflection of upgoing S at the free surface"),

	/**
	 * S410+P resulting from reflection of upgoing S at the free surface
	 */
	sS410P(null, "S410+P resulting from reflection of upgoing S at the free surface"),

	/**
	 * S410+S resulting from reflection of upgoing S at the free surface
	 */
	sS410S(GeoAttributes.SSLOWNESS, "S410+S resulting from reflection of upgoing S at the free surface"),

	/**
	 * S660+P resulting from reflection of upgoing S at the free surface
	 */
	sS660P(null, "S660+P resulting from reflection of upgoing S at the free surface"),

	/**
	 * S660+S resulting from reflection of upgoing S at the free surface
	 */
	sS660S(GeoAttributes.SSLOWNESS, "S660+S resulting from reflection of upgoing S at the free surface"),

	/**
	 * P210+P resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP210P(GeoAttributes.PSLOWNESS, "P210+P resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P210+S resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP210S(null, "P210+S resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P410+P resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP410P(GeoAttributes.PSLOWNESS, "P410+P resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P410+S resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP410S(null, "P410+S resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P660+P resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP660P(GeoAttributes.PSLOWNESS, "P660+P resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P660+S resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP660S(null, "P660+S resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P210+P resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP210P(GeoAttributes.PSLOWNESS, "P210+P resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P210+S resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP210S(null, "P210+S resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P410+P resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP410P(GeoAttributes.PSLOWNESS, "P410+P resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P410+S resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP410S(null, "P410+S resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P660+P resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP660P(GeoAttributes.PSLOWNESS, "P660+P resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P660+S resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP660S(null, "P660+S resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * (alternate: P120P) P reflection from outer side of a discontinuity at depth
	 * 120.
	 */
	P120_plus_P(null, "(alternate: P120P) P reflection from outer side of a discontinuity at depth 120."),

	/**
	 * P reflection from inner side of discontinuity at depth 120. P120-P is a P
	 * reflection from below the 120 km discontinuity, which means it is precursory
	 * to PP.
	 */
	P120_minus_P(null,
			"P reflection from inner side of discontinuity at depth 120."
					+ "<BR>P120-P is a P reflection from below the 120 km discontinuity,"
					+ "<BR>which means it is precursory to PP."),

	/**
	 * (alternate: P120S) P to S converted reflection from outer side of
	 * discontinuity at depth 120
	 */
	P120_plus_S(null,
			"(alternate: P120S) P to S converted reflection from outer side of" + "<BR>discontinuity at depth 120"),

	/**
	 * P to S converted reflection from inner side of discontinuity at depth 120
	 */
	P120_minus_S(null, "P to S converted reflection from inner side of discontinuity at depth 120"),

	/**
	 * (alternate: S120S) S reflection from outer side of a discontinuity at depth
	 * 120.
	 */
	S120_plus_S(null, "(alternate: S120S) S reflection from outer side of a discontinuity at depth 120."),

	/**
	 * S reflection from inner side of discontinuity at depth 120. S120-S is a S
	 * reflection from below the 120 km discontinuity, which means it is precursory
	 * to SS.
	 */
	S120_minus_S(GeoAttributes.SSLOWNESS,
			"S reflection from inner side of discontinuity at depth 120."
					+ "<BR>S120-S is a S reflection from below the 120 km discontinuity,"
					+ "<BR>which means it is precursory to SS."),

	/**
	 * (alternate: S120P) S to P converted reflection from outer side of
	 * discontinuity at depth 120
	 */
	S120_plus_P(null,
			"(alternate: S120P) S to P converted reflection from outer side of" + "<BR>discontinuity at depth 120"),

	/**
	 * S to P converted reflection from inner side of discontinuity at depth 120
	 */
	S120_minus_P(null, "S to P converted reflection from inner side of discontinuity at depth 120"),

	/**
	 * PKP reflected from inner side of a discontinuity at depth 120 outside the
	 * core, which means it is precursory to P'P'
	 */
	P_prime_120_minus_P_prime_(null,
			"PKP reflected from inner side of a discontinuity at depth 120"
					+ "<BR>outside the core, which means it is precursory to P'P'"),

	/**
	 * SKS reflected from inner side of a discontinuity at depth 120 outside the
	 * core, which means it is precursory to S'S'
	 */
	S_prime_120_minus_S_prime_(null,
			"SKS reflected from inner side of a discontinuity at depth 120"
					+ "<BR>outside the core, which means it is precursory to S'S'"),

	/**
	 * P120+P resulting from reflection of upgoing P at the free surface
	 */
	pP120_plus_P(null, "P120+P resulting from reflection of upgoing P at the free surface"),

	/**
	 * P120+S resulting from reflection of upgoing P at the free surface
	 */
	pP120_plus_S(null, "P120+S resulting from reflection of upgoing P at the free surface"),

	/**
	 * P120-P resulting from reflection of upgoing P at the free surface
	 */
	pP120_minus_P(null, "P120-P resulting from reflection of upgoing P at the free surface"),

	/**
	 * P120-S resulting from reflection of upgoing P at the free surface
	 */
	pP120_minus_S(null, "P120-S resulting from reflection of upgoing P at the free surface"),

	/**
	 * S120+P resulting from converted reflection of upgoing P at the free surface
	 */
	pS120_plus_P(null, "S120+P resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * S120+S resulting from converted reflection of upgoing P at the free surface
	 */
	pS120_plus_S(null, "S120+S resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * S120-P resulting from converted reflection of upgoing P at the free surface
	 */
	pS120_minus_P(null, "S120-P resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * S120-S resulting from converted reflection of upgoing P at the free surface
	 */
	pS120_minus_S(null, "S120-S resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * P120+P resulting from converted reflection of upgoing S at the free surface
	 */
	sP120_plus_P(null, "P120+P resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P120+S resulting from converted reflection of upgoing S at the free surface
	 */
	sP120_plus_S(null, "P120+S resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P120-P resulting from converted reflection of upgoing S at the free surface
	 */
	sP120_minus_P(null, "P120-P resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P120-S resulting from converted reflection of upgoing S at the free surface
	 */
	sP120_minus_S(null, "P120-S resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * S120+P resulting from reflection of upgoing S at the free surface
	 */
	sS120_plus_P(null, "S120+P resulting from reflection of upgoing S at the free surface"),

	/**
	 * S120+S resulting from reflection of upgoing S at the free surface
	 */
	sS120_plus_S(null, "S120+S resulting from reflection of upgoing S at the free surface"),

	/**
	 * S120-P resulting from reflection of upgoing S at the free surface
	 */
	sS120_minus_P(null, "S120-P resulting from reflection of upgoing S at the free surface"),

	/**
	 * S120-S resulting from reflection of upgoing S at the free surface
	 */
	sS120_minus_S(GeoAttributes.SSLOWNESS, "S120-S resulting from reflection of upgoing S at the free surface"),

	/**
	 * P120+P resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP120_plus_P(null, "P120+P resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P120+S resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP120_plus_S(null, "P120+S resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P120-P resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP120_minus_P(null, "P120-P resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P120-S resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP120_minus_S(null, "P120-S resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P120+P resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP120_plus_P(null, "P120+P resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P120+S resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP120_plus_S(null, "P120+S resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P120-P resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP120_minus_P(null, "P120-P resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P120-S resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP120_minus_S(null, "P120-S resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * (alternate: P120+P) P reflection from outer side of a discontinuity at depth
	 * 120
	 */
	P120P(GeoAttributes.PSLOWNESS, "(alternate: P120+P) P reflection from outer side of a discontinuity at depth 120"),

	/**
	 * (alternate: P120+S) P to S converted reflection from outer side of
	 * discontinuity at depth 120
	 */
	P120S(null,
			"(alternate: P120+S) P to S converted reflection from outer side of" + "<BR>discontinuity at depth 120"),

	/**
	 * (alternate: S120+S) S reflection from outer side of a discontinuity at depth
	 * 120
	 */
	S120S(GeoAttributes.SSLOWNESS, "(alternate: S120+S) S reflection from outer side of a discontinuity at depth 120"),

	/**
	 * (alternate: S120+P) S to P converted reflection from outer side of
	 * discontinuity at depth 120
	 */
	S120P(null,
			"(alternate: S120+P) S to P converted reflection from outer side of" + "<BR>discontinuity at depth 120"),

	/**
	 * P120+P resulting from reflection of upgoing P at the free surface
	 */
	pP120P(GeoAttributes.PSLOWNESS, "P120+P resulting from reflection of upgoing P at the free surface"),

	/**
	 * P120+S resulting from reflection of upgoing P at the free surface
	 */
	pP120S(null, "P120+S resulting from reflection of upgoing P at the free surface"),

	/**
	 * S120+P resulting from converted reflection of upgoing P at the free surface
	 */
	pS120P(null, "S120+P resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * S120+S resulting from converted reflection of upgoing P at the free surface
	 */
	pS120S(null, "S120+S resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * P120+P resulting from converted reflection of upgoing S at the free surface
	 */
	sP120P(null, "P120+P resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P120+S resulting from converted reflection of upgoing S at the free surface
	 */
	sP120S(null, "P120+S resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * S120+P resulting from reflection of upgoing S at the free surface
	 */
	sS120P(null, "S120+P resulting from reflection of upgoing S at the free surface"),

	/**
	 * S120+S resulting from reflection of upgoing S at the free surface
	 */
	sS120S(GeoAttributes.SSLOWNESS, "S120+S resulting from reflection of upgoing S at the free surface"),

	/**
	 * P120+P resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP120P(GeoAttributes.PSLOWNESS, "P120+P resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P120+S resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP120S(null, "P120+S resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P120+P resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP120P(GeoAttributes.PSLOWNESS, "P120+P resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P120+S resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP120S(null, "P120+S resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P reflection from inner side of discontinuity at depth 35. P35-P is a P
	 * reflection from below the 35 km discontinuity, which means it is precursory
	 * to PP.
	 */
	P35_minus_P(null,
			"P reflection from inner side of discontinuity at depth 35."
					+ "<BR>P35-P is a P reflection from below the 35 km discontinuity,"
					+ "<BR>which means it is precursory to PP."),

	/**
	 * P to S converted reflection from inner side of discontinuity at depth 35
	 */
	P35_minus_S(null, "P to S converted reflection from inner side of discontinuity at depth 35"),

	/**
	 * S reflection from inner side of discontinuity at depth 35. S35-S is a S
	 * reflection from below the 35 km discontinuity, which means it is precursory
	 * to SS.
	 */
	S35_minus_S(GeoAttributes.SSLOWNESS,
			"S reflection from inner side of discontinuity at depth 35."
					+ "<BR>S35-S is a S reflection from below the 35 km discontinuity,"
					+ "<BR>which means it is precursory to SS."),

	/**
	 * S to P converted reflection from inner side of discontinuity at depth 35
	 */
	S35_minus_P(null, "S to P converted reflection from inner side of discontinuity at depth 35"),

	/**
	 * PKP reflected from inner side of a discontinuity at depth 35 outside the
	 * core, which means it is precursory to P'P'
	 */
	P_prime_35_minus_P_prime_(null,
			"PKP reflected from inner side of a discontinuity at depth 35"
					+ "<BR>outside the core, which means it is precursory to P'P'"),

	/**
	 * SKS reflected from inner side of a discontinuity at depth 35 outside the
	 * core, which means it is precursory to S'S'
	 */
	S_prime_35_minus_S_prime_(null,
			"SKS reflected from inner side of a discontinuity at depth 35"
					+ "<BR>outside the core, which means it is precursory to S'S'"),

	/**
	 * P35-P resulting from reflection of upgoing P at the free surface
	 */
	pP35_minus_P(null, "P35-P resulting from reflection of upgoing P at the free surface"),

	/**
	 * P35-S resulting from reflection of upgoing P at the free surface
	 */
	pP35_minus_S(null, "P35-S resulting from reflection of upgoing P at the free surface"),

	/**
	 * S35-P resulting from converted reflection of upgoing P at the free surface
	 */
	pS35_minus_P(null, "S35-P resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * S35-S resulting from converted reflection of upgoing P at the free surface
	 */
	pS35_minus_S(null, "S35-S resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * P35-P resulting from converted reflection of upgoing S at the free surface
	 */
	sP35_minus_P(null, "P35-P resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * P35-S resulting from converted reflection of upgoing S at the free surface
	 */
	sP35_minus_S(null, "P35-S resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * S35-P resulting from reflection of upgoing S at the free surface
	 */
	sS35_minus_P(null, "S35-P resulting from reflection of upgoing S at the free surface"),

	/**
	 * S35-S resulting from reflection of upgoing S at the free surface
	 */
	sS35_minus_S(GeoAttributes.SSLOWNESS, "S35-S resulting from reflection of upgoing S at the free surface"),

	/**
	 * P35-P resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP35_minus_P(null, "P35-P resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P35-S resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwP35_minus_S(null, "P35-S resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * P35-P resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP35_minus_P(null, "P35-P resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * P35-S resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmP35_minus_S(null, "P35-S resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PSKP resulting from converted reflection of upgoing S at the free surface
	 */
	sPSKP(null, "PSKP resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PSKP resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPSKP(null, "PSKP resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PSKP resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPSKP(null, "PSKP resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PSKP resulting from reflection of upgoing P at the free surface
	 */
	pPSKP(null, "PSKP resulting from reflection of upgoing P at the free surface"),

	/**
	 * P (leaving a source downwards) to SKP reflection at the free surface
	 */
	PSKP(null, "P (leaving a source downwards) to SKP reflection at the free surface"),

	/**
	 * PKSP resulting from reflection of upgoing S at the free surface
	 */
	sPKSP(null, "PKSP resulting from reflection of upgoing S at the free surface"),

	/**
	 * PKSP resulting from converted reflection of upgoing P at the free surface
	 */
	pPKSP(null, "PKSP resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * (alternate: S'P) PKS to P reflection at the free surface free surface
	 */
	PKSP(null, "(alternate: S'P) PKS to P reflection at the free surface free surface"),

	/**
	 * PKSP with K leg bottoming in the upper outer core (IASPEI extension)
	 */
	PKSPab(null, "PKSP with K leg bottoming in the upper outer core (IASPEI extension)"),

	/**
	 * PSKP with K leg bottoming in the upper outer core (IASPEI extension)
	 */
	PSKPab(null, "PSKP with K leg bottoming in the upper outer core (IASPEI extension)"),

	/**
	 * PKSPab resulting from converted reflection of upgoing P at the free surface
	 */
	pPKSPab(null, "PKSPab resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * PSKPab resulting from reflection of upgoing P at the free surface
	 */
	pPSKPab(null, "PSKPab resulting from reflection of upgoing P at the free surface"),

	/**
	 * PSKPab resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPSKPab(null, "PSKPab resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PSKPab resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPSKPab(null, "PSKPab resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKSPab resulting from reflection of upgoing S at the free surface
	 */
	sPKSPab(null, "PKSPab resulting from reflection of upgoing S at the free surface"),

	/**
	 * PSKPab resulting from converted reflection of upgoing S at the free surface
	 */
	sPSKPab(null, "PSKPab resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKSP with K leg bottoming in the lower outer core (IASPEI extension)
	 */
	PKSPbc(null, "PKSP with K leg bottoming in the lower outer core (IASPEI extension)"),

	/**
	 * PSKP with K leg bottoming in the lower outer core (IASPEI extension)
	 */
	PSKPbc(null, "PSKP with K leg bottoming in the lower outer core (IASPEI extension)"),

	/**
	 * PKSPbc resulting from converted reflection of upgoing P at the free surface
	 */
	pPKSPbc(null, "PKSPbc resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * PSKPbc resulting from reflection of upgoing P at the free surface
	 */
	pPSKPbc(null, "PSKPbc resulting from reflection of upgoing P at the free surface"),

	/**
	 * PSKPbc resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPSKPbc(null, "PSKPbc resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PSKPbc resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPSKPbc(null, "PSKPbc resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKSPbc resulting from reflection of upgoing S at the free surface
	 */
	sPKSPbc(null, "PKSPbc resulting from reflection of upgoing S at the free surface"),

	/**
	 * PSKPbc resulting from converted reflection of upgoing S at the free surface
	 */
	sPSKPbc(null, "PSKPbc resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PKSP with K leg bottoming in the inner core (IASPEI extension)
	 */
	PKSPdf(null, "PKSP with K leg bottoming in the inner core (IASPEI extension)"),

	/**
	 * PSKP with K leg bottoming in the inner core (IASPEI extension)
	 */
	PSKPdf(null, "PSKP with K leg bottoming in the inner core (IASPEI extension)"),

	/**
	 * PKSPdf resulting from converted reflection of upgoing P at the free surface
	 */
	pPKSPdf(null, "PKSPdf resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * PSKPdf resulting from reflection of upgoing P at the free surface
	 */
	pPSKPdf(null, "PSKPdf resulting from reflection of upgoing P at the free surface"),

	/**
	 * PSKPdf resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPSKPdf(null, "PSKPdf resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PSKPdf resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPSKPdf(null, "PSKPdf resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PKSPdf resulting from reflection of upgoing S at the free surface
	 */
	sPKSPdf(null, "PKSPdf resulting from reflection of upgoing S at the free surface"),

	/**
	 * PSKPdf resulting from converted reflection of upgoing S at the free surface
	 */
	sPSKPdf(null, "PSKPdf resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PcPSKSac resulting from reflection of upgoing P at the free surface
	 */
	pPcPSKSac(null, "PcPSKSac resulting from reflection of upgoing P at the free surface"),

	/**
	 * SKS2ac resulting from converted reflection of upgoing P at the free surface
	 */
	pSKS2ac(null, "SKS2ac resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKSPcPac resulting from converted reflection of upgoing P at the free surface
	 */
	pSKSPcPac(null, "SKSPcPac resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKSPcSac resulting from converted reflection of upgoing P at the free surface
	 */
	pSKSPcSac(null, "SKSPcSac resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKSScPac resulting from converted reflection of upgoing P at the free surface
	 */
	pSKSScPac(null, "SKSScPac resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKSScSac resulting from converted reflection of upgoing P at the free surface
	 */
	pSKSScSac(null, "SKSScSac resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * ScPSKSac resulting from converted reflection of upgoing P at the free surface
	 */
	pScPSKSac(null, "ScPSKSac resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * PcPSKSac resulting from converted reflection of upgoing S at the free surface
	 */
	sPcPSKSac(null, "PcPSKSac resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * SKS2ac resulting from reflection of upgoing S at the free surface
	 */
	sSKS2ac(GeoAttributes.SSLOWNESS, "SKS2ac resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKSPcPac resulting from reflection of upgoing S at the free surface
	 */
	sSKSPcPac(null, "SKSPcPac resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKSPcSac resulting from reflection of upgoing S at the free surface
	 */
	sSKSPcSac(null, "SKSPcSac resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKSScPac resulting from reflection of upgoing S at the free surface
	 */
	sSKSScPac(null, "SKSScPac resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKSScSac resulting from reflection of upgoing S at the free surface
	 */
	sSKSScSac(GeoAttributes.SSLOWNESS, "SKSScSac resulting from reflection of upgoing S at the free surface"),

	/**
	 * PSKSac resulting from reflection of upgoing P at the free surface
	 */
	pPSKSac(null, "PSKSac resulting from reflection of upgoing P at the free surface"),

	/**
	 * PcSSKSac resulting from reflection of upgoing P at the free surface
	 */
	pPcSSKSac(null, "PcSSKSac resulting from reflection of upgoing P at the free surface"),

	/**
	 * SKS3ac resulting from converted reflection of upgoing P at the free surface
	 */
	pSKS3ac(null, "SKS3ac resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKS4ac resulting from converted reflection of upgoing P at the free surface
	 */
	pSKS4ac(null, "SKS4ac resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKS5ac resulting from converted reflection of upgoing P at the free surface
	 */
	pSKS5ac(null, "SKS5ac resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKS6ac resulting from converted reflection of upgoing P at the free surface
	 */
	pSKS6ac(null, "SKS6ac resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKS7ac resulting from converted reflection of upgoing P at the free surface
	 */
	pSKS7ac(null, "SKS7ac resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKSPac resulting from converted reflection of upgoing P at the free surface
	 */
	pSKSPac(null, "SKSPac resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKSSKSac resulting from converted reflection of upgoing P at the free surface
	 */
	pSKSSKSac(null, "SKSSKSac resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * ScSSKS with K leg bottoming in the outer core (IASPEI extension)
	 */
	ScSSKSac(GeoAttributes.SSLOWNESS, "ScSSKS with K leg bottoming in the outer core (IASPEI extension)"),

	/**
	 * PSKS with K leg bottoming in the outer core (IASPEI extension)
	 */
	PSKSac(null, "PSKS with K leg bottoming in the outer core (IASPEI extension)"),

	/**
	 * PcSSKS with K leg bottoming in the outer core (IASPEI extension)
	 */
	PcSSKSac(null, "PcSSKS with K leg bottoming in the outer core (IASPEI extension)"),

	/**
	 * SKSSKS with K leg bottoming in the outer core (IASPEI extension)
	 */
	SKSSKSac(GeoAttributes.SSLOWNESS, "SKSSKS with K leg bottoming in the outer core (IASPEI extension)"),

	/**
	 * SKS3 with K leg bottoming in the outer core (IASPEI extension)
	 */
	SKS3ac(GeoAttributes.SSLOWNESS, "SKS3 with K leg bottoming in the outer core (IASPEI extension)"),

	/**
	 * SKS4 with K leg bottoming in the outer core (IASPEI extension)
	 */
	SKS4ac(GeoAttributes.SSLOWNESS, "SKS4 with K leg bottoming in the outer core (IASPEI extension)"),

	/**
	 * SKS5 with K leg bottoming in the outer core (IASPEI extension)
	 */
	SKS5ac(GeoAttributes.SSLOWNESS, "SKS5 with K leg bottoming in the outer core (IASPEI extension)"),

	/**
	 * SKS6 with K leg bottoming in the outer core (IASPEI extension)
	 */
	SKS6ac(GeoAttributes.SSLOWNESS, "SKS6 with K leg bottoming in the outer core (IASPEI extension)"),

	/**
	 * SKS7 with K leg bottoming in the outer core (IASPEI extension)
	 */
	SKS7ac(GeoAttributes.SSLOWNESS, "SKS7 with K leg bottoming in the outer core (IASPEI extension)"),

	/**
	 * SKSP with K leg bottoming in the outer core (IASPEI extension)
	 */
	SKSPac(null, "SKSP with K leg bottoming in the outer core (IASPEI extension)"),

	/**
	 * ScSSKSac resulting from converted reflection of upgoing P at the free surface
	 */
	pScSSKSac(null, "ScSSKSac resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * PSKSac resulting from converted reflection of upgoing S at the free surface
	 */
	sPSKSac(null, "PSKSac resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PcSSKSac resulting from converted reflection of upgoing S at the free surface
	 */
	sPcSSKSac(null, "PcSSKSac resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * SKS3ac resulting from reflection of upgoing S at the free surface
	 */
	sSKS3ac(GeoAttributes.SSLOWNESS, "SKS3ac resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKS4ac resulting from reflection of upgoing S at the free surface
	 */
	sSKS4ac(GeoAttributes.SSLOWNESS, "SKS4ac resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKS5ac resulting from reflection of upgoing S at the free surface
	 */
	sSKS5ac(GeoAttributes.SSLOWNESS, "SKS5ac resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKS6ac resulting from reflection of upgoing S at the free surface
	 */
	sSKS6ac(GeoAttributes.SSLOWNESS, "SKS6ac resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKS7ac resulting from reflection of upgoing S at the free surface
	 */
	sSKS7ac(GeoAttributes.SSLOWNESS, "SKS7ac resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKSPac resulting from reflection of upgoing S at the free surface
	 */
	sSKSPac(null, "SKSPac resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKSSKSac resulting from reflection of upgoing S at the free surface
	 */
	sSKSSKSac(GeoAttributes.SSLOWNESS, "SKSSKSac resulting from reflection of upgoing S at the free surface"),

	/**
	 * ScSSKSac resulting from reflection of upgoing S at the free surface
	 */
	sScSSKSac(GeoAttributes.SSLOWNESS, "ScSSKSac resulting from reflection of upgoing S at the free surface"),

	/**
	 * PSKSac resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPSKSac(null, "PSKSac resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PcSSKSac resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPcSSKSac(null, "PcSSKSac resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * ScPSKSac resulting from reflection of upgoing S at the free surface
	 */
	sScPSKSac(null, "ScPSKSac resulting from reflection of upgoing S at the free surface"),

	/**
	 * PSKSac resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPSKSac(null, "PSKSac resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PcSSKSac resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPcSSKSac(null, "PcSSKSac resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PcPSKSac resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPcPSKSac(null, "PcPSKSac resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PcPSKSac resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPcPSKSac(null, "PcPSKSac resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * SKS2 with K leg bottoming in the outer core (IASPEI extension)
	 */
	SKS2ac(GeoAttributes.SSLOWNESS, "SKS2 with K leg bottoming in the outer core (IASPEI extension)"),

	/**
	 * ScPSKS with K leg bottoming in the outer core (IASPEI extension)
	 */
	ScPSKSac(null, "ScPSKS with K leg bottoming in the outer core (IASPEI extension)"),

	/**
	 * PcPSKS with K leg bottoming in the outer core (IASPEI extension)
	 */
	PcPSKSac(null, "PcPSKS with K leg bottoming in the outer core (IASPEI extension)"),

	/**
	 * SKSScS with K leg bottoming in the outer core (IASPEI extension)
	 */
	SKSScSac(GeoAttributes.SSLOWNESS, "SKSScS with K leg bottoming in the outer core (IASPEI extension)"),

	/**
	 * SKSPcP with K leg bottoming in the outer core (IASPEI extension)
	 */
	SKSPcPac(null, "SKSPcP with K leg bottoming in the outer core (IASPEI extension)"),

	/**
	 * SKSScP with K leg bottoming in the outer core (IASPEI extension)
	 */
	SKSScPac(null, "SKSScP with K leg bottoming in the outer core (IASPEI extension)"),

	/**
	 * SKSPcS with K leg bottoming in the outer core (IASPEI extension)
	 */
	SKSPcSac(null, "SKSPcS with K leg bottoming in the outer core (IASPEI extension)"),

	/**
	 * PcPSKSdf resulting from reflection of upgoing P at the free surface
	 */
	pPcPSKSdf(null, "PcPSKSdf resulting from reflection of upgoing P at the free surface"),

	/**
	 * SKS2df resulting from converted reflection of upgoing P at the free surface
	 */
	pSKS2df(null, "SKS2df resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKSPcPdf resulting from converted reflection of upgoing P at the free surface
	 */
	pSKSPcPdf(null, "SKSPcPdf resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKSPcSdf resulting from converted reflection of upgoing P at the free surface
	 */
	pSKSPcSdf(null, "SKSPcSdf resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKSScPdf resulting from converted reflection of upgoing P at the free surface
	 */
	pSKSScPdf(null, "SKSScPdf resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKSScSdf resulting from converted reflection of upgoing P at the free surface
	 */
	pSKSScSdf(null, "SKSScSdf resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * ScPSKSdf resulting from converted reflection of upgoing P at the free surface
	 */
	pScPSKSdf(null, "ScPSKSdf resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * PcPSKSdf resulting from converted reflection of upgoing S at the free surface
	 */
	sPcPSKSdf(null, "PcPSKSdf resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * SKS2df resulting from reflection of upgoing S at the free surface
	 */
	sSKS2df(GeoAttributes.SSLOWNESS, "SKS2df resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKSPcPdf resulting from reflection of upgoing S at the free surface
	 */
	sSKSPcPdf(null, "SKSPcPdf resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKSPcSdf resulting from reflection of upgoing S at the free surface
	 */
	sSKSPcSdf(null, "SKSPcSdf resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKSScPdf resulting from reflection of upgoing S at the free surface
	 */
	sSKSScPdf(null, "SKSScPdf resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKSScSdf resulting from reflection of upgoing S at the free surface
	 */
	sSKSScSdf(GeoAttributes.SSLOWNESS, "SKSScSdf resulting from reflection of upgoing S at the free surface"),

	/**
	 * PSKSdf resulting from reflection of upgoing P at the free surface
	 */
	pPSKSdf(null, "PSKSdf resulting from reflection of upgoing P at the free surface"),

	/**
	 * PcSSKSdf resulting from reflection of upgoing P at the free surface
	 */
	pPcSSKSdf(null, "PcSSKSdf resulting from reflection of upgoing P at the free surface"),

	/**
	 * SKS3df resulting from converted reflection of upgoing P at the free surface
	 */
	pSKS3df(null, "SKS3df resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKS4df resulting from converted reflection of upgoing P at the free surface
	 */
	pSKS4df(null, "SKS4df resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKS5df resulting from converted reflection of upgoing P at the free surface
	 */
	pSKS5df(null, "SKS5df resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKS6df resulting from converted reflection of upgoing P at the free surface
	 */
	pSKS6df(null, "SKS6df resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKS7df resulting from converted reflection of upgoing P at the free surface
	 */
	pSKS7df(null, "SKS7df resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKSPdf resulting from converted reflection of upgoing P at the free surface
	 */
	pSKSPdf(null, "SKSPdf resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * SKSSKSdf resulting from converted reflection of upgoing P at the free surface
	 */
	pSKSSKSdf(null, "SKSSKSdf resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * ScSSKS with K leg bottoming in the inner core (IASPEI extension)
	 */
	ScSSKSdf(GeoAttributes.SSLOWNESS, "ScSSKS with K leg bottoming in the inner core (IASPEI extension)"),

	/**
	 * PSKS with K leg bottoming in the inner core (IASPEI extension)
	 */
	PSKSdf(null, "PSKS with K leg bottoming in the inner core (IASPEI extension)"),

	/**
	 * PcSSKS with K leg bottoming in the inner core (IASPEI extension)
	 */
	PcSSKSdf(null, "PcSSKS with K leg bottoming in the inner core (IASPEI extension)"),

	/**
	 * SKSSKS with K leg bottoming in the inner core (IASPEI extension)
	 */
	SKSSKSdf(GeoAttributes.SSLOWNESS, "SKSSKS with K leg bottoming in the inner core (IASPEI extension)"),

	/**
	 * SKS3 with K leg bottoming in the inner core (IASPEI extension)
	 */
	SKS3df(GeoAttributes.SSLOWNESS, "SKS3 with K leg bottoming in the inner core (IASPEI extension)"),

	/**
	 * SKS4 with K leg bottoming in the inner core (IASPEI extension)
	 */
	SKS4df(GeoAttributes.SSLOWNESS, "SKS4 with K leg bottoming in the inner core (IASPEI extension)"),

	/**
	 * SKS5 with K leg bottoming in the inner core (IASPEI extension)
	 */
	SKS5df(GeoAttributes.SSLOWNESS, "SKS5 with K leg bottoming in the inner core (IASPEI extension)"),

	/**
	 * SKS6 with K leg bottoming in the inner core (IASPEI extension)
	 */
	SKS6df(GeoAttributes.SSLOWNESS, "SKS6 with K leg bottoming in the inner core (IASPEI extension)"),

	/**
	 * SKS7 with K leg bottoming in the inner core (IASPEI extension)
	 */
	SKS7df(GeoAttributes.SSLOWNESS, "SKS7 with K leg bottoming in the inner core (IASPEI extension)"),

	/**
	 * SKSP with K leg bottoming in the inner core (IASPEI extension)
	 */
	SKSPdf(null, "SKSP with K leg bottoming in the inner core (IASPEI extension)"),

	/**
	 * ScSSKSdf resulting from converted reflection of upgoing P at the free surface
	 */
	pScSSKSdf(null, "ScSSKSdf resulting from converted reflection of upgoing P at the free surface"),

	/**
	 * PSKSdf resulting from converted reflection of upgoing S at the free surface
	 */
	sPSKSdf(null, "PSKSdf resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * PcSSKSdf resulting from converted reflection of upgoing S at the free surface
	 */
	sPcSSKSdf(null, "PcSSKSdf resulting from converted reflection of upgoing S at the free surface"),

	/**
	 * SKS3df resulting from reflection of upgoing S at the free surface
	 */
	sSKS3df(GeoAttributes.SSLOWNESS, "SKS3df resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKS4df resulting from reflection of upgoing S at the free surface
	 */
	sSKS4df(GeoAttributes.SSLOWNESS, "SKS4df resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKS5df resulting from reflection of upgoing S at the free surface
	 */
	sSKS5df(GeoAttributes.SSLOWNESS, "SKS5df resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKS6df resulting from reflection of upgoing S at the free surface
	 */
	sSKS6df(GeoAttributes.SSLOWNESS, "SKS6df resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKS7df resulting from reflection of upgoing S at the free surface
	 */
	sSKS7df(GeoAttributes.SSLOWNESS, "SKS7df resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKSPdf resulting from reflection of upgoing S at the free surface
	 */
	sSKSPdf(null, "SKSPdf resulting from reflection of upgoing S at the free surface"),

	/**
	 * SKSSKSdf resulting from reflection of upgoing S at the free surface
	 */
	sSKSSKSdf(GeoAttributes.SSLOWNESS, "SKSSKSdf resulting from reflection of upgoing S at the free surface"),

	/**
	 * ScSSKSdf resulting from reflection of upgoing S at the free surface
	 */
	sScSSKSdf(GeoAttributes.SSLOWNESS, "ScSSKSdf resulting from reflection of upgoing S at the free surface"),

	/**
	 * PSKSdf resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPSKSdf(null, "PSKSdf resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PcSSKSdf resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPcSSKSdf(null, "PcSSKSdf resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * ScPSKSdf resulting from reflection of upgoing S at the free surface
	 */
	sScPSKSdf(null, "ScPSKSdf resulting from reflection of upgoing S at the free surface"),

	/**
	 * PSKSdf resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPSKSdf(null, "PSKSdf resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PcSSKSdf resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPcSSKSdf(null, "PcSSKSdf resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * PcPSKSdf resulting from reflection of upgoing P at the ocean's free surface
	 */
	pwPcPSKSdf(null, "PcPSKSdf resulting from reflection of upgoing P at the ocean's free surface"),

	/**
	 * PcPSKSdf resulting from reflection of upgoing P at the inner side of the Moho
	 */
	pmPcPSKSdf(null, "PcPSKSdf resulting from reflection of upgoing P at the inner side of the Moho"),

	/**
	 * SKS2 with K leg bottoming in the inner core (IASPEI extension)
	 */
	SKS2df(GeoAttributes.SSLOWNESS, "SKS2 with K leg bottoming in the inner core (IASPEI extension)"),

	/**
	 * ScPSKS with K leg bottoming in the inner core (IASPEI extension)
	 */
	ScPSKSdf(null, "ScPSKS with K leg bottoming in the inner core (IASPEI extension)"),

	/**
	 * PcPSKS with K leg bottoming in the inner core (IASPEI extension)
	 */
	PcPSKSdf(null, "PcPSKS with K leg bottoming in the inner core (IASPEI extension)"),

	/**
	 * SKSScS with K leg bottoming in the inner core (IASPEI extension)
	 */
	SKSScSdf(GeoAttributes.SSLOWNESS, "SKSScS with K leg bottoming in the inner core (IASPEI extension)"),

	/**
	 * SKSPcP with K leg bottoming in the inner core (IASPEI extension)
	 */
	SKSPcPdf(null, "SKSPcP with K leg bottoming in the inner core (IASPEI extension)"),

	/**
	 * SKSScP with K leg bottoming in the inner core (IASPEI extension)
	 */
	SKSScPdf(null, "SKSScP with K leg bottoming in the inner core (IASPEI extension)"),

	/**
	 * SKSPcS with K leg bottoming in the inner core (IASPEI extension)
	 */
	SKSPcSdf(null, "SKSPcS with K leg bottoming in the inner core (IASPEI extension)"),

	/**
	 * Amplitude measurement for Lg
	 */
	AMLg(null, "Amplitude measurement for Lg"),

	/**
	 * Maximum amplitude of the T phase
	 */
	Tm(null, "Maximum amplitude of the T phase"),

	/**
	 * Coda associated with direct P wave
	 */
	Pc(GeoAttributes.PSLOWNESS, "Coda associated with direct P wave"),

	/**
	 * pre-event noise measurement associated with Pg phase
	 */
	PrEvNPg(GeoAttributes.PSLOWNESS, "pre-event noise measurement associated with Pg phase"),

	/**
	 * pre-event noise measurement associated with Lg phase
	 */
	PrEvNLg(GeoAttributes.PSLOWNESS, "pre-event noise measurement associated with Lg phase"),

	/**
	 * pre-event noise measurement associated with P phase
	 */
	PrEvNP(GeoAttributes.PSLOWNESS, "pre-event noise measurement associated with P phase"),

	/**
	 * pre-event noise measurement associated with S phase
	 */
	PrEvNS(null, "pre-event noise measurement associated with S phase"),

	/**
	 * pre-event noise measurement associated with Pc phase
	 */
	PrEvNPc(GeoAttributes.PSLOWNESS, "pre-event noise measurement associated with Pc phase"),

	/**
	 * pre-phase noise measurement associated with Pg phase
	 */
	PrPhNPg(GeoAttributes.PSLOWNESS, "pre-phase noise measurement associated with Pg phase"),

	/**
	 * pre-phase noise measurement associated with Lg phase
	 */
	PrPhNLg(GeoAttributes.PSLOWNESS, "pre-phase noise measurement associated with Lg phase"),

	/**
	 * pre-phase noise measurement associated with P phase
	 */
	PrPhNP(GeoAttributes.PSLOWNESS, "pre-phase noise measurement associated with P phase"),

	/**
	 * pre-phase noise measurement associated with S phase
	 */
	PrPhNS(null, "pre-phase noise measurement associated with S phase"),

	/**
	 * pre-phase noise measurement associated with Pc phase
	 */
	PrPhNPc(GeoAttributes.PSLOWNESS, "pre-phase noise measurement associated with Pc phase"),

	/**
	 * Siberian phase name, amplitude measurement at Sg, see AMSg
	 */
	Sgm(GeoAttributes.SSLOWNESS, "Siberian phase name, amplitude measurement at Sg, see AMSg"),

	/**
	 * Siberian phase name, amplitude measurement at Pg, see AMPg
	 */
	Pgm(GeoAttributes.PSLOWNESS, "Siberian phase name, amplitude measurement at Pg, see AMPg"),

	/**
	 * Siberian phase name, amplitude measurement at Lg, see AMLg
	 */
	Lgm(null, "Siberian phase name, amplitude measurement at Lg, see AMLg"),

	/**
	 * Siberian phase name, amplitude measurement at P, see A, AMB, AML
	 */
	Pm(GeoAttributes.PSLOWNESS, "Siberian phase name, amplitude measurement at P, see A, AMB, AML"),

	/**
	 * Siberian phase name, amplitude measurement at S, see A, AMB, AML
	 */
	Sm(GeoAttributes.SSLOWNESS, "Siberian phase name, amplitude measurement at S, see A, AMB, AML"),

	/**
	 * Siberian phase name, amplitude measurement at LQ, see A, AMS
	 */
	LQm(null, "Siberian phase name, amplitude measurement at LQ, see A, AMS"),

	/**
	 * Siberian phase name, amplitude measurement at LR, see A, AMS
	 */
	LRm(null, "Siberian phase name, amplitude measurement at LR, see A, AMS"),

	/**
	 * Siberian phase name, amplitude measurement at unknown phase, see A, x
	 */
	Um(null, "Siberian phase name, amplitude measurement at unknown phase, see A, x"),

	/**
	 * Siberian phase name, unknown phase, see x
	 */
	U(null, "Siberian phase name, unknown phase, see x"),

	/**
	 * Siberian phase name, unknown phase (modified from original ID), see x
	 */
	Ux(null, "Siberian phase name, unknown phase (modified from original ID), see x"),

	/**
	 * Siberian phase name, P phase (modified from original ID), see P
	 */
	Px(GeoAttributes.PSLOWNESS, "Siberian phase name, P phase (modified from original ID), see P"),

	/**
	 * Siberian phase name, S phase (modified from original ID), see S
	 */
	Sx(GeoAttributes.SSLOWNESS, "Siberian phase name, S phase (modified from original ID), see S"),

	/**
	 * Siberian phase name, Pg phase (modified from original ID), see Pg
	 */
	Pgx(GeoAttributes.PSLOWNESS, "Siberian phase name, Pg phase (modified from original ID), see Pg"),

	/**
	 * Siberian phase name, Sg phase (modified from original ID), see Sg
	 */
	Sgx(GeoAttributes.SSLOWNESS, "Siberian phase name, Sg phase (modified from original ID), see Sg"),

	/**
	 * ABCE regional phase name (refracted P on 11km depth refractor?)
	 */
	P11(GeoAttributes.PSLOWNESS, "ABCE regional phase name (refracted P on 11km depth refractor?)"),

	/**
	 * ABCE regional phase name (refracted S on 11km depth refractor?)
	 */
	S11(GeoAttributes.SSLOWNESS, "ABCE regional phase name (refracted S on 11km depth refractor?)"),

	/**
	 * seismic coda, direct phase unspecified
	 */
	coda(null, "seismic coda, direct phase unspecified"),

	/**
	 * generally Lg and its coda, but also Sn, Rg, LR depending on period and range
	 */
	Lg_plus_coda(null, "generally Lg and its coda, but also Sn, Rg, LR depending on period and range"),

	PKhKP(null, "?"),

	pPKhKP(null, "?"),

	pPdiff(null, "really?"),

	sPdiff(null, "really?"),

	NULL(null, "not a phase.");

	/**
	 * 
	 */
	private GeoAttributes waveType;
	
	/**
	 * Wavetype (PSLOWNESS or SSLOWNESS) of the ray at the source
	 * deduced from phase name.  May be NA_VALUE if cannot be deduced.
	 */
	private GeoAttributes waveTypeSource;
	
	/**
	 * Wavetype (PSLOWNESS or SSLOWNESS) of the ray at the receiver
	 * deduced from phase name.  May be NA_VALUE if cannot be deduced.
	 */
	private GeoAttributes waveTypeReceiver;

	/**
	 * 
	 */
	private String description;

	/**
	 * 
	 */
	private String fileName;

	private String rayBranchDirectionChangeList;

	private String raySlownessConversionInterfaceList;

	private double approxMinDistanceLimitDeg = 0.0;
	private double approxMaxDistanceLimitDeg = 180.0;

	/**
	 * Constructor
	 * 
	 * @param waveType    GeoAttributes
	 * @param phaseType   String
	 * @param description String
	 */
	SeismicPhase(GeoAttributes waveType, String description) {
		this.waveType = waveType;
		this.description = description;
		this.fileName = this.toString();
		rayBranchDirectionChangeList = null;
		raySlownessConversionInterfaceList = null;
		if (this.waveType != null)
			raySlownessConversionInterfaceList = this.waveType.name();
		deduceWaveTypes(this.toString());
		}

	/**
	 * Constructor
	 * 
	 * @param slownessConversionInterfaceList String of comma separated values
	 *                                        beginning with the rays starting
	 *                                        slowness name (PSLOWNESS, or
	 *                                        SSLOWNESS), followed by one or more
	 *                                        pairs of entries the first of which
	 *                                        must be a valid EarthInterface name
	 *                                        and the second a slowness name. These
	 *                                        pairs imply a change of slowness along
	 *                                        the ray path beginning at the source
	 *                                        and proceeding toward the receiver.
	 *                                        For example, the list "PSLOWNESS, CMB,
	 *                                        PSLOWNESS, CMB, SSLOWNESS" implies a
	 *                                        ray beginning as a P wave, continuing
	 *                                        as a P wave after passing through the
	 *                                        CMB boundary interface, and finally,
	 *                                        converting to an S wave after passing
	 *                                        through the CMB boundary for the
	 *                                        second time.
	 * @param branchDirectionChangeList       String of comma separated values pairs
	 *                                        defining a ray direction change type
	 *                                        (RayDirectionChange) associated with
	 *                                        an EarthInterface. For example,
	 *                                        "BOTTOM_SIDE_REFLECTION, SURFACE,
	 *                                        BOTTOM, M660, BOTTOM_SIDE_REFLECTION,
	 *                                        SURFACE, BOTTOM, M660" describes the
	 *                                        ray pPP (or with andy S variant). The
	 *                                        ray begins and encounters a bottom
	 *                                        side reflection at the surface
	 *                                        interface (must be an upgoing from the
	 *                                        source), then turns into a BOTTOM
	 *                                        branch using a level description for
	 *                                        the "M660" interface, then encounters
	 *                                        another bottom side reflection at the
	 *                                        surface before ending with another
	 *                                        BOTTOM branch using the "M660"
	 *                                        interface level definition.
	 * @param description                     String
	 */
	SeismicPhase(String slownessConversionInterfaceList, String branchDirectionChangeList, String description) {
		String waveTypeStart = slownessConversionInterfaceList.split(",")[0];
		this.waveType = GeoAttributes.valueOf(waveTypeStart);
		this.description = description;
		this.fileName = this.toString();
		raySlownessConversionInterfaceList = slownessConversionInterfaceList;
		rayBranchDirectionChangeList = branchDirectionChangeList;
		deduceWaveTypes(this.toString());
	}

	/**
	 * Constructor
	 * 
	 * @param slownessConversionInterfaceList String of comma separated values
	 *                                        beginning with the rays starting
	 *                                        slowness name (PSLOWNESS, or
	 *                                        SSLOWNESS), followed by one or more
	 *                                        pairs of entries the first of which
	 *                                        must be a valid EarthInterface name
	 *                                        and the second a slowness name. These
	 *                                        pairs imply a change of slowness along
	 *                                        the ray path beginning at the source
	 *                                        and proceeding toward the receiver.
	 *                                        For example, the list "PSLOWNESS, CMB,
	 *                                        PSLOWNESS, CMB, SSLOWNESS" implies a
	 *                                        ray beginning as a P wave, continuing
	 *                                        as a P wave after passing through the
	 *                                        CMB boundary interface, and finally,
	 *                                        converting to an S wave after passing
	 *                                        through the CMB boundary for the
	 *                                        second time.
	 * @param branchDirectionChangeList       String of comma separated values pairs
	 *                                        defining a ray direction change type
	 *                                        (RayDirectionChange) associated with
	 *                                        an EarthInterface. For example,
	 *                                        "BOTTOM_SIDE_REFLECTION, SURFACE,
	 *                                        BOTTOM, M660, BOTTOM_SIDE_REFLECTION,
	 *                                        SURFACE, BOTTOM, M660" describes the
	 *                                        ray pPP (or with andy S variant). The
	 *                                        ray begins and encounters a bottom
	 *                                        side reflection at the surface
	 *                                        interface (must be an upgoing from the
	 *                                        source), then turns into a BOTTOM
	 *                                        branch using a level description for
	 *                                        the "M660" interface, then encounters
	 *                                        another bottom side reflection at the
	 *                                        surface before ending with another
	 *                                        BOTTOM branch using the "M660"
	 *                                        interface level definition.
	 * @param description                     String
	 */
	SeismicPhase(String slownessConversionInterfaceList, String branchDirectionChangeList, String description,
			String fileName) {
		String waveTypeStart = slownessConversionInterfaceList.split(",")[0];
		this.waveType = GeoAttributes.valueOf(waveTypeStart);
		this.description = description;
		this.fileName = fileName;
		raySlownessConversionInterfaceList = slownessConversionInterfaceList;
		rayBranchDirectionChangeList = branchDirectionChangeList;
		deduceWaveTypes(this.toString());
	}

	public String getRayBranchList() {
		return rayBranchDirectionChangeList;
	}

	public String getRayInterfaceWaveTypeList() {
		return raySlownessConversionInterfaceList;
	}

	/**
	 * The waveType is GeoAttributes.PSLOWNESS or GeoAttributes.SSLOWNESS for phases
	 * that are comprised of only one type of wave. For phases that involve
	 * converted phases, such as PmS, null is returned.
	 * 
	 * @return GeoAttributes
	 */
	public GeoAttributes getWaveType() {
		return waveType;
	}

	// /**
	// * General category of the phase. Examples are 'CRUSTAL PHASE',
	// 'MANTLE
	// * PHASE', 'CORE PHASE', 'DEPTH PHASE' and 'SURFACE WAVE'.
	// *
	// * @return String
	// */
	// public String getPhaseType()
	// {
	// return phaseType;
	// }

	/**
	 * Detailed description of the phase.
	 * 
	 * @return String
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Set known phase limits that are most generally used in ray tracing. Add any
	 * others as necessary.
	 */
	static {
		P.setApproximateDistanceLimitDegrees(0.0, 100.0);
		S.setApproximateDistanceLimitDegrees(0.0, 100.0);
		Pn.setApproximateDistanceLimitDegrees(0.0, 30.0);
		Sn.setApproximateDistanceLimitDegrees(0.0, 30.0);
		Pmantle.setApproximateDistanceLimitDegrees(0.0, 100.0);
		Smantle.setApproximateDistanceLimitDegrees(0.0, 100.0);
		PcP.setApproximateDistanceLimitDegrees(0.0, 115.0);
		PKPab.setApproximateDistanceLimitDegrees(150.0, 180.0);
		PKPbc.setApproximateDistanceLimitDegrees(140.0, 160.0);
		PKPdf.setApproximateDistanceLimitDegrees(111.0, 180.0);
		pP.setApproximateDistanceLimitDegrees(0.0, 115.0);
		PP.setApproximateDistanceLimitDegrees(0.0, 180.0);
	}

	/**
	 * Almost always true that fileName = phaseName. Exception where
	 * case-sensitivity results in phase name conflicts:
	 * 
	 * <br>
	 * pP -> littlep_bigP <br>
	 * PP -> bigP_bigP <br>
	 * pS -> littlep_bigS <br>
	 * PS -> bigP_bigS <br>
	 * sP -> littles_bigP <br>
	 * SP -> bigS_bigP <br>
	 * sS -> littles_bigS <br>
	 * SS -> bigS_bigS
	 * 
	 * @return String
	 */
	public String getFileName() {
		return this.fileName;
	}

	protected void setApproximateDistanceLimitDegrees(double mn, double mx) {
		approxMinDistanceLimitDeg = mn;
		approxMaxDistanceLimitDeg = mx;
	}

	public double approximateMinimumDistanceLimitDegrees() {
		return approxMinDistanceLimitDeg;
	}

	public double approximateMaximumDistanceLimitDegrees() {
		return approxMaxDistanceLimitDeg;
	}

	/**
	 * Given a calculated ray, determine what phase it is.
	 * 
	 * @param ray
	 * @return P, Pn, PmP or PcP or shear wave equivalents. Return SeismicPhase.NULL
	 *         for invalid rays, rays with errors, or if cannot determine the phase
	 *         from available information.
	 */
	static public SeismicPhase getPhase(Prediction ray) {
		double turnDepth = ray.getAttribute(GeoAttributes.TURNING_DEPTH);

		if ((ray.getRayType() == RayType.REFRACTION) || (ray.getRayType() == RayType.BOTTOM_SIDE_DIFFRACTION)) {
			if (turnDepth == Globals.NA_VALUE) {
				// if turning depth is not available base phase on
				// source-receiver distance
				if (Math.toDegrees(ray.getDistance()) < 20)
					return ray.getPhase().getWaveType() == GeoAttributes.PSLOWNESS ? SeismicPhase.Pn : SeismicPhase.Sn;

				return ray.getPhase().getWaveType() == GeoAttributes.PSLOWNESS ? SeismicPhase.P : SeismicPhase.S;
			}

			if (turnDepth < 409.9)
				return ray.getPhase().getWaveType() == GeoAttributes.PSLOWNESS ? SeismicPhase.Pn : SeismicPhase.Sn;

			return ray.getPhase().getWaveType() == GeoAttributes.PSLOWNESS ? SeismicPhase.P : SeismicPhase.S;
		}

		if (ray.getRayType() == RayType.REFLECTION) {
			// return a reflected phase.
			if (turnDepth == Globals.NA_VALUE) {
				// if turning depth is not available base phase on travel
				// time
				if (ray.getAttribute(GeoAttributes.TRAVEL_TIME) < 500)
					return ray.getPhase().getWaveType() == GeoAttributes.PSLOWNESS ? SeismicPhase.PmP
							: SeismicPhase.SmS;

				return ray.getPhase().getWaveType() == GeoAttributes.PSLOWNESS ? SeismicPhase.PcP : SeismicPhase.ScS;
			}

			if (turnDepth < 100)
				return ray.getPhase().getWaveType() == GeoAttributes.PSLOWNESS ? SeismicPhase.PmP : SeismicPhase.SmS;

			if (turnDepth < 300)
				return ray.getPhase().getWaveType() == GeoAttributes.PSLOWNESS ? SeismicPhase.P210P
						: SeismicPhase.S210S;

			if (turnDepth < 500)
				return ray.getPhase().getWaveType() == GeoAttributes.PSLOWNESS ? SeismicPhase.P410P
						: SeismicPhase.S410S;

			if (turnDepth < 700)
				return ray.getPhase().getWaveType() == GeoAttributes.PSLOWNESS ? SeismicPhase.P660P
						: SeismicPhase.S660S;

			if (turnDepth < 3000)
				return ray.getPhase().getWaveType() == GeoAttributes.PSLOWNESS ? SeismicPhase.PcP : SeismicPhase.ScS;

			return ray.getPhase().getWaveType() == GeoAttributes.PSLOWNESS ? SeismicPhase.PKiKP : SeismicPhase.SKiKS;
		}

		if (ray.getRayType() == RayType.TOP_SIDE_DIFFRACTION) {
			if (turnDepth == Globals.NA_VALUE) {
				// if turning depth is not available base phase on
				// source-receiver distance
				double distance = Math.toDegrees(ray.getDistance());

				if (distance < 25)
					return ray.getPhase().getWaveType() == GeoAttributes.PSLOWNESS ? SeismicPhase.Pn : SeismicPhase.Sn;

				if (distance < 85)
					return ray.getPhase().getWaveType() == GeoAttributes.PSLOWNESS ? SeismicPhase.P : SeismicPhase.S;

				return ray.getPhase().getWaveType() == GeoAttributes.PSLOWNESS ? SeismicPhase.Pdiff
						: SeismicPhase.Sdiff;
			}

			if (turnDepth > 2800)
				return ray.getPhase().getWaveType() == GeoAttributes.PSLOWNESS ? SeismicPhase.Pdiff
						: SeismicPhase.Sdiff;

			if (turnDepth > 410.1)
				return ray.getPhase().getWaveType() == GeoAttributes.PSLOWNESS ? SeismicPhase.P : SeismicPhase.S;

			return ray.getPhase().getWaveType() == GeoAttributes.PSLOWNESS ? SeismicPhase.Pn : SeismicPhase.Sn;

		}

		return SeismicPhase.NULL;
	}

	/**
	 * If parameter seismicPhase is of type SeismicPhase, it is returned forthwith.
	 * If it is a String (eg. Pn) or a Character (eg. P) then valueOf() is used to
	 * find and return the correct SeismicPhase object.
	 * 
	 * @param seismicPhase Object
	 * @return SeismicPhase
	 * @throws IllegalArgumentException
	 */
	static public SeismicPhase valueOf(Object seismicPhase) {
		if (seismicPhase instanceof SeismicPhase)
			return (SeismicPhase) seismicPhase;

		if (seismicPhase instanceof Character)
			return SeismicPhase.valueOf(((Character) seismicPhase).toString());

		if (seismicPhase instanceof String)
			return SeismicPhase.valueOf(((String) seismicPhase).trim());

		throw new IllegalArgumentException("seismicPhase must be a String, Characater, or SeismicPhase");
	}

	/**
	 * Wavetype (PSLOWNESS or SSLOWNESS) of the ray at the source
	 * deduced from phase name.  May be NA_VALUE if cannot be deduced.
	 */
	public GeoAttributes getWaveTypeSource() {
		return waveTypeSource;
	}

	/**
	 * Wavetype (PSLOWNESS or SSLOWNESS) of the ray at the receiver
	 * deduced from phase name.  May be NA_VALUE if cannot be deduced.
	 */
	public GeoAttributes getWaveTypeReceiver() {
		return waveTypeReceiver;
	}

	private void deduceWaveTypes(String phase)
	{
		waveTypeSource = waveTypeReceiver = GeoAttributes.NA_VALUE;
		if (phase.equals("Lg")) 
			waveTypeSource = waveTypeReceiver = GeoAttributes.SSLOWNESS;
		else if (phase.equals("nP"))
			waveTypeSource = waveTypeReceiver = GeoAttributes.PSLOWNESS;
		else
		{
			// figure out waveTypeSource
			if (phase.toUpperCase().startsWith("P")) waveTypeSource = GeoAttributes.PSLOWNESS;
			if (phase.toUpperCase().startsWith("S")) waveTypeSource = GeoAttributes.SSLOWNESS;

			// figure out waveTypeReceiver.  Start at the end of the phase name and work to the 
			// left.  As soon as 'P' or 'S' is found, set wave type and break.
			for (int i=phase.length()-1; i >= 0; --i)
			{
				if (phase.charAt(i) == 'P') { waveTypeReceiver = GeoAttributes.PSLOWNESS; break; }
				if (phase.charAt(i) == 'S') { waveTypeReceiver = GeoAttributes.SSLOWNESS; break; }
			}
		}
	}


	// slowness interface conversion list (use current slowness as start slowness)
	// interface1, slow1, interface2, slow2, ...

	// ray branch direction change list
	// rbdct1, interface1, rbdct2, interface2, ...

}
