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
package gov.sandia.gmp.seismicbasedata;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;

import gov.sandia.gmp.util.globals.Utils;
import gov.sandia.gmp.util.io.GlobalInputStreamProvider;


/**
 * This class manages the contents of seismicBaseData directory. It overrides the
 * exists() method and implements a method getInputStream(). If an instance is created with a File
 * path that starts with 'seismic-base-data.jar' then exists() and getInputStream() return values
 * based on lookup tables stored in the jar file. If the path does not start with
 * 'seismic-base-data.jar' then exists() and getInputStream() return the same values that File would
 * return.
 * 
 * @author sballar
 *
 */
public class SeismicBaseData implements Serializable{
  private static final long serialVersionUID = 1L;

  /**
   * Set containing the names of all the files in SeismicBaseData. If the set of files in
   * SeismicBase data is ever modified, then this set must be updated to reflect those changes.
   * See static method getResourcesString().
   */
  public final static Set<String> resources = new HashSet<>(Arrays.asList(new String[] { "tt_iasp91_PKKPab",
	  "tt_iasp91_sPKPab", "tt_ak135_PKP2df", "tt_iasp91_SnSn", "tt_ak135_SKKPab", "tt_ak135_pPKPab",
	  "el_ak135_pPKPbc", "el_ak135_SKKPbc", "tt_iasp91_bigP_bigS", "tt_iasp91_PKKPbc", "tt_iasp91_sPKPbc",
	  "tt_ak135_SKKPbc", "tt_ak135_pPKPbc", "el_ak135_bigS_bigP", "el_ak135_PKP2df", "el_ak135_pPKPab",
	  "el_ak135_SKKPab", "el_ak135_SKKPdf", "el_ak135_pPKPdf", "tt_ak135_S", "tt_iasp91_Sg", "tt_iasp91_Sn",
	  "el_ak135_Sdiff", "tt_ak135_pPKPdf", "tt_ak135_SKKPdf", "el_ak135_SSS", "tt_iasp91_sPKPdf",
	  "tt_iasp91_PKKPdf", "tt_ak135_SKS2df", "tt_iasp91_PKKS", "tt_ak135_ScP", "el_ak135_PcP", "tt_ak135_nP",
	  "el_ak135_SKS2df", "el_ak135_SKKP", "tt_ak135_SKS2", "tt_iasp91_Pg", "tt_iasp91_pPKiKP", "el_ak135_PKPPKP",
	  "tt_ak135_sPKiKP", "el_ak135_Pdiff", "tt_ak135_PKPPKP", "el_ak135_sPKiKP", "tt_iasp91_ScP", "tt_iasp91_sPKP",
	  "el_ak135_littles_bigS", "tt_iasp91_Pn", "tt_iasp91_littles_bigS", "tt_ak135_Pn", "tt_ak135_PKPbc",
	  "tt_ak135_bigS_bigS", "el_ak135_SKS2", "tt_ak135_PKPab", "tt_ak135_Pg", "tt_ak135_PKPdf", "tt_ak135_SKKP",
	  "el_ak135_SKSdf", "el_ak135_S", "el_ak135_PKSbc", "tt_iasp91_PKS", "tt_ak135_Sn", "tt_ak135_SKPbc",
	  "el_ak135_SKSac", "tt_ak135_littlep_bigS", "el_ak135_PKSab", "tt_ak135_Sg", "tt_ak135_SKPab",
	  "tt_ak135_SKPdf", "el_ak135_PKSdf", "tt_ak135_PKS", "el_ak135_SKS", "tt_iasp91_SKSSKS", "tt_iasp91_pSKS",
	  "el_ak135_SKKSdf", "tt_ak135_PKKS", "tt_iasp91_PKKSdf", "tt_ak135_SSS", "tt_ak135_SKKSdf", "tt_ak135_Pdiff",
	  "el_ak135_pSKSac", "tt_iasp91_PKKSab", "tt_iasp91_SKS2", "tt_iasp91_S", "el_ak135_Pb", "tt_iasp91_PKiKP",
	  "tt_ak135_sPKP", "tt_iasp91_PKKSbc", "tt_ak135_littles_bigP", "tt_ak135_pSKSac", "tt_iasp91_sSKSac",
	  "tt_iasp91_SSS", "el_ak135_pSKS", "tt_ak135_SnSn", "el_ak135_pSKSdf", "tt_iasp91_PcP", "tt_iasp91_sSKSdf",
	  "tt_ak135_pSKSdf", "tt_ak135_Sdiff", "el_ak135_SKKSac", "tt_ak135_bigP_bigP", "el_ak135_Sb",
	  "tt_iasp91_SKiKP", "tt_iasp91_Lg", "el_ak135_ScP", "tt_ak135_PcP", "tt_ak135_SKKSac", "tt_iasp91_sPdiff",
	  "tt_ak135_PKSdf", "tt_ak135_pPdiff", "el_ak135_SKPdf", "el_ak135_littlep_bigP", "tt_iasp91_littlep_bigP",
	  "tt_ak135_Lg", "el_ak135_pPdiff", "el_ak135_SKPbc", "tt_ak135_SKSac", "tt_ak135_PKSbc", "el_ak135_SKPab",
	  "tt_ak135_PKSab", "tt_ak135_pSKS", "el_ak135_SnSn", "tt_ak135_SKSdf", "el_ak135_sPKP", "el_ak135_PKS",
	  "tt_ak135_SKS", "el_ak135_PKPdf", "el_ak135_PKPbc", "tt_iasp91_SKKP", "tt_iasp91_SKS", "el_ak135_PKKS",
	  "el_ak135_PKPab", "tt_iasp91_bigS_bigP", "el_ak135_bigP_bigS", "tt_ak135_nNL", "tt_ak135_P", "el_ak135_SKiKP",
	  "tt_iasp91_Pb", "tt_iasp91_bigP_bigP", "tt_ak135_sSKS", "el_ak135_bigS_bigS", "tt_iasp91_pPdiff",
	  "tt_ak135_sPdiff", "el_ak135_pPKP", "el_ak135_littles_bigP", "tt_iasp91_Sb", "tt_iasp91_littles_bigP",
	  "el_ak135_PKiKP", "tt_ak135_PPP", "el_ak135_sPdiff", "tt_iasp91_ScS", "el_ak135_Lg", "tt_iasp91_PKKP",
	  "el_ak135_PnPn", "el_ak135_SKKS", "tt_iasp91_PPP", "el_ak135_PcS", "tt_ak135_ScS", "tt_iasp91_PKPab",
	  "el_ak135_sSKSdf", "tt_ak135_SKKS", "tt_ak135_PnPn", "el_ak135_P", "tt_iasp91_PKPbc", "tt_iasp91_pSKSdf",
	  "tt_ak135_sSKSdf", "tt_ak135_bigS_bigP", "tt_ak135_Sb", "tt_ak135_pPKP", "tt_iasp91_PKPdf",
	  "tt_iasp91_SKKSac", "el_ak135_SKP", "tt_iasp91_SKPab", "tt_ak135_PKP", "el_ak135_PKKSdf", "el_ak135_sSKS",
	  "tt_iasp91_SKPbc", "tt_iasp91_SKKSdf", "tt_ak135_PKKSdf", "el_ak135_sSKSac", "tt_iasp91_PKP",
	  "tt_ak135_PKKSab", "tt_ak135_Pb", "el_ak135_PKKSbc", "tt_iasp91_PKP2", "tt_ak135_PKKSbc", "tt_iasp91_SKPdf",
	  "tt_ak135_sSKSac", "el_ak135_PKKSab", "tt_ak135_littlep_bigP", "tt_iasp91_pSKSac", "el_ak135_Sn",
	  "tt_iasp91_Pdiff", "tt_iasp91_P", "tt_ak135_littles_bigS", "el_ak135_Sg", "tt_ak135_PKKP", "el_ak135_SKSSKS",
	  "el_ak135_nNL", "tt_ak135_PKiKP", "tt_ak135_SKSSKS", "tt_iasp91_Sdiff", "el_ak135_Pn", "tt_ak135_bigP_bigS",
	  "el_ak135_Pg", "el_ak135_PKP2", "tt_ak135_PcS", "el_ak135_ScS", "tt_ak135_SKiKP", "tt_iasp91_sSKS",
	  "tt_iasp91_PcS", "el_ak135_PPP", "tt_iasp91_SKS2df", "el_ak135_nP", "tt_iasp91_PKSdf", "tt_iasp91_PKSab",
	  "tt_iasp91_sPKiKP", "tt_ak135_PKP2", "tt_ak135_pPKiKP", "tt_iasp91_PKSbc", "tt_iasp91_PKPPKP",
	  "el_ak135_pPKiKP", "tt_iasp91_SKSac", "el_ak135_littlep_bigS", "tt_iasp91_littlep_bigS", "tt_iasp91_SKKPab",
	  "tt_iasp91_pPKPab", "tt_ak135_PKKPab", "tt_ak135_sPKPab", "tt_iasp91_SKP", "tt_iasp91_PKP2df",
	  "tt_iasp91_SKKS", "tt_iasp91_PnPn", "el_ak135_sPKPbc", "el_ak135_PKKPbc", "tt_iasp91_SKKPbc",
	  "tt_iasp91_pPKPbc", "tt_iasp91_bigS_bigS", "el_ak135_bigP_bigP", "tt_ak135_PKKPbc", "tt_ak135_sPKPbc",
	  "el_ak135_PKKP", "el_ak135_sPKPab", "el_ak135_PKKPab", "tt_iasp91_SKSdf", "el_ak135_PKKPdf",
	  "el_ak135_sPKPdf", "tt_ak135_SKP", "el_ak135_PKP", "tt_iasp91_pPKP", "tt_ak135_sPKPdf", "tt_ak135_PKKPdf",
	  "tt_iasp91_pPKPdf", "tt_iasp91_SKKPdf" }));
  
