package bahri.jamileh.electionmanager;

import java.time.LocalDateTime;
import java.util.Objects;

//public class ConsensusStatistics extends AbstractDomain {
public class CSVStatisticsModel {

    private int electionId;

    private String candidateId;

    private int consensusId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private int votesCount;

    private boolean consensusResult;


    public CSVStatisticsModel(int electionId, String candidateId, int consensusId, LocalDateTime startTime,
                              LocalDateTime endTime, int votesCount, boolean consensusResult) {
        this.electionId = electionId;
        this.candidateId = candidateId;
        this.consensusId = consensusId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.votesCount = votesCount;
        this.consensusResult = consensusResult;
    }

    public int getElectionId() {
        return electionId;
    }

    public int getConsensusId() {
        return consensusId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public int getVotesCount() {
        return votesCount;
    }

    public void setElectionId(int electionId) {
        this.electionId = electionId;
    }

    public void setConsensusId(int consensusId) {
        this.consensusId = consensusId;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void setVotesCount(int votesCount) {
        this.votesCount = votesCount;
    }

    public boolean isConsensusResult() {
        return consensusResult;
    }

    public void setConsensusResult(boolean consensusResult) {
        this.consensusResult = consensusResult;
    }

    public String getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(String candidateId) {
        this.candidateId = candidateId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CSVStatisticsModel that = (CSVStatisticsModel) o;
        return getElectionId() == that.getElectionId() &&
                getConsensusId() == that.getConsensusId();
    }

    @Override
    public int hashCode() {

        return Objects.hash(getElectionId(), getConsensusId());
    }

    @Override
    public String toString() {
        return "ConsensusStatistics{" +
                "electionId=" + electionId +
                ", candidateId='" + candidateId + '\'' +
                ", consensusId=" + consensusId +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", votesCount=" + votesCount +
                ", consensusResult=" + consensusResult +
                '}';
    }
}
