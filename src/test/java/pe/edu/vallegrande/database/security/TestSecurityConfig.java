package pe.edu.vallegrande.database.security;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Configuración de seguridad para pruebas.
 * 
 * Esta clase permite el acceso a todas las rutas durante las pruebas.
 * No debe ser utilizada en producción.
 */
@TestConfiguration
@EnableWebFluxSecurity
public class TestSecurityConfig {

    /**
     * Configura la cadena de filtros de seguridad para las pruebas.
     * 
     * @param http la configuración de seguridad del servidor HTTP
     * @return la cadena de filtros de seguridad configurada
     */
    @Bean
    @Primary
    public SecurityWebFilterChain testSecurityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(auth -> auth
                        .anyExchange().permitAll() // Configuración para entorno de testing
                )
                .build();
    }
}