  public String getResourcesString() throws Exception {
      File dir = new File("/Users/$USER/git/seismic-base-data/src/main/resources");
      if (!GlobalInputStreamProvider.forFiles().isDirectory(dir))
        throw new Exception(dir+" does not exist");
      StringBuffer buf = new StringBuffer();
      for (File f : GlobalInputStreamProvider.forFiles().listFiles(dir))
	  if (f.getName().startsWith("tt_") || f.getName().startsWith("el_ak135_"))
	      buf.append(String.format(", \"%s\"", f.getName()));
      return "public final static Set<String> resources = new HashSet<>(Arrays.asList(new String[] {"
      +buf.toString().substring(2)+"}));\n\n";
  }

  /**
   * The following phases have to have filenames that do not correspond directly with the phase name
   * because the filenames would collide on systems (Windows) where filenames are not case
   * sensitive. E.g., phases PP and pP would collide.
   */
  public final static Map<String, String> phaseToFileName = new HashMap<>();
  static {
    phaseToFileName.put("PP", "bigP_bigP");
    phaseToFileName.put("PS", "bigP_bigS");
    phaseToFileName.put("SP", "bigS_bigP");
    phaseToFileName.put("SS", "bigS_bigS");
    phaseToFileName.put("pP", "littlep_bigP");
    phaseToFileName.put("pS", "littlep_bigS");
    phaseToFileName.put("sP", "littles_bigP");
    phaseToFileName.put("sS", "littles_bigS");
  }

