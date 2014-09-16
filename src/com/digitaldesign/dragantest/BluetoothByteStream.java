package com.digitaldesign.dragantest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.digitaldesign.dragantest.interfaces.OnReceiveValue;

public class BluetoothByteStream {
	ArrayList<Byte> byteStream = new ArrayList<Byte>();
	private OnReceiveValue onReceiveValueCallback = null;
	public static final int X0 = 11;
	

	public void registerValueCallback(OnReceiveValue obj){
		onReceiveValueCallback = obj;
	}
	public void clearValueCallback(){
		onReceiveValueCallback = null;
	}
	
	
	public BluetoothByteStream(OnReceiveValue callBack){
		this.onReceiveValueCallback = callBack;
	}
	
	private void sendMessage(byte[] x){
		byte y = x[1];
		onReceiveValueCallback.onValue((int) y);
		
	}

	private static byte[] convertBytes(List<Byte> bytes)
	{
	    byte[] ret = new byte[bytes.size()];
	    Iterator<Byte> iterator = bytes.iterator();
	    for (int i = 0; i < ret.length; i++)
	    {
	        ret[i] = iterator.next();
	    }
	    return ret;
	}
	
	public void addByte(byte inputByte){
		appendByteToArray(inputByte);
	}
	
	public void addByte(byte[] inputBytes){
		for(byte b : inputBytes){
			appendByteToArray(b);
		}
	}
	
	public void addByte(Short inputByte){
		appendByteToArray(inputByte.byteValue());
	}
	
	private void appendByteToArray(byte inputByte){
		//byteStream.size();  // Broj koliko ima bajtova trenutno

		byteStream.add(inputByte);
		
		if(byteStream.size() == 12){ // USLOV DA JE KRAJ //
			sendMessage(convertBytes(byteStream)); // Salje se poruka 
			byteStream = new ArrayList<Byte>();  // Brise se stari
		}
	}
	


}
