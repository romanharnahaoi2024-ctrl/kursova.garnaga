package travelapp.model;

import org.junit.jupiter.api.Test;
import travelapp.Customer;

import static org.junit.jupiter.api.Assertions.*;

class CustomerTest {

    @Test
    void testConstructorAndGetters() {
        Customer c = new Customer("Roman", "roman@gmail.com", "12345");

        assertEquals("Roman", c.getName());
        assertEquals("roman@gmail.com", c.getEmail());
        assertEquals("12345", c.getPhone());
    }
}
