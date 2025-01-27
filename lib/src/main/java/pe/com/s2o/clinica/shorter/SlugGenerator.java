package pe.com.s2o.clinica.shorter;

import java.util.UUID;

public class SlugGenerator {
    public String generateSlug() {
        return UUID.randomUUID().toString().substring(0, 6); // Slug de 6 caracteres
    }
}
