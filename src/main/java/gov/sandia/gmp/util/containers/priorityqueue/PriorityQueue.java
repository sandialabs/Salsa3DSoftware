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
package gov.sandia.gmp.util.containers.priorityqueue;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * A different PriorityQueue implementation than that provided by standard
 * Java. The primary difference is the update function which allows a node
 * that is already contained in the queue, but whose priority index has
 * recently changed, to be updated in the queue to its new priority
 * location. This requires that the node maintain a priority index and a
 * position within the queue (a queue index). These indicies are provided
 * by implementing the PriorityQueueNode interface and defining the functions
 * getPriority(), setPriority(int p), getQueueIndex(), and
 * setQueueIndex(int qi). The queue also requires that the priority range be
 * known before hand so that two sentinel nodes can be used that are guaranteed
 * to have a priority that is smaller and larger than the smallest and largest
 * priority of the input node set. These are provided at construction along
 * with a best guess for the total capacity.
 * 
 * @author jrhipp
 *
 */
@SuppressWarnings("serial")
public class PriorityQueue implements Serializable
{
  // Vector of SPMNode* used to store the priority queue elements
  ArrayList<PriorityQueueNode> elements = null;

  /**
   * A local PriorityQueueNode implementation used to define the minimum and
   * maximum sentinel nodes.
   * 
   * @author jrhipp
   *
   */
  private class SentinelNode implements PriorityQueueNode
  {
    /**
     * The sentinel nodes priority index.
     */
    private int aPriority = 0;

    /**
     * The sentinel nodes queue index
     */
    private int aIndex    = 0;

    /**
     * Returns the nodes priority.
     */
    public int getPriority()
    {
      return aPriority;
    }

    /**
     * Sets the nodes priority to p.
     */
    public void setPriority(int p)
    {
      aPriority = p;      
    }

    /**
     * Returns the nodes queue index.
     */
    public int getQueueIndex()
    {
      return aIndex;
    }

    /**
     * Sets the nodes queue index.
     */
    public void setQueueIndex(int qi)
    {
      aIndex = qi;
    }
  }

  /**
   * Default minimum sentinel node.
   */
  SentinelNode minNode = new SentinelNode();


  /**
   * Default maximum sentinel node.
   */
  SentinelNode maxNode = new SentinelNode();;

  /**
   * Default constructor with initial capacity, min time, max time.
   * 
   * @param capacity Best guess at the priority queues total capacity.
   * @param minPriority Minimum Priority. Should be smaller than the smallest
   *                    priority in the input node set.
   * @param maxPriority Maximum Priority. Should be larger than the largest
   *                    priority in the input node set.
   */
  public PriorityQueue(int capacity, int minPriority, int maxPriority)
  {
     // setup priority queue
  
     setup(capacity, minPriority, maxPriority);
  }

  /**
   * Constructor that initializes the priority queue with the input
   * set of nodes.

   * @param nodes Input set of nodes to be added to the queue.
   * @param minPriority Minimum Priority. Should be smaller than the smallest
   *                    priority in the input node set.
   * @param maxPriority Maximum Priority. Should be larger than the largest
   *                    priority in the input node set.
   */
  public PriorityQueue(ArrayList<PriorityQueueNode> nodes,
                       int minPriority, int maxPriority)
  {
     // setup priority queue

     setup( nodes.size(), minPriority, maxPriority);

     // copy items (note that min node occupies index 0)

     for( int i = 0; i < nodes.size( ); i++ )
         elements.add(nodes.get(i));

     // build the heap by percolating down 
     // (for each of the nodes except the leaf nodes)

     for(int i = size()/2; i > 0; i--) percolateDown(i);
  }

  /**
   * Dumps out the priority queue for debugging.
   */
  public void dump() 
  {
     System.out.println("Priority Queue Contents");
     System.out.println("Format = Index i: Values Across Level");
     for(int i = 1; i <= size(); i*=2)
     {
       System.out.print("Index " + i + ": ");

       for(int j = i; (j < i*2) && (j <= size()); j++)
         System.out.print(elements.get(j).getPriority() + ", ");

       System.out.println("");
     }
  }

  /**
   * Get the number of elements in the queue, excluding
   * the default min node.
   */
  public int size()
  {
   // do not count the default min node in the returned
   // queue size
   return elements.size() - 1; 
  }

  /**
   * Get the minimum valid item in the priority queue.
   * Returns either a valid PriorityQueueNode, or NULL if the only
   * node in the queue is the default min node.
   */
  public PriorityQueueNode peek()
  {
     if(size() < 1) return null;
     return elements.get(1); // default min node occupies index 0
  }

  /**
   * Get the minimum valid item in the priority queue.
   * Does not check to see if list is empty or not (fast peek).
   */
  public PriorityQueueNode top()
  {
     return elements.get(1);
  }

  /**
   * Remove the next priority node in the queue.
   */
  public void pop()
  {
   if (size() > 0)
   {
     elements.set(1, elements.get(size()));
     elements.get(1).setQueueIndex(1);
     elements.remove(size());
     percolateDown(1);
   }
  }

  /**
   * Return the next priority node or null if none remain. This function
   * removes the priority node from the queue
   */
  public PriorityQueueNode poll()
  {
    if(size() < 1) return null;

    PriorityQueueNode node = elements.get(1);
    elements.set(1, elements.get(size()));
    elements.get(1).setQueueIndex(1);
    elements.remove(size());
    percolateDown(1);

    return node;
  }

