package pl.edu.mimuw.crawler.mw336071;

import java.net.URI;
import java.util.HashMap;

public class Demo1 extends Crawler {
	
	private URI sourceURI;
	
	public Demo1 ( String url ) {
		sourceURI = new URI( url );
	}
	
	private HashMap <String, Integer> visitedDomains = new HashMap <String, Integer >();
	
	@Override
	public boolean validUri( URI uri ) {
		return uri.getAuthority().equals( sourceURI.getAuthority() );
	}
	
	@Override
	public void preVisit( URI url ) {
		
		//sprawdzamy czy URL jest spoza domeny
		if ( !validUri( url )  )
		{
			String key = url.getAuthority().toString();
			Integer amount = visitedDomains.get( key );
			
			if ( amount == null ) amount = 0;
			
			visitedDomains.put( key, amount + 1 );
		}
	}
	

	public static void main(String[] args) {

		Demo1 mycrawler = new Demo1( args[0] );		
		mycrawler.start( mycrawler.sourceURI );
		
		//foreach( )
		//2 do: wypisanie
		System.out.println( mycrawler.visitedDomains.toString() );
		
	}

}
