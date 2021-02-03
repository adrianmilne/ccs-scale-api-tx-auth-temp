package uk.gov.crowncommercial.dsd.api.auth.routes;

import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import uk.gov.crowncommercial.dsd.api.auth.model.TokenResponse;

/**
 * Authorisation Service RouteBuilder
 */
@Component
@RequiredArgsConstructor
public class AuthServiceRouteBuilder extends EndpointRouteBuilder {

  // private static final String SPREE_TOKEN_ENDPOINT =
  // "https://dev.scale-bat-backend.alb.crowncommercial.gov.uk/spree_oauth/token";

  public static final String SPREE_TOKEN_ENDPOINT_TEMPLATE =
      "%s/spree_oauth/token?httpMethod=POST&bridgeEndpoint=true";
  public static final String ROUTE_ID_GET_TOKEN = "get-token";
  private static final String ROUTE_GET_TOKEN = "direct:" + ROUTE_ID_GET_TOKEN;
  private static final String ROUTE_FINALISE_RESPONSE = "direct:finalise-response";

  @Value("${api.paths.base}")
  private String apiBasePath;

  @Value("${api.paths.get-token}")
  private String apiGetToken;

  @Value("${SPREE_API_HOST}")
  private String spreeApiHost;

  @Override
  public void configure() throws Exception {

    // @formatter:off
    restConfiguration()
    .component("servlet")
    .bindingMode(RestBindingMode.json);

    /*
     * Get Token
     */
    rest(apiBasePath)
      .post(apiGetToken).produces(MediaType.APPLICATION_JSON_VALUE)
      .outType(TokenResponse.class)
      .to(ROUTE_GET_TOKEN);

    from(ROUTE_GET_TOKEN)
      .routeId(ROUTE_ID_GET_TOKEN)
      .streamCaching()
      .log(LoggingLevel.INFO, "Endpoint /token invoked, calling '{}'", spreeApiHost)
      .transform(simple("{\"grant_type\": \"${body['grant_type']}\",\"username\": \"${body['username']}\",\"password\": \"${body['password']}\"}"))
      .to(String.format(SPREE_TOKEN_ENDPOINT_TEMPLATE, spreeApiHost)).setHeader(Exchange.CONTENT_TYPE, simple(MediaType.APPLICATION_JSON_VALUE))
      .log(LoggingLevel.INFO, "${body}")
      .convertBodyTo(String.class).unmarshal().json()
      .to(ROUTE_FINALISE_RESPONSE);

    from(ROUTE_FINALISE_RESPONSE)
      .removeHeaders("*")
      .setHeader(ACCESS_CONTROL_ALLOW_ORIGIN, constant("*"));
    // @formatter:on
  }

}
