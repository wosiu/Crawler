package pl.edu.mimuw.crawler.mw336071;

import java.net.URI;
import java.util.Vector;

import org.jsoup.nodes.Document;

public class Page {
	
	private final URI uri;
	private Document doc; //tresc strony po pobraniu
	private int pid; //id strony - rozwojowe (np do budowania grafu)
	private int firstDeph; //glebokosc pierwszego wystapienia
	public Vector <Page> outgoingUris = new Vector <Page>(); //wskaźniki na strony mające uri na this
	
	public Page( URI uri ) {
		this.uri = uri;
		pid = -1; //unvisited
	}
	
	/**
	 * @return ścieżka strony (URI)
	 */
	public URI getUri() {
		return uri;
	}
	/**
	 * @return id nadane stronie przez setPid(int) - opcjonalne
	 */
	public int getPid() {
		return pid;
	}
	/**
	 * Ustawia id strony
	 * @param pid
	 */
	public void setPid(int pid) {
		this.pid = pid;
	}
	/**
	 * @return głebokość pierwszego wystąpienia strony (int)
	 */
	public int getFirstDeph() {
		return firstDeph;
	}
	/**
	 * Ustawia glebokosc pierwszego wystapienia
	 * @param firstDeph
	 */
	public void setFirstDeph(int firstDeph) {
		this.firstDeph = firstDeph;
	}
	/**
	 * Zwraca tresc w formacie <a href="http://jsoup.org/apidocs/org/jsoup/nodes/Document.html">Document</a>
	 * @return <a href="http://jsoup.org/apidocs/org/jsoup/nodes/Document.html">Document</a>
	 */
	public Document getDoc() {
		return doc;
	}
	/**
	 * Ustawia pobraną zawartość w formacie <a href="http://jsoup.org/apidocs/org/jsoup/nodes/Document.html">Document</a>
	 * @param doc
	 */
	public void setDoc(Document doc) {
		this.doc = doc;
	}
}
