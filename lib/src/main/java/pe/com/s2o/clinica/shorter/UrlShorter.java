package pe.com.s2o.clinica.shorter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import pe.com.s2o.clinica.whatsapp.GlobalConstants;

/**
 * Session Bean implementation class urlShorter
 */
@Stateless
@LocalBean
@Path("/recorter")
public class UrlShorter {

	private final UrlStorage storage = new UrlStorage();
    private final SlugGenerator slugGenerator = new SlugGenerator();
    
    @POST
    @Path("/shorten")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response shortenUrl(Map<String, Object> mapIn) throws UnsupportedEncodingException {
        String slug = slugGenerator.generateSlug();
        String url = mapIn.get("url").toString();
        System.out.println(url);
        // Asegurar que el slug no exista ya
        while (storage.exists(slug)) {
            slug = slugGenerator.generateSlug();
        }

        storage.save(slug, url);

        // Devuelve la URL acortada
        String shortUrl = UriBuilder.fromUri(GlobalConstants.API_URL_RECORTER_RESPONSE + "/rs/recorter").path(slug).build().toString();
        return Response.ok(shortUrl).build();
    }

    @GET
    @Path("/{slug}")
    public Response redirectToUrl(@PathParam("slug") String slug) {
        String longUrl = storage.get(slug);
        System.out.println(longUrl);
        if (longUrl == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Slug no encontrado").build();
        }

        // Redirigir al usuario a la URL original
        return Response.seeOther(UriBuilder.fromUri(longUrl).build()).build();
    }

}
