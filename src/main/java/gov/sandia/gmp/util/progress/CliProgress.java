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
package gov.sandia.gmp.util.progress;

import java.util.Properties;

/**
 * Command-line-interface implementation of a progress bar. Due to how Eclipse handles process
 * input/output, it will show each progress update on its own line, but it will show all progress
 * updates in the same, self-updating line when viewed from a "real" terminal.
 * 
 * @author Benjamin Lawry (bjlawry@sandia.gov) created on 09/02/2022
 */
public class CliProgress implements Progress {
  public static final String PROP_HIDE = "cliProgress.hide";
  private StringBuilder progress;
  private boolean hide;

  public CliProgress(Properties p) {
    init();
    hide = Boolean.valueOf(System.getProperty(PROP_HIDE)) || 
        (p != null && Boolean.valueOf(p.getProperty(PROP_HIDE)));
  }
  
  public CliProgress() { this(null); }

  @Override
  public void update(int done, int total, String msg) {
    if(hide) return;

    char[] workchars = {'|', '/', '-', '\\'};
    String format = "\r%3d%% %s %c";
    if (msg != null)
      format += " %s";

    int percent = (++done * 100) / total;
    
    synchronized(this.progress) {
      int extrachars = (percent / 2) - this.progress.length();

      while (extrachars-- > 0) {
        this.progress.append('#');
      }

      System.out.printf(format, percent, this.progress, workchars[done % workchars.length], msg);
    }

    if (done == total) {
      System.out.flush();
      System.out.println();
      init();
    }
  }

  private void init() {
    this.progress = new StringBuilder(60);
  }

  public static void main(String[] args) throws Exception {
    System.out.println("Running progress bar demo ...");
    String msg = null;
    if (args.length > 0)
      msg = args[0];
    Progress bar = new CliProgress();
    int t = 100;
    for (int i = 0; i < t; i++) {
      bar.update(i, t, msg);
      Thread.sleep(250);
    }
    System.out.println("done!");
  }
}
