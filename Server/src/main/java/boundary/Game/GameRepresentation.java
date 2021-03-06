
package boundary.Game;

import boundary.Question.QuestionResource;
import boundary.Representation;
import boundary.User.UserResource;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import entity.Game;
import entity.Question;
import entity.User;
import entity.UserRole;
import java.util.List;
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
@Api(value = "/games", description = "Games management")
public class GameRepresentation extends Representation {

    @EJB
    private GameResource gameResource;
    
    @EJB
    private UserResource userResource;
    
    @EJB
    private QuestionResource questionResource;

    @POST
    @Path("/{id}")
    @ApiOperation(value = "Retrieve a game by its id and its token", notes = "Access : Everyone")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad request"),
            @ApiResponse(code = 404, message = "Game Not Found"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    public Response get(@PathParam("id") String id, String token) {
        if (token == null)
            return flash(400, EMPTY_JSON);

       Game game = gameResource.findById(id);

       if (game == null)
            return flash(404, "Error : Game does not exist");

       if (!token.equals(game.getToken()))
           return Response.status(Response.Status.UNAUTHORIZED).build();

       return Response.ok(game, MediaType.APPLICATION_JSON).build();
    }
    
    @POST
    @ApiOperation(value = "Create a random game", notes = "Access : Everyone")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    public Response add() {
        Game game = new Game();
        game.init();

        List<Question> questions = questionResource.findAll();

        if (questions.isEmpty())
            return flash(500, "Error : the database does not have questions");

        double rnd = Math.random() * (questions.size());
        int i = (int)(rnd - (rnd%1));
        game.setQuestion(questions.get(i));
        game = gameResource.insert(game);
        return Response.ok(game, MediaType.APPLICATION_JSON).build();
    }
    

    @DELETE
    @Secured({UserRole.ADMIN})
    @Path("/{id}")
    @ApiOperation(value = "Delete a game by its id", notes = "Access : Owner only")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "No content"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    public Response delete(@PathParam("id") String id) {
        Game game = gameResource.findById(id);

        if (game == null)
            return flash(404, "Error : Game does not exist");

        gameResource.delete(game);

        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
