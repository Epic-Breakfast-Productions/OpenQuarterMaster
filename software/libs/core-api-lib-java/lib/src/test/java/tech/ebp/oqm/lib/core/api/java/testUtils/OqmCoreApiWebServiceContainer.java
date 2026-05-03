package tech.ebp.oqm.lib.core.api.java.testUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.nio.file.Path;
import java.security.KeyPair;
import java.util.Base64;
import java.util.Map;

import static tech.ebp.oqm.lib.core.api.java.testUtils.TestContainerUtils.KEYCLOAK_DEVSERVICE_HOSTNAME;

public class OqmCoreApiWebServiceContainer extends GenericContainer<OqmCoreApiWebServiceContainer> {
	
	static final int HTTP_PORT = 8123;
	static final int HTTPS_PORT = 8443;
	
	public OqmCoreApiWebServiceContainer(DockerImageName image) {
		super(image);
	}
	
	@Override
	protected void configure() {
		this.withNetwork(Network.SHARED);
		//TODO:: use config to get port
		this.withEnv("quarkus.http.port", "" + HTTP_PORT);
		this.withEnv("mp.messaging.outgoing.events-outgoing.enabled", "false");
		
		this.withReuse(false);
		
		this.addExposedPorts(HTTP_PORT, HTTPS_PORT);
		// Tell the dev service how to know the container is ready
		this.waitingFor(Wait.forLogMessage(".*Open QuarterMaster Web Server starting.*", 1));
		
		
		this.waitingFor(
			Wait.forHttp("/q/health").forResponsePredicate((String response)->{
			ObjectNode status;
			try {
				status = (ObjectNode) new ObjectMapper().readTree(response);
			} catch(Exception e) {
				return false;
			}
			return status.get("status").asText().equals("UP");
		})
		);
	}
	
	public Integer getHttpPort() {
		return this.getMappedPort(HTTP_PORT);
	}
	public Integer getHttpsPort() {
		return this.getMappedPort(HTTPS_PORT);
	}
	public Integer getPort(boolean https) {
		if(https){
			return this.getHttpsPort();
		}
		return this.getHttpPort();
	}
	
	/**
	 * https://quarkus.io/guides/security-jwt#dealing-with-verification-keys
	 * @return
	 */
	public OqmCoreApiWebServiceContainer setupForPlainJwtAuth() {
		this.withEnv(Map.of(
			"mp.jwt.verify.publickey", Base64.getEncoder().encodeToString(JwtUtils.SIGNING_KEYPAIR.getPublic().getEncoded())
		));
		return this;
	}
	
	public OqmCoreApiWebServiceContainer setupForKcAuth(KeycloakContainer keycloakContainer) {
		//TODO: rework for keycloak
		this.withEnv(Map.of(
			"smallrye.jwt.verify.key.location",
			String.format(
				"http://%s:%s/realms/%s/protocol/openid-connect/certs",
				KEYCLOAK_DEVSERVICE_HOSTNAME,
				"8080",
				"oqm"
			)
		));
		
		return this;
	}
	
	public OqmCoreApiWebServiceContainer setupForHttps(){
		this.withCopyFileToContainer(MountableFile.forHostPath(CertUtils.httpsPublicKey), "/tmp/pubKey.pem");
		this.withCopyFileToContainer(MountableFile.forHostPath(CertUtils.httpsPrivateKey), "/tmp/privKey.pem");
		
		this.withEnv(Map.of(
			"quarkus.http.ssl.port", Integer.toString(HTTPS_PORT),
			"quarkus.http.insecure-requests", "enabled",
			"quarkus.tls.key-store.pem.0.cert", "/tmp/pubKey.pem",
			"quarkus.tls.key-store.pem.0.key", "/tmp/privKey.pem"
		));
		return this;
	}
}
