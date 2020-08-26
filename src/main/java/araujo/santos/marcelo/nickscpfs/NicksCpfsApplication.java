package araujo.santos.marcelo.nickscpfs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.gargoylesoftware.css.parser.CSSErrorHandler;
import com.gargoylesoftware.css.parser.CSSException;
import com.gargoylesoftware.css.parser.CSSParseException;
import com.gargoylesoftware.htmlunit.AjaxController;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.ScriptException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlRadioButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.gargoylesoftware.htmlunit.html.parser.HTMLParserListener;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptErrorListener;

@SpringBootApplication
public class NicksCpfsApplication implements CommandLineRunner {

	private static WebClient webClient;
	private List<String> listNicks = new ArrayList<String>();
	private List<String> listCPF = new ArrayList<String>();

	public static void main(String[] args) throws Exception {

		SpringApplication app = new SpringApplication(NicksCpfsApplication.class);
		Properties properties = new Properties();
		properties.setProperty("spring.main.banner-mode", "off");
		properties.setProperty("logging.pattern.console", "");
		app.setDefaultProperties(properties);
		app.setBannerMode(Banner.Mode.OFF);
		app.run(args);
	}

	@Override
	public void run(String... args) throws Exception {

		this.gatherDataNick();
		this.gatherDataCpf();
		this.generateNickCpfToFile();

	}

	private static WebClient singleton() {

		if (NicksCpfsApplication.webClient == null) {
			NicksCpfsApplication.webClient = new WebClient(BrowserVersion.BEST_SUPPORTED);

			NicksCpfsApplication.webClient.getOptions().setJavaScriptEnabled(true);
			NicksCpfsApplication.webClient.getOptions().setActiveXNative(false);
			NicksCpfsApplication.webClient.getOptions().setCssEnabled(false);
			NicksCpfsApplication.webClient.getOptions().setPrintContentOnFailingStatusCode(false);
			NicksCpfsApplication.webClient.getOptions().setThrowExceptionOnScriptError(false);
			NicksCpfsApplication.webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);

			NicksCpfsApplication.webClient.setCssErrorHandler(new CSSErrorHandler() {

				@Override
				public void warning(CSSParseException exception) throws CSSException {

				}

				@Override
				public void error(CSSParseException exception) throws CSSException {

				}

				@Override
				public void fatalError(CSSParseException exception) throws CSSException {

				}

			});
			NicksCpfsApplication.webClient.setJavaScriptErrorListener(new JavaScriptErrorListener() {

				@Override
				public void scriptException(HtmlPage page, ScriptException scriptException) {

				}

				@Override
				public void timeoutError(HtmlPage page, long allowedTime, long executionTime) {

				}

				@Override
				public void malformedScriptURL(HtmlPage page, String url, MalformedURLException malformedURLException) {

				}

				@Override
				public void loadScriptError(HtmlPage page, java.net.URL scriptUrl, Exception exception) {

				}

				@Override
				public void warn(String message, String sourceName, int line, String lineSource, int lineOffset) {

				}

			});
			NicksCpfsApplication.webClient.setHTMLParserListener(new HTMLParserListener() {

				@Override
				public void error(String message, java.net.URL url, String html, int line, int column, String key) {

				}

				@Override
				public void warning(String message, java.net.URL url, String html, int line, int column, String key) {

				}

			});

			NicksCpfsApplication.webClient.setAjaxController(new AjaxController() {

				private static final long serialVersionUID = -6760937588875312112L;

				@Override
				public boolean processSynchron(HtmlPage page, WebRequest request, boolean async) {
					return true;
				}
			});
			return NicksCpfsApplication.webClient;
		}

		return NicksCpfsApplication.webClient;
	}

	public void gatherDataNick()
			throws FailingHttpStatusCodeException, MalformedURLException, IOException, InterruptedException {

		HtmlPage page = NicksCpfsApplication.singleton().getPage("https://www.4devs.com.br/gerador_de_nicks");

		String optionValueToBeSelected = "random";
		HtmlSelect select = (HtmlSelect) page.getElementById("method");

		String optionValueQuantity = "50";
		HtmlInput selectQuantity = (HtmlInput) page.getElementById("quantity");
		selectQuantity.setValueAttribute(optionValueQuantity);

		String optionValueMaxLetters = "8";
		HtmlSelect selectMaxLetters = (HtmlSelect) page.getElementById("limit");

		HtmlOption option = select.getOptionByValue(optionValueToBeSelected);
		select.setSelectedAttribute(option, true);

		HtmlOption optionMaxLetters = selectMaxLetters.getOptionByValue(optionValueMaxLetters);
		selectMaxLetters.setSelectedAttribute(optionMaxLetters, true);

		HtmlElement htmlElement = page.getFirstByXPath("//input[@id='bt_gerar_nick']");
		page = htmlElement.click();

		List<HtmlElement> listNicksAux = page.getByXPath("//span[@class='generated-nick']");

		if (listNicksAux.size() > 0) {
			for (HtmlElement nick : listNicksAux) {
				listNicks.add(nick.getTextContent().toString().trim());
			}
		}
	}

	public void gatherDataCpf() throws FailingHttpStatusCodeException, IOException, InterruptedException {

		HtmlPage page = NicksCpfsApplication.singleton().getPage("https://www.4devs.com.br/gerador_de_cpf");
		HtmlRadioButtonInput radioButton = (HtmlRadioButtonInput) page.getElementById("pontuacao_sim");
		radioButton.setChecked(true);
		HtmlElement htmlElement = page.getFirstByXPath("//input[@id='bt_gerar_cpf']");

		if (this.listNicks.size() > 0) {

			for (int i = 0; i < this.listNicks.size(); i++) {

				HtmlPage pageClick = htmlElement.click();

				page.getEnclosingWindow().getJobManager().waitForJobs(5200);

				DomElement cpf = pageClick.getElementById("texto_cpf");

				this.listCPF.add(cpf.getTextContent().toString().trim());

			}
		}
	}

	public void generateNickCpfToFile() throws FileNotFoundException {

		File file = new File("nicks-cpfs.txt");

		PrintWriter pw = new PrintWriter(new FileOutputStream(file));

		if (listNicks.size() > 0 && this.listCPF.size() > 0 && listNicks.size() == listCPF.size()) {

			pw.println("NICK;CPF");

			for (int i = 0; i < listNicks.size(); i++) {

				pw.println(listNicks.get(i) + ";" + listCPF.get(i));

			}
		}

		pw.close();

	}

}