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
package gov.sandia.gmp.util.containers.tree;

import java.io.Serializable;
import java.util.NoSuchElementException;

/**
 * The base class used by all intrinsic Tree sets and maps. Most of the
 * tree-traversal mechanics are defined here. The key and value operations
 * are defined in all derived classes.
 * 
 * @author jrhipp
 *
 */
@SuppressWarnings("serial")
public abstract class TreeIntrinsic implements Serializable
{
  /**
   * 
   */
  private static final long serialVersionUID = -2110742758085779780L;

  /**
   * The tree root node.
   */
  protected Entry      root  = null;

  /**
   * The number of entries in the tree
   */
  protected int        size  = 0;

  // Red-black mechanics

  protected static final boolean RED   = false;
  protected static final boolean BLACK = true;

  /**
   * Node in the Tree.
   */
  protected static class Entry implements Serializable
  {
    /**
     * The left node entry in the red-black tree.
     */
    public Entry   left  = null;

    /**
     * The right node entry in the red-black tree.
     */
    public Entry   right = null;

    /**
     * The parent node entry in the red-black tree.
     */
    public Entry   parent;

    /**
     * The red-black flag setting for this node.
     */
    public boolean color = BLACK;

    /**
     * Make a new cell with the input parent, and with <tt>null</tt> child
     * links, and BLACK color.
     */
    protected Entry(Entry parent)
    {
      this.parent = parent;
    }
  }

  /**
   * Removes all of the mappings from this map. The map will be empty after this
   * call returns.
   */
  public void clear()
  {
    size = 0;
    root = null;
  }

  /**
   * performs a node re-balance after removing the input node.
   * 
   * @param p
   *          The input node to be removed.
   */
  protected void deleteEntryLocal(Entry p)
  {
    // Start fixup at replacement node, if it exists.

    Entry replacement = (p.left != null ? p.left : p.right);

    if (replacement != null)
    {
      // Link replacement to parent

      replacement.parent = p.parent;
      if (p.parent == null)
        root = replacement;
      else if (p == p.parent.left)
        p.parent.left = replacement;
      else
        p.parent.right = replacement;

      // Null out links so they are OK to use by fixAfterDeletion.

      p.left = p.right = p.parent = null;

      // Fix replacement

      if (p.color == BLACK) fixAfterDeletion(replacement);
    }
    else if (p.parent == null)
    {
      // return if we are the only node.

      root = null;
    }
    else
    {
      // No children. Use self as phantom replacement and unlink.

      if (p.color == BLACK) fixAfterDeletion(p);

      if (p.parent != null)
      {
        if (p == p.parent.left)
          p.parent.left = null;
        else if (p == p.parent.right) p.parent.right = null;
        p.parent = null;
      }
    }
  }

  /**
   * Returns the first Entry in the TreeMap. Returns null if the TreeMap is
   * empty.
   */
  protected final Entry getFirstEntry()
  {
    Entry p = root;
    if (p != null) while (p.left != null)
      p = p.left;
    return p;
  }

  /**
   * Returns the last Entry in the TreeMap. Returns null if the TreeMap is
   * empty.
   */
  protected final Entry getLastEntry()
  {
    Entry p = root;
    if (p != null) while (p.right != null)
      p = p.right;
    return p;
  }

  /**
   * Returns the successor of the specified Entry, or null if no such entry
   * exists.
   */
  protected static Entry successor(Entry t)
  {
    if (t == null)
      return null;
    else if (t.right != null)
    {
      Entry p = t.right;
      while (p.left != null)
        p = p.left;
      return p;
    }
    else
    {
      Entry p = t.parent;
      Entry ch = t;
      while (p != null && ch == p.right)
      {
        ch = p;
        p = p.parent;
      }
      return p;
    }
  }

  /**
   * Returns the predecessor of the specified Entry, or null if no such.
   */
  protected static Entry predecessor(Entry t)
  {
    if (t == null)
      return null;
    else if (t.left != null)
    {
      Entry p = t.left;
      while (p.right != null)
        p = p.right;
      return p;
    }
    else
    {
      Entry p = t.parent;
      Entry ch = t;
      while (p != null && ch == p.left)
      {
        ch = p;
        p = p.parent;
      }
      return p;
    }
  }

