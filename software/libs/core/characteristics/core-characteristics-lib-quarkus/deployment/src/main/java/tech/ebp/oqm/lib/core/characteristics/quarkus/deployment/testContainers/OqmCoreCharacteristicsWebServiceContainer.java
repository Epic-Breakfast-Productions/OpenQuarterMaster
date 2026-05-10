package tech.ebp.oqm.lib.core.characteristics.quarkus.deployment.testContainers;

import com.github.dockerjava.api.model.HostConfig;
import io.quarkus.devservices.common.ConfigureUtil;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.Transferable;
import tech.ebp.oqm.lib.core.characteristics.quarkus.deployment.config.CoreCharacteristicsLibBuildTimeConfig;
import tech.ebp.oqm.lib.core.characteristics.quarkus.runtime.config.OqmCoreCharacteristicsConfig;

import java.util.List;
import java.util.Map;

/**
 * Container for the Open QuarterMaster Core API web service.
 */
public class OqmCoreCharacteristicsWebServiceContainer extends GenericContainer<OqmCoreCharacteristicsWebServiceContainer> {
	
	private final CoreCharacteristicsLibBuildTimeConfig serviceConfig;
	private final CoreCharacteristicsLibBuildTimeConfig.DevserviceConfig devserviceConfig;
	
	/**
	 * Initializes the container
	 */
	public OqmCoreCharacteristicsWebServiceContainer(
		CoreCharacteristicsLibBuildTimeConfig serviceConfig
	) {
		super(serviceConfig.devservices().image().toTestContainerImageName());
		this.serviceConfig = serviceConfig;
		this.devserviceConfig = serviceConfig.devservices();
	}
	
