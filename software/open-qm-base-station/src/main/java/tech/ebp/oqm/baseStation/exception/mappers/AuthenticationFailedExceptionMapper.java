package tech.ebp.oqm.baseStation.exception.mappers;

import io.quarkus.security.AuthenticationFailedException;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.ext.Provider;

@Slf4j
@Provider
public class AuthenticationFailedExceptionMapper extends UiNotAuthorizedExceptionMapper<AuthenticationFailedException> {

}
