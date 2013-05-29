package pl.edu.mimuw.crawler.mw336071;

import java.net.URI;

/**
 * Jednostka polecenia dla visitURL()
 * @author m
 * @param uri (URI) - adres do odwiedzenia
 * @param parent (Page) - ojcec
 */
class Task {
	URI uri;
	Page parent;
	
	Task( URI uri, Page parent ) {
		this.uri = uri;
		this.parent = parent; 
	}
	
	@Override
	public String toString() {
		return "["+uri.toString()+" from: " + 
				( (parent!=null) ? parent.getUri().toString() : "null" )
		+ "]\n";
	}
}	

