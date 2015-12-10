package eu.greenlightning.membermover;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class TypeDeclarationFinder {

	private ASTNode member;
	private ASTNode parent;

	public TypeDeclarationFinder(ASTNode node) {
		member = node;
		parent = member.getParent();
		while (parent != null && !isTypeAndBodyDeclaration()) {
			member = parent;
			parent = member.getParent();
		}
	}

	private boolean isTypeAndBodyDeclaration() {
		return parent.getNodeType() == ASTNode.TYPE_DECLARATION
				&& getTypeDeclaration().bodyDeclarations().contains(member);
	}

	public boolean hasResults() {
		return parent != null;
	}

	public TypeDeclaration getTypeDeclaration() {
		return (TypeDeclaration) parent;
	}

	public BodyDeclaration getBodyDeclaration() {
		return hasResults() ? (BodyDeclaration) member : null;
	}

}
