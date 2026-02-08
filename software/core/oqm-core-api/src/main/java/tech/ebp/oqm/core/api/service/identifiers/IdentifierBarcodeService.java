package tech.ebp.oqm.core.api.service.identifiers;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.Identifier;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types.IdentifierType;
import uk.org.okapibarcode.backend.Code128;
import uk.org.okapibarcode.backend.Code2Of5;
import uk.org.okapibarcode.backend.Ean;
import uk.org.okapibarcode.backend.HumanReadableLocation;
import uk.org.okapibarcode.backend.Symbol;
import uk.org.okapibarcode.backend.Upc;
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

/**
 * Service to generate barcode images.
 *
 * TODO:: move to own service?
 * TODO:: add better labels to images https://github.com/jfree/jfreesvg
 */
@Slf4j
@ApplicationScoped
public class IdentifierBarcodeService {
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
	
	public String getGeneralIdData(IdentifierType type, String data, String label){
		String dataIn = data;
		int hQuietZone = 10;
		
		Symbol barcode = switch (type){
			case UPC_A -> {
				Upc upcaCode = new Upc(Upc.Mode.UPCA);
				dataIn = data.substring(0, 11);//shave off check bit
				upcaCode.setShowCheckDigit(true);
				yield upcaCode;
			}
			case UPC_E -> {
				Upc upceCode = new Upc(Upc.Mode.UPCE);
				dataIn = data.substring(0, 7);//shave off check bit
				yield upceCode;
			}
			case ISBN_10 -> {
				Ean isbn10Code = new Ean(Ean.Mode.EAN13);//TODO:: definately not this
				yield isbn10Code;
			}
			case ISBN_13 -> {
				Ean isbn13Code = new Ean(Ean.Mode.EAN13);//TODO:: likely not this
				dataIn = data.substring(0, 12);//shave off check bit
				yield isbn13Code;
			}
			case EAN_8 -> {
				Ean ean8Code = new Ean(Ean.Mode.EAN8);
				dataIn = data.substring(0, 7);//shave off check bit
				yield ean8Code;
			}
			case EAN_13 -> {
				Ean ean13Code = new Ean(Ean.Mode.EAN13);
				dataIn = data.substring(0, 12);//shave off check bit
				yield ean13Code;
			}
			case GTIN_14 -> {
				Code2Of5 gtin14Code = new Code2Of5(Code2Of5.ToFMode.ITF14);
				dataIn = data.substring(0, 13);//shave off check bit
				yield gtin14Code;
			}
			case GENERIC, GENERATED-> {
				Code128 genericCode = new Code128();
				yield genericCode;
			}
			
			default -> throw new IllegalStateException("Unexpected value: " + type);
		};
		
		barcode.setFontName("Monospaced");
		barcode.setFontSize(8);
//		barcode.setModuleWidth(1);
//		barcode.setBarHeight(50);
		barcode.setQuietZoneHorizontal(hQuietZone);
		barcode.setQuietZoneVertical(2);
		barcode.setHumanReadableLocation(HumanReadableLocation.BOTTOM);
		barcode.setContent(dataIn);
		
		String labelStr = null;
		boolean haveLabelGiven = label != null && !label.isBlank();
		
		if(type.displayInBarcode){
			labelStr = type.prettyName();
			
			if(haveLabelGiven && !type.name().equals(label)){
				labelStr += " / " + label;
			}
		} else {
			if(!haveLabelGiven){
				labelStr = type.prettyName();
			} else {
				labelStr = label;
			}
		}
		
		log.debug("Built label for {}/{}: {}", type, label, labelStr);
		
		return processBarcodeData(
			barcode,
			labelStr
		);
	}
	
	public String getGeneralIdData(Identifier identifier){
		return this.getGeneralIdData(identifier.getType(), identifier.getValue(), identifier.getLabel());
	}
	
}
