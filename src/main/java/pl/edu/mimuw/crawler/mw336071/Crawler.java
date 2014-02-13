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

/**
 * @author Michał Woś mw336071 <br>
 *         projekt na laboratoria Programowanie Obiektowe <br>
 *         MIM Uniwersytet Warszawski maj 2013 <br>
 * <br>
 * 
 *         poniższa klasa implementuje Crawlera odwiedzającego kolejne strony w
 *         drzewie BFS budowanym od zadanego adresu. Jego szczególne przypadki
 *         rozszerzeń znajdują sie w klasach dziedziczących: Demo1, Demo2
 */
public class Crawler {

	/* C O N F I G : */

	// znaczenie pól - patrz komentarz przy ich seterach
	public final int INF = -1;
	private int maxDeph = INF;
	private int maxSitesNumber = INF;
	private int maxRetryNumber = 5;
	private int timeBetweenRetries = 1500; // w ms
	private boolean rememberText = false;

	/**
	 * Ustawia maksymalną dopuszczalną głębokość crawlera. Domyślnie
	 * nieskończoność (-1).
	 * 
	 * @param h
	 *            (int) - maksymalna głębokość
	 */
	public void setMaxDeph(int h) {
		maxDeph = h;
	}

	/**
	 * Ustawia parametr rozwojowy - dopuszczalną maksymalną ilość stron do
	 * przetworzenia. Nie wpływa na działanie programu, dopóki nie zostanie
	 * użyty w implementacji preVisit(..) lub postVisit(..) przez użytkownika.
	 * 
	 * @param n
	 *            - maksymalna ilość stron do przetworzenia (int)
	 */
	public void setMaxSitesNumber(int n) {
		maxSitesNumber = n;
	}

	public int getMaxSitesNumber() {
		return maxSitesNumber;
	}

	/**
	 * Ustawia maksymalną ilość prób pobierania jednej strony w przypadku
	 * niepowodzenia. Domyślna wartość (5)
	 * 
	 * @param maxRetryNumber
	 *            - maksymalną ilość prób (int)
	 */
	public void setMaxRetryNumber(int maxRetryNumber) {
		this.maxRetryNumber = maxRetryNumber;
	}

	/**
	 * Ustawia czas pomiędzy próbami pobrania jednej strony w przypadku
	 * niepowodzenia. Domyślna wartość (1500 ms)
	 * 
	 * @param timeBetweenRetries
	 *            - czas pomiędzy próbami pobrania (int) - w ms
	 */
	public void setTimeBetweenRetries(int timeBetweenRetries) {
		this.timeBetweenRetries = timeBetweenRetries;
	}

	/**
	 * Ustawia, czy strona (Page) po pobraniu treści i przetworzeniu ma zostać
	 * zachowana w pamięci w polu doc (Document) - true;<br>
	 * czy zostać usunięta - false [domyślnie].<br>
	 * Ustawienie true pozwala na dostęp do treści w funkcji postVisit(Page
	 * page) za pomocą odwołania się do page.getDoc().
	 * 
	 * @param b
	 *            - true-pamietaj / false-usun (boolean)
	 */
	public void setRememberText(boolean b) {
		rememberText = b;
	}

	/**
	 * Zwraca stałą (-1) zdefiniowaną jako nieskońoczność w konfiguracji
	 * Crawlera.
	 * 
	 * @return -1 (int)
	 */
	public int infinity() {
		return INF;
	}

	/* S I L N I K */

	private LinkedList<Task> urisQueue = new LinkedList<Task>();
	private HashSet<URI> visitedUris = new HashSet<URI>();

	/**
	 * Uruchamia crawlera
	 * 
	 * @param uri
	 *            (URI) - adres źródłowy
	 * @throws IOException
	 */
	final public void start(URI uri) throws IOException {
		if (!validUri(uri))
			return;
		urisQueue.addLast(new Task(uri, null));

		while (!urisQueue.isEmpty()) {
			Task t = urisQueue.pollFirst();
			visitUri(t.uri, t.parent);
		}
	}

