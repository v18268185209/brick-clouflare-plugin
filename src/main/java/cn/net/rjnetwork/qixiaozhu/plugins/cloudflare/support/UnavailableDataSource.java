package cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.support;

import org.springframework.jdbc.datasource.AbstractDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class UnavailableDataSource extends AbstractDataSource {

    private final String reason;

    public UnavailableDataSource(String reason) {
        this.reason = reason == null ? "Host datasource is unavailable" : reason;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public Connection getConnection() throws SQLException {
        throw new SQLException(reason);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        throw new SQLException(reason);
    }
}
