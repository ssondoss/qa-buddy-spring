package com.sondos.qa_buddy.security;

import java.time.Instant;
import java.util.UUID;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
	private final JwtEncoder jwtEncoder;
	private final JwtProperties properties;

	public JwtService(JwtEncoder jwtEncoder, JwtProperties properties) {
		this.jwtEncoder = jwtEncoder;
		this.properties = properties;
	}

	public JwtToken createAccessToken(UUID userId, String email) {
		Instant issuedAt = Instant.now();
		Instant expiresAt = issuedAt.plus(properties.getTtl());

		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(properties.getIssuer())
				.subject(email)
				.issuedAt(issuedAt)
				.expiresAt(expiresAt)
				.claim("uid", userId.toString())
				.build();

		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		String tokenValue = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
		return new JwtToken(tokenValue, expiresAt);
	}
}