	/**
	 * Uruchamia crawlera
	 * 
	 * @param uri
	 *            (String) - adres źródłowy
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	final public void start(String uri) throws URISyntaxException, IOException {
		start(new URI(uri));
	}

	/**
	 * Funkcja wywoływana dla każdej ścieżki w kolejce, analizująca nowe adresy.<br>
	 * Odpowiednio wywołuje i obsługuje: preVisit(), validUri(), download(),
	 * getUris(), postVisit()
	 * 
	 * @param uri
	 *            - adres stronu do przejrzenia (URI)
	 * @param parent
	 *            - rodzic (Page)
	 * @return <b>true</b> - jesli wywolano dla nowej ścieżki oraz przetworzenie
	 *         strony o tej ścieżce przebiegło pomyślnie<br>
	 *         <b>false</b> - w p.p.
	 * @throws IOException
	 */
	private boolean visitUri(URI uri, Page parent) throws IOException {

		// instrukcje uzytkownika
		preVisit(uri);

		// sprawdzam glebokosc, jesli ustawiona
		int h = (parent != null) ? (parent.getFirstDeph() + 1) : 0;
		if (maxDeph != INF && h > maxDeph)
			return false;

		// sprawdzamy czy ten uri juz nie wystapil - jak tak, nie przetwarzam
		if (visitedUris.contains(uri)) {
			return false;
		}

		// sprawdzamy czy ten uri podoba sie uzytkownikowi
		if (!validUri(uri)) {
			return false;
		}

		// zaznaczam, że odwiedzona
		visitedUris.add(uri);

		Page page = new Page(uri);
		page.setFirstDeph(h);

		// dodaje obecną stronę jako syna strony źródłowej - rozwojowe
		if (parent != null)
			parent.outgoingUris.add(page);

		// obsluga DOWNLOADu:
		boolean fail = true;
		int downloadCounter = 0;

		while (maxRetryNumber == -1
				|| (downloadCounter < maxRetryNumber && fail)) {
			try {
				fail = false;
				download(page);
			} catch (UnknownHostException e) {
				fail = true;
				try {
					log("Problem z połączeniem podczas pobierania: "
							+ page.getUri().toString() + ". Oczekiwanie na "
							+ (downloadCounter + 1) + " próbę pobrania..");

					Thread.sleep(timeBetweenRetries);
				} catch (InterruptedException e1) {
					log("Proba zatrzymania zatrzymanego wczesniej wątku.");
					// to nie powinno sie zdarzyc, chyba ze zmienie koncepcje na
					// wielowątkową
				}
			} catch (java.net.ConnectException e) {
				// TODO remove redundation of code
				fail = true;
				try {
					log("Problem z połączeniem podczas pobierania: "
							+ page.getUri().toString() + ". Oczekiwanie na "
							+ (downloadCounter + 1) + " próbę pobrania..");

					Thread.sleep(timeBetweenRetries);
				} catch (InterruptedException e1) {
					log("Proba zatrzymania zatrzymanego wczesniej wątku.");
				}
			} catch (IllegalArgumentException e) {
				log("Nie przetworzono strony: " + uri.toString() + ": "
						+ e.toString());
				// fail = true;
				return false;
			} catch (IOException e) {
				log("Nie przetworzono strony: " + uri.toString() + ": "
						+ e.toString());
				return false;
			}

			downloadCounter++;
		}

		if (downloadCounter == maxRetryNumber && fail) {
			log("Nie pobrano strony: " + uri.toString()
					+ ". Limit czasu przekroczony.");
			return false;
		}
		// END OF DOWNLOAD

		// pobieram wszystkie odnosniki z bierzącej strony i kolejkuję je
		Vector<URI> uris = getUris(page);

		for (URI child : uris)
			urisQueue.addLast(new Task(child, page));

		// instrukcje uzytkownika
		postVisit(page);

		if (!rememberText) {
			// zwalniam pamiec z treścią strony
			page.getDoc().empty();
		}

		return true;
	}

