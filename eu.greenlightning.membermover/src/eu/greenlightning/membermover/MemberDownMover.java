package eu.greenlightning.membermover;

import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;

public class MemberDownMover extends AbstractMemberMover {

	public MemberDownMover(ExecutionEvent event) {
		super(event);
	}

	@Override
	protected boolean move(List<BodyDeclaration> declarations, BodyDeclaration member) {
		int index = declarations.indexOf(member);
		if (index >= 0 && index + 1 < declarations.size()) {
			BodyDeclaration partner = declarations.remove(index + 1);
			declarations.add(index, partner);
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	protected ITextSelection adjustSelection(ITextSelection selection, int length) {
		int end = selection.getOffset() + selection.getLength();
		return new TextSelection(end - length, length);
	}

}
