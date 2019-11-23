package bahri.jamileh.electionmanager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;

@Component("electionmanager")
public class Manager {

    @Value("${BallotBox.topicname}")
    private String ballotBoxGenesisTopic;

    @Value("${Candidate.topicname}")
    private String candidateGenesisTopic;

    @Value("${Tally.reply.vote.topicname}")
    private String tallyReplyTopic;

    @Value("${Consensus.statistics}")
    private String consensusStatisticsTopic;

    @Autowired
    @Qualifier("electionId")
    private int electionId;

    @Autowired
    @Qualifier("candidateChoose")
    private int candidateChoose;

    private Map<String,Integer> ballotBox = new HashMap<>();

    @Autowired
    @Qualifier("maxGenerateVotes")
    private int maxGenerateVotes;

    @Autowired
    @Qualifier("maxParticipants")
    private int maxParticipants;

    @Autowired
    @Qualifier("minParticipants")
    private int minParticipants;

    @Autowired
    @Qualifier("candidatesCount")
    private int candidatesCount;

    @Autowired
    @Qualifier("masterCandidateId")
    private int masterCandidateId;

    @Autowired
    @Qualifier("startconsensusvotecount")
    private int startconsensusvotecount;


    private Set<Integer> candidates = new HashSet<>();

    private TransferQueue<String> transferQueueEndTally = new LinkedTransferQueue<>();

    private TransferQueue<String> transferQueueStatistic = new LinkedTransferQueue<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(Manager.class);

    private Thread tallyResultNotificationThread = new Thread(this::getTallyResult);

    private Thread consensusStatisticsThread = new Thread(this::getConsensusStatistics);

    List<ConsensusStatistics> consensusStatisticsList = new ArrayList<>();

    private boolean isRunning =true;

    private final Object lock1 = new Object();

    private final Object lock2 = new Object();

    @Autowired
    private JmsTemplate jmsTemplate;

    @PostConstruct
    public void init() {

        tallyResultNotificationThread.start();
        consensusStatisticsThread.start();
    }

    @PreDestroy
    public void finishing(){
        isRunning = false;
        tallyResultNotificationThread.interrupt();
        consensusStatisticsThread.interrupt();
        try {
            tallyResultNotificationThread.join(30000);
            consensusStatisticsThread.join(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @JmsListener(destination = "${Consensus.statistics}")
    public void receiveConsensusStatistics(String message) {
        try {
            LOGGER.info("'Election Manager' received Consensus Statistics message='{}'", message);
            transferQueueStatistic.transfer(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @JmsListener(destination = "${Tally.reply.vote.topicname}")
    public void receiveEndTally(String message) {
        try {
            LOGGER.info("'Election Manager' received End Tally message='{}'", message);
            transferQueueEndTally.transfer(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void getTallyResult(){

        while (isRunning) {
//            synchronized (lock1) {
//                try {
//                    lock1.wait();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
            try {
                String r = transferQueueEndTally.take();
                LOGGER.info("getTallyResult r :"+ r);
                synchronized (lock2) {
                    lock2.notify();
                }
//                consensusStatisticsList.add(convertJsonToStatistic(transferQueueStatistic.take()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void getConsensusStatistics() {

        while (isRunning) {
            synchronized (lock2) {
                try {
                    lock2.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            try {
                consensusStatisticsList.add(convertJsonToStatistic(transferQueueStatistic.take()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // compute avg consensus time
//            float sum = 0;
//            for ( ConsensusStatistics cs : consensusStatisticsList){
//                float t = cs.getEndTime().getNano() - cs.getStartTime().getNano();
//                LOGGER.info("Duration of consensus Id '{}' is : '{}'", cs.getConsensusId() , t);
//                sum += t;
//            }
//            float avg = (sum / consensusStatisticsList.size());
//            LOGGER.info("Consensus Duration for election Id : '{}' is : '{}' " ,electionId, avg );

        }
    }

    public void sendBallotBoxGenesis() {

        String message = generateBallotBoxGenesis();
        jmsTemplate.convertAndSend(ballotBoxGenesisTopic, message);
        LOGGER.info("sending message='{}' to destination='{}'", message,
                ballotBoxGenesisTopic);
    }

    public void sendCandidateGenesis() {

        String message = generateCandidateGenesis();
        jmsTemplate.convertAndSend(candidateGenesisTopic, message);
        LOGGER.info("sending message='{}' to destination='{}'", message,
                candidateGenesisTopic);

    }

    public  String generateBallotBoxGenesis(){
       BallotBoxGenesis  b_genesis = new BallotBoxGenesis(electionId,candidateChoose,setBallotBoxOffset()
               ,maxGenerateVotes, LocalDateTime.now(),LocalDateTime.now(),setCandidates());

            return convertObjectToJson(b_genesis);
    }

    public  String generateCandidateGenesis(){
        CandidateGenesis c_genesis = new CandidateGenesis(electionId,candidateChoose,masterCandidateId,
                minParticipants,maxParticipants,startconsensusvotecount ,setCandidates() , maxGenerateVotes);

            return convertObjectToJson(c_genesis);
    }

    public  String convertObjectToJson(Object obj){
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        String jsonString = gson.toJson(obj);
        System.out.println(jsonString);
        return jsonString;
    }

    public ConsensusStatistics convertJsonToStatistic(String jsonString) {

        if (jsonString != null) {
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            return gson.fromJson(jsonString, ConsensusStatistics.class);

        } else
            return null;
    }

    public Map<String,Integer> setBallotBoxOffset(){
        int ballotBoxId = 1;
        for (int i = 1; i <= maxParticipants; i += maxGenerateVotes){

            ballotBox.put(ballotBoxId++ +"", i );
        }
        return ballotBox;

    }

    public Set<Integer> setCandidates(){
        for (int i = 1; i <= candidatesCount; i++){
            int candidateId = 1;
            candidates.add(i);
        }
        return candidates;

    }
}
