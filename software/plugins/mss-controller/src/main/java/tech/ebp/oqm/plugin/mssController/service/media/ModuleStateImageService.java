package tech.ebp.oqm.plugin.mssController.service.media;

import jakarta.enterprise.context.ApplicationScoped;
import org.jfree.svg.SVGGraphics2D;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.moduleInfo.ModuleInfo;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.state.BlockState;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.state.ModuleState;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.state.light.BlockLightPowerState;
import tech.ebp.oqm.plugin.mssController.service.mssConn.connectors.MssConnector;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

@ApplicationScoped
public class ModuleStateImageService {
	private static final Color BORDER_COLOR = Color.BLACK;
	private static final int BLOCK_SIZE = 100;
	private static final int BLOCK_BORDER_SIZE = 8;
	private static final int WHOLE_BORDER_SIZE = BLOCK_BORDER_SIZE / 2;

	private static final int BASE_FONT_SIZE = 12;
	private static final int TITLE_TEXT_SIZE = BASE_FONT_SIZE * 2;
	private static final int TITLE_BAR_HEIGHT = (TITLE_TEXT_SIZE * 4);

	private static final Font BASE_FONT = new Font("Arial", Font.PLAIN, BASE_FONT_SIZE);
	private static final Font MAIN_TITLE_FONT = BASE_FONT.deriveFont(Font.BOLD, TITLE_TEXT_SIZE);
	private static final Font BLOCK_TITLE_FONT = BASE_FONT.deriveFont(Font.BOLD, TITLE_TEXT_SIZE);

	public static String generateStateImage(
		ModuleInfo moduleInfo,
		ModuleState moduleState
	){
		final int numRowCol = (int) Math.ceil(Math.sqrt(moduleInfo.getNumBlocks()));
		final int imgWidth = numRowCol * (BLOCK_SIZE);
		final int imgHeight = imgWidth + TITLE_BAR_HEIGHT;

		SVGGraphics2D g2 = new SVGGraphics2D(imgWidth, imgHeight);
		g2.setFont(BASE_FONT);
		{//background / main border
			g2.setBackground(Color.WHITE);
			g2.setColor(Color.WHITE);
			g2.fillRect(0, 0, imgWidth, imgHeight);

			BasicStroke customStroke = new BasicStroke(
				WHOLE_BORDER_SIZE,
				BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND
			);
			g2.setStroke(customStroke);
			g2.setColor(BORDER_COLOR);
			g2.drawRect(0, 0, imgWidth, imgHeight);
		}
		{//main title
			g2.setFont(MAIN_TITLE_FONT);

			String text = "Module: " + moduleInfo.getSerialId();

			FontMetrics fm = g2.getFontMetrics();
			int textX = ((imgWidth - fm.stringWidth(text)) / 2);
			int textY = (fm.getHeight() / 2) + fm.getAscent();

			g2.drawString(text, textX, textY);

			//TODO:: more module infos
		}

		int row = 0;
		int col = 0;
		for(BlockState s : moduleState.getStorageBlocks()){
			int startx = col * BLOCK_SIZE;
			int starty = TITLE_BAR_HEIGHT + (row * BLOCK_SIZE);
			Rectangle2D.Double rect = new Rectangle2D.Double(startx, starty, BLOCK_SIZE, BLOCK_SIZE);

			BasicStroke customStroke = new BasicStroke(
				BLOCK_BORDER_SIZE,
				BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND
			);
			g2.setStroke(customStroke);


			{//Fill the interior first (optional, to keep outline on top)
				Color bgColor = Color.LIGHT_GRAY;
				if(moduleInfo.getCapabilities().isBlockLights()) {
					if (!s.getLightSettings().getPowerState().equals(BlockLightPowerState.OFF)) {
						if(moduleInfo.getCapabilities().isBlockLightColor()){
							//TODO::color based on set color
						}
						bgColor = Color.WHITE;
					}

				}

				g2.setColor(bgColor);
				g2.fill(rect);
			}

			// Apply the outline color and draw the boundary
			g2.setColor(BORDER_COLOR);
			g2.draw(rect);

			{ // blockNum text
				int offset = 0;
				{
					g2.setFont(BLOCK_TITLE_FONT);
					String text = "" + s.getBlockNum();
					FontMetrics fm = g2.getFontMetrics();
					int textX = startx + ((BLOCK_SIZE - fm.stringWidth(text)) / 2);
					int textY = starty + ((BLOCK_BORDER_SIZE + 24 - fm.getHeight()) / 2) + fm.getAscent();

					g2.drawString(text, textX, textY);
					offset += BLOCK_TITLE_FONT.getSize();
				}
				{
					g2.setFont(BASE_FONT);

					String text = "Pow: " + s.getLightSettings().getPowerState();
					FontMetrics fm = g2.getFontMetrics();
					int textX = startx + ((BLOCK_SIZE - fm.stringWidth(text)) / 2);
					int textY = offset + starty + ((BLOCK_BORDER_SIZE + 24 - fm.getHeight()) / 2) + fm.getAscent();

					g2.drawString(text, textX, textY);
					offset += BASE_FONT.getSize();
				}
				if(moduleInfo.getCapabilities().isItemEventReporting()){
					{
						g2.setFont(BASE_FONT);

						String text = "Weight: " + s.getWeightState().getWeightStr();
						FontMetrics fm = g2.getFontMetrics();
						int textX = startx + ((BLOCK_SIZE - fm.stringWidth(text)) / 2);
						int textY = offset + starty + ((BLOCK_BORDER_SIZE + 24 - fm.getHeight()) / 2) + fm.getAscent();

						g2.drawString(text, textX, textY);
						offset += BASE_FONT.getSize();
					}
				}
			}

			//increment as necessary
			if((col + 1) < numRowCol){
				col++;
			}else{
				col = 0;
				row++;
			}
		}


		return g2.getSVGElement();
	}


	public BufferedImage generateStateImage(MssConnector connector){
		//TODO
		return null;
	}
}
