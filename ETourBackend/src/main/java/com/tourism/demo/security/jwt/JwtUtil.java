package com.tourism.demo.security.jwt;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

public class JwtUtil {
	private final  String secretKey;
	
	
	
	public JwtUtil(String secretKey) {
	        this.secretKey=secretKey;  // ✅ Now it's valid
	    }
	   
	   
	   
	   
	//private final static  String secretKey=JwtProperties.getJwtSecretKey();
	
	public  String generateToken(String username,String role) {
//		Map<String,Object> claims=new HashMap<>();
//		claims.put("urole",role);
//		return Jwts.builder()
//				.claims()
//				.add(claims)
//				.subject(username)
//				.issuedAt(new Date(System.currentTimeMillis()))
//				.expiration(new Date(System.currentTimeMillis()+1000*60*30))
//				.and()
//				.signWith(getKey())
//				.compact();
		
		    Map<String, Object> claims = new HashMap<>();
		    claims.put("sub", username);  // Add subject to claims map
		    claims.put("urole", role);
		    
		    return Jwts.builder()
		        .claims(claims)  // Set all claims at once
		        .issuedAt(new Date(System.currentTimeMillis()))
		        .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 30))
		        .signWith(getKey())
		        .compact();
		
	}
	
 public  Key getKey() {
		byte[] skByte=Decoders.BASE64.decode(secretKey);

		return Keys.hmacShaKeyFor(skByte);
	}
	
	public  Claims extractAllClaims(String token) {
		return Jwts.parser()
				.verifyWith((SecretKey) getKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}
	
	public  String extractUsername(String token) {
		Claims claims=extractAllClaims(token);
		
		return (String)claims.get("sub");
	}
	
	public boolean isValid(String token) {
		@SuppressWarnings("unused")
		Claims claim=extractAllClaims(token);
		
		return !isExpired(token);
	}
	
	public boolean isExpired(String token) {
		Claims claim=extractAllClaims(token);
		
		return claim.getExpiration().before(new Date());
	}
	
	public String extractRole(String token) {
		Claims claim=extractAllClaims(token);
		
		return (String) claim.get("urole");
	}
	
	
	
}
