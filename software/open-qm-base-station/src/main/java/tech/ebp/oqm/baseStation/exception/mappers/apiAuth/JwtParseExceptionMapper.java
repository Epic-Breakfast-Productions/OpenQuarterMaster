package tech.ebp.oqm.baseStation.exception.mappers.apiAuth;

import io.smallrye.jwt.auth.principal.ParseException;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.ext.Provider;

@Slf4j
@Provider
public class JwtParseExceptionMapper extends UiNotAuthorizedExceptionMapper<ParseException> {

}
