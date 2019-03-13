package org.apereo.services.persondir.support;

import com.sun.net.httpserver.HttpServer;
import org.apereo.services.persondir.AbstractPersonAttributeDaoTest;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.apereo.services.persondir.IPersonAttributes;
import org.junit.Before;
import org.springframework.http.HttpMethod;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;


public class RestfulPersonAttributeDaoTest extends AbstractPersonAttributeDaoTest {
    private RestfulPersonAttributeDao dao;
    private HttpServer httpServer;

    public RestfulPersonAttributeDaoTest() {
        this.dao = new RestfulPersonAttributeDao();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        httpServer = HttpServer.create();
        httpServer.bind(new InetSocketAddress("localhost",8080), 0);
        httpServer.createContext("/test", exchange -> {
            final String json = "{ \n" +
                    "  \"backgroundcolor\":\"#656667\",\n" +
                    "  \"height\":4\n" +
                    '}';
            final byte[] response = json.getBytes();
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        httpServer.start();
    }

    public void testGetAttributes() {
        this.dao.setUrl("http://localhost:8080/test");
        this.dao.setMethod(HttpMethod.GET.name());
        final IPersonAttributes person = this.dao.getPerson("something", IPersonAttributeDaoFilter.alwaysChoose());
        assertEquals(person.getName(), "something");
        assertEquals(person.getAttributes().size(), 2);
    }

    @Override
    protected void tearDown() throws Exception {
        this.httpServer.stop(0);
    }

    @Override
    protected IPersonAttributeDao getPersonAttributeDaoInstance() {
        return this.dao;
    }
}
