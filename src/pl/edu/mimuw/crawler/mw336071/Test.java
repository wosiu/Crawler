package pl.edu.mimuw.crawler.mw336071;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Vector;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

//
//import java.io.IOException;
//import java.net.URISyntaxException;

//import org.jsoup.Jsoup;


public class Test {

	
	public static void main(String[] args) throws IOException, URISyntaxException {
		
		// URL folder = this.getClass().getResource(".")
		/*URI uri = new URI("test/czytaj.html");
		
		System.out.println( uri );
		System.out.println( uri.getPath() );
		
		File f = null;
		
		try {
			f = new File( uri.getPath() ); 
		} catch (java.lang.IllegalArgumentException e )
		{
			System.out.println( "Niepoprawna sciezka: " + uri.toString() + ": " + e.toString() );
		}*/
		
		
		
		//File input = new File( "/home/m/studia/PO/eclipse/crawler/test/czytaj.html" );		
		//File input = new File( "test/dump/1/index.html" );
		String sciezka = "test/dump/1/pages/2.html";
		File input = new File( sciezka );
		
		Document doc = Jsoup.parse(input, "UTF-8", input.toURI().toString() );
		
		
		//Document doc = Jsoup.connect( "http://www.mimuw.edu.pl/~kdr/test/1/" ).get() ;		
		
		
		Elements anchors = doc.select("a[href]");
		System.out.println( anchors.size() );
		
		/*String base;
		int endindex = doc.baseUri().lastIndexOf('/');
		base = doc.baseUri().substring( 0, (endindex != -1) ? endindex : doc.baseUri().length() );
		
		System.out.println( "doc: " + doc.baseUri() );
		System.out.println( "doc base: " + base );*/
		
		
        for (Element link : anchors) {
        	//System.out.println( link.html() );
        	//System.out.println(link.text());
        	System.out.println( link.attr("href")  );
        	System.out.println( link.absUrl("href")  );
 
           System.out.println( "__________" );
           //link = null;
        }
		
	}

}

