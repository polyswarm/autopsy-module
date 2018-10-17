/*
 * The MIT License
 *
 * Copyright 2018 PolySwarm PTE. LTD.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
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
    final private String bountyGuid;
    final private String fileHash;

    public SwarmItVerdict(String guid, String hash, SwarmItVerdictEnum assertion, SwarmItVerdictEnum votes, SwarmItVerdictEnum quorum) {
        assertionsVerdict = assertion;
        votesVerdict = votes;
        quorumVerdict = quorum;
        bountyGuid = guid;
        fileHash = hash;
    }

    public static SwarmItVerdict errorFactory() {
        return new SwarmItVerdict(null, null, SwarmItVerdictEnum.ERROR, SwarmItVerdictEnum.ERROR, SwarmItVerdictEnum.ERROR);
    }

    public static SwarmItVerdict unknownFactory() {
        return new SwarmItVerdict(null, null, SwarmItVerdictEnum.UNKNOWN, SwarmItVerdictEnum.UNKNOWN, SwarmItVerdictEnum.UNKNOWN);
    }

    public SwarmItVerdictEnum getAssertionsVerdict() {
        return assertionsVerdict;
    }

    public String getBountyGuid() {
        return bountyGuid;
    }

    public String getFileHash() {
        return fileHash;
    }

    public SwarmItVerdictEnum getQuorumVerdict() {
        return quorumVerdict;
    }

    public SwarmItVerdictEnum getVotesVerdict() {
        return votesVerdict;
    }
}
