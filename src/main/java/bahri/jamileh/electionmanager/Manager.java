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
import java.io.BufferedReader;
import java.io.FileReader;
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

    @Value("${Tally.statistics}")
    private String tallyStatisticsTopic;

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

    @Autowired
    @Qualifier("maxBallotBoxCount")
    private int maxBallotBoxCount;


    private Set<Integer> candidates = new HashSet<>();

    private TransferQueue<String> transferQueueEndTally = new LinkedTransferQueue<>();

    private TransferQueue<String> transferQueueStatistic = new LinkedTransferQueue<>();

    private TransferQueue<String> transferQueueStatisticTally = new LinkedTransferQueue<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(Manager.class);

    private Thread tallyResultNotificationThread = new Thread(this::getTallyResult);

    private Thread consensusStatisticsThread = new Thread(this::getConsensusStatistics);

    private Thread consensusStatisticsTallyThread = new Thread(this::getConsensusStatisticsTally);

    List<ConsensusStatistics> consensusStatisticsList = new ArrayList<>();

    List<TallyStatistics> TallyconsensusStatisticsList = new ArrayList<>();


    private boolean isRunning = true;

    private final Object lock1 = new Object();

    private final Object lock2 = new Object();

    private final Object lock3 = new Object();

    @Autowired
    private JmsTemplate jmsTemplate;

    @PostConstruct
    public void init() {

        tallyResultNotificationThread.start();
        consensusStatisticsThread.start();
        consensusStatisticsTallyThread.start();
    }

    @PreDestroy
    public void finishing() {
        isRunning = false;
        tallyResultNotificationThread.interrupt();
        consensusStatisticsThread.interrupt();
        consensusStatisticsTallyThread.interrupt();
        try {
            tallyResultNotificationThread.join(1000000);
            consensusStatisticsThread.join(1000000);
            consensusStatisticsTallyThread.join(1000000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @JmsListener(destination = "${Tally.reply.vote.topicname}")
    public void receiveEndTally(String message) {
        try {
            LOGGER.info("'Election Manager' received End Tally message='{}'", message);
            synchronized (lock1) {
                lock1.notify();
            }
            transferQueueEndTally.transfer(message);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @JmsListener(destination = "${Consensus.statistics}")
    public void receiveConsensusStatistics(String message) {
        try {
            LOGGER.info("'Election Manager' received Consensus Statistics message='{}'", message);
            transferQueueStatistic.transfer(message);
//            synchronized (lock2) {
//                lock2.notify();
//            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @JmsListener(destination = "${Tally.statistics}")
    public void receiveConsensusStatisticsTally(String message) {
        try {
            LOGGER.info("'Election Manager' received Consensus Statistics Tally message='{}'", message);
            transferQueueStatisticTally.transfer(message);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void getTallyResult() {

        while (isRunning) {
            synchronized (lock1) {
                try {
                    LOGGER.info("Election manager  wait to get Tally Result...........");
                    lock1.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                String msg = transferQueueEndTally.take();
                LOGGER.info("Election manager get Tally Result message ='{}'", msg);
                synchronized (lock2) {
                    lock2.notify();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void getConsensusStatistics() {
        int receivedCount = 0;
        int totalTime = 0;
        Map<Integer, Integer> avgEachCandidateMap = new HashMap<>();

        while (isRunning) {
            synchronized (lock2) {
                try {
                    LOGGER.info("Election manager  wait to get Consensus Statistics Result...........");
                    lock2.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            while ((maxGenerateVotes / startconsensusvotecount) * candidatesCount != receivedCount) {
                try {
                    consensusStatisticsList.add(convertJsonToStatistic(transferQueueStatistic.take()));
                    receivedCount++;
                    LOGGER.info("'Election Manager' received Consensus Statistics number='{}'", receivedCount);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if ((maxGenerateVotes / startconsensusvotecount) * candidatesCount == receivedCount) {

                receivedCount = 0;

                //filename
                StringBuilder stringBuilderFilename = new StringBuilder();
                stringBuilderFilename.append("statistic_Result_C");
                stringBuilderFilename.append(candidatesCount);
                stringBuilderFilename.append("_V");
                stringBuilderFilename.append(maxGenerateVotes);
                stringBuilderFilename.append(".csv");

                //header
                StringBuilder stringBuilderHeader = new StringBuilder();
                stringBuilderHeader.append("ElectionId");
                stringBuilderHeader.append(",");
                stringBuilderHeader.append("CandidateId");
                stringBuilderHeader.append(",");
                stringBuilderHeader.append("ConsensusId");
                stringBuilderHeader.append(",");
                stringBuilderHeader.append("ConsensusDurationTime");
                stringBuilderHeader.append(",");
                stringBuilderHeader.append("VoteCount");
                stringBuilderHeader.append(",");
                stringBuilderHeader.append("ConsensusResult");

                writeStatisticDataToCSVFile(stringBuilderHeader, stringBuilderFilename);

                // compute consensus duration time  and add to csv file
                for (ConsensusStatistics cs : consensusStatisticsList) {

                    Duration t = Duration.between(cs.getStartTime(), cs.getEndTime());

                    StringBuilder data = new StringBuilder();
                    data.append(cs.getElectionId());
                    data.append(",");
                    data.append(cs.getCandidateId());
                    data.append(",");
                    data.append(cs.getConsensusId());
                    data.append(",");
                    data.append(t.toMillis());
                    data.append(",");
                    data.append(cs.getVotesCount());
                    data.append(",");
                    data.append(cs.isConsensusResult());

                    writeStatisticDataToCSVFile(data, stringBuilderFilename);

                    LOGGER.info("consensusStatistics data, candidateId '{}', Consensus Id '{}' inserted , consensus duration time is: '{}' ms",
                            cs.getCandidateId(), cs.getConsensusId(), t.toMillis());

                }
                //compute each candidate ,avg consensus time
                //each candidate consensus Avg data
                String line = "";
                String cvsSplitBy = ",";

                try (BufferedReader br = new BufferedReader(new FileReader(stringBuilderFilename.toString()))) {

                    String header = br.readLine(); //read first line

                    while ((line = br.readLine()) != null) {

                        // use comma as separator
                        String[] record = line.split(cvsSplitBy);

                        if (!avgEachCandidateMap.containsKey(Integer.parseInt(record[1]))) {
                            avgEachCandidateMap.put(Integer.parseInt(record[1]), Integer.parseInt(record[3]));
                        } else { //sum each consensus for each candidate
                            int c = avgEachCandidateMap.get(Integer.parseInt(record[1]));
                            avgEachCandidateMap.put(Integer.parseInt(record[1]), Integer.parseInt(record[3]) + c); //is sum

                        }
                    }

                    //header each candidate consensus Avg
                    StringBuilder stringBuilderHeaderEachCandidateAvg = new StringBuilder();
                    stringBuilderHeaderEachCandidateAvg.append("CandidateId");
                    stringBuilderHeaderEachCandidateAvg.append(",");
                    stringBuilderHeaderEachCandidateAvg.append("AvgConsensusTime");
                    stringBuilderHeaderEachCandidateAvg.append(",");
                    stringBuilderHeaderEachCandidateAvg.append("CandidateCount");
                    stringBuilderHeaderEachCandidateAvg.append(",");
                    stringBuilderHeaderEachCandidateAvg.append("VoteCount");

                    writeStatisticDataToCSVFile(stringBuilderHeaderEachCandidateAvg, stringBuilderFilename);

                    //is avg
                    for (int i = 1; i <= avgEachCandidateMap.size(); i++) {
                        int avg = (avgEachCandidateMap.get(i) / avgEachCandidateMap.size());
                        avgEachCandidateMap.put(i, avg);
                        StringBuilder dataAvg_eachCandidate = new StringBuilder();
                        dataAvg_eachCandidate.append(i);
                        dataAvg_eachCandidate.append(",");
                        dataAvg_eachCandidate.append((avg));
                        dataAvg_eachCandidate.append(",");
                        dataAvg_eachCandidate.append(candidatesCount);
                        dataAvg_eachCandidate.append(",");
                        dataAvg_eachCandidate.append(maxGenerateVotes);


                        writeStatisticDataToCSVFile(dataAvg_eachCandidate, stringBuilderFilename);

                        LOGGER.info("Consensus Time Avg, CandidateId '{}', is: '{}' ms", i, avg);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

                //compute total Avg
                //header total Avg
                StringBuilder stringBuilderHeaderTotalAvg = new StringBuilder();
                stringBuilderHeaderTotalAvg.append("VoteCount");
                stringBuilderHeaderTotalAvg.append(",");
                stringBuilderHeaderTotalAvg.append("TotalConsensusTime");
                stringBuilderHeaderTotalAvg.append(",");
                stringBuilderHeaderTotalAvg.append("CandidateCount");

                writeStatisticDataToCSVFile(stringBuilderHeaderTotalAvg, stringBuilderFilename);

                for (Integer x : avgEachCandidateMap.values()) {
                    totalTime += x;
                }

                StringBuilder dataAvg = new StringBuilder();
                dataAvg.append(maxGenerateVotes);
                dataAvg.append(",");
                dataAvg.append((totalTime / candidatesCount));
                dataAvg.append(",");
                dataAvg.append(candidatesCount);

                writeStatisticDataToCSVFile(dataAvg, stringBuilderFilename);
                LOGGER.info("Total Avg Time for '{}' candidates and '{}' votes is: '{}' ms",
                        candidatesCount, maxGenerateVotes, totalTime / candidatesCount);

                synchronized (lock3) {
                    lock3.notify();
                }
            }
        }
    }

    public void getConsensusStatisticsTally() {
        int receivedCount = 0;
        int totalTime = 0;

        while (isRunning) {
            synchronized (lock3) {
                try {
                    LOGGER.info("Election manager  wait to get Consensus Statistics Tally ...........");
                    lock3.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            while (candidatesCount != receivedCount) {
                try {
                    TallyconsensusStatisticsList.add(convertJsonToStatisticTally(transferQueueStatisticTally.take()));
                    receivedCount++;
                    LOGGER.info("'Election Manager' received Consensus Statistics Tally number='{}'", receivedCount);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (candidatesCount == receivedCount) {

                receivedCount = 0;

                //filename
                StringBuilder stringBuilderFilename = new StringBuilder();
                stringBuilderFilename.append("statistic_Result_Tally_C");
                stringBuilderFilename.append(candidatesCount);
                stringBuilderFilename.append("_V");
                stringBuilderFilename.append(maxGenerateVotes);
                stringBuilderFilename.append(".csv");

                //header
                StringBuilder stringBuilderHeader = new StringBuilder();
                stringBuilderHeader.append("ElectionId");
                stringBuilderHeader.append(",");
                stringBuilderHeader.append("CandidateId");
                stringBuilderHeader.append(",");
                stringBuilderHeader.append("TallyDurationTime");
                stringBuilderHeader.append(",");
                stringBuilderHeader.append("VoteCount");
                stringBuilderHeader.append(",");
                stringBuilderHeader.append("TallyConsensusResult");

                writeStatisticDataToCSVFile(stringBuilderHeader, stringBuilderFilename);

                // compute consensus duration time  and add to csv file
                for (TallyStatistics cs : TallyconsensusStatisticsList) {

                    Duration t = Duration.between(cs.getStartTime(), cs.getEndTime());

                    StringBuilder data = new StringBuilder();
                    data.append(cs.getElectionId());
                    data.append(",");
                    data.append(cs.getCandidateId());
                    data.append(",");
                    data.append(t.toMillis());
                    data.append(",");
                    data.append(cs.getVotesCount());
                    data.append(",");
                    data.append(cs.isConsensusTallyResult());

                    writeStatisticDataToCSVFile(data, stringBuilderFilename);

                    LOGGER.info("consensusStatisticsTally data, candidateId '{}', inserted , consensus duration time Tally  is: '{}' ms",
                            cs.getCandidateId(), t.toMillis());
                }

                //compute total Avg
                //header total Avg
                StringBuilder stringBuilderHeaderTotalAvg = new StringBuilder();
                stringBuilderHeaderTotalAvg.append("VoteCount");
                stringBuilderHeaderTotalAvg.append(",");
                stringBuilderHeaderTotalAvg.append("TotalConsensusTimeTally");
                stringBuilderHeaderTotalAvg.append(",");
                stringBuilderHeaderTotalAvg.append("CandidateCount");

                writeStatisticDataToCSVFile(stringBuilderHeaderTotalAvg, stringBuilderFilename);

                // compute consensus Tally duration time  and add to csv file
                for (TallyStatistics cs : TallyconsensusStatisticsList) {
                    Duration t = Duration.between(cs.getStartTime(), cs.getEndTime());
                    totalTime += t.toMillis();
                }

                StringBuilder dataAvg = new StringBuilder();
                dataAvg.append(maxGenerateVotes);
                dataAvg.append(",");
                dataAvg.append((totalTime / candidatesCount));
                dataAvg.append(",");
                dataAvg.append(candidatesCount);

                writeStatisticDataToCSVFile(dataAvg, stringBuilderFilename);
                LOGGER.info("Total Avg  Time for Tally by '{}' candidates and '{}' votes is: '{}' ms",
                        candidatesCount, maxGenerateVotes, totalTime / candidatesCount);

            }
        }
    }

    public void writeStatisticDataToCSVFile(StringBuilder data, StringBuilder filename) {

        try {
            FileWriter csvWriter = new FileWriter(filename.toString(), true);

            csvWriter.append(data);
            csvWriter.append("\n");

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

    public TallyStatistics convertJsonToStatisticTally(String jsonString) {

        if (jsonString != null) {
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            return gson.fromJson(jsonString, TallyStatistics.class);

        } else
            return null;
    }

    public Map<String, Integer> setBallotBoxOffset() {
        int ballotBoxId = 1;
        int offset = maxGenerateVotes / maxBallotBoxCount;
        for (int i = 1; i <= maxBallotBoxCount; i++) {

            if (i == 1)
                ballotBox.put(ballotBoxId++ + "", 1);
            else
                ballotBox.put(ballotBoxId++ + "", offset + ballotBox.get((i - 1) + ""));
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
