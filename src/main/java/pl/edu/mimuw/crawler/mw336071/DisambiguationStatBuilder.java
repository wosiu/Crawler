package pl.edu.mimuw.crawler.mw336071;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Vector;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @author Michał Woś mw336071 <br>
 *         This is an extension of crawler for collecting statistics from hadoop
 *         logs at http://hadoop-master.vls.icm.edu.pl:50030/. <br>
 *         Basic crawler was made as lab project on Objective Programming.<br>
 *         MIM University of Warsaw, May 2013 <br>
 * @param
 */
public class DisambiguationStatBuilder extends Crawler {

	private URI sourceURI;
	// private List<String> logs = new ArrayList<String>();

	// writing to file staff
	private PrintWriter statistics = null;
	private String logPath = null;

	// proxy for icm after open the tunnel
	private Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(
			"127.0.0.1", 23456)); // or whatever your proxy is

	public DisambiguationStatBuilder(String logPath)
			throws FileNotFoundException, UnsupportedEncodingException {

		if (logPath != null) {
			File f = new File(logPath);
			int suf = 0;
			String newLogPath = logPath;

			while (f.exists()) {
				newLogPath = logPath + "_" + suf;
				f = new File(newLogPath);
				suf++;
			}
			this.logPath = newLogPath;
			statistics = new PrintWriter(newLogPath, "UTF-8");
		}
	}

	/* R O Z S Z E R Z E N I E C R A W L E R A */
	@Override
	public boolean validUri(URI uri) {
		if (uri == null)
			return false;

		if (uri.getAuthority() != null
				&& !uri.getAuthority().contains("hadoop-master")
				&& !uri.getAuthority().contains("50030"))
			return false;

		String uriString = uri.toString();

		// main page of job
		if (uriString.contains("jobdetails.jsp"))
			return true;
		// links to list of task
		if (uriString.contains("jobtasks.jsp"))
			return true;
		// links table with attemps of task
		if (uriString.contains("taskdetails.jsp"))
			return true;
		// links to proper logs
		if (uriString.contains("tasklog?attemptid=attempt_")
				&& uriString.contains("all=true"))
			return true;

		return false;
	}

	private int taskCounter = 1;

	@Override
	public void postVisit(Page page) {
		// checking whether we are on the task log
		if (!page.getUri().toString().contains("tasklog?attemptid=attempt_"))
			return;

		Document doc = page.getDoc();
		Elements preBlock = doc.select("pre");
		String task = doc.select("h1").get(0).html();

		write("\n=========== " + task + " ===========");

		int lines_number = 0;
		String block_name[] = { "stdout logs", "stderr logs", "syslog logs" };
		for (int i = 0; i < 3; i++) {
			write("\n--------------------------- " + block_name[i]
					+ " ---------------------------------");
			String block = preBlock.get(i).html();
			lines_number += block.split("\r\n|\r|\n").length;
			write(block);
		}

		System.out.println("Stored " + lines_number + " lines from task "
				+ (taskCounter++) + " in file: " + logPath);
	}

	private void write(String line) {
		if (statistics == null) {
			System.out.println(line);
			return;
		}
		statistics.println(line);
		statistics.flush();
	}

	@Override
	public Vector<URI> getUris(Page page) {
		Elements anchors = page.getDoc().select("a[href]");
		Vector<URI> ret = new Vector<URI>();
		String uriString;

		for (Element link : anchors) {
			try {
				uriString = link.attr("href");
				ret.add(new URI(uriString));
			} catch (URISyntaxException e) {
				log("Niepoprawny format URI na: " + page.getUri() + ":"
						+ e.getMessage());
			}
		}

		return ret;
	}

	@Override
	public void download(Page page) throws IllegalArgumentException,
			UnknownHostException, IOException {

		URI uri = page.getUri();

		// there is no sense to download foreign domains
		if (!validUri(uri)) {
			page.setDoc(new Document(""));
			return;
		}

		String uriString = uri.toString();
		String base = "http://hadoop-master.vls.icm.edu.pl:50030/";

		// if relative link, we need to add domain and protocol
		if (uri.getAuthority() == null) {
			try {
				uri = new URI(base + uriString);
			} catch (URISyntaxException e) {
				log("Niepoprawny format URI na: " + page.getUri() + ":" + base
						+ uriString);
				e.printStackTrace();
			}
		}

		URL url = uri.toURL();

		HttpURLConnection uc = (HttpURLConnection) url.openConnection(proxy);

		uc.connect();

		String line = null;
		StringBuffer tmp = new StringBuffer();
		BufferedReader in = new BufferedReader(new InputStreamReader(
				uc.getInputStream()));

		while ((line = in.readLine()) != null) {
			tmp.append(line + "\n");
		}

		Document doc = Jsoup.parse(String.valueOf(tmp));
		page.setDoc(doc);

		uc.disconnect();
	}

	/* M A I N */
	public static void main(String[] args) throws Exception {

		String startURL = "http://hadoop-master.vls.icm.edu.pl:50030/jobtasks.jsp?jobid=job_201308201233_0570&type=map&pagenum=1&state=completed";
		String logPath = "logs/apr.stat";

		if (args.length == 0) {
			throw new Exception("Nie podano URLa startowego.");
		}
		if (args.length >= 1) {
			startURL = args[0];
		}
		if (args.length >= 2) {
			logPath = args[1];
		}

		DisambiguationStatBuilder mycrawler = new DisambiguationStatBuilder(
				logPath);

		try {
			/* inicjacja */
			mycrawler.sourceURI = new URI(startURL);
			// mycrawler.setMaxDeph( 2 );

			mycrawler.start(mycrawler.sourceURI);

		} catch (URISyntaxException e) {
			mycrawler.log("Niepoprawna sciezka inicjujujaca.");
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("Nie podano adresu domeny zrodlowej.");
		}
	}
}

