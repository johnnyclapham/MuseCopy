package edu.wm.cs.muse.dataleak.operators;

import java.io.File;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;


import edu.wm.cs.muse.dataleak.DataLeak;
import edu.wm.cs.muse.dataleak.support.OperatorType;
import edu.wm.cs.muse.dataleak.support.Placementchecker;
import edu.wm.cs.muse.dataleak.support.TryCatchHandler;
import edu.wm.cs.muse.dataleak.support.Utility;
import edu.wm.cs.muse.dataleak.support.node_containers.SourceNodeChangeContainers;
import edu.wm.cs.muse.dataleak.support.node_containers.SourceNodeChangeContainers.INSERTION_TYPE;

/**
 * The SourceScopeOperator class has three methods, InsertChanges, insertInMethodBody,
 * and insertVariable Declaration. InsertChanges allows for the other two methods to
 * be called depending on the Insertion Type called for in SourceNodeChangeContainers.
 * 
 * @author Yang Zhang, Amit Seal Ami
 */

public class ScopeSourceOperator {
	Placementchecker checker = new Placementchecker();
	File temp_file;
	String source_file;
	ArrayList<String> variablelist = new ArrayList<String>();
	private TryCatchHandler handler = new TryCatchHandler();
	ArrayList<SourceNodeChangeContainers> nodeChanges;
	ASTRewrite rewriter;
	private boolean checkCompilability;

	public ScopeSourceOperator(ASTRewrite rewriter, ArrayList<SourceNodeChangeContainers> nodeChanges,
			String source_file, boolean checkCompilability) {
		this.rewriter = rewriter;
		this.nodeChanges = nodeChanges;
		this.source_file = source_file;
		this.checkCompilability = checkCompilability;
		
	}

	/**
	 * Modifies the ASTRewrite to insert a taint declaration and a source string in
	 * the method, then returns it.
	 * 
	 * @return ASTRewrite modified ASTRewrite
	 */
	public ASTRewrite InsertChanges() {

		for (SourceNodeChangeContainers nodeChange : nodeChanges) {
			// if (nodeChange.insertionType == 0 && nodeChange.node != null ) {
			if (nodeChange.type == INSERTION_TYPE.METHOD_BODY && nodeChange.node != null) {
				try {
					insertInMethodBody((Block) nodeChange.node, nodeChange.index, nodeChange.propertyDescriptor);
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else {
				if (nodeChange.node != null) {
					try {
						insertVariableDeclaration(nodeChange.node, nodeChange.index, nodeChange.propertyDescriptor);
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			}

		}
		if (!(temp_file ==null)) {
			temp_file.delete();
		}		
		return rewriter;

	}

	// for inserting source inside methodBody
	public void insertInMethodBody(Block node, int index, ChildListPropertyDescriptor nodeProperty) throws ClassNotFoundException {
		int placement = 0;
		for (Object obj : node.statements()) {
			if (obj.toString().startsWith("super") || obj.toString().startsWith("this(")) {
				placement++;
			}
		}
		int identifier = Utility.COUNTER_GLOBAL - 1;

		ListRewrite listRewrite = rewriter.getListRewrite(node, nodeProperty);
		String source = DataLeak.getSource(OperatorType.SCOPESOURCE, identifier);
		//Statement placeHolder = (Statement) rewriter.createStringPlaceholder(source, ASTNode.EMPTY_STATEMENT);
		// listRewrite.insertAt(placeHolder, index, null);
		ASTNode placeHolder;
		if (handler.stringHasThrows(source)) {
			placeHolder = handler.addTryCatch((Statement) rewriter.createStringPlaceholder(source, ASTNode.EMPTY_STATEMENT), rewriter);
		}
		else {
			placeHolder = (Statement) rewriter.createStringPlaceholder(source, ASTNode.EMPTY_STATEMENT);
		}
		
		listRewrite.insertAt(placeHolder, placement, null);
		if (!(listRewrite.getParent().getRoot() instanceof Block) && checkCompilability) {
			temp_file = checker.getTempFile((CompilationUnit)listRewrite.getParent().getRoot(), rewriter, source_file);
			try {
				if (!checker.check(temp_file))
					listRewrite.remove(placeHolder,null);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// for declaration.
	private void insertVariableDeclaration(ASTNode node, int index, ChildListPropertyDescriptor nodeProperty) throws ClassNotFoundException {

		ListRewrite listRewrite = rewriter.getListRewrite(node, nodeProperty);
		String variable = String.format(DataLeak.getVariableDeclaration(OperatorType.SCOPESOURCE), Utility.COUNTER_GLOBAL, 
				Utility.COUNTER_GLOBAL);
		ASTNode placeHolder;
		if (handler.stringHasThrows(variable)) {
			placeHolder = handler.addTryCatch((Statement) rewriter.createStringPlaceholder(variable, ASTNode.EMPTY_STATEMENT), rewriter);
		}
		else {
			placeHolder = (Statement) rewriter.createStringPlaceholder(variable, ASTNode.EMPTY_STATEMENT);
		}
		
		listRewrite.insertAt(placeHolder, index, null);
		ASTNode aRoot = listRewrite.getParent().getRoot();
		if (!(aRoot instanceof Block) && checkCompilability) {
			CompilationUnit astRoot = (CompilationUnit)aRoot;
		
			try {
				temp_file = checker.getTempFile(astRoot, rewriter, source_file);
				if (!checker.check(temp_file)) {
					System.out.println("Removing " + variable);
					listRewrite.remove(placeHolder,null);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		Utility.COUNTER_GLOBAL++;


	}

}
