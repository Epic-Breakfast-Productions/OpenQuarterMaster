package tech.ebp.oqm.core.api.service.identifiers;

import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import uk.org.okapibarcode.backend.Symbol;
import uk.org.okapibarcode.graphics.Color;
import uk.org.okapibarcode.output.SvgRenderer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
public abstract class IdBarcodeService {
	public static final String DATA_MEDIA_TYPE = "image/svg+xml";
	
	private static final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
	private static final TransformerFactory tf = TransformerFactory.newInstance();
	
	static {
		//prevent factory from validating schema, reaching out to URL's
		docBuilderFactory.setValidating(false);
		
		try {
			docBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			docBuilderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
			docBuilderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		} catch(ParserConfigurationException e) {
			throw new IllegalStateException("FAILED to setup document builder (this should not happen)", e);
		}
	}
	
	private static String toImageData(Symbol code){
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		SvgRenderer renderer = new SvgRenderer(os, 1, Color.WHITE, Color.BLACK, true);
		try {
			renderer.render(code);
		} catch(IOException e) {
			throw new IllegalStateException(new RuntimeException(e));
		}
		return os.toString();
	}

	public static String processBarcodeData(Symbol code, String label){
		String output = toImageData(code);
		
		if(label != null && !label.isBlank()){// add label to image
			log.debug("Adding label: {}", label);
			Document document;
			try(
				ByteArrayInputStream is = new ByteArrayInputStream(output.getBytes());
			) {
				DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
				document = docBuilder.parse(is);
			} catch(ParserConfigurationException | SAXException | IOException e) {
				throw new RuntimeException("Failed to parse svg xml.", e);
			}
			
			Element svgNode = (Element) document.getElementsByTagName("svg").item(0);
			Element barcodeNode = (Element) svgNode.getElementsByTagName("g").item(0);
			
			NodeList barcodeElementList = barcodeNode.getChildNodes();
			Element backdropNode = (Element) barcodeNode.getElementsByTagName("rect").item(0);
			
			float imgWidth = Float.parseFloat(svgNode.getAttribute("width"));
			float origHeight = Float.parseFloat(svgNode.getAttribute("height"));
			float fontSize = Float.parseFloat(
				((Element)barcodeNode.getElementsByTagName("text").item(0)).getAttribute("font-size")
			);
			
			float newTextLineHeight = fontSize + 1;
			float newHeight = origHeight + newTextLineHeight;
			
			//adjust image size, current elements
			svgNode.setAttribute("height", String.valueOf(newHeight));
			backdropNode.setAttribute("height", String.valueOf(newHeight));
			
			for(int i = 0; i < barcodeElementList.getLength(); i++){
				if(!(barcodeElementList.item(i) instanceof Element curNode)){
					continue;
				}
				
				if(
					!curNode.hasAttribute("y") ||
					curNode.getAttribute("y").equals("0")
				){
					continue;
				}
				
				float oldY = Float.parseFloat(curNode.getAttribute("y"));
				float newY = oldY + newTextLineHeight;
				
				curNode.setAttribute("y", String.valueOf(newY));
			}
			
			//add new text element for label
			Element newLabelNode = document.createElement("text");
			newLabelNode.setAttribute("x", String.valueOf(imgWidth / 2));
			newLabelNode.setAttribute("y", String.valueOf(newTextLineHeight - 1));
			newLabelNode.setAttribute("text-anchor", "middle");
			newLabelNode.setAttribute("text-decoration", "underline");
			newLabelNode.setAttribute("font-family", "Monospaced");
			newLabelNode.setAttribute("font-size", "8.00");
			newLabelNode.setAttribute("fill", "#000000");
			newLabelNode.appendChild(document.createTextNode(label));
			
			barcodeNode.appendChild(newLabelNode);
			
			try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
				Transformer transformer = tf.newTransformer();
				transformer.transform(new DOMSource(document), new StreamResult(baos));
				
				output = baos.toString();
			} catch(IOException | TransformerException e) {
				throw new RuntimeException("Failed to get string of svg xml.", e);
			}
		}
		return output;
	}
	
}
