package travelapp.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Manages JDBC connections.
 *
 * <p>For <em>file-based</em> databases a fresh connection is returned by
 * {@link #getConnection()} every time and the caller is expected to close it
 * (standard try-with-resources pattern).
 *
 * <p>For <em>in-memory</em> SQLite databases (detected by the presence of
 * {@code :memory:} or {@code mode=memory} in the URL) a single
 * <em>keep-alive</em> connection is kept open for the lifetime of this
 * manager so that the schema created by {@link #initializeDatabase()}
 * remains accessible.  Every call to {@link #getConnection()} returns a
 * lightweight <em>non-closing proxy</em> that wraps the keep-alive connection
 * but silently ignores {@code close()} calls — so try-with-resources blocks
 * in {@code PackageRepository} do <em>not</em> destroy the schema.
 */
public class DatabaseManager {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);

    private final String dbUrl;
    private final boolean inMemory;
    private Connection keepAliveConnection;

    public DatabaseManager(String dbUrl) {
        this.dbUrl = dbUrl;
        this.inMemory = dbUrl.contains(":memory:") || dbUrl.contains("mode=memory");
    }

    /**
     * Returns a usable JDBC {@link Connection}.
     *
     * <ul>
     *   <li>File-based DB → fresh connection each call (caller must close).</li>
     *   <li>In-memory DB → non-closing proxy around the keep-alive connection
     *       (safe to use inside try-with-resources).</li>
     * </ul>
     */
    public Connection getConnection() throws Exception {
        if (!inMemory) {
            return DriverManager.getConnection(dbUrl);
        }

        // Ensure the keep-alive connection is open
        if (keepAliveConnection == null || keepAliveConnection.isClosed()) {
            keepAliveConnection = DriverManager.getConnection(dbUrl);
        }

        // Return a proxy whose close() is a no-op, so try-with-resources
        // in PackageRepository cannot destroy the in-memory schema.
        return nonClosingProxy(keepAliveConnection);
    }

    /**
     * Closes the keep-alive connection (only meaningful for in-memory databases).
     * Call this in {@code @AfterEach} or on application shutdown.
     */
    public void close() {
        if (keepAliveConnection != null) {
            try {
                if (!keepAliveConnection.isClosed()) {
                    keepAliveConnection.close();
                }
            } catch (Exception e) {
                // ignore — called during teardown
            } finally {
                keepAliveConnection = null;
            }
        }
    }

    /**
     * Executes the DDL script from {@code /db/init.sql}.
     * Uses {@code CREATE TABLE IF NOT EXISTS}, so repeated calls are safe.
     */
    public void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            try (InputStream is = getClass().getResourceAsStream("/db/init.sql")) {
                if (is == null) {
                    throw new IllegalStateException("init.sql not found in classpath resources");
                }
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                    String ddl = reader.lines().collect(Collectors.joining("\n"));
                    for (String sql : ddl.split(";")) {
                        String trimmed = sql.trim();
                        if (!trimmed.isEmpty()) {
                            stmt.execute(trimmed);
                        }
                    }
                }
            }
            logger.info("Database initialized successfully at: {}", dbUrl);
        } catch (Exception e) {
            logger.error("Failed to initialize database", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Wraps {@code real} in a JDK dynamic proxy that forwards every
     * {@link Connection} method call to the real connection <em>except</em>
     * {@code close()}, which becomes a silent no-op.
     */
    private static Connection nonClosingProxy(Connection real) {
        return (Connection) Proxy.newProxyInstance(
                real.getClass().getClassLoader(),
                new Class[]{Connection.class},
                (proxy, method, args) -> {
                    if ("close".equals(method.getName())) {
                        return null; // intentional no-op
                    }
                    return method.invoke(real, args);
                }
        );
    }
}
