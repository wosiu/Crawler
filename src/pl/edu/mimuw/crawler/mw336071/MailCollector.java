package pl.edu.mimuw.crawler.mw336071;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * @author Michał Woś mw336071 <br>
 *         This is an extension of crawler for collecting mails from google search
 *         Basic crawler was made as lab project on Objective Programming.<br>
 *         MIM University of Warsaw, May 2013 <br>
 * @param
 */
public class MailCollector extends Crawler {

	private URI sourceURI;
	private Set<String> mails = new HashSet<>();
	private Set<String> processedDomains = new HashSet<>();

	@Override
	public void postVisit(Page page) {

		Document doc = page.getDoc();
		String mydata = doc.toString();

		// note that this pattern would not include all valid email addresses form regarding RFC 822
		Pattern pattern = Pattern.compile("[A-Z0-9._%+-]+@[A-Z0-9.-_]+\\.[A-Z]+", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(mydata);


		while (matcher.find()) {
			String mail = matcher.group();
			String domain;

			if ( mails.add(mail) ) {
				// set domain as pocessed - do not search for more mails in such domain
				domain = getDomain(page.getUri());
				processedDomains.add(domain);

				System.out.println(domain + " <" + mail + ">");
			}
		}
	}

	private String getDomain(URI uri) {
		try {
			return uri.toURL().getHost();
		} catch (MalformedURLException | IllegalArgumentException e) {}
		return "unknown";
	}

	public boolean validUri( URI uri ) {
		String domain = getDomain(uri);
		String[] blacklist = {"groups.google.", "tvp", "tvn", "polity", "gazeta", "agora", "infor", "wiadomosci",
				"apps", "rmf", "radio", "dziennik", "forsal", "support.google", "pap", "play.google" };
		if (processedDomains.contains(domain)) {
			return false;
		}
		for (String black: blacklist) {
			if (domain.contains(black)) {
				return false;
			}
		}
		return true;
	}

	public void download( Page page ) throws IllegalArgumentException, IOException
	{
		// remember that google do not allow search by robots,
		// note that it might be against google policy
		String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36" +
				" (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36";
		String uri = page.getUri().toString();
		page.setDoc( Jsoup.connect(uri).userAgent(userAgent).get() );
	}

	@Override
	public void log(String monit) {}

	/* M A I N */
	public static void main(String[] args) throws Exception {
		String startURLs[] = {
				/*
				"https://www.google" +
				".pl/search?safe=off&q=meble+na+zam%C3%B3wienie+bydgoszcz&npsic=0&rflfq" +
				"=1&rlha=0&tbm=lcl&sa=X&ved=0CDEQtgNqFQoTCIun0r33vMgCFcIXLAod_1AMiw#q=meble+na+zam%C3%B3wienie" +
				"+bydgoszcz&safe=off&tbm=lcl&start=0",
				*/
		/*"https://www.google.pl/search?safe=off&q=meble+na+zam%C3%B3wienie+bydgoszcz&npsic=0&rflfq=1&rlha=0&tbm=lcl" +
				"&sa" +
				"=X&ved=0CDEQtgNqFQoTCIun0r33vMgCFcIXLAod_1AMiw#q=meble+na+zam%C3%B3wienie+bydgoszcz&safe=off&tbs=lf:1,lf_ui:2&tbm=lcl&start=20&fll=53.148806628641275,17.93980358242186&fspn=0.057344259984319024,0.1558685302734375&fz=13&oll=53.12687225547699,17.990615349999985&ospn=0.1012535006393378,0.2574920654296875&oz=13",
				*/
				"https://www.google.pl/search?q=stolarz+bydgoszcz&oq=stolarz+bydgoszcz&aqs=chrome." +
						".69i57j69i65l3j69i60l2.2391j0j7&sourceid=chrome&es_sm=122&ie=UTF-8#safe=off&q=stolarz+bydgoszcz&rflfq=1&rlha=0&tbm=lcl",
				"https://www.google.pl/search?q=stolarz+bydgoszcz&oq=stolarz+bydgoszcz&aqs=chrome..69i57j69i65l3j69i60l2.2391j0j7&sourceid=chrome&es_sm=122&ie=UTF-8#q=stolarz+bydgoszcz&safe=off&tbm=lcl&start=20"
		};


		MailCollector mycrawler = new MailCollector();
		mycrawler.setMaxRetryNumber(2);
		mycrawler.setTimeBetweenRetries(3000);
		mycrawler.setMaxDeph(2);


		for ( String startURL : startURLs ) {
			System.out.println("START");
			try {
				// inicjacja
				mycrawler.sourceURI = new URI(startURL);
				mycrawler.start(mycrawler.sourceURI);

			} catch (URISyntaxException e) {
				mycrawler.log("Niepoprawna sciezka inicjujujaca.");
			} catch (ArrayIndexOutOfBoundsException e) {
				System.out.println("Nie podano adresu domeny zrodlowej.");
			}
		}

	}
}

