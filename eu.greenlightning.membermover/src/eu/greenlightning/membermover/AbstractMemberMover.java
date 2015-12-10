package eu.greenlightning.membermover;

import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.RewriteSessionEditProcessor;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;

public abstract class AbstractMemberMover {

	private final ITextEditor editor;
	private final ITextSelection selection;
	private final CompilationUnit unit;

	public AbstractMemberMover(ExecutionEvent event) {
		this.editor = getEditor(event);
		this.selection = getSelection();
		this.unit = parseCompilationUnit();
		this.unit.recordModifications();
	}

	private ITextEditor getEditor(ExecutionEvent event) {
		return (ITextEditor) HandlerUtil.getActiveEditor(event);
	}

	private IDocument getDocument() {
		return editor.getDocumentProvider().getDocument(editor.getEditorInput());
	}

	private ITextSelection getSelection() {
		return (ITextSelection) editor.getSelectionProvider().getSelection();
	}

	private CompilationUnit parseCompilationUnit() {
		ITypeRoot typeRoot = JavaUI.getEditorInputTypeRoot(editor.getEditorInput());
		ICompilationUnit unit = typeRoot.getAdapter(ICompilationUnit.class);
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit);
		return (CompilationUnit) parser.createAST(null);
	}

	public final void execute() throws ExecutionException {
		TypeDeclarationFinder finder = new TypeDeclarationFinder(getSelectedNode());
		if (finder.hasResults()) {
			@SuppressWarnings("unchecked")
			List<BodyDeclaration> declarations = finder.getTypeDeclaration().bodyDeclarations();
			boolean success = move(declarations, finder.getBodyDeclaration());
			if (success) {
				applyChanges();
				adjustSelection(finder.getBodyDeclaration());
			}
		}
	}

	private ASTNode getSelectedNode() {
		NodeFinder finder = new NodeFinder(unit, selection.getOffset(), selection.getLength());
		return finder.getCoveringNode();
	}

	protected abstract boolean move(List<BodyDeclaration> declarations, BodyDeclaration member);

	private void applyChanges() throws ExecutionException {
		IDocument document = getDocument();
		TextEdit edit = unit.rewrite(document, null);
		performTextEdit(document, edit);
	}

	private void performTextEdit(IDocument document, TextEdit edit) throws ExecutionException {
		try {
			tryPerformTextEdit(document, edit);
		} catch (MalformedTreeException e) {
			throw new ExecutionException(null, e);
		} catch (BadLocationException e) {
			throw new ExecutionException(null, e);
		}
	}

	private void tryPerformTextEdit(IDocument document, TextEdit edit) throws BadLocationException {
		int flags = TextEdit.CREATE_UNDO | TextEdit.UPDATE_REGIONS;
		new RewriteSessionEditProcessor(document, edit, flags).performEdits();
	}

	private void adjustSelection(BodyDeclaration member) {
		int memberStart = member.getStartPosition();
		int memberEnd = member.getStartPosition() + member.getLength();
		int selectionStart = selection.getOffset();
		int selectionEnd = selection.getOffset() + selection.getLength();
		
		if (selectionStart == memberStart || selectionEnd == memberEnd) {
			ITextSelection newSelection = getSelection();
			editor.getSelectionProvider().setSelection(adjustSelection(newSelection, selection.getLength()));
		}
	}

	protected abstract ITextSelection adjustSelection(ITextSelection selection, int length);
	
}