  /**
   * Insert the given node into the priority queue,
   * allowing duplicate node priorities.
   * 
   * @param node The node to be inserted in the priority queue.
   */
  public void add(PriorityQueueNode node)
  {
    insert(node);
  }

  /**
   * Insert the given node into the priority queue,
   * allowing duplicate node priorities.
   * 
   * @param node The node to be inserted in the priority queue.
   */
  public void insert(PriorityQueueNode node)
  {
     // create a new hole in the vector by 
     // pushing this node onto the back of it

    elements.add(node);
     int hole = size();

     // Percolate up with new hole at end

     if (hole > 1) percolateUp(hole, node);
  }

  /**
   * Remove the input priority node.
   * 
   * @param node The node to be removed from the priority queue.
   */
  public void remove(PriorityQueueNode node)
  {
   if (size() > 0)
   {
     int hole = node.getQueueIndex();
     if (hole == size())
       elements.remove(size());
     else
     {
       elements.set(hole, elements.get(size()));
       elements.get(hole).setQueueIndex(hole);
       elements.remove(size());
       percolateDown(hole);
     }
   }
  }

  /**
   * Update the node's position within the priority queue. This function
   * is typically called for nodes already in the queue but whose priority
   * value has changed.
   * 
   * @param node The node to be updated in the priority queue.
   */
  public void update(PriorityQueueNode node)
  {
     // hole starts at original index

     int hole = node.getQueueIndex();        
  
     // Percolate up or down depending on whether the 
     // new node time is greater or less than the original
     // time (based on comparing with its parent)

     if(hole > 1 && node.getPriority() < elements.get(hole/2).getPriority())
     {
         // new time is less than the parent's time,
         // so percolate up with the updated node

         percolateUp(hole, node);
     }
     else
     {
         // try to percolate down (this will move the
         // updated node at the hole position)

         percolateDown(hole);
     }
  }

  /**
   * Returns the ith node of the priority queue. This is not
   * the ith node in priority order.
   * 
   * @param i The index of the ith node in the queue
   */
  public PriorityQueueNode getNode(int i)
  {
   return elements.get(i+1);
  }

  /**
   * Returns true if the queue is empty
   */
  public boolean empty()
  {
    return (elements.size() == 1);
  }

  /**
   * Setup the priority queue based on the desired capacity, 
   * minimum time for the min sentinel node, and maximum time
   * for the max sentinel node.
   * 
   * @param capacity Best guess at the priority queues total capacity.
   * @param minPriority Minimum Priority. Should be smaller than the smallest
   *                    priority in the input node set.
   * @param maxPriority Maximum Priority. Should be larger than the largest
   *                    priority in the input node set.
   */
  private void setup(int capacity, int minPriority, int maxPriority)
  {
     // initialize min/max node times

     minNode.setPriority(minPriority);
     maxNode.setPriority(maxPriority);

     // reserve capacity of vector but increase
     // by 2 to account for min/max nodes

     elements = new ArrayList<PriorityQueueNode>(capacity+2);
     elements.clear();
     elements.ensureCapacity(capacity+2);

     // add min node to heap at element 0

     elements.add(minNode);
     minNode.setQueueIndex(0);
  }

  /**
   * Percolate the hole up and insert the node into
   * the appropriate location.
   * 
   * @param hole The queue index to be percolated up into its
   *             appropriate location as determined by the input nodes
   *             priority.
   * @param node The input node that will be inserted into the new hole
   *             location.    
   */  
  private void percolateUp(int hole, PriorityQueueNode node)
  {
     int p = node.getPriority();

     // iterate through the tree and swap parent node with
     // hole node until the new node time is no longer
     // less than the parent's time

     for( int h2 = hole/2; p < elements.get(h2).getPriority(); hole >>= 1, h2 = hole/2)
     {
         elements.set(hole, elements.get(h2));
         elements.get(hole).setQueueIndex(hole);
     }

     // assign new node position and save index into node

     elements.set(hole, node);
     node.setQueueIndex(hole);
  }

  /**
   * Percolate down from the given starting hole position.
   * 
   * @param hole The hole, or queue index, of a node that will be percolated
   *             down in the queue to a new location specified by the
   *             associated nodes priority.
   */ 
  private void percolateDown(int hole)
  {
     // Warning: This algorithm uses a default max sentinel
     // node ONLY if there are an even number of nodes in the
     // queue (which means there is one node that has a left
     // child but no right child).  This is done to avoid having
     // to test that the right child exists.  
  
     boolean addedSentinel = false;
     if(size() % 2 == 0)
     {
         // add sentinel as the last right child
         addedSentinel = true;
         elements.add(maxNode);
     }
  
     int child = hole;
     PriorityQueueNode node = elements.get(hole); // node to move from hole position
     int p = node.getPriority();
     int queueSize = size();
  
     // Loop through and check each of its children.
     // Find the lesser of the left child and the
     // right child, and swap the hole node with the 
     // smaller child node.

     for(int h2 = 2*hole; h2 <= queueSize; hole = child, h2 = 2*hole )
     {
         child = h2;
         if((child != queueSize) && 
            (elements.get(child+1).getPriority() <
             elements.get(child).getPriority())) child++;
         
         if(elements.get(child).getPriority() < p)
         {
             elements.set(hole, elements.get(child));
             elements.get(hole).setQueueIndex(hole);
         }
         else
             break;
     }
  
     // assign new node position and save index into node

     elements.set(hole, node);
     node.setQueueIndex(hole);
  
     // if added a max sentinel, remove it at the end

     if(addedSentinel) elements.remove(elements.size() - 1);
  }
}
