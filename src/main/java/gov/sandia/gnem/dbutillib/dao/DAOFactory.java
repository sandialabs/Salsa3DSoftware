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
package gov.sandia.gnem.dbutillib.dao;

import gov.sandia.gnem.dbutillib.ParInfo;
import gov.sandia.gnem.dbutillib.Schema;
import gov.sandia.gnem.dbutillib.util.DBDefines;

/**
 * This class is an abstract class for creating DAO objects. The DAOType
 * parameter within the ParInfo object passed to the {@link #create create()}
 * method determines which type of DAO object to create.
 * <P>The currently supported DAO objects and their corresponding DAOTypes are
 * DAODatabase (DB) and DAOFlatFile (FF or XML). See
 * {@link DBDefines DBDefines.java} for constants that can be used to specify
 * DAO type.
 */
public abstract class DAOFactory {
    /* *********************************************************************
     * Sandy, why does the create method take a schema? DAO's shouldn't need
     * to know about their schemas since that sort of violates the whole
     * separation issue.
     **********************************************************************/

    /**
     * Creates a DAO object based on the DAOType parameter in the configInfo
     * object. The currently supported DAO objects and their corresponding
     * DAOTypes are DAODatabase (DB) and DAOFlatFile (FF or XML). See
     * {@link DBDefines DBDefines.java} for constants that can be used to specify
     * DAO type.
     *
     * @param schema
     * @param configInfo ParInfo object that must contain the DAOType paramter
     * @param name       prefix to be used when searching for paramters within the
     *                   configInfo object. For example, if the configInfo object needs to have two
     *                   DAOType parameters, it would be wise to give them two different names,
     *                   say SourceDAOType and TargetDAOType. So, if the create method needs to
     *                   create the Source DAO, name would = "Source"
     * @return instantiated DAO object of type DAODatabase, DAOXML, or
     * DAOFlatFile
     * @throws DBDefines.FatalDBUtilLibException if a DAO creation error occurs
     */
    public static DAO create(Schema schema, ParInfo configInfo, String name)
            throws DBDefines.FatalDBUtilLibException {
        DAO dao = null;

        // Get the dao type.
        String daoType = configInfo.getItem(name + "DAOType").toUpperCase();

        // Create a DAODatabase object.
        if (daoType.equals(DBDefines.DATABASE_DAO))
            dao = new DAODatabase(schema, configInfo, name);

            // Create a DAOFlatFile object.
        else if (daoType.equals(DBDefines.FF_DAO) || daoType.equals(DBDefines.XML_DAO))
            dao = new DAOFlatFile(schema, configInfo, name);

            // Create a DAOPool object
        else if (daoType.equals(DBDefines.POOL_DAO))
            dao = new DAOPool(schema, configInfo, name);

            // Invalid DAO type.
        else
            throw new DBDefines.FatalDBUtilLibException(
                    "FATAL ERROR in DAOFactory.create(configInfo, name=" + name + ").  "
                            + daoType + " is not a valid DataAccessObject type."
                            + " Valid types are: DB, FF and XML");

        return dao;
    }
}
