package edu.wm.cs.muse;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.junit.After;
import org.junit.Test;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;

import edu.wm.cs.muse.dataleak.DataLeak;
import edu.wm.cs.muse.dataleak.support.Arguments;
import edu.wm.cs.muse.dataleak.support.FileUtility;
import edu.wm.cs.muse.dataleak.support.OperatorType;
import edu.wm.cs.muse.dataleak.support.Utility;
import  edu.wm.cs.muse.mdroid.ASTHelper;

/*
 * We will be focusing on creating behavior based test cases. AAA pattern, i.e. 
 * Arrange the preconditions
 * Act on test Object
 * Assert the results
 * will be utilized.
 */

/**
 * Unit test file of Muse.
 * 
 * @author Amit Seal Ami, Liz Weech, Yang Zhang, Scott Murphy
 *
 */

@TestMethodOrder(OrderAnnotation.class)
public class MuseTest {

	File expectedOutput;
	String content = null;
	Muse muse;
	CompilationUnit root;
	Document sourceDoc;
	ASTRewrite rewriter;
	TextEdit edits;
	File processedOutput;

	// Muse output is written to this file in each test, and compared to
	// the expected output.
	File output = new File("test/output/output.txt");
	
	/**
	 * This is done after every test to reset output.txt to contain a sample, pre-mutation output.
	 * This then works to clean up the output.txt file and make sure that the 
	 * placementchecker doesn't remove leaks from the previous test cases.
	 * 
	 * @throws FileNotFoundException  output.txt or output_reset.txt was not found
	 */
	@After
	public void reset() throws FileNotFoundException {
		
		FileReader fr = new FileReader("test/output/output_reset.txt");
		BufferedReader br = new BufferedReader(fr);
		
		try {
			FileWriter fw = new FileWriter("test/output/output.txt", true);
			String s;

			while ((s = br.readLine()) != null) { // read a line
				fw.write(s); // write to output file
				fw.flush();
			}
			br.close();
			fw.close();
		} catch (IOException e) {
			System.out.println("IOException when refreshing output.txt");
			e.printStackTrace();
		}
		
		PrintWriter pw = new PrintWriter("output.txt");
		pw.close();
	}
	
	private void scope_multi_reset() throws FileNotFoundException {
		FileReader fr = new FileReader("test/output/scope_multi_reset.txt");
		BufferedReader br = new BufferedReader(fr);
		
		try {
			FileWriter fw = new FileWriter("test/output/output.txt", false);
			String s;

			while ((s = br.readLine()) != null) { // read a line
				fw.write(s); // write to output file
				fw.flush();
			}
			br.close();
			fw.close();
		} catch (IOException e) {
			System.out.println("IOException when refreshing output.txt");
			e.printStackTrace();
		}
		
		PrintWriter pw = new PrintWriter("output.txt");
		pw.close();
	}
	
	@Test
	@Order(1)
	public void reachability_operation_on_hello_world() throws Exception {

		try {
			prepare_test_files(OperatorType.REACHABILITY, 1);
			execute_muse(OperatorType.REACHABILITY);
			assertEquals(true, FileUtility.testFileEquality(expectedOutput, processedOutput));

		} catch (IOException e) {
			e.printStackTrace();

		} catch (MalformedTreeException e) {
			e.printStackTrace();

		} catch (BadLocationException e) {
			e.printStackTrace();

		}
	}
	
	@Test
	@Order(2)
	public void complex_reachability_operation_on_multi_class() {
		try {
			prepare_test_files(OperatorType.COMPLEXREACHABILITY, 1);
			execute_muse(OperatorType.COMPLEXREACHABILITY);
			assertEquals(true, FileUtility.testFileEquality(expectedOutput, processedOutput));

		} catch (IOException e) {
			e.printStackTrace();

		} catch (MalformedTreeException e) {
			e.printStackTrace();

		} catch (BadLocationException e) {
			e.printStackTrace();

		}
	}
	
