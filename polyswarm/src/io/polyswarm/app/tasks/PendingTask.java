/*
 * The MIT License
 *
 * Copyright 2020 PolySwarm PTE. LTD.
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
package io.polyswarm.app.tasks;

import io.polyswarm.app.PolySwarmController;
import io.polyswarm.app.PolySwarmModule;
import io.polyswarm.app.apiclient.BadRequestException;
import io.polyswarm.app.apiclient.RateLimitException;
import io.polyswarm.app.apiclient.v2.requests.utils.ArtifactInstance;
import io.polyswarm.app.apiclient.v2.requests.utils.Assertion;
import io.polyswarm.app.apiclient.v2.requests.utils.Tag;
import io.polyswarm.app.datamodel.PolySwarmDbException;
import io.polyswarm.app.datamodel.PolySwarmDb;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.ingest.IngestServices;
import org.sleuthkit.autopsy.ingest.ModuleDataEvent;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.BlackboardArtifact;
import org.sleuthkit.datamodel.BlackboardAttribute;
import org.sleuthkit.datamodel.TskCoreException;
import org.sleuthkit.datamodel.TskData;

/**
 * PendingTask abstract class with helper functions for updating blackboard.
 *
 * All children classes must implement `process(Case autopsyCase)`
 */
public abstract class PendingTask implements Cancellable {

    private static final Logger LOGGER = Logger.getLogger(PendingTask.class.getName());

    public abstract boolean process(Case autopsyCase) throws PolySwarmDbException, BadRequestException, RateLimitException, IOException, TskCoreException;

    @Override
    public abstract boolean cancel();

    public String getHumanReadableName() {
        return "Task";
    }

    public PolySwarmDb getDbInstance() throws PolySwarmDbException {
        return PolySwarmDb.getInstance();
    }

    /**
     * Adds a hash not found message to the artifact
     *
     * @param autopsyCase open case
     * @param abstractFileId id of the file in question
     */
    public static void updateNotFound(Case autopsyCase, Long abstractFileId) throws TskCoreException {
        AbstractFile abstractFile = autopsyCase.getSleuthkitCase().getAbstractFileById(abstractFileId);
        BlackboardArtifact artifact = getBlackboardArtifact(autopsyCase, abstractFile, PolySwarmController.POLYSWARM_ARTIFACT_TYPE_NAME);
        artifact.addAttribute(new BlackboardAttribute(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_COMMENT, PolySwarmModule.getModuleName(), "Not Found in PolySwarm"));
    }

    /**
     * Fills in all the blackboard fields under a new BlackboardArtifact. Also sets the known status of an AbstractFile
     *
     * Fills in the following attributes: PolyScore, Sha256, Assertions, Malware Families, Tags
     *
     * @param autopsyCase open case
     * @param abstractFileId id of the file in question
     * @param artifactInstance response from PolySwarm
     */
    public static void updateBlackboard(Case autopsyCase, Long abstractFileId, ArtifactInstance artifactInstance, List<Tag> tags) throws TskCoreException {
        AbstractFile abstractFile = autopsyCase.getSleuthkitCase().getAbstractFileById(abstractFileId);

        // Set file to known bad when polyscore > 0.7 and at least 2 malicious responses
        if (artifactInstance.detection.malicious >= 2 && Double.parseDouble(artifactInstance.polyscore) > 0.7d) {
            abstractFile.setKnown(TskData.FileKnown.BAD);
        }

        // Add all results attributes
        BlackboardArtifact artifact = getBlackboardArtifact(autopsyCase, abstractFile, PolySwarmController.POLYSWARM_ARTIFACT_TYPE_NAME);
        artifact.addAttribute(new BlackboardAttribute(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_HASH_SHA2_256, PolySwarmModule.getModuleName(), artifactInstance.sha256));
        addArtifactAttribute(autopsyCase, artifact, PolySwarmController.POLYSWARM_ARTIFACT_ATTRIBUTE_POLYSCORE_NAME, artifactInstance.polyscore);
        addArtifactAttribute(autopsyCase, artifact, PolySwarmController.POLYSWARM_ARTIFACT_ATTRIBUTE_MALICIOUS_DETECTIONS_NAME, artifactInstance.detection.malicious);
        addArtifactAttribute(autopsyCase, artifact, PolySwarmController.POLYSWARM_ARTIFACT_ATTRIBUTE_BENIGN_DETECTIONS_NAME, artifactInstance.detection.benign);
        addArtifactAttribute(autopsyCase, artifact, PolySwarmController.POLYSWARM_ARTIFACT_ATTRIBUTE_TOTAL_DETECTIONS_NAME, artifactInstance.detection.total);
        addArtifactAttribute(autopsyCase, artifact, PolySwarmController.POLYSWARM_ARTIFACT_ATTRIBUTE_FIRST_SEEN_NAME, artifactInstance.firstSeen);
        addArtifactAttribute(autopsyCase, artifact, PolySwarmController.POLYSWARM_ARTIFACT_ATTRIBUTE_LAST_SCANNED_NAME, artifactInstance.lastScanned);
        // notify UI to update and display this result

        for (Assertion assertion : artifactInstance.assertions) {
            if (assertion.mask) {
                addAssertion(autopsyCase, artifact, assertion);
            }
        }

        for (Assertion assertion : artifactInstance.assertions) {
            if (!assertion.malwareFamily.isEmpty()) {
                addArtifactAttribute(autopsyCase, artifact, PolySwarmController.POLYSWARM_ARTIFACT_ATTRIBUTE_MALWARE_FAMILY_NAME, assertion.malwareFamily);
            }
        }

        for (Tag tag : tags) {
            addArtifactAttribute(autopsyCase, artifact, PolySwarmController.POLYSWARM_ARTIFACT_ATTRIBUTE_TAG_NAME, tag.name);
        }

        IngestServices.getInstance().fireModuleDataEvent(new ModuleDataEvent(PolySwarmModule.getModuleName(),
                autopsyCase.getSleuthkitCase().getArtifactType(PolySwarmController.POLYSWARM_ARTIFACT_TYPE_NAME)));
    }

