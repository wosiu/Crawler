package pl.edu.mimuw.crawler.mw336071;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Vector;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Crawler /*implements Runnable*/ {

	/* S I L N I K */
	
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
	final public void start( URI uri ) {
		
		if ( !validUri( uri ) ) return;
		urisQueue.addLast( new Task( uri, null ) );
		
		while ( ! urisQueue.isEmpty() ) {
			Task t = urisQueue.peekFirst();
			visitUri( t.uri, t.parent );
		}		
	}
	
	//TO DO: public czy protected?
	//rzucam, a nie przechwytuje, zeby uzytkownik mogl zdecydowac 
	//moze na przyklad bedzie chcial ponownie wczytywac w takim przypadku 
	//ze standardowego wejscia, kto go tam wie..
	final public void start( String uri ) throws URISyntaxException {
		start( new URI( uri ) );
	}


	private boolean visitUri( URI uri, Page parent ) {
		//instrukcje uzytkownika
		preVisit( uri );
		
		int h = parent.getFirstDeph() + 1;
		if ( maxDeph != inf && h > maxDeph ) return false;
		
		//sprawdzamy czy ten uri juz nie wystapil - jak tak, nie przetwarzam
		if( visitedUris.contains( uri ) ) return false;

		//sprawdzamy czy ten uri podoba sie uzytkownikowi
		if( !validUri( uri ) ) return false;
		
		Page page = new Page( uri );
		page.setFirstDeph(h);
		parent.outgoingUris.add( page );
		

		// DOWNLOAD:
		boolean fail = true;
		int downloadIterator = 0;
				
		while ( maxRetryNumber == - 1 || (downloadIterator < maxRetryNumber && fail) )
		{
			try {
				fail = false;
				download( page );
			} catch (UnknownHostException e) {
				fail = true;
				try { 
					log("Problem z połączeniem podczas pobierania: " 
							+ page.getUri().toString() + ". Oczekiwanie na " 
							+ (downloadIterator+1) + " próbę pobrania..");
					
					Thread.sleep( timeBetweenRetries );
				} catch (InterruptedException e1) {
					log("Proba zatrzymania zatrzymanego wczesniej wątku.");
					//to nie powinno sie zdarzyc, chyba ze zmienie koncepcje na wielowątkową
				}
			} catch ( IllegalArgumentException | IOException e ) {
				log( "Nie pobrano strony: " + e.getMessage() );
				return false;
			}
			
			downloadIterator++;
		}
		
		if( downloadIterator == maxRetryNumber && fail ) 
		{
			log( "Nie pobrano strony: " + uri.toString() + ". Limit czasu przekroczony." );
			return false;	
		}
		// END OF DOWNLOAD
		
		
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
	
	//znajduje wszystko pomiedzy znacznikami <a> 
	//nie waliduje preferencjami uzytkownika
	//sprawdza poprawnosc URI
	private Vector<URI> getUris( Page page ) 
	{
		Elements anchors = page.getDoc().select("a[href]");
        Vector<URI>ret = new Vector<URI>();
        
		for ( Element link : anchors )
		{
            try {
				ret.add( new URI( link.attr("href") ) );
			} catch (URISyntaxException e) {
				//TO DO: sprawdzic czy domyslne Uri().toString dziala pieknie
				log("Niepoprawny format URI na: " + page.getUri() + ":" + e.getMessage() );
			}
		}
		return ret;
	}
	
	
	/* C O N F I G : */
	public final int inf = -1;
	private int maxDeph = inf;
	@SuppressWarnings("unused")
	private int maxSitesNumber = inf;
	private int maxRetryNumber = 5; 
	private int timeBetweenRetries = 1500; //w ms 
	private boolean rememberText = false;	
	
	
	public void setMaxDeph( int h ) {
		maxDeph = h;
	}
	public void setPagesNumber( int n ) {
		maxSitesNumber = n;
	}
	public void setMaxRetryNumber(int maxRetryNumber) {
		this.maxRetryNumber = maxRetryNumber;
	}
	public void setTimeBetweenRetries(int timeBetweenRetries) {
		this.timeBetweenRetries = timeBetweenRetries;
	}
	public void setRememberText( boolean b ) {
		rememberText = b;
	}
	public int infinity() {
		return inf;
	}
	
	
	/* DO   R E I M P L E M E N T A C J I   W CELU ROZSZERZANIA: */
	
	public void download( Page page ) throws IllegalArgumentException, 
										UnknownHostException, IOException 
	{
		//domyslnie pobiera strony internetowe
		//do reimplementacji jeśli chcemy inny sposób pobierania:
		String uri = page.getUri().toString();
		page.setDoc( Jsoup.connect( uri ).get() );
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
	
	//sposób wypisywania błędów i komunikatow do urzytkownika:
	public void log( String monit )
	{
		//do reimplementacji w razie potrzeby
		System.out.println( monit );
	}
}
