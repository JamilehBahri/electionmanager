package bahri.jamileh.electionmanager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ElectionmanagerApplicationTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElectionmanagerApplicationTests.class);

    ConsensusStatistics consensusStatistics1;
    ConsensusStatistics consensusStatistics2;
    ConsensusStatistics consensusStatistics3;
    ConsensusStatistics consensusStatistics4;
    ConsensusStatistics consensusStatistics5;
    ConsensusStatistics consensusStatistics6;
    List<ConsensusStatistics> consensusStatisticsList = new ArrayList<>();
    Map<Integer,Integer> statisticsMap = new HashMap<>();

    @Before
    public void init(){
        consensusStatistics1 = new ConsensusStatistics(1 , "1",1, LocalDateTime.now(),
                                        LocalDateTime.now(),5,true);
        consensusStatistics2 = new ConsensusStatistics(1 ,"1",2, LocalDateTime.now(),
                LocalDateTime.now(),5,true);
        consensusStatistics3 = new ConsensusStatistics(1 ,"2",1, LocalDateTime.now(),
                LocalDateTime.now(),5,true);
        consensusStatistics4 = new ConsensusStatistics(1 ,"2",2, LocalDateTime.now(),
                LocalDateTime.now(),5,true);
        consensusStatistics5 = new ConsensusStatistics(1 ,"3",1, LocalDateTime.now(),
                LocalDateTime.now(),5,true);
        consensusStatistics6 = new ConsensusStatistics(1 ,"3",2, LocalDateTime.now(),
                LocalDateTime.now(),5,true);

        consensusStatisticsList.add(consensusStatistics1);
        consensusStatisticsList.add(consensusStatistics2);
        consensusStatisticsList.add(consensusStatistics3);
        consensusStatisticsList.add(consensusStatistics4);
        consensusStatisticsList.add(consensusStatistics5);
        consensusStatisticsList.add(consensusStatistics6);


    }
    @Test
    public void testCSV_VoteCount5() {
        // compute consensus time duration and add to csv file
        for ( ConsensusStatistics cs : consensusStatisticsList){

            if(!statisticsMap.containsKey(cs.getConsensusId())){
                //TODO: check diff time
                int t = cs.getEndTime().getNano() - cs.getStartTime().getNano();
                statisticsMap.put(cs.getConsensusId(),t);
                LOGGER.info("consensus Id '{}' insert to map, consensus duration is: '{}'", cs.getConsensusId() , t);
            }else { //sum each consensus for each candidate
                int c =statisticsMap.get(cs.getConsensusId());
                //TODO: check diff time
                int t = cs.getEndTime().getNano() - cs.getStartTime().getNano();
                statisticsMap.put(cs.getConsensusId(),c+t); //is sum
                LOGGER.info("consensus Id '{}' update in map, consensus duration is: '{}', updated value is: '{}",
                        cs.getConsensusId() , t , c+t);
            }
        } // is avg
        for (int i=1 ; i<= statisticsMap.size() ;i++) {
            int avg = (statisticsMap.get(i) / statisticsMap.size());
            statisticsMap.put(i, avg);

        }

        writeMapToCSVFile(statisticsMap);
    }


    public void writeMapToCSVFile(Map<Integer,Integer> map){

    }
}
