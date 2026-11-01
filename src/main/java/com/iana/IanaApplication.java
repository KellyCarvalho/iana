package com.iana;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.MapPropertySource;

import java.net.InetAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class IanaApplication {

	private static final Logger log = LoggerFactory.getLogger(IanaApplication.class);

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(IanaApplication.class);
		app.addInitializers(context -> {
			Map<String, Object> overrides = buildDatabaseOverrides();
			if (!overrides.isEmpty()) {
				context.getEnvironment().getPropertySources()
						.addFirst(new MapPropertySource("ianaCloudDatabaseOverrides", overrides));
			}
		});
		app.run(args);
	}

	private static Map<String, Object> buildDatabaseOverrides() {
		Map<String, Object> props = new HashMap<>();

		String rawUrl = System.getenv("SPRING_DATASOURCE_URL");
		if (rawUrl == null || rawUrl.isBlank()) {
			rawUrl = System.getenv("DATABASE_URL");
		}

		if (rawUrl != null && !rawUrl.isBlank()) {
			normalizeDatabaseUrl(rawUrl.trim(), props);
			return props;
		}

		// Se nenhuma variável de ambiente foi definida e o host local 'mysql' não existe, usa H2 persistente
		if (!isHostReachable("mysql")) {
			log.info("Nenhuma variável SPRING_DATASOURCE_URL definida e host 'mysql' indisponível. Usando H2 local.");
			props.put("spring.datasource.url", "jdbc:h2:file:./data/ianadb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;AUTO_SERVER=TRUE");
			props.put("spring.datasource.username", "sa");
			props.put("spring.datasource.password", "");
		}
		return props;
	}

	private static void normalizeDatabaseUrl(String rawUrl, Map<String, Object> props) {
		try {
			String sanitizedUrl = rawUrl.replace("ssl-mode=", "sslMode=");

			if (sanitizedUrl.startsWith("jdbc:")) {
				props.put("spring.datasource.url", sanitizedUrl);
				return;
			}

			if (sanitizedUrl.startsWith("mysql://") || sanitizedUrl.startsWith("postgres://") || sanitizedUrl.startsWith("postgresql://")) {
				URI uri = URI.create(sanitizedUrl);
				String scheme = uri.getScheme().equals("postgres") ? "postgresql" : uri.getScheme();
				String host = uri.getHost();
				int port = uri.getPort();
				String path = uri.getPath();
				String query = uri.getRawQuery();

				StringBuilder jdbcUrl = new StringBuilder("jdbc:")
						.append(scheme)
						.append("://")
						.append(host);
				if (port != -1) {
					jdbcUrl.append(":").append(port);
				}
				if (path != null) {
					jdbcUrl.append(path);
				}
				if (query != null && !query.isBlank()) {
					jdbcUrl.append("?").append(query);
				}

				props.put("spring.datasource.url", jdbcUrl.toString());

				String userInfo = uri.getUserInfo();
				if (userInfo != null && !userInfo.isBlank()) {
					String[] parts = userInfo.split(":", 2);
					if (isEnvBlank("SPRING_DATASOURCE_USERNAME")) {
						props.put("spring.datasource.username", parts[0]);
					}
					if (parts.length > 1 && isEnvBlank("SPRING_DATASOURCE_PASSWORD")) {
						props.put("spring.datasource.password", parts[1]);
					}
				}
			}
		} catch (Exception e) {
			log.warn("Não foi possível normalizar a URL de banco '{}': {}", rawUrl, e.getMessage());
		}
	}

	private static boolean isEnvBlank(String key) {
		String val = System.getenv(key);
		return val == null || val.isBlank();
	}

	private static boolean isHostReachable(String host) {
		try {
			InetAddress.getByName(host);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
