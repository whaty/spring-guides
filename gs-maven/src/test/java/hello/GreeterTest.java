package hello;

import org.junit.Test;

import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

public class GreeterTest {
    private Greeter greeter = new Greeter();

    @Test
    public void sayHello() {
        assertThat(greeter.sayHello(), containsString("Hello"));
    }
}