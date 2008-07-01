/*
 *  Copyright 2008, Plutext Pty Ltd.
 *   
 *  This file is part of Docx4all.

    Docx4all is free software: you can redistribute it and/or modify
    it under the terms of version 3 of the GNU General Public License 
    as published by the Free Software Foundation.

    Docx4all is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License   
    along with Docx4all.  If not, see <http://www.gnu.org/licenses/>.
    
 */

package org.plutext.client;

import org.apache.log4j.Logger;
import org.docx4all.swing.text.DocumentElement;
import org.docx4all.swing.text.WordMLDocument;
//import org.docx4all.xml.DocumentML;
//import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
//import org.docx4j.wml.Id;
import org.docx4j.wml.SdtBlock;
import org.plutext.client.state.StateChunk;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;


/* Represent the document at a point in time.
 * 
 * The representation is made up of two things:
 * 1. the pkg xml document (ie VSTO's .WordOpenXML)
 * 2. a Hashmap of StateChunks
 * 
 * We need to be able to:
 * (i)  infer a skeleton from it
 * (ii) create a new Document from it  
 * 
 * In the Word addin, apply a transform is a matter
 * of manipulating the raw XML in this object (because
 * operating at a higher level is too clunky).
 * 
 * But in our Java world, it probably makes more
 * sense to operate at this higher level.
 * 
 * In other words, rather than using WordprocessingMLPackage.exportPkgXml()
 * to create a org.docx4j.wml.Package, this
 * class should operate on the JAXB representation of the 
 * MainDocumentPart. 
 *  
 * 
 * 
 */
public class Pkg implements Cloneable
{

	private static Logger log = Logger.getLogger(Pkg.class);
	
//	WordprocessingMLPackage wordMLPackage;
//	public WordprocessingMLPackage getWordMLPackage() {
//		return wordMLPackage;
//	}
	

    public Pkg(WordMLDocument doc)  
    {
  		DocumentElement root = (DocumentElement) doc.getDefaultRootElement();    	
//  		wordMLPackage = 
//    		((DocumentML) root.getElementML()).getWordprocessingMLPackage();
    	
//    	this.wordMLPackage = wordMLPackage;
    	
        initChunks(doc);
    }



    /* create an inferredSkeleton by transforming document.xml
    //    (this will also contain:            	
    //    (i)  new content controls for anything not in a cc            	
    //    (ii) flattening of any content controls which user has managed to nest via a paste */
    public Skeleton getInferedSkeleton()
    {
    	// TODO
    	
    	return null;
    	
    }

    HashMap<String, StateChunk> stateChunks = null;
    public HashMap<String, StateChunk> getStateChunks()
    {
        return stateChunks;
    }

    public void initChunks(WordMLDocument doc)
    {
    	
		Map<BigInteger, SdtBlock> map = doc.getSnapshots(0, doc.getLength());
		if (map != null) {
			this.stateChunks = 
				new HashMap<String, StateChunk>(map.size());
			
			for (SdtBlock sdt:map.values()) {
				StateChunk sc = new StateChunk(sdt);
				this.stateChunks.put(sc.getId(), sc);
			}
		} else {
			this.stateChunks = new HashMap<String, StateChunk>();
		}

    }

    public Object Clone()
    {
        // TODO
    	return null;
    }


//    HashMap<String, StateChunk> cloneStateChunks() 
//    {
//
//    	HashMap<String, StateChunk> dictNew = 
//            new HashMap<String, StateChunk>();
//
//        for (KeyValuePair<string, StateChunk> kvp : stateChunks)
//        {
//            dictNew.Add(kvp.Key, (StateChunk)kvp.Value.Clone());
//        }
//
//        return dictNew;
//    }




}
