package pl.edu.mimuw.crawler.mw336071;

import java.io.File;
import java.net.URI;

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
	public void download( Page page ) {
		File input = new File( page.getUri().toString() );
		page.setDoc( Jsoup.parse(input, "UTF-8") );
	}
	
	
	public static void main(String[] args) {

		String sourceUrl = args[0];
		int maxh = Integer.parseInt( args[1] );
		
		Demo2 mycrawler = new Demo2();		
		mycrawler.setMaxDeph( maxh );
		
		mycrawler.start( sourceUrl );
		
		System.out.println( max(0, mycrawler.counter) );
	}

}