  /**
   * The following phases have to have filenames that do not correspond directly with the phase name
   * because the filenames would collide on systems (Windows) where filenames are not case
   * sensitive. E.g., phases PP and pP would collide.
   */
  public final static Map<String, String> fileNameToPhase = new HashMap<>();
  static {
    fileNameToPhase.put("bigP_bigP", "PP");
    fileNameToPhase.put("bigP_bigS", "PS");
    fileNameToPhase.put("bigS_bigP", "SP");
    fileNameToPhase.put("bigS_bigS", "SS");
    fileNameToPhase.put("littlep_bigP", "pP");
    fileNameToPhase.put("littlep_bigS", "pS");
    fileNameToPhase.put("littles_bigP", "sP");
    fileNameToPhase.put("littles_bigS", "sS");
  }

  /**
   * Retrieve a component of a filename for a specified phase. For most phases, this simply returns
   * the specified phase, but for phases where the phase name will cause a collision on Windows
   * operating systems (e.g., PP and pP), this will return something else (bigP_bigP, littlep_bigP).
   * 
   * @param phase
   * @return
   */
  public static String getFileName(String phase) {
    String s = phaseToFileName.get(phase);
    return s == null ? phase : s;
  }

  /**
   * For a specified component of a filename (e.g., bigP_bigP) return the corresponding phase name
   * (PP). For most phases, this simply returns the specified filename, but for phases where the
   * phase name will cause a collision on Windows operating systems (e.g., bigP_bigP and
   * littlep_bigP), this will return the appropriate phase (PP or pP).
   * 
   * @param phase
   * @return
   */
  public static String getPhase(String fileName) {
    String s = fileNameToPhase.get(fileName);
    return s == null ? fileName : s;
  }
  
  /**
   * Performance optimization for Fabric tasks that use SeismicBaseData instances to load phase
   * data. Phase File contents are stored in this map the first time they are read using calls to
   * Class.getResourceAsStream(). Subsequent calls to getInputStream() wrap the stored bytes in
   * ByteArrayInputStream, avoiding unnecessary calls to Fabric's network class/resource loader.
   * 
   * @author bjlawry
   */
  protected static final Map<File, byte[]> CACHE = new TreeMap<File, byte[]>(
      Comparator.comparing(File::toString));
  
