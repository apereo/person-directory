package org.apereo.services.persondir.support;

import com.sun.net.httpserver.HttpServer;
import org.apereo.services.persondir.AbstractPersonAttributeDaoTest;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.*;

public class RestfulPersonAttributeDaoTest extends AbstractPersonAttributeDaoTest {
    private RestfulPersonAttributeDao dao;

    private HttpServer httpServer;

    public RestfulPersonAttributeDaoTest() {
        this.dao = new RestfulPersonAttributeDao();
    }

    @BeforeEach
    public void setUp() throws Exception {
        httpServer = HttpServer.create();
        httpServer.bind(new InetSocketAddress("localhost", 8080), 0);
        httpServer.createContext("/test", exchange -> {
            var json = "{ \n" +
                       "  \"backgroundcolor\":\"#656667\",\n" +
                       "  \"height\":4\n" +
                       '}';
            var response = json.getBytes();
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        httpServer.start();
    }

    @Test
    public void testGetAttributes() {
        this.dao.setUrl("http://localhost:8080/test");
        this.dao.setMethod(HttpMethod.GET.name());
        var person = this.dao.getPerson("something", IPersonAttributeDaoFilter.alwaysChoose());
        assertEquals(person.getName(), "something");
        assertEquals(person.getAttributes().size(), 2);
    }

    @AfterEach
    protected void tearDown() throws Exception {
        this.httpServer.stop(0);
    }

    @Override
    protected IPersonAttributeDao getPersonAttributeDaoInstance() {
        return this.dao;
    }
}
