package bahri.jamileh.electionmanager;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


public class BallotBoxGenesis {

     private int electionId;

     private int candidateChoose;

     private Map<String, Integer> ballotBox;

     private int maxGenerateVotes;

     private LocalDateTime issuedTime;

     private LocalDateTime persistTime;

     private Set<Integer> candidates;

    public BallotBoxGenesis() {
    }

    public BallotBoxGenesis(int electionId, int candidateChoose, Map<String, Integer> ballotBox,
                            int maxGenerateVotes, LocalDateTime issuedTime, LocalDateTime persistTime , Set<Integer> candidates) {
        this.electionId = electionId;
        this.candidateChoose = candidateChoose;
        this.ballotBox = ballotBox;
        this.maxGenerateVotes = maxGenerateVotes;
        this.issuedTime = issuedTime;
        this.persistTime = persistTime;
        this.candidates = candidates;
    }


    public int getMaxGenerateVotes() {
        return maxGenerateVotes;
    }

    public int getElectionId() {
        return electionId;
    }

    public int getCandidateChoose() {
        return candidateChoose;
    }

    public Map<String, Integer> getBallotBox() {
        return ballotBox;
    }

    public LocalDateTime getIssuedTime() {
        return issuedTime;
    }

    public LocalDateTime getPersistTime() {
        return persistTime;
    }

    public Set<Integer> getCandidates() {
        return candidates;
    }

    public void setElectionId(int electionId) {
        this.electionId = electionId;
    }

    public void setCandidateChoose(int candidateChoose) {
        this.candidateChoose = candidateChoose;
    }

    public void setBallotBox(Map<String, Integer> ballotBox) {
        this.ballotBox = ballotBox;
    }

    public void setMaxGenerateVotes(int maxGenerateVotes) {
        this.maxGenerateVotes = maxGenerateVotes;
    }

    public void setIssuedTime(LocalDateTime issuedTime) {
        this.issuedTime = issuedTime;
    }

    public void setPersistTime(LocalDateTime persistTime) {
        this.persistTime = persistTime;
    }

    public void setCandidates(Set<Integer> candidates) {
        this.candidates = candidates;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BallotBoxGenesis candidateGenesis = (BallotBoxGenesis) o;
        return getElectionId() == candidateGenesis.getElectionId();
    }

    @Override
    public int hashCode() {

        return Objects.hash(getElectionId());
    }

    @Override
    public String toString() {
        return "Genesis{" +
                ", electionId=" + electionId +
                ", candidateChoose=" + candidateChoose +
                ", candidates=" + ballotBox +
                ", issuedTime=" + issuedTime +
                ", persistTime=" + persistTime +
                '}';
    }
}
