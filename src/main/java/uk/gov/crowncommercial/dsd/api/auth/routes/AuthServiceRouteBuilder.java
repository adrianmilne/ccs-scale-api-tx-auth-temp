package uk.gov.crowncommercial.dsd.api.auth.routes;

import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.apache.camel.builder.endpoint.dsl.RestEndpointBuilderFactory.RestBindingMode;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import uk.gov.crowncommercial.dsd.api.auth.model.Token;

/**
 *
 */
@Component
@RequiredArgsConstructor
public class AuthServiceRouteBuilder extends EndpointRouteBuilder {

  private static final String JSON_BINDING = RestBindingMode.json.name();
  private static final String PATH_ROOT = "/oauth";

  private static final String ROUTE_ID_GET_TOKEN = "get-token";
  private static final String ROUTE_GET_TOKEN = "direct:" + ROUTE_ID_GET_TOKEN;
  private static final String ROUTE_FINALISE_RESPONSE = "direct:finalise-response";

  @Override
  public void configure() throws Exception {

    // @formatter:off
    restConfiguration()
      .component("servlet")
      .bindingMode(JSON_BINDING);

    /*
     * GET Token
     */
    rest()
      .post(PATH_ROOT + "/token")
      .outType(Token.class)
      .to(ROUTE_GET_TOKEN);

    from(ROUTE_GET_TOKEN)
      .routeId(ROUTE_ID_GET_TOKEN)
      .log(LoggingLevel.INFO, "Endpoint token invoked")

      // TODO: Magic happens

      .setBody(e -> new Token("A", "B", 1, "C", 2))
      .to(ROUTE_FINALISE_RESPONSE);

    from(ROUTE_FINALISE_RESPONSE)
      .removeHeaders("*")
      .setHeader(ACCESS_CONTROL_ALLOW_ORIGIN, constant("*"));
    // @formatter:on
  }

}
