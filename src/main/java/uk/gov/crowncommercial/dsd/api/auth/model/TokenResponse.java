package uk.gov.crowncommercial.dsd.api.auth.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class TokenResponse {

  @JsonProperty("access_token")
  String accessToken;

  @JsonProperty("token_type")
  String tokenType;

  @JsonProperty("expires_in")
  Integer expiresIn;

  @JsonProperty("refresh_token")
  String refreshToken;

  @JsonProperty("created_at")
  Integer createdAt;

}
