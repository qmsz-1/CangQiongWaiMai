import springboot.cqwm.DatabaseConfig;

import java.sql.*;

public class DatabaseCreator {
    static String url = DatabaseConfig.getJdbcUrl();
    static String username = DatabaseConfig.getUsername();
    static String password = DatabaseConfig.getPassword();
    static String DB_name = DatabaseConfig.getDbName();

    private static boolean databaseExists() throws SQLException{
        try(Connection conn = DriverManager.getConnection(url,username,password);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SHOW DATABASES LIKE '"+DB_name+"'")){
            return rs.next();
        }
    }


    private static void createDatabase() throws SQLException {
        try(Connection conn = DriverManager.getConnection(url,username,password);
            Statement stmt = conn.createStatement()){
            stmt.execute(" CREATE DATABASE "+DB_name);
        }
    }


    private static boolean tableExists(String TB_name) throws SQLException {
        try(Connection conn = DriverManager.getConnection(url,username,password);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SHOW TABLES LIKE '" + TB_name + "'")){
            return rs.next();
        }
    }

    private static void createTable(String tableName, String tableDefinition) throws SQLException {
        if (!tableExists(tableName)) {
            try (Connection conn = DriverManager.getConnection(url, username, password);
                 Statement stmt = conn.createStatement()) {
                String sql = "CREATE TABLE " + tableName + " (" + tableDefinition +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='" + tableName + "'";
                stmt.execute(sql);
                System.out.println("表 " + tableName + " 创建成功");
            }
        } else {
            System.out.println("表 " + tableName + " 已存在，跳过创建");
        }
    }

