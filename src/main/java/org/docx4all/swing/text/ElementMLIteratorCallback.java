/*
 *  Copyright 2007, Plutext Pty Ltd.
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

package org.docx4all.swing.text;

import java.util.ArrayList;
import java.util.List;

import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.DefaultStyledDocument.ElementSpec;

import org.docx4all.ui.main.Constants;
import org.docx4all.xml.BodyML;
import org.docx4all.xml.ElementML;
import org.docx4all.xml.ElementMLIterator;
import org.docx4all.xml.ParagraphML;
import org.docx4all.xml.PropertiesContainerML;
import org.docx4all.xml.RunContentML;
import org.docx4all.xml.RunML;
import org.docx4all.xml.SdtBlockML;

public class ElementMLIteratorCallback extends ElementMLIterator.Callback {
	private List<ElementSpec> _elementSpecs = new ArrayList<ElementSpec>();
	private MutableAttributeSet _paragraphAttrs, _runAttrs;
	
	public void handleStartElement(ElementML elem) {
		//TODO: Find a better handler
		if (elem instanceof RunContentML) {
			openElementSpec((RunContentML) elem);
			
		} else if (elem instanceof RunML) {
			openElementSpec((RunML) elem);
			
		} else if (elem instanceof ParagraphML) {
			openElementSpec((ParagraphML) elem);
		
		} else if (elem instanceof SdtBlockML) {
			openElementSpec((SdtBlockML) elem);
			
		} else if (elem instanceof BodyML) {
			;//bypass
		
		} else {
			SimpleAttributeSet elemAttrs = new SimpleAttributeSet();
			WordMLStyleConstants.setElementML(elemAttrs, elem);
			openElementSpec(elemAttrs);
		}
	}
	
	public void handleEndElement(ElementML elem) {		
		//TODO: Find a better handler
		if (elem instanceof RunContentML) {
			closeElementSpec((RunContentML) elem); 
			
		} else if (elem instanceof RunML) {
			closeElementSpec((RunML) elem);
			
		} else if (elem instanceof ParagraphML) {
			closeElementSpec((ParagraphML) elem);
			
		} else if (elem instanceof SdtBlockML) {
			closeElementSpec((SdtBlockML) elem);
			
		} else if (elem instanceof BodyML) {
			;//bypass
			
		} else {
			closeElementSpec((AttributeSet) null);
			//It turns out that closing ElementSpec can be done 
			//without passing any AttributeSet.
			//If later on this causes problem then comment out
			//the above line and uncomment the following lines.
			//SimpleAttributeSet elemAttrs = new SimpleAttributeSet();
			//WordMLStyleConstants.setElementML(elemAttrs, elem);
			//closeElementSpec(elemAttrs);
		}
	}
	
	public List<ElementSpec> getElementSpecs() {
		return _elementSpecs;
	}
	
	private void openElementSpec(AttributeSet attrs) {
		ElementSpec es = new ElementSpec(attrs, ElementSpec.StartTagType);
		_elementSpecs.add(es);
	}
	
	private void addContentElementSpec(AttributeSet attrs, String text) {
		ElementSpec es = 
			new ElementSpec(
				attrs, 
				ElementSpec.ContentType, 
				text.toCharArray(), 
				0, 
				text.length());
		_elementSpecs.add(es);
	}
	
	private void closeElementSpec(AttributeSet attrs) {
		ElementSpec es = new ElementSpec(attrs, ElementSpec.EndTagType);
		_elementSpecs.add(es);
	}
	
	private void openElementSpec(ParagraphML paragraphML) {
		_paragraphAttrs = new SimpleAttributeSet();
		
		PropertiesContainerML pc = paragraphML.getParagraphProperties();
		if (pc != null) {
			AttributeSet pcAttrs = pc.getAttributeSet();
			_paragraphAttrs.addAttributes(pcAttrs);
			String pStyle = 
				(String) pcAttrs.getAttribute(WordMLStyleConstants.PStyleAttribute);
			if (pStyle != null) {
				StyleSheet styleSheet = paragraphML.getStyleSheet();
				if (styleSheet != null) {
					Style s = styleSheet.getIDStyle(pStyle);
					if (s != null) {
						((SimpleAttributeSet) _paragraphAttrs)
								.setResolveParent(s);
					}
				}
			}
		}
		
		WordMLStyleConstants.setElementML(_paragraphAttrs, paragraphML);
		openElementSpec(_paragraphAttrs);
		
		//Insert IMPLIED_PARAGRAPH inside every ParagraphML BLOCK
		SimpleAttributeSet elemAttrs = new SimpleAttributeSet();
		WordMLStyleConstants.setElementML(elemAttrs,
				ElementML.IMPLIED_PARAGRAPH);
		openElementSpec(elemAttrs);
	}
	
	private void closeElementSpec(ParagraphML paragraphML) {
		closeElementSpec(paragraphML, true);
	}
	
	private void closeElementSpec(ParagraphML paragraphML, boolean resetAttr) {
		//TODO: Check first whether this paragraph already
		//ends with a newline character.
		
		//End every paragraph with a IMPLIED_RUN and IMPLIED_NEWLINE
		RunML runML = ElementML.IMPLIED_RUN;
		SimpleAttributeSet runAttrs = new SimpleAttributeSet();
		WordMLStyleConstants.setElementML(runAttrs, runML);
		openElementSpec(runAttrs);
		
		RunContentML rcML = (RunContentML) ElementML.IMPLIED_NEWLINE;
		
		SimpleAttributeSet tempAttrs = new SimpleAttributeSet();
		WordMLStyleConstants.setElementML(tempAttrs, rcML);
		addContentElementSpec(tempAttrs, rcML.getTextContent());
		
		closeElementSpec((AttributeSet) null);
		//It turns out that closing ElementSpec can be done 
		//without passing any AttributeSet.
		//If later on this causes problem then comment out
		//the above line and uncomment the following lines.
		//closeElementSpec(runAttrs.copyAttributes());
		
		//Remember to close the inserted IMPLIED_PARAGRAPH
		closeElementSpec((AttributeSet) null);
		//tempAttrs = new SimpleAttributeSet();
		//WordMLStyleConstants.setElementML(tempAttrs, ElementML.IMPLIED_PARAGRAPH);
		//closeElementSpec(tempAttrs);
		
		//Close ParagraphML BLOCK
		closeElementSpec((AttributeSet) null);
		//tempAttrs = new SimpleAttributeSet();
		//WordMLStyleConstants.setElementML(tempAttrs, paragraphML);
		//closeElementSpec(tempAttrs);

		if (resetAttr) {
			_paragraphAttrs = null;
		}
	}
	
	private void openElementSpec(RunML runML) {
		_runAttrs = new SimpleAttributeSet();
		
		PropertiesContainerML pc = runML.getRunProperties();
		if (pc != null) {
			AttributeSet pcAttrs = pc.getAttributeSet();
			_runAttrs.addAttributes(pcAttrs);
			String rStyle = 
				(String) pcAttrs.getAttribute(WordMLStyleConstants.RStyleAttribute);
			if (rStyle != null) {
				StyleSheet styleSheet = runML.getStyleSheet();
				if (styleSheet != null) {
					Style s = styleSheet.getIDStyle(rStyle);
					if (s != null) {
						((SimpleAttributeSet) _runAttrs).setResolveParent(s);
					}
				}
			}
		}
		
		WordMLStyleConstants.setElementML(_runAttrs, runML);
		openElementSpec(_runAttrs);
	}

	private void closeElementSpec(RunML runML) {
		closeElementSpec(runML, true);
	}
	
	private void closeElementSpec(RunML runML, boolean resetAttr) {
		closeElementSpec((AttributeSet) null);
		//It turns out that closing ElementSpec can be done 
		//without passing any AttributeSet.
		//If later on this causes problem then comment out
		//the above line and uncomment the following lines.
		//SimpleAttributeSet elemAttrs = new SimpleAttributeSet();
		//WordMLStyleConstants.setElementML(elemAttrs, runML);
		//closeElementSpec(elemAttrs);

		if (resetAttr) {
			_runAttrs = null;
		}
	}
	
	private void openElementSpec(RunContentML runContentML) {
		SimpleAttributeSet elemAttrs = new SimpleAttributeSet();
		WordMLStyleConstants.setElementML(elemAttrs, runContentML);
		String text = runContentML.getTextContent();
		addContentElementSpec(elemAttrs, text);
	}
	
	private void closeElementSpec(RunContentML runContentML) {
		if (Constants.NEWLINE.equals(runContentML.getTextContent())) {
			//Close the current RUN block
			RunML runML = (RunML) WordMLStyleConstants.getElementML(_runAttrs);
			SimpleAttributeSet elemAttrs = new SimpleAttributeSet();
			WordMLStyleConstants.setElementML(elemAttrs, runML);
			closeElementSpec(runML, false);
			
			//Close the inserted IMPLIED_PARAGRAPH
			closeElementSpec((AttributeSet) null);
			//It turns out that closing ElementSpec can be done 
			//without passing any AttributeSet.
			//If later on this causes problem then comment out
			//the above line and uncomment the following lines.
			//elemAttrs = new SimpleAttributeSet();
			//WordMLStyleConstants.setElementML(elemAttrs, ElementML.IMPLIED_PARAGRAPH);
			//closeElementSpec(elemAttrs);

			//Open a new IMPLIED_PARAGRAPH
			openElementSpec(elemAttrs.copyAttributes());
			
			//Open a copy of RUN block
			openElementSpec(_runAttrs.copyAttributes());
		}
	}
	
	private void openElementSpec(SdtBlockML sdtBlockML) {
		SimpleAttributeSet elemAttrs = new SimpleAttributeSet();
		WordMLStyleConstants.setElementML(elemAttrs, sdtBlockML);
		if (sdtBlockML.isBorderVisible()) {
			WordMLStyleConstants.setBorderVisible(elemAttrs, true);
		}
		openElementSpec(elemAttrs);
	}
	
	private void closeElementSpec(SdtBlockML sdtBlockML) {
		closeElementSpec((AttributeSet) null);
	}
	
}// ElementMLIteratorCallback class






















