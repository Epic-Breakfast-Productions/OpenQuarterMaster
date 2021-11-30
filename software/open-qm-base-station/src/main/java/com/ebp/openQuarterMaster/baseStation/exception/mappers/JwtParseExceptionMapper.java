package com.ebp.openQuarterMaster.baseStation.exception.mappers;

import io.smallrye.jwt.auth.principal.ParseException;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.ext.Provider;

@Slf4j
@Provider
public class JwtParseExceptionMapper extends UiNotAuthorizedExceptionMapper<ParseException> {

}
