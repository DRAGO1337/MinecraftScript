import java.sql.*;
import java.util.*;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:minecraft_bot.db";
    private Connection conn;

    public DatabaseManager() {
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
            conn = DriverManager.getConnection(DB_URL);
            createTables();
        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
        }
    }

    private void createTables() throws SQLException {
        String[] tables = {
            """
            CREATE TABLE IF NOT EXISTS blocks (
                x INTEGER,
                y INTEGER,
                z INTEGER,
                type TEXT,
                PRIMARY KEY (x, y, z)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS waypoints (
                name TEXT PRIMARY KEY,
                x INTEGER,
                y INTEGER,
                z INTEGER
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS mining_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                block_type TEXT,
                x INTEGER,
                y INTEGER,
                z INTEGER,
                timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
            )
            """
        };

        for (String sql : tables) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
            }
        }
    }

    public void saveBlock(Block block) {
        String sql = "INSERT OR REPLACE INTO blocks (x, y, z, type) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, block.position.x);
            pstmt.setInt(2, block.position.y);
            pstmt.setInt(3, block.position.z);
            pstmt.setString(4, block.type);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to save block: " + e.getMessage());
        }
    }

    public void recordMining(String blockType, Position pos) {
        String sql = "INSERT INTO mining_history (block_type, x, y, z) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, blockType);
            pstmt.setInt(2, pos.x);
            pstmt.setInt(3, pos.y);
            pstmt.setInt(4, pos.z);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to record mining: " + e.getMessage());
        }
    }

    public List<Block> getKnownBlocks(String type) {
        List<Block> blocks = new ArrayList<>();
        String sql = "SELECT x, y, z FROM blocks WHERE type = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, type);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Position pos = new Position(
                    rs.getInt("x"),
                    rs.getInt("y"),
                    rs.getInt("z")
                );
                blocks.add(new Block(pos, type, true));
            }
        } catch (SQLException e) {
            System.err.println("Failed to get blocks: " + e.getMessage());
        }
        return blocks;
    }

    public void saveWaypoint(String name, Position pos) {
        String sql = "INSERT OR REPLACE INTO waypoints (name, x, y, z) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setInt(2, pos.x);
            pstmt.setInt(3, pos.y);
            pstmt.setInt(4, pos.z);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to save waypoint: " + e.getMessage());
        }
    }

    public Position getWaypoint(String name) {
        String sql = "SELECT x, y, z FROM waypoints WHERE name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Position(
                    rs.getInt("x"),
                    rs.getInt("y"),
                    rs.getInt("z")
                );
            }
        } catch (SQLException e) {
            System.err.println("Failed to get waypoint: " + e.getMessage());
        }
        return null;
    }

    public void close() {
        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.err.println("Failed to close database: " + e.getMessage());
        }
    }
}