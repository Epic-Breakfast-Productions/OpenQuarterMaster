package tech.ebp.oqm.core.baseStation.interfaces.rest;

import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.core.baseStation.interfaces.RestInterface;


@Slf4j
public abstract class ApiProvider extends RestInterface {
	public static final String API_ROOT = "/api";
}