	@Test
	@Order(3)
	public void taint_source_operation_on_hello_world() {
		try {
			prepare_test_files(OperatorType.TAINTSOURCE, 1);
			execute_muse(OperatorType.TAINTSOURCE);
			assertEquals(true, FileUtility.testFileEquality(expectedOutput, processedOutput));

		} catch (IOException e) {
			e.printStackTrace();

		} catch (MalformedTreeException e) {
			e.printStackTrace();

		} catch (BadLocationException e) {
			e.printStackTrace();

		}
	}

	@Test
	@Order(4)
	public void taint_sink_operation_on_hello_world() {

		try {
			prepare_test_files(OperatorType.TAINTSINK, 1);
			execute_muse(OperatorType.TAINTSINK);
			assertEquals(true, FileUtility.testFileEquality(expectedOutput, processedOutput));
			 
		} catch (IOException e) {
			e.printStackTrace();

		} catch (MalformedTreeException e) {
			e.printStackTrace();

		} catch (BadLocationException e) {
			e.printStackTrace();

		}

	}

	@Test
	@Order(5)
	public void scope_source_operation_on_hello_world() {
		try {
			prepare_test_files(OperatorType.SCOPESOURCE, 1);
			execute_muse(OperatorType.SCOPESOURCE);
			assertEquals(true, FileUtility.testFileEquality(expectedOutput, processedOutput));
			
		} catch (IOException e) {
			e.printStackTrace();

		} catch (MalformedTreeException e) {
			e.printStackTrace();

		} catch (BadLocationException e) {
			e.printStackTrace();

		}

	}

	@Test
	@Order(6)
	public void scope_source_operation_on_multi_class() {
		try {
			scope_multi_reset();
			prepare_test_files(OperatorType.SCOPESOURCE, 2);
			execute_muse(OperatorType.SCOPESOURCE);
			assertEquals(true, FileUtility.testFileEquality(expectedOutput, processedOutput));

		} catch (IOException e) {
			e.printStackTrace();

		} catch (MalformedTreeException e) {
			e.printStackTrace();

		} catch (BadLocationException e) {
			e.printStackTrace();

		}

	}
	
	@Test
	@Order(7)
	public void scope_sink_operation_on_multi_class() {
		try {
			prepare_test_files(OperatorType.SCOPESINK, 1);
			execute_muse(OperatorType.SCOPESINK);
			assertEquals(true, FileUtility.testFileEquality(expectedOutput, processedOutput));
			
		} catch (IOException e) {
			e.printStackTrace();

		} catch (MalformedTreeException e) {
			e.printStackTrace();

		} catch (BadLocationException e) {
			e.printStackTrace();

		}
	}
	