    /**
     *
     * Adds an assertion to the blackboard. Creates a new BlackboardAttribute.Type per assertion author
     *
     * @param autopsyCase open case
     * @param artifact BlackboardArtifact to add the result
     * @param assertion Assertion from PolySwarm
     */
    public static void addAssertion(Case autopsyCase, BlackboardArtifact artifact, Assertion assertion) throws TskCoreException {
        String attributeName = String.format(PolySwarmController.POLYSWARM_ARTIFACT_ATTRIBUTE_ASSERTION_NAME_FORMAT, assertion.name.toUpperCase());
        PolySwarmController.createCustomArtifactAttribute(autopsyCase, attributeName, BlackboardAttribute.TSK_BLACKBOARD_ATTRIBUTE_VALUE_TYPE.STRING, assertion.name);
        addArtifactAttribute(autopsyCase, artifact, attributeName, assertion.getHumanReadableVerdict());
    }

    /**
     * Creates a new BlackboardArtifact for tracking scan/lookup results.
     *
     * A new artifact is added per scan/lookup
     *
     * @param autopsyCase Open case
     * @param abstractFile File in in being updated
     * @param artifactName Name of the Artifact type
     *
     * @return BlackboardArtifact artifact to update so it shows all together
     */
    public static BlackboardArtifact getBlackboardArtifact(Case autopsyCase, AbstractFile abstractFile, String artifactName) throws TskCoreException {
        // New artifact per result
        return abstractFile.newArtifact(autopsyCase.getSleuthkitCase().getArtifactType(artifactName).getTypeID());
    }

    /**
     * Adds a String field to the blackboard
     *
     * @param autopsyCase open case
     * @param artifact BlackboardArtifact to add the result
     * @param attributeName String name of the attribute to add the data in
     * @param data String data to store in blackboard
     */
    public static void addArtifactAttribute(Case autopsyCase, BlackboardArtifact artifact, String attributeName, String data) throws TskCoreException {
        BlackboardAttribute.Type attributeType = autopsyCase.getSleuthkitCase().getAttributeType(attributeName);
        if (attributeType == null) {
            return;
        }

        artifact.addAttribute(new BlackboardAttribute(attributeType, PolySwarmModule.getModuleName(), data));
    }

    /**
     * Adds a double field to the blackboard
     *
     * @param autopsyCase open case
     * @param artifact BlackboardArtifact to add the result
     * @param attributeName String name of the attribute to add the data in
     * @param data double data to store in blackboard
     */
    public static void addArtifactAttribute(Case autopsyCase, BlackboardArtifact artifact, String attributeName, double data) throws TskCoreException {
        BlackboardAttribute.Type attributeType = autopsyCase.getSleuthkitCase().getAttributeType(attributeName);
        if (attributeType == null) {
            return;
        }

        artifact.addAttribute(new BlackboardAttribute(attributeType, PolySwarmModule.getModuleName(), data));
    }

    /**
     * Adds an int field to the blackboard
     *
     * @param autopsyCase open case
     * @param artifact BlackboardArtifact to add the result
     * @param attributeName String name of the attribute to add the data in
     * @param data int data to store in blackboard
     */
    public static void addArtifactAttribute(Case autopsyCase, BlackboardArtifact artifact, String attributeName, int data) throws TskCoreException {
        BlackboardAttribute.Type attributeType = autopsyCase.getSleuthkitCase().getAttributeType(attributeName);
        if (attributeType == null) {
            return;
        }

        artifact.addAttribute(new BlackboardAttribute(attributeType, PolySwarmModule.getModuleName(), data));
    }

    @NbBundle.Messages({"PendingTask.populatingDb.status=Processing %s."})
    public ProgressHandle getPendingTaskProgressHandle() {
        return ProgressHandle.createHandle(String.format(io.polyswarm.app.tasks.Bundle.PendingTask_populatingDb_status(), getHumanReadableName()), this);
    }
}
