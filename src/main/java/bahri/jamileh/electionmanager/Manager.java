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
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
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

    private Map<String, Integer> ballotBox = new HashMap<>();

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

    private boolean isRunning = true;

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
    public void finishing() {
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
            synchronized (lock2){
                lock2.notify();
            }
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
//                LOGGER.info("getTallyResult r :"+ r);
//                synchronized (lock2) {
//                    lock2.notify();
//                }
//                consensusStatisticsList.add(convertJsonToStatistic(transferQueueStatistic.take()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void getConsensusStatistics() {
        int receivedCount = 0;
        Map<Integer, Long> statisticsMap = new HashMap<>();

        while (isRunning) {
            synchronized (lock2) {
                try {
                    lock2.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            while ((maxGenerateVotes / startconsensusvotecount)*candidatesCount != receivedCount) {
                try {
                    consensusStatisticsList.add(convertJsonToStatistic(transferQueueStatistic.take()));
                    receivedCount++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if((maxGenerateVotes / startconsensusvotecount) * candidatesCount == receivedCount){
                receivedCount=0;
                // compute consensus time duration and add to csv file
                for (ConsensusStatistics cs : consensusStatisticsList) {

                    if (!statisticsMap.containsKey(cs.getConsensusId())) {
                        Duration t = Duration.between(cs.getStartTime(),cs.getEndTime());
                        statisticsMap.put(cs.getConsensusId(), t.toMillis());
                        LOGGER.info("consensus Id '{}' insert to map, consensus duration is: '{}' ms", cs.getConsensusId(), t.toMillis());
                    } else { //sum each consensus for each candidate
                        long c = statisticsMap.get(cs.getConsensusId());
                        Duration t = Duration.between(cs.getStartTime(),cs.getEndTime());
                        statisticsMap.put(cs.getConsensusId(), c + t.toMillis()); //is sum
                        LOGGER.info("consensus Id '{}' update in map, consensus duration is: '{}' ms, updated value is: '{}' ms",
                                cs.getConsensusId(), t.toMillis(), c + t.toMillis());

                    }
                } // is avg
                for (int i = 1; i <= statisticsMap.size(); i++) {
                    long avg = (statisticsMap.get(i) / statisticsMap.size());
                    statisticsMap.put(i, avg);
                    LOGGER.info("Avg Consensus Duration for consensus Id : '{}' is : '{}' ms ", i, avg);

                }

                writeMapToCSVFile(statisticsMap);
            }
        }

    }

    public void writeMapToCSVFile(Map<Integer, Long> map) {

        try {
            FileWriter csvWriter = new FileWriter("new.csv");
            csvWriter.append("candidateId");
            csvWriter.append(",");
            csvWriter.append("avgConsensusTime");
            csvWriter.append("\n");

            for (int i = 1; i <= map.size(); i++) {
               csvWriter.append(i + "," + map.get(i));
               csvWriter.append("\n");

            }
            csvWriter.flush();
            csvWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
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

    public String generateBallotBoxGenesis() {
        BallotBoxGenesis b_genesis = new BallotBoxGenesis(electionId, candidateChoose, setBallotBoxOffset()
                , maxGenerateVotes, LocalDateTime.now(), LocalDateTime.now(), setCandidates());

        return convertObjectToJson(b_genesis);
    }

    public String generateCandidateGenesis() {
        CandidateGenesis c_genesis = new CandidateGenesis(electionId, candidateChoose, masterCandidateId,
                minParticipants, maxParticipants, startconsensusvotecount, setCandidates(), maxGenerateVotes);

        return convertObjectToJson(c_genesis);
    }

    public String convertObjectToJson(Object obj) {
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

    public Map<String, Integer> setBallotBoxOffset() {
        int ballotBoxId = 1;
        for (int i = 1; i <= maxParticipants; i += maxGenerateVotes) {

            ballotBox.put(ballotBoxId++ + "", i);
        }
        return ballotBox;

    }

    public Set<Integer> setCandidates() {
        for (int i = 1; i <= candidatesCount; i++) {
            int candidateId = 1;
            candidates.add(i);
        }
        return candidates;

    }
}