	@Override
	protected void configure() {
		//configure network
		ConfigureUtil.configureSharedNetwork(this, "oqm-core-characteristics");
		
		// Tell the dev service how to know the container is ready. All 3 is likely overkill, but eh
		this.waitingFor(Wait.forHealthcheck());
		this.addExposedPort(80);
		
		//configuration of characteristics
		
		this.devserviceConfig.devData().characteristics().title().ifPresent(title -> this.addEnv("CHARACTERISTICS_VAL_TITLE", title));
		this.devserviceConfig.devData().characteristics().motd().ifPresent(motd -> this.addEnv("CHARACTERISTICS_VAL_MOTD", motd));
		
		this.devserviceConfig.devData().characteristics().runBy().name().ifPresent(name -> this.addEnv("CHARACTERISTICS_VAL_RUNBY_NAME", name));
		this.devserviceConfig.devData().characteristics().runBy().email().ifPresent(email -> this.addEnv("CHARACTERISTICS_VAL_RUNBY_EMAIL", email));
		this.devserviceConfig.devData().characteristics().runBy().phone().ifPresent(phone -> this.addEnv("CHARACTERISTICS_VAL_RUNBY_PHONE", phone));
		this.devserviceConfig.devData().characteristics().runBy().website().ifPresent(website -> this.addEnv("CHARACTERISTICS_VAL_RUNBY_WEBSITE", website));
		
		
		if(this.devserviceConfig.devData().characteristics().runBy().haveLogoImg()){
			this.addEnv("CHARACTERISTICS_RUNBY_IMG_DIR", "/data/");
			this.addEnv("CHARACTERISTICS_VAL_RUNBY_LOGOIMG", "logo.svg");
			this.withCopyToContainer(Transferable.of(
				"""
					<?xml version="1.0" encoding="UTF-8" standalone="no"?>
					<!-- Created with Inkscape (http://www.inkscape.org/) -->
					
					<svg
					   width="152.40408mm"
					   height="143.43915mm"
					   viewBox="0 0 152.40408 143.43915"
					   version="1.1"
					   id="svg5"
					   sodipodi:docname="drawing.svg"
					   inkscape:version="1.2.2 (b0a8486541, 2022-12-01)"
					   xmlns:inkscape="http://www.inkscape.org/namespaces/inkscape"
					   xmlns:sodipodi="http://sodipodi.sourceforge.net/DTD/sodipodi-0.dtd"
					   xmlns="http://www.w3.org/2000/svg"
					   xmlns:svg="http://www.w3.org/2000/svg">
					  <sodipodi:namedview
					     id="namedview7"
					     pagecolor="#ffffff"
					     bordercolor="#000000"
					     borderopacity="0.25"
					     inkscape:showpageshadow="2"
					     inkscape:pageopacity="0.0"
					     inkscape:pagecheckerboard="0"
					     inkscape:deskcolor="#d1d1d1"
					     inkscape:document-units="mm"
					     showgrid="false"
					     inkscape:zoom="0.914906"
					     inkscape:cx="110.39385"
					     inkscape:cy="284.7287"
					     inkscape:window-width="2526"
					     inkscape:window-height="1371"
					     inkscape:window-x="0"
					     inkscape:window-y="0"
					     inkscape:window-maximized="1"
					     inkscape:current-layer="layer1" />
					  <defs
					     id="defs2" />
					  <g
					     inkscape:label="Layer 1"
					     inkscape:groupmode="layer"
					     id="layer1"
					     transform="translate(-30.943521,-30.943521)">
					    <rect
					       style="fill:#000000;stroke-width:0.264583"
					       id="rect111"
					       width="152.40408"
					       height="143.43915"
					       x="30.943521"
					       y="30.943521" />
					    <rect
					       style="fill:#ffffff;stroke-width:0.264583"
					       id="rect113"
					       width="134.4742"
					       height="85.889969"
					       x="35.859787"
					       y="36.148975" />
					    <rect
					       style="fill:#ff0000;stroke-width:0.264583"
					       id="rect167"
					       width="114.23077"
					       height="69.116844"
					       x="44.82473"
					       y="42.511196" />
					  </g>
					</svg>
					"""
			), "/data/logo.svg");
		}
		if(this.devserviceConfig.devData().characteristics().runBy().haveBannerImg()){
			this.addEnv("CHARACTERISTICS_RUNBY_IMG_DIR", "/data/");
			this.addEnv("CHARACTERISTICS_VAL_RUNBY_BANNERIMG", "banner.svg");
			
			this.withCopyToContainer(Transferable.of(
				"""
					<?xml version="1.0" encoding="UTF-8" standalone="no"?>
					      <!-- Created with Inkscape (http://www.inkscape.org/) -->
					
					      <svg
					         width="369.29794mm"
					         height="142.86076mm"
					         viewBox="0 0 369.29792 142.86077"
					         version="1.1"
					         id="svg5"
					         sodipodi:docname="drawing.svg"
					         inkscape:version="1.2.2 (b0a8486541, 2022-12-01)"
					         xmlns:inkscape="http://www.inkscape.org/namespaces/inkscape"
					         xmlns:sodipodi="http://sodipodi.sourceforge.net/DTD/sodipodi-0.dtd"
					         xmlns="http://www.w3.org/2000/svg"
					         xmlns:svg="http://www.w3.org/2000/svg">
					        <sodipodi:namedview
					           id="namedview7"
					           pagecolor="#ffffff"
					           bordercolor="#000000"
					           borderopacity="0.25"
					           inkscape:showpageshadow="2"
					           inkscape:pageopacity="0.0"
					           inkscape:pagecheckerboard="0"
					           inkscape:deskcolor="#d1d1d1"
					           inkscape:document-units="mm"
					           showgrid="false"
					           inkscape:zoom="0.914906"
					           inkscape:cx="110.39385"
					           inkscape:cy="284.7287"
					           inkscape:window-width="2526"
					           inkscape:window-height="1371"
					           inkscape:window-x="0"
					           inkscape:window-y="0"
					           inkscape:window-maximized="1"
					           inkscape:current-layer="layer1" />
					        <defs
					           id="defs2" />
					        <g
					           inkscape:label="Layer 1"
					           inkscape:groupmode="layer"
					           id="layer1"
					           transform="translate(-30.943521,-30.943521)">
					          <rect
					             style="fill:#000000;stroke-width:0.264583"
					             id="rect111"
					             width="369.29794"
					             height="142.86076"
					             x="30.943521"
					             y="30.943521" />
					          <rect
					             style="fill:#ffffff;stroke-width:0.264583"
					             id="rect113"
					             width="134.4742"
					             height="85.889969"
					             x="35.859787"
					             y="36.148975" />
					          <rect
					             style="fill:#ff0000;stroke-width:0.264583"
					             id="rect167"
					             width="114.23077"
					             height="69.116844"
					             x="44.82473"
					             y="42.511196" />
					          <rect
					             style="fill:#008000;stroke-width:0.264583"
					             id="rect366"
					             width="225.56961"
					             height="137.07692"
					             x="171.77994"
					             y="34.703014" />
					          <rect
					             style="fill:#008000;stroke-width:0.264583"
					             id="rect368"
					             width="127.82278"
					             height="43.957157"
					             x="34.703014"
					             y="126.37682" />
					        </g>
					      </svg>
					"""
			), "/data/banner.svg");
		}
		
		
		//configuration of UI's
		this.addEnv("CHARACTERISTICS_UIS_ICON_DIR", "/data/uis/images/");
		
		this.withCopyToContainer(Transferable.of(
			"""
				<?xml version="1.0" encoding="UTF-8" standalone="no"?>
				<!-- Created with Inkscape (http://www.inkscape.org/) -->
				
				<svg
				   width="90.806229mm"
				   height="93.119766mm"
				   viewBox="0 0 90.806229 93.119766"
				   version="1.1"
				   id="svg391"
				   inkscape:version="1.2.2 (b0a8486541, 2022-12-01)"
				   sodipodi:docname="drawing.svg"
				   xmlns:inkscape="http://www.inkscape.org/namespaces/inkscape"
				   xmlns:sodipodi="http://sodipodi.sourceforge.net/DTD/sodipodi-0.dtd"
				   xmlns="http://www.w3.org/2000/svg"
				   xmlns:svg="http://www.w3.org/2000/svg">
				  <sodipodi:namedview
				     id="namedview393"
				     pagecolor="#ffffff"
				     bordercolor="#000000"
				     borderopacity="0.25"
				     inkscape:showpageshadow="2"
				     inkscape:pageopacity="0.0"
				     inkscape:pagecheckerboard="0"
				     inkscape:deskcolor="#d1d1d1"
				     inkscape:document-units="mm"
				     showgrid="false"
				     inkscape:zoom="0.914906"
				     inkscape:cx="396.76207"
				     inkscape:cy="367.79735"
				     inkscape:window-width="2526"
				     inkscape:window-height="1371"
				     inkscape:window-x="0"
				     inkscape:window-y="0"
				     inkscape:window-maximized="1"
				     inkscape:current-layer="layer1" />
				  <defs
				     id="defs388">
				    <rect
				       x="91.812714"
				       y="143.18411"
				       width="276.53113"
				       height="260.13602"
				       id="rect681" />
				  </defs>
				  <g
				     inkscape:label="Layer 1"
				     inkscape:groupmode="layer"
				     id="layer1"
				     transform="translate(-15.905549,-21.689386)">
				    <text
				       xml:space="preserve"
				       style="font-size:3.175px;fill:#008000;stroke-width:0.264583"
				       x="82.998047"
				       y="104.39825"
				       id="text449"><tspan
				         sodipodi:role="line"
				         id="tspan447"
				         style="stroke-width:0.264583"
				         x="82.998047"
				         y="104.39825"></tspan></text>
				    <rect
				       style="fill:#d35f5f;stroke-width:0.264583"
				       id="rect625"
				       width="90.806229"
				       height="93.119766"
				       x="15.905549"
				       y="21.689386" />
				    <text
				       xml:space="preserve"
				       transform="scale(0.26458333)"
				       id="text679"
				       style="font-size:192px;white-space:pre;shape-inside:url(#rect681);fill:#008000"><tspan
				         x="91.8125"
				         y="317.87963"
				         id="tspan774">C</tspan></text>
				  </g>
				</svg>
				"""
		), "/data/uis/images/core.svg");
		this.withCopyToContainer(Transferable.of(
			"""
				<?xml version="1.0" encoding="UTF-8" standalone="no"?>
				<!-- Created with Inkscape (http://www.inkscape.org/) -->
				
				<svg
				   width="90.806229mm"
				   height="93.119766mm"
				   viewBox="0 0 90.806229 93.119766"
				   version="1.1"
				   id="svg391"
				   inkscape:version="1.2.2 (b0a8486541, 2022-12-01)"
				   sodipodi:docname="drawing.svg"
				   xmlns:inkscape="http://www.inkscape.org/namespaces/inkscape"
				   xmlns:sodipodi="http://sodipodi.sourceforge.net/DTD/sodipodi-0.dtd"
				   xmlns="http://www.w3.org/2000/svg"
				   xmlns:svg="http://www.w3.org/2000/svg">
				  <sodipodi:namedview
				     id="namedview393"
				     pagecolor="#ffffff"
				     bordercolor="#000000"
				     borderopacity="0.25"
				     inkscape:showpageshadow="2"
				     inkscape:pageopacity="0.0"
				     inkscape:pagecheckerboard="0"
				     inkscape:deskcolor="#d1d1d1"
				     inkscape:document-units="mm"
				     showgrid="false"
				     inkscape:zoom="0.914906"
				     inkscape:cx="396.76207"
				     inkscape:cy="367.79735"
				     inkscape:window-width="2526"
				     inkscape:window-height="1371"
				     inkscape:window-x="0"
				     inkscape:window-y="0"
				     inkscape:window-maximized="1"
				     inkscape:current-layer="layer1" />
				  <defs
				     id="defs388">
				    <rect
				       x="91.812714"
				       y="143.18411"
				       width="276.53113"
				       height="260.13602"
				       id="rect681" />
				  </defs>
				  <g
				     inkscape:label="Layer 1"
				     inkscape:groupmode="layer"
				     id="layer1"
				     transform="translate(-15.905549,-21.689386)">
				    <text
				       xml:space="preserve"
				       style="font-size:3.175px;fill:#008000;stroke-width:0.264583"
				       x="82.998047"
				       y="104.39825"
				       id="text449"><tspan
				         sodipodi:role="line"
				         id="tspan447"
				         style="stroke-width:0.264583"
				         x="82.998047"
				         y="104.39825"></tspan></text>
				    <rect
				       style="fill:#d35f5f;stroke-width:0.264583"
				       id="rect625"
				       width="90.806229"
				       height="93.119766"
				       x="15.905549"
				       y="21.689386" />
				    <text
				       xml:space="preserve"
				       transform="scale(0.26458333)"
				       id="text679"
				       style="font-size:192px;white-space:pre;shape-inside:url(#rect681);fill:#008000"><tspan
				         x="91.8125"
				         y="317.87963"
				         id="tspan774">P</tspan></text>
				  </g>
				</svg>
				"""
		), "/data/uis/images/plugin.svg");
		this.withCopyToContainer(Transferable.of(
			"""
				<?xml version="1.0" encoding="UTF-8" standalone="no"?>
				<!-- Created with Inkscape (http://www.inkscape.org/) -->
				
				<svg
				   width="90.806229mm"
				   height="93.119766mm"
				   viewBox="0 0 90.806229 93.119766"
				   version="1.1"
				   id="svg391"
				   inkscape:version="1.2.2 (b0a8486541, 2022-12-01)"
				   sodipodi:docname="drawing.svg"
				   xmlns:inkscape="http://www.inkscape.org/namespaces/inkscape"
				   xmlns:sodipodi="http://sodipodi.sourceforge.net/DTD/sodipodi-0.dtd"
				   xmlns="http://www.w3.org/2000/svg"
				   xmlns:svg="http://www.w3.org/2000/svg">
				  <sodipodi:namedview
				     id="namedview393"
				     pagecolor="#ffffff"
				     bordercolor="#000000"
				     borderopacity="0.25"
				     inkscape:showpageshadow="2"
				     inkscape:pageopacity="0.0"
				     inkscape:pagecheckerboard="0"
				     inkscape:deskcolor="#d1d1d1"
				     inkscape:document-units="mm"
				     showgrid="false"
				     inkscape:zoom="0.914906"
				     inkscape:cx="396.76207"
				     inkscape:cy="367.79735"
				     inkscape:window-width="2526"
				     inkscape:window-height="1371"
				     inkscape:window-x="0"
				     inkscape:window-y="0"
				     inkscape:window-maximized="1"
				     inkscape:current-layer="layer1" />
				  <defs
				     id="defs388">
				    <rect
				       x="91.812714"
				       y="143.18411"
				       width="276.53113"
				       height="260.13602"
				       id="rect681" />
				  </defs>
				  <g
				     inkscape:label="Layer 1"
				     inkscape:groupmode="layer"
				     id="layer1"
				     transform="translate(-15.905549,-21.689386)">
				    <text
				       xml:space="preserve"
				       style="font-size:3.175px;fill:#008000;stroke-width:0.264583"
				       x="82.998047"
				       y="104.39825"
				       id="text449"><tspan
				         sodipodi:role="line"
				         id="tspan447"
				         style="stroke-width:0.264583"
				         x="82.998047"
				         y="104.39825"></tspan></text>
				    <rect
				       style="fill:#d35f5f;stroke-width:0.264583"
				       id="rect625"
				       width="90.806229"
				       height="93.119766"
				       x="15.905549"
				       y="21.689386" />
				    <text
				       xml:space="preserve"
				       transform="scale(0.26458333)"
				       id="text679"
				       style="font-size:192px;white-space:pre;shape-inside:url(#rect681);fill:#008000"><tspan
				         x="91.8125"
				         y="317.87963"
				         id="tspan774">I</tspan></text>
				  </g>
				</svg>
				"""
		), "/data/uis/images/infra.svg");
		this.withCopyToContainer(Transferable.of(
			"""
				<?xml version="1.0" encoding="UTF-8" standalone="no"?>
				<!-- Created with Inkscape (http://www.inkscape.org/) -->
				
				<svg
				   width="90.806229mm"
				   height="93.119766mm"
				   viewBox="0 0 90.806229 93.119766"
				   version="1.1"
				   id="svg391"
				   inkscape:version="1.2.2 (b0a8486541, 2022-12-01)"
				   sodipodi:docname="drawing.svg"
				   xmlns:inkscape="http://www.inkscape.org/namespaces/inkscape"
				   xmlns:sodipodi="http://sodipodi.sourceforge.net/DTD/sodipodi-0.dtd"
				   xmlns="http://www.w3.org/2000/svg"
				   xmlns:svg="http://www.w3.org/2000/svg">
				  <sodipodi:namedview
				     id="namedview393"
				     pagecolor="#ffffff"
				     bordercolor="#000000"
				     borderopacity="0.25"
				     inkscape:showpageshadow="2"
				     inkscape:pageopacity="0.0"
				     inkscape:pagecheckerboard="0"
				     inkscape:deskcolor="#d1d1d1"
				     inkscape:document-units="mm"
				     showgrid="false"
				     inkscape:zoom="0.914906"
				     inkscape:cx="396.76207"
				     inkscape:cy="367.79735"
				     inkscape:window-width="2526"
				     inkscape:window-height="1371"
				     inkscape:window-x="0"
				     inkscape:window-y="0"
				     inkscape:window-maximized="1"
				     inkscape:current-layer="layer1" />
				  <defs
				     id="defs388">
				    <rect
				       x="91.812714"
				       y="143.18411"
				       width="276.53113"
				       height="260.13602"
				       id="rect681" />
				  </defs>
				  <g
				     inkscape:label="Layer 1"
				     inkscape:groupmode="layer"
				     id="layer1"
				     transform="translate(-15.905549,-21.689386)">
				    <text
				       xml:space="preserve"
				       style="font-size:3.175px;fill:#008000;stroke-width:0.264583"
				       x="82.998047"
				       y="104.39825"
				       id="text449"><tspan
				         sodipodi:role="line"
				         id="tspan447"
				         style="stroke-width:0.264583"
				         x="82.998047"
				         y="104.39825"></tspan></text>
				    <rect
				       style="fill:#d35f5f;stroke-width:0.264583"
				       id="rect625"
				       width="90.806229"
				       height="93.119766"
				       x="15.905549"
				       y="21.689386" />
				    <text
				       xml:space="preserve"
				       transform="scale(0.26458333)"
				       id="text679"
				       style="font-size:192px;white-space:pre;shape-inside:url(#rect681);fill:#008000"><tspan
				         x="91.8125"
				         y="317.87963"
				         id="tspan774">M</tspan></text>
				  </g>
				</svg>
				"""
		), "/data/uis/images/metrics.svg");
		
		this.serviceConfig.serviceId();
		
		this.withCopyToContainer(Transferable.of(
			"""
				{
				   "type": "%s",
				   "order": 0,
				   "id": "%s",
				   "name": "Consuming Service",
				   "description": "The service that is using this library",
				   "url": "http://foo",
				   "urlConfigKey": "core.baseStation.externalBaseUri",
				   "icon": "core.svg",
				   "monitorEndpoint": "/q/health",
				   "endpoints": {
				      "item": {
				         "view": "/items?item={item}"
				      }
				   }
				}
				""".formatted(
					this.serviceConfig.serviceCategory(),
					this.serviceConfig.serviceId()
			)
		), "/data/uis/consuming.json");
		
		for(String curCat : List.of("core", "plugin", "infra", "metrics")) {
			for(int i = 1; i <= 3; i++) {
				this.withCopyToContainer(Transferable.of(
					"""
						{
						   "type": "%s",
						   "order": 0,
						   "id": "%s",
						   "name": "Example Service %s",
						   "description": "An example service",
						   "url": "http://foo",
						   "urlConfigKey": "core.baseStation.externalBaseUri",
						   "icon": "core.svg",
						   "monitorEndpoint": "/q/health",
						   "endpoints": {
							  "item": {
								 "view": "/items?item={item}"
							  }
						   }
						}
						""".formatted(
						curCat,
						curCat+"-"+i,
						curCat+"-"+i
					)
				), "/data/uis/"+curCat+"-"+i+".json");
			}
		}
		
	}
}
