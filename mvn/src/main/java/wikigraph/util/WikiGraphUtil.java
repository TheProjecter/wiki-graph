package wikigraph.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class WikiGraphUtil {
	/**
	 * Convert wikipedia XML to text
	 * 
	 * @param pageXml
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public static String wikiPageXmlToMarkup(String pageXml)
			throws ParserConfigurationException, SAXException, IOException {
		return getStringFromXmlNoChild(pageXml);
	}

	public static String wikiPageXmlToMarkupOld(String pageXml) {
		// page -> revision -> <text xml:space="preserve">
		if (isBlank(pageXml)) {
			return "";
		}

		Pattern pattern = Pattern
				.compile("(?s)^.*<text xml:space=\"preserve\">(.+)</text>.*$");

		Matcher matcher = pattern.matcher(pageXml);
		if (matcher.matches()) {
			return matcher.group(1);
		}

		return "";
	}

    private static boolean isBlank(String pageXml) {
        return (pageXml==null)||(pageXml.length()==0);
    }

    public static String wikiMarkupToText(String wikiMarkup)
			throws ParserConfigurationException, SAXException, IOException {

		// wikiMarkup = getStringFromXmlNoChild(wikiMarkup);
		wikiMarkup = removeTemplates(wikiMarkup);
		wikiMarkup = removeRefs(wikiMarkup);
		// | separated patterns to remove
		wikiMarkup = wikiMarkup.replaceAll("[\\[\\]\\|'\\(\\),]", " ");

		// remove everything else that we don't want to keep alpha num and
		// period and '-'
		wikiMarkup = wikiMarkup.replaceAll("[^\\.a-zA-Z0-9]", " ");

		// remove extra spaces
		wikiMarkup = wikiMarkup.replaceAll("\\s+", " ");
		wikiMarkup = wikiMarkup.replaceAll("(\\S\\S\\.) ", "$1\n");
		wikiMarkup = wikiMarkup.toLowerCase();

		return wikiMarkup;
	}

	/**
	 * @param wikiMarkup
	 * @return
	 */
	private static String removeTemplates(String wikiMarkup) {
		// remove everything between {{ }}
		// {{Redirect|Anarchist|the fictional character|Anarchist (comics)}}

		Pattern p = Pattern.compile("\\{\\{[^}]+\\}\\}", Pattern.DOTALL);
		Matcher m = p.matcher(wikiMarkup);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			m.appendReplacement(sb, "");
		}
		m.appendTail(sb);
		return sb.toString();
	}

	private static String removeRefs(String wikiMarkup) {
		wikiMarkup = removeRefs1(wikiMarkup);
		// wikiMarkup = removeRefs2(wikiMarkup);
		wikiMarkup = removeRefs3(wikiMarkup);
		return wikiMarkup;
	}

	private static String removeRefs3(String wikiMarkup) {
		wikiMarkup = wikiMarkup.replaceAll("<ref>|</ref\\s*>", "");

		// <ref name="illegalism">
		wikiMarkup = wikiMarkup.replaceAll("<ref[^>]", "");
		return wikiMarkup;
	}

	private static String removeRefs1(String wikiMarkup) {
		// remove everything &lt;ref name=&quot;definition&quot;&gt;
		// &lt;/ref&gt;

		// Pattern p = Pattern.compile("&lt;ref.+(&lt;/ref&gt;|/&gt;)",
		// Pattern.DOTALL);

		// <ref name="Anarchism 1962"/>
		Pattern p = Pattern.compile("<ref[^<>]+/>", Pattern.DOTALL);
		Matcher m = p.matcher(wikiMarkup);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			m.appendReplacement(sb, "");
		}
		m.appendTail(sb);
		return sb.toString();
	}

	private static String removeRefs2(String wikiMarkup) {
		// remove everything &lt;ref name=&quot;definition&quot;&gt;
		// &lt;/ref&gt;

		// Pattern p = Pattern.compile("&lt;ref.+(&lt;/ref&gt;|/&gt;)",
		// Pattern.DOTALL);
		Pattern p = Pattern.compile("&lt;ref.+(&lt;/ref&gt;|/&gt;)",
				Pattern.DOTALL);
		Matcher m = p.matcher(wikiMarkup);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			m.appendReplacement(sb, "");
		}
		m.appendTail(sb);
		return sb.toString();
	}

	private static void readXml(String xml)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(xml));
		Document doc = docBuilder.parse(is);

		NodeList pageList = doc.getElementsByTagName("page");
		if (pageList.getLength() != 1)
			throw new IllegalArgumentException();

		Element page = (Element) pageList.item(0);
		// setTitle(getStringFromTag(page, "title"));
		// setId(Integer.parseInt(getStringFromTag(page, "id")));

		NodeList revisionList = page.getElementsByTagName("revision");
		if (revisionList.getLength() < 1)
			throw new IllegalArgumentException();

		// Element revision = (Element) revisionList.item(0);

		// setRevisionId(Integer.parseInt(getStringFromTag(revision,
		// "id")));
		// setRevisionComment(getStringFromTag(revision, "comment"));

		// String revisionDateString = getStringFromTag(revision, "timestamp");
		// if (revisionDateString != null)
		// setRevisionDate(df.parse(revisionDateString));

		// setText(getStringFromTag(revision, "text"));
	}

	private static String getStringFromTag(Element rootElement, String tagname) {
		NodeList list = rootElement.getElementsByTagName(tagname);
		if (list.getLength() > 0) {
			Element element = (Element) list.item(0);
			Node child = element.getFirstChild();
			if (child instanceof CharacterData) {
				CharacterData cd = (CharacterData) child;
				return cd.getData();
			}
		}
		return null;
	}

	private static String getStringFromXml(String xml)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(xml));
		Document doc = docBuilder.parse(is);

		NodeList list = doc.getElementsByTagName("text");

		if (list.getLength() > 0) {
			Element element = (Element) list.item(0);

			return element.getTextContent();
		}
		return "";
	}

	private static String getStringFromXmlNoChild(String xml)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(xml));
		Document doc = docBuilder.parse(is);

		StringBuilder sb = new StringBuilder();

		NodeList list = doc.getElementsByTagName("text");

		if (list.getLength() > 0) {
			Element element = (Element) list.item(0);

			// we are inside the text element

			NodeList nodeList = element.getChildNodes();
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node n = nodeList.item(i);
				// if (n .instanceof CharacterData) {
				if (n.getNodeType() == Node.TEXT_NODE) {
					sb.append(n.getNodeValue()).append(" ");
				}
			}
		}
		return sb.toString();
	}
}