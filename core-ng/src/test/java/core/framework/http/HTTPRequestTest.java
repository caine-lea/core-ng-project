package core.framework.http;

import core.framework.util.Strings;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

/**
 * @author neo
 */
class HTTPRequestTest {
    @Test
    void body() {
        var request = new HTTPRequest(HTTPMethod.POST, "http://localhost/uri");
        request.body("text", ContentType.TEXT_PLAIN);

        assertThat(request.method).isEqualTo(HTTPMethod.POST);
        assertThat(request.contentType).isEqualTo(ContentType.TEXT_PLAIN);
        assertThat(request.headers).containsEntry(HTTPHeaders.CONTENT_TYPE, "text/plain; charset=utf-8");
        assertThat(request.body).isEqualTo(Strings.bytes("text"));
    }

    @Test
    void accept() {
        var request = new HTTPRequest(HTTPMethod.PATCH, "http://localhost/uri");
        request.accept(ContentType.APPLICATION_JSON);

        assertThat(request.headers.get(HTTPHeaders.ACCEPT)).isEqualTo(ContentType.APPLICATION_JSON.toString());
    }

    @Test
    void method() {
        assertThat(new HTTPRequest(HTTPMethod.GET, "http://localhost/uri").method).isEqualTo(HTTPMethod.GET);
        assertThat(new HTTPRequest(HTTPMethod.POST, "http://localhost/uri").method).isEqualTo(HTTPMethod.POST);
    }

    @Test
    void basicAuth() {  // refer to https://en.wikipedia.org/wiki/Basic_access_authentication
        var request = new HTTPRequest(HTTPMethod.GET, "http://localhost/uri");
        request.basicAuth("Aladdin", "OpenSesame");

        assertThat(request.headers).contains(entry(HTTPHeaders.AUTHORIZATION, "Basic QWxhZGRpbjpPcGVuU2VzYW1l"));
    }

    @Test
    void form() {
        var request = new HTTPRequest(HTTPMethod.POST, "http://localhost/uri");
        Map<String, String> form = new LinkedHashMap<>();   // make order deterministic
        form.put("key1", "v1 v2");
        form.put("key2", "v1+v2");
        request.form(form);

        assertThat(request.contentType).isEqualTo(ContentType.APPLICATION_FORM_URLENCODED);
        assertThat(request.headers).containsEntry(HTTPHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
        assertThat(new String(request.body, UTF_8)).isEqualTo("key1=v1+v2&key2=v1%2Bv2");
    }
}