  private File file;

  /**
   * File copy constructor. If the specified file has a path that starts with
   * 'seismic-base-data.jar' then methods exists() and getInputStream() will return values based on
   * the resources stored in the jar file. Otherwise they will return values based on stuff out on
   * the file system.
   * 
   * @param file
   */
  public SeismicBaseData(File file) {
    this.file = file;
  }
  
  public SeismicBaseData() { }

  public File getFile() { return file; }
  
  public File getAlternate() {
    return new File(file.getPath().replaceFirst("seismic-base-data.jar(/?)", "")); 
  }
  
  public File getIde() {
    try {
      return new File("src/main/resources",getResourceName());
    } catch (FileNotFoundException e) {
      return null;
    }
  }
  
  /**
   * If this File's path starts with 'seismic-base-data.jar' then this method returns true if the
   * requested resource exists in this jar file. Otherwise it returns true if the file exists out on
   * the file system.
   * 
   * @return If this File's path starts with 'seismic-base-data.jar' then this method returns true
   *         if the requested resource exists in this jar file. Otherwise it returns true if the
   *         file exists out on the file system.
   */
  public boolean exists() {
    boolean exists = false;
    
    /*if (file.getPath().startsWith("seismic-base-data.jar")) {
      try {
        return Utils.getResourceAsStream(getResourceName()) != null;
        // exists = resources.contains(getResourceName());
      } catch (Exception e) {
        exists = false;
      }
    } else {
      try {
        exists = fisp.exists(file);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    if (!exists) {
      try {
        exists = Utils.getResourceAsStream(getResourceName()) != null || fisp.exists(file);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }*/
    
    //0: we may have checked this already, consult the cache first:
    synchronized(CACHE) {
      if(CACHE.containsKey(file)) return CACHE.get(file) != null;
    }
    
    //1: check the class path first:
    String resourceName = null;
    try (InputStream is = Utils.getResourceAsStream(resourceName = getResourceName())) {
      if (is != null) {
        is.close();
        return true;
      }
    } catch (IOException e) {}
    
    try(InputStream is = Utils.getResourceAsStream(file.getName())){
      if(is != null) {
        is.close();
        return true;
      }
    } catch (IOException e) {}
    
    //2: check the file itself:
    if(file.exists()) return true;
    
    //3: maybe it was prefixed with "seismic-base-data.jar"; get rid of that and try again:
    File alternate = getAlternate();
    if(alternate.exists()) return true;
    
    //4: see if the client application has the original file on its local filesystem:
    try {
      if(GlobalInputStreamProvider.forFiles().exists(file)) return true;
    } catch (IOException e) {}
    
    //5: see if the client application has the alternate:
    try {
      if(GlobalInputStreamProvider.forFiles().exists(alternate)) return true;
    } catch (IOException e) {}
    
    
    
    //7: finally, check to see if we're running in an IDE:
    if(resourceName != null) {
      File ide = getIde();
      if (ide != null) {
        try {
          if (GlobalInputStreamProvider.forFiles().exists(ide))
            return true;
        } catch (IOException e) {}
      }
    }

    if (!exists)
      synchronized (CACHE) {
        CACHE.put(this.file, null);
      }

    return exists;
  }

  /**
   * If this File's path starts with 'seismic-base-data.jar' then this method returns an InputStream
   * backed by the resource in this jar file. Otherwise it returns a FileInputStream backed by the
   * file on the file system.
   * 
   * @return an InputStream
   * @throws FileNotFoundException if this.file could not be found
   * @throws IOException may be thrown during the file read
   */
  public InputStream getInputStream() throws FileNotFoundException, IOException {
    return getInputStream(null);
  }

