package pl.edu.mimuw.crawler.mw336071;

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

	
	public static void main(String[] args) throws IOException {
		
		String uri = "http://www.mimuw.edu.pl/~kdr/test/1/";
		Document doc = Jsoup.connect( uri ).get();
		
		Elements anchors = doc.select("a[href]");
        Vector<URI>ret = new Vector<URI>();
        
		for ( Element link : anchors )
		{
            System.out.println( link.absUrl("href") );
			
			try {
				ret.add( new URI( link.attr("href") ) );
			} catch (URISyntaxException e) {
				//TO DO: sprawdzic czy domyslne Uri().toString dziala pieknie
			}
		}
	}

}

