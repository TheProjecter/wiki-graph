package wikigraph.util;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

public class WikiGraphUtilTest {
	private static Log log = LogFactory.getLog(WikiGraphUtilTest.class);

	@Test
	public void wikiPagesXmlToText() throws IOException, ParserConfigurationException, SAXException {
		File sampleFile = new File("src" + File.separator + "test"
				+ File.separator + "data" + File.separator + "anarchism.xml");

		String pageXml = FileUtils.readFileToString(sampleFile);
		String text = WikiGraphUtil.wikiPageXmlToMarkup(pageXml);
		log.info(text);
	}

	@Test
	public void wikiMarkupToText() throws IOException, ParserConfigurationException, SAXException {
		File sampleFile = new File("src" + File.separator + "test"
				+ File.separator + "data" + File.separator
				+ "anarchism-wiki-markup.txt");

		String wikiMarkup = FileUtils.readFileToString(sampleFile);
		String text = WikiGraphUtil.wikiMarkupToText(wikiMarkup);
		log.info(text);
	}

	@Test
	public void wikiPageXmlToText() throws IOException, ParserConfigurationException, SAXException {
		File sampleFile = new File("src" + File.separator + "test"
				+ File.separator + "data" + File.separator
				+ "anarchism-page.xml");
		String pageXml = FileUtils.readFileToString(sampleFile);
		String wikiMarkup = WikiGraphUtil.wikiPageXmlToMarkup(pageXml);
		
		String text = WikiGraphUtil.wikiMarkupToText(wikiMarkup);
		log.info(text);
	}
}