package uk.gov.crowncommercial.dsd.api.auth.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class TokenRequest {

  @JsonProperty("grant_type")
  String grantType;

  @JsonProperty("username")
  String userName;

  @JsonProperty("password")
  String password;
}
