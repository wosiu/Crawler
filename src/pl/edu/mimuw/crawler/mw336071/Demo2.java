package pl.edu.mimuw.crawler.mw336071;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import org.jsoup.Jsoup;

/**
 * @author Michał Woś mw336071 <br>
 * projekt na laboratoria Programowanie Obiektowe <br>
 * MIM Uniwersytet Warszawski maj 2013 <br>
 */
public class Demo2 extends Crawler {

	//ilosc stron do zadannej glebokosci
	private int counter = 0;
	
	@Override
	synchronized public void postVisit( Page page ) {
		counter++;
	}
	
	@Override
	public boolean validUri( URI uri ) {
		if( ( new File( uri.getPath() ) ).isFile() ) return true;
		log("validUri(): Nie istnieje plik lokalny o sciezce: " + uri.toString() );
		return false;
	}
	
	@Override
	public void download( Page page ) throws IOException {
		File input = new File( page.getUri().getPath() );
		page.setDoc( Jsoup.parse(input, "UTF-8", input.toURI().toString()) );
	}
	
	
	public static void main(String[] args) {

		Demo2 mycrawler = new Demo2();		
		int maxh;
		File source;
		
		try {
			source = new File( args[0] );
			maxh = Integer.parseInt( args[1] );
		} catch( ArrayIndexOutOfBoundsException e ) {
			mycrawler.log( "Podano za mało argumentów.");
			return;
		}

		if( maxh < 0 ) 
		{
			mycrawler.log( "Niepoprawna dopuszczalna glebokosc przeszukuwania. ");
			return;
		}
			
		mycrawler.setMaxDeph( maxh );		
		mycrawler.start( source.toURI() );
		
		System.out.println( Math.max(0, mycrawler.counter) );
	}
}