  /**
   * Balancing operations.
   * 
   * Implementations of re-balancings during insertion and deletion are slightly
   * different than the CLR version. Rather than using dummy nilnodes, we use a
   * set of accessors that deal properly with null. They are used to avoid
   * messiness surrounding nullness checks in the main algorithms.
   */

  private static boolean colorOf(Entry p)
  {
    return (p == null ? BLACK : p.color);
  }

  private static Entry parentOf(Entry p)
  {
    return (p == null ? null : p.parent);
  }

  private static void setColor(Entry p, boolean c)
  {
    if (p != null) p.color = c;
  }

  private static Entry leftOf(Entry p)
  {
    return (p == null) ? null : p.left;
  }

  private static Entry rightOf(Entry p)
  {
    return (p == null) ? null : p.right;
  }

  /** From CLR */
  private void rotateLeft(Entry p)
  {
    if (p != null)
    {
      Entry r = p.right;
      p.right = r.left;
      if (r.left != null) r.left.parent = p;

      r.parent = p.parent;
      if (p.parent == null)
        root = r;
      else if (p.parent.left == p)
        p.parent.left = r;
      else
        p.parent.right = r;

      r.left = p;
      p.parent = r;
    }
  }

  /** From CLR */
  private void rotateRight(Entry p)
  {
    if (p != null)
    {
      Entry l = p.left;
      p.left = l.right;
      if (l.right != null) l.right.parent = p;

      l.parent = p.parent;
      if (p.parent == null)
        root = l;
      else if (p.parent.right == p)
        p.parent.right = l;
      else
        p.parent.left = l;

      l.right = p;
      p.parent = l;
    }
  }

  /** From CLR */
  protected void fixAfterInsertion(Entry x)
  {
    x.color = RED;

    while (x != null && x != root && x.parent.color == RED)
    {
      if (parentOf(x) == leftOf(parentOf(parentOf(x))))
      {
        Entry y = rightOf(parentOf(parentOf(x)));
        if (colorOf(y) == RED)
        {
          setColor(parentOf(x), BLACK);
          setColor(y, BLACK);
          setColor(parentOf(parentOf(x)), RED);
          x = parentOf(parentOf(x));
        }
        else
        {
          if (x == rightOf(parentOf(x)))
          {
            x = parentOf(x);
            rotateLeft(x);
          }
          setColor(parentOf(x), BLACK);
          setColor(parentOf(parentOf(x)), RED);
          rotateRight(parentOf(parentOf(x)));
        }
      }
      else
      {
        Entry y = leftOf(parentOf(parentOf(x)));
        if (colorOf(y) == RED)
        {
          setColor(parentOf(x), BLACK);
          setColor(y, BLACK);
          setColor(parentOf(parentOf(x)), RED);
          x = parentOf(parentOf(x));
        }
        else
        {
          if (x == leftOf(parentOf(x)))
          {
            x = parentOf(x);
            rotateRight(x);
          }
          setColor(parentOf(x), BLACK);
          setColor(parentOf(parentOf(x)), RED);
          rotateLeft(parentOf(parentOf(x)));
        }
      }
    }
    root.color = BLACK;
  }

  /** From CLR */
  protected void fixAfterDeletion(Entry x)
  {
    while (x != root && colorOf(x) == BLACK)
    {
      if (x == leftOf(parentOf(x)))
      {
        Entry sib = rightOf(parentOf(x));

        if (colorOf(sib) == RED)
        {
          setColor(sib, BLACK);
          setColor(parentOf(x), RED);
          rotateLeft(parentOf(x));
          sib = rightOf(parentOf(x));
        }

        if (colorOf(leftOf(sib)) == BLACK && colorOf(rightOf(sib)) == BLACK)
        {
          setColor(sib, RED);
          x = parentOf(x);
        }
        else
        {
          if (colorOf(rightOf(sib)) == BLACK)
          {
            setColor(leftOf(sib), BLACK);
            setColor(sib, RED);
            rotateRight(sib);
            sib = rightOf(parentOf(x));
          }
          setColor(sib, colorOf(parentOf(x)));
          setColor(parentOf(x), BLACK);
          setColor(rightOf(sib), BLACK);
          rotateLeft(parentOf(x));
          x = root;
        }
      }
      else
      {
        // symmetric
        Entry sib = leftOf(parentOf(x));

        if (colorOf(sib) == RED)
        {
          setColor(sib, BLACK);
          setColor(parentOf(x), RED);
          rotateRight(parentOf(x));
          sib = leftOf(parentOf(x));
        }

        if (colorOf(rightOf(sib)) == BLACK && colorOf(leftOf(sib)) == BLACK)
        {
          setColor(sib, RED);
          x = parentOf(x);
        }
        else
        {
          if (colorOf(leftOf(sib)) == BLACK)
          {
            setColor(rightOf(sib), BLACK);
            setColor(sib, RED);
            rotateLeft(sib);
            sib = leftOf(parentOf(x));
          }
          setColor(sib, colorOf(parentOf(x)));
          setColor(parentOf(x), BLACK);
          setColor(leftOf(sib), BLACK);
          rotateRight(parentOf(x));
          x = root;
        }
      }
    }

    setColor(x, BLACK);
  }

