package demo;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.client.ClientCredentialsTokenEndpointFilter;
import org.springframework.security.oauth2.provider.client.ClientDetailsUserDetailsService;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;
import org.springframework.security.oauth2.provider.error.OAuth2AuthenticationEntryPoint;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.InMemoryTokenStore;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@ComponentScan
@EnableAutoConfiguration
@ImportResource("classpath:/spring-servlet.xml")
@Order(Ordered.LOWEST_PRECEDENCE - 100)
public class Application extends WebSecurityConfigurerAdapter {

	@Autowired
	private ClientDetailsService clientDetailsService;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		// This filter has it's own client-specific authentication manager
		// @formatter:off
		http.userDetailsService(new ClientDetailsUserDetailsService(
				clientDetailsService))
			.exceptionHandling().authenticationEntryPoint(
				clientAuthenticationEntryPoint())
		.and()
			.requestMatchers().antMatchers("/oauth/token")
		.and()
		    .authorizeRequests().anyRequest().authenticated()//
				.and().httpBasic() //
				.and().anonymous().disable()
			.exceptionHandling().accessDeniedHandler(
				new OAuth2AccessDeniedHandler())
		.and()
		    .sessionManagement().sessionCreationPolicy(
				SessionCreationPolicy.STATELESS)
		.and()
			.csrf().requireCsrfProtectionMatcher(new AntPathRequestMatcher("/oauth/**")).disable()
		    .addFilterAfter(clientCredentialsTokenEndpointFilter(),
				BasicAuthenticationFilter.class);
		// @formatter:on

	}

	@Bean
	public OAuth2AuthenticationEntryPoint clientAuthenticationEntryPoint() {
		OAuth2AuthenticationEntryPoint entryPoint = new OAuth2AuthenticationEntryPoint();
		entryPoint.setRealmName("sparklr2/client");
		entryPoint.setTypeName("Basic");
		return entryPoint;
	}

	@Bean
	protected ClientCredentialsTokenEndpointFilter clientCredentialsTokenEndpointFilter() {
		ClientCredentialsTokenEndpointFilter clientCredentialsTokenEndpointFilter = new ClientCredentialsTokenEndpointFilter();
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setUserDetailsService(new ClientDetailsUserDetailsService(
				clientDetailsService));
		clientCredentialsTokenEndpointFilter
				.setAuthenticationManager(new ProviderManager(Arrays
						.<AuthenticationProvider> asList(provider)));
		clientCredentialsTokenEndpointFilter.setAuthenticationEntryPoint(clientAuthenticationEntryPoint());
		return clientCredentialsTokenEndpointFilter;
	}

	@Bean
	protected TokenStore tokenStore() {
		return new InMemoryTokenStore();
	}

	@Bean
	public DefaultTokenServices tokenServices() {
		DefaultTokenServices tokenServices = new DefaultTokenServices();
		tokenServices.setClientDetailsService(clientDetailsService);
		tokenServices.setSupportRefreshToken(true);
		tokenServices.setTokenStore(tokenStore());
		return tokenServices;
	}

}
