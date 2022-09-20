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
package gov.sandia.gnem.dbtabledefs.nnsa_kb_custom;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Scanner;
import java.util.Set;

import gov.sandia.gnem.dbtabledefs.BaseRow;
import gov.sandia.gnem.dbtabledefs.Columns;

/**
 * mdac_fi
 */
public class Mdac_fi extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Correlation or post measurement processing identifier for the amplitude measurement. If no
   * correction or processing is done i.e. ('raw') measurements, the NA Value is used.
   */
  private long corrid;

  static final public long CORRID_NA = Long.MIN_VALUE;

  /**
   * Name of correction or other processing table applied to the amplitude. Examples are raw,
   * band-averaged, MDAC, phase-match filter, Coda, stack, beam, etc.
   */
  private String corrname;

  static final public String CORRNAME_NA = null;

  /**
   * Unique polygon identifier used for bounding Q tomography models.
   */
  private long polyid;

  static final public long POLYID_NA = -1;

  /**
   * Unique identifier for version of a set of parameter files.
   */
  private long versionid;

  static final public long VERSIONID_NA = -1;

  /**
   * Station code. This is the code name of a seismic observatory and identifies a geographic
   * location record in the <B>site</B> table.
   */
  private String sta;

  static final public String STA_NA = null;

  /**
   * Channel code. <I>Chan</I>,taken together with <I>sta</I>, <I>jdate</I>, and <I>time</I>,
   * uniquely identifies the seismic time-series data.
   */
  private String chan;

  static final public String CHAN_NA = null;

  /**
   * Phase type. The identity of a seismic phase that is associated with an amplitude or arrival
   * measurement.
   */
  private String phase;

  static final public String PHASE_NA = null;

  /**
   * Stress drop for MDAC processing.
   * <p>
   * Units: Pa
   */
  private double sigma;

  static final public double SIGMA_NA = Double.NaN;

  /**
   * One sigma error of the <I>sigma</I> parameter for MDAC processing
   * <p>
   * Units: Pa
   */
  private double delsigma;

  static final public double DELSIGMA_NA = -1;

  /**
   * Exponent controlling moment-corner frequency scaling for MDAC processing.
   */
  private double psi;

  static final public double PSI_NA = Double.NaN;

  /**
   * One sigma error of the <I>psi</I> parameter for MDAC processing
   */
  private double delpsi;

  static final public double DELPSI_NA = -1;

  /**
   * Attenuation at 1 Hz for MDAC processing. Not applicable if tomographic corrections are used.
   */
  private double q0;

  static final public double Q0_NA = -1;

  /**
   * One sigma error of the <I>q0</I> parameter for MDAC processing
   */
  private double delq0;

  static final public double DELQ0_NA = -1;

  /**
   * Frequency exponent of attenuation function. NA if tomographic corrections are used.
   */
  private double gamma;

  static final public double GAMMA_NA = Double.NaN;

  /**
   * One sigma error of the <I>gamma</I> parameter for MDAC processing
   */
  private double delgamma;

  static final public double DELGAMMA_NA = -1;

  /**
   * Parameter relating P- and S-wave corner frequencies for MDAC processing.
   */
  private double zeta;

  static final public double ZETA_NA = Double.NaN;

  /**
   * Reference seismic moment used with <I>psi</I>. Parameter used in MDAC calculations.
   * <p>
   * Units: N-m
   */
  private double m0_ref;

  static final public double M0_REF_NA = Double.NaN;

  /**
   * Velocity of phase for MDAC processing.
   * <p>
   * Units: m/s
   */
  private double u0;

  static final public double U0_NA = Double.NaN;

  /**
   * Geometric spreading exponent parameter for MDAC processing.
   */
  private double eta;

  static final public double ETA_NA = Double.NaN;

  /**
   * One sigma error of the <I>eta</I> parameter for MDAC processing.
   */
  private double deleta;

  static final public double DELETA_NA = -1;

  /**
   * Critical distance for geometric spreading function for MDAC processing.
   * <p>
   * Units: m
   */
  private double distcrit;

  static final public double DISTCRIT_NA = Double.NaN;

  /**
   * P velocity at the source for MDAC processing.
   * <p>
   * Units: m/s
   */
  private double alphas;

  static final public double ALPHAS_NA = Double.NaN;

  /**
   * S velocity at the source for MDAC processing
   * <p>
   * Units: m/s
   */
  private double betas;

  static final public double BETAS_NA = Double.NaN;

  /**
   * Density at source for MDAC processing.
   * <p>
   * Units: kg/(m^3)
   */
  private double rhos;

  static final public double RHOS_NA = Double.NaN;

  /**
   * P velocity at the receiver for MDAC processing.
   * <p>
   * Units: m/s
   */
  private double alphar;

  static final public double ALPHAR_NA = Double.NaN;

  /**
   * S velocity at the receiver for MDAC processing
   * <p>
   * Units: m/s
   */
  private double betar;

  static final public double BETAR_NA = Double.NaN;

  /**
   * Density at receiver for MDAC processing.
   * <p>
   * Units: kg/(m^3)
   */
  private double rhor;

  static final public double RHOR_NA = Double.NaN;

  /**
   * P wave radiation pattern excitation factor for MDAC processing.
   */
  private double radpatp;

  static final public double RADPATP_NA = Double.NaN;

  /**
   * S wave radiation pattern excitation factor for MDAC processing.
   */
  private double radpats;

  static final public double RADPATS_NA = Double.NaN;

  /**
   * Minimum signal-to-noise used to select spectral amplitudes for estimating MDAC parameters.
   */
  private double snr1;

  static final public double SNR1_NA = Double.NaN;

  /**
   * Minimum signal-to-noise used to select spectral amplitudes for applying MDAC corrections.
   */
  private double snr2;

  static final public double SNR2_NA = Double.NaN;

  /**
   * Noise identifier e.g. pre-event, pre-phase for the MDAC correction.
   */
  private String noisetype;

  static final public String NOISETYPE_NA = null;

  /**
   * Unique magnitude identifier.
   */
  private long magid;

  static final public long MAGID_NA = -1;

  /**
   * Magnitude type.
   */
  private String magtype;

  static final public String MAGTYPE_NA = null;

  /**
   * Algorithm identifier. Identifies algorithm(s) used to measure data.
   */
  private long algoid;

  static final public long ALGOID_NA = -1;

  /**
   * Author, the originator of the data or measurements. The format is varchar2(20), but that size
   * is only used in the Knowledge Base tables. All other tables (i.e. laboratory specific tables)
   * should use a size of varchar2(15). This is because "SOURCE:" is added to the auth name for the
   * KB.
   */
  private String auth;

  static final public String AUTH_NA = null;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("corrid", Columns.FieldType.LONG, "%d");
    columns.add("corrname", Columns.FieldType.STRING, "%s");
    columns.add("polyid", Columns.FieldType.LONG, "%d");
    columns.add("versionid", Columns.FieldType.LONG, "%d");
    columns.add("sta", Columns.FieldType.STRING, "%s");
    columns.add("chan", Columns.FieldType.STRING, "%s");
    columns.add("phase", Columns.FieldType.STRING, "%s");
    columns.add("sigma", Columns.FieldType.DOUBLE, "%10.3e");
    columns.add("delsigma", Columns.FieldType.DOUBLE, "%10.3e");
    columns.add("psi", Columns.FieldType.DOUBLE, "%1.3f");
    columns.add("delpsi", Columns.FieldType.DOUBLE, "%1.3f");
    columns.add("q0", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("delq0", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("gamma", Columns.FieldType.DOUBLE, "%1.3f");
    columns.add("delgamma", Columns.FieldType.DOUBLE, "%1.3f");
    columns.add("zeta", Columns.FieldType.DOUBLE, "%1.3f");
    columns.add("m0_ref", Columns.FieldType.DOUBLE, "%10.3e");
    columns.add("u0", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("eta", Columns.FieldType.DOUBLE, "%1.3f");
    columns.add("deleta", Columns.FieldType.DOUBLE, "%1.3f");
    columns.add("distcrit", Columns.FieldType.DOUBLE, "%1.1f");
    columns.add("alphas", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("betas", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("rhos", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("alphar", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("betar", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("rhor", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("radpatp", Columns.FieldType.DOUBLE, "%1.3f");
    columns.add("radpats", Columns.FieldType.DOUBLE, "%1.3f");
    columns.add("snr1", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("snr2", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("noisetype", Columns.FieldType.STRING, "%s");
    columns.add("magid", Columns.FieldType.LONG, "%d");
    columns.add("magtype", Columns.FieldType.STRING, "%s");
    columns.add("algoid", Columns.FieldType.LONG, "%d");
    columns.add("auth", Columns.FieldType.STRING, "%s");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Mdac_fi(long corrid, String corrname, long polyid, long versionid, String sta, String chan,
      String phase, double sigma, double delsigma, double psi, double delpsi, double q0,
      double delq0, double gamma, double delgamma, double zeta, double m0_ref, double u0,
      double eta, double deleta, double distcrit, double alphas, double betas, double rhos,
      double alphar, double betar, double rhor, double radpatp, double radpats, double snr1,
      double snr2, String noisetype, long magid, String magtype, long algoid, String auth) {
    setValues(corrid, corrname, polyid, versionid, sta, chan, phase, sigma, delsigma, psi, delpsi,
        q0, delq0, gamma, delgamma, zeta, m0_ref, u0, eta, deleta, distcrit, alphas, betas, rhos,
        alphar, betar, rhor, radpatp, radpats, snr1, snr2, noisetype, magid, magtype, algoid, auth);
  }

  private void setValues(long corrid, String corrname, long polyid, long versionid, String sta,
      String chan, String phase, double sigma, double delsigma, double psi, double delpsi,
      double q0, double delq0, double gamma, double delgamma, double zeta, double m0_ref, double u0,
      double eta, double deleta, double distcrit, double alphas, double betas, double rhos,
      double alphar, double betar, double rhor, double radpatp, double radpats, double snr1,
      double snr2, String noisetype, long magid, String magtype, long algoid, String auth) {
    this.corrid = corrid;
    this.corrname = corrname;
    this.polyid = polyid;
    this.versionid = versionid;
    this.sta = sta;
    this.chan = chan;
    this.phase = phase;
    this.sigma = sigma;
    this.delsigma = delsigma;
    this.psi = psi;
    this.delpsi = delpsi;
    this.q0 = q0;
    this.delq0 = delq0;
    this.gamma = gamma;
    this.delgamma = delgamma;
    this.zeta = zeta;
    this.m0_ref = m0_ref;
    this.u0 = u0;
    this.eta = eta;
    this.deleta = deleta;
    this.distcrit = distcrit;
    this.alphas = alphas;
    this.betas = betas;
    this.rhos = rhos;
    this.alphar = alphar;
    this.betar = betar;
    this.rhor = rhor;
    this.radpatp = radpatp;
    this.radpats = radpats;
    this.snr1 = snr1;
    this.snr2 = snr2;
    this.noisetype = noisetype;
    this.magid = magid;
    this.magtype = magtype;
    this.algoid = algoid;
    this.auth = auth;
  }

  /**
   * Copy constructor.
   */
  public Mdac_fi(Mdac_fi other) {
    this.corrid = other.getCorrid();
    this.corrname = other.getCorrname();
    this.polyid = other.getPolyid();
    this.versionid = other.getVersionid();
    this.sta = other.getSta();
    this.chan = other.getChan();
    this.phase = other.getPhase();
    this.sigma = other.getSigma();
    this.delsigma = other.getDelsigma();
    this.psi = other.getPsi();
    this.delpsi = other.getDelpsi();
    this.q0 = other.getQ0();
    this.delq0 = other.getDelq0();
    this.gamma = other.getGamma();
    this.delgamma = other.getDelgamma();
    this.zeta = other.getZeta();
    this.m0_ref = other.getM0_ref();
    this.u0 = other.getU0();
    this.eta = other.getEta();
    this.deleta = other.getDeleta();
    this.distcrit = other.getDistcrit();
    this.alphas = other.getAlphas();
    this.betas = other.getBetas();
    this.rhos = other.getRhos();
    this.alphar = other.getAlphar();
    this.betar = other.getBetar();
    this.rhor = other.getRhor();
    this.radpatp = other.getRadpatp();
    this.radpats = other.getRadpats();
    this.snr1 = other.getSnr1();
    this.snr2 = other.getSnr2();
    this.noisetype = other.getNoisetype();
    this.magid = other.getMagid();
    this.magtype = other.getMagtype();
    this.algoid = other.getAlgoid();
    this.auth = other.getAuth();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Mdac_fi() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(CORRID_NA, CORRNAME_NA, POLYID_NA, VERSIONID_NA, STA_NA, CHAN_NA, PHASE_NA, SIGMA_NA,
        DELSIGMA_NA, PSI_NA, DELPSI_NA, Q0_NA, DELQ0_NA, GAMMA_NA, DELGAMMA_NA, ZETA_NA, M0_REF_NA,
        U0_NA, ETA_NA, DELETA_NA, DISTCRIT_NA, ALPHAS_NA, BETAS_NA, RHOS_NA, ALPHAR_NA, BETAR_NA,
        RHOR_NA, RADPATP_NA, RADPATS_NA, SNR1_NA, SNR2_NA, NOISETYPE_NA, MAGID_NA, MAGTYPE_NA,
        ALGOID_NA, AUTH_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "corrname":
        return corrname;
      case "sta":
        return sta;
      case "chan":
        return chan;
      case "phase":
        return phase;
      case "noisetype":
        return noisetype;
      case "magtype":
        return magtype;
      case "auth":
        return auth;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setStringField(String name, String input) throws IOException {
    String value = getInputString(input);
    switch (name) {
      case "corrname":
        corrname = value;
        break;
      case "sta":
        sta = value;
        break;
      case "chan":
        chan = value;
        break;
      case "phase":
        phase = value;
        break;
      case "noisetype":
        noisetype = value;
        break;
      case "magtype":
        magtype = value;
        break;
      case "auth":
        auth = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public double getDoubleField(String name) throws IOException {
    switch (name) {
      case "sigma":
        return sigma;
      case "delsigma":
        return delsigma;
      case "psi":
        return psi;
      case "delpsi":
        return delpsi;
      case "q0":
        return q0;
      case "delq0":
        return delq0;
      case "gamma":
        return gamma;
      case "delgamma":
        return delgamma;
      case "zeta":
        return zeta;
      case "m0_ref":
        return m0_ref;
      case "u0":
        return u0;
      case "eta":
        return eta;
      case "deleta":
        return deleta;
      case "distcrit":
        return distcrit;
      case "alphas":
        return alphas;
      case "betas":
        return betas;
      case "rhos":
        return rhos;
      case "alphar":
        return alphar;
      case "betar":
        return betar;
      case "rhor":
        return rhor;
      case "radpatp":
        return radpatp;
      case "radpats":
        return radpats;
      case "snr1":
        return snr1;
      case "snr2":
        return snr2;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setDoubleField(String name, String input) throws IOException {
    double value = getInputDouble(input, name, this.getClass().getName());
    switch (name) {
      case "sigma":
        sigma = value;
        break;
      case "delsigma":
        delsigma = value;
        break;
      case "psi":
        psi = value;
        break;
      case "delpsi":
        delpsi = value;
        break;
      case "q0":
        q0 = value;
        break;
      case "delq0":
        delq0 = value;
        break;
      case "gamma":
        gamma = value;
        break;
      case "delgamma":
        delgamma = value;
        break;
      case "zeta":
        zeta = value;
        break;
      case "m0_ref":
        m0_ref = value;
        break;
      case "u0":
        u0 = value;
        break;
      case "eta":
        eta = value;
        break;
      case "deleta":
        deleta = value;
        break;
      case "distcrit":
        distcrit = value;
        break;
      case "alphas":
        alphas = value;
        break;
      case "betas":
        betas = value;
        break;
      case "rhos":
        rhos = value;
        break;
      case "alphar":
        alphar = value;
        break;
      case "betar":
        betar = value;
        break;
      case "rhor":
        rhor = value;
        break;
      case "radpatp":
        radpatp = value;
        break;
      case "radpats":
        radpats = value;
        break;
      case "snr1":
        snr1 = value;
        break;
      case "snr2":
        snr2 = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "corrid":
        return corrid;
      case "polyid":
        return polyid;
      case "versionid":
        return versionid;
      case "magid":
        return magid;
      case "algoid":
        return algoid;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "corrid":
        corrid = value;
        break;
      case "polyid":
        polyid = value;
        break;
      case "versionid":
        versionid = value;
        break;
      case "magid":
        magid = value;
        break;
      case "algoid":
        algoid = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  /**
   * Sets inputColumnNames to the input columnNames. If any entry in columnNames is invalid, or if
   * not all required columns are contained in columnNames then an error is thrown.
   * 
   * @param columnNames
   * @throws IOException
   */
  public static void setNewInputColumnNames(String[] columnNames) throws IOException {
    columns.containsValidColumnNames(columnNames);
    columns.containsAllRequiredColumns(columnNames);
    inputColumnNames = columnNames;
  }

  /**
   * Sets outputColumnNames to the input columnNames. If any entry in columnNames is invalid then an
   * error is thrown.
   * 
   * @param columnNames
   * @throws IOException
   */
  public static void setNewOutputColumnNames(String[] columnNames) throws IOException {
    columns.containsValidColumnNames(columnNames);
    outputColumnNames = columnNames;
  }

  public static Columns getColumns() {
    return columns;
  }

  public static String[] getInputColumnNames() {
    return inputColumnNames;
  }

  public static String[] getOutputColumnNames() {
    return outputColumnNames;
  }

  /**
   * Constructor that loads values from a Scanner. It can read the output of the toString()
   * function.
   */
  public Mdac_fi(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Mdac_fi(DataInputStream input) throws IOException {
    this(input.readLong(), readString(input), input.readLong(), input.readLong(), readString(input),
        readString(input), readString(input), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readDouble(), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readDouble(), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readDouble(), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readDouble(), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readDouble(), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readDouble(), readString(input), input.readLong(),
        readString(input), input.readLong(), readString(input));
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Mdac_fi(ByteBuffer input) {
    this(input.getLong(), readString(input), input.getLong(), input.getLong(), readString(input),
        readString(input), readString(input), input.getDouble(), input.getDouble(),
        input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(),
        input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(),
        input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(),
        input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(),
        input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(),
        input.getDouble(), input.getDouble(), readString(input), input.getLong(), readString(input),
        input.getLong(), readString(input));
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Mdac_fi(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Mdac_fi(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getString(offset + 2), input.getLong(offset + 3),
        input.getLong(offset + 4), input.getString(offset + 5), input.getString(offset + 6),
        input.getString(offset + 7), input.getDouble(offset + 8), input.getDouble(offset + 9),
        input.getDouble(offset + 10), input.getDouble(offset + 11), input.getDouble(offset + 12),
        input.getDouble(offset + 13), input.getDouble(offset + 14), input.getDouble(offset + 15),
        input.getDouble(offset + 16), input.getDouble(offset + 17), input.getDouble(offset + 18),
        input.getDouble(offset + 19), input.getDouble(offset + 20), input.getDouble(offset + 21),
        input.getDouble(offset + 22), input.getDouble(offset + 23), input.getDouble(offset + 24),
        input.getDouble(offset + 25), input.getDouble(offset + 26), input.getDouble(offset + 27),
        input.getDouble(offset + 28), input.getDouble(offset + 29), input.getDouble(offset + 30),
        input.getDouble(offset + 31), input.getString(offset + 32), input.getLong(offset + 33),
        input.getString(offset + 34), input.getLong(offset + 35), input.getString(offset + 36));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[36];
    values[0] = corrid;
    values[1] = corrname;
    values[2] = polyid;
    values[3] = versionid;
    values[4] = sta;
    values[5] = chan;
    values[6] = phase;
    values[7] = sigma;
    values[8] = delsigma;
    values[9] = psi;
    values[10] = delpsi;
    values[11] = q0;
    values[12] = delq0;
    values[13] = gamma;
    values[14] = delgamma;
    values[15] = zeta;
    values[16] = m0_ref;
    values[17] = u0;
    values[18] = eta;
    values[19] = deleta;
    values[20] = distcrit;
    values[21] = alphas;
    values[22] = betas;
    values[23] = rhos;
    values[24] = alphar;
    values[25] = betar;
    values[26] = rhor;
    values[27] = radpatp;
    values[28] = radpats;
    values[29] = snr1;
    values[30] = snr2;
    values[31] = noisetype;
    values[32] = magid;
    values[33] = magtype;
    values[34] = algoid;
    values[35] = auth;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[37];
    values[0] = corrid;
    values[1] = corrname;
    values[2] = polyid;
    values[3] = versionid;
    values[4] = sta;
    values[5] = chan;
    values[6] = phase;
    values[7] = sigma;
    values[8] = delsigma;
    values[9] = psi;
    values[10] = delpsi;
    values[11] = q0;
    values[12] = delq0;
    values[13] = gamma;
    values[14] = delgamma;
    values[15] = zeta;
    values[16] = m0_ref;
    values[17] = u0;
    values[18] = eta;
    values[19] = deleta;
    values[20] = distcrit;
    values[21] = alphas;
    values[22] = betas;
    values[23] = rhos;
    values[24] = alphar;
    values[25] = betar;
    values[26] = rhor;
    values[27] = radpatp;
    values[28] = radpats;
    values[29] = snr1;
    values[30] = snr2;
    values[31] = noisetype;
    values[32] = magid;
    values[33] = magtype;
    values[34] = algoid;
    values[35] = auth;
    values[36] = lddate;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * <p>
   * The supplied java.util.Date is converted to a java.sql.Date in the output.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.util.Date lddate) {
    return getValues(new java.sql.Date(lddate.getTime()));
  }

  /**
   * Write this row to a DataOutputStream.
   */
  public void write(DataOutputStream output) throws IOException {
    output.writeLong(corrid);
    writeString(output, corrname);
    output.writeLong(polyid);
    output.writeLong(versionid);
    writeString(output, sta);
    writeString(output, chan);
    writeString(output, phase);
    output.writeDouble(sigma);
    output.writeDouble(delsigma);
    output.writeDouble(psi);
    output.writeDouble(delpsi);
    output.writeDouble(q0);
    output.writeDouble(delq0);
    output.writeDouble(gamma);
    output.writeDouble(delgamma);
    output.writeDouble(zeta);
    output.writeDouble(m0_ref);
    output.writeDouble(u0);
    output.writeDouble(eta);
    output.writeDouble(deleta);
    output.writeDouble(distcrit);
    output.writeDouble(alphas);
    output.writeDouble(betas);
    output.writeDouble(rhos);
    output.writeDouble(alphar);
    output.writeDouble(betar);
    output.writeDouble(rhor);
    output.writeDouble(radpatp);
    output.writeDouble(radpats);
    output.writeDouble(snr1);
    output.writeDouble(snr2);
    writeString(output, noisetype);
    output.writeLong(magid);
    writeString(output, magtype);
    output.writeLong(algoid);
    writeString(output, auth);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(corrid);
    writeString(output, corrname);
    output.putLong(polyid);
    output.putLong(versionid);
    writeString(output, sta);
    writeString(output, chan);
    writeString(output, phase);
    output.putDouble(sigma);
    output.putDouble(delsigma);
    output.putDouble(psi);
    output.putDouble(delpsi);
    output.putDouble(q0);
    output.putDouble(delq0);
    output.putDouble(gamma);
    output.putDouble(delgamma);
    output.putDouble(zeta);
    output.putDouble(m0_ref);
    output.putDouble(u0);
    output.putDouble(eta);
    output.putDouble(deleta);
    output.putDouble(distcrit);
    output.putDouble(alphas);
    output.putDouble(betas);
    output.putDouble(rhos);
    output.putDouble(alphar);
    output.putDouble(betar);
    output.putDouble(rhor);
    output.putDouble(radpatp);
    output.putDouble(radpats);
    output.putDouble(snr1);
    output.putDouble(snr2);
    writeString(output, noisetype);
    output.putLong(magid);
    writeString(output, magtype);
    output.putLong(algoid);
    writeString(output, auth);
  }

  /**
   * Read a Collection of Mdac_fi objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Mdac_fi objects.
   * @throws IOException
   */
  static public void readMdac_fis(BufferedReader input, Collection<Mdac_fi> rows)
      throws IOException {
    String[] saved = Mdac_fi.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Mdac_fi.setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Mdac_fi(new Scanner(line)));
    }
    input.close();
    Mdac_fi.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Mdac_fi objects from an ascii file. The Collection is not emptied before
   * reading.
   * 
   * @param inputFile
   * @param rows a Collection of Mdac_fi objects.
   * @throws IOException
   */
  static public void readMdac_fis(File inputFile, Collection<Mdac_fi> rows) throws IOException {
    readMdac_fis(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Mdac_fi objects from an ascii input stream. The Collection is not emptied
   * before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Mdac_fi objects.
   * @throws IOException
   */
  static public void readMdac_fis(InputStream inputStream, Collection<Mdac_fi> rows)
      throws IOException {
    readMdac_fis(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Mdac_fi objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Mdac_fi objects
   * @throws IOException
   */
  static public Set<Mdac_fi> readMdac_fis(BufferedReader input) throws IOException {
    Set<Mdac_fi> rows = new LinkedHashSet<Mdac_fi>();
    readMdac_fis(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Mdac_fi objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Mdac_fi objects
   * @throws IOException
   */
  static public Set<Mdac_fi> readMdac_fis(File inputFile) throws IOException {
    return readMdac_fis(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Mdac_fi objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Mdac_fi objects
   * @throws IOException
   */
  static public Set<Mdac_fi> readMdac_fis(InputStream input) throws IOException {
    return readMdac_fis(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Mdac_fi objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param mdac_fis the Mdac_fi objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Mdac_fi> mdac_fis)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Mdac_fi mdac_fi : mdac_fis)
      mdac_fi.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Mdac_fi objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param mdac_fis the Mdac_fi objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Mdac_fi> mdac_fis, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement("insert into " + tableName
          + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
      for (Mdac_fi mdac_fi : mdac_fis) {
        int i = 0;
        statement.setLong(++i, mdac_fi.corrid);
        statement.setString(++i, mdac_fi.corrname);
        statement.setLong(++i, mdac_fi.polyid);
        statement.setLong(++i, mdac_fi.versionid);
        statement.setString(++i, mdac_fi.sta);
        statement.setString(++i, mdac_fi.chan);
        statement.setString(++i, mdac_fi.phase);
        statement.setDouble(++i, mdac_fi.sigma);
        statement.setDouble(++i, mdac_fi.delsigma);
        statement.setDouble(++i, mdac_fi.psi);
        statement.setDouble(++i, mdac_fi.delpsi);
        statement.setDouble(++i, mdac_fi.q0);
        statement.setDouble(++i, mdac_fi.delq0);
        statement.setDouble(++i, mdac_fi.gamma);
        statement.setDouble(++i, mdac_fi.delgamma);
        statement.setDouble(++i, mdac_fi.zeta);
        statement.setDouble(++i, mdac_fi.m0_ref);
        statement.setDouble(++i, mdac_fi.u0);
        statement.setDouble(++i, mdac_fi.eta);
        statement.setDouble(++i, mdac_fi.deleta);
        statement.setDouble(++i, mdac_fi.distcrit);
        statement.setDouble(++i, mdac_fi.alphas);
        statement.setDouble(++i, mdac_fi.betas);
        statement.setDouble(++i, mdac_fi.rhos);
        statement.setDouble(++i, mdac_fi.alphar);
        statement.setDouble(++i, mdac_fi.betar);
        statement.setDouble(++i, mdac_fi.rhor);
        statement.setDouble(++i, mdac_fi.radpatp);
        statement.setDouble(++i, mdac_fi.radpats);
        statement.setDouble(++i, mdac_fi.snr1);
        statement.setDouble(++i, mdac_fi.snr2);
        statement.setString(++i, mdac_fi.noisetype);
        statement.setLong(++i, mdac_fi.magid);
        statement.setString(++i, mdac_fi.magtype);
        statement.setLong(++i, mdac_fi.algoid);
        statement.setString(++i, mdac_fi.auth);
        statement.setTimestamp(++i, new java.sql.Timestamp(lddate.getTime()));
        statement.addBatch();
      }
      statement.executeBatch();
      statement.close();
      if (commit)
        connection.commit();
    } finally {
      if (statement != null)
        statement.close();
    }
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a Mdac_fi
   *        table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Mdac_fi> readMdac_fis(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Mdac_fi> results = new HashSet<Mdac_fi>();
    readMdac_fis(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a Mdac_fi
   *        table.
   * @param mdac_fis
   * @throws SQLException
   */
  static public void readMdac_fis(Connection connection, String selectStatement,
      Set<Mdac_fi> mdac_fis) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        mdac_fis.add(new Mdac_fi(rs));
    } catch (Exception e) {
      throw new SQLException(String.format("%s%n%s%n", e.getMessage(), selectStatement));
    } finally {
      if (rs != null)
        rs.close();
      if (statement != null)
        statement.close();
    }
  }

  /**
   * Retrieve a String representation of a sql statement that can be used to insert the values of
   * this Mdac_fi object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Mdac_fi object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "corrid, corrname, polyid, versionid, sta, chan, phase, sigma, delsigma, psi, delpsi, q0, delq0, gamma, delgamma, zeta, m0_ref, u0, eta, deleta, distcrit, alphas, betas, rhos, alphar, betar, rhor, radpatp, radpats, snr1, snr2, noisetype, magid, magtype, algoid, auth, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(corrid)).append(", ");
    sql.append("'").append(corrname).append("', ");
    sql.append(Long.toString(polyid)).append(", ");
    sql.append(Long.toString(versionid)).append(", ");
    sql.append("'").append(sta).append("', ");
    sql.append("'").append(chan).append("', ");
    sql.append("'").append(phase).append("', ");
    sql.append(Double.toString(sigma)).append(", ");
    sql.append(Double.toString(delsigma)).append(", ");
    sql.append(Double.toString(psi)).append(", ");
    sql.append(Double.toString(delpsi)).append(", ");
    sql.append(Double.toString(q0)).append(", ");
    sql.append(Double.toString(delq0)).append(", ");
    sql.append(Double.toString(gamma)).append(", ");
    sql.append(Double.toString(delgamma)).append(", ");
    sql.append(Double.toString(zeta)).append(", ");
    sql.append(Double.toString(m0_ref)).append(", ");
    sql.append(Double.toString(u0)).append(", ");
    sql.append(Double.toString(eta)).append(", ");
    sql.append(Double.toString(deleta)).append(", ");
    sql.append(Double.toString(distcrit)).append(", ");
    sql.append(Double.toString(alphas)).append(", ");
    sql.append(Double.toString(betas)).append(", ");
    sql.append(Double.toString(rhos)).append(", ");
    sql.append(Double.toString(alphar)).append(", ");
    sql.append(Double.toString(betar)).append(", ");
    sql.append(Double.toString(rhor)).append(", ");
    sql.append(Double.toString(radpatp)).append(", ");
    sql.append(Double.toString(radpats)).append(", ");
    sql.append(Double.toString(snr1)).append(", ");
    sql.append(Double.toString(snr2)).append(", ");
    sql.append("'").append(noisetype).append("', ");
    sql.append(Long.toString(magid)).append(", ");
    sql.append("'").append(magtype).append("', ");
    sql.append(Long.toString(algoid)).append(", ");
    sql.append("'").append(auth).append("', ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Mdac_fi in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Mdac_fi in the database
   * 
   * @param connection
   * @param tableName
   * @param includePrimaryKeyConstraint
   * @param includeUniqueKeyConstraint
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName,
      boolean includePrimaryKeyConstraint, boolean includeUniqueKeyConstraint) throws SQLException {
    Statement statement = connection.createStatement();
    for (String s : createTableScript(tableName, includePrimaryKeyConstraint,
        includeUniqueKeyConstraint))
      statement.execute(s);
    statement.close();
  }

  /**
   * Generate a sql script to create a table of type Mdac_fi in the database Primary and unique keys
   * are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Mdac_fi in the database
   * 
   * @param tableName
   * @param includePrimaryKeyConstraint
   * @param includeUniqueKeyConstraint
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName,
      boolean includePrimaryKeyConstraint, boolean includeUniqueKeyConstraint) throws SQLException {
    ArrayList<String> script = new ArrayList<String>();
    StringBuffer buf = new StringBuffer();
    buf.append("create table " + tableName + " (\n");
    buf.append("corrid       number(9)            NOT NULL,\n");
    buf.append("corrname     varchar2(32)         NOT NULL,\n");
    buf.append("polyid       number(9)            NOT NULL,\n");
    buf.append("versionid    number(9)            NOT NULL,\n");
    buf.append("sta          varchar2(6)          NOT NULL,\n");
    buf.append("chan         varchar2(8)          NOT NULL,\n");
    buf.append("phase        varchar2(8)          NOT NULL,\n");
    buf.append("sigma        float(24)            NOT NULL,\n");
    buf.append("delsigma     float(24)            NOT NULL,\n");
    buf.append("psi          float(24)            NOT NULL,\n");
    buf.append("delpsi       float(24)            NOT NULL,\n");
    buf.append("q0           float(24)            NOT NULL,\n");
    buf.append("delq0        float(24)            NOT NULL,\n");
    buf.append("gamma        float(24)            NOT NULL,\n");
    buf.append("delgamma     float(24)            NOT NULL,\n");
    buf.append("zeta         float(24)            NOT NULL,\n");
    buf.append("m0_ref       float(24)            NOT NULL,\n");
    buf.append("u0           float(24)            NOT NULL,\n");
    buf.append("eta          float(24)            NOT NULL,\n");
    buf.append("deleta       float(24)            NOT NULL,\n");
    buf.append("distcrit     float(24)            NOT NULL,\n");
    buf.append("alphas       float(24)            NOT NULL,\n");
    buf.append("betas        float(24)            NOT NULL,\n");
    buf.append("rhos         float(24)            NOT NULL,\n");
    buf.append("alphar       float(24)            NOT NULL,\n");
    buf.append("betar        float(24)            NOT NULL,\n");
    buf.append("rhor         float(24)            NOT NULL,\n");
    buf.append("radpatp      float(24)            NOT NULL,\n");
    buf.append("radpats      float(24)            NOT NULL,\n");
    buf.append("snr1         float(24)            NOT NULL,\n");
    buf.append("snr2         float(24)            NOT NULL,\n");
    buf.append("noisetype    varchar2(15)         NOT NULL,\n");
    buf.append("magid        number(9)            NOT NULL,\n");
    buf.append("magtype      varchar2(6)          NOT NULL,\n");
    buf.append("algoid       number(9)            NOT NULL,\n");
    buf.append("auth         varchar2(20)         NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (corrid)");
    if (includeUniqueKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_uk unique (versionid,sta,chan,phase)");
    script.add("grant select on " + tableName + " to public");
    return script;
  }

  /**
   * Write this row to an ascii String with no newline at the end.
   */
  @Override
  public String toString() {
    try {
      return getOutputString(outputColumnNames, columns);
    } catch (Exception ex) {
      System.out.println(ex.getStackTrace());
      return "";
    }
  }

  /**
   * Write this row to an ascii file. No newline is appended at the end of the record.
   */
  public void write(BufferedWriter output) throws IOException {
    output.write(toString());
  }

  /**
   * Write this row to an ascii file, including a newline appended at the end of the record.
   */
  public void writeln(BufferedWriter output) throws IOException {
    output.write(toString());
    output.newLine();
  }

  /**
   * Return table output header line.
   * 
   * @param outputColumnNames output table column names.
   * @return Table output header line.
   */
  public static String getHeader() {
    return getOutputHeaderString(outputColumnNames);
  }

  /**
   * Writes the output header to the input buffered writer.
   * 
   * @param output The buffered writer.
   * @throws IOException
   */
  public static void writeHeader(BufferedWriter output) throws IOException {
    output.write(getOutputHeaderString(outputColumnNames));
    output.newLine();
  }

  /**
   * Maximum number of bytes required to store an instance of this in a ByteBuffer or
   * DataOutputStream.
   */
  @Override
  public int maxBytes() {
    return 355;
  }

  /**
   * Return true if primary keys are equal in this and other. Returns false if primary keys are not
   * defined.
   * 
   * @param other
   * @return true if primary keys are equal in this and other.
   */
  @Override
  public boolean equalPrimaryKey(BaseRow other) {
    return (other instanceof Mdac_fi) && ((Mdac_fi) other).corrid == corrid;
  }

  /**
   * Return true if unique keys are equal in this and other. Returns false if unique keys are not
   * defined.
   * 
   * @param other
   * @return true if unique keys are equal in this and other.
   */
  @Override
  public boolean equalUniqueKey(BaseRow other) {
    return (other instanceof Mdac_fi) && ((Mdac_fi) other).versionid == versionid
        && ((Mdac_fi) other).sta.equals(sta) && ((Mdac_fi) other).chan.equals(chan)
        && ((Mdac_fi) other).phase.equals(phase);
  }

  /**
   * Correlation or post measurement processing identifier for the amplitude measurement. If no
   * correction or processing is done i.e. ('raw') measurements, the NA Value is used.
   * 
   * @return corrid
   */
  public long getCorrid() {
    return corrid;
  }

  /**
   * Correlation or post measurement processing identifier for the amplitude measurement. If no
   * correction or processing is done i.e. ('raw') measurements, the NA Value is used.
   * 
   * @param corrid
   * @throws IllegalArgumentException if corrid >= 1000000000
   */
  public Mdac_fi setCorrid(long corrid) {
    if (corrid >= 1000000000L)
      throw new IllegalArgumentException("corrid=" + corrid + " but cannot be >= 1000000000");
    this.corrid = corrid;
    setHash(null);
    return this;
  }

  /**
   * Name of correction or other processing table applied to the amplitude. Examples are raw,
   * band-averaged, MDAC, phase-match filter, Coda, stack, beam, etc.
   * 
   * @return corrname
   */
  public String getCorrname() {
    return corrname;
  }

  /**
   * Name of correction or other processing table applied to the amplitude. Examples are raw,
   * band-averaged, MDAC, phase-match filter, Coda, stack, beam, etc.
   * 
   * @param corrname
   * @throws IllegalArgumentException if corrname.length() >= 32
   */
  public Mdac_fi setCorrname(String corrname) {
    if (corrname.length() > 32)
      throw new IllegalArgumentException(
          String.format("corrname.length() cannot be > 32.  corrname=%s", corrname));
    this.corrname = corrname;
    setHash(null);
    return this;
  }

  /**
   * Unique polygon identifier used for bounding Q tomography models.
   * 
   * @return polyid
   */
  public long getPolyid() {
    return polyid;
  }

  /**
   * Unique polygon identifier used for bounding Q tomography models.
   * 
   * @param polyid
   * @throws IllegalArgumentException if polyid >= 1000000000
   */
  public Mdac_fi setPolyid(long polyid) {
    if (polyid >= 1000000000L)
      throw new IllegalArgumentException("polyid=" + polyid + " but cannot be >= 1000000000");
    this.polyid = polyid;
    setHash(null);
    return this;
  }

  /**
   * Unique identifier for version of a set of parameter files.
   * 
   * @return versionid
   */
  public long getVersionid() {
    return versionid;
  }

  /**
   * Unique identifier for version of a set of parameter files.
   * 
   * @param versionid
   * @throws IllegalArgumentException if versionid >= 1000000000
   */
  public Mdac_fi setVersionid(long versionid) {
    if (versionid >= 1000000000L)
      throw new IllegalArgumentException("versionid=" + versionid + " but cannot be >= 1000000000");
    this.versionid = versionid;
    setHash(null);
    return this;
  }

  /**
   * Station code. This is the code name of a seismic observatory and identifies a geographic
   * location record in the <B>site</B> table.
   * 
   * @return sta
   */
  public String getSta() {
    return sta;
  }

  /**
   * Station code. This is the code name of a seismic observatory and identifies a geographic
   * location record in the <B>site</B> table.
   * 
   * @param sta
   * @throws IllegalArgumentException if sta.length() >= 6
   */
  public Mdac_fi setSta(String sta) {
    if (sta.length() > 6)
      throw new IllegalArgumentException(String.format("sta.length() cannot be > 6.  sta=%s", sta));
    this.sta = sta;
    setHash(null);
    return this;
  }

  /**
   * Channel code. <I>Chan</I>,taken together with <I>sta</I>, <I>jdate</I>, and <I>time</I>,
   * uniquely identifies the seismic time-series data.
   * 
   * @return chan
   */
  public String getChan() {
    return chan;
  }

  /**
   * Channel code. <I>Chan</I>,taken together with <I>sta</I>, <I>jdate</I>, and <I>time</I>,
   * uniquely identifies the seismic time-series data.
   * 
   * @param chan
   * @throws IllegalArgumentException if chan.length() >= 8
   */
  public Mdac_fi setChan(String chan) {
    if (chan.length() > 8)
      throw new IllegalArgumentException(
          String.format("chan.length() cannot be > 8.  chan=%s", chan));
    this.chan = chan;
    setHash(null);
    return this;
  }

  /**
   * Phase type. The identity of a seismic phase that is associated with an amplitude or arrival
   * measurement.
   * 
   * @return phase
   */
  public String getPhase() {
    return phase;
  }

  /**
   * Phase type. The identity of a seismic phase that is associated with an amplitude or arrival
   * measurement.
   * 
   * @param phase
   * @throws IllegalArgumentException if phase.length() >= 8
   */
  public Mdac_fi setPhase(String phase) {
    if (phase.length() > 8)
      throw new IllegalArgumentException(
          String.format("phase.length() cannot be > 8.  phase=%s", phase));
    this.phase = phase;
    setHash(null);
    return this;
  }

  /**
   * Stress drop for MDAC processing.
   * <p>
   * Units: Pa
   * 
   * @return sigma
   */
  public double getSigma() {
    return sigma;
  }

  /**
   * Stress drop for MDAC processing.
   * <p>
   * Units: Pa
   * 
   * @param sigma
   */
  public Mdac_fi setSigma(double sigma) {
    this.sigma = sigma;
    setHash(null);
    return this;
  }

  /**
   * One sigma error of the <I>sigma</I> parameter for MDAC processing
   * <p>
   * Units: Pa
   * 
   * @return delsigma
   */
  public double getDelsigma() {
    return delsigma;
  }

  /**
   * One sigma error of the <I>sigma</I> parameter for MDAC processing
   * <p>
   * Units: Pa
   * 
   * @param delsigma
   */
  public Mdac_fi setDelsigma(double delsigma) {
    this.delsigma = delsigma;
    setHash(null);
    return this;
  }

  /**
   * Exponent controlling moment-corner frequency scaling for MDAC processing.
   * 
   * @return psi
   */
  public double getPsi() {
    return psi;
  }

  /**
   * Exponent controlling moment-corner frequency scaling for MDAC processing.
   * 
   * @param psi
   */
  public Mdac_fi setPsi(double psi) {
    this.psi = psi;
    setHash(null);
    return this;
  }

  /**
   * One sigma error of the <I>psi</I> parameter for MDAC processing
   * 
   * @return delpsi
   */
  public double getDelpsi() {
    return delpsi;
  }

  /**
   * One sigma error of the <I>psi</I> parameter for MDAC processing
   * 
   * @param delpsi
   */
  public Mdac_fi setDelpsi(double delpsi) {
    this.delpsi = delpsi;
    setHash(null);
    return this;
  }

  /**
   * Attenuation at 1 Hz for MDAC processing. Not applicable if tomographic corrections are used.
   * 
   * @return q0
   */
  public double getQ0() {
    return q0;
  }

  /**
   * Attenuation at 1 Hz for MDAC processing. Not applicable if tomographic corrections are used.
   * 
   * @param q0
   */
  public Mdac_fi setQ0(double q0) {
    this.q0 = q0;
    setHash(null);
    return this;
  }

  /**
   * One sigma error of the <I>q0</I> parameter for MDAC processing
   * 
   * @return delq0
   */
  public double getDelq0() {
    return delq0;
  }

  /**
   * One sigma error of the <I>q0</I> parameter for MDAC processing
   * 
   * @param delq0
   */
  public Mdac_fi setDelq0(double delq0) {
    this.delq0 = delq0;
    setHash(null);
    return this;
  }

  /**
   * Frequency exponent of attenuation function. NA if tomographic corrections are used.
   * 
   * @return gamma
   */
  public double getGamma() {
    return gamma;
  }

  /**
   * Frequency exponent of attenuation function. NA if tomographic corrections are used.
   * 
   * @param gamma
   */
  public Mdac_fi setGamma(double gamma) {
    this.gamma = gamma;
    setHash(null);
    return this;
  }

  /**
   * One sigma error of the <I>gamma</I> parameter for MDAC processing
   * 
   * @return delgamma
   */
  public double getDelgamma() {
    return delgamma;
  }

  /**
   * One sigma error of the <I>gamma</I> parameter for MDAC processing
   * 
   * @param delgamma
   */
  public Mdac_fi setDelgamma(double delgamma) {
    this.delgamma = delgamma;
    setHash(null);
    return this;
  }

  /**
   * Parameter relating P- and S-wave corner frequencies for MDAC processing.
   * 
   * @return zeta
   */
  public double getZeta() {
    return zeta;
  }

  /**
   * Parameter relating P- and S-wave corner frequencies for MDAC processing.
   * 
   * @param zeta
   */
  public Mdac_fi setZeta(double zeta) {
    this.zeta = zeta;
    setHash(null);
    return this;
  }

  /**
   * Reference seismic moment used with <I>psi</I>. Parameter used in MDAC calculations.
   * <p>
   * Units: N-m
   * 
   * @return m0_ref
   */
  public double getM0_ref() {
    return m0_ref;
  }

  /**
   * Reference seismic moment used with <I>psi</I>. Parameter used in MDAC calculations.
   * <p>
   * Units: N-m
   * 
   * @param m0_ref
   */
  public Mdac_fi setM0_ref(double m0_ref) {
    this.m0_ref = m0_ref;
    setHash(null);
    return this;
  }

  /**
   * Velocity of phase for MDAC processing.
   * <p>
   * Units: m/s
   * 
   * @return u0
   */
  public double getU0() {
    return u0;
  }

  /**
   * Velocity of phase for MDAC processing.
   * <p>
   * Units: m/s
   * 
   * @param u0
   */
  public Mdac_fi setU0(double u0) {
    this.u0 = u0;
    setHash(null);
    return this;
  }

  /**
   * Geometric spreading exponent parameter for MDAC processing.
   * 
   * @return eta
   */
  public double getEta() {
    return eta;
  }

  /**
   * Geometric spreading exponent parameter for MDAC processing.
   * 
   * @param eta
   */
  public Mdac_fi setEta(double eta) {
    this.eta = eta;
    setHash(null);
    return this;
  }

  /**
   * One sigma error of the <I>eta</I> parameter for MDAC processing.
   * 
   * @return deleta
   */
  public double getDeleta() {
    return deleta;
  }

  /**
   * One sigma error of the <I>eta</I> parameter for MDAC processing.
   * 
   * @param deleta
   */
  public Mdac_fi setDeleta(double deleta) {
    this.deleta = deleta;
    setHash(null);
    return this;
  }

  /**
   * Critical distance for geometric spreading function for MDAC processing.
   * <p>
   * Units: m
   * 
   * @return distcrit
   */
  public double getDistcrit() {
    return distcrit;
  }

  /**
   * Critical distance for geometric spreading function for MDAC processing.
   * <p>
   * Units: m
   * 
   * @param distcrit
   */
  public Mdac_fi setDistcrit(double distcrit) {
    this.distcrit = distcrit;
    setHash(null);
    return this;
  }

  /**
   * P velocity at the source for MDAC processing.
   * <p>
   * Units: m/s
   * 
   * @return alphas
   */
  public double getAlphas() {
    return alphas;
  }

  /**
   * P velocity at the source for MDAC processing.
   * <p>
   * Units: m/s
   * 
   * @param alphas
   */
  public Mdac_fi setAlphas(double alphas) {
    this.alphas = alphas;
    setHash(null);
    return this;
  }

  /**
   * S velocity at the source for MDAC processing
   * <p>
   * Units: m/s
   * 
   * @return betas
   */
  public double getBetas() {
    return betas;
  }

  /**
   * S velocity at the source for MDAC processing
   * <p>
   * Units: m/s
   * 
   * @param betas
   */
  public Mdac_fi setBetas(double betas) {
    this.betas = betas;
    setHash(null);
    return this;
  }

  /**
   * Density at source for MDAC processing.
   * <p>
   * Units: kg/(m^3)
   * 
   * @return rhos
   */
  public double getRhos() {
    return rhos;
  }

  /**
   * Density at source for MDAC processing.
   * <p>
   * Units: kg/(m^3)
   * 
   * @param rhos
   */
  public Mdac_fi setRhos(double rhos) {
    this.rhos = rhos;
    setHash(null);
    return this;
  }

  /**
   * P velocity at the receiver for MDAC processing.
   * <p>
   * Units: m/s
   * 
   * @return alphar
   */
  public double getAlphar() {
    return alphar;
  }

  /**
   * P velocity at the receiver for MDAC processing.
   * <p>
   * Units: m/s
   * 
   * @param alphar
   */
  public Mdac_fi setAlphar(double alphar) {
    this.alphar = alphar;
    setHash(null);
    return this;
  }

  /**
   * S velocity at the receiver for MDAC processing
   * <p>
   * Units: m/s
   * 
   * @return betar
   */
  public double getBetar() {
    return betar;
  }

  /**
   * S velocity at the receiver for MDAC processing
   * <p>
   * Units: m/s
   * 
   * @param betar
   */
  public Mdac_fi setBetar(double betar) {
    this.betar = betar;
    setHash(null);
    return this;
  }

  /**
   * Density at receiver for MDAC processing.
   * <p>
   * Units: kg/(m^3)
   * 
   * @return rhor
   */
  public double getRhor() {
    return rhor;
  }

  /**
   * Density at receiver for MDAC processing.
   * <p>
   * Units: kg/(m^3)
   * 
   * @param rhor
   */
  public Mdac_fi setRhor(double rhor) {
    this.rhor = rhor;
    setHash(null);
    return this;
  }

  /**
   * P wave radiation pattern excitation factor for MDAC processing.
   * 
   * @return radpatp
   */
  public double getRadpatp() {
    return radpatp;
  }

  /**
   * P wave radiation pattern excitation factor for MDAC processing.
   * 
   * @param radpatp
   */
  public Mdac_fi setRadpatp(double radpatp) {
    this.radpatp = radpatp;
    setHash(null);
    return this;
  }

  /**
   * S wave radiation pattern excitation factor for MDAC processing.
   * 
   * @return radpats
   */
  public double getRadpats() {
    return radpats;
  }

  /**
   * S wave radiation pattern excitation factor for MDAC processing.
   * 
   * @param radpats
   */
  public Mdac_fi setRadpats(double radpats) {
    this.radpats = radpats;
    setHash(null);
    return this;
  }

  /**
   * Minimum signal-to-noise used to select spectral amplitudes for estimating MDAC parameters.
   * 
   * @return snr1
   */
  public double getSnr1() {
    return snr1;
  }

  /**
   * Minimum signal-to-noise used to select spectral amplitudes for estimating MDAC parameters.
   * 
   * @param snr1
   */
  public Mdac_fi setSnr1(double snr1) {
    this.snr1 = snr1;
    setHash(null);
    return this;
  }

  /**
   * Minimum signal-to-noise used to select spectral amplitudes for applying MDAC corrections.
   * 
   * @return snr2
   */
  public double getSnr2() {
    return snr2;
  }

  /**
   * Minimum signal-to-noise used to select spectral amplitudes for applying MDAC corrections.
   * 
   * @param snr2
   */
  public Mdac_fi setSnr2(double snr2) {
    this.snr2 = snr2;
    setHash(null);
    return this;
  }

  /**
   * Noise identifier e.g. pre-event, pre-phase for the MDAC correction.
   * 
   * @return noisetype
   */
  public String getNoisetype() {
    return noisetype;
  }

  /**
   * Noise identifier e.g. pre-event, pre-phase for the MDAC correction.
   * 
   * @param noisetype
   * @throws IllegalArgumentException if noisetype.length() >= 15
   */
  public Mdac_fi setNoisetype(String noisetype) {
    if (noisetype.length() > 15)
      throw new IllegalArgumentException(
          String.format("noisetype.length() cannot be > 15.  noisetype=%s", noisetype));
    this.noisetype = noisetype;
    setHash(null);
    return this;
  }

  /**
   * Unique magnitude identifier.
   * 
   * @return magid
   */
  public long getMagid() {
    return magid;
  }

  /**
   * Unique magnitude identifier.
   * 
   * @param magid
   * @throws IllegalArgumentException if magid >= 1000000000
   */
  public Mdac_fi setMagid(long magid) {
    if (magid >= 1000000000L)
      throw new IllegalArgumentException("magid=" + magid + " but cannot be >= 1000000000");
    this.magid = magid;
    setHash(null);
    return this;
  }

  /**
   * Magnitude type.
   * 
   * @return magtype
   */
  public String getMagtype() {
    return magtype;
  }

  /**
   * Magnitude type.
   * 
   * @param magtype
   * @throws IllegalArgumentException if magtype.length() >= 6
   */
  public Mdac_fi setMagtype(String magtype) {
    if (magtype.length() > 6)
      throw new IllegalArgumentException(
          String.format("magtype.length() cannot be > 6.  magtype=%s", magtype));
    this.magtype = magtype;
    setHash(null);
    return this;
  }

  /**
   * Algorithm identifier. Identifies algorithm(s) used to measure data.
   * 
   * @return algoid
   */
  public long getAlgoid() {
    return algoid;
  }

  /**
   * Algorithm identifier. Identifies algorithm(s) used to measure data.
   * 
   * @param algoid
   * @throws IllegalArgumentException if algoid >= 1000000000
   */
  public Mdac_fi setAlgoid(long algoid) {
    if (algoid >= 1000000000L)
      throw new IllegalArgumentException("algoid=" + algoid + " but cannot be >= 1000000000");
    this.algoid = algoid;
    setHash(null);
    return this;
  }

  /**
   * Author, the originator of the data or measurements. The format is varchar2(20), but that size
   * is only used in the Knowledge Base tables. All other tables (i.e. laboratory specific tables)
   * should use a size of varchar2(15). This is because "SOURCE:" is added to the auth name for the
   * KB.
   * 
   * @return auth
   */
  public String getAuth() {
    return auth;
  }

  /**
   * Author, the originator of the data or measurements. The format is varchar2(20), but that size
   * is only used in the Knowledge Base tables. All other tables (i.e. laboratory specific tables)
   * should use a size of varchar2(15). This is because "SOURCE:" is added to the auth name for the
   * KB.
   * 
   * @param auth
   * @throws IllegalArgumentException if auth.length() >= 20
   */
  public Mdac_fi setAuth(String auth) {
    if (auth.length() > 20)
      throw new IllegalArgumentException(
          String.format("auth.length() cannot be > 20.  auth=%s", auth));
    this.auth = auth;
    setHash(null);
    return this;
  }

  /**
   * Retrieve the name of the schema.
   * 
   * @return schema name
   */
  static public String getSchemaName() {
    return "NNSA KB Custom";
  }

}