  /**
   * Returns the right-most parent.
   * 
   * @param p
   *          The input node from which the search is begun.
   * @return The right-most parent.
   */
  protected Entry findParentRight(Entry p)
  {
    Entry parent = p.parent;
    Entry ch = p;
    while (parent != null && ch == parent.right)
    {
      ch = parent;
      parent = parent.parent;
    }
    return parent;
  }

  /**
   * Returns the left-most parent.
   * 
   * @param p
   *          The input node from which the search is begun.
   * @return The left-most parent.
   */
  protected Entry findParentLeft(Entry p)
  {
    Entry parent = p.parent;
    Entry ch = p;
    while (parent != null && ch == parent.left)
    {
      ch = parent;
      parent = parent.parent;
    }
    return parent;
  }

  /**
   * Base class for TreeMap Iterators
   */
  protected abstract class PrivateEntryIterator
  {
    /**
     * Next entry to return.
     */
    Entry next;

    /**
     * Last entry to return.
     */
    Entry lastReturned;

    /**
     * Protected default constructor.
     */
    protected PrivateEntryIterator()
    {
      lastReturned = null;
      next = getFirstEntry();
    }

    /**
     * Protected standard constructor that sets the starting iterator at input
     * entry first.
     * 
     * @param first
     *          The starting iterator node.
     */
    protected PrivateEntryIterator(Entry first)
    {
      lastReturned = null;
      next = first;
    }

    /**
     * Returns true if the next iterator position is not null.
     * 
     * @return true if the next iterator position is not null.
     */
    public final boolean hasNext()
    {
      return next != null;
    }

    /**
     * Gets the next entry.
     * 
     * @return The next entry.
     */
    public Entry nextEntry()
    {
      Entry e = next;
      if (e == null) throw new NoSuchElementException();
      next = successor(e);
      lastReturned = e;
      return e;
    }

    /**
     * Gets the previous entry.
     * 
     * @return The previous entry.
     */
    public Entry prevEntry()
    {
      Entry e = next;
      if (e == null) throw new NoSuchElementException();
      next = predecessor(e);
      lastReturned = e;
      return e;
    }

    /**
     * Sets up to remove the current entry. This function is called by derived
     * classes to actually perform the remove.
     * 
     * @return The entry to be removed which is passed to a derived class to
     *         perform the remove.
     */
    public Entry removeBase()
    {
      if (lastReturned == null) throw new IllegalStateException();

      // deleted entries are replaced by their successors

      if (lastReturned.left != null && lastReturned.right != null)
        next = lastReturned;
      Entry savLastReturned = lastReturned;
      lastReturned = null;
      return savLastReturned;
    }
  }

  /**
   * Returns an estimate of the bulk memory size used by this object. The
   * input pointer size (ptrsize) should be 8 for 64-bit and 4 for 32-bit.
   * 
   * @param ptrsize The pointer size set to 8 for 64-bit and 4 for 32-bit.
   * @return The bulk memory estimate in bytes.
   */
  public long memoryEstimate(int ptrsize)
  {
    return (long) size * (3 * ptrsize + 1);
  }

  /**
   * Return number of entries in this map/set.
   * 
   * @return number of entries.
   */
  public int size()
  {
    return size;
  }
}
