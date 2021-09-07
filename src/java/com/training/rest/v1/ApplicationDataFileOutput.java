package com.training.rest.v1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.apache.xmlbeans.impl.common.IOUtil;

public class ApplicationDataFileOutput implements StreamingOutput {
	private InputStream is;
	private String mimeType;

	public ApplicationDataFileOutput(InputStream is, String mimeType) {
		this.is = is;
		this.mimeType = mimeType;
	}

	@Override
	public void write(OutputStream outPutStream) throws IOException, WebApplicationException {
		IOUtil.copyCompletely(is, outPutStream);
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
}
