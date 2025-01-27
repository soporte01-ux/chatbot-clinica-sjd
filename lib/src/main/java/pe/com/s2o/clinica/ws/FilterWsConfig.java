package pe.com.s2o.clinica.ws;

import java.util.Collections;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;


public class FilterWsConfig extends ServerEndpointConfig.Configurator {

    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        response.getHeaders().put("Access-Control-Allow-Origin", Collections.singletonList("*"));
    }
}
