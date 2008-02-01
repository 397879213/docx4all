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

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.CaretEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.TextAction;
import javax.swing.text.Utilities;
import javax.swing.text.DefaultStyledDocument.ElementSpec;

import org.apache.log4j.Logger;
import org.docx4all.swing.WordMLTextPane;
import org.docx4all.ui.main.WordMLEditor;
import org.docx4all.util.DocUtil;
import org.docx4all.xml.DocumentML;
import org.docx4all.xml.ElementML;
import org.docx4all.xml.ElementMLFactory;
import org.docx4all.xml.ParagraphML;
import org.docx4all.xml.ParagraphPropertiesML;
import org.jdesktop.application.ResourceMap;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class WordMLEditorKit extends StyledEditorKit {
	private static Logger log = Logger.getLogger(WordMLEditorKit.class);

    /**
     * Name of the action to place a soft line break into
     * the document.  If there is a selection, it is removed before
     * the break is added.
     * @see #getActions
     */
    public static final String insertSoftBreakAction = "insert-soft-break";

    public static final String enterKeyTypedAction = "enter-key-typed-action";
    
    public static final String fontBoldAction = "font-bold";
    
    public static final String fontItalicAction = "font-italic";
    
    public static final String fontUnderlineAction = "font-underline";

	private static final Cursor MoveCursor = Cursor
			.getPredefinedCursor(Cursor.HAND_CURSOR);
	private static final Cursor DefaultCursor = Cursor
			.getPredefinedCursor(Cursor.DEFAULT_CURSOR);

	private static final javax.swing.text.ViewFactory defaultFactory = new ViewFactory();

	// TODO: Later implementation
	private static final Action[] defaultActions = {};

	private Cursor defaultCursor = DefaultCursor;
	private CaretListener caretListener;
	
	/**
	 * Constructs an WordMLEditorKit, creates a StyleContext, and loads the
	 * style sheet.
	 */
	public WordMLEditorKit() {
		super();
		caretListener = new CaretListener();
	}

	public void saveCaretText() {
		DocUtil.saveTextContentToElementML(caretListener.caretElement);
	}
	
	/**
	 * Get the MIME type of the data that this kit represents support for. This
	 * kit supports the type <code>text/xml</code>.
	 * 
	 * @return the type
	 */
	@Override
	public String getContentType() {
		return "application/xml";
	}

	/**
	 * Fetch a factory that is suitable for producing views of any models that
	 * are produced by this kit.
	 * 
	 * @return the factory
	 */
	@Override
	public javax.swing.text.ViewFactory getViewFactory() {
		return defaultFactory;
	}

	/**
	 * Create an uninitialized text storage model that is appropriate for this
	 * type of editor.
	 * 
	 * @return the model
	 */
	@Override
	public Document createDefaultDocument() {
		Document doc = new WordMLDocument();
		if (log.isDebugEnabled()) {
			log.debug("createDefaultDocument():");
			DocUtil.displayStructure(doc);
		}
		return doc;
	}

	@Override
	public void read(Reader in, Document doc, int pos) 
		throws IOException,	BadLocationException {
		throw new NotImplementedException();
	}

    /**
     * Creates a WordMLDocument from the given .docx file
     * 
     * @param f  The file to read from
     * @exception IOException on any I/O error
     */
	public WordMLDocument read(File f) throws IOException {
		DocumentML docML = ElementMLFactory.createDocumentML(f);
		List<ElementSpec> specs = DocUtil.getElementSpecs(docML);
		
		WordMLDocument doc = (WordMLDocument) createDefaultDocument();
		doc.createElementStructure(specs);
		
		if (log.isDebugEnabled()) {
			log.debug("read(): File = " + f.getAbsolutePath());
			DocUtil.displayStructure(specs);
			DocUtil.displayStructure(doc);
		}
			
		return doc;
	}

	/**
	 * Write content from a document to the given stream in a format appropriate
	 * for this kind of content handler.
	 * 
	 * @param out
	 *            the stream to write to
	 * @param doc
	 *            the source for the write
	 * @param pos
	 *            the location in the document to fetch the content
	 * @param len
	 *            the amount to write out
	 * @exception IOException
	 *                on any I/O error
	 * @exception BadLocationException
	 *                if pos represents an invalid location within the document
	 */
	@Override
	public void write(Writer out, Document doc, int pos, int len)
			throws IOException, BadLocationException {

		if (doc instanceof WordMLDocument) {
			// TODO: Later implementation
		} else {
			super.write(out, doc, pos, len);
		}
	}

	/**
	 * Called when the kit is being installed into the a JEditorPane.
	 * 
	 * @param c
	 *            the JEditorPane
	 */
	@Override
	public void install(JEditorPane c) {
		super.install(c);

		c.addCaretListener(caretListener);

		initKeyBindings(c);
		
	}

	/**
	 * Called when the kit is being removed from the JEditorPane. This is used
	 * to unregister any listeners that were attached.
	 * 
	 * @param c
	 *            the JEditorPane
	 */
	public void deinstall(JEditorPane c) {
		super.deinstall(c);
		c.removeCaretListener(caretListener);
	}

	/**
	 * Fetches the command list for the editor. This is the list of commands
	 * supported by the superclass augmented by the collection of commands
	 * defined locally for style operations.
	 * 
	 * @return the command list
	 */
	@Override
	public Action[] getActions() {
		return TextAction.augmentList(super.getActions(), WordMLEditorKit.defaultActions);
	}

	public void setDefaultCursor(Cursor cursor) {
		defaultCursor = cursor;
	}

	public Cursor getDefaultCursor() {
		return defaultCursor;
	}

    protected void createInputAttributes(Element element,
					 MutableAttributeSet set) {
    	;//we do not need the work of super class' AttributeTracker
    }

	private DocumentElement getCaretElement() {
		return (DocumentElement) caretListener.caretElement;
	}
	
	private void initKeyBindings(JEditorPane editor) {
		ActionMap myActionMap = new ActionMap();
		InputMap myInputMap = new InputMap();
		
		KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_MASK);
		myActionMap.put(insertSoftBreakAction, new InsertSoftBreakAction(insertSoftBreakAction));
		myInputMap.put(ks, insertSoftBreakAction);

		ks = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		myActionMap.put(enterKeyTypedAction, new EnterKeyTypedAction(enterKeyTypedAction));
		myInputMap.put(ks, enterKeyTypedAction);
		
		ks = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
		myActionMap.put(deleteNextCharAction, new DeleteNextCharAction(deleteNextCharAction));
		myInputMap.put(ks, deleteNextCharAction);

		ks = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0);
		myActionMap.put(deletePrevCharAction, new DeletePrevCharAction(deletePrevCharAction));
		myInputMap.put(ks, deletePrevCharAction);

		myActionMap.setParent(editor.getActionMap());
		myInputMap.setParent(editor.getInputMap());
		editor.setActionMap(myActionMap);
		editor.setInputMap(JComponent.WHEN_FOCUSED, myInputMap);
	}
	
	//**************************
	//*****  INNER CLASSES *****
	//**************************
	private class CaretListener implements javax.swing.event.CaretListener {
		private WordMLDocument.TextElement caretElement;
		
	    public void caretUpdate(CaretEvent evt) {
	    	//clean input attributes
			MutableAttributeSet attrs = getInputAttributes();
			attrs.removeAttributes(attrs);		
			
	    	JEditorPane editor = (JEditorPane) evt.getSource();
	    	WordMLDocument doc = (WordMLDocument) editor.getDocument();
	    	int dot = evt.getDot();
	    	
	    	//Update caret element
	    	Element elem = doc.getParagraphMLElement(dot, true);
	    	if (elem.getStartOffset() == dot) {
	    		elem = doc.getCharacterElement(dot);
	    	} else {
	    		elem = doc.getCharacterElement(Math.max(dot - 1, 0));
	    	}
	    	
    		if (caretElement != elem) {
    			DocUtil.saveTextContentToElementML(caretElement);
    		}
    		
    		caretElement = (WordMLDocument.TextElement) elem;
	    	
    		//Validate selected area if any
	    	int mark = evt.getMark();
	    	if (dot != mark) {
	    		try {
	    			int start = Math.min(dot, mark);
	    			int end = Math.max(dot, mark);
	    			new TextSelector(doc, start, end-start);
	    		} catch (BadSelectionException exc) {
	    			UIManager.getLookAndFeel().provideErrorFeedback(editor);
	    			editor.moveCaretPosition(mark);
	    		}
	    	} 
	    }
	    
	}// CaretListener inner class
	
	public abstract static class StyledTextAction extends javax.swing.text.StyledEditorKit.StyledTextAction {
        public StyledTextAction(String nm) {
    	    super(nm);
    	}
        
        protected final void setRunMLAttributes(final WordMLTextPane editor,
				AttributeSet attrs, boolean replace) {
        	
			Runnable caretPosRunnable = createCaretPositionRunnable(editor);
			
			WordMLDocument doc = (WordMLDocument) editor.getDocument();
			
			int p0 = editor.getSelectionStart();
			int p1 = editor.getSelectionEnd();
			if (p0 == p1) {
				try {
					int wordStart = Utilities.getWordStart(editor, p0);
					int wordEnd = Utilities.getWordEnd(editor, p1);
					if (wordStart < p0 && p0 < wordEnd) {
						p0 = wordStart;
						p1 = wordEnd;
					}
				} catch (BadLocationException exc) {
					;//ignore
				}
			}
			
			StyledEditorKit kit = getStyledEditorKit(editor);
			MutableAttributeSet inputAttributes = kit.getInputAttributes();
			if (p0 != p1) {
				try {
					doc.setRunMLAttributes(p0, p1 - p0, attrs, replace);
					SwingUtilities.invokeLater(caretPosRunnable);
				} catch (BadLocationException exc) {
					;//ignore
				}
				inputAttributes.removeAttributes(inputAttributes);
			} else {
				if (replace) {
					inputAttributes.removeAttributes(inputAttributes);
				}
				inputAttributes.addAttributes(attrs);
			}
		}

		protected final void setParagraphMLAttributes(WordMLTextPane editor,
				AttributeSet attr, boolean replace) {
			Runnable caretPosRunnable = createCaretPositionRunnable(editor);
			
			int p0 = editor.getSelectionStart();
			int p1 = editor.getSelectionEnd();
			WordMLDocument doc = (WordMLDocument) editor.getDocument();
			doc.setParagraphMLAttributes(p0, (p1 - p0), attr, replace);
			SwingUtilities.invokeLater(caretPosRunnable);
		}
		
		protected final Runnable createCaretPositionRunnable(final WordMLTextPane editor) {
			final int caretDot = editor.getCaret().getDot();
			final int caretMark = editor.getCaret().getMark();
			Runnable caretPosRunnable = new Runnable() {
				public void run() {
					if (caretDot != caretMark) {
						editor.setCaretPosition(caretMark);
						editor.moveCaretPosition(caretDot);
					} else {
						editor.setCaretPosition(caretDot);
					}
				}
			};
			return caretPosRunnable;
		}
	}// StyledTextAction inner class
	
	public static class AlignmentAction extends StyledTextAction {

		/**
		 * Creates a new AlignmentAction.
		 *
		 * @param nm the action name
		 * @param a the alignment >= 0
		 */
    	private int _alignment;
    	
		public AlignmentAction(String name, int alignment) {
		    super(name);
		    this._alignment = alignment;
		}

        public void actionPerformed(ActionEvent e) {
			JEditorPane editor = getEditor(e);
			if (editor instanceof WordMLTextPane) {
				int a = this._alignment;
				if ((e != null) && (e.getSource() == editor)) {
					String s = e.getActionCommand();
					try {
						a = Integer.parseInt(s, 10);
					} catch (NumberFormatException nfe) {
					}
				}
				MutableAttributeSet attr = new SimpleAttributeSet();
				StyleConstants.setAlignment(attr, a);
				setParagraphMLAttributes((WordMLTextPane) editor, attr, false);
			}
		}
	}// AlignmentAction inner class
	
	public static class BoldAction extends StyledTextAction {
		private boolean isBold;
		
		public BoldAction(boolean isBold) {
		    super(fontBoldAction);
		    this.isBold = isBold;
		}

        public void actionPerformed(ActionEvent e) {
			JEditorPane editor = getEditor(e);
			if (editor instanceof WordMLTextPane) {
				SimpleAttributeSet sas = new SimpleAttributeSet();
				StyleConstants.setBold(sas, this.isBold);
				setRunMLAttributes((WordMLTextPane) editor, sas, false);
			}
		}
	}// BoldAction inner class
	
	public static class ItalicAction extends StyledTextAction {
		private boolean isItalic;
		
		public ItalicAction(boolean isItalic) {
		    super(fontItalicAction);
		    this.isItalic = isItalic;
		}

        public void actionPerformed(ActionEvent e) {
			JEditorPane editor = getEditor(e);
			if (editor instanceof WordMLTextPane) {
				SimpleAttributeSet sas = new SimpleAttributeSet();
				StyleConstants.setItalic(sas, this.isItalic);
				setRunMLAttributes((WordMLTextPane) editor, sas, false);
			}
		}
	}// ItalicAction inner class
	
	public static class UnderlineAction extends StyledTextAction {
		private boolean isUnderlined;
		
		public UnderlineAction(boolean isUnderlined) {
		    super(fontUnderlineAction);
		    this.isUnderlined = isUnderlined;
		}

        public void actionPerformed(ActionEvent e) {
			JEditorPane editor = getEditor(e);
			if (editor instanceof WordMLTextPane) {
				SimpleAttributeSet sas = new SimpleAttributeSet();
				StyleConstants.setUnderline(sas, this.isUnderlined);
				setRunMLAttributes((WordMLTextPane) editor, sas, false);
			}
		}
	}// UnderlineAction inner class
	
    private static class InsertSoftBreakAction extends StyledTextAction {
    	public InsertSoftBreakAction(String name) {
    		super(name);
    	}
    	
    	public void actionPerformed(ActionEvent e) {
			JEditorPane editor = getEditor(e);
			if (editor instanceof WordMLTextPane) {
				if ((! editor.isEditable()) || (! editor.isEnabled())) {
				    UIManager.getLookAndFeel().provideErrorFeedback(editor);
				    return;
				}
				
				if (log.isDebugEnabled()) {
					log.debug("InsertSoftBreakAction.actionPerformed()");
				}
			}
    	}
    }// InsertSoftBreakAction inner class
    
    private static class EnterKeyTypedAction extends StyledTextAction {
    	public EnterKeyTypedAction(String name) {
    		super(name);
    	}
    	
    	public void actionPerformed(ActionEvent e) {
			final JEditorPane editor = getEditor(e);
			if (editor instanceof WordMLTextPane) {
				if ((! editor.isEditable()) || (! editor.isEnabled())) {
				    UIManager.getLookAndFeel().provideErrorFeedback(editor);
				    return;
				}
				
				WordMLDocument doc = (WordMLDocument) editor.getDocument();
				final int pos = editor.getCaretPosition();
				
				DocumentElement leafE = 
					(DocumentElement) doc.getCharacterElement(pos);
				if (!leafE.isEditable()
					&& leafE.getStartOffset() < pos
					&& pos < leafE.getEndOffset()) {
			        WordMLEditor wmlEditor = 
			        	WordMLEditor.getInstance(WordMLEditor.class);
			        ResourceMap rm = wmlEditor.getContext().getResourceMap();
			        String title = 
			        	rm.getString("Application.edit.info.dialog.title");
			        String message =
			        	rm.getString("Application.edit.info.unEditableMessage");
			        wmlEditor.showMessageDialog(
			        	title, message, JOptionPane.INFORMATION_MESSAGE);
			        return;
				}
				
				if (log.isDebugEnabled()) {
					log.debug("EnterKeyTypedAction.actionPerformed(): START");
				}
				
				DocumentElement paraE = 
					(DocumentElement) doc.getParagraphMLElement(pos, false);
				if (paraE.getStartOffset() == pos
					|| pos == paraE.getEndOffset() - 1) {
					//Create a new empty paragraph
		    		boolean before = (paraE.getStartOffset() == pos);
		    		insertNewEmptyParagraph(paraE, before);
				} else {
					splitParagraph(paraE, pos);
				}
				
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						editor.setCaretPosition(pos + 1);
						
						if (log.isDebugEnabled()) {
							log.debug("EnterKeyTypedAction.actionPerformed(): Resulting Structure...");
							DocUtil.displayXml(editor.getDocument());
							DocUtil.displayStructure(editor.getDocument());
						}
					}
				});
			}
    	}
    	
    	private void insertNewEmptyParagraph(DocumentElement paraE, boolean before) {
    		ParagraphML paraML = (ParagraphML) paraE.getElementML();
        	ParagraphPropertiesML pPr =
        		(ParagraphPropertiesML) paraML.getParagraphProperties();
        	if (pPr != null) {
        		pPr = (ParagraphPropertiesML) pPr.clone();
        	}
        	
    		ParagraphML newParaML = ElementMLFactory.createParagraphML(null, pPr, null);    		
    		paraML.addSibling(newParaML, !before);

			WordMLDocument doc = (WordMLDocument) paraE.getDocument();
			int pos = before ? paraE.getStartOffset() : paraE.getEndOffset();
			List<ElementSpec> newParaSpecs = DocUtil.getElementSpecs(newParaML);
			
			try {
				DocUtil.insertParagraphs(doc, pos, newParaSpecs);
			} catch (BadLocationException exc) {
				;//ignore
			}
    	}
    	
    	private void splitParagraph(DocumentElement paraE, int caretPos) {
			WordMLDocument doc = (WordMLDocument) paraE.getDocument();
			
			ElementML newParaML = 
				DocUtil.splitElementML(paraE, caretPos - paraE.getStartOffset());
			List<ElementSpec> specs = DocUtil.getElementSpecs(paraE.getElementML());
			specs.addAll(DocUtil.getElementSpecs(newParaML));
    		
			DocUtil.displayStructure(specs);
			
			int offset = paraE.getStartOffset();
			try {
				int length = paraE.getEndOffset() - offset;

				//Disable filter temporarily
				WordMLDocumentFilter filter = 
					(WordMLDocumentFilter) doc.getDocumentFilter();
				filter.setEnabled(false);
				
				doc.remove(offset, length);
				
				//Remember to enable filter back
				filter.setEnabled(true);
			} catch (BadLocationException exc) {
				;//ignore
			}
			
			insertParagraphsLater(doc, offset, specs);
    	}
    	
    	private void insertParagraphsLater(
    		final WordMLDocument doc, 
    		final int offset, 
    		final List<ElementSpec> specs) {
    		
    		SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						DocUtil.insertParagraphs(doc, offset, specs);
					} catch (BadLocationException exc) {
						;//ignore
					}
				}
			});
    	}
    	
    }// EnterKeyTypedAction inner class
    
    private static class DeleteNextCharAction extends StyledTextAction {

		/* Create this object with the appropriate identifier. */
		DeleteNextCharAction(String name) {
			super(deleteNextCharAction);
		}

		/** The operation to perform when this action is triggered. */
		public void actionPerformed(ActionEvent e) {
			final JEditorPane editor = getEditor(e);
			if (editor instanceof WordMLTextPane) {
				if ((! editor.isEditable()) || (! editor.isEnabled())) {
				    UIManager.getLookAndFeel().provideErrorFeedback(editor);
				    return;
				}
				final WordMLDocument doc = (WordMLDocument) editor.getDocument();
				
				Caret caret = editor.getCaret();
				int dot = caret.getDot();
				int mark = caret.getMark();
				
				WordMLEditorKit kit = (WordMLEditorKit) editor.getEditorKit();
				if (kit.getCaretElement() != doc.getCharacterElement(dot)) {
					kit.saveCaretText();
				}
				
				if (log.isDebugEnabled()) {
					log.debug("DeleteNextCharAction.actionPerformed(): dot=" + dot
						+ " doc.getLength()=" + doc.getLength());
				}
							
				try {
					if (dot != mark) {
						doc.remove(Math.min(dot, mark), Math.abs(dot - mark));
						dot = Math.min(dot, mark);
						
					} else if (dot < doc.getLength() - 1) {
						int delChars = 1;

						String dotChars = doc.getText(dot, 2);
						char c0 = dotChars.charAt(0);
						char c1 = dotChars.charAt(1);

						if (c0 >= '\uD800' && c0 <= '\uDBFF'
								&& c1 >= '\uDC00' && c1 <= '\uDFFF') {
							delChars = 2;
						}

						doc.remove(dot, delChars);
					}
				} catch (BadLocationException exc) {
					;// ignore
				}
				
				final int caretPos = dot;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						editor.setCaretPosition(caretPos);
						
						if (log.isDebugEnabled()) {
							DocUtil.displayXml(doc);
							DocUtil.displayStructure(doc);
						}
					}
				});
			}//if (editor != null)
		}//actionPerformed()
	}//DeleteNextCharAction()

    private static class DeletePrevCharAction extends StyledTextAction {

		/* Create this object with the appropriate identifier. */
    	DeletePrevCharAction(String name) {
			super(deletePrevCharAction);
		}

		/** The operation to perform when this action is triggered. */
		public void actionPerformed(ActionEvent e) {
			final JEditorPane editor = getEditor(e);
			if (editor instanceof WordMLTextPane) {
				if ((! editor.isEditable()) || (! editor.isEnabled())) {
				    UIManager.getLookAndFeel().provideErrorFeedback(editor);
				    return;
				}
				
				final WordMLDocument doc = (WordMLDocument) editor.getDocument();
				Caret caret = editor.getCaret();
				int dot = caret.getDot();
				int mark = caret.getMark();
				
				WordMLEditorKit kit = (WordMLEditorKit) editor.getEditorKit();
				if (kit.getCaretElement() != doc.getCharacterElement(dot - 1)) {
					kit.saveCaretText();
				}
				
				if (log.isDebugEnabled()) {
					log.debug("DeletePrevCharAction.actionPerformed(): dot=" + dot
						+ " doc.getLength()=" + doc.getLength());
				}
				
				try {
					if (dot != mark) {
						doc.remove(Math.min(dot, mark), Math.abs(dot - mark));
						dot = Math.min(dot, mark);
						
					} else if (0 < dot && dot < doc.getLength()) {
	                    int delChars = 1;
	                    
	                    if (dot > 1) {
	                        String dotChars = doc.getText(dot - 2, 2);
	                        char c0 = dotChars.charAt(0);
	                        char c1 = dotChars.charAt(1);
	                        
	                        if (c0 >= '\uD800' && c0 <= '\uDBFF' &&
	                            c1 >= '\uDC00' && c1 <= '\uDFFF') {
	                            delChars = 2;
	                        }
	                    }
	                    
	                    doc.remove(dot - delChars, delChars);
	                    dot = dot - delChars;
					}
				} catch (BadLocationException exc) {
					;//ignore
				}
				
				final int caretPos = dot;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						editor.setCaretPosition(caretPos);
						
						if (log.isDebugEnabled()) {
							DocUtil.displayXml(doc);
							DocUtil.displayStructure(doc);
						}
					}
				});
			}//if (editor != null)
		}//actionPerformed()
		
	}//DeletePrevCharAction()


}// WordMLEditorKit class























