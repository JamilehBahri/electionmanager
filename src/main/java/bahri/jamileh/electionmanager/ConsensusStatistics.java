package bahri.jamileh.electionmanager;

import java.time.LocalDateTime;
import java.util.Objects;

//public class ConsensusStatistics extends AbstractDomain {
public class ConsensusStatistics {

    private int electionId;

    private int consensusId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private int votesCount;

    public ConsensusStatistics(long id, int electionId, int consensusId, LocalDateTime startTime,
                               LocalDateTime endTime, int votesCount) {
//        super(id);
        this.electionId = electionId;
        this.consensusId = consensusId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.votesCount = votesCount;
    }

    public ConsensusStatistics(int electionId, int consensusId, LocalDateTime startTime,
                               LocalDateTime endTime, int votesCount) {
        this.electionId = electionId;
        this.consensusId = consensusId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.votesCount = votesCount;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConsensusStatistics that = (ConsensusStatistics) o;
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
//                "id=" + getId() +
                ", electionId=" + electionId +
                ", consensusId=" + consensusId +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", votesCount=" + votesCount +
                '}';
    }
}
