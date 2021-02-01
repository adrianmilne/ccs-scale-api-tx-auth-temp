package uk.gov.crowncommercial.dsd.api.auth.routes;

import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.apache.camel.builder.endpoint.dsl.RestEndpointBuilderFactory.RestBindingMode;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import uk.gov.crowncommercial.dsd.api.auth.model.TokenResponse;

/**
 *
 */
@Component
@RequiredArgsConstructor
public class AuthServiceRouteBuilder extends EndpointRouteBuilder {

  private static final String SPREE_TOKEN_ENDPOINT =
      "https://dev.scale-bat-backend.alb.crowncommercial.gov.uk/spree_oauth/token";

  private static final String JSON_BINDING = RestBindingMode.json.name();
  private static final String PATH_ROOT = "/oauth2";

  private static final String ROUTE_ID_GET_TOKEN = "get-token";
  private static final String ROUTE_GET_TOKEN = "direct:" + ROUTE_ID_GET_TOKEN;
  private static final String ROUTE_FINALISE_RESPONSE = "direct:finalise-response";

  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder.build();
  }

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
      .post(PATH_ROOT + "/token").produces("application/json")
      .outType(TokenResponse.class)
      .to(ROUTE_GET_TOKEN);

    from(ROUTE_GET_TOKEN)
      .routeId(ROUTE_ID_GET_TOKEN)
      .log(LoggingLevel.INFO, "Endpoint token invoked")

      
      .transform(simple("{\"grant_type\": \"${body['grant_type']}\",\"username\": \"${body['username']}\",\"password\": \"${body['password']}\"}"))
      .to(SPREE_TOKEN_ENDPOINT +
          "?httpMethod=POST&bridgeEndpoint=true").setHeader(Exchange.CONTENT_TYPE, simple("application/json"))

      .process(new Processor() {              
        @Override
        public void process(Exchange exchange) throws Exception {
          Message in = exchange.getIn();
          String msg = in.getBody(String.class);
          ObjectMapper om = new ObjectMapper();
          TokenResponse response = om.readValue(msg,  TokenResponse.class);
          log("Response: " + response);
          exchange.getMessage().setBody(response);
        }
      })
      
      //.setBody(e -> new TokenResponse("A", "B", 1, "C", 2))
      .to(ROUTE_FINALISE_RESPONSE);

    from(ROUTE_FINALISE_RESPONSE)
      .removeHeaders("*")
      .setHeader(ACCESS_CONTROL_ALLOW_ORIGIN, constant("*"));
    // @formatter:on
  }

}
