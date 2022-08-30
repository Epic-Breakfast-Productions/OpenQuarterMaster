package tech.ebp.oqm.baseStation.exception.mappers;

import lombok.extern.slf4j.Slf4j;
import org.jose4j.jwt.consumer.InvalidJwtException;

import javax.ws.rs.ext.Provider;

@Slf4j
@Provider
public class InvalidJwtExceptionMapper extends UiNotAuthorizedExceptionMapper<InvalidJwtException> {

}
