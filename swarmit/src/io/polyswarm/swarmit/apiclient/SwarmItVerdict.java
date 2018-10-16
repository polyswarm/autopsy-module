/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.polyswarm.swarmit.apiclient;
/**
 *
 * POJO that holds all aspects of the PolySwarm verdict
 */
public class SwarmItVerdict {
    final private SwarmItVerdictEnum assertionsVerdict;
    final private SwarmItVerdictEnum votesVerdict;
    final private SwarmItVerdictEnum quorumVerdict;
    
    public SwarmItVerdict(SwarmItVerdictEnum assertion, SwarmItVerdictEnum votes, SwarmItVerdictEnum quorum) {
        assertionsVerdict = assertion;
        votesVerdict = votes;
        quorumVerdict = quorum;
    }
    
    public static SwarmItVerdict errorFactory() {
        return new SwarmItVerdict(SwarmItVerdictEnum.ERROR, SwarmItVerdictEnum.ERROR, SwarmItVerdictEnum.ERROR);
    }
    
    public static SwarmItVerdict unknownFactory() {
        return new SwarmItVerdict(SwarmItVerdictEnum.UNKNOWN, SwarmItVerdictEnum.UNKNOWN, SwarmItVerdictEnum.UNKNOWN);
    }
    
    public SwarmItVerdictEnum getAssertionsVerdict() {
        return assertionsVerdict;
    }
    
    public SwarmItVerdictEnum getVotesVerdict() {
        return votesVerdict;
    }
    
    public SwarmItVerdictEnum getQuorumVerdict() {
        return quorumVerdict;
    }
}
