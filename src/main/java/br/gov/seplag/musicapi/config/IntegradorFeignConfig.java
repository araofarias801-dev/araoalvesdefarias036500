package br.gov.seplag.musicapi.config;

import feign.Client;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

public class IntegradorFeignConfig {
	@Bean
	@ConditionalOnProperty(name = "app.integrador.insecure-ssl", havingValue = "true")
	public Client feignClient() throws Exception {
		TrustManager[] trustAll = new TrustManager[] {
			new X509TrustManager() {
				@Override
				public void checkClientTrusted(X509Certificate[] chain, String authType) {
				}

				@Override
				public void checkServerTrusted(X509Certificate[] chain, String authType) {
				}

				@Override
				public X509Certificate[] getAcceptedIssuers() {
					return new X509Certificate[0];
				}
			}
		};

		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(null, trustAll, new SecureRandom());
		SSLSocketFactory socketFactory = sslContext.getSocketFactory();
		HostnameVerifier hostnameVerifier = (hostname, session) -> true;
		return new Client.Default(socketFactory, hostnameVerifier);
	}
}
