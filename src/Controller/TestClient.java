package Controller;
import static org.junit.Assert.assertEquals;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.junit.Test;

    public class TestClient {

        private Client client = new Client(); // This will automatically start the server

        @Test
        public void testLogin() {
            provideInput("no");
            provideInput("john");
            provideInput("abc");
            String input = client.askLoginData();
            assertEquals(client.getUsername(), "john");

        }

        void provideInput(String data) {
            ByteArrayInputStream testIn = new ByteArrayInputStream(data.getBytes());
            System.setIn(testIn);
        }

        private void captureOutput() {
            System.setOut(new java.io.PrintStream(new java.io.ByteArrayOutputStream()));
        }
    }
