package pl.edu.mimuw.crawler.mw336071;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @author Michał Woś mw336071 <br>
 * projekt na laboratoria Programowanie Obiektowe <br>
 * MIM Uniwersytet Warszawski maj 2013 <br><br>
 *
 * poniższa klasa implementuje Crawlera odwiedzającego kolejne strony w drzewie BFS
 * budowanym od zadanego adresu. Jego szczególne przypadki rozszerzeń znajdują sie 
 * w klasach dziedziczących: Demo1, Demo2
 */
public class Crawler  {

	
	/* W Ą T K I */
	class myThread implements Runnable {
		Task task;
		
		public myThread ( Task task ) {
			this.task = task;
		}

		public void run() {
			if( task == null ) { log("Wątek: puste polecenie."); return; }
			
			visitUri( task.uri, task.parent );
		}
	} 
	
	/* S I L N I K */	
	//private LinkedList < Task  > urisQueue = new LinkedList < Task > ();
	private BlockingQueue < Task  > urisQueue = new LinkedBlockingQueue < Task > ();
	//private HashSet < URI > visitedUris = new HashSet < URI > ();
	private Set < URI > visitedUris = Collections.synchronizedSet(new HashSet< URI > ());
	
	/**
	 * Uruchamia crawlera
	 * @param uri (URI) - adres źródłowy
	 */
	final public void start( URI uri ) {
		if ( !validUri( uri ) ) return;
		
		try {
			urisQueue.put( new Task( uri, null ) );
		} catch (InterruptedException e1) {
			log( "start(): Ponowne uruchomienie. ");
			start( uri );
		}
		
		//TO DO: && (maxSitesNumber==-1 || maxSitesNumber>0 ) albo globalny booblean stop
		do{
			//deb( Thread.activeCount() );
			Task t = null;
			
			try {
				t = urisQueue.poll(1500, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) { 
				//to sie nie powinno zdarzyc
				log("start(): while: Blad podczas przetwarzania."); 
			}
			
			if( t == null ) continue;
			
			if(  Thread.activeCount() < 4 ) //TO DO: zmienic spowrotem wieksze
			{
				Runnable obiekt = new myThread( t );
				(new Thread(obiekt)).start();
			}
			else
			{
				visitUri( t.uri, t.parent );
			}
			
		} while ( Thread.activeCount() > 1 || ! urisQueue.isEmpty() ); 
		
		//deb( "Thread.activeCount(): " + Thread.activeCount()  );
	}
	
	/**
	 * Uruchamia crawlera
	 * @param uri (String) - adres źródłowy
	 * @throws URISyntaxException
	 */
	final public void start( String uri ) throws URISyntaxException {
		start( new URI( uri ) );
	}

	
	synchronized private boolean isVisited( URI uri ) {
		//sprawdzamy czy ten uri juz nie wystapil - jak tak, nie przetwarzam
		if ( visitedUris.contains( uri ) ) {return true;}
		
		visitedUris.add( uri );
		return false;
	}
	
	/**
	 * Funkcja wywoływana dla każdej ścieżki w kolejce, analizująca nowe adresy.<br> 
	 * Odpowiednio wywołuje i obsługuje: 
	 * preVisit(), validUri(), download(), getUris(), postVisit()
	 * @param uri - adres stronu do przejrzenia (URI)
	 * @param parent - rodzic (Page)
	 * @return <b>true</b> - jesli wywolano dla nowej ścieżki oraz przetworzenie strony o tej ścieżce przebiegło pomyślnie<br>
	 * <b>false</b> - w p.p.
	 */
	
	//TODO: parent.outgoings - concurrent
	
	/*synchronized*/ private boolean visitUri( URI uri, Page parent ) {
		
		//instrukcje uzytkownika
		preVisit( uri );
		
		if ( isVisited(uri) ) return false;
		
		//sprawdzam glebokosc, jesli ustawiona
		int h = (parent != null) ? ( parent.getFirstDeph() + 1 ) : 0;
		
		if ( maxDeph != inf && h > maxDeph ) return false;
		
		//sprawdzamy czy ten uri juz nie wystapil - jak tak, nie przetwarzam
		//if ( visitedUris.contains( uri ) ) {return false;}
		//to
		
		//sprawdzamy czy ten uri podoba sie uzytkownikowi
		if( !validUri( uri ) ) {return false;}
		
		//zaznaczam, że odwiedzona
		//visitedUris.add( uri );
		//i to dac razem do zewnetrznej synchronized i wywolywac po previsit
		
		Page page = new Page( uri );
		page.setFirstDeph(h);
		
		//dodaje obecną stronę jako syna strony źródłowej - rozwojowe
		if( parent != null ) parent.outgoingUris.add( page );

		// obsluga DOWNLOADu:
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
				log( "Nie przetworzono strony: " + uri.toString() + ": " + e.toString() );
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
		
		//pobieram wszystkie odnosniki z bierzącej strony i kolejkuję je
		Vector<URI> uris = getUris( page );
		
		for ( int i = 0; i < uris.size(); i++ )
			try {
				urisQueue.put( new Task( uris.get(i), page) );
			} catch (InterruptedException e) {
				log("visitUrl(): uris do urisQueue: powtorzenie wlozenia.");
				i--;
			}
			
		//instrukcje uzytkownika
		postVisit( page );
		
		if ( !rememberText )
		{
			//zwalniam pamiec z treścią strony
			page.getDoc().empty();
		}

		return true;

	}
	
	/**
	 * Znajduje wszystko pomiedzy znacznikami < a >. 
	 * Nie waliduje preferencjami uzytkownika. Sprawdza poprawnosc URI.
	 * 
	 * @param page (Page) - strona do analizy
	 * @return Vector < URI > - Vector wszystkich znalezionych odnosnikow na stronie
	 */
	private Vector<URI> getUris( Page page ) 
	{
		Elements anchors = page.getDoc().select("a[href]");
        Vector<URI>ret = new Vector<URI>();
        String uriString;
        
		for ( Element link : anchors )
		{
            try {
            	uriString = link.absUrl("href"); //link.attr("href")
            	ret.add( new URI( uriString ) );
			} catch (URISyntaxException e) {
				log("Niepoprawny format URI na: " + page.getUri() + ":" + e.getMessage() );
			}
		}
		
		return ret;
	}
	
	
	/* C O N F I G : */
	public final int inf = -1;
	private int maxDeph = inf;
	private int maxSitesNumber = inf;
	private int maxRetryNumber = 5; 
	private int timeBetweenRetries = 1500; //w ms 
	private boolean rememberText = false;	
	
	/**
	 * Ustawia maksymalną dopuszczalną głębokość crawlera. Domyślnie nieskończoność (-1).
	 * @param h (int) - maksymalna głębokość
	 */
	public void setMaxDeph( int h ) {
		maxDeph = h;
	}
	/**
	 * Ustawia parametr rozwojowu - dopuszczalną maksymalną ilość stron do przetworzenia.
	 * Nie wpływa na działanie programu, dopóki nie zostanie użyty w implementacji
	 * preVisit(..) lub postVisit(..) przez użytkownika.
	 * @param n - maksymalna ilość stron do przetworzenia (int) 
	 */
	public void setMaxSitesNumber( int n ) {
		maxSitesNumber = n;
	}
	public int getMaxSitesNumber() {
		return maxSitesNumber;
	}
	/**
	 * Ustawia maksymalną ilość prób pobierania jednej strony w przypadku niepowodzenia.
	 * Domyślna wartość (5)
	 * @param maxRetryNumber - maksymalną ilość prób (int)
	 */
	public void setMaxRetryNumber(int maxRetryNumber) {
		this.maxRetryNumber = maxRetryNumber;
	}
	/**
	 * Ustawia czas pomiędzy próbami pobrania jednej strony w przypadku niepowodzenia.
	 * Domyślna wartość (1500 ms)
	 * @param timeBetweenRetries - czas pomiędzy próbami pobrania (int) - w ms
	 */
	public void setTimeBetweenRetries(int timeBetweenRetries) {
		this.timeBetweenRetries = timeBetweenRetries;
	}
	/**
	 * Ustawia, czy strona (Page) po pobraniu treści i przetworzeniu
	 * ma zostać zachowana w pamięci w polu doc (Document) - true;<br>
	 * czy zostać usunięta - false [domyślnie].<br> 
	 * Ustawienie true pozwala na dostęp do treści w funkcji postVisit(Page page) 
	 * za pomocą odwołania się do page.getDoc(). 
	 * @param b - true-pamietaj / false-usun (boolean)
	 */
	public void setRememberText( boolean b ) {
		rememberText = b;
	}
	/**
	 * Zwraca stałą (-1) zdefiniowaną jako nieskońoczność w konfiguracji Crawlera.
	 * @return -1 (int)
	 */
	public int infinity() {
		return inf;
	}
	
	
	/* DO   R E I M P L E M E N T A C J I   W CELU ROZSZERZANIA: */
	
	/**
	 * Domyslnie pobiera strony internetowe.<br>
	 * Do opcjonalnej reimplementacji w klasie dziedziczącej w celu uzyskania innego 
	 * sposobu pobierania.<br>
	 * <br>
	 * Pobiera strone o ścieżce: page.getURI().
	 * Pobraną treść w formacie <a href="http://jsoup.org/apidocs/org/jsoup/nodes/Document.html">Document</a>
	 * zapisuje w page używając page.setDoc( treść ).
	 * 
	 * @param page - strona do pobrania (Page)
	 * @throws IllegalArgumentException
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public void download( Page page ) throws IllegalArgumentException, 
										UnknownHostException, IOException 
	{
		String uri = page.getUri().toString();
		page.setDoc( Jsoup.connect( uri ).get() );
	}	
	
	/**
	 * Zwraca czy dodac dany URI do kolejki stron do pobrania.<br>
	 * Domyślnie true - kolejkuje wszystkie.<br>
	 * <br>
	 * Do opcjonalnej reimplementacji w klasie dziedziczącej.
	 * @param uri (URI) - ścieżka do walidacji
	 * @return true-poprawny adres do przetworzenia / false - niepoprawny (boolean)
	 */
	public boolean validUri( URI uri ) {
		return true;
	}

	/**
	 * Metoda wywoływana przed analizowaniem danej strony podczas chodzenia po
	 * grafie stron (zostanie wywołana dla każdej strony w kolejce stron do przejrzenia). 
	 * Domyślnie nie robi nic.<br>
	 * Do opcjonalnej reimplementacji w podklasach w celu rozszerzenia funkcjonalności
	 * Crawlera.
	 * @param uri - adres strony (URI)
	 */
	synchronized public void preVisit( URI uri ) {
	}

	/**
	 * Metoda wywoływana po przeanalizowaniu danej strony 
	 * (zostanie wywołana raz dla każdej przetworzonej poprawnie strony).
	 * Domyślnie nie robi nic.<br>
	 * Do opcjonalnej reimplementacji w podklasach w celu rozszerzenia funkcjonalności
	 * Crawlera.<br><br>
	 * <i>Przykład: odwołując się wewnątrz tej metody do page.getDoc() uzyskujemy 
	 * jeszcze przechowywaną w pamięci treść strony, którą możemy wedle uznania 
	 * zapisać do pliku, bądź przetworzyć w dowolny sposób.</i>
	 * @param page - przetworzona strona (Page)
	 */
	synchronized public void postVisit( Page page ) {		
	} 
	
	/**
	 * Sposób wypisywania błędów i komunikatow do urzytkownika. <br>
	 * Domyślnie: standardowe wyjście (konsola). 
	 * Do reimplementacji w klasie dziedziczącej w razie potrzeby
	 * @param monit - wiadomosc do logu (String)
	 */
	public void log( String monit ) {
		System.out.println( monit );
	}
	
	@SuppressWarnings("unused")
	private void deb( String monit ) {
		System.out.println( monit );
	}
	//@SuppressWarnings("unused")
	private void deb( int number ) {
		System.out.println( ""+number );
	}
}