	/**
	 * Znajduje wszystko pomiedzy znacznikami < a >. Nie waliduje preferencjami
	 * uzytkownika. Sprawdza poprawnosc URI.
	 * 
	 * @param page
	 *            (Page) - strona do analizy
	 * @return Vector < URI > - Vecto#r wszystkich znalezionych odnosnikow na
	 *         stronie
	 */
	public Vector<URI> getUris(Page page) {
		Elements anchors = page.getDoc().select("a[href]");
		Vector<URI> ret = new Vector<URI>();
		String uriString;

		for (Element link : anchors) {
			try {
				uriString = link.absUrl("href"); // link.attr("href")
				ret.add(new URI(uriString));
			} catch (URISyntaxException e) {
				log("Niepoprawny format URI na: " + page.getUri() + ":"
						+ e.getMessage());
			}
		}

		return ret;
	}

	/* DO R E I M P L E M E N T A C J I W CELU ROZSZERZANIA: */

	/**
	 * Domyslnie pobiera strony internetowe.<br>
	 * Do opcjonalnej reimplementacji w klasie dziedziczącej w celu uzyskania
	 * innego sposobu pobierania.<br>
	 * <br>
	 * Pobiera strone o ścieżce: page.getURI(). Pobraną treść w formacie <a
	 * href=
	 * "http://jsoup.org/apidocs/org/jsoup/nodes/Document.html">Document</a>
	 * zapisuje w page używając page.setDoc( treść ).
	 * 
	 * @param page
	 *            - strona do pobrania (Page)
	 * @throws IllegalArgumentException
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public void download(Page page) throws IllegalArgumentException,
			UnknownHostException, IOException {
		String uri = page.getUri().toString();
		page.setDoc(Jsoup.connect(uri).get());
	}

	/**
	 * Zwraca czy dodac dany URI do kolejki stron do pobrania.<br>
	 * Domyślnie true - kolejkuje wszystkie.<br>
	 * <br>
	 * Do opcjonalnej reimplementacji w klasie dziedziczącej.
	 * 
	 * @param uri
	 *            (URI) - ścieżka do walidacji
	 * @return true-poprawny adres do przetworzenia / false - niepoprawny
	 *         (boolean)
	 */
	public boolean validUri(URI uri) {
		return true;
	}

	/**
	 * Metoda wywoływana przed analizowaniem danej strony podczas chodzenia po
	 * grafie stron (zostanie wywołana dla każdej strony w kolejce stron do
	 * przejrzenia). Domyślnie nie robi nic.<br>
	 * Do opcjonalnej reimplementacji w podklasach w celu rozszerzenia
	 * funkcjonalności Crawlera.
	 * 
	 * @param uri
	 *            - adres strony (URI)
	 */
	public void preVisit(URI uri) {
	}

	/**
	 * Metoda wywoływana po przeanalizowaniu danej strony (zostanie wywołana raz
	 * dla każdej przetworzonej poprawnie strony). Domyślnie nie robi nic.<br>
	 * Do opcjonalnej reimplementacji w podklasach w celu rozszerzenia
	 * funkcjonalności Crawlera.<br>
	 * <br>
	 * <i>Przykład: odwołując się wewnątrz tej metody do page.getDoc()
	 * uzyskujemy jeszcze przechowywaną w pamięci treść strony, którą możemy
	 * wedle uznania zapisać do pliku, bądź przetworzyć w dowolny sposób.</i>
	 * 
	 * @param page
	 *            - przetworzona strona (Page)
	 */
	public void postVisit(Page page) {
	}

	/**
	 * Sposób wypisywania błędów i komunikatow do urzytkownika. <br>
	 * Domyślnie: standardowe wyjście (konsola). Do reimplementacji w klasie
	 * dziedziczącej w razie potrzeby
	 * 
	 * @param monit
	 *            - wiadomosc do logu (String)
	 */
	public void log(String monit) {
		System.out.println(monit);
	}

	@SuppressWarnings("unused")
	private void deb(String monit) {
		log(monit);
	}

	@SuppressWarnings("unused")
	private void deb(int number) {
		log("" + number);
	}
}
