package bahri.jamileh.electionmanager;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Configuration
@EnableJms
public class GenesisConfiguration {

    @Value("${activemq.broker-url}")
    private String brokerUrl;

     @Value("${electionId}")
     private int electionId;

     @Value("${election.candidateChoose}")
     private int candidateChoose;

     private Map<String, Integer> ballotBox;

     @Value("${election.maxGenerateVotes}")
     private int maxGenerateVotes;

    @Value("${election.candidatesCount}")
    private int candidatesCount;

    @Value("${election.minParticipants}")
    private int minParticipants;

    @Value("${election.maxParticipants}")
    private int maxParticipants;

  ///////candidate genesis
    @Value("${election.masterCandidateId}")
    private int masterCandidateId;

    @Value("${election.candidate.startconsensus.votecount}")
    private int startconsensusvotecount;


    private LocalDateTime issuedTime;

     private LocalDateTime persistTime;

     private Set<Integer> candidates;

    public GenesisConfiguration() {
    }

    @Bean(name = "electionId")
    public int getElectionId() {
        return electionId;
    }

    @Bean(name = "candidateChoose")
    public int getCandidateChoose() {
        return candidateChoose;
    }

    @Bean(name = "ballotBox")
    public Map<String, Integer> getBallotBox() {
        return ballotBox;
    }

    @Bean(name = "maxGenerateVotes")
    public int getMaxGenerateVotes() {
        return maxGenerateVotes;
    }

    @Bean(name = "candidates")
    public Set<Integer> getCandidates() {
        return candidates;
    }

    @Bean(name = "candidatesCount")
    public int getCandidatesCount() {
        return candidatesCount;
    }

    @Bean(name = "minParticipants")
    public int getMinParticipants() {
        return minParticipants;
    }

    @Bean(name = "maxParticipants")
    public int getMaxParticipants() {
        return maxParticipants;
    }

    @Bean(name = "masterCandidateId")
    public int  getMasterCandidateId() {
        return masterCandidateId;
    }

    @Bean(name = "startconsensusvotecount")
    public int getStartConsensusVoteCount() {
        return startconsensusvotecount;
    }



    @Bean (name = "activeMQConnectionFactory")
    public ActiveMQConnectionFactory senderActiveMQConnectionFactory() {
        ActiveMQConnectionFactory activeMQConnectionFactory =
                new ActiveMQConnectionFactory();
        activeMQConnectionFactory.setBrokerURL(brokerUrl);

        return activeMQConnectionFactory;
    }

    @Bean
    public CachingConnectionFactory cachingConnectionFactory() {
        return new CachingConnectionFactory(
                senderActiveMQConnectionFactory());
    }
    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory() {
        DefaultJmsListenerContainerFactory factory =
                new DefaultJmsListenerContainerFactory();
        factory
                .setConnectionFactory(receiverActiveMQConnectionFactory());
        factory.setPubSubDomain(true);

        return factory;
    }
    @Bean
    public ActiveMQConnectionFactory receiverActiveMQConnectionFactory() {
        ActiveMQConnectionFactory activeMQConnectionFactory =
                new ActiveMQConnectionFactory();
        activeMQConnectionFactory.setBrokerURL(brokerUrl);

        return activeMQConnectionFactory;
    }

    @Bean
    public JmsTemplate jmsTemplate() {
        JmsTemplate jmsTemplate =
                new JmsTemplate(cachingConnectionFactory());
        jmsTemplate.setPubSubDomain(true);

        return jmsTemplate;
    }



}