	@Test
	@Order(8)
	public void try_catch_taint_sink_operation_on_hello_world() {
		String[] original_operators = new String[] {DataLeak.getRawSource(OperatorType.TAINTSOURCE), DataLeak.getVariableDeclaration(OperatorType.TAINTSOURCE),
													DataLeak.getRawSink(OperatorType.TAINTSINK)};
		
		try {
			/*
			DataLeak.setSource(OperatorType.TAINTSOURCE, "Cipher cipher_leak%d = javax.crypto.Cipher.getInstance(dataLeAk%<d);");
			DataLeak.setVariableDeclaration(OperatorType.TAINTSOURCE, "String dataLeAk%d = \"AES\"; ");
			DataLeak.setSink(OperatorType.TAINTSINK, "android.util.Log.d(\"leak-%d-%d\", cipher_leak%d.getAlgorithm())");
			*/
			
			DataLeak.setSource(OperatorType.TAINTSOURCE, "cipher_leak%d = javax.crypto.Cipher.getInstance(\"AES\");");
			DataLeak.setVariableDeclaration(OperatorType.TAINTSOURCE, "javax.crypto.Cipher cipher_leak%d = null; ");
			DataLeak.setSink(OperatorType.TAINTSINK, "android.util.Log.d(\"leak-%d-%d\", cipher_leak%d.getAlgorithm())");
			
			prepare_try_test_files();
			execute_muse(OperatorType.TAINTSINK);
			DataLeak.setSource(OperatorType.TAINTSOURCE, original_operators[0]);
			DataLeak.setVariableDeclaration(OperatorType.TAINTSOURCE, original_operators[1]);
			DataLeak.setSink(OperatorType.TAINTSINK, original_operators[2]);
			assertEquals(true, FileUtility.testFileEquality(expectedOutput, processedOutput));
			 
		} catch (IOException e) {
			e.printStackTrace();
			DataLeak.setSource(OperatorType.TAINTSOURCE, original_operators[0]);
			DataLeak.setVariableDeclaration(OperatorType.TAINTSOURCE, original_operators[1]);
			DataLeak.setSink(OperatorType.TAINTSINK, original_operators[2]);
		} catch (MalformedTreeException e) {
			e.printStackTrace();
			DataLeak.setSource(OperatorType.TAINTSOURCE, original_operators[0]);
			DataLeak.setVariableDeclaration(OperatorType.TAINTSOURCE, original_operators[1]);
			DataLeak.setSink(OperatorType.TAINTSINK, original_operators[2]);
		} catch (BadLocationException e) {
			e.printStackTrace();
			DataLeak.setSource(OperatorType.TAINTSOURCE, original_operators[0]);
			DataLeak.setVariableDeclaration(OperatorType.TAINTSOURCE, original_operators[1]);
			DataLeak.setSink(OperatorType.TAINTSINK, original_operators[2]);
		}
	}
	
	@Test
	@Order(9)
	public void try_catch_scope_sink_operation_on_multi_class() {
		String[] original_operators = new String[] {DataLeak.getRawSource(OperatorType.SCOPESOURCE), DataLeak.getVariableDeclaration(OperatorType.SCOPESOURCE),
													DataLeak.getRawSink(OperatorType.SCOPESINK)};
		
		try {
			scope_multi_reset();
			
			DataLeak.setSource(OperatorType.SCOPESOURCE, "cipher_leak%d = javax.crypto.Cipher.getInstance(\"AES\");");
			DataLeak.setVariableDeclaration(OperatorType.SCOPESOURCE, "javax.crypto.Cipher cipher_leak%d = null; ");
			DataLeak.setSink(OperatorType.SCOPESINK, "android.util.Log.d(\"leak-%d-%d\", cipher_leak%d.getAlgorithm())");
			
			prepare_try_multi_test_files();
			execute_muse(OperatorType.SCOPESINK);
			DataLeak.setSource(OperatorType.SCOPESOURCE, original_operators[0]);
			DataLeak.setVariableDeclaration(OperatorType.SCOPESOURCE, original_operators[1]);
			DataLeak.setSink(OperatorType.SCOPESINK, original_operators[2]);
			assertEquals(true, FileUtility.testFileEquality(expectedOutput, processedOutput));
			 
		} catch (IOException e) {
			e.printStackTrace();
			DataLeak.setSource(OperatorType.SCOPESOURCE, original_operators[0]);
			DataLeak.setVariableDeclaration(OperatorType.SCOPESOURCE, original_operators[1]);
			DataLeak.setSink(OperatorType.SCOPESINK, original_operators[2]);
		} catch (MalformedTreeException e) {
			e.printStackTrace();
			DataLeak.setSource(OperatorType.SCOPESOURCE, original_operators[0]);
			DataLeak.setVariableDeclaration(OperatorType.SCOPESOURCE, original_operators[1]);
			DataLeak.setSink(OperatorType.SCOPESINK, original_operators[2]);
		} catch (BadLocationException e) {
			e.printStackTrace();
			DataLeak.setSource(OperatorType.SCOPESOURCE, original_operators[0]);
			DataLeak.setVariableDeclaration(OperatorType.SCOPESOURCE, original_operators[1]);
			DataLeak.setSink(OperatorType.SCOPESINK, original_operators[2]);
		}
	}
	
