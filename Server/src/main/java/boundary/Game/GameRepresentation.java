
package boundary.Game;

import boundary.Question.QuestionResource;
import boundary.Representation;
import boundary.User.UserResource;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import entity.Game;
import entity.User;
import entity.UserRole;
import provider.Secured;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;

@Path("/games")
@Stateless
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GameRepresentation extends Representation {

    @EJB
    private GameResource gameResource;
    
    @EJB
    private UserResource userResource;
    
    @EJB
    private QuestionResource questionResource;

    @GET
    @Path("/{id}")
    @Secured({UserRole.CUSTOMER})
    @ApiOperation(value = "Get a game by its id", notes = "Access : Owner only")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    public Response get(@Context SecurityContext securityContext, @PathParam("id") String id) {
       Game game = gameResource.findById(id);
       if (game == null)
            flash(404, "Error : Game does not exist");

       String currentEmail = securityContext.getUserPrincipal().getName();
       String ownersEmail = game.getUser().getEmail();

       if (!ownersEmail.equals(currentEmail))
           return Response.status(Response.Status.UNAUTHORIZED).build();

       return Response.ok(game, MediaType.APPLICATION_JSON).build();
    }
    
    @POST
    @Secured({UserRole.CUSTOMER})
    @ApiOperation(value = "Get a game by its id", notes = "Access : Owner only")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    public Response add(@Context SecurityContext securityContext, Game game) {
        if (game == null)
            flash(400, EMPTY_JSON);
        
        if (!game.isValid())
            flash(400, INVALID_JSON);

        User user = userResource.findByEmail(securityContext.getUserPrincipal().getName());

        game.setUser(user);

        if (questionResource.findById(game.getQuestion().getId()) == null)
            flash(400, "Error : the question does not exist");

        return Response.ok(gameResource.insert(game), MediaType.APPLICATION_JSON).build();
    }
    

    @DELETE
    @Path("/{id}")
    @Secured({UserRole.ADMIN})
    @ApiOperation(value = "Delete a game by its id", notes = "Access : Admin only")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "No content"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    public Response delete(@PathParam("id") String id) {
        Game game = gameResource.findById(id);

        if (game == null)
            flash(404, "Error : Game does not exist");

        gameResource.delete(game);

        return Response.status(Response.Status.NO_CONTENT).build();
    }
}