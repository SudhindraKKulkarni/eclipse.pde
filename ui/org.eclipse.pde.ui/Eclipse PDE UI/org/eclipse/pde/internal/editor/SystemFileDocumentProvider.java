package org.eclipse.pde.internal.editor;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.pde.internal.editor.text.*;
import java.io.*;
import org.eclipse.jface.text.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.text.source.*;
import org.eclipse.ui.editors.text.*;
import org.eclipse.ui.texteditor.*;
import org.eclipse.pde.internal.PDEPlugin;

public class SystemFileDocumentProvider extends AbstractDocumentProvider {
	private IDocumentPartitioner partitioner;

public SystemFileDocumentProvider(IDocumentPartitioner partitioner) {
	this.partitioner = partitioner;
}
protected IAnnotationModel createAnnotationModel(Object element) throws CoreException {
	return null;
}
protected IDocument createDocument(Object element) throws CoreException {
	if (element instanceof SystemFileEditorInput) {
		Document document = new Document();
		if (partitioner != null) {
			partitioner.connect(document);
			document.setDocumentPartitioner(partitioner);
		}
		File file = (File)((SystemFileEditorInput)element).getAdapter(File.class);
		setDocumentContent(document, file);
		return document;
	}
	return null;
}
protected void doSaveDocument(IProgressMonitor monitor, Object element, IDocument document) throws CoreException {}
protected void setDocumentContent(IDocument document, File file) {
	try {
		InputStream contentStream = new FileInputStream(file);
		Reader in = new InputStreamReader(contentStream);
		int chunkSize = contentStream.available();
		StringBuffer buffer = new StringBuffer(chunkSize);
		char[] readBuffer = new char[chunkSize];
		int n = in.read(readBuffer);
		while (n > 0) {
			buffer.append(readBuffer);
			n = in.read(readBuffer);
		}
		in.close();
		document.set(buffer.toString());

	} catch (IOException e) {
		PDEPlugin.logException(e);
	}
}
}
