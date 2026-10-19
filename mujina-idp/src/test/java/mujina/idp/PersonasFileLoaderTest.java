package mujina.idp;

import org.junit.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;

public class PersonasFileLoaderTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    public void validJsonIsParsedSuccessfully() throws Exception {
        String json = "[{\"name\":\"test\",\"description\":\"A test persona\"}]";
        ByteArrayResource resource = new ByteArrayResource(json.getBytes());

        String result = UserController.loadFilePersonas(jsonMapper, new Resource[]{resource});

        assertNotNull(result);
        assertTrue(result.contains("test"));
        assertTrue(result.contains("A test persona"));
    }

    @Test
    public void invalidJsonReturnsNull() throws Exception {
        String invalidJson = "{this is not valid json}";
        ByteArrayResource resource = new ByteArrayResource(invalidJson.getBytes());

        String result = UserController.loadFilePersonas(jsonMapper, new Resource[]{resource});

        assertNull(result);
    }

    @Test
    public void noResourcesReturnsNull() throws Exception {
        Resource nonExistentResource = new FailingResource(false);

        String result = UserController.loadFilePersonas(jsonMapper, new Resource[]{nonExistentResource});

        assertNull(result);
    }

    @Test
    public void multipleResourcesUsesFirstValidOne() throws Exception {
        ByteArrayResource validResource = new ByteArrayResource("[{\"name\":\"valid\"}]".getBytes());
        ByteArrayResource invalidResource = new ByteArrayResource("{invalid}".getBytes());

        String result = UserController.loadFilePersonas(jsonMapper, new Resource[]{invalidResource, validResource});

        assertNotNull(result);
        assertTrue(result.contains("valid"));
    }

    @Test
    public void allResourcesInvalidReturnsNull() throws Exception {
        ByteArrayResource invalidResource1 = new ByteArrayResource("{invalid1}".getBytes());
        ByteArrayResource invalidResource2 = new ByteArrayResource("{invalid2}".getBytes());

        String result = UserController.loadFilePersonas(jsonMapper, new Resource[]{invalidResource1, invalidResource2});

        assertNull(result);
    }

    @Test
    public void allResourcesNoExistReturnsNull() throws Exception {
        Resource nonExistent1 = new FailingResource(false);
        Resource nonExistent2 = new FailingResource(false);

        String result = UserController.loadFilePersonas(jsonMapper, new Resource[]{nonExistent1, nonExistent2});

        assertNull(result);
    }

    /**
     * Helper class that implements Resource but can be configured to fail.
     */
    private static class FailingResource implements Resource {
        private final boolean existsFlag;

        FailingResource(boolean existsFlag) {
            this.existsFlag = existsFlag;
        }

        @Override
        public boolean exists() {
            return existsFlag;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            throw new IOException("Resource not available");
        }

        @Override
        public String getDescription() {
            return "test resource";
        }

        @Override
        public String getFilename() {
            return "personas.json";
        }

        @Override
        public java.io.File getFile() throws IOException {
            throw new IOException("Not a file");
        }

        @Override
        public long contentLength() throws IOException {
            return 0;
        }

        @Override
        public long lastModified() throws IOException {
            return 0;
        }

        @Override
        public Resource createRelative(String relativePath) throws IOException {
            throw new IOException("Cannot create relative resource");
        }

        @Override
        public java.net.URL getURL() throws IOException {
            throw new IOException("Not a URL");
        }

        @Override
        public java.net.URI getURI() throws IOException {
            throw new IOException("Not a URI");
        }

        @Override
        public boolean isReadable() {
            return existsFlag;
        }

        public boolean isAnonymous() {
            return true;
        }

        @Override
        public boolean isFile() {
            return false;
        }
    }
}