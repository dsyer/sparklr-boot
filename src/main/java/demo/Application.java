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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.InMemoryUserDetailsManagerConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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

@ComponentScan
@EnableAutoConfiguration
@ImportResource("classpath:/spring-servlet.xml")
@Order(Ordered.LOWEST_PRECEDENCE - 100)
@EnableWebSecurity
public class Application extends WebSecurityConfigurerAdapter {

	@Autowired
	private ClientDetailsService clientDetailsService;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		http.userDetailsService(new ClientDetailsUserDetailsService(
				clientDetailsService));
		http.exceptionHandling().authenticationEntryPoint(
				oauthAuthenticationEntryPoint());
		http.requestMatchers().antMatchers("/oauth/token");
		http.authorizeRequests().anyRequest().authenticated()//
				.and().httpBasic() //
				.and().anonymous().disable();
		http.exceptionHandling().accessDeniedHandler(
				new OAuth2AccessDeniedHandler());
		http.sessionManagement().sessionCreationPolicy(
				SessionCreationPolicy.STATELESS);
		http.addFilterAfter(clientCredentialsTokenEndpointFilter(),
				BasicAuthenticationFilter.class);

	}

	@Bean
	public OAuth2AuthenticationEntryPoint oauthAuthenticationEntryPoint() {
		OAuth2AuthenticationEntryPoint entryPoint = new OAuth2AuthenticationEntryPoint();
		entryPoint.setRealmName("sparklr2");
		return entryPoint;
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
		clientCredentialsTokenEndpointFilter
				.setAuthenticationManager(clientAuthenticationManager());
		return clientCredentialsTokenEndpointFilter;
	}

	@Bean
	protected AuthenticationManager clientAuthenticationManager() {
		DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
		ClientDetailsUserDetailsService clientUserDetailsService = new ClientDetailsUserDetailsService(
				clientDetailsService);
		authenticationProvider.setUserDetailsService(clientUserDetailsService);
		return new ProviderManager(
				Arrays.<AuthenticationProvider> asList(authenticationProvider));
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

	@Bean
	public AuthenticationManager authenticationManager() throws Exception {

		InMemoryUserDetailsManagerConfigurer<AuthenticationManagerBuilder> builder = new AuthenticationManagerBuilder(
				ObjectPostProcessor.QUIESCENT_POSTPROCESSOR)
				.inMemoryAuthentication();
		builder.withUser("marissa").password("koala").roles("USER");
		return builder.and().build();

	}

}
