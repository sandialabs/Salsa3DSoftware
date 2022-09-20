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
package gov.sandia.gmp.util.changenotifier;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * This notifier keeps a list of ChangeListeners, and notifies them when its
 * fireStateChanged method is called.
 *
 * This implements the Observer design pattern. ChangeNotifier is the
 * ConcreteObservable class, ChangeListener is the Observer interface, and the
 * Objects saved in list are the ConcreteObservers.
 *
 * @author Randall W. Simons
 * @version $Revision: 1.2 $, $Date: 2009/07/21 17:03:56 $
 */
public class ChangeNotifier
{
  // ~ Instance fields
  // ********************************************************

  /** Event that will tell listeners where a notification came from. */
  protected ChangeEvent event = null;

  /** List of listeners desiring notification of changes. */
  protected List<ChangeListener> list = new ArrayList<ChangeListener> ();

  // ~ Constructors
  // ***********************************************************

  /**
   * Create a new ChangeNotifier object.
   *
   * @param source the object that will be included as the source in the
   *            ChangeEvent sent to all listeners.
   */
  public ChangeNotifier(Object source)
  {
    event = new ChangeEvent(source);
  }

  // ~ Methods
  // ****************************************************************

  public void setSource(Object source)
  {
    event = new ChangeEvent(source);
  }

  /**
   * Register a listener to be notified of changes.
   *
   * @param ls the listener to add
   */
  public void addListener(ChangeListener ls)
  {
    list.add(ls);
  }

  /**
   * Cease notifying a listener of changes.
   *
   * @param ls the listener to stop notifying
   */
  public void removeListener(ChangeListener ls)
  {
    list.remove(ls);
  }

  public void removeAll()
  {
    list.clear();
  }

  /**
   * Notify all listeners that are registered for notification.
   */
  public void fireStateChanged()
  {
    for (ChangeListener listnr : list)
    {
      listnr.stateChanged(event);
    }
  }
}
