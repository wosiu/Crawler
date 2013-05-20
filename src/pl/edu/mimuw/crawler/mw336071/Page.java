package pl.edu.mimuw.crawler.mw336071;

import java.net.URI;
import java.util.Vector;

import org.jsoup.nodes.Document;

public class Page {
	
	private final URI uri;
	//private String text;
	private Document doc;
	private int pid;
	private int firstDeph; //glebokosc pierwszego wystapienia
	public Vector <Page> outgoingUris; //wskaźniki na strony mające uri na this
	
	public Page( URI uri ) {
		this.uri = uri;
		pid = -1; //unvisited
	}
	
	public URI getUri() {
		return uri;
	}
	/*public void setUrl(String uri) {
		this.url = uri;
	}*/
	/*public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}*/
	public int getPid() {
		return pid;
	}
	public void setPid(int pid) {
		this.pid = pid;
	}
	public int getFirstDeph() {
		return firstDeph;
	}
	public void setFirstDeph(int firstDeph) {
		this.firstDeph = firstDeph;
	}
	public Document getDoc() {
		return doc;
	}
	public void setDoc(Document doc) {
		this.doc = doc;
	}
}
