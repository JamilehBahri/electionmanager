package bahri.jamileh.electionmanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class ElectionmanagerApplication {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ElectionmanagerApplication.class);

    public static void main(String[] args) {

        ConfigurableApplicationContext context = SpringApplication.run(ElectionmanagerApplication.class, args);

        context.getBean("electionmanager" , Manager.class).sendBallotBoxGenesis();
        context.getBean("electionmanager" , Manager.class).sendCandidateGenesis();

        LOGGER.info("Election Manager Send Genesis To BallotBox And Candidates. . . . ");

    }

}