    // 创建所有表
    private static void createAllTables() throws SQLException {
        // 1. 员工表，用于存储商家内部的员工信息。
        createTable("employee",
                "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                        "name VARCHAR(32) NOT NULL, " +
                        "username VARCHAR(32) NOT NULL UNIQUE, " +
                        "password VARCHAR(64) NOT NULL, " +
                        "phone VARCHAR(11) NOT NULL CHECK (LENGTH(phone) = 11 AND phone REGEXP '^1[3-9][0-9]{9}$'), " +
                        "sex VARCHAR(2), " +
                        "id_number VARCHAR(18), " +
                        "status INT DEFAULT 1 COMMENT '1正常 0锁定', " +
                        "create_time DATETIME NOT NULL, " +
                        "update_time DATETIME NOT NULL, " +
                        "create_user BIGINT, " +
                        "update_user BIGINT");

        //2.分类表，用于存储商品的分类信息。
        createTable("category",
                "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                        "name VARCHAR(32) NOT NULL UNIQUE, " +
                        "type INT NOT NULL COMMENT '1菜品分类 2套餐分类', " +
                        "sort INT DEFAULT 0 COMMENT '排序字段', " +
                        "status INT DEFAULT 1 COMMENT '1启用 0禁用', " +
                        "create_time DATETIME NOT NULL, " +
                        "update_time DATETIME NOT NULL, " +
                        "create_user BIGINT, " +
                        "update_user BIGINT");

        //3.菜品表，用于存储菜品的信息。
        createTable("dish",
                "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                        "name VARCHAR(32) NOT NULL UNIQUE, " +
                        "category_id BIGINT NOT NULL COMMENT '分类id', " +
                        "price DECIMAL(10,2) NOT NULL," +
                        "image VARCHAR(255), " +
                        "description VARCHAR(255), " +
                        "status INT DEFAULT 1 COMMENT '1起售 0停售', " +
                        "create_time DATETIME NOT NULL, " +
                        "update_time DATETIME NOT NULL, " +
                        "create_user BIGINT, " +
                        "update_user BIGINT");

        //4.菜品口味表，用于存储菜品的口味信息。
        createTable("dish_flavor",
                "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                        "dish_id BIGINT NOT NULL COMMENT '菜品id', " +
                        "name VARCHAR(32) NOT NULL, " +
                        "value VARCHAR(255) NOT NULL");

        //5.套餐表，用于存储套餐的信息。
        createTable("setmeal",
                "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                        "name VARCHAR(32) NOT NULL UNIQUE, " +
                        "category_id BIGINT NOT NULL COMMENT '分类id', " +
                        "price DECIMAL(10,2) NOT NULL, " +
                        "image VARCHAR(255), " +
                        "description VARCHAR(255), " +
                        "status INT NOT NULL DEFAULT 1 COMMENT '1起售 0停售', " +
                        "create_time DATETIME NOT NULL, " +
                        "update_time DATETIME NOT NULL, " +
                        "create_user BIGINT, " +
                        "update_user BIGINT");

        //6.套餐菜品关系表，用于存储套餐和菜品的关联关系。
        createTable("setmeal_dish",
                "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                        "setmeal_id BIGINT NOT NULL COMMENT '套餐id', " +
                        "dish_id BIGINT NOT NULL COMMENT '菜品id', " +
                        "name VARCHAR(32) NOT NULL, " +
                        "price DECIMAL(10,2) NOT NULL, " +
                        "copies INT NOT NULL DEFAULT 1 COMMENT '菜品份数'");

        //7.用户表，用于存储C端用户的信息。
        createTable("user",
                "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                        "openid VARCHAR(45) NOT NULL, " +
                        "name VARCHAR(32) NOT NULL, " +
                        "phone VARCHAR(11) NOT NULL CHECK (LENGTH(phone) = 11 AND phone REGEXP '^1[3-9][0-9]{9}$'), " +
                        "sex VARCHAR(2), " +
                        "id_number VARCHAR(18), " +
                        "avatar VARCHAR(500), " +
                        "create_time DATETIME NOT NULL");

        //8.地址表，用于存储C端用户的收货地址信息。
        createTable("address_book",
                "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                        "user_id BIGINT NOT NULL COMMENT '用户id', " +
                        "consignee VARCHAR(50) NOT NULL, " +
                        "sex VARCHAR(2), " +
                        "phone VARCHAR(11) NOT NULL CHECK (LENGTH(phone) = 11 AND phone REGEXP '^1[3-9][0-9]{9}$'), " +
                        "province_code VARCHAR(12), " +
                        "province_name VARCHAR(32), " +
                        "city_code VARCHAR(12), " +
                        "city_name VARCHAR(32), " +
                        "district_code VARCHAR(12), " +
                        "district_name VARCHAR(32), " +
                        "detail VARCHAR(200) NOT NULL COMMENT '详细地址信息', " +
                        "label VARCHAR(100) COMMENT '标签', " +
                        "is_default TINYINT(1) NOT NULL DEFAULT 1 COMMENT '1是默认地址 0不是默认地址'");

        //9.购物车表，用于存储C端用户的购物车信息。
        createTable("shopping_cart",
                "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                        "name VARCHAR(32) NOT NULL, " +
                        "image VARCHAR(255), " +
                        "user_id BIGINT NOT NULL COMMENT '用户id', " +
                        "dish_id BIGINT NOT NULL COMMENT '菜品id', " +
                        "setmeal_id BIGINT NOT NULL COMMENT '套餐id', " +
                        "dish_flavor VARCHAR(50), " +
                        "number INT NOT NULL DEFAULT 1 COMMENT '商品数量', " +
                        "amount DECIMAL(10,2) NOT NULL COMMENT '商品单价', " +
                        "create_time DATETIME NOT NULL");

        //10.订单表，用于存储C端用户的订单数据。
        createTable("orders",
                "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                        "number VARCHAR(50) NOT NULL COMMENT '订单号', " +
                        "status INT NOT NULL DEFAULT 1 COMMENT '1待付款 2待接单 3已接单 4派送中 5已完成 6已取消', " +
                        "user_id BIGINT NOT NULL COMMENT '用户id', " +
                        "address_book_id BIGINT NOT NULL COMMENT '地址id', " +
                        "order_time DATETIME NOT NULL COMMENT '下单时间', " +
                        "checkout_time DATETIME NOT NULL COMMENT '付款时间', " +
                        "pay_method INT NOT NULL DEFAULT 1 COMMENT '1微信支付 2支付宝支付', " +
                        "pay_status TINYINT NOT NULL DEFAULT 0 COMMENT '0未支付 1已支付 2退款', " +
                        "amount DECIMAL(10, 2) NOT NULL COMMENT '订单金额', " +
                        "remark VARCHAR(100), " +
                        "phone VARCHAR(11) NOT NULL CHECK (LENGTH(phone) = 11 AND phone REGEXP '^1[3-9][0-9]{9}$'), "+
                        "address VARCHAR(255) NOT NULL COMMENT '详细地址', " +
                        "user_name VARCHAR(32)," +
                        "consignee VARCHAR(32) NOT NULL COMMENT '收货人', " +
                        "cancel_reason VARCHAR(255), " +
                        "rejection_reason VARCHAR(255), " +
                        "cancel_time DATETIME, " +
                        "estimated_delivery_time DATETIME, " +
                        "delivery_status TINYINT NOT NULL DEFAULT 1 COMMENT '1立即送出 0选择具体时间', " +
                        "delivery_time DATETIME, " +
                        "pack_amount INT DEFAULT 0 COMMENT '打包费', " +
                        "tableware_number INT COMMENT '餐具数量', " +
                        "tableware_status TINYINT DEFAULT 1 COMMENT '1按餐量提供 0选择具体数量'");

        //11.订单明细表，用于存储C端用户的订单明细数据。
        createTable("order_detail",
                "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                        "name VARCHAR(32) NOT NULL, " +
                        "image VARCHAR(255), " +
                        "order_id BIGINT COMMENT '订单id', " +
                        "dish_id BIGINT COMMENT '菜品id', " +
                        "setmeal_id BIGINT COMMENT '套餐id', " +
                        "dish_flavor VARCHAR(50), " +
                        "number INT NOT NULL DEFAULT 1 COMMENT '商品数量', " +
                        "amount DECIMAL(10,2) NOT NULL COMMENT '商品单价'");
    }


    public static void main(String[] args) {
        String TB_name = "";
        try {
            if(!databaseExists()){
                createDatabase();
                System.out.println("数据库创建成功");
            }else{
                System.out.println("数据库已存在,跳过创建");
            }

            createAllTables();
            System.out.print("所有表创建完成");

        }catch (SQLException e){
            System.out.println("数据库操作出错:" + e.getMessage());
            e.printStackTrace();
        }
    }

}
