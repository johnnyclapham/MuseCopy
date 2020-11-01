package edu.wm.cs.muse.dataleak.operators;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import edu.wm.cs.muse.dataleak.DataLeak;
import edu.wm.cs.muse.dataleak.support.OperatorType;
import edu.wm.cs.muse.dataleak.support.Placementchecker;
import edu.wm.cs.muse.dataleak.support.TryCatchHandler;
import edu.wm.cs.muse.dataleak.support.Utility;
import edu.wm.cs.muse.dataleak.support.node_containers.SinkNodeChangeContainers;

/**
 * The TaintSinkOperator class formats and inserts sinks or sources
 * depending on SinkNodeChangeContainers' insertion type.
 * 
 * @author Yang Zhang
 */

public class TaintSinkOperator {
	ArrayList<SinkNodeChangeContainers> nodeChanges;
	ASTRewrite rewriter;
	HashMap<Integer, Integer> repeatCounts = new HashMap<Integer, Integer>();
	Placementchecker checker = new Placementchecker();
	File temp_file;
	String source_file;
	private TryCatchHandler handler = new TryCatchHandler();

	public TaintSinkOperator(ASTRewrite rewriter) {
		this.rewriter = rewriter;
	}

	public TaintSinkOperator(ASTRewrite rewriter, ArrayList<SinkNodeChangeContainers> nodeChanges, String source_file) {
		this.rewriter = rewriter;
		this.nodeChanges = nodeChanges;
		this.source_file = source_file;
	}

	/**
	 * Modifies the ASTRewrite to swap between insertions based on the nodeChanges
	 * and returns it.
	 * 
	 * @return ASTRewrite modified ASTRewrite
	 * @throws ClassNotFoundException 
	 */
	public ASTRewrite InsertChanges() {

		for (SinkNodeChangeContainers nodeChange : nodeChanges) {

			if (nodeChange.insertionType == 0) {
				try {
					insertSink(nodeChange.node, nodeChange.index, nodeChange.count, nodeChange.propertyDescriptor,
							nodeChange.method);
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			else {
				try {
					insertSource(nodeChange.node, nodeChange.index, nodeChange.propertyDescriptor, nodeChange.count);
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		if (!(temp_file ==null)) {
			temp_file.delete();
		}
		return rewriter;
	}

	void insertSink(ASTNode node, int index, int count, ChildListPropertyDescriptor nodeProperty, ASTNode method) throws ClassNotFoundException {
		ListRewrite listRewrite = rewriter.getListRewrite(node, nodeProperty);
		int cur = repeatCounts.containsKey(count) ? repeatCounts.get(count) : -1;
		repeatCounts.put(count, cur + 1);
		ASTNode placeHolder;
		if (handler.stringHasThrows(DataLeak.getSink(OperatorType.TAINTSINK, count, repeatCounts.get(count)))) {
			placeHolder = handler.addTryCatch((Statement) rewriter.createStringPlaceholder(
					DataLeak.getSink(OperatorType.TAINTSINK, count, repeatCounts.get(count)), ASTNode.EMPTY_STATEMENT));
		}
		else {
			 placeHolder = (Statement) rewriter.createStringPlaceholder(
						DataLeak.getSink(OperatorType.TAINTSINK, count, repeatCounts.get(count)), ASTNode.EMPTY_STATEMENT);
		}
		listRewrite.insertAt(placeHolder, index, null);
		if (!(listRewrite.getParent().getRoot() instanceof Block)) {
			temp_file = checker.getTempFile((CompilationUnit) listRewrite.getParent().getRoot(), rewriter, source_file);
			try {
				if (!checker.check(temp_file)) {
					listRewrite.remove(placeHolder, null);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		String methodName = ((MethodDeclaration) method).getName().toString();
		String className = "";
		method = method.getParent();
		while (method != null) {
			if (method.getNodeType() == ASTNode.TYPE_DECLARATION) {
				className = ((TypeDeclaration) method).getName().toString();
				break;
			} else if (method.getNodeType() == ASTNode.ANONYMOUS_CLASS_DECLARATION) {
				className = "1";
				break;
			}
			method = method.getParent();
		}
		System.out.println(String.format("leak-%d-%d: %s.%s", count, repeatCounts.get(count), className, methodName));
	}

	void insertSource(ASTNode node, int index, ChildListPropertyDescriptor nodeProperty, int count) throws ClassNotFoundException {
		ListRewrite listRewrite = rewriter.getListRewrite(node, nodeProperty);
//		Statement placeHolder = (Statement) rewriter
//				.createStringPlaceholder(DataLeak.getSource(OperatorType.TAINTSINK, count), ASTNode.EMPTY_STATEMENT);
		ASTNode placeHolder;
		if (handler.stringHasThrows(DataLeak.getTaintSourceFinalDecl(count))) {
			placeHolder = handler.addTryCatch((Statement) rewriter.createStringPlaceholder(
					DataLeak.getTaintSourceFinalDecl(count), ASTNode.EMPTY_STATEMENT));
		}
		else {
			placeHolder = (Statement) rewriter
					.createStringPlaceholder(DataLeak.getTaintSourceFinalDecl(count), ASTNode.EMPTY_STATEMENT);
		}
		listRewrite.insertAt(placeHolder, index, null);
		if (!(listRewrite.getParent().getRoot() instanceof Block)) {
			temp_file = checker.getTempFile((CompilationUnit) listRewrite.getParent().getRoot(), rewriter, source_file);
			try {
				if (!checker.check(temp_file)) {
					listRewrite.remove(placeHolder, null);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
