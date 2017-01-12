package test;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlParagraph;

import java.io.IOException;

/**
 * Created by ReggieYang on 2017/1/8.
 */
public class Test {

    public static void main(String[] args) {
        final WebClient webClient = new WebClient();
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(false);
        final HtmlPage page;
        try {
            page = webClient.getPage("http://www.kb.cert.org/vuls/id/619767");
            System.out.println("get the page!");
            final HtmlParagraph div = (HtmlParagraph) page.getByXPath("//div[@id='vulnerability-note-content']//table[1]//p").get(0);
            System.out.println(div.asXml());
        } catch (IOException e) {
            e.printStackTrace();
        }
        webClient.closeAllWindows();
    }

}