	@Test
	@Order(10)
	public void ivh_operation_on_hello_world() {
		try {
			prepare_test_files(OperatorType.IVH, 1);
			execute_muse(OperatorType.IVH);
			assertEquals(true, FileUtility.testFileEquality(expectedOutput, processedOutput));
			prepare_test_files(OperatorType.IVH, 2);
			execute_muse(OperatorType.IVH);
			assertEquals(true, FileUtility.testFileEquality(expectedOutput, processedOutput));
			prepare_test_files(OperatorType.IVH, 3);
			execute_muse(OperatorType.IVH);
			assertEquals(true, FileUtility.testFileEquality(expectedOutput, processedOutput));

		} catch (IOException e) {
			e.printStackTrace();

		} catch (MalformedTreeException e) {
			e.printStackTrace();

		} catch (BadLocationException e) {
			e.printStackTrace();
		}

	}
	
	@Test
	@Order(11)
	public void reachability_operation_on_hello_world_try_catch() {
		try {
			DataLeak.setSource(OperatorType.REACHABILITY, "javax.crypto.Cipher dataLeAk%d = javax.crypto.Cipher.getInstance(\"AES\");");
			prepare_test_files(OperatorType.REACHABILITY, 2);
			execute_muse(OperatorType.REACHABILITY);
			assertEquals(true, FileUtility.testFileEquality(expectedOutput, processedOutput));
		} catch (IOException e) {
			e.printStackTrace();

		} catch (MalformedTreeException e) {
			e.printStackTrace();

		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	@Order(12)
	public void complexreachability_operation_on_hello_world_try_catch() {
		try {
			DataLeak.setSource(OperatorType.COMPLEXREACHABILITY, "javax.crypto.Cipher dataLeAk%d = javax.crypto.Cipher.getInstance(\"AES\");");
			String[] string = {"String leAkPath%d = dataLeAk%d.getAlgorithm();"};
			DataLeak.setPaths(string);
			prepare_test_files(OperatorType.COMPLEXREACHABILITY, 2);
			execute_muse(OperatorType.COMPLEXREACHABILITY);
			assertEquals(true, FileUtility.testFileEquality(expectedOutput, processedOutput));
		} catch (IOException e) {
			e.printStackTrace();

		} catch (MalformedTreeException e) {
			e.printStackTrace();

		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	private void execute_muse(OperatorType operator) throws BadLocationException, MalformedTreeException, IOException {
		Arguments.setTestMode(true);
		rewriter = ASTRewrite.create(root.getAST());
		sourceDoc = new Document(content);
		muse.operatorExecution(root, rewriter, sourceDoc.get(),output, operator);
		processedOutput = output;
		//if temp_file is created by execution of muse, break it down after use
		File file = new File("test/temp/temp_file.java");
		if (!(file == null)) {
			file.delete();
		}
		File folder = new File("test/temp");
		if (!(file == null)) {
			folder.delete();
		}
		DataLeak.reset(operator);
	}
	
	private void prepare_try_test_files() throws FileNotFoundException, IOException {
		Utility.COUNTER_GLOBAL = 0;
		//Doesn't do anything, since output is already initialized to this value
		//output = new File("test/output/output.txt");
	
		content = FileUtility.readSourceFile("test/input/sample_helloWorld.txt").toString();
		expectedOutput = new File("test/output/sample_hello_world_try_catch_sink.txt");
		
		muse = new Muse();
		root = ASTHelper.getTestingAST(content, Arguments.getRootPath());
	}
	
	private void prepare_try_multi_test_files() throws FileNotFoundException, IOException {
		Utility.COUNTER_GLOBAL = 0;
		//Doesn't do anything, since output is already initialized to this value
		//output = new File("test/output/output.txt");
	
		content = FileUtility.readSourceFile("test/input/sample_multilevelclass.txt").toString();
		expectedOutput = new File("test/output/sample_try_catch_multilevel_class_sink.txt");
		
		muse = new Muse();
		root = ASTHelper.getTestingAST(content, Arguments.getRootPath());
	}

	private void prepare_test_files(OperatorType operator, int test) throws FileNotFoundException, IOException {
		Utility.COUNTER_GLOBAL = 0;
		//Doesn't do anything, since output is already initialized to this value
		//output = new File("test/output/output.txt");
	

		switch (operator) {
		case TAINTSINK:
			// the input for the sink test is the output from the source operator
			// this is because the sink operator relies on sources already being inserted in
			// the code base
			content = FileUtility.readSourceFile("test/input/sample_helloWorld.txt").toString();
			expectedOutput = new File("test/output/sample_hello_world_sink.txt");
			break;

		case REACHABILITY:
			if (test == 1) {
				content = FileUtility.readSourceFile("test/input/sample_helloWorld.txt").toString();
				expectedOutput = new File("test/output/sample_hello_world_reachability.txt");
			}
			else if (test == 2) {
				content = FileUtility.readSourceFile("test/input/sample_helloWorld.txt").toString();
				expectedOutput = new File("test/output/sample_hello_world_try_catch_reachability.txt");
			}
			break;
			
		case COMPLEXREACHABILITY:
			if (test ==1) {
				content = FileUtility.readSourceFile("test/input/sample_helloWorld.txt").toString();
				expectedOutput = new File("test/output/sample_hello_world_complex_reachability.txt");
			}
			else if (test == 2) {
				content = FileUtility.readSourceFile("test/input/sample_helloWorld.txt").toString();
				expectedOutput = new File("test/output/sample_hello_world_try_catch_complex_reachability.txt");
			}
			break;
			
		case TAINTSOURCE:
			content = FileUtility.readSourceFile("test/input/sample_helloWorld.txt").toString();
			expectedOutput = new File("test/output/sample_hello_world_source.txt");
			break;

		case SCOPESOURCE:
			if (test == 1) {
				content = FileUtility.readSourceFile("test/input/sample_helloWorld.txt").toString();
				expectedOutput = new File("test/output/sample_hello_world_taint.txt");
			}
			else if (test == 2) {
				content = FileUtility.readSourceFile("test/input/sample_multilevelclass.txt").toString();
				expectedOutput = new File("test/output/sample_multilevelclass_taint.txt");
			}
			break;
			
		case SCOPESINK:
			content = FileUtility.readSourceFile("test/input/sample_multilevelclass.txt").toString();
			expectedOutput = new File("test/output/sample_multilevelclass_taint_sink.txt");
			break;
			
		case IVH:
			if (test == 1) {
				content = FileUtility.readSourceFile("test/input/sample_class_extended.txt").toString();
				expectedOutput = new File("test/output/sample_class_extended_output.txt");
			}
			else if (test == 2) {
				content = FileUtility.readSourceFile("test/input/sample_class_extended_from_B.txt").toString();
				expectedOutput = new File("test/output/sample_class_extended_from_B_output.txt");
			}
			else if (test == 3) {
				content = FileUtility.readSourceFile("test/input/sample_class_extended_twice.txt").toString();
				expectedOutput = new File("test/output/sample_class_extended_twice_output.txt");
			}
			break;
		}
		
		muse = new Muse();
		root = ASTHelper.getTestingAST(content, Arguments.getRootPath());
	}

		private CompilationUnit getTestAST(String source) {
			
			ASTParser parser = ASTParser.newParser(AST.JLS8);
			Map options = JavaCore.getOptions();
			options.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);
			options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
			options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
			options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);		
			parser.setCompilerOptions(options);

			parser.setSource(source.toCharArray());

			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			parser.setResolveBindings(true);
			parser.setBindingsRecovery(true);

			return (CompilationUnit) parser.createAST(new NullProgressMonitor());
	  } 
}
