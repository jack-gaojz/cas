package org.apereo.cas.config;

import lombok.val;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.hz.HazelcastConfigurationFactory;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.registry.HazelcastTicketRegistry;
import org.apereo.cas.ticket.registry.NoOpTicketRegistryCleaner;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistryCleaner;
import org.apereo.cas.util.CoreTicketUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Spring's Java configuration component for {@code HazelcastInstance} that is consumed and used by
 * {@link HazelcastTicketRegistry}.
 * <p>
 * This configuration class has the smarts to choose the configuration source for the {@link HazelcastInstance}
 * that it produces by either loading the native hazelcast XML config file from a resource location
 * or it creates the {@link HazelcastInstance} programmatically
 * with a handful properties and their defaults (if not set) that it exposes to CAS deployers.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 4.2.0
 */
@Configuration("hazelcastTicketRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class HazelcastTicketRegistryConfiguration {


    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Bean
    public TicketRegistry ticketRegistry(@Qualifier("ticketCatalog") final TicketCatalog ticketCatalog) {
        val hz = casProperties.getTicket().getRegistry().getHazelcast();
        val r = new HazelcastTicketRegistry(hazelcast(ticketCatalog),
                ticketCatalog,
                hz.getPageSize());
        r.setCipherExecutor(CoreTicketUtils.newTicketRegistryCipherExecutor(hz.getCrypto(), "hazelcast"));
        return r;
    }

    @Bean
    public TicketRegistryCleaner ticketRegistryCleaner() {
        return NoOpTicketRegistryCleaner.getInstance();
    }

    @Autowired
    @Bean
    public HazelcastInstance hazelcast(@Qualifier("ticketCatalog") final TicketCatalog ticketCatalog) {
        return Hazelcast.newHazelcastInstance(getConfig(ticketCatalog));
    }

    private Config getConfig(final TicketCatalog ticketCatalog) {
        val hz = casProperties.getTicket().getRegistry().getHazelcast();
        val configs = buildHazelcastMapConfigurations(ticketCatalog);
        val factory = new HazelcastConfigurationFactory();
        return factory.build(hz, configs);
    }

    private Map<String, MapConfig> buildHazelcastMapConfigurations(final TicketCatalog ticketCatalog) {
        val mapConfigs = new HashMap<String, MapConfig>();

        val hz = casProperties.getTicket().getRegistry().getHazelcast();
        val factory = new HazelcastConfigurationFactory();

        val definitions = ticketCatalog.findAll();
        definitions.forEach(t -> {
            val mapConfig = factory.buildMapConfig(hz, t.getProperties().getStorageName(), t.getProperties().getStorageTimeout());
            LOGGER.debug("Created Hazelcast map configuration for [{}]", t);
            mapConfigs.put(t.getProperties().getStorageName(), mapConfig);
        });
        return mapConfigs;
    }
}
