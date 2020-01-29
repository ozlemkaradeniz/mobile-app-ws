package com.appdeveloper.app.ws.shared;

import java.security.SecureRandom;
import java.util.Date;
import java.util.Random;

import org.springframework.stereotype.Component;

import com.appdeveloper.app.ws.security.SecurityConstants;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class Utils {
	
	private final String ALPHABET="0123456789ABCDEFGHIJKLMNOPQRSTUVXWYZabcdefghijklmnopqrstuvxwyz";
	private Random RANDOM = new SecureRandom();

	public String generateUserId(int length) {
	
		StringBuilder returnValue = new StringBuilder();
		
		for(int i= 0; i < length; i++)
			returnValue.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
		
		return returnValue.toString();
	}
	
	public String generateAddressId(int length) {
		
		StringBuilder returnValue = new StringBuilder();
		
		for(int i= 0; i < length; i++)
			returnValue.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
		
		return returnValue.toString();
	}
	
	public static boolean hasTokenExpired(String token) {
		boolean returnValue = false;

		try {
			Claims claims = Jwts.parser().setSigningKey(SecurityConstants.getTokenSecret()).parseClaimsJws(token)
					.getBody();

			Date tokenExpirationDate = claims.getExpiration();
			Date todayDate = new Date();

			returnValue = tokenExpirationDate.before(todayDate);
		} catch (ExpiredJwtException ex) {
			returnValue = true;
		}

		return returnValue;
	}
	
    public String generateEmailVerificationToken(String userId) {
        String token = Jwts.builder()
                .setSubject(userId)
                .setExpiration(new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, SecurityConstants.getTokenSecret())
                .compact();
        return token;
    }
    
    
    
    public String generatePasswordResetToken(String userId) {
        String token = Jwts.builder()
                .setSubject(userId)
                .setExpiration(new Date(System.currentTimeMillis() + SecurityConstants.PASSWORD_RESET_EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, SecurityConstants.getTokenSecret())
                .compact();
        return token;
    }
}
