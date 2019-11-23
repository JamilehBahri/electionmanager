package bahri.jamileh.electionmanager;

import java.util.Objects;
import java.util.Set;

public class CandidateGenesis {

     private int electionId;

     private int candidateChoose;

     private int masterCandidateId;

     private int minParticipants;

     private int maxParticipants;

     private int startconsensusvotecount;

     private Set<Integer> candidates;

    private int maxGenerateVotes;


    public CandidateGenesis(int electionId, int candidateChoose, int masterCandidateId,
                            int minParticipants, int maxParticipants, int startconsensusvotecount,
                            Set<Integer> candidates , int maxGenerateVotes) {
        this.electionId = electionId;
        this.candidateChoose = candidateChoose;
        this.masterCandidateId = masterCandidateId;
        this.minParticipants = minParticipants;
        this.maxParticipants = maxParticipants;
        this.startconsensusvotecount = startconsensusvotecount;
        this.candidates = candidates;
        this.maxGenerateVotes = maxGenerateVotes;
    }

    public int getElectionId() {
        return electionId;
    }

    public int getCandidateChoose() {
        return candidateChoose;
    }

    public int getMasterCandidateId() {
        return masterCandidateId;
    }

    public int getMinParticipants() {
        return minParticipants;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public int getStartconsensusvotecount() {
        return startconsensusvotecount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CandidateGenesis candidateGenesis = (CandidateGenesis) o;
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
                ", masterCandidateId='" + masterCandidateId + '\'' +
                ", minParticipants=" + minParticipants +
                ", maxParticipants=" + maxParticipants +
                '}';
    }
}