  /**
   * Reads this File and caches it, if CACHE doesn't already contain the contents of the File.
   * 
   * @param alternateSource if non-null, the alternate will be used to create the InputStream if
   * the file could not be found on the local file system
   * @param loadCallback called whenever an actual file read occurs (not merely when contents are
   *        retrieved from CACHE)
   * @return an input stream to the data contained within this file
   * @throws FileNotFoundException if this file doesn't exist
   */
  protected InputStream getInputStream(Consumer<File> loadCallback)
      throws FileNotFoundException, IOException {
    //boolean cb = false;
    byte[] bytes = null;

    synchronized (CACHE) {
      // Check the cache first:
      if (CACHE.containsKey(this.file)) {
        bytes = CACHE.get(this.file);
        if (bytes == null)
          return null;
        return new ByteArrayInputStream(bytes);
      }
      
      //Checks both local classpath and Fabric Client classpath (if applicable):
      String resourceName = getResourceName();
      InputStream s = Utils.getResourceAsStream(resourceName);
      
      if(s == null) {
        //Checks both the local file system and Fabric Client file system (if applicable):
        s = GlobalInputStreamProvider.forFiles().newStream(file);
      }
      
      //Check the alternate file name:
      if(s == null) {
        File alternate = new File(file.getPath().replace("seismic-base-data.jar(/?)", ""));
        s = GlobalInputStreamProvider.forFiles().newStream(alternate);
      }
      
      if(s == null) {
        File ide = new File("src/main/java",resourceName);
        s = GlobalInputStreamProvider.forFiles().newStream(ide);
      }
      
      //It really doesn't exist anywhere:
      if(s == null) {
        throw new FileNotFoundException("could not find \""+file+
            "\" on local file system or client!!");
      }

      // Cache didn't have it, try to load it:
      /*InputStream s;
      try {
        if (file.getPath().startsWith("seismic-base-data.jar")) {
          s = Utils.getResourceAsStream(getResourceName());
          if (s == null) {
            // The jar didn't have it either, cache it as null and throw an exception:
            CACHE.put(this.file, null);
            throw new FileNotFoundException(
                "Resource " + getResourceName() + " does not exist in seismic-base-data.jar");
          }
        } else
          s = new FileInputStream(this.file);
      } catch (FileNotFoundException e) {
        //if all else fails, try to load it from the alternate source:
        if(alternateSource != null) {
          s = alternateSource.newStream(this.file);
        }
        else throw e;
      }*/

      // Resource exists, read it and cache the data:
      try (ByteArrayOutputStream baos = new ByteArrayOutputStream(s.available());
          OutputStream out = new BufferedOutputStream(baos)) {

        byte[] buf = new byte[16384];
        int read = -1;
        while ((read = s.read(buf)) != -1)
          out.write(buf, 0, read);
        out.flush();
        bytes = baos.toByteArray();
        CACHE.put(this.file, bytes);
        //cb = true;
      } catch (IOException e) {
        // We couldn't read the file (possible EOF exception), warn user and cache a null value:
        e.printStackTrace();
        CACHE.put(this.file, null);
      }
      
      if(loadCallback != null) loadCallback.accept(file);
    }

    if (bytes != null)
      return new ByteArrayInputStream(bytes);
    return null;
  }

  public String getResourceName() throws FileNotFoundException {
    try {
      return String.format("%s_%s_%s", file.getParentFile().getParentFile().getName(),
          file.getParentFile().getName(), file.getName());
    } catch (Exception e) {
      throw new FileNotFoundException(
          file.getPath() + " does not have a corresponding resource in seismic-base-data.jar");
    }
  }

  static public String getVersion() {
    return Utils.getVersion("seismic-base-data");
  }

  /**
   * This mail program will load a bunch of files from a seismicBaseData directory on a file system
   * and convert them into files with new names compatible with storing in a resource directory so
   * that the files can be incorporated into a jar.
   * 
   * @param args
   */
  public static void main(String[] args) {
    System.out.println(new SeismicBaseData(new File("tt_ak135_P")).exists());
    
    System.out.println("SeismicBaseData " + getVersion());
    // try {
    // // NOTE: you may NOT run this on a PC because the filenames like pP and PP will get mangles!
    // File inDir = new File("/nfs/old_computer/devlpool/sballar/SNL_Tool_Root//seismicBaseData");
    // File outDir = new File("/nfs/old_computer/devlpool/sballar/GMP_testing/seismicBaseData");
    //
    // for (String type : new String[] {"tt", "el"})
    // for (String model : new String[] {"ak135", "iasp91"})
    // {
    // File id = new File(new File(inDir, type), model);
    // if (id.exists() && id.isDirectory())
    // {
    // for (File f : id.listFiles())
    // if (f.isFile())
    // {
    // Scanner in = new Scanner(f);
    // BufferedWriter out = new BufferedWriter(new FileWriter(new File(outDir,
    // String.format("%s_%s_%s", type, model, f.getName()))));
    // while (in.hasNext())
    // {
    // out.write(in.nextLine());
    // out.newLine();
    // }
    // in.close();
    // out.close();
    // }
    // }
    // }
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
  }
}
