package springboot.cqwm;

public class DatabaseConfig {
    // 使用 private final 防止修改
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/sky";
    private static final String DB_NAME = "sky";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "254568";

    // 提供getter方法访问配置
    public static String getJdbcUrl() {
        return JDBC_URL;
    }

    public static String getDbName() {
        return DB_NAME;
    }

    public static String getUsername() {
        return USERNAME;
    }

    public static String getPassword() {
        return PASSWORD;
    }

}