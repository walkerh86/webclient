package com.hcj.webclient.net;

import java.io.InputStream;

public class TStatusResponse extends Response<String>{
	public TStatusResponse(ResponseHandler handler,int step){
		super(handler,step);
	}
		
	@Override
	protected String parseContent(InputStream is){
		return null;
	}
}
