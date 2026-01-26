package tech.ebp.oqm.core.characteristics.utils;

import jakarta.enterprise.context.ApplicationScoped;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class ColorUtils {
	
	private final Map<String, Color> colorNameMap = new HashMap<>() {{
		//standard web colors
		put("aliceblue", Color.decode("#F0F8FF"));
		put("antiquewhite", Color.decode("#FAEBD7"));
		put("aqua", Color.decode("#00FFFF"));
		put("aquamarine", Color.decode("#7FFFD4"));
		put("azure", Color.decode("#F0FFFF"));
		put("beige", Color.decode("#F5F5DC"));
		put("bisque", Color.decode("#FFE4C4"));
		put("black", Color.decode("#000000"));
		put("blanchedalmond", Color.decode("#FFEBCD"));
		put("blue", Color.decode("#0000FF"));
		put("blueviolet", Color.decode("#8A2BE2"));
		put("brown", Color.decode("#A52A2A"));
		put("burlywood", Color.decode("#DEB887"));
		put("cadetblue", Color.decode("#5F9EA0"));
		put("chartreuse", Color.decode("#7FFF00"));
		put("chocolate", Color.decode("#D2691E"));
		put("coral", Color.decode("#FF7F50"));
		put("cornflowerblue", Color.decode("#6495ED"));
		put("cornsilk", Color.decode("#FFF8DC"));
		put("crimson", Color.decode("#DC143C"));
		put("cyan", Color.decode("#00FFFF"));
		put("darkblue", Color.decode("#00008B"));
		put("darkcyan", Color.decode("#008B8B"));
		put("darkgoldenrod", Color.decode("#B8860B"));
		put("darkgray", Color.decode("#A9A9A9"));
		put("darkgrey", Color.decode("#A9A9A9"));
		put("darkgreen", Color.decode("#006400"));
		put("darkkhaki", Color.decode("#BDB76B"));
		put("darkmagenta", Color.decode("#8B008B"));
		put("darkolivegreen", Color.decode("#556B2F"));
		put("darkorange", Color.decode("#FF8C00"));
		put("darkorchid", Color.decode("#9932CC"));
		put("darkred", Color.decode("#8B0000"));
		put("darksalmon", Color.decode("#E9967A"));
		put("darkseagreen", Color.decode("#8FBC8F"));
		put("darkslateblue", Color.decode("#483D8B"));
		put("darkslategray", Color.decode("#2F4F4F"));
		put("darkslategrey", Color.decode("#2F4F4F"));
		put("darkturquoise", Color.decode("#00CED1"));
		put("darkviolet", Color.decode("#9400D3"));
		put("deeppink", Color.decode("#FF1493"));
		put("deepskyblue", Color.decode("#00BFFF"));
		put("dimgray", Color.decode("#696969"));
		put("dimgrey", Color.decode("#696969"));
		put("dodgerblue", Color.decode("#1E90FF"));
		put("firebrick", Color.decode("#B22222"));
		put("floralwhite", Color.decode("#FFFAF0"));
		put("forestgreen", Color.decode("#228B22"));
		put("fuchsia", Color.decode("#FF00FF"));
		put("gainsboro", Color.decode("#DCDCDC"));
		put("ghostwhite", Color.decode("#F8F8FF"));
		put("gold", Color.decode("#FFD700"));
		put("goldenrod", Color.decode("#DAA520"));
		put("gray", Color.decode("#808080"));
		put("grey", Color.decode("#808080"));
		put("green", Color.decode("#008000"));
		put("greenyellow", Color.decode("#ADFF2F"));
		put("honeydew", Color.decode("#F0FFF0"));
		put("hotpink", Color.decode("#FF69B4"));
		put("indianred", Color.decode("#CD5C5C"));
		put("indigo", Color.decode("#4B0082"));
		put("ivory", Color.decode("#FFFFF0"));
		put("khaki", Color.decode("#F0E68C"));
		put("lavender", Color.decode("#E6E6FA"));
		put("lavenderblush", Color.decode("#FFF0F5"));
		put("lawngreen", Color.decode("#7CFC00"));
		put("lemonchiffon", Color.decode("#FFFACD"));
		put("lightblue", Color.decode("#ADD8E6"));
		put("lightcoral", Color.decode("#F08080"));
		put("lightcyan", Color.decode("#E0FFFF"));
		put("lightgoldenrodyellow", Color.decode("#FAFAD2"));
		put("lightgray", Color.decode("#D3D3D3"));
		put("lightgrey", Color.decode("#D3D3D3"));
		put("lightgreen", Color.decode("#90EE90"));
		put("lightpink", Color.decode("#FFB6C1"));
		put("lightsalmon", Color.decode("#FFA07A"));
		put("lightseagreen", Color.decode("#20B2AA"));
		put("lightskyblue", Color.decode("#87CEFA"));
		put("lightslategray", Color.decode("#778899"));
		put("lightslategrey", Color.decode("#778899"));
		put("lightsteelblue", Color.decode("#B0C4DE"));
		put("lightyellow", Color.decode("#FFFFE0"));
		put("lime", Color.decode("#00FF00"));
		put("limegreen", Color.decode("#32CD32"));
		put("linen", Color.decode("#FAF0E6"));
		put("magenta", Color.decode("#FF00FF"));
		put("maroon", Color.decode("#800000"));
		put("mediumaquamarine", Color.decode("#66CDAA"));
		put("mediumblue", Color.decode("#0000CD"));
		put("mediumorchid", Color.decode("#BA55D3"));
		put("mediumpurple", Color.decode("#9370DB"));
		put("mediumseagreen", Color.decode("#3CB371"));
		put("mediumslateblue", Color.decode("#7B68EE"));
		put("mediumspringgreen", Color.decode("#00FA9A"));
		put("mediumturquoise", Color.decode("#48D1CC"));
		put("mediumvioletred", Color.decode("#C71585"));
		put("midnightblue", Color.decode("#191970"));
		put("mintcream", Color.decode("#F5FFFA"));
		put("mistyrose", Color.decode("#FFE4E1"));
		put("moccasin", Color.decode("#FFE4B5"));
		put("navajowhite", Color.decode("#FFDEAD"));
		put("navy", Color.decode("#000080"));
		put("oldlace", Color.decode("#FDF5E6"));
		put("olive", Color.decode("#808000"));
		put("olivedrab", Color.decode("#6B8E23"));
		put("orange", Color.decode("#FFA500"));
		put("orangered", Color.decode("#FF4500"));
		put("orchid", Color.decode("#DA70D6"));
		put("palegoldenrod", Color.decode("#EEE8AA"));
		put("palegreen", Color.decode("#98FB98"));
		put("paleturquoise", Color.decode("#AFEEEE"));
		put("palevioletred", Color.decode("#DB7093"));
		put("papayawhip", Color.decode("#FFEFD5"));
		put("peachpuff", Color.decode("#FFDAB9"));
		put("peru", Color.decode("#CD853F"));
		put("pink", Color.decode("#FFC0CB"));
		put("plum", Color.decode("#DDA0DD"));
		put("powderblue", Color.decode("#B0E0E6"));
		put("purple", Color.decode("#800080"));
		put("rebeccapurple", Color.decode("#663399"));
		put("red", Color.decode("#FF0000"));
		put("rosybrown", Color.decode("#BC8F8F"));
		put("royalblue", Color.decode("#4169E1"));
		put("saddlebrown", Color.decode("#8B4513"));
		put("salmon", Color.decode("#FA8072"));
		put("sandybrown", Color.decode("#F4A460"));
		put("seagreen", Color.decode("#2E8B57"));
		put("seashell", Color.decode("#FFF5EE"));
		put("sienna", Color.decode("#A0522D"));
		put("silver", Color.decode("#C0C0C0"));
		put("skyblue", Color.decode("#87CEEB"));
		put("slateblue", Color.decode("#6A5ACD"));
		put("slategray", Color.decode("#708090"));
		put("slategrey", Color.decode("#708090"));
		put("snow", Color.decode("#FFFAFA"));
		put("springgreen", Color.decode("#00FF7F"));
		put("steelblue", Color.decode("#4682B4"));
		put("tan", Color.decode("#D2B48C"));
		put("teal", Color.decode("#008080"));
		put("thistle", Color.decode("#D8BFD8"));
		put("tomato", Color.decode("#FF6347"));
		put("turquoise", Color.decode("#40E0D0"));
		put("violet", Color.decode("#EE82EE"));
		put("wheat", Color.decode("#F5DEB3"));
		put("white", Color.decode("#FFFFFF"));
		put("whitesmoke", Color.decode("#F5F5F5"));
		put("yellow", Color.decode("#FFFF00"));
		put("yellowgreen", Color.decode("#9ACD32"));
		
		//easter egg colors
		put("redhatpurple", Color.decode("#D303FC"));
	}};
	
	
	public Color getColor(String hexStr) {
		if (hexStr == null) {
			return null;
		}
		
		if (!hexStr.startsWith("#")) {
			return this.colorNameMap.get(hexStr.toLowerCase());
		}
		
		return Color.decode(hexStr);
	}
}
