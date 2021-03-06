package boundary.User;


import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import control.KeyGenerator;

import entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.mindrot.jbcrypt.BCrypt;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Path("/authentication")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "/authentication", description = "To login")

public class AuthenticationEndpoint {

    @Inject
    private KeyGenerator keyGenerator;

    @EJB
    private UserResource accountResource;

    @Context
    private UriInfo uriInfo;

    @POST
    @ApiOperation(value = "Login as a customer", notes = "Access : Everyone")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 401, message = "Unauthorized : invalid credentials"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    public Response authenticateUser(User user) {
        try {
            authenticate(user.getEmail(), user.getPassword());
            JsonObject token = Json.createObjectBuilder().add("token", issueToken(user.getEmail())).build();
            return Response.ok(token , MediaType.APPLICATION_JSON).build();
        } catch (Exception e) {
            return Response.status(Response.Status.UNAUTHORIZED).type("text/plain").entity("Invalid credentials").build();
        }
    }

    /**
     * Authenticate against a LDAP
     * @param email
     * @param password
     * @throws NotAuthorizedException if the credentials are invalid
     */ 
    private void authenticate(String email, String password) throws NotAuthorizedException {
        User user = accountResource.findByEmail(email);

        if (user == null && !BCrypt.checkpw(password,user.getPassword()))
            throw new SecurityException("Email address or password is invalid");
    }

    /**
     * Helper method that converts a Date for a localDateTime given
     * @param localDateTime to convert
     * @return Date
     */
    private Date toDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Method that issues a JWT token
     * @param email of the associated user
     * @return the issued token
     */
    private String issueToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuer(uriInfo.getAbsolutePath().toString())
                .setIssuedAt(new Date())
                .setExpiration(toDate(LocalDateTime.now().plusMinutes(15L)))
                .signWith(SignatureAlgorithm.HS512, keyGenerator.generateKey())
                .compact();
    }
}