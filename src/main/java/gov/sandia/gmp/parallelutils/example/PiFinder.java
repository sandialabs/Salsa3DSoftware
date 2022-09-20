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
package gov.sandia.gmp.parallelutils.example;

import gov.sandia.gmp.parallelutils.ParallelBroker;
import gov.sandia.gmp.parallelutils.ParallelBroker.ParallelMode;
import gov.sandia.gmp.parallelutils.ParallelResult;
import gov.sandia.gmp.parallelutils.ParallelTask;

import java.util.LinkedList;

/**
 * @author bjlawry
 * <p>PiFinder is an example of the Monte Carlo method being used to derive the
 * value of Pi, using NRM.  A square with a circle inscribed inside is randomly
 * sampled by each task one million times.  The numbers of samples inside and
 * out are tracked and returned to the client and are then used to estimate pi.
 */
public class PiFinder {
//1: Split your job up into tasks and run them: -------------------------------
	public static void main(String[] args){
		long numTasks = 5L;
		long insideTotal = 0;
		long outsideTotal = 0;
		ParallelMode mode = ParallelMode.DISTRIBUTED_FABRIC;
		//Options for "mode" include "sequential", "concurrent", "distributed"
		ParallelBroker pb = ParallelBroker.create(mode);
		
		/* Optional: Set the preferred operating systems for this run 
		 * (separate OS names with commas - spaces are optional).
		 * 
		 * In the line below, only hosts that have Windows 2003 and any version
		 * of linux will be allowed to start nodes.  Setting this to null
		 * (which is the default value) allows nodes to run on any operating
		 * system.
		 */
		//pb.setPreferredOperatingSystems("windows 2003, linux");
		pb.setTaskTimeout(60);
		System.out.println("Estimating pi in "+mode+" mode...");
		long startTime = System.currentTimeMillis();
		
		//Here's where the task list gets created:
		LinkedList<ParallelTask> tasks = new LinkedList<ParallelTask>();
		for(long i = 0; i < numTasks; i++) tasks.addLast(new PiFinderTask());
		
		//Here's where tasks are submitted:
		pb.submit(tasks);
		
		//Here's how results are retrieved:
		PiFinderResult pfr = null;
		int resultsRetrieved = 0;
		while((pfr = (PiFinderResult)pb.getResultWait()) != null){
			insideTotal += pfr.getInside();
			outsideTotal += pfr.getOutside();
			resultsRetrieved++;
			if(resultsRetrieved == numTasks)
				break;
		}
		
		//Calculate pi using the results obtained from the run:
		double pi = 4*insideTotal/(double)(insideTotal+outsideTotal);
		
		System.out.println("Estimated pi at "+pi+" in "+
				(System.currentTimeMillis()-startTime)/1000.0+" seconds using"+
						" "+resultsRetrieved+" tasks.");
		pb.close();
		System.exit(0);
	}
	
//2. Create task implementation with a run method: ----------------------------
	public static class PiFinderTask extends ParallelTask{
		private static final long serialVersionUID = 1L;
		private long trials;
		private long inside;
		private long outside;
		
		public PiFinderTask(){
			trials = 10000000;
			inside = 0;
			outside = 0;
		}

		@Override
		public void run() {
			System.out.println("HERE");
			for(long t = 0; t < trials; t++){
				double x = Math.random();
				double y = Math.random();
				
				if(Math.sqrt(Math.pow(x-.5,2)+Math.pow(y-.5,2))<=.5)
					inside++;
				else outside++;
			}
			
//3. Set your result when computation is finished: ----------------------------
			setResult(new PiFinderResult(inside,outside));
		}
	}
	
//4. Implement your result class: ---------------------------------------------
	public static class PiFinderResult extends ParallelResult{
		private static final long serialVersionUID = 1L;
		private long inside;
		private long outside;
		
		public PiFinderResult(long i, long o){
			inside = i;
			outside = o;
		}
		
		public long getInside(){ return inside; }
		
		public long getOutside(){ return outside; }
	}
}
