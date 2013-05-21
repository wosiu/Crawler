package pl.edu.mimuw.crawler.mw336071;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.jsoup.Jsoup;

public class Demo2 extends Crawler {

	//ilosc stron do zadannej glebokosci, -1, bo nie wliczamy startowej
	private int counter = - 1;
	
	public int getCounter() {
		return counter;
	}
	
	@Override
	public void postVisit( Page page ) {
		counter++;
	}
	
	@Override
	public boolean validUri( URI uri ) {
		return (new File( uri )).exists();
	}
	
	@Override
	public void download( Page page ) throws IOException {
		File input = new File( page.getUri().toString() );
		page.setDoc( Jsoup.parse(input, "UTF-8") );
	}
	
	
	public static void main(String[] args) {

		Demo2 mycrawler = new Demo2();		
		int maxh;
		String sourceUrl;
		
		try {
			sourceUrl = args[0];
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
		
		try {
			mycrawler.start( sourceUrl );
		} catch (URISyntaxException e) {
			mycrawler.log( "Niepoprawna sciezka inicjujujaca. ");
		}
		
		System.out.println( Math.max(0, mycrawler.counter) );
	}

}
