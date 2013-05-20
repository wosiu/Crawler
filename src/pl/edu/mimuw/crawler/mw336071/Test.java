package pl.edu.mimuw.crawler.mw336071;

import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class Test {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws URISyntaxException 
	 */
	public static void main(String[] args) throws IOException, URISyntaxException {
		
		File input = new File( "/home/m/studia/PO/eclipse/crawler/test/czytaj.html" );		
		Document doc = Jsoup.parse(input, "UTF-8");
		
		
		//Document doc = Jsoup.connect( "http://www.mimuw.edu.pl/~kdr/test/1/" ).get() ;		
		
		
		Elements anchors = doc.select("a[href]");
		System.out.println( anchors.size() );
		
        for (Element link : anchors) {
        	//System.out.println(link.baseUri());
        	//System.out.println( link.html() );
        	//System.out.println(link.text());
        	//System.out.println(link.toString());
        	System.out.println( link.attr("href")  );
        	//System.out.println( link.attr("abs:href")  );
           System.out.println( "__________" );
           //link = null;
        }
	}

}

