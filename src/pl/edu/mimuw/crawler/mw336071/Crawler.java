package pl.edu.mimuw.crawler.mw336071;

import java.net.URI;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Vector;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Crawler /*implements Runnable*/ {

	//jednostka polecenia do odwiedzenia
	private class Task {
		URI uri;
		Page parent;
		
		Task( URI uri, Page parent ) {
			this.uri = uri;
			//ojciec przydatny, bo np mam w nim jego głębokość
			this.parent = parent; 
		}
	}	
	
	private LinkedList < Task  > urisQueue = new LinkedList < Task > ();
	private HashSet < URI > visitedUris = new HashSet < URI > ();

	
	//Pytanie: co zrobic, zeby ta metoda byla widoczna do odpalenia dla dziedzi
	//czacyc po niej, ale zebynie mozna bylo ja z override'owac ?
	public void start( URI uri ) {
		
		if ( !validUri( uri ) ) return;
		urisQueue.addLast( new Task( uri, null ) );
		
		while ( ! urisQueue.isEmpty() ) {
			Task t = urisQueue.peekFirst();
			visitUri( t.uri, t.parent );
		}		
	}
	
	public void start( String uri ) {
		start( new URI( uri ) );
	}


	private boolean visitUri( URI uri, Page parent )
	{
		//instrukcje uzytkownika
		preVisit( uri );
		
		int h = parent.getFirstDeph() + 1;
		if ( maxDeph != -1 && h > maxDeph ) return false;
		
		//sprawdzamy czy ten uri juz nie wystapil - jak tak, nie przetwarzam
		if( visitedUris.contains( uri ) ) return false;

		//sprawdzamy czy ten uri podoba sie uzytkownikowi
		if( !validUri( uri ) ) return false;
		
		Page page = new Page( uri );
		page.setFirstDeph(h);
		parent.outgoingUris.add( page );
		
		//page.download(); //pytanie czy page, bo ona nie widzi configu crawlera
		download( page ); //try catch

		Vector<URI> uris = getUris( page );
		
		for ( URI child : uris  )
			urisQueue.addLast( new Task(child, page) );
			
		postVisit( page );
		
		if ( !rememberText )
		{
			//page.setText( null );
			page.getDoc().remove();
		}
		
		return true;
	}
	
	//do reimplementacji jeśli chcemy inny sposób pobierania:
	public void download( Page page ) {
		String uri = page.getUri().toString();
		page.setDoc( Jsoup.connect( uri ).get() );
	}	
	
	//znajduje wszystko pomiedzy znacznikami <a> - nie sprawdza poprawnosci
	private Vector<URI> getUris( Page page ) 
	{
		Elements anchors = page.getDoc().select("a[href]");
        Vector<URI>ret = new Vector<URI>();
		
		for ( Element link : anchors ) 
             ret.add( new URI( link.attr("href") ) );
		
		return ret;
	}
	
	/* C O N F I G : */
	private int maxDeph = -1;
	private int maxPagesNumber = -1;
	private boolean rememberText = false;	
	/* END OF CONFIG */
	
	public void setMaxDeph( int h ) {
		maxDeph = h;
	}
	public void setPagesNumber( int n ) {
		maxPagesNumber = n;
	}
	public void setRememberText( boolean b ) {
		rememberText = b;
	}
	
	//zwraca czy dodac dany URI do kolejki stron do pobrania
	public boolean validUri( URI uri ) {
		//domyslnie kolejkuje wszystkie 
		//do reimplementacji w podklasach
		return true;
	}

	public void preVisit( URI uri ) {
		//odpalana przed analizowaniem strony
		//do reimplementacji w podklasach	
	}

	public void postVisit( Page page ) {
		//odpalana po analizowaniu strony
		//do reimplementacji w podklasach			
	} 
}
