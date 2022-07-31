package cy.ac.ouc.cognition.nlrg.service;

import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

@Configuration
@ConfigurationProperties(prefix = "nlrgpipeline")
public class ConfigProperties {
    
	@Value("${init:true}")
	private boolean init;

	@Bean
    public boolean getInit() {
		return this.init;
	}

    public void setInit(boolean initValue) {
		this.init = initValue;
	}
}