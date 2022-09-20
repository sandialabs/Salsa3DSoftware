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
package gov.sandia.gmp.util.containers.linkedlist;

/**
 * Just like LinkedList except the reference storage (the next and previous
 * references) are defined directly in the element to be defined as a linked
 * list (E). The major difference with the Java LinkedList is one that two less
 * references per element E are required and the remove operation avoids a list
 * traversal to find the node to be removed before removing it (this
 * implementation is constant time). The E element must implement the
 * EmbeddedLinkedListInterface which defines the get and set methods for the
 * next and previous reference fields.
 * 
 * @author jrhipp
 *
 * @param <E>
 */
public class EmbeddedLinkedList<E extends EmbeddedLinkedListInterface<E>>
{
  /**
   * List element count.
   */
  private int              aSize = 0;
  
  /**
   * Head element of the list.
   */
  private E                aHead = null;

  /**
   * Tail element of the list.
   */
  private E                aTail = null;

  /**
   * Default constructor.
   */
  public EmbeddedLinkedList()
  {
    // no code
  }

  /**
   * Returns the number of elements in this list.
   * 
   * @return The number of elements in this list.
   */
  public int size()
  {
    return aSize;
  }

  /**
   * Returns the head element of the list. If the list is empty null is
   * returned.
   * 
   * @return The head element of the list.
   */
  public E getFirst()
  {
    return aHead;
  }

  /**
   * Returns the tail element of the list. If the list is empty null is
   * returned.
   * 
   * @return The tail element of the list.
   */
  public E getLast()
  {
    return aTail;
  }

  /**
   * Adds the input element to the front of this list.
   * 
   * @param elem The element to be added at the front of the this list.
   */
  public void addFirst(E elem)
  {
    // if head is null this is the first entry

    if (aHead == null)
    {
      // add first entry ... set head and tail to point at elem ...
      // nullify elem next reference

      aHead = aTail = elem;
      elem.setNextElement(null);
    }
    else
    {
      // add elem to head of list ... set the head elements previous
      // reference to elem and the elem next reference to the head element ...
      // then set the head element to elem

      aHead.setPrevElement(elem);
      elem.setNextElement(aHead);
      aHead = elem;
    }

    // always nullify the previous reference of elem since it was added to the
    // beginning of the list and increment the size

    elem.setPrevElement(null);
    ++aSize;
  }

  /**
   * Adds the input element to the end of this list.
   * 
   * @param elem The element to be added at the end of the this list.
   */
  public void addLast(E elem)
  {
    // if head is null this is the first entry

    if (aHead == null)
    {
      // add first entry ... set head tail to point at elem ...
      // nullify elem previous reference

      aHead = aTail = elem;
      elem.setPrevElement(null);
    }
    else
    {
      // add elem to tail of list ... set the tail elements next
      // reference to elem and the elem previous reference to the tail
      // element ... then set the tail element to elem

      aTail.setNextElement(elem);
      elem.setPrevElement(aTail);
      aTail = elem;
    }

    // always nullify the next reference of elem since it was added to the end
    // of the list

    elem.setNextElement(null);
    ++aSize;
  }

  /**
   * Removes the first element from the linked list. If the list is empty null
   * is returned. 
   */
  public E removeFirst()
  {
    // return null if empty ... else set return element to head

    if (aSize == 0) return null;
    E elem = aHead;

    // nullify everything if last element

    if (aSize == 1)
    {
      aHead = aTail = null;
      elem.setPrevElement(null);
    }
    else // (aSize > 1)
    {
      // assign head to head next element
      // assign new head previous reference to null
      // assign old head (elem) next reference to null
  
      aHead = aHead.getNextElement();
      aHead.setPrevElement(null);
    }

    // nullify next reference, decrement size and exit

    elem.setNextElement(null);
    --aSize;
    return elem;      
  }

  /**
   * Removes the last element from the linked list. If the list is empty null
   * is returned.
   */
  public E removeLast()
  {
    // return null if empty ... else set return element to tail

    if (aSize == 0) return null;
    E elem = aTail;

    // nullify everything if last element

    if (aSize == 1)
    {
      aHead = aTail = null;
      elem.setNextElement(null);
    }
    else // (aSize > 1)
    {
      // assign tail to tail previous element
      // assign new tail next reference to null
      // assign old tail (elem) previous reference to null
      
      aTail = aTail.getPrevElement();
      aTail.setNextElement(null);
    }

    // nullify previous reference, decrement size and exit

    elem.setPrevElement(null);
    --aSize;
    return elem;
  }

  /**
   * Removes the input element from the linked list. If the list is empty null
   * is returned.
   * 
   * @param elem The element to be removed from this list.
   */
  public E remove(E elem)
  {
    if (aSize == 0) return null;

    // see if the input task is at the head, tail, or somewhere in-between of
    // the input sentinels

    if (elem == aHead)
    {
      // elem is the head node of the list see if this is the last entry
      
      if (elem == aTail)
        aHead = aTail = null;
      else
      {
        // more than one entry left ... set the next elements previous reference
        // to null and set the head of the list to the next node.

        elem.getNextElement().setPrevElement(null);
        aHead = elem.getNextElement();
      }
    }
    else if (elem == aTail)
    {
      // elem is the tail of the list ... cannot be the head of the list
      // because of the first if check so there are more than one entry left
      // set the previous elements next reference to null and assign the tail to
      // the previous element

      elem.getPrevElement().setNextElement(null);
      aTail = elem.getPrevElement();
    }
    else
    {
      // elem is somewhere in-between the head and tail ... assign the previous
      // element next reference to the next element and the next elements
      // previous reference to the previous element

      elem.getPrevElement().setNextElement(elem.getNextElement());
      elem.getNextElement().setPrevElement(elem.getPrevElement());
    }

    // node has been removed ... nullify its previous and next references
    // and decrement the size

    elem.setNextElement(null);
    elem.setPrevElement(null);
    --aSize;
    return elem;
  }  
}
