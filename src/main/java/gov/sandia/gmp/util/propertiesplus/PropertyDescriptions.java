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
package gov.sandia.gmp.util.propertiesplus;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
//import gov.sandia.gmp.tomography.GeoTomography;
import java.util.stream.Stream;

/**
 * Used in conjunction with @Property to generate descriptions of statically-defined property names
 * in key GMP applications such as gov.sandia.gmp.tomography.GeoTomography. To use the generate()
 * function, simply pass it the class that defines static property strings annotated with @Property
 * and a table of names, types, and descriptions will be returned in human-readable form. These
 * statically-defined strings contain the name of the property itself and the @Property annotation
 * is used to describe their intended type conversion and a description of what they do.
 * 
 * @author Benjamin Lawry (bjlawry@sandia.gov)
 * created on 03/08/2022
 */
public class PropertyDescriptions {
  private static StringBuilder append(StringBuilder s, int chars, char val) {
    for(int i = 0; i < chars; i++) s = s.append(val);
    return s;
  }
  
  /**
   * @param title optional table title
   * @param annotated at least one class with annotated @Property names (public static strings)
   * @return human-readable table of property names, types, and descriptions, sorted by name
   */
  public static String generate(String title, Class<?> ... annotatedClasses) {
    if(title == null) title = annotatedClasses[0].getSimpleName()+" Properties";
    
    List<String[]> ntd = new ArrayList<>();
    
    String nh = "Name:";
    String th = "Type:";
    String dh = "Description:";
    
    int[] maxNameLen = new int[]{nh.length()};
    int[] maxTypeLen = new int[]{th.length()};
    int[] maxDescLen = new int[]{dh.length()};
    
    for(Class<?> annotated : annotatedClasses) {
      List.of(annotated.getFields()).stream()
      .filter(f -> f.getDeclaredAnnotation(Property.class) != null)
      .filter(f -> {
        int m = f.getModifiers();
        return f.getType() == String.class && Modifier.isPublic(m) && Modifier.isStatic(m);
      })
      .forEach(f -> {
        Property p = f.getDeclaredAnnotation(Property.class);
        try {
          String name = f.get(null)+"";
          String type = p.type().getSimpleName();
          String desc = p.desc();

          ntd.add(new String[] {name,type,desc});

          maxNameLen[0] = Math.max(maxNameLen[0], name.length());
          maxTypeLen[0] = Math.max(maxTypeLen[0], type.length());
          maxDescLen[0] = Math.max(maxDescLen[0], desc.length());
        } catch (IllegalAccessException e) {
          e.printStackTrace();
        }
      });
    }
    
    //Sort the table by property name:
    Collections.sort(ntd,Comparator.comparing(sa -> sa[0]));
    
    //Build header and divider:
    StringBuilder sb = new StringBuilder(3+title.length()+
        2*(maxNameLen[0]+maxTypeLen[0]+maxDescLen[0]+3));
    sb.append(title).append("\n\n");
    
    append(sb.append(nh),3+(maxNameLen[0]-nh.length()),' ');
    append(sb.append(th),3+(maxTypeLen[0]-th.length()),' ');
    append(sb.append(dh),3+(maxDescLen[0]-dh.length()),' ').append("\n");
    append(sb,maxNameLen[0]+maxTypeLen[0]+maxDescLen[0]+8,'-').append("\n");
    
    //Build table entries:
    for(int i = 0; i < ntd.size(); i++) {
      append(sb.append(ntd.get(i)[0]),3+(maxNameLen[0]-ntd.get(i)[0].length()),' ');
      append(sb.append(ntd.get(i)[1]),3+(maxTypeLen[0]-ntd.get(i)[1].length()),' ');
      append(sb.append(ntd.get(i)[2]),3+(maxDescLen[0]-ntd.get(i)[2].length()),' ');
      
      if(i < ntd.size()-1) sb.append("\n");
    }
    
    return sb.toString();
  }
  
  /**
   * Convenience method, equivalent to calling generate(annotated,null)
   * @param annotated annotated class
   * @return human-readable table of property names, types, and descriptions, sorted by name
   */
  public static String generate(Class<?> ... annotated) {
    return generate(null, annotated);
  }
  
  public static Stream<String> propertyNames(Class<?> ... classes){
    return List.of(classes).stream()
      .<Field>flatMap(annotated -> List.of(annotated.getFields())
          .stream()
          .filter(f -> {
            int m = f.getModifiers();
            return f.getType() == String.class && Modifier.isPublic(m) && Modifier.isStatic(m);
          }))
      .map(f -> {
        try {
          return f.get(null)+"";
        } catch (IllegalArgumentException | IllegalAccessException e) {
          e.printStackTrace();
          return null;
        }
      })
      .filter(Objects::nonNull)
      .sorted();
  }
  
  //public static void main(String[] args) {
  //  System.out.println(generate(GeoTomography.class));
  //}
}
