package bahri.jamileh.electionmanager;

import java.time.LocalDateTime;
import java.util.Objects;

public class TallyStatistics {

    private int electionId;

    private String candidateId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private int votesCount;

    private boolean consensusTallyResult;

    public TallyStatistics(int electionId, String candidateId,
                           LocalDateTime startTime, LocalDateTime endTime,
                           int votesCount, boolean consensusTallyResult) {
        this.electionId = electionId;
        this.candidateId = candidateId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.votesCount = votesCount;
        this.consensusTallyResult = consensusTallyResult;
    }

    public int getElectionId() {
        return electionId;
    }

    public void setElectionId(int electionId) {
        this.electionId = electionId;
    }

    public String getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(String candidateId) {
        this.candidateId = candidateId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public boolean isConsensusTallyResult() {
        return consensusTallyResult;
    }

    public void setConsensusTallyResult(boolean consensusTallyResult) {
        this.consensusTallyResult = consensusTallyResult;
    }

    public int getVotesCount() {
        return votesCount;
    }

    public void setVotesCount(int votesCount) {
        this.votesCount = votesCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TallyStatistics that = (TallyStatistics) o;
        return electionId == that.electionId &&
                votesCount == that.votesCount &&
                consensusTallyResult == that.consensusTallyResult &&
                Objects.equals(candidateId, that.candidateId) &&
                Objects.equals(startTime, that.startTime) &&
                Objects.equals(endTime, that.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(electionId, candidateId, startTime, endTime, votesCount, consensusTallyResult);
    }

    @Override
    public String toString() {
        return "TallyStatistics{" +
                "electionId=" + electionId +
                ", candidateId='" + candidateId + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", votesCount=" + votesCount +
                ", consensusTallyResult=" + consensusTallyResult +
                '}';
    }
}
