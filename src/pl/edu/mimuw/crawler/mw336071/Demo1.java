package pl.edu.mimuw.crawler.mw336071;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Michał Woś mw336071 <br>
 * projekt na laboratoria Programowanie Obiektowe <br>
 * MIM Uniwersytet Warszawski maj 2013 <br>
 */
public class Demo1 extends Crawler {
	
	//znalezione strony zewnetrzne i ilosc wystapien (key - url, value - liniczk):
	private ConcurrentHashMap <String, Integer> visitedDomains = new ConcurrentHashMap <String, Integer >();
	private URI sourceURI;
	
	/* R O Z S Z E R Z E N I E  C R A W L E R A */
	@Override
	public boolean validUri( URI uri ) {
		if ( uri == null || uri.getAuthority() == null ) return false;
		return sourceURI.getAuthority().equals( uri.getAuthority() );
	}
	
	@Override
	synchronized public void preVisit( URI url ) {
		//sprawdzamy czy URL jest spoza domeny
		if ( url.getAuthority() != null && !validUri( url ) ) 
		{
			String key = url.getAuthority().toString();
			Integer amount = visitedDomains.get( key );
			
			if ( amount == null ) amount = 0;
			
			visitedDomains.put( key, amount + 1 );
		}
	}
	
	
	/* komperator do  P O S O R T O W A N I A   W Y N I K U */
	private class PairComparator implements Comparator< Entry<String, Integer> > {
	    @Override
	    public int compare( Entry<String, Integer> o1, Entry<String, Integer> o2) {
	    	if( o2.getValue() == o1.getValue() )
	    		return o1.getKey().compareTo( o2.getKey() ) ;
	    	
	        return o2.getValue().compareTo( o1.getValue() );
	    }
	}
	private PairComparator entryCmp = new PairComparator();
	
	public FileOutputStream fos = null;
	
	@Override
	public void log( String monit ) {
		   for(int i = 0; i < monit.length(); i++){
			     try {
					fos.write((int)monit.charAt(i));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} //Zapis bajt po bajcie każdego znaku...
			   }
	}
	

	/* M A I N  */
	public static void main(String[] args) {

		Demo1 mycrawler = new Demo1(); 
		
		try {
			mycrawler.fos = new FileOutputStream("mojerror.log");
		} catch (FileNotFoundException e1) {
			System.out.println("Nie udalo sie stworzyc pliku z logiem");
		}
		
		try {
			/* inicjacja */
			mycrawler.sourceURI = new URI(args[0]);
			mycrawler.start( mycrawler.sourceURI );
			
			/* posortowanie wyniku */
			Iterator<Entry<String, Integer>> it = 
					mycrawler.visitedDomains.entrySet().iterator();
			List< Entry<String,Integer> > sorted = 
					new ArrayList< Entry<String,Integer> >();
		    
			//przepisuję mapę do listy
			while (it.hasNext()) sorted.add( it.next() );
		    
			//nastepnie sortuje wlasnym comperatorem uwzgledniajac tresc zadania
			Collections.sort( sorted, mycrawler.entryCmp );
			
			/*wypisanie*/
			Iterator< Entry<String,Integer> > iterator = sorted.iterator();
			
			while (iterator.hasNext()) {
				Entry<String,Integer> pozycja = iterator.next();
				System.out.println( pozycja.getKey() + " " + pozycja.getValue() );
			}
		
		} catch (URISyntaxException e) {
			mycrawler.log( "Niepoprawna sciezka inicjujujaca.");
		} catch( ArrayIndexOutOfBoundsException e ) {
			System.out.println( "Nie podano adresu domeny zrodlowej.");
		}
	}
}
