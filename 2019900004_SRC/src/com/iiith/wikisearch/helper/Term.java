package com.iiith.wikisearch.helper;

/*
 * Arjun Gambhir
 * 
*/

public class Term {

	private Byte b;
	private int termFrequency;

	public Term(Byte b, int termFrequency) {
		this.b = b;
		this.termFrequency = termFrequency;
	}

	public Byte getB() {
		return b;
	}

	public void setB(Byte b) {
		this.b = b;
	}

	public int getTermFrequency() {
		return termFrequency;
	}

	public void setTermFrequency(int termFrequency) {
		this.termFrequency = termFrequency;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(b);
		str.append('$');
		str.append(termFrequency);
		return new String(str);
	}
}
